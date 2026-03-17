package com.finalproject.coordi.recommendation.config;

import com.finalproject.coordi.recommendation.infra.gemini.GeminiProperties;
import com.finalproject.coordi.recommendation.infra.s3.S3Properties;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalApiClientConfig {
    @Bean(destroyMethod = "close")
    public ExecutorService recommendationPipelineExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("recommendation-", 0).factory());
    }

    @Bean(destroyMethod = "close")
    public Client geminiClient(GeminiProperties geminiProperties) {
        Client.Builder builder = Client.builder().apiKey(geminiProperties.getKey());
        if (StringUtils.hasText(geminiProperties.getEndpoint())) {
            builder.httpOptions(HttpOptions.builder().baseUrl(geminiProperties.getEndpoint()).build());
        }
        return builder.build();
    }

    @Bean
    public RestClient naverShoppingRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    public RestClient productImageRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    public S3Client s3Client(S3Properties imageStorageProperties) {
        S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(imageStorageProperties.getRegion()));
        if (StringUtils.hasText(imageStorageProperties.getAccessKey())
            && StringUtils.hasText(imageStorageProperties.getSecretKey())) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        imageStorageProperties.getAccessKey(),
                        imageStorageProperties.getSecretKey()
                    )
                )
            );
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }
}

