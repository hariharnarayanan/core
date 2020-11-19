package net.securustech.ews.data.elasticsearch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElasticsearchSinkDto {

    private String id;
    private String syncLogId;
    private String indexName;
    //Elasticsearch json document
    private String document;
}
