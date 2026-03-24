(function (window) {
  const NS_KEY = "RecommendationPage";
  const ns = (window[NS_KEY] = window[NS_KEY] || {});

  // 추천 페이지 서비스 모듈을 생성한다.
  ns.createService = function createService(core) {
    const { root, C, S, E, byId, hide, esc, request, card, askLogin } = core;

    // 화면 모듈: 출력과 버튼 상태를 담당한다.
    const View = {
      feedback(msg = "", tone = "default") {
        if (!E.feedbackMessage) return;
        E.feedbackMessage.textContent = msg;
        E.feedbackMessage.classList.remove(C.tone.error, C.tone.success, C.tone.default);
        E.feedbackMessage.classList.add(C.tone[tone] ?? C.tone.default);
      },
      toggle(screen) {
        const result = screen === "result";
        hide(E.composeView, result);
        hide(E.resultView, !result);
      },
      submitting(on) {
        S.submitBusy = on;
        if (E.submitButton) {
          E.submitButton.disabled = on;
          E.submitButton.textContent = on ? C.msg.submitBusy : C.msg.submitIdle;
        }
        hide(E.loadingOverlay, !on);
      },
      preview(on) {
        hide(E.imagePreview, !on);
        hide(E.imageEmptyState, on);
      },
      saveBtn() {
        const b = E.saveRecommendationButton;
        if (!b) return;
        b.disabled = !S.persist || S.saveBusy || S.saved;
        b.textContent = S.saveBusy ? C.msg.saveBusy : S.saved ? C.msg.saveDone : C.msg.saveIdle;
      },
      weather(w = {}, isDefault = false, addr) {
        E.weatherStatusBadge && (E.weatherStatusBadge.textContent = w?.label ?? w?.weatherStateKo ?? w?.weatherStatus ?? "날씨");
        E.weatherDescription && (E.weatherDescription.textContent = w?.displayMessage ?? w?.weatherDesc ?? "");
        E.weatherTemperature && (E.weatherTemperature.textContent = `${Math.round(w?.temperature ?? 0)}°`);
        E.weatherFeelsLike && (E.weatherFeelsLike.textContent = `${Math.round(w?.feelsLike ?? 0)}°`);
        E.weatherHumidity && (E.weatherHumidity.textContent = `${w?.humidity ?? 0}%`);
        E.weatherWind && (E.weatherWind.textContent = `${w?.windMs ?? 0} m/s`);
        E.weatherPrecipitation && (E.weatherPrecipitation.textContent = `${w?.precipMm ?? 0} mm`);
        E.locationLabel && (E.locationLabel.textContent = addr ?? w?.locationText ?? C.map.defaultText);
        E.locationSubLabel && (E.locationSubLabel.textContent = isDefault ? C.map.subDefault : C.map.subSelected);
      },
      result(out = {}, query = "") {
        E.resultQueryText && (E.resultQueryText.textContent = query);
        E.resultMetaText && (E.resultMetaText.textContent = [out?.tpoType ? `상황 ${out.tpoType}` : "", out?.styleType ? `스타일 ${out.styleType}` : ""].filter(Boolean).join(" · "));
        E.resultExplanation && (E.resultExplanation.textContent = out?.aiExplanation ?? "추천 설명이 아직 준비되지 않았습니다.");
        const items = Array.isArray(out?.coordination) ? out.coordination : [];
        E.resultItemCount && (E.resultItemCount.textContent = items.length ? `${items.length}개 아이템` : "");
        E.resultItems && (E.resultItems.innerHTML = items.length ? items.map(card).join("") : `<div class="rc-card recommend-empty-card"><p>${esc(C.msg.noItems)}</p></div>`);
      },
    };

    // 지도 모듈: SDK/마커/주소 조회를 담당한다.
    const Map = (() => {
      let map = null;
      let marker = null;
      let geocoder = null;

      const load = async () => {
        if (!window.kakao?.maps) {
          const key = root.dataset.kakaoMapApiKey ?? "";
          if (!key) throw new Error("카카오 지도 키가 설정되지 않았습니다.");
          await new Promise((ok, no) => {
            if (byId(C.map.sdkId)) return ok();
            const s = document.createElement("script");
            s.id = C.map.sdkId;
            s.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(key)}&autoload=false&libraries=services`;
            s.onload = ok;
            s.onerror = () => no(new Error("카카오 지도 스크립트 로딩 실패"));
            document.head.appendChild(s);
          });
        }
        await new Promise((ok) => window.kakao.maps.load(ok));
      };

      const move = (lat, lon, pan = false) => {
        if (!map || !marker) return;
        const pos = new window.kakao.maps.LatLng(lat, lon);
        marker.setPosition(pos);
        pan ? map.panTo(pos) : map.setCenter(pos);
      };

      const addr = async (lat, lon) => {
        if (!geocoder) return C.map.defaultText;
        return new Promise((ok) => {
          geocoder.coord2Address(lon, lat, (result, status) => {
            const a = result?.[0]?.road_address ?? result?.[0]?.address;
            const text = [a?.region_1depth_name, a?.region_2depth_name].filter(Boolean).join(" ");
            ok(status === window.kakao.maps.services.Status.OK ? text || C.map.defaultText : C.map.defaultText);
          });
        });
      };

      const init = async (pos, onPick) => {
        await load();
        const p = new window.kakao.maps.LatLng(pos.lat, pos.lon);
        map = new window.kakao.maps.Map(E.map, { center: p, level: C.map.level });
        marker = new window.kakao.maps.Marker({ position: p, map });
        geocoder = new window.kakao.maps.services.Geocoder();
        window.kakao.maps.event.addListener(map, "click", (ev) => {
          const lat = ev?.latLng?.getLat?.() ?? C.map.defaultPos.lat;
          const lon = ev?.latLng?.getLng?.() ?? C.map.defaultPos.lon;
          move(lat, lon, true);
          onPick?.({ lat, lon });
        });
      };

      return { init, move, addr };
    })();

    // 이미지 모듈: 파일 검증/인코딩/미리보기를 담당한다.
    const Image = (() => {
      const max = Number(root.dataset.recommendationImageMaxBytes ?? 0);

      const revoke = () => {
        const u = E.imagePreview?.dataset?.objectUrl;
        if (!u) return;
        URL.revokeObjectURL(u);
        delete E.imagePreview.dataset.objectUrl;
      };

      const clear = () => {
        revoke();
        S.img = { b64: "", mime: "", url: "" };
        E.imageFile && (E.imageFile.value = "");
        E.imagePreview?.removeAttribute("src");
        E.imageMetaText && (E.imageMetaText.textContent = C.img.emptyMeta);
        View.preview(false);
      };

      const to64 = (f) =>
        new Promise((ok, no) => {
          const r = new FileReader();
          r.onload = () => {
            const t = typeof r.result === "string" ? r.result : "";
            ok(t.includes(",") ? t.split(",")[1] : t);
          };
          r.onerror = () => no(new Error("이미지 인코딩에 실패했습니다."));
          r.readAsDataURL(f);
        });

      const set = async (f) => {
        if (!f) return clear();
        if (max > 0 && f.size > max) {
          clear();
          throw new Error("이미지 크기가 업로드 제한을 초과했습니다.");
        }
        revoke();
        const url = URL.createObjectURL(f);
        S.img = { b64: await to64(f), mime: f.type || C.img.mime, url };
        if (E.imagePreview) {
          E.imagePreview.src = url;
          E.imagePreview.dataset.objectUrl = url;
        }
        E.imageMetaText && (E.imageMetaText.textContent = `${f.name} · ${Math.round((f.size / 1024) * 10) / 10} KB`);
        View.preview(true);
      };

      return { set };
    })();

    // 날씨 모듈: API 조회 후 화면 반영을 담당한다.
    const Weather = {
      async refresh(lat, lon, isDefault = false) {
        const w = await request(`${C.api.weather}?lat=${lat}&lon=${lon}`, { defaultErrorMessage: C.msg.weatherError });
        S.weather = w;
        View.weather(w, isDefault, await Map.addr(lat, lon));
      },
    };

    // 추천 모듈: FormData 기반 payload 생성/요청/저장을 담당한다.
    const Recommend = {
      payload() {
        const fd = new FormData(E.recommendForm);
        fd.set("brandEnabled", String(E.brandEnabled?.checked ?? false));
        fd.set("imageBase64", S.img.b64 ?? "");
        fd.set("imageMimeType", S.img.mime || C.img.mime);
        fd.set("weatherStatus", S.weather?.status ?? S.weather?.weatherStatus ?? "");
        fd.set("weatherTemperature", String(S.weather?.temperature ?? 0));
        fd.set("weatherFeelsLike", String(S.weather?.feelsLike ?? 0));
        const r = Object.fromEntries(fd.entries());
        return {
          naturalText: String(r.naturalText ?? "").trim(),
          gender: r.gender,
          weather: { status: r.weatherStatus, temperature: Number(r.weatherTemperature ?? 0), feelsLike: Number(r.weatherFeelsLike ?? 0) },
          imageBase64: r.imageBase64,
          imageMimeType: r.imageMimeType,
          brandEnabled: r.brandEnabled === "true",
        };
      },
      persist(out, p) {
        if (!Array.isArray(out?.coordination) || !out?.tpoType || !out?.styleType || !out?.queryMap || typeof out.queryMap !== "object") return null;
        return {
          naturalText: p.naturalText,
          weather: p.weather,
          tpoType: out.tpoType,
          styleType: out.styleType,
          aiExplanation: out?.aiExplanation ?? "",
          mainItemImageBase64: p.imageBase64,
          mainItemImageMimeType: p.imageMimeType || C.img.mime,
          coordination: out.coordination,
          queryMap: out.queryMap,
        };
      },
      validate(p) {
        if (!p.naturalText) throw new Error("추천 요청 문장을 입력해주세요.");
        if (!p.imageBase64) throw new Error("추천을 위해 사진을 업로드해주세요.");
        if (!p.weather?.status) throw new Error("날씨 정보를 아직 불러오지 못했습니다.");
      },
      async submit(ev) {
        ev.preventDefault();
        if (S.submitBusy) return;
        try {
          View.feedback();
          const p = Recommend.payload();
          Recommend.validate(p);
          View.submitting(true);
          const out = await request(C.api.recommend, { method: "POST", body: p });
          S.persist = Recommend.persist(out, p);
          S.saved = false;
          S.saveBusy = false;
          View.saveBtn();
          View.result(out, p.naturalText);
          View.toggle("result");
          View.feedback("추천 결과를 불러왔습니다.", "success");
        } catch (e) {
          View.feedback(e?.message ?? C.msg.recommendError, "error");
        } finally {
          View.submitting(false);
        }
      },
      async save() {
        if (S.saveBusy || S.saved) return;
        if (!S.persist) return View.feedback(C.msg.saveNoResult, "error");
        if (!S.saveEnabled) return askLogin();
        try {
          S.saveBusy = true;
          View.saveBtn();
          const out = await request(C.api.save, { method: "POST", body: S.persist });
          S.saved = true;
          View.feedback(`코디가 저장되었습니다. (recId: ${out?.recId ?? "-"})`, "success");
        } catch (e) {
          if (e?.status === 401 || e?.code === "T100") return askLogin();
          View.feedback(e?.message ?? "추천 저장에 실패했습니다.", "error");
        } finally {
          S.saveBusy = false;
          View.saveBtn();
        }
      },
    };

    return { View, Map, Image, Weather, Recommend };
  };
})(window);
