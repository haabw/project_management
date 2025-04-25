// --- 점 3개 메뉴 토글 (공통) ---
window.currentlyOpenAction = null;

function toggleActions(el) {
  const menu = el.querySelector(".project-actions");
  if (window.currentlyOpenAction && window.currentlyOpenAction !== menu) {
    window.currentlyOpenAction.style.display = "none";
  }
  const isOpen = menu.style.display === "block";
  menu.style.display = isOpen ? "none" : "block";
  window.currentlyOpenAction = isOpen ? null : menu;
}

document.addEventListener("click", function(e) {
  const insideActions = e.target.closest(".project-actions");
  const insideDots    = e.target.closest(".menu-dots");
  if (!insideActions && !insideDots && window.currentlyOpenAction) {
    window.currentlyOpenAction.style.display = "none";
    window.currentlyOpenAction = null;
  }
});

// --- '프로젝트 추가' 입력창 띄우기 (옵션) ---
function showAddInput() {
  const list = document.getElementById("project-list");
  const input = document.createElement("input");
  input.type = "text";
  input.placeholder = "새 프로젝트명 입력";
  input.className = "form-control mb-2";
  list.prepend(input);
  input.focus();
}
