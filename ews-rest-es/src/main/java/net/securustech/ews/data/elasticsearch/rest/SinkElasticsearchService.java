package net.securustech.ews.data.elasticsearch.rest;

import net.securustech.ews.data.elasticsearch.dto.ElasticsearchSinkDto;
import net.securustech.ews.exception.entities.EWSException;
import net.securustech.ews.logger.EWSLogger;
import net.securustech.ews.logger.LogLevel;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static net.securustech.ews.logger.SourceType.ELASTIC_SEARCH;

@Component
public class SinkElasticsearchService {

    @Value("${es.docType}")
    String docType;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${es.sink.retry.attempts:3}")
    Integer esSinkRetryAttempts;

    @Value("${es.sink.retry.delay.ms:3000}")
    Long esSinkRetryDelayMs;

    private static final Logger LOGGER = LoggerFactory.getLogger(SinkElasticsearchService.class);

    @EWSLogger(type = ELASTIC_SEARCH, name = "syncElasticsearch", logLevel = LogLevel.DEBUG)
    public BulkResponse bulkSync(List<ElasticsearchSinkDto> elasticsearchSinkDtos) throws EWSException {
        BulkResponse bulkResponse = null;
        try {
            BulkRequest bulkRequest = new BulkRequest();
            elasticsearchSinkDtos.forEach(elasticsearchSinkDto -> {
                String id = elasticsearchSinkDto.getId().replaceAll("^\"|\"$", "");
                if (elasticsearchSinkDto.getDocument() != null) {
                    IndexRequest indexRequest = new IndexRequest(
                            elasticsearchSinkDto.getIndexName(), docType, id)
                            .source(elasticsearchSinkDto.getDocument(), XContentType.JSON);
                    bulkRequest.add(indexRequest);
                }
            });
            bulkResponse = syncBulkAndGetResponse(bulkRequest);
            if (!bulkResponse.hasFailures()) {
                prepareSuccess(elasticsearchSinkDtos, bulkResponse);
            } else {
                prepareFailures(bulkResponse);
            }

        } catch (Exception e) {
            elasticsearchSinkDtos.forEach(elasticsearchSinkDto -> {
                LOGGER.error("@@@@Failure while bulk sync@@@@elasticsearch document id@@@@ >>>> " + elasticsearchSinkDto.getId() +
                        " @@@@SyncLogId@@@@ >>>> " + elasticsearchSinkDto.getSyncLogId());
            });
            LOGGER.error("@@@@Failure while bulk sync@@@@ >>>> ", e);
            throw new EWSException(e);
        }
        return bulkResponse;
    }

    @Retryable(
            maxAttemptsExpression = "#{@esSinkRetryAttempts}",
            backoff = @Backoff(delayExpression = "#{@esSinkRetryDelayMs}")
    )
    private BulkResponse syncBulkAndGetResponse(BulkRequest bulkRequest) throws Exception {
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    private void prepareSuccess(List<ElasticsearchSinkDto> elasticsearchSinkDtos, BulkResponse bulkResponse) {
        LOGGER.info("@@@@Bulk Sink Success@@@Bulk operation took " + bulkResponse.getTook() + " milliseconds");
        elasticsearchSinkDtos.forEach(elasticsearchSinkDto -> {
               LOGGER.info("@@@@Bulk Sink Success for @@@@ >>>> document id >>>> " + elasticsearchSinkDto.getId()
                       + "@@@@SyncLogId@@@@ >>>> " + elasticsearchSinkDto.getSyncLogId());
               LOGGER.trace("@@@@Bulk Sink Success for request@@@@ >>>> " + elasticsearchSinkDto.getDocument());
            });

    }
    private void prepareFailures(BulkResponse bulkResponse) {
        try {
            LOGGER.info("@@@@Bulk operation during failure took@@@@ >>>> " + bulkResponse.getTook() + " milliseconds");
            List<BulkItemResponse> bulkResponseList = Arrays.asList(bulkResponse.getItems());
            bulkResponseList.forEach(bulkItemResponse -> {
                if (bulkItemResponse.isFailed()) {
                    LOGGER.info("@@@@Bulk Sink failure for @@@@ >>>> document id >>>> " + bulkItemResponse.getId());
                }
            });
        } catch (Exception e) {
            LOGGER.error("@@@@Failure while preparing failures@@@@ >>>> ", e);
        }


    }


}
