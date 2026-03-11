// 1. 코디 룩북 모달 제어
let currentCoordiId = null;

function openCoordiModal(element) {
    currentCoordiId = element.getAttribute('data-id');
    document.getElementById('modalTitle').innerText = element.getAttribute('data-title');
    document.getElementById('modalDesc').innerText = element.getAttribute('data-desc');
    document.getElementById('modalDate').innerText = element.getAttribute('data-date');
    
    document.getElementById('deleteCoordiForm').action = '/api/closet/recommendations/' + currentCoordiId;
    document.getElementById('coordiModal').style.display = 'flex';
}

function closeCoordiModal() {
    document.getElementById('coordiModal').style.display = 'none';
}

function editCoordi() {
    if(currentCoordiId) {
        window.location.href = '/recommend?editId=' + currentCoordiId;
    }
}

function shareCoordi() {
    if(currentCoordiId) {
        window.location.href = '/board/write?coordiId=' + currentCoordiId;
    }
}

// 2. 옷 등록 모달 제어
function openAddItemModal() {
    document.getElementById('addItemModal').style.display = 'flex';
}

function closeAddItemModal() {
    document.getElementById('addItemModal').style.display = 'none';
}

// 모달 바깥 어두운 배경 클릭 시 닫히도록 처리
window.onclick = function(event) {
    const coordiModal = document.getElementById('coordiModal');
    const addItemModal = document.getElementById('addItemModal');
    
    if (event.target == coordiModal) {
        closeCoordiModal();
    }
    if (event.target == addItemModal) {
        closeAddItemModal();
    }
}