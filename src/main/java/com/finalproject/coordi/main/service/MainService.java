package com.finalproject.coordi.main.service;

import org.springframework.stereotype.Service;

import com.finalproject.coordi.main.dto.CategoryRecommendationDto;
import com.finalproject.coordi.main.dto.MainResponse;
import com.finalproject.coordi.main.dto.WeatherResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainService {

    private final WeatherService weatherService;

    public MainResponse preview(WeatherResponse weather) {
        return buildMainResponse(weather, true);
    }

    public MainResponse getSummary(double lat, double lon, boolean isDefault) {
        WeatherResponse weather = weatherService.getToday(lat, lon);
        return buildMainResponse(weather, isDefault);
    }

    private MainResponse buildMainResponse(WeatherResponse weather, boolean isDefault) {
        String main = safeMain(weather.getWeatherMainRaw());
        String weatherStatus = safeWeatherStatus(weather.getWeatherStatus());

        return MainResponse.builder()
                .locationText(
                        weather.getLocationText() != null && !weather.getLocationText().isBlank()
                                ? weather.getLocationText()
                                : "서울"
                )
                .locationSubText(isDefault ? "기본 위치" : "현재 위치")
                .weatherMain(main)
                .weatherStatus(weatherStatus)
                .weatherStateKo(getWeatherStateKo(weatherStatus))
                .weatherIcon(getWeatherIcon(weatherStatus))
                .weatherDesc(getWeatherDesc(weatherStatus, weather))
                .temperature(weather.getTemperature())
                .feelsLike(weather.getFeelsLike())
                .humidity(weather.getHumidity())
                .windMs(weather.getWindMs())
                .precipMm(weather.getPrecipMm())
                .todayRain(weather.isTodayRain())
                .fxMode(chooseFxMode(weatherStatus))
                .top(buildTopCategory(weather))
                .bottom(buildBottomCategory(weather))
                .outer(buildOuterCategory(weather))
                .accessory(buildAccessoryCategory(weather))
                .build();
    }

    private String safeMain(String main) {
        return main == null || main.isBlank() ? "Clear" : main;
    }

    private String safeWeatherStatus(String weatherStatus) {
        if (weatherStatus == null || weatherStatus.isBlank()) {
            return "CLOUDY";
        }

        return weatherStatus.trim().toUpperCase();
    }

    private String getWeatherStateKo(String weatherStatus) {
        return switch (safeWeatherStatus(weatherStatus)) {
            case "CLEAR" -> "맑음";
            case "PARTLY_CLOUDY" -> "구름 조금";
            case "CLOUDY" -> "흐림";
            case "WINDY" -> "바람 많음";
            case "RAIN" -> "비";
            case "CLOUDY_RAIN" -> "흐리고 비";
            case "THUNDERSTORM" -> "뇌우";
            case "THUNDERSTORM_RAIN" -> "뇌우와 비";
            case "SNOW" -> "눈";
            case "CLOUDY_SNOW" -> "흐리고 눈";
            case "SLEET" -> "진눈깨비";
            case "HAIL" -> "우박";
            default -> "";
        };
    }

    private String getWeatherIcon(String weatherStatus) {
        return switch (safeWeatherStatus(weatherStatus)) {
            case "CLEAR" -> "☼";
            case "PARTLY_CLOUDY", "CLOUDY" -> "☁";
            case "WINDY" -> "🌬";
            case "RAIN", "CLOUDY_RAIN" -> "☂";
            case "THUNDERSTORM", "THUNDERSTORM_RAIN" -> "☇";
            case "SNOW", "CLOUDY_SNOW", "SLEET", "HAIL" -> "❄";
            default ->"";
        };
    }

    private String getWeatherDesc(String weatherStatus, WeatherResponse weather) {
        String status = safeWeatherStatus(weatherStatus);

        if ("SNOW".equals(status) || "CLOUDY_SNOW".equals(status)) {
            return "눈이 내려 미끄러울 수 있으니 주의하세요";
        }

        if ("SLEET".equals(status)) {
            return "진눈깨비가 내려 우산을 챙기는 것이 좋아요";
        }

        if ("HAIL".equals(status)) {
            return "우박이 떨어질 수 있으니 외출 시 주의하세요";
        }

        if ("THUNDERSTORM".equals(status) || "THUNDERSTORM_RAIN".equals(status)) {
            return "천둥번개가 예상되니 외출 시 주의하세요";
        }

        if ("RAIN".equals(status) || "CLOUDY_RAIN".equals(status)) {
            return "비에 대비해 우산을 챙기세요";
        }

        if (weather.isTodayRain()) {
            return "오늘 강수 예보가 있어 우산을 챙기는 것이 좋아요";
        }

        if ("WINDY".equals(status)) {
            return "바람이 강할 수 있어 겉옷을 챙기는 것이 좋아요";
        }

        return "";
    }

    private CategoryRecommendationDto buildTopCategory(WeatherResponse weather) {
        if (weather.getTemperature() <= 8) {
            return CategoryRecommendationDto.builder()
                    .title("상의")
                    .hint("니트")
                    .icon("/main/images/clothes-icons/top_knit.png")
                    .visible(true)
                    .build();
        }

        if (weather.getTemperature() >= 25) {
            return CategoryRecommendationDto.builder()
                    .title("상의")
                    .hint("반팔티")
                    .icon("/main/images/clothes-icons/top_tshirt.png")
                    .visible(true)
                    .build();
        }

        return CategoryRecommendationDto.builder()
                .title("상의")
                .hint("긴팔티/셔츠")
                .icon("/main/images/clothes-icons/top_long_sleeve.png")
                .visible(true)
                .build();
    }

    private CategoryRecommendationDto buildBottomCategory(WeatherResponse weather) {
        if (weather.getTemperature() >= 26) {
            return CategoryRecommendationDto.builder()
                    .title("하의")
                    .hint("반바지")
                    .icon("/main/images/clothes-icons/bottom_shorts.png")
                    .visible(true)
                    .build();
        }

        return CategoryRecommendationDto.builder()
                .title("하의")
                .hint(weather.getTemperature() <= 8 ? "두꺼운 긴바지" : "기본 긴바지")
                .icon("/main/images/clothes-icons/bottom_longpants.png")
                .visible(true)
                .build();
    }

    private boolean isOuterNeeded(WeatherResponse weather) {
        return weather.getFeelsLike() <= 15
                || weather.getWindMs() >= 6
                || "Snow".equals(safeMain(weather.getWeatherMainRaw()));
    }

    private boolean isAccessoryNeeded(WeatherResponse weather) {
        String main = safeMain(weather.getWeatherMainRaw());

        return ("Clear".equals(main) && weather.getTemperature() >= 18)
                || "Snow".equals(main)
                || "Rain".equals(main)
                || "Drizzle".equals(main)
                || weather.getPrecipMm() >= 1
                || weather.isTodayRain();
    }

    private CategoryRecommendationDto buildOuterCategory(WeatherResponse weather) {
        boolean visible = isOuterNeeded(weather);

        if (!visible) {
            return CategoryRecommendationDto.builder()
                    .title("아우터")
                    .hint("-")
                    .icon("/main/images/clothes-icons/outer_jacket.png")
                    .visible(false)
                    .build();
        }

        if (weather.getFeelsLike() <= 3) {
            return CategoryRecommendationDto.builder()
                    .title("아우터")
                    .hint("패딩")
                    .icon("/main/images/clothes-icons/outer_padding.png")
                    .visible(true)
                    .build();
        }

        if (weather.getFeelsLike() <= 8) {
            return CategoryRecommendationDto.builder()
                    .title("아우터")
                    .hint("코트")
                    .icon("/main/images/clothes-icons/outer_coat.png")
                    .visible(true)
                    .build();
        }

        if (weather.getFeelsLike() <= 15) {
            return CategoryRecommendationDto.builder()
                    .title("아우터")
                    .hint("자켓")
                    .icon("/main/images/clothes-icons/outer_jacket.png")
                    .visible(true)
                    .build();
        }

        return CategoryRecommendationDto.builder()
                .title("아우터")
                .hint("가디건")
                .icon("/main/images/clothes-icons/outer_cardigan.png")
                .visible(true)
                .build();
    }

    private CategoryRecommendationDto buildAccessoryCategory(WeatherResponse weather) {
        String main = safeMain(weather.getWeatherMainRaw());
        boolean visible = isAccessoryNeeded(weather) || weather.isTodayRain();

        if (!visible) {
            return CategoryRecommendationDto.builder()
                    .title("악세서리")
                    .hint("-")
                    .icon("/main/images/clothes-icons/acc_scarf.png")
                    .visible(false)
                    .build();
        }

        if ("Rain".equals(main)
                || "Drizzle".equals(main)
                || weather.getPrecipMm() >= 1
                || weather.isTodayRain()) {
            return CategoryRecommendationDto.builder()
                    .title("악세서리")
                    .hint("우산 챙기기")
                    .icon("/main/images/clothes-icons/acc_umbrella.png")
                    .visible(true)
                    .build();
        }

        if ("Snow".equals(main) || weather.getFeelsLike() <= 5) {
            return CategoryRecommendationDto.builder()
                    .title("악세서리")
                    .hint("장갑/목도리")
                    .icon("/main/images/clothes-icons/acc_gloves.png")
                    .visible(true)
                    .build();
        }

        if ("Clear".equals(main) && weather.getTemperature() >= 18) {
            return CategoryRecommendationDto.builder()
                    .title("악세서리")
                    .hint("선글라스/모자")
                    .icon("/main/images/clothes-icons/acc_sunglasses.png")
                    .visible(true)
                    .build();
        }

        return CategoryRecommendationDto.builder()
                .title("악세서리")
                .hint("필요한 소품 추천")
                .icon("/main/images/clothes-icons/acc_scarf.png")
                .visible(true)
                .build();
    }

    private String chooseFxMode(String weatherStatus) {
        return switch (safeWeatherStatus(weatherStatus)) {
            case "CLEAR" -> "sun";

            case "RAIN", "CLOUDY_RAIN" -> "rain";

            case "THUNDERSTORM" -> "thunder";
            case "THUNDERSTORM_RAIN" -> "thunder_rain";

            case "SNOW", "CLOUDY_SNOW" -> "snow";

            case "SLEET" -> "sleet";
            case "HAIL" -> "hail";
            case "WINDY" -> "wind";

            default -> "none";
        };
    }
}