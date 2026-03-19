document.addEventListener("DOMContentLoaded", () => {
    const weatherEl = document.getElementById("weather");
    const styleEl = document.getElementById("style");
    const tpoEl = document.getElementById("tpo");

    const searchBtn = document.getElementById("searchBtn");
    const resetBtn = document.getElementById("resetBtn");
    const postListEl = document.getElementById("postList");
    const postCountTextEl = document.getElementById("postCountText");

    searchBtn.addEventListener("click", loadPosts);

    resetBtn.addEventListener("click", () => {
        weatherEl.value = "";
        styleEl.value = "";
        tpoEl.value = "";
        loadPosts();
    });

    async function loadPosts() {
        try {
            postListEl.innerHTML = `<div class="board-loading">게시글을 불러오는 중입니다...</div>`;
            postCountTextEl.textContent = "불러오는 중...";

            const params = new URLSearchParams();

            if (weatherEl.value) params.append("weather", weatherEl.value);
            if (styleEl.value) params.append("style", styleEl.value);
            if (tpoEl.value) params.append("tpo", tpoEl.value);

            const queryString = params.toString();
            const response = await fetch(`/api/board/posts${queryString ? `?${queryString}` : ""}`);

            if (!response.ok) {
                throw new Error(`목록 조회 실패: ${response.status}`);
            }

            const data = await response.json();
            const posts = data.posts || data.content || data || [];

            renderPosts(Array.isArray(posts) ? posts : []);
            postCountTextEl.textContent = `총 ${Array.isArray(posts) ? posts.length : 0}개의 게시글`;
        } catch (error) {
            console.error("게시글 목록 API 실패 → 더미 데이터로 테스트합니다.", error);

            const dummyPosts = [
                {
                    postId: 101,
                    title: "맑은 날 데이트 코디",
                    weatherStatus: "clear",
                    styleType: "minimal",
                    tpoType: "date",
                    nickname: "지은",
                    viewCount: 24,
                    commentCount: 5,
                    previewItems: [
                        { slotKey: "tops", productName: "화이트 셔츠", imageUrl: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "bottoms", productName: "슬랙스", imageUrl: "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "shoes", productName: "로퍼", imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "outerwear", productName: "자켓", imageUrl: "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=900&q=80" }
                    ],
                    extraItemCount: 0
                },
                {
                    postId: 102,
                    title: "비 오는 날 캐주얼 코디",
                    weatherStatus: "rain",
                    styleType: "comfortable",
                    tpoType: "casual",
                    nickname: "코디러버",
                    viewCount: 12,
                    commentCount: 2,
                    previewItems: [
                        { slotKey: "tops", productName: "맨투맨", imageUrl: "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "bottoms", productName: "청바지", imageUrl: "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "shoes", productName: "스니커즈", imageUrl: "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=900&q=80" }
                    ],
                    extraItemCount: 0
                },
                {
                    postId: 103,
                    title: "출근용 클래식 코디",
                    weatherStatus: "cloudy",
                    styleType: "classic",
                    tpoType: "work",
                    nickname: "오피스룩",
                    viewCount: 31,
                    commentCount: 6,
                    previewItems: [
                        { slotKey: "tops", productName: "셔츠", imageUrl: "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "bottoms", productName: "슬랙스", imageUrl: "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "shoes", productName: "구두", imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "outerwear", productName: "코트", imageUrl: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=80" },
                        { slotKey: "accessories", productName: "시계", imageUrl: "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=900&q=80" }
                    ],
                    extraItemCount: 1
                }
            ];

            const filteredDummyPosts = dummyPosts.filter(post => {
                const weatherMatch = !weatherEl.value || String(post.weatherStatus).toLowerCase() === weatherEl.value.toLowerCase();
                const styleMatch = !styleEl.value || String(post.styleType).toLowerCase() === styleEl.value.toLowerCase();
                const tpoMatch = !tpoEl.value || String(post.tpoType).toLowerCase() === tpoEl.value.toLowerCase();
                return weatherMatch && styleMatch && tpoMatch;
            });

            renderPosts(filteredDummyPosts);
            postCountTextEl.textContent = `테스트 데이터 ${filteredDummyPosts.length}개`;
        }
    }

    function renderPosts(posts) {
        if (!posts || posts.length === 0) {
            postListEl.innerHTML = `<div class="board-empty">조건에 맞는 게시글이 없습니다.</div>`;
            return;
        }

        postListEl.innerHTML = posts.map(post => {
            return `
                <article class="board-feed-card">
                    <a href="/board/${post.postId}" class="board-feed-card__link">
                        ${renderFeedMedia(post)}

                        <div class="board-feed-card__tag-bar">
                            <div class="board-feed-card__badge-row">
                                <span class="board-feed-card__badge">${escapeHtml(formatWeather(post.weatherStatus ?? post.weather ?? "-"))}</span>
                                <span class="board-feed-card__badge">${escapeHtml(post.styleType ?? post.style ?? "-")}</span>
                                <span class="board-feed-card__badge">${escapeHtml(post.tpoType ?? post.tpo ?? "-")}</span>
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
        }).join("");
    }

    function renderFeedMedia(post) {
        const previewItems = Array.isArray(post.previewItems) ? post.previewItems : [];

        const uniqueImages = [...new Set(
            previewItems
                .map(item => item?.imageUrl)
                .filter(Boolean)
        )];

        if (uniqueImages.length === 0) {
            return `
                <div class="board-feed-card__media media-1">
                    ${renderThumb("", "이미지 없음", "media-img")}
                </div>
            `;
        }

        const visibleImages = uniqueImages.slice(0, 6);
        const remainCount = Number(post.extraItemCount ?? 0);
        const layoutClass = `media-${Math.min(visibleImages.length, 6)}`;

        return `
            <div class="board-feed-card__media ${layoutClass}">
                ${visibleImages.map((imageUrl, index) => {
                    const isLastVisible = index === visibleImages.length - 1;
                    const moreText = remainCount > 0 && isLastVisible ? `+${remainCount}` : "";

                    return renderThumb(
                        imageUrl,
                        `이미지 ${index + 1}`,
                        "media-img",
                        moreText
                    );
                }).join("")}
            </div>
        `;
    }

    function renderThumb(imageUrl, altText, className, moreText = "") {
        if (!imageUrl) {
            return `<div class="${className} ${className}--empty"></div>`;
        }

        return `
            <div class="${className}">
                <img
                    src="${escapeAttr(imageUrl)}"
                    alt="${escapeAttr(altText)}"
                    onerror="this.parentElement.outerHTML='<div class=&quot;${className} ${className}--empty&quot;></div>'"
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

    loadPosts();
});