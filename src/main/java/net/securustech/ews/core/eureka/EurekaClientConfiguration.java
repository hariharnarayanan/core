package net.securustech.ews.core.eureka;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"kube", "blue","green","freeride","qa","load","uat","pp","prod"})
@Configuration
@EnableDiscoveryClient
public class EurekaClientConfiguration {
}
