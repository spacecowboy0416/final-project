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

    // 주소창에서 파라미터 제거 (깔끔한 URL 유지)
    const cleanUrl = window.location.pathname;
    window.history.replaceState({}, document.title, cleanUrl);
  }

  // 중복 로그인 메시지 알림
  else if (urlParams.get("error") === "duplicate_login") {
    alert("다른 기기에서 로그인하여 접속이 종료되었습니다.");
    window.location.href = "/"; // 알림 후 파라미터 깔끔하게 제거
  }

  // 이용 정지 유저(SUSPENDED) 감지 시
  else if (urlParams.get("error") === "suspended_user") {
    alert("이용 정지된 계정입니다. 고객센터에 문의해 주세요.");
    window.location.href = "/";
  }
});
