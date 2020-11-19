package net.securustech.ews.data.elasticsearch.rest.extension.entity;

import java.util.HashMap;
import java.util.Map;

public class EsDocumentBuilder {
    private Map<String, Object> builder = new HashMap<>();

    public void add(String key, Object value) {
        builder.put(key, value);
    }

    public void add(Map<String, Object> map) {
        builder.putAll(map);
    }

    public Map<String, Object> build() {
        return builder;
    }
}
