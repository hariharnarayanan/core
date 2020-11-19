package net.securustech.ews.util.jmx;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

class DataConverter {

    private final ObjectMapper objectMapper;

    private final JavaType listObject;

    private final JavaType mapStringObject;

    DataConverter(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper == null ? new ObjectMapper() : objectMapper);
        this.listObject = this.objectMapper.getTypeFactory()
                .constructParametricType(List.class, Object.class);
        this.mapStringObject = this.objectMapper.getTypeFactory()
                .constructParametricType(Map.class, String.class, Object.class);

    }

    public Object convert(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof String) {
            return data;
        }
        if (data.getClass().isArray() || data instanceof List) {
            return this.objectMapper.convertValue(data, this.listObject);
        }
        return this.objectMapper.convertValue(data, this.mapStringObject);
    }

}
