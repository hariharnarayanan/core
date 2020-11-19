package net.securustech.ews.core.zipkin;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

@ConditionalOnProperty(value = "ews.zipkin.enabled", havingValue = "true")
@Configuration
public class ZipkinConfiguration {

    @Value("${ews.zipkin.server}")
    private String zipkinServer;

    @Value("${ews.zipkin.port}")
    private String zipkinPort;

    @Value("${spring.application.name:ews-customers}")
    private String applicationName;

    @Bean(name = "zipkinTracer")
    @Primary
    public io.opentracing.Tracer zipkinTracer() {

        OkHttpSender okHttpSender = OkHttpSender.create("http://" + zipkinServer + ":" + zipkinPort + "/api/v2/spans");
        AsyncReporter<Span> reporter = AsyncReporter.builder(okHttpSender).build();
        Tracing braveTracer = Tracing.newBuilder().localServiceName(applicationName).spanReporter(reporter).build();

        return BraveTracer.create(braveTracer);
    }
}
