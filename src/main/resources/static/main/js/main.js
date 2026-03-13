async function initMainPage() {
  document.body.classList.add("is-loading");
  resizeCanvas();

  document.getElementById("goSearch")?.addEventListener("click", goToRecommend);
  document.getElementById("nlq")?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") goToRecommend();
  });

  bindDevPreview();

  try {
    const { lat, lon, isDefault } = await getLocationOrDefault();
    const summary = await fetchMainSummary(lat, lon, isDefault);

    applyMainSummary(summary);
    hideIntroOverlay(0);
  } catch (e) {
    console.error("초기 로딩 실패:", e);

	applyMainSummary({
	  locationText: "서울",
	  locationSubText: "기본 위치",
	  weatherStatus: "CLOUDY",
	  weatherStateKo: "흐림",
	  weatherIcon: "☁",
	  weatherDesc: "날씨를 불러오지 못했어요",
	  temperature: 0,
	  feelsLike: 0,
	  humidity: 0,
	  windMs: 0,
	  precipMm: 0,
	  fxMode: "none",
	  top: {
	    title: "상의",
	    hint: "긴팔티/셔츠",
	    icon: "/main/images/clothes-icons/top_long_sleeve.png",
	    visible: true
	  },
	  bottom: {
	    title: "하의",
	    hint: "기본 긴바지",
	    icon: "/main/images/clothes-icons/bottom_longpants.png",
	    visible: true
	  },
	  outer: {
	    title: "아우터",
	    hint: "-",
	    icon: "/main/images/clothes-icons/outer_jacket.png",
	    visible: false
	  },
	  accessory: {
	    title: "악세서리",
	    hint: "-",
	    icon: "/main/images/clothes-icons/acc_scarf.png",
	    visible: false
	  }
	});

    hideIntroOverlay();
  }
}

document.addEventListener("DOMContentLoaded", initMainPage);
window.addEventListener("resize", resizeCanvas);