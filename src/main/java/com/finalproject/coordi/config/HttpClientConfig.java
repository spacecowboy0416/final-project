package com.finalproject.coordi.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 외부 API 호출에서 사용할 공용 HTTP 클라이언트를 구성한다.
 * recommendation의 Gemini 호출은 대용량(이미지 base64) 요청을 주기적으로 보내므로
 * 커넥션 풀/타임아웃/유휴 커넥션 정리를 명시적으로 설정해 지연시간의 분산(tail latency)을 낮춘다.
 */
@Configuration
public class HttpClientConfig {
    @Value("${external.api.gemini.http.connect-timeout-ms:1500}")
    private int connectTimeoutMs;

    @Value("${external.api.gemini.http.response-timeout-ms:12000}")
    private int responseTimeoutMs;

    @Value("${external.api.gemini.http.connection-request-timeout-ms:1000}")
    private int connectionRequestTimeoutMs;

    @Value("${external.api.gemini.http.max-connections:200}")
    private int maxConnections;

    @Value("${external.api.gemini.http.max-connections-per-route:100}")
    private int maxConnectionsPerRoute;

    @Value("${external.api.gemini.http.idle-evict-seconds:30}")
    private int idleEvictSeconds;

    @Bean(destroyMethod = "close")
    public CloseableHttpClient geminiHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(maxConnections)
            .setMaxConnPerRoute(maxConnectionsPerRoute)
            .build();

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
            .setResponseTimeout(Timeout.ofMilliseconds(responseTimeoutMs))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeoutMs))
            .build();

        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .evictIdleConnections(TimeValue.ofSeconds(idleEvictSeconds))
            .build();
    }

    @Bean
    public RestClient restClient(CloseableHttpClient geminiHttpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(geminiHttpClient);
        return RestClient.builder()
            .requestFactory(requestFactory)
            .build();
    }
}
