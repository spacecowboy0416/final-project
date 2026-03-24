(function (window) {
  const NS_KEY = "RecommendationPage";
  const ns = (window[NS_KEY] = window[NS_KEY] || {});

  // 공통 DOM 조회 유틸이다.
  const byId = (id) => document.getElementById(id);

  // 추천 페이지 공통 코어를 생성한다.
  ns.createCore = function createCore(root) {
    const pick = (ids) => Object.fromEntries(ids.map((id) => [id, byId(id)]));

    const C = {
      api: { recommend: "/api/recommendations", save: "/api/recommendations/save", weather: "/api/main/summary" },
      map: {
        sdkId: "kakao-map-script",
        level: 4,
        defaultPos: { lat: 37.5665, lon: 126.978 },
        // 위치 기본 표시는 화면 요구사항에 맞춰 강남구 기준으로 노출한다.
        defaultText: "서울 강남구",
        subDefault: "",
        subSelected: "",
      },
      ui: { hidden: "rc-hidden" },
      // 업로드 메타 기본 문구는 비워서 화면 안내 문구를 노출하지 않는다.
      img: { mime: "image/jpeg", emptyMeta: "" },
      msg: {
        submitIdle: "코디 생성",
        submitBusy: "코디 생성 중...",
        saveIdle: "저장하기",
        saveBusy: "저장 중...",
        saveDone: "저장됨",
        noItems: "추천 아이템이 없습니다.",
        saveNeedLogin: "저장 기능은 로그인 후 사용할 수 있습니다. 로그인 페이지로 이동하시겠습니까?",
        saveNoResult: "저장할 추천 결과가 없습니다. 먼저 추천을 생성해주세요.",
        weatherError: "날씨 정보를 가져오지 못했습니다.",
        recommendError: "추천 요청을 처리하지 못했습니다.",
      },
      tone: { error: "rc-feedback--error", success: "rc-feedback--success", default: "rc-feedback--default" },
    };

    const S = {
      img: { b64: "", mime: "", url: "" },
      weather: null,
      persist: null,
      submitBusy: false,
      saveBusy: false,
      saved: false,
      saveEnabled: root.dataset.recommendationSaveEnabled === "true",
    };

    const E = pick([
      "recommendForm",
      "composeView",
      "resultView",
      "naturalText",
      "submitButton",
      "imageFile",
      "imagePreview",
      "imageEmptyState",
      "imageMetaText",
      "gender",
      "brandEnabled",
      "useCurrentLocationButton",
      "locationLabel",
      "locationSubLabel",
      "weatherStatusBadge",
      "weatherTemperature",
      "weatherDescription",
      "weatherFeelsLike",
      "weatherHumidity",
      "weatherWind",
      "weatherPrecipitation",
      "loadingOverlay",
      "feedbackMessage",
      "resultQueryText",
      "resultMetaText",
      "resultExplanation",
      "resultItems",
      "resultItemCount",
      "saveRecommendationButton",
      "backToComposeButton",
      "map",
    ]);

    const hide = (el, on) => el?.classList?.toggle(C.ui.hidden, on);
    const esc = (v) => String(v).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");

    const request = async (url, opt = {}) => {
      const { method = "GET", body, headers = {}, defaultErrorMessage = C.msg.recommendError } = opt;
      const res = await fetch(url, { method, headers: { "Content-Type": "application/json", ...headers }, body: body === undefined ? undefined : JSON.stringify(body) });
      const out = await res.json().catch(() => ({}));
      if (res.ok) return out;
      const err = new Error(out?.message ?? out?.error?.message ?? defaultErrorMessage);
      err.status = res.status;
      err.code = out?.code ?? out?.error?.code;
      throw err;
    };

    // 결과 카드 템플릿: 수치 스타일은 CSS 클래스에서만 관리한다.
    const card = (i = {}) => {
      const img = i?.isMyItem ? S.img.url || (S.img.b64 ? `data:${S.img.mime};base64,${S.img.b64}` : "") : i?.imageUrl ?? "";
      const price = i?.isMyItem ? "내 옷" : typeof i?.salePrice === "number" ? `${i.salePrice.toLocaleString("ko-KR")}원` : "가격 정보 없음";
      const label = i?.label ?? i?.slotLabel ?? i?.slotKey ?? "추천";
      const link = i?.productDetailUrl ? `<a class="rc-btn rc-btn-ghost rc-btn-xs recommend-item-link" href="${esc(i.productDetailUrl)}" target="_blank" rel="noopener noreferrer">상품 보러가기</a>` : "";
      return `<article class="rc-card recommend-item-card">${img ? `<img class="recommend-item-image" src="${esc(img)}" alt="${esc(i?.itemName ?? "추천 아이템")}">` : `<div class="recommend-item-image--placeholder"></div>`}<div class="recommend-item-body"><span class="recommend-item-slot">${esc(String(label))}</span><div><p class="recommend-item-brand">${esc(i?.brandName ?? "브랜드 정보 없음")}</p><h4 class="recommend-item-name">${esc(i?.itemName ?? "상품명 정보 없음")}</h4></div><p class="recommend-item-price">${esc(price)}</p><p class="recommend-item-reason">${esc(i?.reasoning ?? "추천 이유 정보가 없습니다.")}</p>${link}</div></article>`;
    };

    const askLogin = () => {
      if (typeof window.showGlobalModal === "function") {
        return window.showGlobalModal("로그인 필요", C.msg.saveNeedLogin, "confirm", () => {
          window.location.href = "/login";
        });
      }
      if (window.confirm(C.msg.saveNeedLogin)) window.location.href = "/login";
    };

    const locate = () =>
      new Promise((ok) => {
        if (!navigator.geolocation) return ok({ ...C.map.defaultPos, isDefault: true });
        navigator.geolocation.getCurrentPosition(
          (p) => ok({ lat: p.coords.latitude, lon: p.coords.longitude, isDefault: false }),
          () => ok({ ...C.map.defaultPos, isDefault: true }),
          { enableHighAccuracy: true, timeout: 8000, maximumAge: 300000 }
        );
      });

    return { root, C, S, E, byId, hide, esc, request, card, askLogin, locate };
  };
})(window);
