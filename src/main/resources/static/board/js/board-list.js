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

            const response = await fetch(`/api/board/posts?${params.toString()}`);

            if (!response.ok) {
                throw new Error(`목록 조회 실패: ${response.status}`);
            }

            const data = await response.json();
            const posts = data.posts || [];

            renderPosts(posts);
            postCountTextEl.textContent = `총 ${posts.length}개의 게시글`;
        } catch (error) {
            console.error("게시글 목록 API 실패 → 더미 데이터로 테스트합니다.", error);

            const dummyPosts = [
                {
                    postId: 101,
                    title: "맑은 날 데이트 코디",
                    contentPreview: "셔츠와 슬랙스를 조합해 깔끔하고 단정한 무드로 맞춘 데이트룩입니다.",
                    weatherStatus: "clear",
                    temp: 22,
                    styleType: "minimal",
                    tpoType: "date",
                    topItemName: "화이트 셔츠",
                    bottomItemName: "블랙 슬랙스",
                    viewCount: 24,
                    commentCount: 5,
                    thumbnailImageUrl: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=80"
                },
                {
                    postId: 102,
                    title: "비 오는 날 캐주얼 코디",
                    contentPreview: "가벼운 아우터와 데님으로 편하게 입기 좋은 우중 코디예요.",
                    weatherStatus: "rain",
                    temp: 16,
                    styleType: "comfortable",
                    tpoType: "casual",
                    topItemName: "방수 자켓",
                    bottomItemName: "와이드 데님",
                    viewCount: 12,
                    commentCount: 2,
                    thumbnailImageUrl: "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=900&q=80"
                },
                {
                    postId: 103,
                    title: "출근용 클래식 코디",
                    contentPreview: "오피스에서 단정하게 보이면서도 답답하지 않은 포멀 스타일입니다.",
                    weatherStatus: "cloudy",
                    temp: 19,
                    styleType: "classic",
                    tpoType: "work",
                    topItemName: "베이지 블레이저",
                    bottomItemName: "네이비 슬랙스",
                    viewCount: 31,
                    commentCount: 6,
                    thumbnailImageUrl: "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=80"
                },
                {
                    postId: 104,
                    title: "운동 가는 날 스포티룩",
                    contentPreview: "가볍고 활동성 좋은 조합으로 운동 전후에 입기 편한 코디예요.",
                    weatherStatus: "windy",
                    temp: 18,
                    styleType: "sporty",
                    tpoType: "exercise",
                    topItemName: "후드 집업",
                    bottomItemName: "조거 팬츠",
                    viewCount: 15,
                    commentCount: 1,
                    thumbnailImageUrl: "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=80"
                },
                {
                    postId: 105,
                    title: "여행용 스트릿 코디",
                    contentPreview: "사진도 잘 나오고 오래 걸어도 편한 여행 코디로 골라봤어요.",
                    weatherStatus: "partly_cloudy",
                    temp: 21,
                    styleType: "street",
                    tpoType: "travel",
                    topItemName: "오버핏 티셔츠",
                    bottomItemName: "카고 팬츠",
                    viewCount: 18,
                    commentCount: 4,
                    thumbnailImageUrl: "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=900&q=80"
                },
                {
                    postId: 106,
                    title: "하객룩 참고 코디",
                    contentPreview: "차분한 컬러감으로 정리한 단정한 하객 코디입니다.",
                    weatherStatus: "clear",
                    temp: 23,
                    styleType: "glam",
                    tpoType: "wedding",
                    topItemName: "트위드 자켓",
                    bottomItemName: "롱 스커트",
                    viewCount: 40,
                    commentCount: 8,
                    thumbnailImageUrl: "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=900&q=80"
                }
            ];

            const filteredDummyPosts = dummyPosts.filter(post => {
                const weatherMatch = !weatherEl.value || post.weatherStatus === weatherEl.value;
                const styleMatch = !styleEl.value || post.styleType === styleEl.value;
                const tpoMatch = !tpoEl.value || post.tpoType === tpoEl.value;
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

        postListEl.innerHTML = posts.map(post => `
            <article class="board-card">
                <a href="/board/${post.postId}" class="board-card__thumb-link">
                    ${post.thumbnailImageUrl
                        ? `<img class="board-card__thumb" src="${post.thumbnailImageUrl}" alt="${escapeHtml(post.title)}">`
                        : `<div class="board-card__thumb board-card__thumb--empty"></div>`
                    }
                </a>

                <div class="board-card__body">
                    <div class="board-card__badge-row">
                        <span class="board-card__badge">${escapeHtml(post.weatherStatus ?? "-")}</span>
                        <span class="board-card__badge">${escapeHtml(post.styleType ?? "-")}</span>
                        <span class="board-card__badge">${escapeHtml(post.tpoType ?? "-")}</span>
                    </div>

                    <h3 class="board-card__title">
                        <a href="/board/${post.postId}">${escapeHtml(post.title ?? "제목 없음")}</a>
                    </h3>

                    <p class="board-card__preview">
                        ${escapeHtml(post.contentPreview ?? "내용이 없습니다.")}
                    </p>

                    <div class="board-card__meta">
                        <div class="board-card__meta-item">
                            <span>온도</span>
                            <strong>${escapeHtml(String(post.temp ?? "-"))}°</strong>
                        </div>
                        <div class="board-card__meta-item">
                            <span>코디</span>
                            <strong>${escapeHtml(post.topItemName ?? "-")} / ${escapeHtml(post.bottomItemName ?? "-")}</strong>
                        </div>
                        <div class="board-card__meta-item">
                            <span>조회수</span>
                            <strong>${escapeHtml(String(post.viewCount ?? 0))}</strong>
                        </div>
                        <div class="board-card__meta-item">
                            <span>댓글수</span>
                            <strong>${escapeHtml(String(post.commentCount ?? 0))}</strong>
                        </div>
                    </div>
                </div>
            </article>
        `).join("");
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    loadPosts();
});