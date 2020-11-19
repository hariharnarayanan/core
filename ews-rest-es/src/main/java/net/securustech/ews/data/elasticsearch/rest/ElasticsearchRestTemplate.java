package net.securustech.ews.data.elasticsearch.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.ews.data.elasticsearch.rest.extension.entity.EsDocumentCommon;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.util.CloseableIterator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ElasticsearchRestTemplate implements ElasticsearchOperations, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchRestTemplate.class);
    public static final String DOC = "_doc";

    private RestHighLevelClient client;
    private ElasticsearchConverter elasticsearchConverter;
    private ResultsMapper resultsMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RestClient restClient;

    public ElasticsearchRestTemplate(RestHighLevelClient client) {
        this(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()));
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, EntityMapper entityMapper) {
        this(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()), entityMapper);
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter elasticsearchConverter, EntityMapper entityMapper) {
        this(client, elasticsearchConverter, new DefaultResultMapper(elasticsearchConverter.getMappingContext(), entityMapper));
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, ResultsMapper resultsMapper) {
        this(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()), resultsMapper);
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter elasticsearchConverter) {
        this(client, elasticsearchConverter, new DefaultResultMapper(elasticsearchConverter.getMappingContext()));
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter elasticsearchConverter, ResultsMapper resultsMapper) {
        Assert.notNull(client, "Client must not be null!");
        Assert.notNull(elasticsearchConverter, "ElasticsearchConverter must not be null!");
        Assert.notNull(resultsMapper, "ResultsMapper must not be null!");
        this.client = client;
        this.elasticsearchConverter = elasticsearchConverter;
        this.resultsMapper = resultsMapper;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (elasticsearchConverter instanceof ApplicationContextAware) {
            ((ApplicationContextAware) elasticsearchConverter).setApplicationContext(applicationContext);
        }
    }

    @Override
    public ElasticsearchConverter getElasticsearchConverter() {
        return this.elasticsearchConverter;
    }

    @Override
    public Client getClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean createIndex(Class<T> aClass) {
        return false;
    }

    @Override
    public boolean createIndex(String s) {
        return false;
    }

    @Override
    public boolean createIndex(String s, Object o) {
        return false;
    }

    @Override
    public <T> boolean createIndex(Class<T> aClass, Object o) {
        return false;
    }

    @Override
    public <T> boolean putMapping(Class<T> aClass) {
        return false;
    }

    @Override
    public boolean putMapping(String s, String s1, Object o) {
        return false;
    }

    @Override
    public <T> boolean putMapping(Class<T> aClass, Object o) {
        return false;
    }

    @Override
    public <T> Map getMapping(Class<T> aClass) {
        return null;
    }

    @Override
    public Map getMapping(String s, String s1) {
        return null;
    }

    @Override
    public Map getSetting(String s) {
        return null;
    }

    @Override
    public <T> Map getSetting(Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T queryForObject(GetQuery getQuery, Class<T> aClass) {
        return this.queryForObject(getQuery, aClass, this.resultsMapper);
    }

    public <T> T queryForObject(String index, String type, String id, Class<T> aClass) {
        GetRequest request = new GetRequest(
                index,
                type,
                id
        );
        try {
            GetResponse response = this.client.get(request, RequestOptions.DEFAULT);
            T entity = this.resultsMapper.mapResult(response, aClass);
            return entity;
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in queryForObject", ex);
        }
    }

    @Override
    public <T> T queryForObject(GetQuery getQuery, Class<T> aClass, GetResultMapper getResultMapper) {
        ElasticsearchPersistentEntity persistentEntity = this.getPersistentEntityFor(aClass);
        GetRequest request = new GetRequest(
                persistentEntity.getIndexName(),
                persistentEntity.getIndexType(),
                getQuery.getId()
        );
        try {
            GetResponse response = this.client.get(request, RequestOptions.DEFAULT);
            T entity = getResultMapper.mapResult(response, aClass);
            return entity;
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in queryForObject", ex);
        }
    }

    @Override
    public <T> T queryForObject(CriteriaQuery criteriaQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T queryForObject(StringQuery stringQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> Page<T> queryForPage(SearchQuery searchQuery, Class<T> aClass) {
        return this.queryForPage((SearchQuery) searchQuery, aClass, this.resultsMapper);
    }

    @Override
    public <T> Page<T> queryForPage(SearchQuery searchQuery, Class<T> aClass, SearchResultMapper searchResultMapper) {
        try {
            SearchResponse response = this.doSearch(this.prepareSearch(searchQuery, aClass), searchQuery);
            return searchResultMapper.mapResults(response, aClass, searchQuery.getPageable());
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in queryForPage", ex);
        }
    }

    @Override
    public <T> Page<T> queryForPage(CriteriaQuery criteriaQuery, Class<T> clazz) {
//        criteriaQuery.addCriteria((new Criteria("docTypeNm")).is(this.getPersistentEntityFor(clazz).getIndexType()));
        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
        QueryBuilder elasticsearchFilter = new CriteriaFilterProcessor()
                .createFilterFromCriteria(criteriaQuery.getCriteria());


        String[] indexName = !CollectionUtils.isEmpty(criteriaQuery.getIndices()) ? (String[]) ((String[]) criteriaQuery.getIndices().toArray(new String[criteriaQuery.getIndices().size()])) : new String[]{this.retrieveIndexNameFromPersistentEntity(clazz)};
        Assert.notNull(indexName, "No index defined for Query");

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.types(DOC);

        SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();

        if (criteriaQuery.getPageable() !=null && criteriaQuery.getPageable().getPageSize()>0) {
            searchRequestBuilder.size(criteriaQuery.getPageable().getPageSize());
        }

        if (elasticsearchQuery != null) {
            searchRequestBuilder.query(elasticsearchQuery);
        } else {
            searchRequestBuilder.query(QueryBuilders.matchAllQuery());
        }

        if (elasticsearchFilter != null) {
            searchRequestBuilder.postFilter(elasticsearchFilter);
        }

        searchRequest.source(searchRequestBuilder);

        try {
            SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
            return this.resultsMapper.mapResults(searchResponse, clazz, criteriaQuery.getPageable());
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in count", ex);
        }
    }

    @Override
    public <T> Page<T> queryForPage(StringQuery stringQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> Page<T> queryForPage(StringQuery stringQuery, Class<T> aClass, SearchResultMapper searchResultMapper) {
        return null;
    }

    private void setPersistentEntityIndexAndType(Query query, Class clazz) {
        if (query.getIndices().isEmpty()) {
            query.addIndices(this.retrieveIndexNameFromPersistentEntity(clazz));
        }

        query.addTypes(DOC);
    }

    private static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return (String[]) values.toArray(valuesAsArray);
    }

    private <T> SearchSourceBuilder prepareSearch(Query query, Class<T> clazz) {
        this.setPersistentEntityIndexAndType(query, clazz);
        return this.prepareSearch(query);
    }

    private SearchSourceBuilder prepareSearch(Query query) {
        Assert.notNull(query.getIndices(), "No index defined for Query");
        Assert.notNull(query.getTypes(), "No type defined for Query");
        int startRecord = 0;
        SearchSourceBuilder searchRequestBuilder = new SearchSourceBuilder();
        if (query.getSourceFilter() != null) {
            SourceFilter sourceFilter = query.getSourceFilter();
            searchRequestBuilder.fetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            searchRequestBuilder.size(query.getPageable().getPageSize());
        }

        searchRequestBuilder.from(startRecord);
        if (!query.getFields().isEmpty()) {
            searchRequestBuilder.fetchSource(toArray(query.getFields()), (String[]) null);
        }

        if (query.getSort() != null) {
            Iterator sourceFilter1 = query.getSort().iterator();

            while (sourceFilter1.hasNext()) {
                Sort.Order order = (Sort.Order) sourceFilter1.next();
                searchRequestBuilder.sort(order.getProperty(), order.getDirection() == Sort.Direction.DESC ? SortOrder.DESC : SortOrder.ASC);
            }
        }

        if (query.getMinScore() > 0.0F) {
            searchRequestBuilder.minScore(query.getMinScore());
        }

        return searchRequestBuilder;
    }

    private SearchResponse doSearch(SearchSourceBuilder searchSourceBuilder, SearchQuery searchQuery) throws IOException {
        SearchRequest searchRequest = new SearchRequest()
                .indices(toArray(searchQuery.getIndices()))
                .searchType(searchQuery.getSearchType())
                .types(toArray(searchQuery.getTypes()));

        if (searchQuery.getFilter() != null) {
            searchSourceBuilder.postFilter(searchQuery.getFilter());
        }

        Iterator var3;
        if (!CollectionUtils.isEmpty(searchQuery.getElasticsearchSorts())) {
            var3 = searchQuery.getElasticsearchSorts().iterator();

            while (var3.hasNext()) {
                SortBuilder aggregatedFacet = (SortBuilder) var3.next();
                searchSourceBuilder.sort(aggregatedFacet);
            }
        }

        if (!searchQuery.getScriptFields().isEmpty()) {
            var3 = searchQuery.getScriptFields().iterator();

            while (var3.hasNext()) {
                ScriptField var8 = (ScriptField) var3.next();
                searchSourceBuilder.scriptField(var8.fieldName(), var8.script());
            }
        }

        if (searchQuery.getHighlightFields() != null) {
            Field[] var7 = searchQuery.getHighlightFields();
            int var9 = var7.length;

            for (int var5 = 0; var5 < var9; ++var5) {
                Field highlightField = var7[var5];
                searchSourceBuilder.highlighter((new HighlightBuilder()).field(highlightField));
            }
        }

        if (!CollectionUtils.isEmpty(searchQuery.getIndicesBoost())) {
            var3 = searchQuery.getIndicesBoost().iterator();

            while (var3.hasNext()) {
                IndexBoost var10 = (IndexBoost) var3.next();
                searchSourceBuilder.indexBoost(var10.getIndexName(), var10.getBoost());
            }
        }

        if (!CollectionUtils.isEmpty(searchQuery.getAggregations())) {
            var3 = searchQuery.getAggregations().iterator();

            while (var3.hasNext()) {
                AbstractAggregationBuilder var11 = (AbstractAggregationBuilder) var3.next();
                searchSourceBuilder.aggregation(var11);
            }
        }

        searchSourceBuilder.query(searchQuery.getQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }

    @Override
    public <T> CloseableIterator<T> stream(CriteriaQuery criteriaQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> CloseableIterator<T> stream(SearchQuery searchQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> CloseableIterator<T> stream(SearchQuery searchQuery, Class<T> aClass, SearchResultMapper searchResultMapper) {
        return null;
    }

    @Override
    public <T> List<T> queryForList(CriteriaQuery criteriaQuery, Class<T> aClass) {
        return queryForPage(criteriaQuery, aClass).getContent();
    }

    @Override
    public <T> List<T> queryForList(StringQuery stringQuery, Class<T> aClass) {
        return queryForPage(stringQuery, aClass).getContent();
    }

    @Override
    public <T> List<T> queryForList(SearchQuery searchQuery, Class<T> aClass) {
        return queryForPage(searchQuery, aClass).getContent();
    }

    @Override
    public <T> List<String> queryForIds(SearchQuery searchQuery) {
        return null;
    }

    @Override
    public <T> long count(CriteriaQuery criteriaQuery, Class<T> clazz) {
        criteriaQuery.addCriteria(
                new Criteria(EsDocumentCommon.DOC_TYPE_NAME).is(
                        this.getPersistentEntityFor(clazz).getIndexType()
                )
        );
        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
        QueryBuilder elasticsearchFilter = new CriteriaFilterProcessor().createFilterFromCriteria(criteriaQuery.getCriteria());

        if (elasticsearchFilter == null) {
            return doCount(criteriaQuery, clazz, elasticsearchQuery);
        } else {
            // filter could not be set into CountRequestBuilder, convert request into search request
            return doCount(criteriaQuery, clazz, elasticsearchQuery, elasticsearchFilter);
        }
    }

    @Override
    public <T> long count(CriteriaQuery criteriaQuery) {
        return count(criteriaQuery, null);
    }

    @Override
    public <T> long count(SearchQuery searchQuery, Class<T> clazz) {
        QueryBuilder elasticsearchQuery = searchQuery.getQuery();
        QueryBuilder elasticsearchFilter = searchQuery.getFilter();
        return elasticsearchFilter == null ? this.doCount(searchQuery, clazz, elasticsearchQuery) : this.doCount(searchQuery, clazz, elasticsearchQuery, elasticsearchFilter);
    }

    @Override
    public <T> long count(SearchQuery searchQuery) {
        return count(searchQuery, null);
    }

    private <T> long doCount(Query query, Class<T> clazz, QueryBuilder elasticsearchQuery) {
        String[] indexName = !CollectionUtils.isEmpty(query.getIndices()) ? (String[]) query.getIndices().toArray(new String[query.getIndices().size()]) : new String[]{this.retrieveIndexNameFromPersistentEntity(clazz)};
        Assert.notNull(indexName, "No index defined for Query");

        SearchRequest countRequest = new SearchRequest();
        countRequest.indices(indexName);
        countRequest.types(DOC);

        SearchSourceBuilder countRequestBuilder = new SearchSourceBuilder();
        countRequestBuilder.size(0);

        if (elasticsearchQuery != null) {
            countRequestBuilder.query(elasticsearchQuery);
        }

        countRequest.source(countRequestBuilder);
        try {
            SearchResponse searchResponse = this.client.search(countRequest, RequestOptions.DEFAULT);
            return searchResponse.getHits().getTotalHits();
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in count", ex);
        }
    }

    private <T> long doCount(Query query, Class<T> clazz, QueryBuilder elasticsearchQuery, QueryBuilder elasticsearchFilter) {
        String[] indexName = !CollectionUtils.isEmpty(query.getIndices()) ? (String[]) query.getIndices().toArray(new String[query.getIndices().size()]) : new String[]{this.retrieveIndexNameFromPersistentEntity(clazz)};
        Assert.notNull(indexName, "No index defined for Query");

        SearchRequest countRequest = new SearchRequest();
        countRequest.indices(indexName);
        countRequest.types(DOC);

        SearchSourceBuilder countRequestBuilder = new SearchSourceBuilder();
        countRequestBuilder.size(0);

        if (elasticsearchQuery != null) {
            countRequestBuilder.query(elasticsearchQuery);
        } else {
            countRequestBuilder.query(QueryBuilders.matchAllQuery());
        }

        if (elasticsearchFilter != null) {
            countRequestBuilder.postFilter(elasticsearchFilter);
        }

        countRequest.source(countRequestBuilder);
        try {
            SearchResponse searchResponse = this.client.search(countRequest, RequestOptions.DEFAULT);
            return searchResponse.getHits().getTotalHits();
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in count", ex);
        }
    }

    @Override
    public <T> LinkedList<T> multiGet(SearchQuery searchQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> LinkedList<T> multiGet(SearchQuery searchQuery, Class<T> aClass, MultiGetResultMapper multiGetResultMapper) {
        return null;
    }

    private String retrieveIndexNameFromPersistentEntity(Class clazz) {
        return clazz != null ? this.getPersistentEntityFor(clazz).getIndexName() : null;
    }

    private String retrieveTypeFromPersistentEntity(Class clazz) {
        return clazz != null ? this.getPersistentEntityFor(clazz).getIndexType() : null;
    }

    @Override
    public String index(IndexQuery indexQuery) {
        String index = StringUtils.isBlank(indexQuery.getIndexName()) ? this.retrieveIndexNameFromPersistentEntity(indexQuery.getObject().getClass()) : indexQuery.getIndexName();
        String type = StringUtils.isBlank(indexQuery.getType()) ? this.retrieveTypeFromPersistentEntity(indexQuery.getObject().getClass()) : indexQuery.getType();

        IndexRequest request = new IndexRequest(
                index,
                type,
                indexQuery.getId()
        );
        try {
            String jsonString = indexQuery.getObject() instanceof String ? (String) indexQuery.getObject() : objectMapper.writeValueAsString(indexQuery.getObject());
            request.source(jsonString, XContentType.JSON);
            IndexResponse response = this.client.index(request, RequestOptions.DEFAULT);
            return response.getId();
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in index", ex);
        }
    }

    @Override
    public void bulkIndex(List<IndexQuery> indexQueries) {
        Assert.notNull(indexQueries, "Cannot insert \'null\' as a List.");
        Assert.notEmpty(indexQueries, "Cannot insert empty List.");

        String index = StringUtils.isBlank(indexQueries.get(0).getIndexName()) ? this.retrieveIndexNameFromPersistentEntity(indexQueries.get(0).getObject().getClass()) : indexQueries.get(0).getIndexName();
        String type = StringUtils.isBlank(indexQueries.get(0).getType()) ? this.retrieveTypeFromPersistentEntity(indexQueries.get(0).getObject().getClass()) : indexQueries.get(0).getType();

        BulkRequest bulkRequest = new BulkRequest();

        indexQueries.stream().forEach(indexQuery -> {
            IndexRequest indexRequest = new IndexRequest(index, type, indexQuery.getId());
            try {
                String jsonString = indexQuery.getObject() instanceof String ? (String) indexQuery.getObject() : objectMapper.writeValueAsString(indexQuery.getObject());
                indexRequest.source(jsonString, XContentType.JSON);
            } catch (Exception ex) {
                throw new ElasticsearchException("Failed in bulkIndex when building bulk request", ex);
            }
            bulkRequest.add(indexRequest);
        });

        try {
            BulkResponse bulkResponse = this.client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures())
                throw new ElasticsearchException("Failed in bulkIndex");
        } catch (IOException ex) {
            throw new ElasticsearchException("Failed in bulkIndex when sending bulk request", ex);
        }
    }

    public BulkResponse bulkIndexWithResponse(List<IndexQuery> indexQueries) {
        Assert.notNull(indexQueries, "Cannot insert \'null\' as a List.");
        Assert.notEmpty(indexQueries, "Cannot insert empty List.");

        String index = StringUtils.isBlank(indexQueries.get(0).getIndexName()) ? this.retrieveIndexNameFromPersistentEntity(indexQueries.get(0).getObject().getClass()) : indexQueries.get(0).getIndexName();
        String type = StringUtils.isBlank(indexQueries.get(0).getType()) ? this.retrieveTypeFromPersistentEntity(indexQueries.get(0).getObject().getClass()) : indexQueries.get(0).getType();

        BulkRequest bulkRequest = new BulkRequest();

        indexQueries.stream().forEach(indexQuery -> {
            IndexRequest indexRequest = new IndexRequest(index, type, indexQuery.getId());
            try {
                String jsonString = indexQuery.getObject() instanceof String ? (String) indexQuery.getObject() : objectMapper.writeValueAsString(indexQuery.getObject());
                indexRequest.source(jsonString, XContentType.JSON);
            } catch (Exception ex) {
                throw new ElasticsearchException("Failed in bulkIndex when building bulk request", ex);
            }
            bulkRequest.add(indexRequest);
        });

        try {
            BulkResponse bulkResponse = this.client.bulk(bulkRequest, RequestOptions.DEFAULT);
            return bulkResponse;
        } catch (IOException ex) {
            throw new ElasticsearchException("Failed in bulkIndex when sending bulk request", ex);
        }
    }

    @Override
    public void bulkUpdate(List<UpdateQuery> updateQueryList) {
        try {
            BulkRequest request = new BulkRequest();
            updateQueryList.forEach(updateQuery -> request.add(prepareUpdate(updateQuery)));

            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);

//            if (bulkResponse.hasFailures()) {
//                bulkResponse.forEach(response -> logger.error("Error during bulk update to Elasticsearch ::: ID :-> " + response.getId() + " ::: Error Message:-> " + response.getFailureMessage()));
//                throw new ElasticsearchException("Failed during bulk update");
//            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticsearchException(e.getMessage());
        }

    }

    @Override
    public UpdateResponse update(UpdateQuery updateQuery) {
        try {
            UpdateResponse updateResponse = client.update(this.prepareUpdate(updateQuery), RequestOptions.DEFAULT);
            return updateResponse;
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in update", ex);
        }
    }

    private UpdateRequest prepareUpdate(UpdateQuery updateQuery) {
        UpdateRequest updateRequest = updateQuery.getUpdateRequest();
        if (!(updateRequest instanceof UpdateRequest)) {
            String indexName = StringUtils.isBlank(updateQuery.getIndexName()) ? this.getPersistentEntityFor(updateQuery.getClazz()).getIndexName() : updateQuery.getIndexName();
            String type = StringUtils.isBlank(updateQuery.getType()) ? this.getPersistentEntityFor(updateQuery.getClazz()).getIndexType() : updateQuery.getType();
            String id = updateQuery.getId();
            Assert.notNull(indexName, "No index defined for Query");
            Assert.notNull(type, "No type define for Query");
            Assert.notNull(id, "No Id define for Query");
            Assert.notNull(updateQuery.getUpdateRequest(), "No IndexRequest define for Query");
            Assert.isTrue(!updateQuery.getUpdateRequest().scriptedUpsert(), "Script upsert is not supported");

            try {
                Map<String, Object> payLoadMap = updateRequest.doc().sourceAsMap();
                updateRequest = new UpdateRequest(indexName, type, id);
                updateRequest.doc(payLoadMap);
            } catch (Exception ex) {
                throw new ElasticsearchException("Failed in prepareUpdate", ex);
            }
        }
        return updateRequest;
    }

    @Override
    public String delete(String index, String type, String id) {
        DeleteRequest request = new DeleteRequest(index, type, id);
        try {
            DeleteResponse response = this.client.delete(request, RequestOptions.DEFAULT);
            return response.getId();
        } catch (Exception ex) {
            throw new ElasticsearchException("Failed in delete", ex);
        }
    }

    @Override
    public <T> void delete(CriteriaQuery criteriaQuery, Class<T> aClass) {

    }

    @Override
    public <T> String delete(Class<T> aClass, String s) {
        return null;
    }

    @Override
    public <T> void delete(DeleteQuery deleteQuery, Class<T> aClass) {

    }

    @Override
    public void delete(DeleteQuery deleteQuery) {

    }

    @Override
    public <T> boolean deleteIndex(Class<T> aClass) {
        return false;
    }

    @Override
    public boolean deleteIndex(String s) {
        return false;
    }

    @Override
    public <T> boolean indexExists(Class<T> aClass) {
        return false;
    }

    @Override
    public boolean indexExists(String s) {
        return false;
    }

    @Override
    public boolean typeExists(String s, String s1) {
        return false;
    }

    private String generateEndpoint(String uri, String endpoint) {
        return (uri.startsWith("/") ? "" : "/") + uri + "/" + endpoint;
    }

    @Override
    public void refresh(String index) {
        Assert.isTrue(StringUtils.isNotEmpty(index), "index should not be null or empty");


        try {
            String endpoint = generateEndpoint(index, "_refresh");
            Request request = new Request("POST", endpoint);
            restClient.performRequest(request);
        } catch(IOException ex) {
            throw new ElasticsearchException("Failed in refresh", ex);
        }
    }

    @Override
    public <T> void refresh(Class<T> aClass) {
        refresh(getPersistentEntityFor(aClass).getIndexName());
    }

    @Override
    public <T> Page<T> startScroll(long l, SearchQuery searchQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> Page<T> startScroll(long l, SearchQuery searchQuery, Class<T> aClass, SearchResultMapper searchResultMapper) {
        return null;
    }

    @Override
    public <T> Page<T> startScroll(long l, CriteriaQuery criteriaQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> Page<T> startScroll(long l, CriteriaQuery criteriaQuery, Class<T> aClass, SearchResultMapper searchResultMapper) {
        return null;
    }

    @Override
    public <T> Page<T> continueScroll(String s, long l, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> Page<T> continueScroll(String s, long l, Class<T> aClass, SearchResultMapper searchResultMapper) {
        return null;
    }

    @Override
    public <T> void clearScroll(String s) {

    }

    @Override
    public <T> Page<T> moreLikeThis(MoreLikeThisQuery moreLikeThisQuery, Class<T> aClass) {
        return null;
    }

    @Override
    public Boolean addAlias(AliasQuery aliasQuery) {
        return null;
    }

    @Override
    public Boolean removeAlias(AliasQuery aliasQuery) {
        return null;
    }

    @Override
    public List<AliasMetaData> queryForAlias(String s) {
        return null;
    }

    @Override
    public <T> T query(SearchQuery searchQuery, ResultsExtractor<T> resultsExtractor) {
        return null;
    }

    @Override
    public ElasticsearchPersistentEntity getPersistentEntityFor(Class clazz) {
        Assert.isTrue(clazz.isAnnotationPresent(Document.class), "Unable to identify index name. " + clazz.getSimpleName()
                + " is not a Document. Make sure the document class is annotated with @Document(indexName=\"foo\")");
        return (ElasticsearchPersistentEntity<Object>) elasticsearchConverter.getMappingContext().getPersistentEntity(clazz);
    }
}
