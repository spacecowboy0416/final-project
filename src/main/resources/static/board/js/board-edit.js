document.addEventListener("DOMContentLoaded", () => {
    const postId = window.location.pathname.split("/")[2];

    const titleEl = document.getElementById("title");
    const contentEl = document.getElementById("content");
    const updateBtn = document.getElementById("updateBtn");
    const cancelBtn = document.getElementById("cancelBtn");

    updateBtn.addEventListener("click", updatePost);
    cancelBtn.addEventListener("click", () => {
        location.href = `/board/${postId}`;
    });

    async function loadPost() {
        try {
            const response = await fetch(`/api/board/posts/${postId}`);
            if (!response.ok) {
                throw new Error("게시글 조회에 실패했습니다.");
            }

            const post = await response.json();

            titleEl.value = post.title ?? "";
            contentEl.value = post.content ?? "";
        } catch (error) {
            console.error(error);
            alert("게시글 정보를 불러오지 못했습니다.");
        }
    }

    async function updatePost() {
        const title = titleEl.value.trim();
        const content = contentEl.value.trim();

        if (!title) {
            alert("제목을 입력해주세요.");
            titleEl.focus();
            return;
        }

        try {
            const response = await fetch(`/api/board/posts/${postId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    title,
                    content
                })
            });

            if (!response.ok) {
                throw new Error("게시글 수정에 실패했습니다.");
            }

            location.href = `/board/${postId}`;
        } catch (error) {
            console.error(error);
            alert("게시글 수정 중 오류가 발생했습니다.");
        }
    }

    loadPost();
});