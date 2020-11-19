package net.securustech.embs.zookeeper.broker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrokerInfo {

    private String host;
    private Integer port;
}
