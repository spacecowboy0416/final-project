document.addEventListener("DOMContentLoaded", () => {
  const navLinks = document.querySelectorAll(".admin-nav a");
  const contentArea = document.getElementById("admin-content-area");
  const initialPanel = document.getElementById("initial-panel");

  const templates = {
    "item-management": document.getElementById("item-management-template"),
    "user-management": document.getElementById("user-management-template"),
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

      if (targetId === "item-management") {
        attachTagFormEvents();
      } else if (targetId === "user-management") {
        loadUserManagementPanel();
      }
    } else {
      const div = document.createElement("div");
      div.className = "glass-panel";
      div.innerHTML = `<h1 class="glass-panel-title">${targetId.replace(
        /-/g,
        " ",
      )}</h1><p class="glass-panel-text">이 섹션의 콘텐츠가 준비 중입니다.</p>`;
      contentArea.appendChild(div);
    }
  }

  navLinks.forEach((link) => {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      navLinks.forEach((navLink) => navLink.classList.remove("active"));
      link.classList.add("active");
      const targetId = link.getAttribute("href").substring(1);
      history.pushState(null, null, `#${targetId}`);
      loadContent(targetId);
    });
  });

  function handleInitialLoad() {
    if (window.location.hash) {
      const initialTargetId = window.location.hash.substring(1);
      const linkToActivate = document.querySelector(
        `.admin-nav a[href="#${initialTargetId}"]`,
      );
      if (linkToActivate) {
        linkToActivate.click();
      }
    }
  }

  window.addEventListener("popstate", handleInitialLoad);
  handleInitialLoad();
});

// --- User Management Functions ---
let currentSort = "id_desc"; // Default sort

async function loadUserManagementPanel() {
  await fetchAndRenderUsers();
  attachUserPanelEvents();
}

function attachUserPanelEvents() {
  document
    .getElementById("role-filter")
    .addEventListener("change", fetchAndRenderUsers);
  document
    .getElementById("search-user-btn")
    .addEventListener("click", fetchAndRenderUsers);
  document
    .getElementById("user-search-input")
    .addEventListener("keyup", (e) => {
      if (e.key === "Enter") fetchAndRenderUsers();
    });

  document.getElementById("nickname-sort-header").addEventListener("click", () => {
    if (currentSort === "name_asc") {
      currentSort = "name_desc";
    } else {
      currentSort = "name_asc";
    }
    fetchAndRenderUsers();
  });

  document.getElementById("user-table-body").addEventListener("click", (e) => {
    if (e.target.classList.contains("btn-save")) {
      const userId = e.target.dataset.id;
      const roleSelect = document.getElementById(`role-select-${userId}`);
      const statusSelect = document.getElementById(`status-select-${userId}`);
      const newRole = roleSelect.value;
      const newStatus = statusSelect.value;
      updateUser(userId, newRole, newStatus);
    }
  });
}

function updateSortUI() {
  const ascArrow = document.querySelector("#nickname-sort-header .sort-arrow.asc");
  const descArrow = document.querySelector("#nickname-sort-header .sort-arrow.desc");
  if (!ascArrow || !descArrow) return;

  ascArrow.classList.remove("active");
  descArrow.classList.remove("active");

  if (currentSort === "name_asc") {
    ascArrow.classList.add("active");
  } else if (currentSort === "name_desc") {
    descArrow.classList.add("active");
  }
}

async function fetchAndRenderUsers() {
  updateSortUI(); // Update UI before fetching
  const role = document.getElementById("role-filter").value;
  const searchTerm = document.getElementById("user-search-input").value;

  const query = new URLSearchParams({
    role: role,
    searchTerm: searchTerm,
    sort: currentSort,
  });

  try {
    const response = await fetch(`/api/admin/users?${query.toString()}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const users = await response.json();
    renderUsers(users);
  } catch (error) {
    console.error("Failed to fetch users:", error);
    const tableBody = document.getElementById("user-table-body");
    tableBody.innerHTML = `<tr><td colspan="10" class="text-center text-danger">사용자 정보를 불러오는 데 실패했습니다.</td></tr>`;
  }
}

function renderUsers(users) {
  const tableBody = document.getElementById("user-table-body");
  const emptyMessage = document.querySelector(
    "#user-management-template .empty-list-message",
  );

  if (!users || users.length === 0) {
    tableBody.innerHTML = "";
    if (emptyMessage) emptyMessage.style.display = "block";
    return;
  }

  if (emptyMessage) emptyMessage.style.display = "none";
  tableBody.innerHTML = users
    .map(
      (user) => `
    <tr data-user-id="${user.userId}">
      <td>${user.userId}</td>
      <td><img src="${
        user.profileImageUrl || "https://i.pravatar.cc/150?u=default"
      }" alt="${user.nickname}" class="user-profile-img" referrerPolicy="no-referrer"></td>
      <td>${user.nickname}</td>
      <td>${user.email}</td>
      <td>${getSocialIcon(user.provider)}</td>
      <td><span class="role-badge role-badge-${user.role}">${user.role}</span></td>
      <td><span class="status-badge status-badge-${user.status}">${user.status}</span></td>
      <td>${formatDate(user.createdAt)}</td>
      <td>${formatDate(user.lastLoginAt)}</td>
      <td class="user-actions">
        <select id="role-select-${user.userId}" class="form-select">
          <option value="USER" ${user.role === "USER" ? "selected" : ""}>USER</option>
          <option value="ADMIN" ${user.role === "ADMIN" ? "selected" : ""}>ADMIN</option>
          <option value="MASTER" ${user.role === "MASTER" ? "selected" : ""}>MASTER</option>
        </select>
        <select id="status-select-${user.userId}" class="form-select">
          <option value="ACTIVE" ${user.status === "ACTIVE" ? "selected" : ""}>활성</option>
          <option value="INACTIVE" ${user.status === "INACTIVE" ? "selected" : ""}>비활성</option>
          <option value="SUSPENDED" ${user.status === "SUSPENDED" ? "selected" : ""}>정지</option>
        </select>
        <button class="btn btn-save" data-id="${user.userId}">저장</button>
      </td>
    </tr>
  `,
    )
    .join("");
}

async function updateUser(userId, role, status) {
  try {
    const response = await fetch(`/api/admin/users/${userId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ role: role, status: status }),
    });

    if (response.ok) {
      alert(`사용자 ID ${userId}의 정보가 변경되었습니다.`);
      await fetchAndRenderUsers(); // 목록 새로고침
    } else {
      const errorData = await response.json();
      throw new Error(errorData.message || "정보 변경에 실패했습니다.");
    }
  } catch (error) {
    console.error("Failed to update user:", error);
    alert(`오류: ${error.message}`);
  }
}

function getSocialIcon(provider) {
  if (!provider) return "N/A";
  const sanitizedProvider = provider.toLowerCase();
  const iconUrl = `https://www.google.com/s2/favicons?domain=${sanitizedProvider}.com`;
  return `<img src="${iconUrl}" alt="${provider}" class="social-icon"> ${provider}`;
}

function formatDate(dateString) {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return date.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

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
