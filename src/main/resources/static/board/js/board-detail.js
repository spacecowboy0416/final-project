// board-detail.js
document.addEventListener("DOMContentLoaded", () => {
    const {
        sortItemsBySlotOrder,
        formatWeather,
		formatStyle,
		formatTpo,
        formatDate,
        formatDegree,
        formatSlotLabel,
        getInitial,
        escapeHtml,
        escapeAttr
    } = window.BoardCommon || {};

    if (!window.BoardCommon) {
        console.error("board-common.js가 먼저 로드되어야 합니다.");
        return;
    }

    // ===== 기본 요소 =====
    const currentPostId = window.location.pathname.split("/").pop();

    const postTopButtonsEl = document.getElementById("postTopButtons");
    const postHeaderCardEl = document.getElementById("postHeaderCard");
    const postUnifiedCardEl = document.getElementById("postUnifiedCard");
    const commentListEl = document.getElementById("commentList");
    const commentContentEl = document.getElementById("commentContent");
    const commentSubmitBtn = document.getElementById("commentSubmitBtn");
    const commentLengthTextEl = document.getElementById("commentLengthText");
    const commentCountTextEl = document.getElementById("commentCountText");

    // ===== 필수 요소 방어 =====
    if (
        !currentPostId ||
        !postTopButtonsEl ||
        !postHeaderCardEl ||
        !postUnifiedCardEl ||
        !commentListEl ||
        !commentContentEl ||
        !commentSubmitBtn ||
        !commentLengthTextEl ||
        !commentCountTextEl
    ) {
        console.error("board-detail.js: 필요한 DOM 요소를 찾지 못했습니다.");
        return;
    }

    // ===== 초기 이벤트 =====
    updateCommentLengthText();
    commentContentEl.addEventListener("input", updateCommentLengthText);
    commentSubmitBtn.addEventListener("click", createComment);

    // ===== 게시글 상세 조회 =====
    async function loadDetail() {
        try {
            postHeaderCardEl.innerHTML = `<div class="board-loading">게시글을 불러오는 중입니다...</div>`;
            postUnifiedCardEl.innerHTML = "";
            postTopButtonsEl.innerHTML = "";

            const response = await fetch(`/api/board/posts/${currentPostId}`);
            if (!response.ok) {
                throw new Error("게시글 상세 조회에 실패했습니다.");
            }

            const post = await response.json();

            renderTopButtons(post);
            renderHeaderCard(post);
            renderUnifiedCard(post);
            renderComments(post.comments || []);
            updateCommentCount(post.comments || []);
            bindPostActionEvents();
            bindCommentActionEvents();
        } catch (error) {
            console.error(error);
            postHeaderCardEl.innerHTML = `<div class="board-error">게시글 정보를 불러오지 못했습니다.</div>`;
            postUnifiedCardEl.innerHTML = "";
            postTopButtonsEl.innerHTML = "";
            commentListEl.innerHTML = "";
            updateCommentCount([]);
        }
    }

    // ===== 상단 버튼 =====
    function renderTopButtons(post) {
        const canEditPost = Boolean(post.canEdit || post.canDelete || post.isAuthor || post.mine);

        if (!canEditPost) {
            postTopButtonsEl.innerHTML = "";
            return;
        }

        postTopButtonsEl.innerHTML = `
            <button type="button" class="board-ghost-btn" id="editPostBtn">수정</button>
            <button type="button" class="board-danger-btn" id="deletePostBtn">삭제</button>
        `;
    }

    // ===== 헤더 카드 =====
    function renderHeaderCard(post) {
        const authorName = post.nickname ?? post.authorNickname ?? "익명";

        postHeaderCardEl.innerHTML = `
            <div class="board-detail-header-card">
                <div class="board-detail-header-top">
                    <div class="board-detail-header-left">
                        <div class="board-detail-meta-top">
                            <span>조회 ${escapeHtml(String(post.viewCount ?? 0))}</span>
                            <span>댓글 ${escapeHtml(String((post.comments || []).length || post.commentCount || 0))}</span>
                        </div>

                        <div class="board-detail-title-row">
                            <h1 class="board-detail-title">${escapeHtml(post.title ?? "제목 없음")}</h1>
                            <span class="board-detail-author-inline">by ${escapeHtml(authorName)}</span>
                        </div>
                    </div>

                    <div class="board-detail-meta-date">
                        ${escapeHtml(formatDate(post.createdAt ?? post.createdDate ?? ""))}
                    </div>
                </div>

                <div class="board-detail-header-content">
                    <p class="board-detail-content">${escapeHtml(post.content ?? "")}</p>
                </div>
            </div>
        `;
    }

    // ===== 통합 카드 =====
    function renderUnifiedCard(post) {
        const items = sortItemsBySlotOrder(post.items || []);

        postUnifiedCardEl.innerHTML = `
            <div class="board-detail-unified-card">
                <div class="board-detail-ai-box">
                    <p class="board-detail-ai-title">AI 코디 설명</p>
                    <p class="board-detail-ai-text">${escapeHtml(post.aiExplanation ?? "AI 설명이 없습니다.")}</p>
                </div>

                <div class="board-detail-section-divider"></div>

                <div class="board-detail-section-bar">
                    <div class="board-detail-section-bar__left">
                        <h3>코디 구성</h3>
                    </div>

					<div class="board-detail-section-bar__right">
					    <span class="board-detail-chip">${escapeHtml(formatWeather(post.weatherStatus ?? post.weather ?? "-"))}</span>
					    <span class="board-detail-chip">${escapeHtml(formatStyle(post.styleType ?? post.style))}</span>
					    <span class="board-detail-chip">${escapeHtml(formatTpo(post.tpoType ?? post.tpo))}</span>
					</div>
                </div>

                <div class="board-item-list">
                    ${items.length > 0 ? items.map(item => `
                        <article class="board-item-card">
                            <div class="board-item-visual">
                                <span class="board-item-slot">${escapeHtml(formatSlotLabel(item.slotKey ?? "-"))}</span>

                                ${item.imageUrl
                                    ? `<img class="board-item-thumb" src="${escapeAttr(item.imageUrl)}" alt="${escapeAttr(item.productName ?? "코디 아이템")}" onerror="this.outerHTML='<div class=&quot;board-item-thumb--empty&quot;></div>'">`
                                    : `<div class="board-item-thumb--empty"></div>`
                                }

                                <p class="board-item-name-under">${escapeHtml(item.productName ?? "-")}</p>
                            </div>

                            <div class="board-item-info">
                                <div class="board-item-meta">
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">브랜드</span>
                                        <span class="board-item-meta-value">${escapeHtml(item.brand ?? "-")}</span>
                                    </div>
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">색상</span>
                                        <span class="board-item-meta-value">${escapeHtml(item.color ?? "-")}</span>
                                    </div>
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">소재</span>
                                        <span class="board-item-meta-value">${escapeHtml(item.material ?? "-")}</span>
                                    </div>
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">핏</span>
                                        <span class="board-item-meta-value">${escapeHtml(item.fit ?? "-")}</span>
                                    </div>
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">스타일</span>
                                        <span class="board-item-meta-value">${escapeHtml(item.style ?? "-")}</span>
                                    </div>
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">시즌</span>
                                        <span class="board-item-meta-value">${escapeHtml(item.season ?? "-")}</span>
                                    </div>
                                    <div class="board-item-meta-row">
                                        <span class="board-item-meta-label">온도</span>
                                        <span class="board-item-meta-value">${formatDegree(item.temperature)}</span>
                                    </div>
                                </div>

                                ${item.link
                                    ? `<a class="board-item-link" href="${escapeAttr(item.link)}" target="_blank" rel="noopener noreferrer">상품 링크 보기</a>`
                                    : ""
                                }
                            </div>
                        </article>
                    `).join("") : `<div class="board-empty">등록된 코디 아이템이 없습니다.</div>`}
                </div>
            </div>
        `;
    }

    // ===== 댓글 렌더링 =====
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
            const nickname = comment.nickname ?? "익명";

            return `
                <div class="board-comment-card" data-comment-id="${escapeAttr(commentId)}">
                    <div class="board-comment-header">
                        <div class="board-comment-author-wrap">
                            <div class="board-comment-author-avatar">${escapeHtml(getInitial(nickname))}</div>
                            <div class="board-comment-author-meta">
                                <p class="board-comment-author">${escapeHtml(nickname)}</p>
                                <span class="board-comment-date">${escapeHtml(formatDate(comment.createdAt ?? comment.createdDate ?? ""))}</span>
                            </div>
                        </div>

                        ${canManageComment ? `
                            <div class="board-comment-actions">
                                <button type="button" class="board-ghost-btn board-comment-action-btn" data-action="edit">수정</button>
                                <button type="button" class="board-danger-btn board-comment-action-btn" data-action="delete">삭제</button>
                            </div>
                        ` : ""}
                    </div>

                    <p class="board-comment-content">${escapeHtml(comment.content ?? "")}</p>

                    <div class="board-comment-edit-box" style="display:none;">
                        <textarea maxlength="500">${escapeHtml(comment.content ?? "")}</textarea>
                        <div class="board-comment-edit-actions">
                            <button type="button" class="board-ghost-btn board-comment-action-btn" data-action="cancel-edit">취소</button>
                            <button type="button" class="board-dark-btn board-comment-action-btn" data-action="save-edit">저장</button>
                        </div>
                    </div>
                </div>
            `;
        }).join("");
    }

    // ===== 게시글 버튼 이벤트 =====
    function bindPostActionEvents() {
        const editPostBtn = document.getElementById("editPostBtn");
        const deletePostBtn = document.getElementById("deletePostBtn");

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

                    if (redirectToLoginIfNeeded(response)) return;

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

    // ===== 댓글 버튼 이벤트 =====
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

                        if (redirectToLoginIfNeeded(response)) return;

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

                        if (redirectToLoginIfNeeded(response)) return;

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

    // ===== 댓글 등록 =====
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

            if (redirectToLoginIfNeeded(response)) return;

            if (!response.ok) {
                throw new Error("댓글 등록에 실패했습니다.");
            }

            commentContentEl.value = "";
            updateCommentLengthText();
            await loadDetail();
        } catch (error) {
            console.error(error);
            alert("댓글 등록 중 오류가 발생했습니다.");
        }
    }

    // ===== 로그인 리다이렉트 처리 =====
    function redirectToLoginIfNeeded(response) {
        const isLoginRedirect =
            response.status === 401 ||
            response.redirected ||
            response.url.includes("/login");

        if (isLoginRedirect) {
            alert("로그인이 필요합니다.");
            location.href = "/login";
            return true;
        }

        return false;
    }

    // ===== 댓글 길이 표시 =====
    function updateCommentLengthText() {
        commentLengthTextEl.textContent = `${commentContentEl.value.length} / 500`;
    }

    // ===== 댓글 수 표시 =====
    function updateCommentCount(comments) {
        commentCountTextEl.textContent = `${comments.length}개`;
    }

    // ===== 초기 로딩 =====
    loadDetail();
});