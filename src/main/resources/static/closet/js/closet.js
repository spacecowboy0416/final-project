// 전역 변수 설정
let currentCoordiId = null;

// 회원 탈퇴 확인 모달 띄우기 (정보 수정 모달 내부에 있으므로 먼저 닫아줌)
function confirmWithdraw() {
    closeProfileEditModal(); // 기존 모달 닫기
    setTimeout(function() {
        // 공용 JS에 있는 showGlobalModal 호출
        showGlobalModal('회원 탈퇴', '정말로 탈퇴하시겠습니까? 탈퇴 시 등록한 모든 옷장 데이터와 코디가 삭제되며 복구할 수 없습니다.', 'danger', function() {
            const withdrawForm = document.getElementById('withdrawForm');
            if (withdrawForm) withdrawForm.submit();
        });
    }, 100); 
}

// 개인정보 수정 모달 제어
function openProfileEditModal() {
    const modal = document.getElementById('profileEditModal');
    if (modal) modal.style.display = 'flex';
}

function closeProfileEditModal() {
    const modal = document.getElementById('profileEditModal');
    if (modal) modal.style.display = 'none';
}

// 프로필 사진 선택 시 썸네일 미리보기 로직
function previewProfileImage(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('profilePreview');
            const placeholder = document.getElementById('profilePlaceholder');
            if (preview) {
                preview.src = e.target.result;
                preview.style.display = 'block';
            }
            if (placeholder) {
                placeholder.style.display = 'none';
            }
        }
        reader.readAsDataURL(input.files[0]);
    }
}

// 기존 코디 상세 모달 관련 로직
function openCoordiModal(element) {
    currentCoordiId = element.getAttribute('data-id');
    document.getElementById('modalTitle').innerText = element.getAttribute('data-title');
    document.getElementById('modalDesc').innerText = element.getAttribute('data-desc');
    document.getElementById('modalDate').innerText = element.getAttribute('data-date');
    
    const deleteForm = document.getElementById('deleteCoordiForm');
    if (deleteForm) deleteForm.action = '/closet/delete-set/' + currentCoordiId;
    
    const modal = document.getElementById('coordiModal');
    if (modal) modal.style.display = 'flex';
}

function closeCoordiModal() {
    const modal = document.getElementById('coordiModal');
    if (modal) modal.style.display = 'none';
}

function editCoordi() {
    if(currentCoordiId) window.location.href = '/recommend?editId=' + currentCoordiId;
}

function shareCoordi() {
    if(currentCoordiId) window.location.href = '/board/write?coordiId=' + currentCoordiId;
}

// 개별 옷 등록 모달 제어 로직
function openAddItemModal() {
    const modal = document.getElementById('addItemModal');
    if (modal) modal.style.display = 'flex';
}

function closeAddItemModal() {
    const modal = document.getElementById('addItemModal');
    if (modal) modal.style.display = 'none';
    
    const form = document.getElementById('addItemForm');
    if (form) form.reset();
    
    const container = document.getElementById('imagePreviewContainer');
    if (container) container.innerHTML = '';
    
    selectedFiles = [];
    updateFileInput();
    resetPillGroup('categoryPillGroup', 'selectedCategoryId');
    resetPillGroup('seasonPillGroup', 'selectedSeason');
}

// 세트 등록 모달 제어 로직
function openAddSetModal() {
    const modal = document.getElementById('addSetModal');
    if (modal) modal.style.display = 'flex';
}

function closeAddSetModal() {
    const modal = document.getElementById('addSetModal');
    if (modal) modal.style.display = 'none';
    
    const form = document.getElementById('addSetForm');
    if (form) form.reset();
    
    const container = document.getElementById('setPreviewContainer');
    if (container) container.innerHTML = '';
    
    setFiles = [];
    updateSetFileInput();
    resetPillGroup('setSeasonPillGroup', 'setSelectedSeason');
}

// 모달 외부 클릭 시 닫기 처리 (충돌 방지를 위해 addEventListener 사용)
window.addEventListener('click', function(event) {
    if (event.target == document.getElementById('coordiModal')) closeCoordiModal();
    if (event.target == document.getElementById('addItemModal')) closeAddItemModal();
    if (event.target == document.getElementById('addSetModal')) closeAddSetModal();
    if (event.target == document.getElementById('profileEditModal')) closeProfileEditModal();
});

// 타원형 버튼 그룹 초기화 바인딩
document.addEventListener('DOMContentLoaded', function() {
    setupPillGroup('categoryPillGroup', 'selectedCategoryId', 'data-id');
    setupPillGroup('seasonPillGroup', 'selectedSeason', 'data-val');
    setupPillGroup('setSeasonPillGroup', 'setSelectedSeason', 'data-val');
});

// 타원형 버튼 제어 로직
function setupPillGroup(groupId, hiddenInputId, dataAttributeName) {
    const group = document.getElementById(groupId);
    if(!group) return;
    const pills = group.querySelectorAll('.closet-pill');
    const hiddenInput = document.getElementById(hiddenInputId);
    if (!hiddenInput) return;

    pills.forEach(pill => {
        pill.addEventListener('click', function() {
            pills.forEach(p => p.classList.remove('active'));
            this.classList.add('active');
            hiddenInput.value = this.getAttribute(dataAttributeName);
        });
    });
}

// 타원형 버튼 리셋 로직
function resetPillGroup(groupId, hiddenInputId) {
    const group = document.getElementById(groupId);
    if(!group) return;
    const pills = group.querySelectorAll('.closet-pill');
    const hiddenInput = document.getElementById(hiddenInputId);
    if(pills.length > 0 && hiddenInput) {
        pills.forEach(p => p.classList.remove('active'));
        pills[0].classList.add('active');
        hiddenInput.value = pills[0].getAttribute(pills[0].hasAttribute('data-id') ? 'data-id' : 'data-val');
    }
}

// 개별 옷 등록 이미지 미리보기 및 데이터 관리
let selectedFiles = [];
const imageInput = document.getElementById('imageInput');

if (imageInput) {
    imageInput.addEventListener('change', function(e) {
        const container = document.getElementById('imagePreviewContainer');
        if (!container) return;
        
        const files = Array.from(e.target.files);
        
        if(selectedFiles.length + files.length > 10) {
            // 공용 JS의 알림 모달 호출
            showGlobalModal('알림', '최대 10장까지만 업로드 가능함.', 'alert');
            updateFileInput();
            return;
        }

        files.forEach(file => {
            selectedFiles.push(file);
            const reader = new FileReader();
            reader.onload = function(event) {
                const div = document.createElement('div');
                div.className = 'image-preview-item';
                
                const img = document.createElement('img');
                img.src = event.target.result;
                img.className = 'image-preview-img';
                
                const removeBtn = document.createElement('button');
                removeBtn.className = 'image-remove-btn';
                removeBtn.innerHTML = '&times;';
                removeBtn.type = 'button';
                removeBtn.onclick = function() {
                    div.remove();
                    const index = selectedFiles.indexOf(file);
                    if (index > -1) {
                        selectedFiles.splice(index, 1);
                        updateFileInput(); 
                    }
                };
                
                div.appendChild(img);
                div.appendChild(removeBtn);
                container.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
        updateFileInput();
    });
}

function updateFileInput() {
    const input = document.getElementById('imageInput');
    if (!input) return;
    const dataTransfer = new DataTransfer();
    selectedFiles.forEach(file => dataTransfer.items.add(file));
    input.files = dataTransfer.files;
}

// 코디 세트 등록 이미지 미리보기 및 동적 입력 폼 관리
let setFiles = [];
const setImageInput = document.getElementById('setImageInput');

if (setImageInput) {
    setImageInput.addEventListener('change', function(e) {
        const container = document.getElementById('setPreviewContainer');
        if (!container) return;
        
        const files = Array.from(e.target.files);

        files.forEach(file => {
            setFiles.push(file);
            const reader = new FileReader();
            reader.onload = function(event) {
                const div = document.createElement('div');
                div.className = 'set-dynamic-item';
                
                div.innerHTML = `
                    <img src="${event.target.result}" class="set-dynamic-preview" alt="미리보기">
                    <div class="set-dynamic-inputs">
                        <input type="text" name="setItemNames" class="set-dynamic-input" placeholder="상품명 (필수)" required>
                        <select name="setCategoryIds" class="set-dynamic-select">
                            <option value="1">상의</option>
                            <option value="2">하의</option>
                            <option value="3">아우터</option>
                            <option value="4">신발</option>
                            <option value="5">가방</option>
                            <option value="6">모자</option>
                            <option value="7">기타</option>
                        </select>
                        <div class="form-row-2" style="margin-top: 5px;">
                            <input type="text" name="setBrands" class="set-dynamic-input" placeholder="브랜드">
                            <input type="text" name="setColors" class="set-dynamic-input" placeholder="색상">
                            <input type="text" name="setMaterials" class="set-dynamic-input" placeholder="소재">
                            <input type="text" name="setFits" class="set-dynamic-input" placeholder="핏">
                            <input type="text" name="setStyles" class="set-dynamic-input" placeholder="스타일" style="grid-column: span 2;">
                        </div>
                    </div>
                    <button type="button" class="image-remove-btn">&times;</button>
                `;

                div.querySelector('.image-remove-btn').onclick = function() {
                    div.remove();
                    const index = setFiles.indexOf(file);
                    if (index > -1) {
                        setFiles.splice(index, 1);
                        updateSetFileInput(); 
                    }
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
    if (!input) return;
    const dataTransfer = new DataTransfer();
    setFiles.forEach(file => dataTransfer.items.add(file));
    input.files = dataTransfer.files;
}