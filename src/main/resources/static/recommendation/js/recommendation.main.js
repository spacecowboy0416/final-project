(function (window) {
  const NS_KEY = "RecommendationPage";
  const ns = (window[NS_KEY] = window[NS_KEY] || {});

  // 추천 페이지 이벤트를 연결한다.
  const bind = (core, service) => {
    const { E, locate, C } = core;
    const { View, Map, Image, Weather, Recommend } = service;

    E.imageFile?.addEventListener("change", async (ev) => {
      try {
        View.feedback();
        await Image.set(ev?.target?.files?.[0]);
      } catch (e) {
        View.feedback(e?.message ?? C.msg.imageReadFail, "error");
      }
    });

    E.useCurrentLocationButton?.addEventListener("click", async () => {
      try {
        View.feedback();
        const p = await locate();
        Map.move(p.lat, p.lon);
        await Weather.refresh(p.lat, p.lon);
      } catch (e) {
        View.feedback(e?.message ?? C.msg.locationReloadFail, "error");
      }
    });

    E.recommendForm?.addEventListener("submit", Recommend.submit);
    E.saveRecommendationButton?.addEventListener("click", Recommend.save);
    E.backToComposeButton?.addEventListener("click", () => {
      View.toggle("compose");
      View.feedback();
    });

  };

  // 추천 페이지를 초기화한다.
  const boot = async () => {
    const root = document.getElementById("recommendRoot");
    if (!root) return;

    if (typeof ns.createCore !== "function" || typeof ns.createService !== "function") {
      console.error("추천 페이지 모듈 로딩 누락");
      return;
    }

    const core = ns.createCore(root);
    const service = ns.createService(core);
    const { E, C } = core;
    const { View, Map, Weather } = service;

    const missing = ["recommendForm", "submitButton", "imageFile", "map"].filter((k) => !E[k]);
    if (missing.length) {
      console.error("추천 페이지 필수 요소 누락", missing);
      return;
    }

    bind(core, service);
    E.naturalText && (E.naturalText.value = root.dataset.initialNaturalText ?? "");
    View.preview(false);
    View.saveBtn();
    View.result();
    View.weather({}, true, C.map.defaultText);

    try {
      const p = await core.locate();
      await Map.init(p, async ({ lat, lon }) => {
        try {
          await Weather.refresh(lat, lon);
        } catch (e) {
          View.feedback(e?.message ?? C.msg.weatherError, "error");
        }
      });
      await Weather.refresh(p.lat, p.lon);
    } catch (_) {
      View.weather({}, true, C.map.defaultText);
      View.feedback(C.msg.weatherFallback, "error");
    }
  };

  document.addEventListener("DOMContentLoaded", boot);
})(window);
