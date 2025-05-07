

;(function sidebarModule(){
  'use strict';

  // ─────────────────────────────────────────────
  // 1) 템플릿 및 상수 선언 (한 번만 읽어오기)
  // ─────────────────────────────────────────────
  const template        = document.getElementById('project-item-template');
  const PROJECT_LIST_ID = 'project-list';

    // ─────────────────────────────────────────────
    // 2) 사이드바 DOM 요소 참조 추가
    // ─────────────────────────────────────────────
    // HTML에서 id 속성이 아래와 같아야 합니다.
    const left    = document.getElementById('leftSidebar');
    const middle  = document.getElementById('middleSidebar');
    const resizer = document.getElementById('resizer');

  // 리사이징·플로팅 잠금 플래그 (한 번만 선언)
  let isResizing       = false;
  let isFloatingLocked = false;

  // ─────────────────────────────────────────────
  // 2) “프로젝트 추가” 모달 관련 엘리먼트
  // ─────────────────────────────────────────────
  const addBtn        = document.getElementById('addProjectBtn');
  const nameInput     = document.getElementById('projectNameInput');
  const addModalEl    = document.getElementById('addProjectModal');
  const confirmAddBtn = document.getElementById('confirmAddProjectBtn');
  const addModal      = addModalEl ? new bootstrap.Modal(addModalEl) : null;

  // ─────────────────────────────────────────────
  // 3) 초기화 및 외부 호출 노출
  // ─────────────────────────────────────────────
  document.addEventListener('DOMContentLoaded', initSidebar);
  window.loadSidebarProjects = loadSidebarProjects;

  // ─────────────────────────────────────────────
  // 4) 사이드바 초기화 함수
  // ─────────────────────────────────────────────
  function initSidebar() {
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
		/*// 플로팅 자동 열기/닫기
		const nearLeft = e.clientX >= left.offsetWidth + 6 && e.clientX <= left.offsetWidth + 10;
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
		}*/
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

    // “프로젝트 추가” 모달 바인딩
    bindAddProjectModal();

    // 최초 프로젝트 목록 로드
    loadSidebarProjects();
  }

  // ─────────────────────────────────────────────
  // 5) “프로젝트 추가” 모달 바인딩 함수
  // ─────────────────────────────────────────────
  function bindAddProjectModal() {
    if (!addBtn || !addModal || !confirmAddBtn) return;

    // 2-1) 버튼 클릭 시 모달 열기
    addBtn.addEventListener('click', () => {
      nameInput.value = '';
      nameInput.classList.remove('is-invalid');
      addModal.show();
    });

    // 2-2) “추가” 클릭 시 API 호출 & 사이드바 갱신
    confirmAddBtn.addEventListener('click', () => {
      const name = nameInput.value.trim();
      if (!name) {
        nameInput.classList.add('is-invalid');
        return;
      }
      addModal.hide();
      fetch('/projects/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ projectName: name })
      })
      .then(res => res.json())
      .then(() => {
        console.log('[Sidebar] 새 프로젝트 추가됨');
        loadSidebarProjects();
      })
      .catch(err => console.error('[Sidebar] 프로젝트 추가 실패', err));
    });
  }

  // ─────────────────────────────────────────────
  // 6) 사이드바 프로젝트 목록 다시 불러오기
  // ─────────────────────────────────────────────
  function loadSidebarProjects() {
    console.log('[Sidebar] loadSidebarProjects 호출');
    const projectList = document.getElementById(PROJECT_LIST_ID);
    if (!projectList) {
      console.error(`[Sidebar] #${PROJECT_LIST_ID} 요소를 찾을 수 없습니다.`);
      return;
    }
    projectList.innerHTML = '';

    fetch('/projects/list')
      .then(res => res.json())
      .then(data => {
        console.log('[Sidebar] 불러온 데이터:', data);
        data.forEach(appendProject);
      })
      .catch(err => console.error('[Sidebar] 프로젝트 목록 로드 실패', err));
  }

  // 7) 단일 프로젝트 렌더링
  function appendProject(proj) {
    const projectList = document.getElementById(PROJECT_LIST_ID);
    const clone       = template.content.cloneNode(true);
    const item        = clone.querySelector('.accordion-item');
    const header      = clone.querySelector('.accordion-header');
    const btn         = clone.querySelector('.accordion-button');
    const collapse    = clone.querySelector('.accordion-collapse');

    // 1) 헤더에 고유 ID 붙이기
    const headingId  = `heading-${proj.projectId}`;
    header.id       = headingId;

    // 2) item.dataset, 버튼 텍스트 바인딩
    item.dataset.id = proj.projectId;
    btn.textContent = proj.projectName;

    // 3) 버튼 초기 aria-expanded, collapsed 클래스
    btn.classList.add('collapsed');
    btn.setAttribute('aria-expanded', 'false');
    btn.setAttribute('aria-controls', `collapse-${proj.projectId}`);

    // 4) collapse 패널에 id·aria-labelledby·data-bs-parent 붙이기
    const collapseId = `collapse-${proj.projectId}`;
    collapse.id               = collapseId;
    collapse.setAttribute('aria-labelledby', headingId);
    collapse.setAttribute('data-bs-parent', `#${PROJECT_LIST_ID}`);

    // 5) body 내용 세팅
	collapse.querySelector('.accordion-body').innerHTML = `
	  <div class="page-list">
	    <div class="list-group-item"><a href="/gantt?projectId=${proj.projectId}">Gantt</a></div>
	    <div class="list-group-item"><a href="/mindmap?projectId=${proj.projectId}">Mindmap</a></div>
	    <div class="list-group-item"><a href="/admin?projectId=${proj.projectId}">Admin</a></div>
	    <div class="list-group-item"><a href="/chat?projectId=${proj.projectId}">Chat</a></div>
	  </div>`;

    // 6) 수동 토글 바인딩: Data-API 대신 직접 Collapse API 사용
    btn.addEventListener('click', () => {
      const inst = bootstrap.Collapse.getOrCreateInstance(collapse, {
        toggle: false,
        parent: `#${PROJECT_LIST_ID}`
      });
      inst.toggle();

      // aria-expanded / collapsed 클래스 동기화
      const expanded = btn.getAttribute('aria-expanded') === 'true';
      btn.setAttribute('aria-expanded', expanded ? 'false' : 'true');
      btn.classList.toggle('collapsed', expanded);
    });

    projectList.appendChild(item);


}


})();