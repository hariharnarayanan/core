/*
 * Copyright (c) 2012 SECURUSTECH and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADERS
 */
package net.securustech.ews.core.repository.entity;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * SCN_OPDB.EXTERNAL_SYSTEM_RETRY_LOG
 * (
 *      RETRY_ID       NUMBER NOT NULL
 *      ,STATUS         VARCHAR2( 30 )
 *      ,RETRY_COUNT    NUMBER( 2 ) DEFAULT 0
 *      ,TOPIC     VARCHAR2( 50 )
 *      ,TOPIC_EVENT    VARCHAR2( 50 )
 *      ,ENDPOINT_URL   VARCHAR2( 256 )
 *      ,PAYLOAD        CLOB
 *      ,CREATED_BY     VARCHAR2( 30 )
 *      ,CREATED_DT     DATE DEFAULT SYSDATE NOT NULL
 *      ,MODIFIED_BY    VARCHAR2( 30 )
 *      ,MODIFIED_DT    DATE
 * );
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@SequenceGenerator(name="RETRY_ID_SEQ", sequenceName="RETRY_ID_SEQ", allocationSize = 1)
@Table(name = "EXTERNAL_SYSTEM_RETRY_LOG", schema = "SCN_OPDB")
@SuppressWarnings("unused")
public class ExternalSystemRetryLog implements Serializable {

    private static final long serialVersionUID = -8822312798641523837L;

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator="RETRY_ID_SEQ")
    @Column(name = "RETRY_ID")
    private Long retryId;

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @Column(name = "RETRY_COUNT", nullable = false)
    private Short retryCount;

    @Column(name = "TOPIC", length = 50)
    private String topic;

    @Column(name = "KEY", length = 50)
    private String key;

    @Column(name = "TOPIC_EVENT", length = 50)
    private String topicEvent;

    @Column(name = "ENDPOINT_URL", length = 256)
    private String endpointUrl;

    @Column(name = "PAYLOAD", nullable = false)
    private String payload;

    @Column(name = "HTTP_HEADERS", length = 256)
    private String httpHeaders;

    @Column(name = "HTTP_METHODS", length = 256)
    private String httpMethods;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_DT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @Column(name = "MODIFIED_DT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    public ExternalSystemRetryLog(String status, Short retryCount, String topic, String key, String topicEvent, String endpointUrl,
                                  String payload, String httpHeaders, String httpMethods, String createdBy, Date createdDate,
                                  String modifiedBy, Date modifiedDate) {
        this.status = status;
        this.retryCount = retryCount;
        this.topic = topic;
        this.key = key;
        this.topicEvent = topicEvent;
        this.endpointUrl = endpointUrl;
        this.payload = payload;
        this.httpHeaders = httpHeaders;
        this.httpMethods = httpMethods;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
    }

    public RetryType getRetryType() {

        if(StringUtils.isNotBlank(topic) || StringUtils.isNotBlank(topicEvent)) {

            return RetryType.EMBS_PUBLISH;
        } else if(StringUtils.isNotBlank(endpointUrl)) {

            return RetryType.EWS_REST;
        } else {

            return RetryType.UNKNOWN;
        }
    }

    public void incrementRetryCount() {

        this.retryCount++;
    }
}
