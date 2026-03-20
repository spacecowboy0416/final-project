package com.finalproject.coordi.recommendation.infra.navershopping;

import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.api.shopping")
public class NaverShoppingProperties {
    private String endpoint;
    private String clientId;
    private String clientSecret;
    private int resultLimit = 1;
    private String PROVIDER_NAME = "naver_shopping";
    private String USER_AGENT = "Mozilla/5.0";

    @PostConstruct
    void validate() {
        if (!StringUtils.hasText(endpoint)
            || !StringUtils.hasText(clientId)
            || !StringUtils.hasText(clientSecret)) {
            throw new RecommendationException.AdapterException(
                "Naver Shopping 설정이 누락되었습니다.",
                ErrorCode.RECOMMENDATION_NAVER_SHOPPING_CONFIG_MISSING
            );
        }
        if (resultLimit < 1) {
            resultLimit = 1;
        }
    }
}
