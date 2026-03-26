window.CoordiConfig = {
  DEV_WEATHER_PREVIEW: false, // 미리보기 버튼, false로 숨김가능
  
  // 위치 권한 실패 시 사용할 기본 위치
  DEFAULT_LOCATION: {
	lat: 37.5636,
	lon: 126.9976,
	name: "서울 중구"
  },
  
  // 날씨 상태별 배경 이미지
  BG: {
    CLEAR: "/main/images/weather/clear.png",
	
	//수정?
    PARTLY_CLOUDY: "/main/images/weather/partly_cloudy.png",
	WINDY: "/main/images/weather/windy.png",
    CLOUDY: "/main/images/weather/cloudy.png",
	
	CLOUDY_RAIN: "/main/images/weather/cloudy_rain.png",
    RAIN: "/main/images/weather/rain.png",
	
    THUNDERSTORM: "/main/images/weather/thunderstorm.png",
    THUNDERSTORM_RAIN: "/main/images/weather/thunderstorm.png",

    SNOW: "/main/images/weather/snow.png",
    CLOUDY_SNOW: "/main/images/weather/cloudy_snow.png",
    SLEET: "/main/images/weather/cloudy_snow.png",
    HAIL: "/main/images/weather/cloudy_snow.png"
  }
};

window.CoordiState = {
  currentWeatherStatus: "CLOUDY"
};