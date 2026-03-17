// 개발용 미리보기 날씨 데이터 생성
async function createPreviewWeather(weatherStatus) {
  const locationText = window.CoordiConfig.DEFAULT_LOCATION.name || "서울";
  
  const base = {
    locationText,
    weatherStatus,
    conditionText: "",
    weatherStateKo: "",
    temperature: 15,
    feelsLike: 14,
    humidity: 60,
    windMs: 3,
    precipMm: 0,
    todayRain: false
  };

  switch (weatherStatus) {
    case "CLEAR":
      return {
        ...base,
        conditionText: "맑음",
        weatherStateKo: "맑음",
        temperature: 23,
        feelsLike: 24,
        humidity: 48,
        windMs: 2.2
      };

    case "PARTLY_CLOUDY":
      return {
        ...base,
        conditionText: "구름 조금",
        weatherStateKo: "구름 조금",
        temperature: 20,
        feelsLike: 20,
        humidity: 52,
        windMs: 2.8
      };

    case "CLOUDY":
      return {
        ...base,
        conditionText: "흐림",
        weatherStateKo: "흐림",
        temperature: 17,
        feelsLike: 16,
        humidity: 58,
        windMs: 3.5
      };

    case "WINDY":
      return {
        ...base,
        conditionText: "바람 많음",
        weatherStateKo: "강풍",
        temperature: 13,
        feelsLike: 9,
        humidity: 55,
        windMs: 10.5
      };

    case "RAIN":
      return {
        ...base,
        conditionText: "비",
        weatherStateKo: "비",
        temperature: 9,
        feelsLike: 6,
        humidity: 85,
        windMs: 4.8,
        precipMm: 3.0,
        todayRain: true
      };

    case "CLOUDY_RAIN":
      return {
        ...base,
        conditionText: "흐리고 비",
        weatherStateKo: "흐리고 비",
        temperature: 12,
        feelsLike: 10,
        humidity: 88,
        windMs: 3.2,
        precipMm: 1.2,
        todayRain: true
      };

    case "THUNDERSTORM":
      return {
        ...base,
        conditionText: "천둥번개",
        weatherStateKo: "천둥번개",
        temperature: 14,
        feelsLike: 11,
        humidity: 80,
        windMs: 9.0,
        precipMm: 5.5,
        todayRain: true
      };

    case "THUNDERSTORM_RAIN":
      return {
        ...base,
        conditionText: "비를 동반한 천둥번개",
        weatherStateKo: "뇌우",
        temperature: 13,
        feelsLike: 10,
        humidity: 84,
        windMs: 8.8,
        precipMm: 6.0,
        todayRain: true
      };

    case "SNOW":
      return {
        ...base,
        conditionText: "눈",
        weatherStateKo: "눈",
        temperature: -2,
        feelsLike: -6,
        humidity: 72,
        windMs: 4.2,
        precipMm: 1.8
      };

    case "CLOUDY_SNOW":
      return {
        ...base,
        conditionText: "흐리고 눈",
        weatherStateKo: "흐리고 눈",
        temperature: -1,
        feelsLike: -5,
        humidity: 78,
        windMs: 4.0,
        precipMm: 1.6
      };

    case "SLEET":
      return {
        ...base,
        conditionText: "진눈깨비",
        weatherStateKo: "진눈깨비",
        temperature: 1,
        feelsLike: -2,
        humidity: 82,
        windMs: 4.5,
        precipMm: 2.3,
        todayRain: true
      };

    case "HAIL":
      return {
        ...base,
        conditionText: "우박",
        weatherStateKo: "우박",
        temperature: 3,
        feelsLike: 0,
        humidity: 76,
        windMs: 6.2,
        precipMm: 2.0,
        todayRain: true
      };

    default:
      return {
        ...base,
        weatherStatus: "CLOUDY",
        conditionText: "흐림",
        weatherStateKo: "흐림"
      };
  }
}

async function fetchPreviewSummary(weather) {
  const response = await fetch("/api/main/preview", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(weather)
  });

  if (!response.ok) {
    throw new Error("Preview API request failed");
  }

  return response.json();
}

// 개발용 날씨 상태 버튼 클릭 시 preview API를 호출해 메인 UI를 테스트한다.
function bindDevPreview() {
  if (!window.CoordiConfig.DEV_WEATHER_PREVIEW) return;
  const panel = document.getElementById("devWeatherPanel");
  if (!panel) return;

  panel.classList.remove("hidden");

  panel.querySelectorAll("button[data-w]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      try {
        const weatherStatus = btn.getAttribute("data-w");
        const weather = await createPreviewWeather(weatherStatus);
        const summary = await fetchPreviewSummary(weather);

        applyMainSummary(summary);
        hideIntroOverlay();
      } catch (e) {
        console.error("preview 실패:", e);
      }
    });
  });
}