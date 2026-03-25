// board-common.js
// 게시판 전역 공통 유틸 모음
(() => {
    // ===== 슬롯 정렬 기준 =====
    const SLOT_ORDER = ["headwear", "top", "bottom", "outer", "shoes", "accessory"];

    // ===== 날씨 코드 한글 변환 =====
    function formatWeather(value) {
        const map = {
            clear: "맑음",
            partly_cloudy: "구름 조금",
            cloudy: "흐림",
            windy: "바람 많음",
            rain: "비",
            cloudy_rain: "흐리고 비",
            thunderstorm: "뇌우",
            thunderstorm_rain: "뇌우와 비",
            snow: "눈",
            cloudy_snow: "흐리고 눈",
            sleet: "진눈깨비",
            hail: "우박"
        };

        return map[String(value ?? "").toLowerCase()] || value || "-";
    }
	
	//
	function formatStyle(value) {
	    const map = {
	        minimal: "미니멀",
	        casual: "캐주얼",
	        street: "스트릿",
	        classic: "클래식",
	        lovely: "러블리",
	        amikaji: "아메카지",
	        gorpcore: "고프코어",
	        chic: "시크"
	    };

	    return map[String(value ?? "").toLowerCase()] || value || "-";
	}
	
	//
	function formatTpo(value) {
	    const map = {
	        daily: "일상",
	        office: "출근",
	        date: "데이트",
	        wedding: "하객",
	        formal: "격식",
	        exercise: "운동",
	        travel: "여행",
	        camping: "캠핑"
	    };

	    return map[String(value ?? "").toLowerCase()] || value || "-";
	}

    // ===== 슬롯 key 정규화 =====
    function normalizeSlotKey(value) {
        const slot = String(value ?? "").toLowerCase().trim();

        const map = {
            headwear: "headwear",
            hat: "headwear",
            cap: "headwear",

            top: "top",
            tops: "top",

            bottom: "bottom",
            bottoms: "bottom",
            pants: "bottom",

            outer: "outer",
            outerwear: "outer",

            shoe: "shoes",
            shoes: "shoes",
            sneaker: "shoes",
            sneakers: "shoes",

            accessory: "accessory",
            accessories: "accessory",
            acc: "accessory"
        };

        return map[slot] || slot;
    }

    // ===== 슬롯 정렬 순서 =====
    function getSlotOrderIndex(value) {
        const normalized = normalizeSlotKey(value);
        const index = SLOT_ORDER.indexOf(normalized);
        return index === -1 ? 999 : index;
    }

    // ===== 슬롯 배열 정렬 =====
    function sortItemsBySlotOrder(items) {
        return [...(items || [])].sort((a, b) => {
            const aKey = a?.slotKey ?? a?.key ?? a?.label ?? "";
            const bKey = b?.slotKey ?? b?.key ?? b?.label ?? "";
            return getSlotOrderIndex(aKey) - getSlotOrderIndex(bKey);
        });
    }

    // ===== 슬롯 라벨 표시용 포맷 =====
    function formatSlotLabel(value) {
        const slot = String(value ?? "").toLowerCase().trim();

        const map = {
            headwear: "HEADWEAR",
            top: "TOP",
            tops: "TOP",
            outer: "OUTER",
            outerwear: "OUTER",
            bottom: "BOTTOM",
            bottoms: "BOTTOM",
            shoe: "SHOES",
            shoes: "SHOES",
            accessory: "ACC",
            accessories: "ACC"
        };

        return map[slot] || slot.toUpperCase() || "-";
    }

    // ===== 날짜 포맷 =====
    function formatDate(value) {
        if (!value) return "-";

        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }

        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, "0");
        const dd = String(date.getDate()).padStart(2, "0");
        const hh = String(date.getHours()).padStart(2, "0");
        const mi = String(date.getMinutes()).padStart(2, "0");

        return `${yyyy}.${mm}.${dd} ${hh}:${mi}`;
    }
	
	// ===== 텍스트 안전 처리 =====
	function safeText(value) {
	    return value && value.trim() ? value : "-";
	}

    // ===== 댓글/작성자 이니셜 =====
    function getInitial(name) {
        const safeName = String(name ?? "").trim();
        if (!safeName) return "?";
        return safeName.charAt(0).toUpperCase();
    }

    // ===== XSS 방지 =====
    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    function escapeAttr(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll('"', "&quot;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
    }

    // ===== 전역 노출 =====
    window.BoardCommon = {
        SLOT_ORDER,
        formatWeather,
        normalizeSlotKey,
        getSlotOrderIndex,
        sortItemsBySlotOrder,
        formatSlotLabel,
        formatDate,
        getInitial,
        escapeHtml,
        escapeAttr,
		formatStyle,
		formatTpo,
		safeText
    };
})();