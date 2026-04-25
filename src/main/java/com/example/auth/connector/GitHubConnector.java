package com.example.auth.connector;

import com.example.auth.dto.FetchedItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.IDN;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches repository file/folder info from the GitHub REST API (contents endpoint).
 * <p>
 * Accepts either:
 * <ul>
 *   <li>API URL: {@code https://api.github.com/repos/{owner}/{repo}/contents/...} (optional ?ref=branch)</li>
 *   <li>Web URL: {@code https://github.com/{owner}/{repo}} or /tree/branch/path (normalized to the API above)</li>
 * </ul>
 * Browser URLs are normalized because calling {@code https://github.com/...} returns HTML, not JSON.
 */
@Component
public class GitHubConnector implements DatasourceConnector {

    private static final Logger log = LoggerFactory.getLogger(GitHubConnector.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubConnector(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getType() {
        return "GITHUB";
    }

    @Override
    public List<FetchedItem> fetch(String apiKey, String folderUrl) {
        String apiUrl = resolveToContentsApiUrl(folderUrl);
        log.info("Fetching GitHub (resolved API URL): {}", apiUrl);

        ResponseEntity<String> response = exchangeGet(apiKey, apiUrl);

        String body = response.getBody();
        if (body == null || body.isBlank()) {
            return List.of();
        }

        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null && contentType.includes(MediaType.TEXT_HTML)) {
            throw new IllegalStateException(
                    "Received HTML instead of JSON. Use a GitHub API URL (https://api.github.com/repos/owner/repo/contents) "
                            + "or a standard https://github.com/owner/repo web URL; avoid redirects to login pages. Resolved URL: "
                            + apiUrl
            );
        }

        return parseContentsJson(body, folderUrl, apiUrl);
    }

    /**
     * Resolves a browser or API folder URL to the GitHub contents REST endpoint (same as internal resolution).
     */
    public String toContentsApiUrl(String folderUrl) {
        return resolveToContentsApiUrl(folderUrl);
    }

    /**
     * GET JSON from a GitHub contents / metadata URL (array for directory, object for a single file's metadata or content).
     */
    public JsonNode fetchContentsJsonNode(String apiKey, String apiUrl) {
        ResponseEntity<String> response = exchangeGet(apiKey, apiUrl);
        String body = response.getBody();
        if (body == null || body.isBlank()) {
            return objectMapper.createArrayNode();
        }
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null && contentType.includes(MediaType.TEXT_HTML)) {
            throw new IllegalStateException("Expected JSON from GitHub but received HTML for URL: " + apiUrl);
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JSON from GitHub API: " + apiUrl, e);
        }
    }

    /**
     * Downloads raw file bytes for a {@code .../contents/{path}} file API URL.
     * Works for private repositories when authenticated (uses GitHub raw media type).
     */
    public byte[] fetchRawFile(String apiKey, String fileContentsApiUrl) {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer " + apiKey);
        h.set("Accept", "application/vnd.github.raw");
        h.set("X-GitHub-Api-Version", "2022-11-28");
        try {
            ResponseEntity<byte[]> r = restTemplate.exchange(
                    fileContentsApiUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(h),
                    byte[].class
            );
            return r.getBody() != null ? r.getBody() : new byte[0];
        } catch (RestClientException e) {
            log.warn("GitHub raw file request failed: {}", e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<String> exchangeGet(String apiKey, String apiUrl) {
        try {
            return restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(githubHeaders(apiKey)),
                    String.class
            );
        } catch (RestClientException e) {
            log.warn("GitHub API request failed: {}", e.getMessage());
            throw e;
        }
    }

    private static HttpHeaders githubHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    /**
     * Converts github.com web URLs to the contents API. Leaves api.github.com URLs valid for the contents endpoint.
     */
    String resolveToContentsApiUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("folderUrl is required for GitHub");
        }
        String u = raw.trim();
        URI uri;
        try {
            uri = URI.create(u);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid folderUrl: " + raw, e);
        }

        String host = uri.getHost() != null ? IDN.toUnicode(uri.getHost()) : null;
        if (host == null) {
            throw new IllegalArgumentException("Invalid folderUrl (no host): " + raw);
        }

        if (host.equalsIgnoreCase("api.github.com")) {
            return ensureContentsPath(u);
        }

        if (!host.equalsIgnoreCase("github.com") && !host.equalsIgnoreCase("www.github.com")) {
            log.warn("Unknown GitHub host '{}'; using URL as-is (must be a valid GitHub API contents URL)", host);
            return u;
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty() || path.equals("/")) {
            throw new IllegalArgumentException("Invalid GitHub URL: need /owner/repo in path: " + raw);
        }

        String[] segs = path.split("/");
        List<String> parts = new ArrayList<>();
        for (String s : segs) {
            if (s != null && !s.isEmpty()) {
                parts.add(s);
            }
        }
        if (parts.size() < 2) {
            throw new IllegalArgumentException("Invalid GitHub URL: expected at least owner and repo: " + raw);
        }

        String owner = parts.get(0);
        String repo = parts.get(1);
        String ref = null;
        StringBuilder subPath = new StringBuilder();

        if (parts.size() > 2) {
            String marker = parts.get(2);
            if (("tree".equals(marker) || "blob".equals(marker)) && parts.size() > 3) {
                ref = parts.get(3);
                for (int i = 4; i < parts.size(); i++) {
                    if (subPath.length() > 0) {
                        subPath.append('/');
                    }
                    subPath.append(parts.get(i));
                }
            } else {
                for (int i = 2; i < parts.size(); i++) {
                    if (subPath.length() > 0) {
                        subPath.append('/');
                    }
                    subPath.append(parts.get(i));
                }
            }
        }

        StringBuilder api = new StringBuilder("https://api.github.com/repos/");
        api.append(encodeSegment(owner)).append('/').append(encodeSegment(repo)).append("/contents");
        if (subPath.length() > 0) {
            api.append('/').append(UriUtils.encodePath(subPath.toString(), StandardCharsets.UTF_8));
        }
        if (ref != null) {
            api.append("?ref=").append(URLEncoder.encode(ref, StandardCharsets.UTF_8));
        }
        return api.toString();
    }

    private static String encodeSegment(String s) {
        return UriUtils.encodePathSegment(s, StandardCharsets.UTF_8);
    }

    private static String ensureContentsPath(String apiUrl) {
        String t = apiUrl.split("\\?")[0].replaceAll("/+$", "");
        if (t.contains("/contents")) {
            return apiUrl;
        }
        if (t.matches("https?://api\\.github\\.com/repos/[^/]+/[^/]+")) {
            return t + "/contents" + (apiUrl.contains("?") ? apiUrl.substring(apiUrl.indexOf('?')) : "");
        }
        return apiUrl;
    }

    private List<FetchedItem> parseContentsJson(String body, String originalUrl, String apiUrl) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JSON from GitHub API for " + apiUrl + ": " + e.getMessage(), e);
        }

        if (root.isObject() && root.has("message") && !root.has("name")) {
            String msg = root.get("message").asText("GitHub API error");
            String doc = root.has("documentation_url") ? root.get("documentation_url").asText() : "";
            throw new IllegalStateException("GitHub API: " + msg + (doc.isEmpty() ? "" : " (" + doc + ")"));
        }

        List<FetchedItem> items = new ArrayList<>();

        if (root.isArray()) {
            for (JsonNode node : root) {
                addIfEntry(node, items);
            }
        } else if (root.isObject()) {
            addIfEntry(root, items);
        }

        if (items.isEmpty() && !root.isMissingNode()) {
            log.debug("No directory/file entries parsed from response for URL {}", originalUrl);
        }
        log.info("Fetched {} GitHub entries", items.size());
        return items;
    }

    private void addIfEntry(JsonNode node, List<FetchedItem> items) {
        if (node == null || !node.isObject() || !node.has("name")) {
            return;
        }
        String name = node.get("name").asText("");
        String html = node.path("html_url").asText("");
        items.add(new FetchedItem(name, html));
    }
}
