document.addEventListener("DOMContentLoaded", () => {
    const currentPostId = window.location.pathname.split("/").pop();
    const postDetailEl = document.getElementById("postDetail");
    const coordiItemsEl = document.getElementById("coordiItems");
    const commentListEl = document.getElementById("commentList");
    const commentContentEl = document.getElementById("commentContent");
    const commentSubmitBtn = document.getElementById("commentSubmitBtn");

    commentSubmitBtn.addEventListener("click", createComment);

    async function loadDetail() {
        try {
            postDetailEl.innerHTML = `<div class="board-loading">게시글을 불러오는 중입니다...</div>`;

            const response = await fetch(`/api/board/posts/${currentPostId}`);
            if (!response.ok) {
                throw new Error("게시글 상세 조회에 실패했습니다.");
            }

            const post = await response.json();
            renderPostDetail(post);
            renderItems(post.items || []);
            renderComments(post.comments || []);
        } catch (error) {
            console.error(error);
            postDetailEl.innerHTML = `<div class="board-error">게시글 정보를 불러오지 못했습니다.</div>`;
            coordiItemsEl.innerHTML = "";
            commentListEl.innerHTML = "";
        }
    }

    function renderPostDetail(post) {
        postDetailEl.innerHTML = `
            <h1 class="board-detail-title">${escapeHtml(post.title)}</h1>
            <p class="board-detail-content">${escapeHtml(post.content ?? "")}</p>

            <div class="board-detail-meta-grid">
                <div class="board-detail-meta-item">
                    <span>작성자</span>
                    <strong>${escapeHtml(post.nickname ?? "-")}</strong>
                </div>
                <div class="board-detail-meta-item">
                    <span>날씨</span>
                    <strong>${escapeHtml(post.weatherStatus ?? "-")}</strong>
                </div>
                <div class="board-detail-meta-item">
                    <span>온도</span>
                    <strong>${escapeHtml(String(post.temp ?? "-"))}</strong>
                </div>
                <div class="board-detail-meta-item">
                    <span>체감온도</span>
                    <strong>${escapeHtml(String(post.feelsLike ?? "-"))}</strong>
                </div>
                <div class="board-detail-meta-item">
                    <span>스타일</span>
                    <strong>${escapeHtml(post.styleType ?? "-")}</strong>
                </div>
                <div class="board-detail-meta-item">
                    <span>TPO</span>
                    <strong>${escapeHtml(post.tpoType ?? "-")}</strong>
                </div>
                <div class="board-detail-meta-item">
                    <span>조회수</span>
                    <strong>${escapeHtml(String(post.viewCount ?? 0))}</strong>
                </div>
            </div>

            <div class="board-detail-ai-box">
                <strong>AI 설명</strong>
                <p>${escapeHtml(post.aiExplanation ?? "AI 설명이 없습니다.")}</p>
            </div>
        `;
    }

    function renderItems(items) {
        if (items.length === 0) {
            coordiItemsEl.innerHTML = `<div class="board-empty">등록된 코디 아이템이 없습니다.</div>`;
            return;
        }

        coordiItemsEl.innerHTML = items.map(item => `
            <article class="board-item-card">
                ${item.imageUrl
                    ? `<img class="board-item-thumb" src="${item.imageUrl}" alt="${escapeHtml(item.productName ?? "코디 아이템")}">`
                    : `<div class="board-item-thumb"></div>`
                }

                <div class="board-item-info">
                    <h4>${escapeHtml(item.productName ?? "-")}</h4>
                    <p><strong>슬롯</strong> ${escapeHtml(item.slotKey ?? "-")}</p>
                    <p><strong>브랜드</strong> ${escapeHtml(item.brand ?? "-")}</p>
                    <p><strong>가격</strong> ${escapeHtml(String(item.price ?? "-"))}</p>
                    <p><strong>색상</strong> ${escapeHtml(item.color ?? "-")}</p>
                    <p><strong>소재</strong> ${escapeHtml(item.material ?? "-")}</p>
                    <p><strong>핏</strong> ${escapeHtml(item.fit ?? "-")}</p>
                    <p><strong>스타일</strong> ${escapeHtml(item.style ?? "-")}</p>
                    <p><strong>시즌</strong> ${escapeHtml(item.season ?? "-")}</p>
                    ${item.link ? `<a class="board-item-link" href="${item.link}" target="_blank" rel="noopener noreferrer">상품 링크 보기</a>` : ""}
                </div>
            </article>
        `).join("");
    }

    function renderComments(comments) {
        if (comments.length === 0) {
            commentListEl.innerHTML = `<div class="board-empty">아직 댓글이 없습니다.</div>`;
            return;
        }

        commentListEl.innerHTML = comments.map(comment => `
            <div class="board-comment-card">
                <p class="board-comment-author">${escapeHtml(comment.nickname ?? "익명")}</p>
                <p class="board-comment-content">${escapeHtml(comment.content ?? "")}</p>
            </div>
        `).join("");
    }

    async function createComment() {
        const content = commentContentEl.value.trim();

        if (!content) {
            alert("댓글 내용을 입력해주세요.");
            commentContentEl.focus();
            return;
        }

        try {
            const response = await fetch(`/api/board/posts/${currentPostId}/comments`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ content })
            });

            if (!response.ok) {
                throw new Error("댓글 등록에 실패했습니다.");
            }

            commentContentEl.value = "";
            await loadDetail();
        } catch (error) {
            console.error(error);
            alert("댓글 등록 중 오류가 발생했습니다.");
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

    loadDetail();
});