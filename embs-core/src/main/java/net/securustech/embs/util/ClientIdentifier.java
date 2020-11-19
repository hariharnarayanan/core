package net.securustech.embs.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Setter
public class ClientIdentifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientIdentifier.class);

    private String localServerHost;

    private String localServerPort;

    private String applicationName;

    private String groupId;

    private String clientId;

    private String customGroupId;

    private String customClientId;

    public ClientIdentifier(final String applicationName, final String localServerPort, final String customGroupId, final String customClientId) {

        this.applicationName = applicationName;
        this.localServerPort = localServerPort;
        this.customGroupId = customGroupId;
        this.customClientId = customClientId;

        try {

            localServerHost = InetAddress.getLocalHost().getHostName();

            buildClientId();
            buildGroupId();

        } catch (UnknownHostException e) {

            LOGGER.error("ClientIdentifier@LocalServerHost@Failed :-> Local Server Host can't be determined -- This Exception is a WARNING and CAN BE IGNORED >>> " + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * FORMAT == ApplicationName_GroupID
     * esp-visitors_es-sync
     * esp-visitors
     * es-sync
     * ews-anonymous
     */
    public String buildGroupId() {

        StringBuffer groupIdStr = new StringBuffer("");

        if (!StringUtils.isBlank(applicationName)) {

            groupIdStr.append(applicationName);
        }

        if (!StringUtils.isBlank(customGroupId)) {

            if (!StringUtils.isBlank(groupIdStr)) {

                groupIdStr.append("_" + customGroupId);
            } else {

                groupIdStr.append(customGroupId);
            }
        }

        if (StringUtils.isBlank(groupIdStr)) {

            groupIdStr.append("ews-anonymous");
        }

        LOGGER.debug("BUILD@EMBS@GroupID :::>>> " + groupIdStr.toString());

        groupId = groupIdStr.toString();

        return groupId;
    }

    /**
     * FORMAT == ClientID_ApplicationName_Host_Port
     * es_sync_esp-visitors_ld-midsrvcs01_1202
     * esp-visitors_ld-midsrvcs01_1202
     * ews-anonymous_ld-midsrvcs01_1202
     * esp-visitors
     * ews-anonymous
     */
    public String buildClientId() {

        StringBuffer clientIdStr = new StringBuffer("");

        if (!StringUtils.isBlank(customClientId)) {

            clientIdStr.append(customClientId);
        }

        if (!StringUtils.isBlank(applicationName)) {

            if (!StringUtils.isBlank(clientIdStr)) {

                clientIdStr.append("_" + applicationName);
            } else {

                clientIdStr.append(applicationName);
            }
        } else {

            if (StringUtils.isBlank(clientIdStr)) {

                clientIdStr.append("ews-anonymous");
            }
        }

        if (!StringUtils.isBlank(localServerHost)) {

            clientIdStr.append("_" + localServerHost);
        }

        if (!StringUtils.isBlank(localServerPort)) {

            clientIdStr.append("_" + localServerPort);
        }

        LOGGER.debug("BUILD@EMBS@ClientID :::>>> " + clientIdStr.toString());

        clientId = clientIdStr.toString();

        return clientId;
    }
}
