// board-write.js
document.addEventListener("DOMContentLoaded", () => {
    const {
        formatWeather,
		formatStyle,
		formatTpo,
        normalizeSlotKey,
        getSlotOrderIndex,
        formatSlotLabel,
        escapeHtml,
        escapeAttr
    } = window.BoardCommon || {};

    if (!window.BoardCommon) {
        console.error("board-common.js가 먼저 로드되어야 합니다.");
        return;
    }

    // ===== 입력 / 버튼 / 모달 요소 =====
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

    // ===== 필수 요소 방어 =====
    if (
        !recIdEl || !titleEl || !contentEl ||
        !createBtn || !cancelBtn ||
        !loadCoordiBtn || !coordiModal || !closeCoordiModalBtn ||
        !coordiListEl || !selectedCoordiPreviewEl
    ) {
        console.error("board-write.js: 필요한 DOM 요소를 찾지 못했습니다.");
        return;
    }

    // ===== 이벤트 바인딩 =====
    createBtn.addEventListener("click", createPost);

    cancelBtn.addEventListener("click", () => {
        location.href = "/board";
    });

    loadCoordiBtn.addEventListener("click", openCoordiModal);
    closeCoordiModalBtn.addEventListener("click", closeCoordiModal);

    const backdrop = coordiModal.querySelector(".board-modal__backdrop");
    if (backdrop) {
        backdrop.addEventListener("click", closeCoordiModal);
    }
	
	initializePreselectedCoordi();
	
	// 옷장 자랑하기 버튼 누를시
	async function initializePreselectedCoordi() {
	    const params = new URLSearchParams(window.location.search);

	    // recId도 받고, closet에서 보내는 coordiId도 같이 받기
	    const preselectedId = params.get("recId") || params.get("coordiId");

	    if (!preselectedId) {
	        return;
	    }

	    try {
	        const response = await fetch("/api/board/posts/coordis");

	        if (!response.ok) {
	            throw new Error("저장 코디 조회 실패");
	        }

	        const coordis = await response.json();
	        const coordiList = Array.isArray(coordis) ? coordis : [];

	        const matchedCoordi = coordiList.find(
	            coordi => String(coordi.recId) === String(preselectedId)
	        );

	        if (!matchedCoordi) {
	            console.warn("전달받은 코디 ID에 해당하는 코디를 찾지 못했습니다.", preselectedId);
	            return;
	        }

	        selectedCoordi = matchedCoordi;
	        recIdEl.value = matchedCoordi.recId;
	        renderSelectedCoordi(matchedCoordi);
	    } catch (error) {
	        console.error("초기 코디 자동 불러오기 실패", error);
	    }
	}

    // ===== 저장된 코디 모달 열기 =====
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

    // ===== 모달 닫기 =====
    function closeCoordiModal() {
        coordiModal.classList.add("hidden");
    }

    // ===== 코디 목록 렌더링 =====
    function renderCoordiList(coordis) {
        if (!coordis || coordis.length === 0) {
            coordiListEl.innerHTML = `<div class="board-empty">저장된 코디가 없습니다.</div>`;
            return;
        }

        coordiListEl.innerHTML = coordis.map((coordi, index) => `
            <article class="board-coordi-card" data-index="${index}" data-recid="${escapeAttr(coordi.recId)}">
                <div class="board-coordi-card__badge-row">
                    <span class="board-coordi-card__badge">${escapeHtml(formatWeather(coordi.weather ?? coordi.weatherStatus ?? "-"))}</span>
					<span class="board-coordi-card__badge">${escapeHtml(formatStyle(coordi.style ?? coordi.styleType))}</span>
					<span class="board-coordi-card__badge">${escapeHtml(formatTpo(coordi.tpo ?? coordi.tpoType))}</span>
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

    // ===== 코디 카드 내부 미리보기 =====
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

    // ===== 선택한 코디 미리보기 =====
    function renderSelectedCoordi(coordi) {
        const items = getCoordiItems(coordi);

        selectedCoordiPreviewEl.className = "board-selected-coordi__preview";
        selectedCoordiPreviewEl.innerHTML = `
            <div class="board-selected-coordi__summary">
                <span class="board-selected-coordi__chip">${escapeHtml(formatWeather(coordi.weather ?? coordi.weatherStatus ?? "-"))}</span>
				<span class="board-selected-coordi__chip">${escapeHtml(formatStyle(coordi.style ?? coordi.styleType))}</span>
				<span class="board-selected-coordi__chip">${escapeHtml(formatTpo(coordi.tpo ?? coordi.tpoType))}</span>
            </div>

            <div class="board-selected-coordi__gallery">
                ${items.map(item => `
                    <div class="board-selected-coordi__photo-card">
                        <span class="board-selected-coordi__slot">
                            ${escapeHtml(formatSlotLabel(item.key))}
                        </span>

                        ${item.imageUrl
                            ? `<img class="board-selected-coordi__photo" src="${escapeAttr(item.imageUrl)}" alt="${escapeAttr(item.name ?? "코디 아이템")}" onerror="this.outerHTML='<div class=&quot;board-selected-coordi__photo-empty&quot;></div>'">`
                            : `<div class="board-selected-coordi__photo-empty"></div>`
                        }
                    </div>
                `).join("")}
            </div>
        `;
    }

    // ===== 게시글 등록 =====
    async function createPost() {
        const recId = Number(recIdEl.value);
        const title = titleEl.value.trim();
        const content = contentEl.value.trim();

        if (!recId || recId <= 0) {
            showGlobalModal("알림", "저장한 코디를 먼저 불러와 선택해주세요.");
            return;
        }

        if (!title) {
			showGlobalModal("알림", "제목을 입력해주세요.", "alert", function() {
			    titleEl.focus();
			});
            return;
        }

        try {
            const body = {
                recId,
                title,
                content,
                isPublic: true
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
            showGlobalModal("오류", "게시글 등록 중 오류가 발생했습니다.");
        }
    }

    // ===== 코디 아이템 정리 =====
    function getCoordiItems(coordi) {
        if (!Array.isArray(coordi.items)) {
            return [];
        }

        return coordi.items
            .filter(item =>
                item &&
                (
                    (item.itemName && String(item.itemName).trim() !== "" && item.itemName !== "-") ||
                    (item.imageUrl && String(item.imageUrl).trim() !== "")
                )
            )
            .map(item => ({
                key: normalizeSlotKey(item.slotKey ?? item.label ?? ""),
                label: item.label ?? "기타",
                name: item.itemName ?? "-",
                imageUrl: item.imageUrl ?? ""
            }))
            .sort((a, b) => getSlotOrderIndex(a.key) - getSlotOrderIndex(b.key));
    }

    // ===== 코디 이미지 그리드 =====
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

    // ===== 동적 썸네일 =====
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
});