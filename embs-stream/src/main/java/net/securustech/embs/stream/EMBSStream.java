package net.securustech.embs.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class EMBSStream {

    @Bean
    public KStream<String, String> kStream(KStreamBuilder kStreamBuilder) {
        KStream<String, String> stream = kStreamBuilder.stream(Serdes.String(), Serdes.String(), "streams-file-input");
        KTable<String, Long> counts = stream
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .groupBy((key, value) -> value)
                .count("Counts");
        // need to override value serde to Long type
        counts.to(Serdes.String(), Serdes.Long(), "streams-wordcount-output");

        return stream;
    }
}
