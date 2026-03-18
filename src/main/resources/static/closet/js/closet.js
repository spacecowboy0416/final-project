// 전역 변수 설정
let currentCoordiId = null;

// 코디 상세 모달 관련 로직
function openCoordiModal(element) {
    currentCoordiId = element.getAttribute('data-id');
    document.getElementById('modalTitle').innerText = element.getAttribute('data-title');
    document.getElementById('modalDesc').innerText = element.getAttribute('data-desc');
    document.getElementById('modalDate').innerText = element.getAttribute('data-date');
    document.getElementById('deleteCoordiForm').action = '/closet/delete-set/' + currentCoordiId;
    document.getElementById('coordiModal').style.display = 'flex';
}

function closeCoordiModal() {
    document.getElementById('coordiModal').style.display = 'none';
}

function editCoordi() {
    if(currentCoordiId) window.location.href = '/recommend?editId=' + currentCoordiId;
}

function shareCoordi() {
    if(currentCoordiId) window.location.href = '/board/write?coordiId=' + currentCoordiId;
}

// 개별 옷 등록 모달 제어 로직
function openAddItemModal() {
    document.getElementById('addItemModal').style.display = 'flex';
}

function closeAddItemModal() {
    document.getElementById('addItemModal').style.display = 'none';
    document.getElementById('addItemForm').reset();
    document.getElementById('imagePreviewContainer').innerHTML = '';
    selectedFiles = [];
    updateFileInput();
    resetPillGroup('categoryPillGroup', 'selectedCategoryId');
    resetPillGroup('seasonPillGroup', 'selectedSeason');
}

// 세트 등록 모달 제어 로직
function openAddSetModal() {
    document.getElementById('addSetModal').style.display = 'flex';
}

function closeAddSetModal() {
    document.getElementById('addSetModal').style.display = 'none';
    document.getElementById('addSetForm').reset();
    document.getElementById('setPreviewContainer').innerHTML = '';
    setFiles = [];
    updateSetFileInput();
    resetPillGroup('setSeasonPillGroup', 'setSelectedSeason');
}

// 모달 외부 클릭 시 닫기 처리
window.onclick = function(event) {
    if (event.target == document.getElementById('coordiModal')) closeCoordiModal();
    if (event.target == document.getElementById('addItemModal')) closeAddItemModal();
    if (event.target == document.getElementById('addSetModal')) closeAddSetModal();
}

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
    if(pills.length > 0) {
        pills.forEach(p => p.classList.remove('active'));
        pills[0].classList.add('active');
        hiddenInput.value = pills[0].getAttribute(pills[0].hasAttribute('data-id') ? 'data-id' : 'data-val');
    }
}

// 개별 옷 등록 이미지 미리보기 및 데이터 관리
let selectedFiles = [];

document.getElementById('imageInput').addEventListener('change', function(e) {
    const container = document.getElementById('imagePreviewContainer');
    const files = Array.from(e.target.files);
    
    if(selectedFiles.length + files.length > 10) {
        alert('최대 10장까지만 업로드 가능합니다.');
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

function updateFileInput() {
    const dataTransfer = new DataTransfer();
    selectedFiles.forEach(file => dataTransfer.items.add(file));
    document.getElementById('imageInput').files = dataTransfer.files;
}

// 코디 세트 등록 이미지 미리보기 및 동적 입력 폼 관리
let setFiles = [];

document.getElementById('setImageInput').addEventListener('change', function(e) {
    const container = document.getElementById('setPreviewContainer');
    const files = Array.from(e.target.files);

    files.forEach(file => {
        setFiles.push(file);
        const reader = new FileReader();
        reader.onload = function(event) {
            const div = document.createElement('div');
            div.className = 'set-dynamic-item';
            
            // 세트 구성품 상세 정보를 입력할 수 있는 2단 폼 HTML 주입
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

            // 삭제 버튼 이벤트 바인딩
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

function updateSetFileInput() {
    const dataTransfer = new DataTransfer();
    setFiles.forEach(file => dataTransfer.items.add(file));
    document.getElementById('setImageInput').files = dataTransfer.files;
}