// --- 왼쪽 사이드바 토글 (열기 버튼) ---
const leftSidebar = document.getElementById("leftSidebar");
const openToggle  = document.getElementById("openToggle");
const middleSidebar = document.getElementById("middleSidebar");

leftSidebar.addEventListener("mouseenter", () => {
  if (middleSidebar.classList.contains("hidden")) {
    openToggle.style.display = "flex";
  }
});
leftSidebar.addEventListener("mouseleave", () => {
  if (middleSidebar.classList.contains("hidden")) {
    openToggle.style.display = "none";
  }
});

// 사이드바 열기 버튼
openToggle.addEventListener("click", () => {
  middleSidebar.classList.remove("hidden", "floating");
  openToggle.style.display = "none";
});
