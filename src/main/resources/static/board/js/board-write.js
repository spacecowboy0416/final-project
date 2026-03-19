document.addEventListener("DOMContentLoaded", () => {
    const recIdEl = document.getElementById("recId");
    const titleEl = document.getElementById("title");
    const contentEl = document.getElementById("content");

    const createBtn = document.getElementById("createBtn");
    const cancelBtn = document.getElementById("cancelBtn");

    const loadCoordiBtn = document.getElementById("loadCoordiBtn");
    const coordiModal = document.getElementById("coordiModal");
    const closeCoordiModalBtn = document.getElementById("closeCoordiModalBtn");
    const coordiListEl = document.getElementById("coordiList");
    const selectedCoordiPreviewEl = document.getElementById("selectedCoordiPreview");

    let selectedCoordi = null;

    createBtn.addEventListener("click", createPost);
    cancelBtn.addEventListener("click", () => {
        location.href = "/board";
    });

    loadCoordiBtn.addEventListener("click", openCoordiModal);
    closeCoordiModalBtn.addEventListener("click", closeCoordiModal);

    if (coordiModal) {
        const backdrop = coordiModal.querySelector(".board-modal__backdrop");
        if (backdrop) {
            backdrop.addEventListener("click", closeCoordiModal);
        }
    }

    async function openCoordiModal() {
        coordiModal.classList.remove("hidden");
        coordiListEl.innerHTML = `<div class="board-loading">저장한 코디를 불러오는 중입니다...</div>`;

        try {
            const response = await fetch("/api/board/posts/coordis");
            if (!response.ok) {
                throw new Error("저장 코디 조회 실패");
            }

            const coordis = await response.json();
            renderCoordiList(Array.isArray(coordis) ? coordis : []);
        } catch (error) {
            console.error(error);
            coordiListEl.innerHTML = `<div class="board-error">저장한 코디를 불러오지 못했습니다.</div>`;
        }
    }

    function closeCoordiModal() {
        coordiModal.classList.add("hidden");
    }

    function renderCoordiList(coordis) {
        if (!coordis || coordis.length === 0) {
            coordiListEl.innerHTML = `<div class="board-empty">저장된 코디가 없습니다.</div>`;
            return;
        }

        coordiListEl.innerHTML = coordis.map((coordi, index) => `
            <article class="board-coordi-card" data-index="${index}" data-recid="${escapeAttr(coordi.recId)}">
                <div class="board-coordi-card__badge-row">
                    <span class="board-coordi-card__badge">${escapeHtml(formatWeather(coordi.weather ?? coordi.weatherStatus ?? "-"))}</span>
                    <span class="board-coordi-card__badge">${escapeHtml(coordi.style ?? coordi.styleType ?? "-")}</span>
                    <span class="board-coordi-card__badge">${escapeHtml(coordi.tpo ?? coordi.tpoType ?? "-")}</span>
                </div>

                ${renderCoordiCardPreview(coordi)}
            </article>
        `).join("");

        coordiListEl.querySelectorAll(".board-coordi-card").forEach(card => {
            card.addEventListener("click", () => {
                const index = Number(card.dataset.index);
                selectedCoordi = coordis[index];
                recIdEl.value = selectedCoordi.recId;
                renderSelectedCoordi(selectedCoordi);
                closeCoordiModal();
            });
        });
    }

    function renderCoordiCardPreview(coordi) {
        const items = getCoordiItems(coordi);

        return `
            ${renderCoordiImageGrid(items, "board-coordi-card")}
            <div class="board-coordi-card__grid">
                ${items.map(item => `
                    <div class="board-coordi-card__item">
                        <span>${escapeHtml(item.label)}</span>
                        <strong>${escapeHtml(item.name ?? "-")}</strong>
                    </div>
                `).join("")}

                <div class="board-coordi-card__item board-coordi-card__item--full">
                    <span>AI 설명</span>
                    <strong>${escapeHtml(coordi.aiExplanation ?? "-")}</strong>
                </div>
            </div>
        `;
    }

    function renderSelectedCoordi(coordi) {
        const items = getCoordiItems(coordi);

        selectedCoordiPreviewEl.className = "board-selected-coordi__preview";
        selectedCoordiPreviewEl.innerHTML = `
            ${renderCoordiImageGrid(items, "board-selected-coordi")}

            <div class="board-selected-coordi__preview-item">
                <span>날씨</span>
                <strong>${escapeHtml(formatWeather(coordi.weather ?? coordi.weatherStatus ?? "-"))}</strong>
            </div>

            <div class="board-selected-coordi__preview-item">
                <span>스타일 / TPO</span>
                <strong>${escapeHtml(coordi.style ?? coordi.styleType ?? "-")} / ${escapeHtml(coordi.tpo ?? coordi.tpoType ?? "-")}</strong>
            </div>

            ${items.map(item => `
                <div class="board-selected-coordi__preview-item">
                    <span>${escapeHtml(item.label)}</span>
                    <strong>${escapeHtml(item.name ?? "-")}</strong>
                </div>
            `).join("")}

            <div class="board-selected-coordi__preview-item board-selected-coordi__preview-item--full">
                <span>AI 설명</span>
                <strong>${escapeHtml(coordi.aiExplanation ?? "-")}</strong>
            </div>
        `;
    }

    async function createPost() {
        const recId = Number(recIdEl.value);
        const title = titleEl.value.trim();
        const content = contentEl.value.trim();

        if (!recId || recId <= 0) {
            alert("저장한 코디를 먼저 불러와 선택해주세요.");
            return;
        }

        if (!title) {
            alert("제목을 입력해주세요.");
            titleEl.focus();
            return;
        }

        try {
            const body = {
                recId,
                title,
                content
            };

            const response = await fetch("/api/board/posts", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            if (!response.ok) {
                throw new Error("게시글 등록에 실패했습니다.");
            }

            const result = await response.json();
            location.href = `/board/${result.postId}`;
        } catch (error) {
            console.error(error);
            alert("게시글 등록 중 오류가 발생했습니다.");
        }
    }

	function getCoordiItems(coordi) {
	    if (!Array.isArray(coordi.items)) {
	        return [];
	    }

	    return coordi.items
	        .filter(item =>
	            item &&
	            (
	                (item.itemName && String(item.itemName).trim() !== "" && item.itemName !== "-")
	                || (item.imageUrl && String(item.imageUrl).trim() !== "")
	            )
	        )
	        .map(item => ({
	            key: item.slotKey ?? "",
	            label: item.label ?? "기타",
	            name: item.itemName ?? "-",
	            imageUrl: item.imageUrl ?? ""
	        }));
	}

    function renderCoordiImageGrid(items, prefixClass) {
        if (!items || items.length === 0) {
            return "";
        }

        const visibleItems = items.slice(0, 6);
        const extraCount = Math.max(items.length - 6, 0);
        const gridClass = `media-${Math.min(visibleItems.length, 6)}`;

        return `
            <div class="${prefixClass}__image-grid ${gridClass}">
                ${visibleItems.map((item, index) => {
                    const isLastVisible = index === visibleItems.length - 1;
                    const moreText = extraCount > 0 && isLastVisible ? `+${extraCount}` : "";

                    return renderDynamicThumb(
                        item.imageUrl,
                        item.label,
                        prefixClass,
                        moreText
                    );
                }).join("")}
            </div>
        `;
    }

    function renderDynamicThumb(imageUrl, altText, prefixClass, moreText = "") {
        const emptyClass = `${prefixClass}__thumb-empty`;

        if (!imageUrl) {
            return `
                <div class="media-img">
                    <div class="${emptyClass}"></div>
                    ${moreText ? `<div class="media-img__more">${escapeHtml(moreText)}</div>` : ""}
                </div>
            `;
        }

        return `
            <div class="media-img">
                <img
                    src="${escapeAttr(imageUrl)}"
                    alt="${escapeAttr(altText)}"
                    onerror="this.outerHTML='<div class=&quot;${emptyClass}&quot;></div>'"
                >
                ${moreText ? `<div class="media-img__more">${escapeHtml(moreText)}</div>` : ""}
            </div>
        `;
    }

    function getCoordiImage(coordi, slot) {
        if (slot === "top") {
            return coordi.topItemImageUrl || coordi.topImageUrl || coordi.topImage || "";
        }
        if (slot === "bottom") {
            return coordi.bottomItemImageUrl || coordi.bottomImageUrl || coordi.bottomImage || "";
        }
        if (slot === "outer") {
            return coordi.outerItemImageUrl || coordi.outerImageUrl || coordi.outerImage || "";
        }
        if (slot === "shoes") {
            return coordi.shoesItemImageUrl || coordi.shoesImageUrl || coordi.shoesImage || "";
        }
        return "";
    }

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

        return map[String(value).toLowerCase()] || value;
    }

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
});