document.addEventListener("DOMContentLoaded", function () {
  const urlParams = new URLSearchParams(window.location.search);

  // 로그인 성공 알림
  if (urlParams.get("login") === "success") {
    const isNew = urlParams.get("isNew") === "true";

    if (isNew) {
      alert("COORDI의 회원이 되신 것을 환영합니다! 🎉");
    } else {
      alert("다시 오신 것을 환영합니다! 😊");
    }

    // 주소창에서 파라미터 제거 (뒤로가기 시 알림 재발생 방지 및 깔끔한 URL 유지)
    const cleanUrl = window.location.pathname;
    window.history.replaceState({}, document.title, cleanUrl);
  }
});
