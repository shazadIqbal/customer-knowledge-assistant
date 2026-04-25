package com.example.auth.connector;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the correct {@link DatasourceConnector} implementation
 * for a given datasource type name at runtime (Strategy selector).
 *
 * Spring auto-injects all DatasourceConnector beans; the factory
 * indexes them by their declared type string.
 */
@Component
public class DatasourceConnectorFactory {

    private final Map<String, DatasourceConnector> connectorMap;

    public DatasourceConnectorFactory(List<DatasourceConnector> connectors) {
        this.connectorMap = connectors.stream()
                .collect(Collectors.toMap(
                        c -> c.getType().toUpperCase(),
                        Function.identity()
                ));
    }

    /**
     * Returns the connector for the given datasource type.
     *
     * @param datasourceType value of Datasource.name (case-insensitive)
     * @throws IllegalArgumentException if no connector is registered for the type
     */
    public DatasourceConnector getConnector(String datasourceType) {
        String key = datasourceType.trim().toUpperCase();
        DatasourceConnector connector = connectorMap.get(key);
        if (connector == null) {
            throw new IllegalArgumentException(
                    "No connector registered for datasource type: " + datasourceType
                            + ". Supported types: " + connectorMap.keySet()
            );
        }
        return connector;
    }
}
