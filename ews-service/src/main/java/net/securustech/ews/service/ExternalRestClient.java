package net.securustech.ews.service;

import net.securustech.ews.logger.EWSLogger;
import net.securustech.ews.logger.LogLevel;
import net.securustech.ews.service.types.HTTPRequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static net.securustech.ews.logger.SourceType.*;
import static net.securustech.ews.service.types.HTTPRequestMethod.*;

@Component
public class ExternalRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRestClient.class);

    @EWSLogger(type = EXTERNAL_SYSTEM_BO, name = "externalEndpointBO", logResults = true,  logLevel = LogLevel.DEBUG)
    public Response callRestForBO(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, String mediaType) throws Exception {

        return callRest(endpointURL, httpMethod, messagePayload, headers, mediaType);
    }

    @EWSLogger(type = EXTERNAL_SYSTEM_SVV, name = "externalEndpointSVV", logResults = true,  logLevel = LogLevel.DEBUG)
    public Response callRestForSVV(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, String mediaType) throws Exception {

        return callRest(endpointURL, httpMethod, messagePayload, headers, mediaType);
    }

    @EWSLogger(type = EXTERNAL_SYSTEM_SCHEDULER, name = "externalEndpointSCHEDULER", logResults = true,  logLevel = LogLevel.DEBUG)
    public Response callRestForSCHEDULER(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, String mediaType) throws Exception {

        return callRest(endpointURL, httpMethod, messagePayload, headers, mediaType);
    }

    @EWSLogger(type = EXTERNAL_SYSTEM_EMS, name = "externalEndpointEMS", logResults = true,  logLevel = LogLevel.DEBUG)
    public Response callRestForEMS(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, String mediaType) throws Exception {

        return callRest(endpointURL, httpMethod, messagePayload, headers, mediaType);
    }

    @EWSLogger(type = EXTERNAL_SYSTEM_EWS, name = "externalEndpointEWS", logResults = true,  logLevel = LogLevel.DEBUG)
    public Response callRestForEWS(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, String mediaType) throws Exception {

        return callRest(endpointURL, httpMethod, messagePayload, headers, mediaType);
    }

    public Response callRest(String endpointURL, HTTPRequestMethod httpMethod, String messagePayload, MultivaluedMap<String, Object> headers, String mediaType) throws Exception {

        Response response = null;

        Client client = getRestClient();
        WebTarget target = client.target(endpointURL);
        Invocation.Builder invocationBuilder = target.request();

        if (headers != null) {

            invocationBuilder.headers(headers);
        }

        if (GET.getValue().equalsIgnoreCase(httpMethod.getValue())) {
            response = invocationBuilder.get();
        } else if (POST.getValue().equalsIgnoreCase(httpMethod.getValue())) {
            response = invocationBuilder.post(Entity.entity(messagePayload, mediaType));
        } else if (PUT.getValue().equalsIgnoreCase(httpMethod.getValue())) {
            response = invocationBuilder.put(Entity.entity(messagePayload, mediaType));
        } else if (DELETE.getValue().equalsIgnoreCase(httpMethod.getValue())) {
            response = invocationBuilder.delete();
        }  else if (HEAD.getValue().equalsIgnoreCase(httpMethod.getValue())) {
            response = invocationBuilder.head();
        }

        return response;
    }

    public Client getRestClient() {

        return ClientBuilder.newClient();
    }
}
