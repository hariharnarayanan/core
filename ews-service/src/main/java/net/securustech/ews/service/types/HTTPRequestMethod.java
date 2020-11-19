package net.securustech.ews.service.types;


import lombok.Getter;

public enum HTTPRequestMethod {
    POST("POST"),
    PUT("PUT"),
    GET("GET"),
    DELETE("DELETE"),
    HEAD("HEAD");

    @Getter
    private String value;

    HTTPRequestMethod(String value){
        this.value=value;
    }

}
