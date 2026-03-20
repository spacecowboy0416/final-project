document.addEventListener("DOMContentLoaded", () => {
  const navLinks = document.querySelectorAll(".admin-nav a");
  const contentArea = document.getElementById("admin-content-area");
  const initialPanel = document.getElementById("initial-panel");

  const templates = {
    "item-management": document.getElementById("item-management-template"),
    "user-management": document.getElementById("user-management-template"),
    "community-management": document.getElementById("community-management-template"),
    "system-stats": document.getElementById("system-stats-template"),
  };

  function loadContent(targetId) {
    if (initialPanel) initialPanel.style.display = "none";
    contentArea.innerHTML = "";

    const template = templates[targetId];
    if (template) {
      const clone = template.content.cloneNode(true);
      contentArea.appendChild(clone);

      if (targetId === "item-management") {
        attachItemPanelEvents();
      } else if (targetId === "user-management") {
        loadUserManagementPanel();
      } else if (targetId === "community-management") {
        loadCommunityManagementPanel();
      } else if (targetId === "system-stats") {
        loadSystemStatsPanel();
      }
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
      history.pushState(null, null, `#${targetId}`);
      loadContent(targetId);
    });
  });

  function handleInitialLoad() {
    const hash = window.location.hash;
    if (hash) {
      const initialTargetId = hash.substring(1);
      const linkToActivate = document.querySelector(
        `.admin-nav a[href="#${initialTargetId}"]`,
      );
      if (linkToActivate) linkToActivate.click();
    } else {
      const firstLink = document.querySelector(".admin-nav a");
      if (firstLink) firstLink.click();
    }
  }

  window.addEventListener("popstate", handleInitialLoad);
  handleInitialLoad();
});

// --- Item (Tag) Management Functions ---
function attachItemPanelEvents() {
  const container = document.getElementById("item-management-content");
  if (!container) return;

  const selector = container.querySelector("#tag-type-selector");
  const contentArea = container.querySelector("#tag-content-area");

  selector.addEventListener("change", () => {
    const selectedType = selector.value;
    if (selectedType) {
      loadTagsForType(selectedType, contentArea);
    } else {
      contentArea.innerHTML =
        '<p class="empty-list-message">위에서 태그 종류를 선택해주세요.</p>';
    }
  });

  if (selector.options.length > 1) {
    selector.value = selector.options[1].value;
    loadTagsForType(selector.value, contentArea);
  } else {
    contentArea.innerHTML =
      '<p class="empty-list-message">관리할 태그 종류가 없습니다.</p>';
  }

  contentArea.addEventListener("submit", async (e) => {
    if (e.target.matches(".add-tag-form")) {
      e.preventDefault();
      const form = e.target;
      const type = form.dataset.type;
      const nameInput = form.querySelector('input[name="name"]');
      const name = nameInput.value.trim();

      if (!name) {
        alert("태그 이름을 입력하세요.");
        return;
      }

      try {
        const response = await fetch("/api/admin/tags", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ type, name }),
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(
            error.message || "태그 추가 실패 (중복된 이름일 수 있습니다).",
          );
        }

        await loadTagsForType(type, contentArea);
      } catch (error) {
        alert(`오류: ${error.message}`);
      }
    }
  });

  contentArea.addEventListener("click", async (e) => {
    const targetButton = e.target;
    if (targetButton.classList.contains("btn-delete")) {
      const tagItem = targetButton.closest(".tag-item");
      const tagId = tagItem.dataset.tagId;
      const tagName = tagItem.dataset.tagName;
      const type = tagItem.dataset.tagType;

      if (confirm(`'${tagName}' 태그를 정말 삭제하시겠습니까?`)) {
        try {
          const response = await fetch(`/api/admin/tags/${tagId}`, {
            method: "DELETE",
          });
          if (!response.ok) throw new Error("태그 삭제 실패.");
          await loadTagsForType(type, contentArea);
        } catch (error) {
          alert(`오류: ${error.message}`);
        }
      }
    }

    if (targetButton.classList.contains("btn-edit")) {
      const tagItem = targetButton.closest(".tag-item");
      handleEditTag(tagItem, contentArea);
    }
  });
}

async function loadTagsForType(type, container) {
  container.innerHTML = `<div class="tag-list-container"><div class="tag-list"><p class="empty-list-message">로딩 중...</p></div></div>`;
  try {
    const response = await fetch(`/api/admin/tags?type=${type}`);
    if (!response.ok) throw new Error("태그를 불러오는 데 실패했습니다.");
    const tags = await response.json();

    let tagListHtml =
      tags.length > 0
        ? tags.map(createTagElementHtml).join("")
        : '<p class="empty-list-message">이 종류에는 등록된 태그가 없습니다.</p>';

    container.innerHTML = `
            <div class="item-management-layout">
                <div class="tag-list-container">
                    <div class="tag-list">
                        ${tagListHtml}
                    </div>
                </div>
                <div class="tag-form-container">
                    <h2 class="item-management-title" style="font-size: 1.25rem;">'${type}' 태그 추가</h2>
                    <form class="add-tag-form" data-type="${type}">
                        <div class="form-group">
                             <input type="text" name="name" class="form-input" placeholder="새 태그 이름..." required>
                        </div>
                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">추가</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
  } catch (error) {
    container.innerHTML = `<div class="tag-list-container"><p class="empty-list-message">오류: ${error.message}</p></div>`;
  }
}

function createTagElementHtml(tag) {
  return `
        <div class="tag-item" data-tag-id="${tag.tagId}" data-tag-name="${tag.name}" data-tag-type="${tag.type}">
            <div class="tag-item-info">
                <span class="tag-name">${tag.name}</span>
            </div>
            <div class="tag-item-actions">
                <button class="btn-edit">수정</button>
                <button class="btn-delete">삭제</button>
            </div>
        </div>
    `;
}

function handleEditTag(tagItem, contentArea) {
  if (tagItem.querySelector("input.form-input")) return;

  const infoDiv = tagItem.querySelector(".tag-item-info");
  const actionsDiv = tagItem.querySelector(".tag-item-actions");
  const originalName = tagItem.dataset.tagName;
  const type = tagItem.dataset.tagType;

  infoDiv.style.display = "none";
  actionsDiv.style.display = "none";

  const input = document.createElement("input");
  input.type = "text";
  input.value = originalName;
  input.className = "form-input";
  input.style.width = "100%";

  tagItem.prepend(input);
  input.focus();
  input.select();

  const saveEdit = async () => {
    input.removeEventListener("blur", saveEdit);
    input.removeEventListener("keydown", keydownHandler);

    const newName = input.value.trim();
    if (newName && newName !== originalName) {
      const tagId = tagItem.dataset.tagId;
      try {
        const response = await fetch(`/api/admin/tags/${tagId}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name: newName }),
        });
        if (!response.ok) throw new Error("태그 업데이트 실패.");
        await loadTagsForType(type, contentArea);
      } catch (error) {
        alert(`오류: ${error.message}`);
        await loadTagsForType(type, contentArea);
      }
    } else {
      await loadTagsForType(type, contentArea);
    }
  };

  const keydownHandler = async (e) => {
    if (e.key === "Enter") await saveEdit();
    else if (e.key === "Escape") {
      input.removeEventListener("blur", saveEdit);
      input.removeEventListener("keydown", keydownHandler);
      await loadTagsForType(type, contentArea);
    }
  };

  input.addEventListener("blur", saveEdit);
  input.addEventListener("keydown", keydownHandler);
}

// --- User Management Functions ---
let currentSort = "id_desc";

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

  document
    .getElementById("nickname-sort-header")
    .addEventListener("click", () => {
      currentSort = currentSort === "name_asc" ? "name_desc" : "name_asc";
      fetchAndRenderUsers();
    });

  document.getElementById("user-table-body").addEventListener("click", (e) => {
    if (e.target.classList.contains("btn-save")) {
      const userId = e.target.dataset.id;
      const roleSelect = document.getElementById(`role-select-${userId}`);
      const statusSelect = document.getElementById(`status-select-${userId}`);
      updateUser(userId, roleSelect.value, statusSelect.value);
    }
  });
}

function updateSortUI() {
  const ascArrow = document.querySelector(
    "#nickname-sort-header .sort-arrow.asc",
  );
  const descArrow = document.querySelector(
    "#nickname-sort-header .sort-arrow.desc",
  );
  if (!ascArrow || !descArrow) return;
  ascArrow.classList.remove("active");
  descArrow.classList.remove("active");
  if (currentSort === "name_asc") ascArrow.classList.add("active");
  else if (currentSort === "name_desc") descArrow.classList.add("active");
}

async function fetchAndRenderUsers() {
  updateSortUI();
  const role = document.getElementById("role-filter").value;
  const searchTerm = document.getElementById("user-search-input").value;
  const query = new URLSearchParams({ role, searchTerm, sort: currentSort });

  try {
    const response = await fetch(`/api/admin/users?${query.toString()}`);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    const users = await response.json();
    renderUsers(users);
  } catch (error) {
    console.error("Failed to fetch users:", error);
    document.getElementById("user-table-body").innerHTML =
      `<tr><td colspan="10" class="text-center text-red-500">사용자 정보를 불러오는 데 실패했습니다.</td></tr>`;
  }
}

function renderUsers(users) {
  const tableBody = document.getElementById("user-table-body");
  const emptyMessage = document.querySelector(
    "#admin-content-area .empty-list-message",
  );

  if (!tableBody || !emptyMessage) return;

  if (!users || users.length === 0) {
    tableBody.innerHTML = "";
    emptyMessage.style.display = "block";
  } else {
    emptyMessage.style.display = "none";
    tableBody.innerHTML = users
      .map(
        (user) => `
        <tr data-user-id="${user.userId}">
            <td>${user.userId}</td>
            <td><img src="${user.profileImageUrl || `https://i.pravatar.cc/150?u=${user.userId}`}" alt="${user.nickname}" class="user-profile-img" referrerPolicy="no-referrer"></td>
            <td>${user.nickname}</td>
            <td>${user.email}</td>
            <td>${getSocialIcon(user.provider)}</td>
            <td><span class="role-badge role-${user.role.toLowerCase()}">${user.role}</span></td>
            <td><span class="status-badge status-${user.status.toLowerCase()}">${user.status}</span></td>
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
}

async function updateUser(userId, newRole, newStatus) {
  const userRow = document.querySelector(`tr[data-user-id="${userId}"]`);
  if (!userRow) {
    alert("오류: 사용자 정보를 찾을 수 없습니다.");
    return;
  }

  const originalRole = userRow.querySelector('.role-badge').textContent.trim();
  const originalStatus = userRow.querySelector('.status-badge').textContent.trim();

  const updatePromises = [];

  // 역할이 변경되었을 경우 API 호출 준비
  if (newRole !== originalRole) {
    const roleUpdatePromise = fetch(`/api/admin/users/${userId}/role`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ role: newRole }),
    });
    updatePromises.push(roleUpdatePromise);
  }

  // 상태가 변경되었을 경우 API 호출 준비
  if (newStatus !== originalStatus) {
    const statusUpdatePromise = fetch(`/api/admin/users/${userId}/status`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: newStatus }),
    });
    updatePromises.push(statusUpdatePromise);
  }

  // 변경된 내용이 없으면 함수 종료
  if (updatePromises.length === 0) {
    alert("변경된 내용이 없습니다.");
    return;
  }

  try {
    const responses = await Promise.all(updatePromises);
    
    // 모든 응답이 성공적인지 확인
    const allOk = responses.every(res => res.ok);
    
    if (!allOk) {
      // 실패한 응답 중 첫 번째 응답에서 에러 메시지를 파싱 시도
      const failedResponse = responses.find(res => !res.ok);
      let errorData;
      try {
        errorData = await failedResponse.json();
      } catch (e) {
        // 응답이 JSON이 아닐 경우
        errorData = { message: `서버 오류 (상태 코드: ${failedResponse.status})` };
      }
      throw new Error(errorData.message || "정보 변경에 실패했습니다.");
    }

    alert(`사용자 ID ${userId}의 정보가 성공적으로 변경되었습니다.`);
    await fetchAndRenderUsers();

  } catch (error) {
    console.error("Failed to update user:", error);
    alert(`오류: ${error.message}`);
  }
}

function getSocialIcon(provider) {
  if (!provider) return "N/A";
  const p = provider.toLowerCase();
  if (p === "google") {
    return `<img src="/admin/images/google-logo.svg" referrerPolicy="no-referer" alt="Google" class="social-icon">`;
  }
  if (p === "kakao") {
    return `<img src="/admin/images/kakao-logo.svg" referrerPolicy="no-referer" alt="Kakao" class="social-icon">`;
  }
  if (p === "naver") {
    return `<img src="/admin/images/naver-logo.svg" referrerPolicy="no-referer" alt="Naver" class="social-icon">`;
  }
  return `<span class="social-icon social-${p}">${p.charAt(0).toUpperCase()}</span>`;
}

function formatDate(dateString) {
  if (!dateString) return "N/A";
  return new Date(dateString).toLocaleString("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

// --- Community Management Functions ---
async function loadCommunityManagementPanel() {
  const tableBody = document.getElementById('community-table-body');
  if (!tableBody) return;

  try {
    const res = await fetch('/api/admin/community/posts');
    const posts = await res.json();
    
    tableBody.innerHTML = posts.map(post => `
      <tr class="border-b border-gray-800 hover:bg-gray-800/30">
        <td class="py-3 px-2">${post.postId}</td>
        <td class="py-3 px-2 font-medium" style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${post.title}</td>
        <td class="py-3 px-2 text-gray-400 text-sm">${post.authorNickname}</td>
        <td class="py-3 px-2 text-xs text-gray-500">${new Date(post.createdAt).toLocaleDateString()}</td>
        <td class="py-3 px-2 text-indigo-400 font-bold">${post.commentCount}</td>
        <td class="py-3 px-2">
          <span class="post-badge ${post.public ? 'badge-public' : 'badge-hidden'}">
            ${post.public ? '공개' : '숨김'}
          </span>
        </td>
        <td class="py-3 px-2">
          <div style="display: flex; gap: 4px; align-items: center;">
            <button onclick="adminTogglePostVisibility(${post.postId}, ${post.public})" 
              class="btn-admin-action ${post.public ? 'btn-admin-hide' : 'btn-admin-show'}">
              ${post.public ? '숨김' : '공개'}
            </button>
            <button onclick="adminDeletePost(${post.postId})" class="btn-admin-action btn-admin-delete">삭제</button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center">데이터를 불러오지 못했습니다.</td></tr>';
  }
}

// 초기화 시 접근 가능하도록 window 객체에 등록
window.loadCommunityManagementPanel = loadCommunityManagementPanel;

window.adminTogglePostVisibility = async (postId, currentStatus) => {
  try {
    const response = await fetch(`/api/admin/community/posts/${postId}/visibility`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isPublic: !currentStatus })
    });
    
    if (response.ok) {
      window.loadCommunityManagementPanel(); // 전역 함수로 호출
    } else {
      alert('상태 변경에 실패했습니다.');
    }
  } catch (error) {
    console.error('Toggle failed:', error);
  }
};

window.adminDeletePost = async (postId) => {
  if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) return;
  try {
    const response = await fetch(`/api/admin/community/posts/${postId}`, { method: 'DELETE' });
    if (response.ok) {
      window.loadCommunityManagementPanel();
    }
  } catch (error) {
    console.error('Delete failed:', error);
  }
};


// --- System Statistics Functions ---
let adminTrendChart = null;

async function loadSystemStatsPanel() {
  try {
    const res = await fetch('/api/admin/statistics/summary');
    const data = await res.json();
    if (!data) return;

    // 요약 지표
    document.getElementById('stat-total-users').textContent = data.totalUsers.toLocaleString();
    document.getElementById('stat-today-users').textContent = '+' + data.todayNewUsers.toLocaleString();
    document.getElementById('stat-total-recs').textContent = data.totalRecommendations.toLocaleString();
    document.getElementById('stat-today-recs').textContent = '+' + data.todayRecommendations.toLocaleString();
    document.getElementById('stat-active-errors').textContent = data.activeErrors.toLocaleString();

    // 인기 태그
    const tagList = document.getElementById('popular-tags-list');
    if (tagList) {
      tagList.innerHTML = data.popularTags.map((tag, index) => `
        <div class="tag-rank-item">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <span style="color: #6366f1; font-weight: 700; width: 1.2rem;">${index + 1}</span>
            <span class="tag-rank-name">${tag.tagName}</span>
            <span style="font-size: 0.65rem; padding: 0.1rem 0.4rem; background: #374151; color: #9ca3af; border-radius: 0.25rem;">${tag.tagType}</span>
          </div>
          <span class="tag-rank-count">${tag.usageCount.toLocaleString()}회</span>
        </div>
      `).join('');
    }

    // 차트
    renderAdminTrendChart(data.dailyTrends);
  } catch (err) {
    console.error('Stats load failed:', err);
  }
}

function renderAdminTrendChart(trends) {
  const canvas = document.getElementById('daily-trend-chart');
  if (!canvas) return;
  
  const ctx = canvas.getContext('2d');
  if (adminTrendChart) adminTrendChart.destroy();

  // Chart.js가 전역에 로드되었는지 확인 후 실행
  if (typeof Chart === 'undefined') {
    console.error('Chart.js not loaded');
    return;
  }

  adminTrendChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: trends.map(t => t.date.substring(5)),
      datasets: [
        {
          label: '신규 가입',
          data: trends.map(t => t.user_count),
          borderColor: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          fill: true,
          tension: 0.4
        },
        {
          label: '코디 추천',
          data: trends.map(t => t.rec_count),
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
          tension: 0.4
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { labels: { color: '#9ca3af' } } },
      scales: {
        y: { grid: { color: 'rgba(75, 85, 99, 0.2)' }, ticks: { color: '#9ca3af' } },
        x: { grid: { display: false }, ticks: { color: '#9ca3af' } }
      }
    }
  });
}
