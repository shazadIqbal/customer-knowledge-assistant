package com.example.auth.service;

import com.example.auth.connector.GitHubConnector;
import com.example.auth.dto.FetchedItem;
import com.example.auth.ingestion.GitHubIngestionProperties;
import com.example.auth.ingestion.model.GitHubIngestionBatch;
import com.example.auth.ingestion.model.GitHubPdfFile;
import com.example.auth.ingestion.model.GitHubTextFile;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recursively loads text files from a GitHub repository path using the Contents API.
 * Binary and vendor/build directories are skipped; see {@link GitHubIngestionProperties} for limits.
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    private static final Pattern REPOS_PATH = Pattern.compile("https://api\\.github\\.com/repos/([^/]+)/([^/]+)(?:/contents)?");

    private static final Set<String> SKIPPED_DIR_NAMES = Set.of(
            ".git", "node_modules", "target", "build", "dist", ".idea", ".vscode", "__pycache__", ".gradle"
    );

    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "ico", "bmp", "tif", "tiff", "heic", "heif",
            "zip", "gz", "tar", "7z", "rar", "iso", "dmg",
            "jar", "war", "ear", "class", "pyc", "pyo", "o", "a", "lib", "so", "dylib", "dll", "exe",
            "mp3", "mp4", "webm", "avi", "mov", "mkv", "wmv", "flac", "ogg",
            "woff", "woff2", "ttf", "eot", "otf",
            "sqlite", "db", "bin", "dat", "p12", "pfx", "key", "pem", "crt", "der"
    );

    private final GitHubConnector gitHubConnector;
    private final GitHubIngestionProperties properties;

    public GitHubService(GitHubConnector gitHubConnector, GitHubIngestionProperties properties) {
        this.gitHubConnector = gitHubConnector;
        this.properties = properties;
    }

    /**
     * Fetches all ingestible text files and PDFs under the given folder URL, recursively.
     * PDFs are returned as raw bytes; use {@code PagePdfDocumentReader} in the document layer to chunk/embed.
     */
    public GitHubIngestionBatch fetchForIngestion(String apiKey, String folderUrl) {
        String baseApi = gitHubConnector.toContentsApiUrl(folderUrl);
        String repoPageUrl = toGitHubWebRepoUrl(baseApi);
        log.info("GitHubService: starting recursive fetch from base API path {}", baseApi);

        JsonNode root = gitHubConnector.fetchContentsJsonNode(apiKey, baseApi);
        List<GitHubTextFile> textOut = new ArrayList<>();
        List<GitHubPdfFile> pdfOut = new ArrayList<>();
        AtomicInteger filesLoaded = new AtomicInteger(0);
        processNode(apiKey, root, repoPageUrl, textOut, pdfOut, 0, filesLoaded);
        log.info("GitHubService: finished — {} text file(s), {} PDF file(s) loaded", textOut.size(), pdfOut.size());
        return new GitHubIngestionBatch(textOut, pdfOut);
    }

    /**
     * Walks the repository tree (same paths as vector ingestion) and returns every file as
     * {@link FetchedItem} with {@code name = repo-relative path} and {@code url = html_url}.
     * No file bodies are downloaded — used by the connect API so the response matches a full recursive listing.
     */
    public List<FetchedItem> listRepositoryFilesForConnect(String apiKey, String folderUrl) {
        String baseApi = gitHubConnector.toContentsApiUrl(folderUrl);
        log.info("GitHubService: recursive connect listing from {}", baseApi);
        JsonNode root = gitHubConnector.fetchContentsJsonNode(apiKey, baseApi);
        List<FetchedItem> out = new ArrayList<>();
        AtomicInteger fileCount = new AtomicInteger(0);
        walkForConnectListing(apiKey, root, out, 0, fileCount);
        log.info("GitHubService: connect listing — {} file reference(s) (recursive)", out.size());
        return out;
    }

    private void walkForConnectListing(
            String token,
            JsonNode node,
            List<FetchedItem> out,
            int depth,
            AtomicInteger fileCount
    ) {
        if (fileCount.get() >= properties.getMaxFiles()) {
            log.warn("GitHubService (connect): max file limit {} reached", properties.getMaxFiles());
            return;
        }
        if (depth > properties.getMaxDepth()) {
            return;
        }
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject() && node.has("message") && !node.has("name")) {
            throw new IllegalStateException("GitHub API: " + node.path("message").asText("error"));
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (!item.isObject()) {
                    continue;
                }
                if (item.has("message") && !item.has("name")) {
                    throw new IllegalStateException("GitHub API: " + item.path("message").asText());
                }
                String type = item.path("type").asText("");
                String name = item.path("name").asText("");
                if ("dir".equals(type)) {
                    if (shouldSkipDir(name)) {
                        continue;
                    }
                    JsonNode sub = gitHubConnector.fetchContentsJsonNode(token, item.get("url").asText());
                    walkForConnectListing(token, sub, out, depth + 1, fileCount);
                } else if ("file".equals(type)) {
                    String path = item.path("path").asText(name);
                    String htmlUrl = item.path("html_url").asText("");
                    out.add(new FetchedItem(path, htmlUrl));
                    fileCount.incrementAndGet();
                }
            }
            return;
        }

        if (node.isObject() && "file".equals(node.path("type").asText())) {
            String path = node.path("path").asText("");
            out.add(new FetchedItem(path, node.path("html_url").asText("")));
            fileCount.incrementAndGet();
        }
    }

    private void processNode(
            String token,
            JsonNode node,
            String repoPageUrl,
            List<GitHubTextFile> textOut,
            List<GitHubPdfFile> pdfOut,
            int depth,
            AtomicInteger filesLoaded
    ) {
        if (filesLoaded.get() >= properties.getMaxFiles()) {
            log.warn("GitHubService: max file limit {} reached, stopping scan", properties.getMaxFiles());
            return;
        }
        if (depth > properties.getMaxDepth()) {
            log.debug("GitHubService: max depth {} reached", properties.getMaxDepth());
            return;
        }
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject() && node.has("message") && !node.has("name")) {
            String msg = node.path("message").asText("GitHub API error");
            throw new IllegalStateException("GitHub API: " + msg);
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (!item.isObject()) {
                    continue;
                }
                if (item.has("message") && !item.has("name")) {
                    throw new IllegalStateException("GitHub API: " + item.path("message").asText());
                }
                String type = item.path("type").asText("");
                String name = item.path("name").asText("");

                if ("dir".equals(type)) {
                    if (shouldSkipDir(name)) {
                        log.debug("GitHubService: skip directory name={}", name);
                        continue;
                    }
                    String nextUrl = item.get("url").asText();
                    JsonNode sub = gitHubConnector.fetchContentsJsonNode(token, nextUrl);
                    processNode(token, sub, repoPageUrl, textOut, pdfOut, depth + 1, filesLoaded);
                } else if ("file".equals(type)) {
                    String path = item.path("path").asText(name);
                    if (shouldSkipByExtension(path) && !isPdfPath(path)) {
                        log.debug("GitHubService: skip binary/extension path={}", path);
                        continue;
                    }
                    long size = item.path("size").asLong(0L);
                    if (size > properties.getMaxFileSizeBytes()) {
                        log.debug("GitHubService: skip large file {} bytes path={}", size, path);
                        continue;
                    }
                    String fileApiUrl = item.get("url").asText();
                    String htmlUrl = item.path("html_url").asText("");

                    if (isPdfPath(path)) {
                        readPdfFile(token, item, fileApiUrl, size, path, repoPageUrl, htmlUrl)
                                .ifPresent(pdf -> {
                                    pdfOut.add(pdf);
                                    filesLoaded.incrementAndGet();
                                });
                    } else {
                        Optional<String> text = readFileText(token, item, fileApiUrl, size);
                        text.filter(s -> !s.isBlank()).ifPresent(s -> {
                            textOut.add(new GitHubTextFile(path, s, repoPageUrl, htmlUrl));
                            filesLoaded.incrementAndGet();
                        });
                    }
                } else {
                    log.debug("GitHubService: skip non-file/dir type={} name={}", type, name);
                }
            }
            return;
        }

        if (node.isObject() && "file".equals(node.path("type").asText())) {
            String path = node.path("path").asText("");
            if (shouldSkipByExtension(path) && !isPdfPath(path)) {
                return;
            }
            long size = node.path("size").asLong(0L);
            if (size > properties.getMaxFileSizeBytes()) {
                return;
            }
            String fileApiUrl = node.get("url").asText();
            String htmlUrl = node.path("html_url").asText("");
            if (isPdfPath(path)) {
                readPdfFile(token, node, fileApiUrl, size, path, repoPageUrl, htmlUrl)
                        .ifPresent(pdf -> {
                            pdfOut.add(pdf);
                            filesLoaded.incrementAndGet();
                        });
            } else {
                readFileText(token, node, fileApiUrl, size)
                        .filter(s -> !s.isBlank())
                        .ifPresent(s -> {
                            textOut.add(new GitHubTextFile(path, s, repoPageUrl, htmlUrl));
                            filesLoaded.incrementAndGet();
                        });
            }
        }
    }

    private static boolean isPdfPath(String path) {
        if (path == null) {
            return false;
        }
        int dot = path.lastIndexOf('.');
        return dot >= 0 && path.substring(dot + 1).equalsIgnoreCase("pdf");
    }

    private static boolean looksLikePdf(byte[] data) {
        if (data == null || data.length < 5) {
            return false;
        }
        // PDF header: %PDF-
        return data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F' && data[4] == '-';
    }

    private Optional<GitHubPdfFile> readPdfFile(
            String token,
            JsonNode fileMeta,
            String fileApiUrl,
            long sizeHint,
            String path,
            String repoPageUrl,
            String htmlUrl
    ) {
        try {
            Optional<byte[]> bytesOpt = readFileBytesRaw(token, fileMeta, fileApiUrl, sizeHint);
            if (bytesOpt.isEmpty()) {
                return Optional.empty();
            }
            byte[] raw = bytesOpt.get();
            if (raw.length > properties.getMaxFileSizeBytes()) {
                log.debug("GitHubService: skip oversize PDF path={} bytes={}", path, raw.length);
                return Optional.empty();
            }
            if (!looksLikePdf(raw)) {
                log.warn("GitHubService: not a valid PDF (missing %PDF- header), path={}", path);
                return Optional.empty();
            }
            log.debug("GitHubService: loaded PDF path={} size={} bytes", path, raw.length);
            return Optional.of(new GitHubPdfFile(path, repoPageUrl, htmlUrl, raw));
        } catch (Exception e) {
            log.warn("GitHubService: could not read PDF at {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<byte[]> readFileBytesRaw(String token, JsonNode fileMeta, String fileApiUrl, long sizeHint) {
        try {
            if (fileMeta.has("content") && "base64".equalsIgnoreCase(fileMeta.path("encoding").asText())) {
                String b64 = fileMeta.get("content").asText("").replaceAll("\\s", "");
                byte[] raw = Base64.getDecoder().decode(b64);
                return Optional.of(raw);
            }
            if (sizeHint > properties.getMaxFileSizeBytes()) {
                return Optional.empty();
            }
            return Optional.of(gitHubConnector.fetchRawFile(token, fileApiUrl));
        } catch (Exception e) {
            log.warn("GitHubService: raw bytes read failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> readFileText(String token, JsonNode fileMeta, String fileApiUrl, long sizeHint) {
        try {
            if (fileMeta.has("content") && "base64".equalsIgnoreCase(fileMeta.path("encoding").asText())) {
                String b64 = fileMeta.get("content").asText("").replaceAll("\\s", "");
                byte[] raw = Base64.getDecoder().decode(b64);
                if (raw.length > properties.getMaxFileSizeBytes()) {
                    return Optional.empty();
                }
                return toUtf8Text(raw);
            }
            if (sizeHint > properties.getMaxFileSizeBytes()) {
                return Optional.empty();
            }
            byte[] raw = gitHubConnector.fetchRawFile(token, fileApiUrl);
            return toUtf8Text(raw);
        } catch (Exception e) {
            log.warn("GitHubService: could not read file at {}: {}", fileApiUrl, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> toUtf8Text(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return Optional.of("");
        }
        if (isLikelyBinary(raw)) {
            return Optional.empty();
        }
        return Optional.of(new String(raw, java.nio.charset.StandardCharsets.UTF_8));
    }

    private boolean isLikelyBinary(byte[] data) {
        int sample = Math.min(data.length, 8_000);
        int nuls = 0;
        for (int i = 0; i < sample; i++) {
            if (data[i] == 0) {
                nuls++;
            }
        }
        if (nuls > 0) {
            return true;
        }
        int suspicious = 0;
        for (int i = 0; i < sample; i++) {
            int b = data[i] & 0xff;
            if (b < 7 || (b > 13 && b < 32 && b != 9 && b != 10 && b != 11 && b != 12)) {
                suspicious++;
            }
        }
        return suspicious > sample * 0.3;
    }

    private static boolean shouldSkipDir(String name) {
        return name != null && SKIPPED_DIR_NAMES.contains(name);
    }

    private boolean shouldSkipByExtension(String path) {
        if (path == null || path.isEmpty() || !path.contains(".")) {
            return false;
        }
        int last = path.lastIndexOf('.');
        if (last < 0 || last >= path.length() - 1) {
            return false;
        }
        String ext = path.substring(last + 1).toLowerCase();
        if (ext.length() > 10) {
            return true;
        }
        return BINARY_EXTENSIONS.contains(ext);
    }

    static String toGitHubWebRepoUrl(String contentsApiUrl) {
        if (contentsApiUrl == null) {
            return "https://github.com";
        }
        Matcher m = REPOS_PATH.matcher(contentsApiUrl);
        if (m.find()) {
            return "https://github.com/" + m.group(1) + "/" + m.group(2);
        }
        return "https://github.com";
    }
}
