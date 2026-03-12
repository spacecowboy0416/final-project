const canvas = document.getElementById("fxCanvas");
const ctx = canvas ? canvas.getContext("2d") : null;

let width = 0;
let height = 0;
let animationId = null;
let fxMode = "none";
let particles = [];
let thunderNext = 0;

function resizeCanvas() {
  if (!canvas) return;
  width = canvas.width = window.innerWidth;
  height = canvas.height = window.innerHeight;
}

function stopFx() {
  fxMode = "none";
  particles = [];
  thunderNext = 0;

  if (animationId) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }

  const sunRays = document.getElementById("sunRays");
  const sparkle = document.getElementById("sparkle");

  if (sunRays) sunRays.style.opacity = 0;
  if (sparkle) sparkle.style.opacity = 0;

  if (ctx) ctx.clearRect(0, 0, width, height);
}

function startSunFx() {
  const sunRays = document.getElementById("sunRays");
  const sparkle = document.getElementById("sparkle");

  if (sunRays) sunRays.style.opacity = 1;
  if (sparkle) sparkle.style.opacity = 1;

  particles = Array.from({ length: 45 }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    r: 0.8 + Math.random() * 1.4,
    vy: -0.15 - Math.random() * 0.2,
    a: 0.05 + Math.random() * 0.06
  }));
}

function startRainFx() {
  const count = Math.min(700, Math.floor(width * 0.62));

  particles = Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    vx: -2 - Math.random() * 1.8,
    vy: 10 + Math.random() * 8,
    len: 10 + Math.random() * 14,
    a: 0.16 + Math.random() * 0.16
  }));
}

function startThunderFx() {
  particles = [];
  thunderNext = performance.now() + 1200 + Math.random() * 1800;
}

function startThunderRainFx() {
  startRainFx();
  thunderNext = performance.now() + 1200 + Math.random() * 1800;
}

function startSnowFx() {
  const count = Math.min(320, Math.floor(width * 0.26));

  particles = Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    r: 0.8 + Math.random() * 1.8,
    vx: -0.4 + Math.random() * 0.8,
    vy: 0.7 + Math.random() * 1.3,
    a: 0.18 + Math.random() * 0.22
  }));
}

function startSleetFx() {
  const count = Math.min(300, Math.floor(width * 0.24));

  particles = Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    vx: -1.2 + Math.random() * 0.8,
    vy: 4.5 + Math.random() * 3.5,
    r: 0.8 + Math.random() * 1.0,
    len: 4 + Math.random() * 5,
    mix: Math.random() > 0.5,
    a: 0.14 + Math.random() * 0.14
  }));
}

function startHailFx() {
  const count = Math.min(130, Math.floor(width * 0.11));

  particles = Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,
    vx: -0.7 + Math.random() * 0.5,
    vy: 7 + Math.random() * 4,
    r: 1.8 + Math.random() * 2,
    a: 0.2 + Math.random() * 0.18
  }));
}

function startWindFx() {

  const count = Math.min(60, Math.floor(width * 0.05));

  particles = Array.from({ length: count }, () => ({
    x: Math.random() * width,
    y: Math.random() * height,

    vx: 9 + Math.random() * 7,   // 속도 증가
    vy: -0.2 + Math.random() * 0.4,

    len: 40 + Math.random() * 45, // 길이 증가

    a: 0.08 + Math.random() * 0.08
  }));
}

function startFx(newMode) {
  stopFx();
  fxMode = newMode;

  if (!ctx) return;

  switch (fxMode) {
    case "sun":
      startSunFx();
      break;
    case "rain":
      startRainFx();
      break;
    case "thunder":
      startThunderFx();
      break;
    case "thunder_rain":
      startThunderRainFx();
      break;
    case "snow":
      startSnowFx();
      break;
    case "sleet":
      startSleetFx();
      break;
    case "hail":
      startHailFx();
      break;
    case "wind":
      startWindFx();
      break;
    default:
      return;
  }

  renderFx();
}

function renderRain() {
  ctx.lineWidth = 1;
  ctx.beginPath();

  for (const p of particles) {
    ctx.moveTo(p.x, p.y);
    ctx.lineTo(p.x + p.vx, p.y + p.len);

    p.x += p.vx;
    p.y += p.vy;

    if (p.y > height || p.x < -50) {
      p.x = Math.random() * width + 50;
      p.y = -Math.random() * height * 0.15;
    }
  }

  ctx.strokeStyle = "rgba(255,255,255,0.26)";
  ctx.stroke();
}

function renderThunder() {
  const now = performance.now();

  if (now > thunderNext) {
    ctx.fillStyle = "rgba(255,255,255,0.24)";
    ctx.fillRect(0, 0, width, height);
    thunderNext = now + 1500 + Math.random() * 2400;
  }
}

function renderSnow() {
  for (const p of particles) {
    ctx.beginPath();
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255,255,255,${p.a})`;
    ctx.fill();

    p.x += p.vx;
    p.y += p.vy;

    if (p.y > height + 10) {
      p.y = -10;
      p.x = Math.random() * width;
    }
    if (p.x < -10) p.x = width + 10;
    if (p.x > width + 10) p.x = -10;
  }
}

function renderSleet() {
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.fillStyle = "rgba(255,255,255,0.24)";
  ctx.strokeStyle = "rgba(255,255,255,0.24)";

  for (const p of particles) {
    if (p.mix) {
      ctx.moveTo(p.x, p.y);
      ctx.lineTo(p.x + p.vx, p.y + p.len);
    } else {
      ctx.fillRect(p.x, p.y, p.r, p.r);
    }

    p.x += p.vx;
    p.y += p.vy;

    if (p.y > height + 10 || p.x < -20 || p.x > width + 20) {
      p.x = Math.random() * width;
      p.y = -10;
    }
  }

  ctx.stroke();
}

function renderHail() {
  for (const p of particles) {
    ctx.beginPath();
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255,255,255,${p.a})`;
    ctx.fill();

    p.x += p.vx;
    p.y += p.vy;

    if (p.y > height + 12) {
      p.y = -12;
      p.x = Math.random() * width;
    }
  }
}

function renderWind() {

  ctx.lineWidth = 1.6;
  ctx.beginPath();
  ctx.strokeStyle = "rgba(255,255,255,0.18)";

  for (const p of particles) {
    ctx.moveTo(p.x, p.y);
	ctx.quadraticCurveTo(
	  p.x + p.len * 0.5,
	  p.y - 6,
	  p.x + p.len,
	  p.y
	);

    p.x += p.vx;
    p.y += p.vy;

    if (p.x > width + 60) {
      p.x = -60;
      p.y = Math.random() * height;
    }
  }

  ctx.stroke();
}

function renderSun() {
  for (const p of particles) {
    ctx.beginPath();
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255,255,255,${p.a})`;
    ctx.fill();

    p.y += p.vy;
    if (p.y < -10) {
      p.y = height + 10;
      p.x = Math.random() * width;
    }
  }
}

function renderFx() {
  if (!ctx) return;

  ctx.clearRect(0, 0, width, height);

  switch (fxMode) {
    case "rain":
      renderRain();
      break;
    case "thunder":
      renderThunder();
      break;
    case "thunder_rain":
      renderRain();
      renderThunder();
      break;
    case "snow":
      renderSnow();
      break;
    case "sleet":
      renderSleet();
      break;
    case "hail":
      renderHail();
      break;
    case "wind":
      renderWind();
      break;
    case "sun":
      renderSun();
      break;
    default:
      return;
  }

  animationId = requestAnimationFrame(renderFx);
}

function chooseFxMode(weatherStatus) {
  switch (weatherStatus) {
    case "CLEAR":
      return "sun";

    case "RAIN":
    case "CLOUDY_RAIN":
      return "rain";

    case "THUNDERSTORM":
      return "thunder";

    case "THUNDERSTORM_RAIN":
      return "thunder_rain";

    case "SNOW":
    case "CLOUDY_SNOW":
      return "snow";

    case "SLEET":
      return "sleet";

    case "HAIL":
      return "hail";

    case "WINDY":
      return "wind";

    default:
      return "none";
  }
}