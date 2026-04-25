package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DatasourceConnectResponse {

    private String status;
    private String message;
    private List<FetchedItem> data;

    public static DatasourceConnectResponse success(List<FetchedItem> data) {
        DatasourceConnectResponse response = new DatasourceConnectResponse();
        response.setStatus("CONNECTED");
        response.setMessage("Datasource connected successfully");
        response.setData(data);
        return response;
    }

    public static DatasourceConnectResponse failure(String errorMessage) {
        DatasourceConnectResponse response = new DatasourceConnectResponse();
        response.setStatus("FAILED");
        response.setMessage(errorMessage);
        return response;
    }
}
