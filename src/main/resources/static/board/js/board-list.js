// board-list.js
document.addEventListener("DOMContentLoaded", () => {
    const {
        formatWeather,
        formatStyle,
        formatTpo,
        normalizeSlotKey,
        getSlotOrderIndex,
        escapeHtml,
        escapeAttr
    } = window.BoardCommon || {};

    if (!window.BoardCommon) {
        console.error("board-common.js가 먼저 로드되어야 합니다.");
        return;
    }

    // ===== 필터 / 정렬 요소 =====
    const weatherEl = document.getElementById("weather");
    const styleEl = document.getElementById("style");
    const tpoEl = document.getElementById("tpo");
    const sortButtons = document.querySelectorAll(".board-sort-btn");

    // ===== 버튼 / 출력 영역 =====
    const searchBtn = document.getElementById("searchBtn");
    const resetBtn = document.getElementById("resetBtn");
    const postListEl = document.getElementById("postList");
    const postColLeftEl = document.getElementById("postColLeft");
    const postColRightEl = document.getElementById("postColRight");
    const postCountTextEl = document.getElementById("postCountText");

    let currentSort = "latest";

    // ===== 필수 요소 체크 =====
    if (
        !weatherEl ||
        !styleEl ||
        !tpoEl ||
        !searchBtn ||
        !resetBtn ||
        !postListEl ||
        !postColLeftEl ||
        !postColRightEl ||
        !postCountTextEl ||
        sortButtons.length === 0
    ) {
        console.error("board-list.js: 필요한 DOM 요소를 찾지 못했습니다.");
        return;
    }

    // ===== 이벤트 바인딩 =====
    searchBtn.addEventListener("click", loadPosts);

    resetBtn.addEventListener("click", () => {
        weatherEl.value = "";
        styleEl.value = "";
        tpoEl.value = "";
        currentSort = "latest";

        updateSortUI();
        loadPosts();
    });

    weatherEl.addEventListener("change", loadPosts);
    styleEl.addEventListener("change", loadPosts);
    tpoEl.addEventListener("change", loadPosts);

    sortButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            currentSort = btn.dataset.sort;
            updateSortUI();
            loadPosts();
        });
    });

    // ===== 정렬 버튼 active 상태 =====
    function updateSortUI() {
        sortButtons.forEach((btn) => {
            btn.classList.toggle("active", btn.dataset.sort === currentSort);
        });
    }

    // ===== 게시글 목록 조회 =====
    async function loadPosts() {
        const startTime = Date.now();

        try {
            renderSkeletonCards();

            const params = new URLSearchParams();

            if (weatherEl.value) params.append("weather", weatherEl.value);
            if (styleEl.value) params.append("style", styleEl.value);
            if (tpoEl.value) params.append("tpo", tpoEl.value);
            params.append("sort", currentSort);

            const requestUrl = `/api/board/posts?${params.toString()}`;
            const response = await fetch(requestUrl);

            if (!response.ok) {
                throw new Error(`목록 조회 실패: ${response.status}`);
            }

            const data = await response.json();
            const posts = extractPostList(data);

            // 스켈레톤이 너무 짧게 번쩍이지 않도록 최소 시간 유지
            const elapsed = Date.now() - startTime;
            const delay = Math.max(400 - elapsed, 0);
            await new Promise(resolve => setTimeout(resolve, delay));

            renderPosts(posts);
            postCountTextEl.textContent = `총 ${posts.length}개의 게시글`;
        } catch (error) {
            console.error("게시글 목록 API 실패", error);

            postColLeftEl.innerHTML = `
                <div class="board-error">
                    게시글을 불러오지 못했습니다 😢<br>
                    잠시 후 다시 시도해주세요.
                </div>
            `;
            postColRightEl.innerHTML = "";
            postCountTextEl.textContent = "총 0개의 게시글";
        }
    }

    // ===== 응답 데이터 정규화 =====
    function extractPostList(data) {
        if (Array.isArray(data)) return data;
        if (Array.isArray(data?.posts)) return data.posts;
        if (Array.isArray(data?.content)) return data.content;
        return [];
    }

    // ===== 스켈레톤 렌더링 =====
    function renderSkeletonCards(count = 6) {
        const leftColumnHtml = [];
        const rightColumnHtml = [];

        for (let i = 0; i < count; i++) {
            const skeletonHtml = `
                <article class="board-feed-card board-feed-card--skeleton">
                    <div class="board-feed-card__link">
                        <div class="board-feed-card__media media-1">
                            <div class="media-img">
                                <div class="board-skeleton-block"></div>
                            </div>
                        </div>

                        <div class="board-feed-card__tag-bar">
                            <div class="board-feed-card__badge-row">
                                <span class="board-skeleton-badge"></span>
                                <span class="board-skeleton-badge"></span>
                                <span class="board-skeleton-badge"></span>
                            </div>
                        </div>
                    </div>
                </article>
            `;

            if (i % 2 === 0) {
                leftColumnHtml.push(skeletonHtml);
            } else {
                rightColumnHtml.push(skeletonHtml);
            }
        }

        postColLeftEl.innerHTML = leftColumnHtml.join("");
        postColRightEl.innerHTML = rightColumnHtml.join("");
    }

    // ===== 게시글 렌더링 =====
    function renderPosts(posts) {
        if (!posts || posts.length === 0) {
            postColLeftEl.innerHTML = `<div class="board-empty">조건에 맞는 게시글이 없습니다.</div>`;
            postColRightEl.innerHTML = "";
            return;
        }

        postColLeftEl.innerHTML = "";
        postColRightEl.innerHTML = "";

        posts.forEach((post, index) => {
            const wrapper = document.createElement("div");
            wrapper.innerHTML = createPostCardHtml(post);

            const cardEl = wrapper.firstElementChild;
            if (!cardEl) return;

            const leftHeight = postColLeftEl.offsetHeight;
            const rightHeight = postColRightEl.offsetHeight;

            if (leftHeight <= rightHeight) {
                postColLeftEl.appendChild(cardEl);
            } else {
                postColRightEl.appendChild(cardEl);
            }

            setTimeout(() => {
                cardEl.classList.add("show");
            }, index * 40);
        });
    }

    // ===== 게시글 카드 HTML 생성 =====
    function createPostCardHtml(post) {
        return `
            <article class="board-feed-card">
                <a href="/board/${escapeAttr(post.postId)}" class="board-feed-card__link">
                    ${renderFeedMedia(post)}

                    <div class="board-feed-card__tag-bar">
                        <div class="board-feed-card__badge-row">
                            <span class="board-feed-card__badge">
                                ${escapeHtml(formatWeather(post.weatherStatus ?? post.weather ?? "-"))}
                            </span>
                            <span class="board-feed-card__badge">
                                ${escapeHtml(formatStyle(post.styleType ?? post.style))}
                            </span>
                            <span class="board-feed-card__badge">
                                ${escapeHtml(formatTpo(post.tpoType ?? post.tpo))}
                            </span>
                        </div>
                    </div>

                    <div class="board-feed-card__hover-info">
                        <h3 class="board-feed-card__title">
                            ${escapeHtml(post.title ?? "제목 없음")}
                        </h3>

                        <div class="board-feed-card__meta">
                            <span class="board-feed-card__author">
                                ${escapeHtml(post.nickname ?? post.authorNickname ?? "작성자")}
                            </span>
                            <span class="board-feed-card__stats">
                                조회 ${escapeHtml(String(post.viewCount ?? 0))} · 댓글 ${escapeHtml(String(post.commentCount ?? 0))}
                            </span>
                        </div>
                    </div>
                </a>
            </article>
        `;
    }

    // ===== 카드 이미지 렌더링 =====
    function renderFeedMedia(post) {
        const previewItems = Array.isArray(post.previewItems) ? post.previewItems : [];

        const normalizedItems = previewItems
            .filter(item => item && item.imageUrl && String(item.imageUrl).trim() !== "")
            .map(item => ({
                slotKey: normalizeSlotKey(item.slotKey ?? ""),
                imageUrl: item.imageUrl
            }))
            .sort((a, b) => getSlotOrderIndex(a.slotKey) - getSlotOrderIndex(b.slotKey));

        const uniqueItems = normalizedItems.filter((item, index, array) => {
            return index === array.findIndex(target =>
                target.slotKey === item.slotKey && target.imageUrl === item.imageUrl
            );
        });

        if (uniqueItems.length === 0) {
            return `
                <div class="board-feed-card__media media-1">
                    ${renderThumb("", "이미지 없음", "media-img")}
                </div>
            `;
        }

        const visibleItems = uniqueItems.slice(0, 6);
        const remainCount = Math.max(Number(post.extraItemCount ?? 0), 0);
        const layoutClass = `media-${Math.min(visibleItems.length, 6)}`;

        return `
            <div class="board-feed-card__media ${layoutClass}">
                ${visibleItems.map((item, index) => {
                    const isLastVisible = index === visibleItems.length - 1;
                    const moreText = remainCount > 0 && isLastVisible ? `+${remainCount}` : "";

                    return renderThumb(
                        item.imageUrl,
                        item.slotKey || `이미지 ${index + 1}`,
                        "media-img",
                        moreText
                    );
                }).join("")}
            </div>
        `;
    }

    // ===== 단일 썸네일 렌더링 =====
    function renderThumb(imageUrl, altText, className, moreText = "") {
        if (!imageUrl) {
            return `<div class="${className} ${className}--empty"></div>`;
        }

        return `
            <div class="${className}">
                <img
                    src="${escapeAttr(imageUrl)}"
                    alt="${escapeAttr(altText)}"
                    onerror="this.parentElement.outerHTML='&lt;div class=&quot;${className} ${className}--empty&quot;&gt;&lt;/div&gt;'"
                >
                ${moreText ? `
                    <div class="media-img__more">
                        ${escapeHtml(moreText)}
                        <span>more</span>
                    </div>
                ` : ""}
            </div>
        `;
    }

    // ===== 초기 실행 =====
    updateSortUI();
    loadPosts();
});