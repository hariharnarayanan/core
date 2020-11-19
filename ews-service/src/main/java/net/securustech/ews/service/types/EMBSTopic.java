package net.securustech.ews.service.types;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class EMBSTopic {

    private String topic;
    private String key;
    private Integer partition;

    public EMBSTopic(String topic, String key) {

        this.topic = topic;
        this.key = key;
    }

    public EMBSTopic(String topic, String key, Integer partition) {

        this.topic = topic;
        this.key = key;
        this.partition = partition;
    }
}
