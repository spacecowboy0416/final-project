async function fetchMainSummary(lat, lon, isDefault) {
  const response = await fetch(
    `/api/main/summary?lat=${lat}&lon=${lon}&isDefault=${isDefault}`
  );

  if (!response.ok) {
    throw new Error("Main summary API request failed");
  }

  return response.json();
}

function getLocationOrDefault() {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      console.warn("geolocation 미지원 -> 기본 위치 사용");
      resolve({
        ...window.CoordiConfig.DEFAULT_LOCATION,
        isDefault: true
      });
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        console.log("현재 위치 성공:", pos.coords.latitude, pos.coords.longitude);
        resolve({
          lat: pos.coords.latitude,
          lon: pos.coords.longitude,
          isDefault: false
        });
      },
      (err) => {
        console.error("현재 위치 실패:", err);
        resolve({
          ...window.CoordiConfig.DEFAULT_LOCATION,
          isDefault: true
        });
      },
      { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
    );
  });
}