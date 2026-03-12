function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

function setIcon(id, src) {
  const img = document.getElementById(id);
  if (img) img.src = src;
}

function toggleCard(id, show) {
  const el = document.getElementById(id);
  if (!el) return;
  el.classList.toggle("hidden", !show);
}

function setLocationSubText(text) {
  const el = document.querySelector(".weatherLocationSub");
  if (el) el.textContent = text;
}

function setBackground(weatherStatus) {
  const bgImage = document.getElementById("bgImage");
  if (!bgImage) return;

  const url =
    window.CoordiConfig.BG[weatherStatus] || window.CoordiConfig.BG.CLOUDY;
  bgImage.style.backgroundImage = `url('${url}')`;

  const body = document.body;
}

function hideIntroOverlay(delay = 700) {
  const overlay = document.getElementById("introOverlay");
  if (!overlay) return;

  setTimeout(() => {
    overlay.classList.add("is-hidden");
  }, delay);
}

function goToRecommend() {
  const query = (document.getElementById("nlq")?.value || "").trim();
  window.location.href = `/recommend?q=${encodeURIComponent(query)}`;
}

function applyCategoryCard(cardId, iconId, hintId, category) {
  if (!category) return;

  toggleCard(cardId, category.visible);
  if (!category.visible) return;

  setIcon(iconId, category.icon);
  setText(hintId, category.hint);
}

function applyMainSummary(summary) {
	console.log("summary =", summary);
	console.log("weatherStateKo =", summary.weatherStateKo);
	console.log("weatherIcon =", summary.weatherIcon);
  window.CoordiState.currentWeatherStatus = summary.weatherStatus || "CLOUDY";

  setBackground(window.CoordiState.currentWeatherStatus);

  setText("weatherLocationLine", summary.locationText || "서울");
  setLocationSubText(summary.locationSubText || "기본 위치");

  setText("panelTemp", `${Math.round(summary.temperature ?? 0)}°`);
  setText(
    "panelState",
    summary.weatherStateKo
      ? `${summary.weatherIcon || ""} ${summary.weatherStateKo}`.trim()
      : ""
  );
  setText("panelDesc", summary.weatherDesc || "");

  setText("feels", `${Math.round(summary.feelsLike ?? 0)}°`);
  setText("humid", `${summary.humidity ?? 0}%`);
  setText("wind", `${summary.windMs ?? 0} m/s`);
  setText("precip", `${summary.precipMm ?? 0} mm`);

  if (summary.top) {
    setIcon("topIcon", summary.top.icon);
    setText("topHint", summary.top.hint);
  }

  if (summary.bottom) {
    setIcon("bottomIcon", summary.bottom.icon);
    setText("bottomHint", summary.bottom.hint);
  }

  applyCategoryCard("outerCard", "outerIcon", "outerHint", summary.outer);
  applyCategoryCard("accCard", "accIcon", "accHint", summary.accessory);

  startFx(summary.fxMode || chooseFxMode(window.CoordiState.currentWeatherStatus));
}