// 전역 식별자 상태 관리 변수
let currentSetId = null;
let currentItemId = null;

// 개별 아이템 상세 정보 및 수정 모달 제어 로직
function openItemDetailModal(element) {
    const isElement = element instanceof HTMLElement;
    currentItemId = isElement ? element.getAttribute('data-id') : element.itemId;
    
    document.getElementById('itemEditId').value = currentItemId;
    document.getElementById('itemDetailImg').src = isElement ? element.getAttribute('data-img') : element.img;
    document.getElementById('itemEditName').value = isElement ? element.getAttribute('data-name') : element.name;
    document.getElementById('itemEditBrand').value = (isElement ? element.getAttribute('data-brand') : element.brand) || '';
    document.getElementById('itemEditColor').value = (isElement ? element.getAttribute('data-color') : element.color) || '';
    document.getElementById('itemEditSeason').value = isElement ? element.getAttribute('data-season') : element.season;

    document.getElementById('itemEditImageInput').value = "";
    document.getElementById('itemDetailModal').style.display = 'flex';
}

function closeItemDetailModal() {
    document.getElementById('itemDetailModal').style.display = 'none';
}

// 개별 아이템 모달 내 이미지 실시간 미리보기 기능
function previewEditImage(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            document.getElementById('itemDetailImg').src = e.target.result;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

// 개별 아이템 삭제 확인 및 폼 전송 제어 로직
function handleItemDelete() {
    showGlobalModal('아이템 삭제', '이 옷을 옷장에서 삭제하시겠습니까?', 'danger', () => {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/closet/delete/' + currentItemId;
        document.body.appendChild(form);
        form.submit();
    });
}

// 코디 세트 상세 정보 모달 제어 및 내부 아이템 수정 연결 로직
function openSetDetailModal(element) {
    currentSetId = element.getAttribute('data-id');
    const title = element.getAttribute('data-title');
    
    document.getElementById('setEditId').value = currentSetId;
    document.getElementById('setEditTitle').value = title;
    
    const container = document.getElementById('setItemsPreview');
    container.innerHTML = "";
    
    const items = element.querySelectorAll('.set-item-row');
    items.forEach(item => {
        const itemId = item.getAttribute('data-item-id');
        const name = item.getAttribute('data-name');
        const img = item.getAttribute('data-img');
        
        const row = document.createElement('div');
        row.className = 'set-item-row';
        row.style.justifyContent = 'space-between';
        row.style.marginBottom = '10px';
        row.innerHTML = `
            <div style="display:flex; align-items:center; gap:10px;">
                <img src="${img}" class="set-item-img">
                <span class="set-item-name">${name}</span>
            </div>
            <button type="button" class="btn-profile-edit" style="padding:4px 8px; font-size:0.75rem;">사진/정보 수정</button>
        `;
        
        row.querySelector('button').onclick = () => {
            const targetInCloset = document.querySelector(`.item-card[data-id="${itemId}"]`);
            if (targetInCloset) {
                openItemDetailModal(targetInCloset);
            } else {
                openItemDetailModal({ itemId, name, img, brand: '', color: '', season: 'SPRING' });
            }
        };
        
        container.appendChild(row);
    });

    document.getElementById('setDetailModal').style.display = 'flex';
}

function closeSetDetailModal() {
    document.getElementById('setDetailModal').style.display = 'none';
}

// 코디 세트 삭제 및 종속 아이템 동시 파기 안내 제어 로직
function handleSetDelete() {
    const title = document.getElementById('setEditTitle').value;
    closeSetDetailModal();
    setTimeout(() => {
        showGlobalModal(
            '세트 삭제 안내', 
            `[${title}] 세트를 삭제하시겠습니까?\n\n주의: 세트 등록 시 함께 생성된 개별 아이템들도 모두 삭제되어 복구할 수 없습니다.`, 
            'danger', 
            () => {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/closet/delete-set/' + currentSetId;
                document.body.appendChild(form);
                form.submit();
            }
        );
    }, 150);
}

// AI 코디 상세 정보 조회 모달 제어 로직
function openCoordiModal(element) {
    currentSetId = element.getAttribute('data-id');
    document.getElementById('modalTitle').innerText = element.getAttribute('data-title');
    document.getElementById('modalDesc').innerText = element.getAttribute('data-desc');
    document.getElementById('modalDate').innerText = element.getAttribute('data-date');
    
    document.getElementById('coordiModal').style.display = 'flex';
}

function closeCoordiModal() {
    document.getElementById('coordiModal').style.display = 'none';
}

// AI 코디 삭제 확인 및 제어 로직
function handleCoordiDelete() {
    showGlobalModal('코디 삭제', '저장된 AI 추천 코디를 삭제하시겠습니까?', 'danger', () => {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/closet/delete-set/' + currentSetId;
        document.body.appendChild(form);
        form.submit();
    });
}

// AI 코디 편집 및 공유 페이지 이동 기능
function editCoordi() {
    if(currentSetId) window.location.href = '/recommend?editId=' + currentSetId;
}

function shareCoordi() {
    if(currentSetId) window.location.href = '/board/write?coordiId=' + currentSetId;
}

// 개인정보 프로필 수정 및 탈퇴 제어 로직
function openProfileEditModal() {
    document.getElementById('profileEditModal').style.display = 'flex';
}

function closeProfileEditModal() {
    document.getElementById('profileEditModal').style.display = 'none';
}

function previewProfileImage(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            const preview = document.getElementById('profilePreview');
            const placeholder = document.getElementById('profilePlaceholder');
            if (preview) { preview.src = e.target.result; preview.style.display = 'block'; }
            if (placeholder) placeholder.style.display = 'none';
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function confirmWithdraw() {
    closeProfileEditModal();
    setTimeout(() => {
        showGlobalModal('회원 탈퇴', '정말로 탈퇴하시겠습니까? 탈퇴 시 모든 데이터가 파기됩니다.', 'danger', () => {
            document.getElementById('withdrawForm').submit();
        });
    }, 100); 
}

// 개별 옷 신규 등록 모달 제어 및 폼 초기화 로직
function openAddItemModal() {
    document.getElementById('addItemModal').style.display = 'flex';
}

function closeAddItemModal() {
    document.getElementById('addItemModal').style.display = 'none';
    document.getElementById('addItemForm').reset();
    document.getElementById('imagePreviewContainer').innerHTML = '';
    selectedFiles = [];
}

// 코디 세트 신규 등록 모달 제어 및 폼 초기화 로직
function openAddSetModal() {
    document.getElementById('addSetModal').style.display = 'flex';
}

function closeAddSetModal() {
    document.getElementById('addSetModal').style.display = 'none';
    document.getElementById('addSetForm').reset();
    document.getElementById('setPreviewContainer').innerHTML = '';
    setFiles = [];
}

// 모달 외부 영역 클릭 시 자동 닫기 통합 제어 로직
window.addEventListener('click', (e) => {
    const ids = ['coordiModal', 'setDetailModal', 'itemDetailModal', 'addItemModal', 'addSetModal', 'profileEditModal'];
    ids.forEach(id => {
        const modal = document.getElementById(id);
        if (e.target === modal) modal.style.display = 'none';
    });
});

// 페이지 로드 시 타원형 버튼 그룹 이벤트 초기 바인딩 로직
document.addEventListener('DOMContentLoaded', () => {
    setupPillGroup('categoryPillGroup', 'selectedCategoryId', 'data-id');
    setupPillGroup('seasonPillGroup', 'selectedSeason', 'data-val');
    setupPillGroup('setSeasonPillGroup', 'setSelectedSeason', 'data-val');
});

// 타원형 선택 버튼 그룹 활성화 제어 기능
function setupPillGroup(groupId, hiddenInputId, dataAttributeName) {
    const group = document.getElementById(groupId);
    if(!group) return;
    const pills = group.querySelectorAll('.closet-pill');
    const hiddenInput = document.getElementById(hiddenInputId);
    pills.forEach(pill => {
        pill.addEventListener('click', function() {
            pills.forEach(p => p.classList.remove('active'));
            this.classList.add('active');
            hiddenInput.value = this.getAttribute(dataAttributeName);
        });
    });
}

// 개별 아이템 다중 이미지 첨부 및 업로드 관리 기능
let selectedFiles = [];
const imageInput = document.getElementById('imageInput');
if (imageInput) {
    imageInput.addEventListener('change', function(e) {
        const container = document.getElementById('imagePreviewContainer');
        const files = Array.from(e.target.files);
        if(selectedFiles.length + files.length > 10) {
            showGlobalModal('알림', '최대 10장까지만 업로드 가능합니다.', 'alert');
            return;
        }
        files.forEach(file => {
            selectedFiles.push(file);
            const reader = new FileReader();
            reader.onload = event => {
                const div = document.createElement('div');
                div.className = 'image-preview-item';
                div.innerHTML = `<img src="${event.target.result}" class="image-preview-img"><button type="button" class="image-remove-btn">&times;</button>`;
                div.querySelector('.image-remove-btn').onclick = () => {
                    div.remove();
                    const index = selectedFiles.indexOf(file);
                    if (index > -1) { selectedFiles.splice(index, 1); updateFileInput(); }
                };
                container.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
        updateFileInput();
    });
}

function updateFileInput() {
    const input = document.getElementById('imageInput');
    const dataTransfer = new DataTransfer();
    selectedFiles.forEach(file => dataTransfer.items.add(file));
    input.files = dataTransfer.files;
}

// 코디 세트 동적 아이템 입력 폼 및 이미지 데이터 관리 기능
let setFiles = [];
const setImageInput = document.getElementById('setImageInput');
if (setImageInput) {
    setImageInput.addEventListener('change', function(e) {
        const container = document.getElementById('setPreviewContainer');
        const files = Array.from(e.target.files);
        files.forEach(file => {
            setFiles.push(file);
            const reader = new FileReader();
            reader.onload = event => {
                const div = document.createElement('div');
                div.className = 'set-dynamic-item';
                div.innerHTML = `
                    <img src="${event.target.result}" class="set-dynamic-preview">
                    <div class="set-dynamic-inputs">
                        <input type="text" name="setItemNames" class="set-dynamic-input" placeholder="상품명 (필수)" required>
                        <select name="setCategoryIds" class="set-dynamic-select">
                            <option value="1">상의</option><option value="2">하의</option><option value="3">아우터</option>
                            <option value="4">신발</option><option value="5">가방</option><option value="6">모자</option><option value="7">기타</option>
                        </select>
                        <div class="form-row-2" style="margin-top: 5px;">
                            <input type="text" name="setBrands" class="set-dynamic-input" placeholder="브랜드">
                            <input type="text" name="setColors" class="set-dynamic-input" placeholder="색상">
                        </div>
                    </div>
                    <button type="button" class="image-remove-btn">&times;</button>
                `;
                div.querySelector('.image-remove-btn').onclick = () => {
                    div.remove();
                    const index = setFiles.indexOf(file);
                    if (index > -1) { setFiles.splice(index, 1); updateSetFileInput(); }
                };
                container.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
        updateSetFileInput();
    });
}

function updateSetFileInput() {
    const input = document.getElementById('setImageInput');
    const dataTransfer = new DataTransfer();
    setFiles.forEach(file => dataTransfer.items.add(file));
    input.files = dataTransfer.files;
}