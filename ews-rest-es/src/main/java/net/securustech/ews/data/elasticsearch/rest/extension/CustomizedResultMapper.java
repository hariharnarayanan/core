package net.securustech.ews.data.elasticsearch.rest.extension;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.ScriptedField;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomizedResultMapper extends DefaultResultMapper {
    private MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CustomizedResultMapper() {
        super();
    }

    public CustomizedResultMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        super(mappingContext);
        this.mappingContext = mappingContext;
    }

    public CustomizedResultMapper(EntityMapper entityMapper) {
        super(entityMapper);
    }

    public CustomizedResultMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext, EntityMapper entityMapper) {
        super(entityMapper);
        this.mappingContext = mappingContext;
    }

    @Override
    public <T> T mapResult(GetResponse getResponse, Class<T> clazz) {
        if (!getResponse.isExists())
            return null;

        if (clazz == Map.class) {
            return (T) getResponse.getSourceAsMap();
        } else if (clazz == String.class) {
            return (T) getResponse.getSourceAsString();
        } else if (this.mappingContext.hasPersistentEntityFor(clazz)) {
            try {
                T result = this.objectMapper.readValue(getResponse.getSourceAsBytes(), clazz);
                setPersistentEntityId(result, getResponse.getId(), clazz);
                return result;
            } catch (Exception ex) {
                throw new ElasticsearchException("Error in mapResult", ex);
            }
        } else
            throw new ElasticsearchException("Not supported type to map result");
    }

    private void addMetaInfo(Map<String, Object> hitMap, SearchHit hit) {
        hitMap.put("_index", hit.getIndex());
        hitMap.put("_type", hit.getType());
        hitMap.put("_id", hit.getId());
    }


    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        long totalHits = response.getHits().getTotalHits();
        List<T> results = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                T result = null;
                if (clazz == Map.class) {
                    Map<String, Object> hitMap =  hit.getSourceAsMap();
                    addMetaInfo(hitMap, hit);
                    result = (T) hitMap;
                }
                else {
                    if (StringUtils.isNotBlank(hit.getSourceAsString())) {
                        result = mapEntity(hit.getSourceAsString(), clazz);
                    } else {
                        result = mapEntity(hit.getFields().values(), clazz);
                    }
                    setPersistentEntityId(result, hit.getId(), clazz);
                    populateScriptFields(result, hit);
                }
                results.add(result);
            }
        }

        return new AggregatedPageImpl<T>(results, pageable, totalHits, response.getAggregations());
    }

    private <T> T mapEntity(Collection<DocumentField> values, Class<T> clazz) {
        return mapEntity(buildJSONFromFields(values), clazz);
    }

    private String buildJSONFromFields(Collection<DocumentField> values) {
        JsonFactory nodeFactory = new JsonFactory();
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
            generator.writeStartObject();
            for (DocumentField value : values) {
                if (value.getValues().size() > 1) {
                    generator.writeArrayFieldStart(value.getName());
                    for (Object val : value.getValues()) {
                        generator.writeObject(val);
                    }
                    generator.writeEndArray();
                } else {
                    generator.writeObjectField(value.getName(), value.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();
            return new String(stream.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            return null;
        }
    }

    private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {

        if (mappingContext != null && clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(clazz);
            ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();
            PersistentPropertyAccessor propertyAccessor = persistentEntity.getPropertyAccessor(result);

            // Only deal with String because ES generated Ids are strings !
            if (idProperty != null && propertyAccessor.getProperty(idProperty) == null && idProperty.getType().isAssignableFrom(String.class)) {
                persistentEntity.getPropertyAccessor(result).setProperty(idProperty, id);
            }

        }

    }

    private <T> void populateScriptFields(T result, SearchHit hit) {
        if (hit.getFields() != null && !hit.getFields().isEmpty() && result != null) {
            for (java.lang.reflect.Field field : result.getClass().getDeclaredFields()) {
                ScriptedField scriptedField = field.getAnnotation(ScriptedField.class);
                if (scriptedField != null) {
                    String name = scriptedField.name().isEmpty() ? field.getName() : scriptedField.name();
                    DocumentField documentField = hit.getFields().get(name);
                    if (documentField != null) {
                        field.setAccessible(true);
                        try {
                            field.set(result, documentField.getValue());
                        } catch (IllegalArgumentException e) {
                            throw new org.springframework.data.elasticsearch.ElasticsearchException("failed to set scripted field: " + name + " with value: "
                                    + documentField.getValue(), e);
                        } catch (IllegalAccessException e) {
                            throw new org.springframework.data.elasticsearch.ElasticsearchException("failed to access scripted field: " + name, e);
                        }
                    }
                }
            }
        }
    }
}
