document.addEventListener("DOMContentLoaded", () => {
    const recIdEl = document.getElementById("recId");
    const titleEl = document.getElementById("title");
    const contentEl = document.getElementById("content");
    const isPublicEl = document.getElementById("isPublic");

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
            // TODO:
            // 실제 저장 코디 조회 API가 있으면 여기로 교체
            // 예: const response = await fetch("/api/closet/saved-coordis");
			/*			
			const response = await fetch("/api/closet/saved-coordis");
			if (!response.ok) {
			    throw new Error("저장 코디 조회 실패");
			}
			const coordis = await response.json();
			*/

            const coordis = [
                {
                    recId: 201,
                    weather: "clear",
                    style: "minimal",
                    tpo: "date",
                    topItemName: "화이트 셔츠",
                    bottomItemName: "블랙 슬랙스",
                    outerItemName: "라이트 가디건",
                    shoesItemName: "로퍼",
                    aiExplanation: "맑은 날 데이트에 어울리는 깔끔하고 단정한 코디입니다."
                },
                {
                    recId: 202,
                    weather: "rain",
                    style: "comfortable",
                    tpo: "casual",
                    topItemName: "방수 자켓",
                    bottomItemName: "와이드 데님",
                    outerItemName: "후드 집업",
                    shoesItemName: "러닝화",
                    aiExplanation: "비 오는 날 활동하기 편한 실용적인 캐주얼 코디입니다."
                },
                {
                    recId: 203,
                    weather: "cloudy",
                    style: "classic",
                    tpo: "work",
                    topItemName: "베이지 셔츠",
                    bottomItemName: "네이비 슬랙스",
                    outerItemName: "블레이저",
                    shoesItemName: "더비슈즈",
                    aiExplanation: "출근용으로 무난하고 신뢰감 있게 보이는 클래식 코디입니다."
                },
                {
                    recId: 204,
                    weather: "partly_cloudy",
                    style: "street",
                    tpo: "travel",
                    topItemName: "오버핏 티셔츠",
                    bottomItemName: "카고 팬츠",
                    outerItemName: "바람막이",
                    shoesItemName: "스니커즈",
                    aiExplanation: "여행 중 사진도 잘 나오고 오래 걸어도 편한 스트릿 코디입니다."
                }
            ];

            renderCoordiList(coordis);
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

        coordiListEl.innerHTML = coordis.map(coordi => `
            <article class="board-coordi-card" data-recid="${coordi.recId}">
                <div class="board-coordi-card__badge-row">
                    <span class="board-coordi-card__badge">${escapeHtml(coordi.weather ?? "-")}</span>
                    <span class="board-coordi-card__badge">${escapeHtml(coordi.style ?? "-")}</span>
                    <span class="board-coordi-card__badge">${escapeHtml(coordi.tpo ?? "-")}</span>
                </div>

                <div class="board-coordi-card__grid">
                    <div class="board-coordi-card__item">
                        <span>상의</span>
                        <strong>${escapeHtml(coordi.topItemName ?? "-")}</strong>
                    </div>
                    <div class="board-coordi-card__item">
                        <span>하의</span>
                        <strong>${escapeHtml(coordi.bottomItemName ?? "-")}</strong>
                    </div>
                    <div class="board-coordi-card__item">
                        <span>아우터</span>
                        <strong>${escapeHtml(coordi.outerItemName ?? "-")}</strong>
                    </div>
                    <div class="board-coordi-card__item">
                        <span>신발</span>
                        <strong>${escapeHtml(coordi.shoesItemName ?? "-")}</strong>
                    </div>
                    <div class="board-coordi-card__item" style="grid-column: 1 / -1;">
                        <span>AI 설명</span>
                        <strong>${escapeHtml(coordi.aiExplanation ?? "-")}</strong>
                    </div>
                </div>
            </article>
        `).join("");

        document.querySelectorAll(".board-coordi-card").forEach((card, index) => {
            card.addEventListener("click", () => {
                selectedCoordi = coordis[index];
                recIdEl.value = selectedCoordi.recId;
                renderSelectedCoordi(selectedCoordi);
                closeCoordiModal();
            });
        });
    }

    function renderSelectedCoordi(coordi) {
        selectedCoordiPreviewEl.className = "board-selected-coordi__preview";
        selectedCoordiPreviewEl.innerHTML = `
            <div class="board-selected-coordi__preview-item">
                <span>날씨</span>
                <strong>${escapeHtml(coordi.weather ?? "-")}</strong>
            </div>
            <div class="board-selected-coordi__preview-item">
                <span>스타일 / TPO</span>
                <strong>${escapeHtml(coordi.style ?? "-")} / ${escapeHtml(coordi.tpo ?? "-")}</strong>
            </div>
            <div class="board-selected-coordi__preview-item">
                <span>상의</span>
                <strong>${escapeHtml(coordi.topItemName ?? "-")}</strong>
            </div>
            <div class="board-selected-coordi__preview-item">
                <span>하의</span>
                <strong>${escapeHtml(coordi.bottomItemName ?? "-")}</strong>
            </div>
            <div class="board-selected-coordi__preview-item">
                <span>아우터</span>
                <strong>${escapeHtml(coordi.outerItemName ?? "-")}</strong>
            </div>
            <div class="board-selected-coordi__preview-item">
                <span>신발</span>
                <strong>${escapeHtml(coordi.shoesItemName ?? "-")}</strong>
            </div>
            <div class="board-selected-coordi__preview-item" style="grid-column: 1 / -1;">
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
                content,
                isPublic: isPublicEl.value === "true"
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

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }
});