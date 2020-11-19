package net.securustech.ews.data.elasticsearch;

import net.securustech.ews.data.elasticsearch.dto.ElasticsearchSinkDto;
import net.securustech.ews.data.elasticsearch.rest.SinkElasticsearchService;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class SinkElasticsearchServiceTest {

    private List<ElasticsearchSinkDto> elasticsearchSinkDtos;

    @Mock
    private SinkElasticsearchService mockSinkElasticsearchService;

    private BulkResponse bulkResponseWithFailure;

    private BulkResponse bulkResponseWithSuccess;

    @Before
    public void setup() {
        elasticsearchSinkDtos = new ArrayList<ElasticsearchSinkDto>();
        ElasticsearchSinkDto elasticsearchSinkDto = new ElasticsearchSinkDto();
        String jsonDocument = "{\"outboundVMUsedFlg\":\"N\",\"cdrModifiedBy\":\"EAI\",\"dialedCountryCd\":\"1\",\"promoCallFlg\":\"N\"," +
                "\"rcfdDetectedFlg\":\"N\",\"customerProvId\":11000007796,\"cdrId\":\"loadtest7-10.81.7.21-dd65a6e70a5000156ac5cd1afcbb10d5\"," +
                "\"callStartDt\":\"2020-06-22T14:58:46-07:00\",\"callFromPhoneNr\":\"9042594678\",\"otherPartyInfo\":" +
                "\"9293356826\",\"threeWayCnt\":0,\"dialedPhoneNr\":\"9293356826\",\"terminalProvId\":11000235675,\"terminalNm\":\"LP 15\"," +
                "\"callCompleteFlg\":\"N\",\"createdDt\":\"2020-06-24T13:52:29-05:00\",\"vbUsedFlg\":\"N\",\"inmateId\":\"19002439\"," +
                "\"geoLocationFoundFlg\":\"N\",\"inmateUid\":1295009703,\"inmateFirstNm\":\"JERARD\",\"callEndDt\":\"2020-06-22T14:59:54-07:00\"," +
                "\"intendedAttorneyCallFlg\":\"N\",\"callAmt\":0,\"liveConnectCallFlg\":\"N\",\"cdrCreatedDt\":\"2020-06-24T10:32:24-07:00\"," +
                "\"callTypeNm\":\"Debit\",\"dtmfDetectFlg\":\"N\",\"cellPhoneFlg\":\"N\",\"callTaxAmt\":0,\"terminationCategoryNm\":\"Caller Hang up\"," +
                "\"callServiceNm\":\"OperSvcs-R2\",\"destinationZoneNm\":\"Interlata/Interstate\",\"aisVMPatternFoundFlg\":\"N\",\"languageNm\":" +
                "\"English\",\"intlCallFlg\":\"N\",\"inmateLastNm\":\"DAVIS\",\"customerId\":\"I-002879\",\"callDurationSec\":0,\"cvvFlg\":\"N\"," +
                "\"remoteCallForwardingAlertFlg\":\"N\",\"recordingAccessedFlg\":\"N\",\"siteNm\":\"Santa Maria Jail, CA\",\"cdrModifiedDt\":" +
                "\"2020-06-24T10:32:24-07:00\",\"dialingClassNm\":\"SCN_DEBIT\",\"privateCallFlg\":\"N\",\"siteProvId\":11000008291,\"cvvVbUsedFlg\":\"N\"," +
                "\"terminalGroupNm\":\"Disable\",\"dialedCountryNm\":\"United States\",\"cdrSyncLogId\":7170181,\"docTypeNm\":\"mainCdr\",\"watchedCallFlg\":" +
                "\"N\",\"terminalGroupProvId\":11000008456,\"createdBy\":\"ESP-Connect\",\"cdrCreatedBy\":\"EAI\",\"siteId\":\"06081\",\"inmatePin\":\"190015681013\"," +
                "\"recordingId\":\"10.80.0.21-dd65a6e70a5000156ac5cd1afcbb10d5\",\"customerNm\":\"Santa Barbara County Jail, CA\",\"testCallFlg\":\"N\"}";
        elasticsearchSinkDto.setDocument(jsonDocument);
        elasticsearchSinkDto.setIndexName("cdr-dev-202006");
        elasticsearchSinkDto.setId("10.7.46.44-063f85440a072e2c458a2ccf9eac8be8");
        elasticsearchSinkDto.setSyncLogId("86457");
        elasticsearchSinkDtos.add(elasticsearchSinkDto);
        BulkItemResponse.Failure failure = new BulkItemResponse.Failure(
                "cdr-dev-202006", "_doc", "10.7.46.44-063f85440a072e2c458a2ccf9eac8be8", new Exception("Indexing exception"));
        BulkItemResponse bulkItemResponseFailure = new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, failure);
        IndexResponse indexResponse = new IndexResponse(
                new ShardId(new Index("cdr-dev-202006", UUID.randomUUID().toString()), 1),"_doc", "10.7.46.44-063f85440a072e2c458a2ccf9eac8be8", 1,
                        1, 1, true);
        BulkItemResponse bulkItemResponseSuccess = new BulkItemResponse(0, DocWriteRequest.OpType.INDEX, indexResponse);
        BulkItemResponse[] bulkItemResponsesFailure = {bulkItemResponseFailure};
        BulkItemResponse[] bulkItemResponsesSucess = {bulkItemResponseSuccess};
        bulkResponseWithFailure = new BulkResponse(bulkItemResponsesFailure, 100);
        bulkResponseWithSuccess = new BulkResponse(bulkItemResponsesSucess, 100);


    }

    @Test
    public void whenCdrSinkReturnSuccess() throws Exception {
        when(mockSinkElasticsearchService.bulkSync(elasticsearchSinkDtos)).thenReturn(bulkResponseWithSuccess);
        BulkResponse bulkResponse = mockSinkElasticsearchService.bulkSync(elasticsearchSinkDtos);
        Assert.assertTrue(!bulkResponse.hasFailures());
        verify(mockSinkElasticsearchService).bulkSync(elasticsearchSinkDtos);
    }

    @Test
    public void whenCdrSinkReturnFailure() throws Exception {
        when(mockSinkElasticsearchService.bulkSync(elasticsearchSinkDtos)).thenReturn(bulkResponseWithFailure);
        BulkResponse bulkResponse = mockSinkElasticsearchService.bulkSync(elasticsearchSinkDtos);
        Assert.assertTrue(bulkResponse.hasFailures());
        verify(mockSinkElasticsearchService).bulkSync(elasticsearchSinkDtos);
    }
}
