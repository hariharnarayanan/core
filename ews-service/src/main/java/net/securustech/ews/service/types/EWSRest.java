package net.securustech.ews.service.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class EWSRest {

    private HTTPRequestMethod httpMethod;
    private String httpHeaders;
    private String endpointUrl;

    public EWSRest(HTTPRequestMethod httpMethod, Map<String, String> httpHeaders, String endpointUrl) throws JsonProcessingException {

        this.httpMethod = httpMethod;
        this.endpointUrl = endpointUrl;
        if(httpHeaders != null && httpHeaders.size() > 0) {

            this.httpHeaders = new ObjectMapper().writeValueAsString(httpHeaders);
        }
    }

    public EWSRest(HTTPRequestMethod httpMethod, String httpHeaders, String endpointUrl) {

        this.httpMethod = httpMethod;
        this.httpHeaders = httpHeaders;
        this.endpointUrl = endpointUrl;
    }
}
