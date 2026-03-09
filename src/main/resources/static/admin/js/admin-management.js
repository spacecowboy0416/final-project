document.addEventListener("DOMContentLoaded", () => {
    const navLinks = document.querySelectorAll(".admin-nav a");
    const contentArea = document.getElementById("admin-content-area");
    const initialPanel = document.getElementById("initial-panel");

    const templates = {
        "item-management": document.getElementById("item-management-template"),
        // 'user-management': document.getElementById('user-management-template'),
    };

    function loadContent(targetId) {
        if (initialPanel) {
            initialPanel.style.display = "none";
        }
        contentArea.innerHTML = "";

        const template = templates[targetId];
        if (template) {
            const clone = template.content.cloneNode(true);
            contentArea.appendChild(clone);
            attachTagFormEvents();
        } else {
            const div = document.createElement("div");
            div.className = "glass-panel";
            div.innerHTML = `<h1 class="glass-panel-title">${targetId.replace(/-/g, " ")}</h1><p class="glass-panel-text">이 섹션의 콘텐츠가 준비 중입니다.</p>`;
            contentArea.appendChild(div);
        }
    }

    navLinks.forEach((link) => {
        link.addEventListener("click", (event) => {
            event.preventDefault();

            navLinks.forEach((navLink) => navLink.classList.remove("active"));
            link.classList.add("active");

            const targetId = link.getAttribute("href").substring(1);
            // Update URL hash without jumping
            history.pushState(null, null, `#${targetId}`);
            loadContent(targetId);
        });
    });

    function handleInitialLoad() {
        if (window.location.hash) {
            const initialTargetId = window.location.hash.substring(1);
            const linkToActivate = document.querySelector(
                `.admin-nav a[href="#${initialTargetId}"]`
            );
            if (linkToActivate) {
                linkToActivate.click();
            }
        }
    }

    window.addEventListener('popstate', handleInitialLoad);
    handleInitialLoad(); // Initial load
});

// --- Item Management Functions ---
function attachTagFormEvents() {
    const tagForm = document.getElementById("tag-form");
    if (tagForm) {
        tagForm.addEventListener("submit", (event) => {
            event.preventDefault();
            saveTag();
        });
    }
}

function saveTag() {
    const tagId = document.getElementById("tag-id").value;
    const tagName = document.getElementById("tag-name").value;
    const tagCategory = document.getElementById("tag-category").value;

    if (!tagName || !tagCategory) {
        alert("태그명과 카테고리를 모두 입력/선택해주세요.");
        return;
    }

    const tagData = { id: tagId, name: tagName, category: tagCategory };

    console.log("Saving tag (실제 API 호출 필요):", tagData);
    alert(`'${tagName}' 태그가 저장되었습니다. (콘솔에서 확인)`);

    clearTagForm();
    // TODO: 성공 응답 후, 태그 목록을 다시 로드하는 함수 호출
    // loadTags();
}

function editTag(id, name, category) {
    document.getElementById("tag-id").value = id;
    document.getElementById("tag-name").value = name;
    document.getElementById("tag-category").value = category;
    document.getElementById("tag-name").focus();
}

function deleteTag(id) {
    if (confirm(`태그 ID ${id}를 정말 삭제하시겠습니까?`)) {
        console.log(`Deleting tag ${id} (실제 API 호출 필요)`);
        alert(`태그 ID ${id}가 삭제되었습니다. (콘솔에서 확인)`);

        const tagElement = document.querySelector(`.tag-item[data-id='${id}']`);
        if (tagElement) {
            tagElement.remove();
        }
    }
}

function clearTagForm() {
    const form = document.getElementById("tag-form");
    if (form) {
        form.reset();
        document.getElementById("tag-id").value = "";
    }
}
