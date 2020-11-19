package net.securustech.ews.core.repository.entity;


import lombok.Getter;

public enum RetryType {

    EMBS_PUBLISH("EMBS_PUBLISH"),
    EWS_REST("EWS_REST"),
    UNKNOWN("UNKNOWN");

    @Getter
    private String value;

    RetryType(String value){
        this.value=value;
    }

}
