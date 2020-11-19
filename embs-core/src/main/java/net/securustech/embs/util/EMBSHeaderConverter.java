package net.securustech.embs.util;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;

@Component
public class EMBSHeaderConverter {

    public static byte[] serialize(Object obj) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public Object getHeaderValue(String key, Headers headers) throws IOException, ClassNotFoundException {

        return deserialize(headers.headers(key).iterator().next().value());
    }

    public Iterable<Header> buildHeaders(HashMap<String, Object> rawHeaders) {
        if (rawHeaders == null)
            return null;

        RecordHeaders recordHeaders = new RecordHeaders();

        rawHeaders.entrySet()
                .stream()
                .forEach(entry -> {
                    try {
                        recordHeaders.add(new RecordHeader(entry.getKey(), serialize(rawHeaders.get(entry.getKey()))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return recordHeaders;
    }
}
