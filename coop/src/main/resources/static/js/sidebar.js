/*변수 선언*/
const middle   = document.getElementById("middleSidebar");
const closeToggle = document.getElementById("closeToggle");
const resizer  = document.getElementById("resizer");
const left     = document.getElementById("leftSidebar");

let isResizing       = false;
let isFloatingLocked = false;

// --- 리사이징 시작 ---
resizer.addEventListener("mousedown", () => {
  isResizing = true;
  isFloatingLocked = true;
  document.body.style.cursor = "ew-resize";
});
document.addEventListener("mouseup", () => {
  isResizing = false;
  isFloatingLocked = false;
  document.body.style.cursor = "";
});
document.addEventListener("mousemove", (e) => {
  if (isResizing) {
    const newWidth = e.clientX - left.offsetWidth;
    middle.style.width = `${Math.max(180, newWidth)}px`;
    return;
  }
  // 플로팅 자동 열기/닫기
  const nearLeft = e.clientX >= left.offsetWidth + 6 && e.clientX <= left.offsetWidth + 40;
  const hoverLeft = left.matches(":hover");

  if (middle.classList.contains("hidden") && nearLeft && !hoverLeft) {
    middle.classList.add("floating");
  } else if (
    middle.classList.contains("floating") &&
    (hoverLeft || !middle.matches(":hover"))
  ) {
    setTimeout(() => {
      if (hoverLeft || (!left.matches(":hover") && !middle.matches(":hover"))) {
        middle.classList.remove("floating");
      }
    }, 300);
  }
});

// --- 중간 사이드바 토글 버튼 보이기/숨기기 ---
middle.addEventListener("mouseenter", () => {
  if (!middle.classList.contains("hidden")) {
    closeToggle.style.visibility = "visible";
  }
});
middle.addEventListener("mouseleave", () => {
  closeToggle.style.visibility = "hidden";
});

// --- 닫기/열기 버튼 ---  
closeToggle.addEventListener("click", () => {
  middle.classList.add("hidden");
  middle.classList.remove("floating");
});
