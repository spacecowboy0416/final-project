window.CoordiConfig = {
  DEV_WEATHER_PREVIEW: true, // 배포 시 false

  DEFAULT_LOCATION: {
    lat: 37.498095,
    lon: 127.027610
  },

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