/*변수 선언*/
const middle = document.getElementById("middleSidebar");
const closeToggle = document.getElementById("closeToggle");
const resizer = document.getElementById("resizer");
const left = document.getElementById("leftSidebar");

let isResizing = false;
let isFloatingLocked = false;

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
	const insideDots = e.target.closest(".menu-dots");
	if (!insideActions && !insideDots && window.currentlyOpenAction) {
		window.currentlyOpenAction.style.display = "none";
		window.currentlyOpenAction = null;
	}
});


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

	// 리사이징중 마우스 하이라이트 방지
	document.addEventListener('mousedown', () => {
		document.body.style.userSelect = 'none';
	});
	document.addEventListener('mouseup', () => {
		document.body.style.userSelect = 'auto';
	});
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

// --- 사이드바 숨기기 버튼 ---  
closeToggle.addEventListener("click", () => {
	middle.classList.add("hidden");
	middle.classList.remove("floating");
});


// --- 프로젝트 추가 입력창 띄우기 ---
function showAddInput() {
	const list = document.getElementById("project-list");
	const existingInput = list.querySelector('input[type="text"]');
	if (existingInput) {
		existingInput.focus();
		return;
	}

	const input = document.createElement("input");
	input.type = "text";
	input.placeholder = "새 프로젝트명 입력";
	input.className = "form-control mb-2";
	list.prepend(input);
	input.focus();
}

