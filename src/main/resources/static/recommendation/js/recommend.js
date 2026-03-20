(function () {
  const root = document.getElementById("recommendRoot");
  if (!root) {
    return;
  }

  const RECOMMENDATION_IMAGE_MAX_BYTES = Number(
    root.dataset.recommendationImageMaxBytes || 0
  );
  const INITIAL_NATURAL_TEXT = root.dataset.initialNaturalText || "";
  const KAKAO_MAP_API_KEY = root.dataset.kakaoMapApiKey || "";
  const DEFAULT_POSITION = { lat: 37.5665, lon: 126.978 };
  const DEFAULT_LOCATION_TEXT = "서울";
  const MAP_SCRIPT_ID = "kakao-map-script";
  const MAP_LEVEL = 4;
  const SAMPLE_IMAGE_URL = "/recommendation/image/KRK-LSX-JK251189-01.webp";
  const SAMPLE_IMAGE_FILE_NAME = "KRK-LSX-JK251189-01.webp";
  const WEATHER_FALLBACK = {
    weatherStatus: "CLOUDY",
    weatherStateKo: "흐림",
    weatherDesc: "일시적으로 날씨 설명을 준비 중입니다.",
    temperature: 0,
    feelsLike: 0,
    humidity: 0,
    windMs: 0,
    precipMm: 0,
    locationText: DEFAULT_LOCATION_TEXT,
    locationSubText: "기본 위치",
  };

  const SLOT_LABELS = {
    HEADWEAR: "헤드웨어",
    TOPS: "상의",
    BOTTOMS: "하의",
    OUTERWEAR: "아우터",
    SHOES: "신발",
    ACCESSORIES: "액세서리",
  };

  const OPTIONAL_HIDDEN_SLOTS = new Set(["HEADWEAR", "ACCESSORIES"]);

  const WEATHER_LABELS = {
    CLEAR: "맑음",
    PARTLY_CLOUDY: "구름 조금",
    CLOUDY: "흐림",
    WINDY: "바람",
    RAIN: "비",
    CLOUDY_RAIN: "비",
    THUNDERSTORM: "뇌우",
    THUNDERSTORM_RAIN: "뇌우",
    SNOW: "눈",
    CLOUDY_SNOW: "눈",
    SLEET: "진눈깨비",
    HAIL: "우박",
  };

  const WEATHER_DESCRIPTION_POLICY = {
    defaultDescription: "일시적으로 날씨 설명을 준비 중입니다.",
    rainDescription: "강수 가능성이 있어 우산을 챙기는 것을 권장해요.",
    stateSuffix: "날씨예요.",
  };

  const state = {
    imageBase64: "",
    imageMimeType: "",
    uploadedImageUrl: "",
    weatherSummary: null,
    selectedPosition: { ...DEFAULT_POSITION },
    selectedLocationLabel: DEFAULT_LOCATION_TEXT,
    currentResult: null,
    map: null,
    marker: null,
    geocoder: null,
    isSubmitting: false,
    isSaving: false,
    currentRequestPayload: null,
    hasSavedCurrentResult: false,
    developerPanelOpen: false,
    currentDebugResult: null,
  };

  const elements = {
    recommendForm: document.getElementById("recommendForm"),
    composeView: document.getElementById("composeView"),
    resultView: document.getElementById("resultView"),
    naturalText: document.getElementById("naturalText"),
    submitButton: document.getElementById("submitButton"),
    imageFile: document.getElementById("imageFile"),
    imagePreview: document.getElementById("imagePreview"),
    imagePreviewCard: document.getElementById("imagePreviewCard"),
    imageMetaText: document.getElementById("imageMetaText"),
    gender: document.getElementById("gender"),
    brandEnabled: document.getElementById("brandEnabled"),
    useCurrentLocationButton: document.getElementById("useCurrentLocationButton"),
    locationLabel: document.getElementById("locationLabel"),
    locationSubLabel: document.getElementById("locationSubLabel"),
    weatherStatusBadge: document.getElementById("weatherStatusBadge"),
    weatherTemperature: document.getElementById("weatherTemperature"),
    weatherDescription: document.getElementById("weatherDescription"),
    weatherFeelsLike: document.getElementById("weatherFeelsLike"),
    weatherHumidity: document.getElementById("weatherHumidity"),
    weatherWind: document.getElementById("weatherWind"),
    weatherPrecipitation: document.getElementById("weatherPrecipitation"),
    loadingOverlay: document.getElementById("loadingOverlay"),
    feedbackMessage: document.getElementById("feedbackMessage"),
    resultQueryText: document.getElementById("resultQueryText"),
    resultMetaText: document.getElementById("resultMetaText"),
    resultExplanation: document.getElementById("resultExplanation"),
    resultItems: document.getElementById("resultItems"),
    resultItemCount: document.getElementById("resultItemCount"),
    saveRecommendationButton: document.getElementById("saveRecommendationButton"),
    backToComposeButton: document.getElementById("backToComposeButton"),
    map: document.getElementById("map"),
    resultLayout: document.getElementById("resultLayout"),
    developerToggleButton: document.getElementById("developerToggleButton"),
    developerPanel: document.getElementById("developerPanel"),
    developerSearchQueries: document.getElementById("developerSearchQueries"),
    developerBlueprint: document.getElementById("developerBlueprint"),
  };

  document.addEventListener("DOMContentLoaded", initRecommendPage);

  async function initRecommendPage() {
    if (!initStateAndElements()) {
      return;
    }
    initImageModule();
    initMapModule();
    initWeatherModule();
    initSubmissionModule();
    initResultModule();

    try {
      preloadSampleImageOnInit();
      await initializeLocationAndMap();
    } catch (error) {
      console.error("추천 페이지 초기화 실패", error);
      applyWeatherSummary(WEATHER_FALLBACK, true);
      setFeedback("위치 또는 날씨 정보를 기본값으로 표시합니다.", "error");
    }
  }

  function initStateAndElements() {
    // 필수 DOM이 없으면 조기 종료해 이후 초기화 실패를 막는다.
    const requiredElementIds = ["recommendForm", "submitButton", "imageFile", "map"];
    const missingElements = requiredElementIds.filter((key) => !elements[key]);
    if (missingElements.length > 0) {
      console.error("추천 페이지 필수 요소 누락", missingElements);
      return false;
    }
    if (elements.naturalText) {
      elements.naturalText.value = INITIAL_NATURAL_TEXT;
    }
    return true;
  }

  function initImageModule() {
    elements.imageFile.addEventListener("change", handleImageChange);
  }

  function initMapModule() {
    elements.useCurrentLocationButton.addEventListener("click", handleUseCurrentLocation);
  }

  function initWeatherModule() {
    applyWeatherSummary(WEATHER_FALLBACK, true);
  }

  function initSubmissionModule() {
    elements.recommendForm.addEventListener("submit", handleRecommendSubmit);
    elements.saveRecommendationButton.addEventListener("click", handleSaveRecommendation);
  }

  function initResultModule() {
    renderEmptyItems();
    elements.backToComposeButton.addEventListener("click", handleBackToCompose);
    elements.developerToggleButton.addEventListener("click", toggleDeveloperPanel);
  }

  async function preloadSampleImageOnInit() {
    try {
      const response = await fetch(SAMPLE_IMAGE_URL);
      if (!response.ok) {
        throw new Error("샘플 이미지를 불러오지 못했습니다.");
      }

      const sampleBlob = await response.blob();
      if (
        RECOMMENDATION_IMAGE_MAX_BYTES > 0 &&
        sampleBlob.size > RECOMMENDATION_IMAGE_MAX_BYTES
      ) {
        throw new Error("샘플 이미지 크기가 업로드 제한을 초과했습니다.");
      }

      const sampleFile = new File([sampleBlob], SAMPLE_IMAGE_FILE_NAME, {
        type: sampleBlob.type || "image/webp",
      });
      const imageBase64 = await fileToBase64(sampleFile);
      // 초기 로딩 시 샘플 이미지를 업로드된 이미지처럼 동일한 상태로 맞춘다.
      applyImagePreview(sampleFile, imageBase64);
      elements.imageMetaText.textContent = `${SAMPLE_IMAGE_FILE_NAME} · ${formatBytes(
        sampleFile.size
      )}`;
    } catch (error) {
      console.warn("초기 샘플 이미지 적용 실패", error);
    }
  }

  async function handleRecommendSubmit(event) {
    event.preventDefault();

    if (state.isSubmitting) {
      return;
    }

    try {
      validateBeforeSubmit();
      setSubmitting(true);
      setFeedback("", "");

      const payload = buildRequestPayload();
      const body = await requestJson("/api/recommendations/debug", {
        method: "POST",
        body: payload,
      });

      state.currentResult = body;
      state.currentRequestPayload = payload;
      state.hasSavedCurrentResult = false;
      state.currentDebugResult = body;
      renderResult(body, payload.naturalText);
      updateSaveButtonState();
      toggleScreen("result");
      setFeedback("추천 결과를 불러왔습니다.", "success");
    } catch (error) {
      console.error("추천 요청 실패", error);
      setFeedback(error.message || "추천 요청에 실패했습니다.", "error");
    } finally {
      setSubmitting(false);
    }
  }

  function handleBackToCompose() {
    toggleScreen("compose");
    setDeveloperPanelOpen(false);
    setFeedback("", "");
  }

  async function handleSaveRecommendation() {
    if (state.isSaving || state.hasSavedCurrentResult) {
      return;
    }

    if (!state.currentRequestPayload) {
      setFeedback("먼저 추천 결과를 생성해주세요.", "error");
      return;
    }
    if (!state.currentDebugResult) {
      setFeedback("저장할 추천 결과가 없습니다. 다시 추천을 생성해주세요.", "error");
      return;
    }

    try {
      setSaving(true);
      setFeedback("", "");

      await requestJson("/api/recommendations/debug/save", {
        method: "POST",
        body: {
          request: state.currentRequestPayload,
          debugResult: state.currentDebugResult,
        },
      });

      state.hasSavedCurrentResult = true;
      setFeedback("추천 결과를 저장했습니다.", "success");
    } catch (error) {
      console.error("추천 저장 실패", error);
      setFeedback(error.message || "추천 저장에 실패했습니다.", "error");
    } finally {
      setSaving(false);
      updateSaveButtonState();
    }
  }

  async function handleUseCurrentLocation() {
    try {
      setFeedback("", "");
      const position = await getLocationOrDefault();
      state.selectedPosition = { lat: position.lat, lon: position.lon };
      await updateMapPosition(position.lat, position.lon);
      await refreshWeather(position.lat, position.lon, position.isDefault);
    } catch (error) {
      console.error("현재 위치 갱신 실패", error);
      setFeedback("현재 위치를 다시 불러오지 못했습니다.", "error");
    }
  }

  async function handleImageChange(event) {
    const imageFile = event.target.files && event.target.files[0];
    if (!imageFile) {
      clearImageState();
      return;
    }

    if (
      RECOMMENDATION_IMAGE_MAX_BYTES > 0 &&
      imageFile.size > RECOMMENDATION_IMAGE_MAX_BYTES
    ) {
      clearImageState();
      setFeedback("이미지 크기가 업로드 제한을 초과했습니다.", "error");
      return;
    }

    try {
      const imageBase64 = await fileToBase64(imageFile);
      applyImagePreview(imageFile, imageBase64);
      elements.imageMetaText.textContent = `${imageFile.name} · ${formatBytes(
        imageFile.size
      )}`;
      setFeedback("", "");
    } catch (error) {
      console.error("이미지 처리 실패", error);
      clearImageState();
      setFeedback("이미지 파일을 읽지 못했습니다.", "error");
    }
  }

  async function initializeLocationAndMap() {
    const position = await getLocationOrDefault();
    state.selectedPosition = { lat: position.lat, lon: position.lon };
    await ensureKakaoMapLoaded();
    initializeMap(position.lat, position.lon);
    await refreshWeather(position.lat, position.lon, position.isDefault);
  }

  async function ensureKakaoMapLoaded() {
    if (window.kakao && window.kakao.maps) {
      await loadKakaoMapsLibraries();
      return;
    }

    if (!KAKAO_MAP_API_KEY) {
      throw new Error("카카오 지도 키가 설정되지 않았습니다.");
    }

    await new Promise((resolve, reject) => {
      if (document.getElementById(MAP_SCRIPT_ID)) {
        resolve();
        return;
      }

      const script = document.createElement("script");
      script.id = MAP_SCRIPT_ID;
      script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(
        KAKAO_MAP_API_KEY
      )}&autoload=false&libraries=services`;
      script.onload = resolve;
      script.onerror = () => reject(new Error("카카오 지도 스크립트를 불러오지 못했습니다."));
      document.head.appendChild(script);
    });

    await loadKakaoMapsLibraries();
  }

  async function loadKakaoMapsLibraries() {
    await new Promise((resolve) => {
      window.kakao.maps.load(resolve);
    });
  }

  function initializeMap(lat, lon) {
    const position = new window.kakao.maps.LatLng(lat, lon);
    state.map = new window.kakao.maps.Map(elements.map, {
      center: position,
      level: MAP_LEVEL,
    });
    state.marker = new window.kakao.maps.Marker({
      position,
      map: state.map,
    });
    state.geocoder = new window.kakao.maps.services.Geocoder();

    window.kakao.maps.event.addListener(state.map, "click", async (mouseEvent) => {
      const clickedLatLng = mouseEvent.latLng;
      const nextLat = clickedLatLng.getLat();
      const nextLon = clickedLatLng.getLng();
      state.selectedPosition = { lat: nextLat, lon: nextLon };
      updateMarkerPosition(nextLat, nextLon);
      await refreshWeather(nextLat, nextLon, false);
    });
  }

  async function updateMapPosition(lat, lon) {
    if (!state.map || !state.marker) {
      return;
    }
    const position = new window.kakao.maps.LatLng(lat, lon);
    state.map.setCenter(position);
    state.marker.setPosition(position);
  }

  function updateMarkerPosition(lat, lon) {
    if (!state.map || !state.marker) {
      return;
    }
    const position = new window.kakao.maps.LatLng(lat, lon);
    state.marker.setPosition(position);
    state.map.panTo(position);
  }

  async function refreshWeather(lat, lon, isDefaultLocation) {
    const summary = await fetchWeatherSummary(lat, lon);
    state.weatherSummary = summary;
    state.selectedPosition = { lat, lon };
    applyWeatherSummary(summary, isDefaultLocation);
    await resolveAddress(lat, lon, isDefaultLocation);
  }

  async function fetchWeatherSummary(lat, lon) {
    return requestJson(`/api/main/summary?lat=${lat}&lon=${lon}`, {
      defaultErrorMessage: "날씨 정보를 가져오지 못했습니다.",
    });
  }

  async function resolveAddress(lat, lon, isDefaultLocation) {
    if (!state.geocoder) {
      setLocationText(DEFAULT_LOCATION_TEXT, isDefaultLocation);
      return;
    }

    await new Promise((resolve) => {
      state.geocoder.coord2Address(lon, lat, (result, status) => {
        if (status === window.kakao.maps.services.Status.OK && result[0]) {
          const address = result[0].road_address || result[0].address;
          const regionText = [address.region_1depth_name, address.region_2depth_name]
            .filter(Boolean)
            .join(" ");
          setLocationText(regionText || DEFAULT_LOCATION_TEXT, isDefaultLocation);
        } else {
          setLocationText(DEFAULT_LOCATION_TEXT, isDefaultLocation);
        }
        resolve();
      });
    });
  }

  function setLocationText(locationText, isDefaultLocation) {
    elements.locationLabel.textContent = locationText;
    elements.locationSubLabel.textContent = isDefaultLocation
      ? "위치 권한이 없어 기본 위치 기준으로 표시합니다."
      : "지도에서 위치를 바꾸면 날씨도 함께 바뀝니다.";
  }

  function applyWeatherSummary(summary, isDefaultLocation) {
    const safeSummary = summary || WEATHER_FALLBACK;
    elements.weatherStatusBadge.textContent =
      WEATHER_LABELS[safeSummary.weatherStatus] || safeSummary.weatherStateKo || "날씨";
    elements.weatherTemperature.textContent = `${Math.round(
      safeSummary.temperature ?? 0
    )}°`;
    elements.weatherDescription.textContent = resolveWeatherDescription(safeSummary);
    elements.weatherFeelsLike.textContent = `${Math.round(
      safeSummary.feelsLike ?? 0
    )}°`;
    elements.weatherHumidity.textContent = `${safeSummary.humidity ?? 0}%`;
    elements.weatherWind.textContent = `${safeSummary.windMs ?? 0} m/s`;
    elements.weatherPrecipitation.textContent = `${safeSummary.precipMm ?? 0} mm`;
    setLocationText(safeSummary.locationText || DEFAULT_LOCATION_TEXT, isDefaultLocation);
  }

  function resolveWeatherDescription(summary) {
    // 1순위: 서버에서 내려준 설명이 있으면 그대로 사용한다.
    if (hasText(summary.weatherDesc)) {
      return summary.weatherDesc.trim();
    }

    // 2순위: 강수 정보가 있으면 우산 안내 문구를 노출한다.
    const precipitation = Number(summary.precipMm ?? 0);
    if (summary.todayRain === true || precipitation > 0) {
      return WEATHER_DESCRIPTION_POLICY.rainDescription;
    }

    // 3순위: 한글 상태명이 있으면 자연어 설명으로 변환한다.
    if (hasText(summary.weatherStateKo)) {
      return `${summary.weatherStateKo.trim()} ${WEATHER_DESCRIPTION_POLICY.stateSuffix}`;
    }

    const weatherLabel = WEATHER_LABELS[summary.weatherStatus];
    if (hasText(weatherLabel)) {
      return `${weatherLabel} ${WEATHER_DESCRIPTION_POLICY.stateSuffix}`;
    }

    // 마지막 폴백: 사용자에게 항상 안내 문구를 제공한다.
    return WEATHER_DESCRIPTION_POLICY.defaultDescription;
  }

  function validateBeforeSubmit() {
    if (!elements.naturalText.value.trim()) {
      throw new Error("추천 요청 문장을 입력해주세요.");
    }

    if (!state.imageBase64) {
      throw new Error("추천을 위해 사진을 업로드해주세요.");
    }

    if (!state.weatherSummary) {
      throw new Error("날씨 정보를 아직 불러오지 못했습니다.");
    }
  }

  function buildRequestPayload() {
    return {
      naturalText: elements.naturalText.value.trim(),
      gender: elements.gender.value,
      weather: {
        status: state.weatherSummary.weatherStatus,
        temperature: state.weatherSummary.temperature,
        feelsLike: state.weatherSummary.feelsLike,
      },
      imageBase64: state.imageBase64,
      imageMimeType: state.imageMimeType || "image/jpeg",
      brandEnabled: elements.brandEnabled.checked,
    };
  }

  function renderResult(result, naturalText) {
    elements.resultQueryText.textContent = naturalText;
    elements.resultMetaText.textContent = buildResultMetaText(result);
    elements.resultExplanation.textContent =
      result.aiExplanation || "추천 설명이 아직 준비되지 않았습니다.";

    const coordination = Array.isArray(result.coordination) ? result.coordination : [];
    const visibleCoordination = coordination.filter((item) =>
      shouldRenderCoordinationItem(item)
    );
    elements.resultItemCount.textContent = `${visibleCoordination.length}개 아이템`;

    if (!visibleCoordination.length) {
      renderEmptyItems("추천 아이템이 없습니다.");
      return;
    }

    elements.resultItems.innerHTML = visibleCoordination
      .map((item) => createItemMarkup(item))
      .join("");
    renderDeveloperPanel(result);
  }

  function buildResultMetaText(result) {
    const metaParts = [];
    if (result.tpoType) {
      metaParts.push(`상황 ${result.tpoType}`);
    }
    if (result.styleType) {
      metaParts.push(`스타일 ${result.styleType}`);
    }
    return metaParts.join(" · ");
  }

  function createItemMarkup(item) {
    const normalizedSlotKey = normalizeSlotKey(item && item.slotKey);
    const imageSource = resolveItemImageSource(item);
    const imageMarkup = imageSource
      ? `<img class="recommend-item-card__image" src="${escapeHtml(
          imageSource
        )}" alt="${escapeHtml(item.itemName || "추천 아이템")}">`
      : `<div class="recommend-item-card__image"></div>`;

    const priceText = item.isMyItem
      ? "main item"
      : typeof item.salePrice === "number"
      ? `${item.salePrice.toLocaleString("ko-KR")}원`
      : "가격 정보 없음";

    const actionMarkup = item.productDetailUrl
      ? `<a class="recommend-item-card__link" href="${escapeHtml(
          item.productDetailUrl
        )}" target="_blank" rel="noopener noreferrer">상품 보러가기</a>`
      : "";

    return `
      <article class="recommend-item-card">
        ${imageMarkup}
        <div class="recommend-item-card__body">
          <span class="recommend-item-card__slot">${escapeHtml(
            SLOT_LABELS[normalizedSlotKey] || item.slotKey || "추천"
          )}</span>
          <div>
            <p class="recommend-item-card__brand">${escapeHtml(
              item.brandName || "브랜드 정보 없음"
            )}</p>
            <h4 class="recommend-item-card__name">${escapeHtml(
              item.itemName || "상품명 정보 없음"
            )}</h4>
          </div>
          <p class="recommend-item-card__price">${escapeHtml(priceText)}</p>
          <p class="recommend-item-card__reason">${escapeHtml(
            item.reasoning || "추천 이유 정보가 없습니다."
          )}</p>
          ${actionMarkup}
        </div>
      </article>
    `;
  }

  function resolveItemImageSource(item) {
    if (item.isMyItem) {
      // 업로드 직후 결과 화면에서도 동일한 이미지를 재사용한다.
      return state.uploadedImageUrl || buildUploadedImageDataUrl();
    }
    return item.imageUrl || "";
  }

  function buildUploadedImageDataUrl() {
    if (!state.imageBase64) {
      return "";
    }
    return `data:${state.imageMimeType || "image/jpeg"};base64,${state.imageBase64}`;
  }

  function isEmptyCoordinationSlot(item) {
    const normalizedSlotKey = normalizeSlotKey(item && item.slotKey);
    if (!item || !OPTIONAL_HIDDEN_SLOTS.has(normalizedSlotKey)) {
      return false;
    }

    // 선택 슬롯은 실제 상품 근거가 없으면 결과 카드에서 숨긴다.
    return (
      !item.isMyItem &&
      !hasText(item.imageUrl) &&
      !hasText(item.productDetailUrl) &&
      typeof item.salePrice !== "number" &&
      !hasText(item.itemName)
    );
  }

  function shouldRenderCoordinationItem(item) {
    // 선택 슬롯이 비어 있으면 결과 카드에서 숨긴다.
    return !isEmptyCoordinationSlot(item);
  }

  function normalizeSlotKey(slotKey) {
    if (!hasText(slotKey)) {
      return "";
    }

    const upperSlotKey = slotKey.trim().toUpperCase();
    const slotKeyMap = {
      HEADWEAR: "HEADWEAR",
      HEADWEARS: "HEADWEAR",
      TOPS: "TOPS",
      BOTTOMS: "BOTTOMS",
      OUTERWEAR: "OUTERWEAR",
      SHOES: "SHOES",
      ACCESSORIES: "ACCESSORIES",
      ACCESSORY: "ACCESSORIES",
    };

    return slotKeyMap[upperSlotKey] || upperSlotKey;
  }

  function renderEmptyItems(message) {
    elements.resultItems.innerHTML = `
      <div class="recommend-empty-state">
        <p>${escapeHtml(message || "추천 결과가 이 영역에 표시됩니다.")}</p>
      </div>
    `;
    elements.resultItemCount.textContent = "";
  }

  function renderDeveloperPanel(result) {
    const safeResult = result || {};
    const slotSearchQueries = safeResult.slotSearchQueries || {};
    const rawBlueprint = safeResult.rawBlueprint || {};

    elements.developerSearchQueries.textContent = toPrettyJson(slotSearchQueries);
    elements.developerBlueprint.textContent = toPrettyJson(rawBlueprint);
  }

  function toggleScreen(screen) {
    const isResult = screen === "result";
    elements.composeView.classList.toggle("recommend-screen--hidden", isResult);
    elements.composeView.classList.toggle("recommend-screen--active", !isResult);
    elements.resultView.classList.toggle("recommend-screen--hidden", !isResult);
    elements.resultView.classList.toggle("recommend-screen--active", isResult);
  }

  function toggleDeveloperPanel() {
    setDeveloperPanelOpen(!state.developerPanelOpen);
  }

  function setDeveloperPanelOpen(isOpen) {
    state.developerPanelOpen = isOpen;
    elements.developerPanel.classList.toggle("recommend-dev-panel--hidden", !isOpen);
    elements.resultLayout.classList.toggle("recommend-result-layout--with-dev", isOpen);
    elements.developerToggleButton.setAttribute("aria-expanded", String(isOpen));
    elements.developerToggleButton.textContent = isOpen
      ? "개발자 영역 닫기"
      : "개발자 영역 보기";
  }

  function setSubmitting(isSubmitting) {
    state.isSubmitting = isSubmitting;
    elements.submitButton.disabled = isSubmitting;
    elements.submitButton.textContent = isSubmitting ? "추천 생성 중..." : "추천 받기";
    elements.loadingOverlay.classList.toggle(
      "recommend-loading-overlay--hidden",
      !isSubmitting
    );
    // 제출 상태가 바뀌면 저장 버튼 비활성 조건도 즉시 다시 계산한다.
    updateSaveButtonState();
  }

  function setSaving(isSaving) {
    state.isSaving = isSaving;
    updateSaveButtonState();
  }

  function updateSaveButtonState() {
    const isDisabled =
      state.isSaving || state.isSubmitting || !state.currentResult || state.hasSavedCurrentResult;
    elements.saveRecommendationButton.disabled = isDisabled;

    if (state.isSaving) {
      elements.saveRecommendationButton.textContent = "저장 중...";
      return;
    }

    if (state.hasSavedCurrentResult) {
      elements.saveRecommendationButton.textContent = "저장 완료";
      return;
    }

    elements.saveRecommendationButton.textContent = "저장";
  }

  function setFeedback(message, tone) {
    elements.feedbackMessage.textContent = message || "";
    elements.feedbackMessage.classList.remove("is-error", "is-success");
    if (tone === "error") {
      elements.feedbackMessage.classList.add("is-error");
    }
    if (tone === "success") {
      elements.feedbackMessage.classList.add("is-success");
    }
  }

  function clearImageState() {
    state.imageBase64 = "";
    state.imageMimeType = "";
    state.uploadedImageUrl = "";
    elements.imageFile.value = "";
    revokeImagePreviewObjectUrl();
    elements.imagePreview.removeAttribute("src");
    elements.imagePreviewCard.classList.remove("recommend-image-card--filled");
    elements.imageMetaText.textContent = "아직 업로드된 사진이 없습니다.";
  }

  function applyImagePreview(file, imageBase64) {
    revokeImagePreviewObjectUrl();
    state.imageBase64 = imageBase64;
    state.imageMimeType = file.type || "image/jpeg";

    const objectUrl = URL.createObjectURL(file);
    state.uploadedImageUrl = objectUrl;
    elements.imagePreview.src = objectUrl;
    elements.imagePreview.dataset.objectUrl = objectUrl;
    elements.imagePreviewCard.classList.add("recommend-image-card--filled");
  }

  function revokeImagePreviewObjectUrl() {
    if (elements.imagePreview && elements.imagePreview.dataset.objectUrl) {
      URL.revokeObjectURL(elements.imagePreview.dataset.objectUrl);
      delete elements.imagePreview.dataset.objectUrl;
    }
  }

  function fileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = typeof reader.result === "string" ? reader.result : "";
        resolve(result.includes(",") ? result.split(",")[1] : result);
      };
      reader.onerror = () => reject(new Error("이미지 인코딩에 실패했습니다."));
      reader.readAsDataURL(file);
    });
  }

  function getLocationOrDefault() {
    return new Promise((resolve) => {
      if (!navigator.geolocation) {
        resolve({ ...DEFAULT_POSITION, isDefault: true });
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            lat: position.coords.latitude,
            lon: position.coords.longitude,
            isDefault: false,
          });
        },
        () => {
          resolve({ ...DEFAULT_POSITION, isDefault: true });
        },
        {
          enableHighAccuracy: true,
          timeout: 8000,
          maximumAge: 300000,
        }
      );
    });
  }

  function formatBytes(size) {
    if (!size) {
      return "0 B";
    }
    const units = ["B", "KB", "MB", "GB"];
    const exponent = Math.min(Math.floor(Math.log(size) / Math.log(1024)), units.length - 1);
    const value = size / Math.pow(1024, exponent);
    return `${value.toFixed(value >= 10 || exponent === 0 ? 0 : 1)} ${units[exponent]}`;
  }

  function resolveErrorMessage(body) {
    return (
      body?.message ||
      body?.error?.message ||
      "추천 요청을 처리하지 못했습니다."
    );
  }

  async function requestJson(url, options = {}) {
    const {
      method = "GET",
      body,
      headers = {},
      defaultErrorMessage = "요청을 처리하지 못했습니다.",
    } = options;

    const fetchOptions = {
      method,
      headers: {
        "Content-Type": "application/json",
        ...headers,
      },
    };
    if (body !== undefined) {
      fetchOptions.body = typeof body === "string" ? body : JSON.stringify(body);
    }

    const response = await fetch(url, fetchOptions);
    const parsedBody = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(resolveErrorMessage(parsedBody) || defaultErrorMessage);
    }
    return parsedBody;
  }

  function escapeHtml(value) {
    return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
  }

  function hasText(value) {
    return typeof value === "string" && value.trim().length > 0;
  }

  function toPrettyJson(value) {
    try {
      return JSON.stringify(value ?? {}, null, 2);
    } catch (error) {
      console.error("디버그 데이터 직렬화 실패", error);
      return "{}";
    }
  }
})();
