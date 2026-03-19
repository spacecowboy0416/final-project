// =========================================
// 공용 모달 로직 (팀원 전체 사용 가능)
// =========================================
// type: 'alert' (확인 버튼만), 'confirm' (취소/확인 버튼), 'danger' (취소/빨간 확인 버튼)
function showGlobalModal(title, message, type = 'alert', confirmCallback = null) {
    const modal = document.getElementById('globalModal');
    if (!modal) return;
    
    document.getElementById('globalModalTitle').innerText = title;
    document.getElementById('globalModalMessage').innerText = message;
    
    const cancelBtn = document.getElementById('globalModalCancelBtn');
    const confirmBtn = document.getElementById('globalModalConfirmBtn');
    
    // 이전 이벤트 리스너 초기화 (복제 후 교체)
    const newConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
    
    if (type === 'alert') {
        cancelBtn.style.display = 'none';
        newConfirmBtn.className = 'btn-global confirm';
        newConfirmBtn.innerText = '확인';
    } else if (type === 'danger') {
        cancelBtn.style.display = 'block';
        newConfirmBtn.className = 'btn-global danger';
        newConfirmBtn.innerText = '삭제';
    } else { // confirm
        cancelBtn.style.display = 'block';
        newConfirmBtn.className = 'btn-global confirm';
        newConfirmBtn.innerText = '확인';
    }

    newConfirmBtn.onclick = function() {
        closeGlobalModal();
        if (confirmCallback) confirmCallback();
    };

    modal.style.display = 'flex';
}

function closeGlobalModal() {
    const modal = document.getElementById('globalModal');
    if (modal) modal.style.display = 'none';
}

// 폼 전송 가로채기 및 공용 모달 연동 (삭제 버튼 등에서 사용)
function confirmAction(event, message) {
    event.preventDefault(); // 기본 폼 전송 막기
    const form = event.target;
    
    showGlobalModal('확인 필요', message, 'danger', function() {
        form.submit(); // 확인 눌렀을 때만 폼 전송 실행함
    });
    return false;
}

// 공용 모달 외부 영역 클릭 시 닫기 (충돌 방지를 위해 addEventListener 사용)
window.addEventListener('click', function(event) {
    const globalModal = document.getElementById('globalModal');
    if (event.target === globalModal) {
        closeGlobalModal();
    }
});