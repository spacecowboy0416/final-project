// board-edit.js
document.addEventListener("DOMContentLoaded", () => {
    const {
        formatWeather,
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

    // ===== 기본 요소 =====
    const postId = window.location.pathname.split("/")[2];

    const titleEl = document.getElementById("title");
    const contentEl = document.getElementById("content");
    const updateBtn = document.getElementById("updateBtn");
    const cancelBtn = document.getElementById("cancelBtn");
    const selectedCoordiPreviewEl = document.getElementById("selectedCoordiPreview");

    // ===== 필수 요소 방어 =====
    if (!postId || !titleEl || !contentEl || !updateBtn || !cancelBtn || !selectedCoordiPreviewEl) {
        console.error("board-edit.js: 필요한 DOM 요소를 찾지 못했습니다.");
        return;
    }

    // ===== 이벤트 =====
    updateBtn.addEventListener("click", updatePost);

    cancelBtn.addEventListener("click", () => {
        location.href = `/board/${postId}`;
    });

    // ===== 게시글 정보 불러오기 =====
    async function loadPost() {
        try {
            const response = await fetch(`/api/board/posts/${postId}`);

            if (!response.ok) {
                throw new Error("게시글 조회에 실패했습니다.");
            }

            const post = await response.json();

            titleEl.value = post.title ?? "";
            contentEl.value = post.content ?? "";

            renderSelectedCoordi(post);
        } catch (error) {
            console.error(error);
            showGlobalModal("오류", "게시글 정보를 불러오지 못했습니다.");

            selectedCoordiPreviewEl.className = "board-selected-coordi__empty";
            selectedCoordiPreviewEl.textContent = "연결된 코디 정보를 불러오지 못했습니다.";
        }
    }

    // ===== 게시글 수정 =====
    async function updatePost() {
        const title = titleEl.value.trim();
        const content = contentEl.value.trim();

        if (!title) {
			showGlobalModal("알림", "제목을 입력해주세요.", "alert", function() {
			    titleEl.focus();
			});
            return;
        }

        try {
            const response = await fetch(`/api/board/posts/${postId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    title,
                    content,
                    isPublic: true
                })
            });

            if (!response.ok) {
                throw new Error("게시글 수정에 실패했습니다.");
            }

            location.href = `/board/${postId}`;
        } catch (error) {
            console.error(error);
            showGlobalModal("오류", "게시글 수정 중 오류가 발생했습니다.");
        }
    }

    // ===== 연결 코디 미리보기 =====
    function renderSelectedCoordi(post) {
        const items = getCoordiItems(post);

        if (!items.length) {
            selectedCoordiPreviewEl.className = "board-selected-coordi__empty";
            selectedCoordiPreviewEl.textContent = "연결된 코디 정보가 없습니다.";
            return;
        }

        selectedCoordiPreviewEl.className = "board-selected-coordi__preview";
        selectedCoordiPreviewEl.innerHTML = `
            <div class="board-selected-coordi__summary">
                <span class="board-selected-coordi__chip">${escapeHtml(formatWeather(post.weatherStatus ?? post.weather ?? "-"))}</span>
                <span class="board-selected-coordi__chip">${escapeHtml(post.styleType ?? post.style ?? "-")}</span>
                <span class="board-selected-coordi__chip">${escapeHtml(post.tpoType ?? post.tpo ?? "-")}</span>
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

    // ===== 코디 아이템 정리 =====
    function getCoordiItems(post) {
        if (!Array.isArray(post.items)) {
            return [];
        }

        return post.items
            .filter(item =>
                item &&
                (
                    (item.productName && String(item.productName).trim() !== "" && item.productName !== "-") ||
                    (item.imageUrl && String(item.imageUrl).trim() !== "")
                )
            )
            .map(item => ({
                key: normalizeSlotKey(item.slotKey ?? item.label ?? ""),
                label: item.label ?? "기타",
                name: item.productName ?? "-",
                imageUrl: item.imageUrl ?? ""
            }))
            .sort((a, b) => getSlotOrderIndex(a.key) - getSlotOrderIndex(b.key));
    }

    // ===== 초기 로딩 =====
    loadPost();
});