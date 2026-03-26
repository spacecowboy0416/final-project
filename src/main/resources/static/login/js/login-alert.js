document.addEventListener("DOMContentLoaded", function () {
  const urlParams = new URLSearchParams(window.location.search);

  // 로그인 성공 알림
  if (urlParams.get("login") === "success") {
    const isNew = urlParams.get("isNew") === "true";
    const title = "로그인 성공";
    const msg = isNew ? "CORAI의 회원이 되신 것을 환영합니다! 🎉" : "다시 오신 것을 환영합니다! 😊";

    // common.js의 공용 모달 호출
    showGlobalModal(title, msg, "alert", function () {
      // 주소창에서 파라미터 제거 (깔끔한 URL 유지)
      const cleanUrl = window.location.pathname;
      window.history.replaceState({}, document.title, cleanUrl);
    });
  }

  // 중복 로그인 메시지 알림
  else if (urlParams.get("error") === "U103") {
    showGlobalModal("접속 종료", "다른 기기에서 로그인하여 접속이 종료되었습니다.", "alert", function () {
      window.location.href = "/";
    });
  }

  // 이용 정지 유저(SUSPENDED) 감지 시 알림
  else if (urlParams.get("error") === "U102") {
    showGlobalModal("계정 정지", "이용 정지된 계정입니다.", "alert", function () {
      window.location.href = "/";
    });
  }
});
