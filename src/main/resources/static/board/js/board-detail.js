document.addEventListener("DOMContentLoaded", () => {
    const currentPostId = window.location.pathname.split("/").pop();
    const postDetailEl = document.getElementById("postDetail");
    const coordiItemsEl = document.getElementById("coordiItems");
    const commentListEl = document.getElementById("commentList");
    const commentContentEl = document.getElementById("commentContent");
    const commentSubmitBtn = document.getElementById("commentSubmitBtn");

    let currentPost = null;

    commentSubmitBtn.addEventListener("click", createComment);

    async function loadDetail() {
        try {
            postDetailEl.innerHTML = `<div class="board-loading">게시글을 불러오는 중입니다...</div>`;

            const response = await fetch(`/api/board/posts/${currentPostId}`);
            if (!response.ok) {
                throw new Error("게시글 상세 조회에 실패했습니다.");
            }

            const post = await response.json();
            currentPost = post;

            renderPostDetail(post);
            renderItems(post.items || []);
            renderComments(post.comments || []);
            bindPostActionEvents();
            bindCommentActionEvents();
        } catch (error) {
            console.error(error);
            postDetailEl.innerHTML = `<div class="board-error">게시글 정보를 불러오지 못했습니다.</div>`;
            coordiItemsEl.innerHTML = "";
            commentListEl.innerHTML = "";
        }
    }

    function renderPostDetail(post) {
        const imageUrl = getPostThumbnail(post);
        const canEditPost = Boolean(post.canEdit || post.canDelete || post.isAuthor || post.mine);

        postDetailEl.innerHTML = `
            <div class="board-detail-hero">
                <div class="board-detail-hero__image-wrap">
                    ${imageUrl
                        ? `<img class="board-detail-hero__image" src="${escapeAttr(imageUrl)}" alt="${escapeAttr(post.title ?? "게시글 이미지")}" onerror="this.outerHTML='<div class=&quot;board-detail-hero__image-empty&quot;></div>'">`
                        : `<div class="board-detail-hero__image-empty"></div>`
                    }
                </div>

                <div class="board-detail-hero__body">
                    <div class="board-detail-chip-row">
                        <span class="board-detail-chip">${escapeHtml(formatWeather(post.weatherStatus ?? post.weather ?? "-"))}</span>
                        <span class="board-detail-chip">${escapeHtml(post.styleType ?? post.style ?? "-")}</span>
                        <span class="board-detail-chip">${escapeHtml(post.tpoType ?? post.tpo ?? "-")}</span>
                    </div>

                    <h1 class="board-detail-title">${escapeHtml(post.title ?? "제목 없음")}</h1>

                    <div class="board-detail-submeta">
                        <span>작성자 ${escapeHtml(post.nickname ?? post.authorNickname ?? "-")}</span>
                        <span>조회수 ${escapeHtml(String(post.viewCount ?? 0))}</span>
                        <span>댓글 ${escapeHtml(String((post.comments || []).length || post.commentCount || 0))}</span>
                    </div>

                    <div class="board-detail-content-box">
                        <p class="board-detail-content">${escapeHtml(post.content ?? "")}</p>
                    </div>

                    <div class="board-detail-meta-grid">
                        <div class="board-detail-meta-item">
                            <span>날씨</span>
                            <strong>${escapeHtml(formatWeather(post.weatherStatus ?? post.weather ?? "-"))}</strong>
                        </div>
                        <div class="board-detail-meta-item">
                            <span>온도</span>
                            <strong>${escapeHtml(String(post.temp ?? "-"))}°</strong>
                        </div>
                        <div class="board-detail-meta-item">
                            <span>체감온도</span>
                            <strong>${escapeHtml(String(post.feelsLike ?? "-"))}°</strong>
                        </div>
                        <div class="board-detail-meta-item">
                            <span>스타일</span>
                            <strong>${escapeHtml(post.styleType ?? post.style ?? "-")}</strong>
                        </div>
                        <div class="board-detail-meta-item">
                            <span>TPO</span>
                            <strong>${escapeHtml(post.tpoType ?? post.tpo ?? "-")}</strong>
                        </div>
                        <div class="board-detail-meta-item">
                            <span>공개 여부</span>
                            <strong>${post.isPublic === false ? "비공개" : "공개"}</strong>
                        </div>
                    </div>

                    <div class="board-detail-ai-box">
                        <strong>AI 설명</strong>
                        <p>${escapeHtml(post.aiExplanation ?? "AI 설명이 없습니다.")}</p>
                    </div>

                    <div class="board-detail-actions">
                        <button type="button" class="board-ghost-btn" id="goListBtn">목록으로</button>
                        ${canEditPost ? `<button type="button" class="board-secondary-btn" id="editPostBtn">게시글 수정</button>` : ""}
                        ${canEditPost ? `<button type="button" class="board-danger-btn" id="deletePostBtn">게시글 삭제</button>` : ""}
                    </div>
                </div>
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
                    ? `<img class="board-item-thumb" src="${escapeAttr(item.imageUrl)}" alt="${escapeAttr(item.productName ?? "코디 아이템")}" onerror="this.outerHTML='<div class=&quot;board-item-thumb--empty&quot;></div>'">`
                    : `<div class="board-item-thumb--empty"></div>`
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
                    ${item.link ? `<a class="board-item-link" href="${escapeAttr(item.link)}" target="_blank" rel="noopener noreferrer">상품 링크 보기</a>` : ""}
                </div>
            </article>
        `).join("");
    }

    function renderComments(comments) {
        if (comments.length === 0) {
            commentListEl.innerHTML = `<div class="board-empty">아직 댓글이 없습니다.</div>`;
            return;
        }

        commentListEl.innerHTML = comments.map(comment => {
            const canManageComment = Boolean(
                comment.canEdit ||
                comment.canDelete ||
                comment.isAuthor ||
                comment.mine
            );

            const commentId = comment.commentId ?? comment.id;

            return `
                <div class="board-comment-card" data-comment-id="${escapeAttr(commentId)}">
                    <div class="board-comment-header">
                        <div class="board-comment-author-wrap">
                            <p class="board-comment-author">${escapeHtml(comment.nickname ?? "익명")}</p>
                            <span class="board-comment-date">${escapeHtml(formatDate(comment.createdAt ?? comment.createdDate ?? ""))}</span>
                        </div>

                        ${canManageComment ? `
                            <div class="board-comment-actions">
                                <button type="button" class="board-comment-action-btn" data-action="edit">수정</button>
                                <button type="button" class="board-comment-action-btn board-comment-action-btn--danger" data-action="delete">삭제</button>
                            </div>
                        ` : ""}
                    </div>

                    <p class="board-comment-content">${escapeHtml(comment.content ?? "")}</p>

                    <div class="board-comment-edit-box" style="display:none;">
                        <textarea maxlength="500">${escapeHtml(comment.content ?? "")}</textarea>
                        <div class="board-comment-edit-actions">
                            <button type="button" class="board-comment-action-btn" data-action="cancel-edit">취소</button>
                            <button type="button" class="board-primary-btn" data-action="save-edit">저장</button>
                        </div>
                    </div>
                </div>
            `;
        }).join("");
    }

    function bindPostActionEvents() {
        const goListBtn = document.getElementById("goListBtn");
        const editPostBtn = document.getElementById("editPostBtn");
        const deletePostBtn = document.getElementById("deletePostBtn");

        if (goListBtn) {
            goListBtn.addEventListener("click", () => {
                location.href = "/board";
            });
        }

        if (editPostBtn) {
            editPostBtn.addEventListener("click", () => {
                location.href = `/board/${currentPostId}/edit`;
            });
        }

        if (deletePostBtn) {
            deletePostBtn.addEventListener("click", async () => {
                const confirmed = confirm("게시글을 삭제할까요?");
                if (!confirmed) return;

                try {
                    const response = await fetch(`/api/board/posts/${currentPostId}`, {
                        method: "DELETE"
                    });

                    if (!response.ok) {
                        throw new Error("게시글 삭제 실패");
                    }

                    alert("게시글이 삭제되었습니다.");
                    location.href = "/board";
                } catch (error) {
                    console.error(error);
                    alert("게시글 삭제 중 오류가 발생했습니다.");
                }
            });
        }
    }

    function bindCommentActionEvents() {
        commentListEl.querySelectorAll(".board-comment-card").forEach(card => {
            const commentId = card.dataset.commentId;
            const editBtn = card.querySelector('[data-action="edit"]');
            const deleteBtn = card.querySelector('[data-action="delete"]');
            const cancelEditBtn = card.querySelector('[data-action="cancel-edit"]');
            const saveEditBtn = card.querySelector('[data-action="save-edit"]');
            const contentEl = card.querySelector(".board-comment-content");
            const editBoxEl = card.querySelector(".board-comment-edit-box");
            const textareaEl = editBoxEl?.querySelector("textarea");

            if (editBtn) {
                editBtn.addEventListener("click", () => {
                    editBoxEl.style.display = "block";
                    textareaEl.focus();
                });
            }

            if (cancelEditBtn) {
                cancelEditBtn.addEventListener("click", () => {
                    editBoxEl.style.display = "none";
                    textareaEl.value = contentEl.textContent;
                });
            }

            if (saveEditBtn) {
                saveEditBtn.addEventListener("click", async () => {
                    const content = textareaEl.value.trim();

                    if (!content) {
                        alert("댓글 내용을 입력해주세요.");
                        textareaEl.focus();
                        return;
                    }

                    try {
                        const response = await fetch(`/api/board/posts/comments/${commentId}`, {
                            method: "PUT",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ content })
                        });

                        if (!response.ok) {
                            throw new Error("댓글 수정 실패");
                        }

                        await loadDetail();
                    } catch (error) {
                        console.error(error);
                        alert("댓글 수정 중 오류가 발생했습니다.");
                    }
                });
            }

            if (deleteBtn) {
                deleteBtn.addEventListener("click", async () => {
                    const confirmed = confirm("댓글을 삭제할까요?");
                    if (!confirmed) return;

                    try {
                        const response = await fetch(`/api/board/posts/comments/${commentId}`, {
                            method: "DELETE"
                        });

                        if (!response.ok) {
                            throw new Error("댓글 삭제 실패");
                        }

                        await loadDetail();
                    } catch (error) {
                        console.error(error);
                        alert("댓글 삭제 중 오류가 발생했습니다.");
                    }
                });
            }
        });
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

    function getPostThumbnail(post) {
        return (
            post.thumbnailImageUrl ||
            post.thumbnailUrl ||
            post.imageUrl ||
            post.topItemImageUrl ||
            post.coordiImageUrl ||
            post.recommendationImageUrl ||
            post?.items?.[0]?.imageUrl ||
            ""
        );
    }

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

    loadDetail();
});