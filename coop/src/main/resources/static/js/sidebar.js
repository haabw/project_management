/* 변수 선언 */
const middle = document.getElementById("middleSidebar");
// 만약 closeToggle 요소가 없다면 sidebar.html에 추가해야 합니다.
// 예시: <div class="toggle-hover" id="closeToggle" style="visibility: hidden;">←</div> (team-header 안에)
const closeToggle = document.getElementById("closeToggle");
const resizer = document.getElementById("resizer");
const left = document.getElementById("leftSidebar");
const listEl = document.getElementById("project-list"); // 프로젝트 리스트 요소
const chatLink = document.getElementById("chat-link"); // 채팅 링크 요소

let isResizing = false;
let isFloatingLocked = false;
let selectedProjectId = null; // 현재 선택된 프로젝트 ID 저장 변수
window.currentlyOpenAction = null; // 현재 열려있는 액션 메뉴 (점3개 메뉴)

// --- 점 3개 메뉴 토글 (공통) ---
function toggleActions(el) {
    const menu = el.querySelector(".project-actions");
    if (!menu) return; // project-actions 요소가 없으면 중단

    // 이미 열린 다른 메뉴 닫기
    if (window.currentlyOpenAction && window.currentlyOpenAction !== menu) {
        window.currentlyOpenAction.style.display = "none";
    }

    const isOpen = menu.style.display === "block";
    menu.style.display = isOpen ? "none" : "block";
    window.currentlyOpenAction = isOpen ? null : menu;
}

// --- 외부 클릭 시 점 3개 메뉴 닫기 ---
document.addEventListener("click", function (e) {
    // 클릭된 요소가 액션 메뉴 내부 또는 점3개 버튼 자체가 아닌 경우
    if (!e.target.closest(".project-actions") && !e.target.closest(".menu-dots")) {
        if (window.currentlyOpenAction) {
            window.currentlyOpenAction.style.display = "none";
            window.currentlyOpenAction = null;
        }
    }
});

// --- 리사이징 로직 ---
if (resizer) { // resizer 요소가 있는지 확인
    resizer.addEventListener("mousedown", () => {
        isResizing = true;
        isFloatingLocked = true; // 리사이징 중에는 플로팅 방지
        document.body.style.cursor = "ew-resize";
        document.body.style.userSelect = 'none'; // 드래그 중 텍스트 선택 방지
    });
} else {
    console.warn("Element with ID 'resizer' not found.");
}


document.addEventListener("mousemove", (e) => {
    if (isResizing) {
        // left 요소가 있는지 확인 후 offsetWidth 접근
        const leftWidth = left ? left.offsetWidth : 0;
        const newWidth = e.clientX - leftWidth;
        // 최소/최대 너비 제한 (예: 180px ~ 500px)
        if (middle) { // middle 요소가 있는지 확인
             middle.style.width = `${Math.max(180, Math.min(newWidth, 500))}px`;
        }
    }
    // 플로팅 로직은 제거되었으므로 관련 코드 없음
});

document.addEventListener("mouseup", () => {
    if (isResizing) {
        isResizing = false;
        isFloatingLocked = false;
        document.body.style.cursor = "";
        document.body.style.userSelect = 'auto'; // 텍스트 선택 복구
    }
});

// --- 중간 사이드바 토글 버튼 (숨기기 버튼) ---
if (closeToggle && middle) { // closeToggle과 middle 요소가 있는지 확인
    middle.addEventListener("mouseenter", () => {
        if (!middle.classList.contains("hidden")) {
            closeToggle.style.visibility = "visible";
        }
    });

    middle.addEventListener("mouseleave", (e) => {
        // 마우스가 토글 버튼 위에 있지 않고, 플로팅 잠금 상태가 아닐 때 숨김
        if (!closeToggle.matches(':hover') && !isFloatingLocked) {
            closeToggle.style.visibility = "hidden";
        }
    });

    closeToggle.addEventListener("click", () => {
        middle.classList.add("hidden");
        middle.classList.remove("floating"); // 혹시 floating 상태였다면 제거
        closeToggle.style.visibility = 'hidden'; // 숨김 처리 후 버튼도 숨김
         // 왼쪽 사이드바에 열기 버튼 표시 로직 필요 시 여기에 추가
         const openToggle = document.getElementById("openToggle");
         if (openToggle) openToggle.style.display = 'flex'; // 또는 submenu.js에서 처리
    });
} else {
    if (!closeToggle) console.warn("Element with ID 'closeToggle' not found.");
    if (!middle) console.warn("Element with ID 'middleSidebar' not found.");
}


/* ========================================
   프로젝트 관리 및 채팅 연동 스크립트
   ======================================== */

// --- 페이지 로드 시 실행 ---
document.addEventListener("DOMContentLoaded", () => {
    // 요소들이 존재하는지 확인 후 이벤트 리스너 등록
    if (listEl) {
        loadProjects();          // 프로젝트 목록 로드
        setupEventDelegation();  // 이벤트 위임 설정 (클릭, 키다운 등)
    } else {
        console.warn("Element with ID 'project-list' not found.");
    }
    if (chatLink) {
        setupChatLinkListener(); // 채팅 링크 이벤트 리스너 설정
        updateChatLinkHref();    // 채팅 링크 초기 상태 설정
    } else {
        console.warn("Element with ID 'chat-link' not found.");
    }
});

// --- 프로젝트 리스트 로드 ---
function loadProjects() {
     if (!listEl) return; // listEl 없으면 함수 종료

    fetch("/list", { credentials: "same-origin" }) // 프로젝트 목록 API 호출
        .then(res => {
            if (!res.ok) {
                 // 서버 오류 응답 처리 (예: 404, 500 등)
                 if (res.status === 404) {
                     throw new Error("프로젝트 목록 API를 찾을 수 없습니다. (/list)");
                 } else if (res.status >= 500) {
                     throw new Error(`서버 오류 발생 (${res.status})`);
                 }
                // 기타 HTTP 오류
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            // 응답이 비어있는지 확인 (204 No Content 등)
            if (res.status === 204) {
                 return []; // 빈 배열 반환
            }
            // JSON 파싱 시도
            return res.json();
        })
        .then(projects => {
            listEl.innerHTML = ""; // 기존 목록 초기화
            if (projects && projects.length > 0) {
                projects.forEach((p, index) => {
                    renderProject(p); // 각 프로젝트 렌더링
                    // 첫 번째 프로젝트를 기본 선택 (selectedProjectId가 null일 때만)
                    if (index === 0 && selectedProjectId === null) {
                        selectProject(p.projectId); // projectId는 number 타입이어야 함
                    }
                });
                 // 페이지 로드 후, 이전에 선택된 프로젝트 ID가 있으면 해당 ID로 하이라이트
                 highlightSelectedProject(selectedProjectId); // selectedProjectId 상태에 따라 하이라이트 적용
            } else {
                // 프로젝트가 없을 경우
                listEl.innerHTML = "<p class='text-muted p-2 small'>참여 중인 프로젝트가 없습니다.</p>";
                selectedProjectId = null; // 선택된 프로젝트 없음
            }
            updateChatLinkHref(); // 프로젝트 로드 후 채팅 링크 업데이트
        })
        .catch(err => {
            console.error("프로젝트 로드 실패:", err);
            listEl.innerHTML = `<p class='text-danger p-2 small'>프로젝트 로드 오류: ${err.message}</p>`;
            selectedProjectId = null;
            updateChatLinkHref(); // 오류 발생 시에도 링크 업데이트 (비활성화)
        });
}

// --- 이벤트 위임 설정 ---
function setupEventDelegation() {
    if (!listEl) return; // listEl 없으면 함수 종료

    // 프로젝트 리스트 영역에서의 클릭 이벤트 처리
    listEl.addEventListener("click", e => {
        const projectTab = e.target.closest(".project-tab");
        if (!projectTab) return; // 프로젝트 탭이 아니면 무시

         // dataset.id 값이 있는지 확인하고 숫자로 변환
         const projectIdStr = projectTab.dataset.id;
         if (!projectIdStr) {
             console.error("Project tab is missing data-id attribute.");
             return;
         }
        const projectId = parseInt(projectIdStr, 10);
         if (isNaN(projectId)) {
             console.error("Invalid project ID found in data-id:", projectIdStr);
             return;
         }

        // 1. 수정/삭제 액션 처리
        const editAction = e.target.closest(".action-edit");
        const deleteAction = e.target.closest(".action-delete");
        const nameSpan = projectTab.querySelector(".project-name"); // 이름 요소 미리 찾기

        if (editAction) {
            const oldName = nameSpan ? nameSpan.textContent : ""; // 이름 요소 없으면 빈 문자열
            startEdit(projectTab, projectId, oldName);
            return; // 액션 처리 후 종료
        }
        if (deleteAction) {
            deleteProjectHandler(projectId);
            return; // 액션 처리 후 종료
        }

        // 2. 점3개 메뉴 토글 처리
        const dotMenu = e.target.closest(".menu-dots");
        if (dotMenu) {
            toggleActions(dotMenu);
            return; // 메뉴 토글 후 종료
        }

        // 3. 위의 경우가 아니면 프로젝트 탭 선택으로 간주
        if (!e.target.closest(".edit-input")) { // 수정 중인 input 클릭은 제외
             selectProject(projectId);
             highlightSelectedProject(projectId);
        }
    });

    // 프로젝트 리스트 영역에서의 키다운 이벤트 (주로 수정 input 처리)
    listEl.addEventListener("keydown", e => {
        if (!e.target.classList.contains("edit-input")) return;

        const tab = e.target.closest(".project-tab");
        if (!tab || !tab.dataset.id) return; // 탭 또는 ID 없으면 무시

        const id = parseInt(tab.dataset.id, 10);
         if (isNaN(id)) return; // 유효하지 않은 ID면 무시

        if (e.key === "Enter") {
            e.preventDefault(); // 기본 Enter 동작(폼 제출 등) 방지
            const newName = e.target.value.trim();
            if (!newName) {
                 alert("프로젝트 이름을 입력하세요.");
                 e.target.focus(); // 다시 input에 포커스
                 return;
            }
            updateProjectHandler(id, newName);
        } else if (e.key === "Escape") {
            loadProjects(); // Esc 누르면 수정 취소하고 목록 새로고침
        }
    });
}

// --- 단일 프로젝트 렌더링 ---
function renderProject(p) {
    if (!listEl || !p || typeof p.projectId === 'undefined' || typeof p.projectName === 'undefined') {
         console.error("Cannot render project: Invalid list element or project data.", p);
         return;
     }

    const tab = document.createElement("div");
    tab.className = "project-tab";
    tab.dataset.id = p.projectId; // 데이터 속성으로 ID 저장

    const nameSpan = document.createElement("span");
    nameSpan.textContent = p.projectName;
    nameSpan.className = "project-name";

    const menuGroup = document.createElement("div");
    menuGroup.className = "menu-group";
    // innerHTML 대신 DOM 조작을 사용하여 XSS 위험 감소 (더 안전한 방법)
    const arrowDiv = document.createElement("div");
    arrowDiv.className = "dropdown-arrow";
    arrowDiv.style.fontSize = "0.7rem";
    arrowDiv.style.color = "#888";
    arrowDiv.textContent = "▼";

    const dotsDiv = document.createElement("div");
    dotsDiv.className = "menu-dots";
    dotsDiv.innerHTML = `
        <span></span><span></span><span></span>
        <div class="project-actions" style="display: none;">
            <div class="action-edit small px-2 py-1">수정</div>
            <div class="action-delete small px-2 py-1 text-danger">삭제</div>
        </div>
    `;
    menuGroup.append(arrowDiv, dotsDiv);


    tab.append(nameSpan, menuGroup);
    listEl.append(tab);
}

// --- 프로젝트 선택 ---
function selectProject(projectId) {
     // projectId 유효성 검사 추가
     if (typeof projectId !== 'number' || isNaN(projectId)) {
         console.warn("Invalid projectId type for selection:", projectId);
         return;
     }
    console.log("Project selected:", projectId);
    selectedProjectId = projectId; // 전역 변수 업데이트
    updateChatLinkHref(); // 채팅 링크 업데이트
     highlightSelectedProject(projectId); // 선택된 프로젝트 하이라이트
}

// --- 선택된 프로젝트 하이라이트 ---
function highlightSelectedProject(projectId) {
     if (!listEl) return; // listEl 없으면 함수 종료

    // 모든 탭에서 'selected' 스타일 제거
    document.querySelectorAll(".project-tab").forEach(tab => {
        tab.classList.remove("selected");
        tab.style.backgroundColor = ''; // 기본 배경색 복원
        tab.style.borderColor = '';     // 기본 테두리색 복원
    });

    // 선택된 ID와 일치하는 탭에 'selected' 스타일 추가
    if (projectId !== null && typeof projectId === 'number' && !isNaN(projectId)) { // 타입 및 유효성 검사 추가
        const selectedTab = listEl.querySelector(`.project-tab[data-id='${projectId}']`);
        if (selectedTab) {
            selectedTab.classList.add("selected");
            selectedTab.style.backgroundColor = '#e7f1ff'; // 선택 배경색 (연한 하늘색)
            selectedTab.style.borderColor = '#007bff';     // 선택 테두리색 (파란색)
        } else {
             console.warn(`Project tab with ID ${projectId} not found for highlighting.`);
        }
    }
}

// --- 채팅 버튼 링크 업데이트 ---
function updateChatLinkHref() {
    if (!chatLink) return; // chatLink 없으면 함수 종료

    const userIdElement = document.getElementById('current-user-id');
    const userNameElement = document.getElementById('current-user-name');

    // 사용자 정보 요소가 있는지, 값이 있는지 확인
    const userId = userIdElement?.value;
    const userName = userNameElement?.value;

    if (!userId || !userName) {
        console.warn("User ID or User Name element not found or has no value.");
        chatLink.href = "#";
        chatLink.classList.add('disabled'); // 부트스트랩 'disabled' 클래스 사용
        chatLink.setAttribute('aria-disabled', 'true'); // 접근성을 위한 속성 추가
        chatLink.style.pointerEvents = 'none'; // 클릭 불가
        chatLink.style.opacity = '0.65';       // 시각적으로 비활성화 표시
        return;
    }

     // selectedProjectId 유효성 검사
    if (selectedProjectId !== null && typeof selectedProjectId === 'number' && !isNaN(selectedProjectId)) {
        try {
            // 사용자 이름 인코딩 (URL에 안전하게 포함시키기 위함)
            const encodedUserName = encodeURIComponent(userName);
            chatLink.href = `/chat/${selectedProjectId}?userId=${userId}&username=${encodedUserName}`;
            chatLink.classList.remove('disabled');
            chatLink.removeAttribute('aria-disabled');
            chatLink.style.pointerEvents = 'auto';
            chatLink.style.opacity = '1';
        } catch (e) {
             console.error("Error encoding username:", e);
             // 인코딩 실패 시 비활성화
             chatLink.href = "#";
             chatLink.classList.add('disabled');
             chatLink.setAttribute('aria-disabled', 'true');
             chatLink.style.pointerEvents = 'none';
             chatLink.style.opacity = '0.65';
        }
    } else {
        // 프로젝트가 선택되지 않았거나 ID가 유효하지 않은 경우
        chatLink.href = "#";
        chatLink.classList.add('disabled');
        chatLink.setAttribute('aria-disabled', 'true');
        chatLink.style.pointerEvents = 'none';
        chatLink.style.opacity = '0.65';
    }
}


// --- 채팅 버튼 클릭 리스너 설정 ---
function setupChatLinkListener() {
    if (!chatLink) return; // chatLink 없으면 함수 종료

    chatLink.addEventListener('click', (event) => {
        // current-user-id와 current-user-name 요소 및 값 확인
        const userId = document.getElementById('current-user-id')?.value;
        const userName = document.getElementById('current-user-name')?.value;

        // 프로젝트 선택 여부 및 사용자 정보 유효성 검사
        if (selectedProjectId === null || typeof selectedProjectId !== 'number' || isNaN(selectedProjectId)) {
            event.preventDefault(); // 링크 이동 방지
            alert("채팅을 시작할 프로젝트를 먼저 선택해주세요.");
        } else if (!userId || !userName) {
            event.preventDefault(); // 링크 이동 방지
            alert("사용자 정보를 가져올 수 없습니다. 페이지를 새로고침하거나 다시 로그인해주세요.");
            // 필요시 로그인 페이지로 리다이렉트
            // window.location.href = '/auth/login';
        }
        // 모든 조건 만족 시 기본 href 동작(링크 이동) 허용
    });
}


// --- 프로젝트 추가 입력창 표시 ---
function showAddInput() {
     if (!listEl) return; // listEl 없으면 함수 종료

    // 이미 추가 입력창이 있는지 확인
    const existingInput = listEl.querySelector('input.new-project-input');
    if (existingInput) {
        existingInput.focus();
        return;
    }

    const input = document.createElement("input");
    input.type = "text";
    input.placeholder = "새 프로젝트명 입력 (Enter)";
    input.className = "form-control form-control-sm mb-2 new-project-input"; // 식별 클래스 추가
    listEl.prepend(input); // 리스트 맨 앞에 추가
    input.focus();
    bindSaveOnEnter(input); // Enter 키 이벤트 바인딩
}

// --- 새 프로젝트 저장 (Enter 키) ---
function bindSaveOnEnter(input) {
    input.addEventListener("keydown", e => {
        if (e.key === "Escape") {
            input.remove(); // Esc: 입력창 제거
            return;
        }
        if (e.key !== "Enter") return; // Enter 아니면 무시

        e.preventDefault(); // Enter 기본 동작 방지

        const name = input.value.trim();
        if (!name) {
            alert("프로젝트 이름을 입력하세요.");
            return;
        }

        // 서버로 프로젝트 추가 요청 보내기
        fetch(`/add`, { // ProjectController의 POST /add 매핑 가정
            method: "POST",
            credentials: "same-origin", // CSRF 토큰 등을 위해 필요할 수 있음
             // Content-Type을 application/json으로 명시
            headers: {
                 "Content-Type": "application/json",
                 // CSRF 토큰 헤더 추가 (Spring Security 사용 시 필요할 수 있음)
                 // 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content') // 예시
             },
             // DTO 형식에 맞게 JSON 문자열로 변환하여 전송
            body: JSON.stringify({ projectName: name })
        })
        .then(res => {
            if (!res.ok) {
                 // 서버 오류 처리 (상태 코드, 메시지 확인)
                 return res.text().then(text => {
                      let errorMsg = `프로젝트 추가 실패 (${res.status})`;
                      try { // 서버에서 JSON 형태의 오류 메시지를 보냈을 경우 파싱 시도
                          const errorJson = JSON.parse(text);
                          if (errorJson && errorJson.message) {
                              errorMsg = errorJson.message;
                          } else if (text) { // JSON 파싱 실패 시 원본 텍스트 사용
                              errorMsg = text;
                          }
                      } catch (parseError) {
                           if (text) errorMsg = text; // JSON 파싱 실패 시 원본 텍스트 사용
                      }
                      throw new Error(errorMsg);
                 });
            }
             // 성공 시 JSON 응답 파싱 시도 (생성된 프로젝트 정보 포함 가정)
             // 응답 본문이 없을 수도 있으므로 확인 후 파싱
             const contentType = res.headers.get("content-type");
             if (contentType && contentType.indexOf("application/json") !== -1) {
                 return res.json();
             } else {
                 return null; // JSON 아니면 null 반환
             }
        })
        .then((newProject) => {
            input.remove();   // 입력창 제거
            loadProjects();   // 목록 새로고침

             // 새로 추가된 프로젝트를 바로 선택 (선택 사항)
            if (newProject && newProject.projectId) {
                // DOM 업데이트 후 실행되도록 약간 지연 (requestAnimationFrame 사용 권장)
                requestAnimationFrame(() => {
                     // projectId 유효성 검사 후 선택
                     const newProjectId = parseInt(newProject.projectId, 10);
                     if (!isNaN(newProjectId)) {
                         selectProject(newProjectId);
                     }
                });
            }
        })
        .catch(err => {
            console.error("프로젝트 추가 오류:", err);
            alert(`프로젝트 추가 실패: ${err.message}`);
        });
    });
}

// --- 프로젝트 이름 수정 시작 ---
function startEdit(tab, id, oldName) {
     if (!tab) return; // tab 없으면 함수 종료

    // 기존 요소 숨기기 (요소 존재 여부 확인)
    const nameSpan = tab.querySelector('.project-name');
    const menuGroup = tab.querySelector('.menu-group');
    if (nameSpan) nameSpan.style.display = 'none';
    if (menuGroup) menuGroup.style.display = 'none';

    // 기존 input 있으면 제거
    const existingInput = tab.querySelector('.edit-input');
	if (existingInput) existingInput.remove();

    // 새 input 생성 및 설정
    const input = document.createElement("input");
    input.type = "text";
    input.value = oldName;
    input.className = "form-control form-control-sm flex-grow-1 edit-input";
    input.style.marginRight = '5px'; // 메뉴와의 간격

    // nameSpan 앞에 삽입 (nameSpan이 없다면 tab의 시작 부분에 추가)
     if (nameSpan) {
         tab.insertBefore(input, nameSpan);
     } else {
         tab.prepend(input); // nameSpan 없으면 맨 앞에 추가
     }
    input.focus();
    input.select();

    // input에서 포커스가 벗어나면 수정 취소 (선택적)
    input.addEventListener('blur', () => {
        // Enter나 Escape로 이미 처리된 경우 제외
        // setTimeout을 사용하여 Enter/Esc 이벤트가 먼저 처리될 시간 확보
        setTimeout(() => {
            const stillEditing = tab.contains(input); // input이 아직 DOM에 있는지 확인
            if (stillEditing) {
                loadProjects(); // 포커스 잃으면 목록 새로고침 (수정 취소 효과)
            }
        }, 150); // 150ms 정도 지연
    }, { once: true }); // 이벤트 리스너 한 번만 실행
}

// --- 프로젝트 수정 요청 핸들러 ---
function updateProjectHandler(id, newName) {
     // id, newName 유효성 검사
     if (typeof id !== 'number' || isNaN(id) || typeof newName !== 'string' || !newName) {
         console.error("Invalid arguments for updateProjectHandler:", id, newName);
         alert("프로젝트 수정 중 오류가 발생했습니다. (입력값 오류)");
         return;
     }

    fetch(`/update`, { // ProjectController의 PUT /update 매핑 가정
        method: "PUT",
        credentials: "same-origin",
         headers: {
             "Content-Type": "application/json",
             // 'X-CSRF-TOKEN': csrfToken // 필요시 CSRF 토큰 추가
         },
        body: JSON.stringify({ projectId: id, projectName: newName }) // DTO 형식 준수
    })
    .then(res => {
        if (!res.ok) {
             // 오류 처리 강화
             return res.text().then(text => {
                  let errorMsg = `프로젝트 수정 실패 (${res.status})`;
                  try {
                      const errorJson = JSON.parse(text);
                      if (errorJson && errorJson.message) errorMsg = errorJson.message;
                      else if (text) errorMsg = text;
                  } catch (e) { if (text) errorMsg = text; }
                  throw new Error(errorMsg);
             });
        }
        // 성공 시 JSON 응답 파싱 시도 (응답 본문 있을 경우)
        const contentType = res.headers.get("content-type");
         if (contentType && contentType.indexOf("application/json") !== -1) {
             return res.json();
         } else {
             return null; // JSON 아니면 null 반환
         }
    })
    .then(() => {
        console.log("Project updated, reloading list...");
        loadProjects(); // 성공 시 목록 새로고침
    })
    .catch(err => {
        console.error("프로젝트 수정 오류:", err);
        alert(`프로젝트 수정 실패: ${err.message}`);
        loadProjects(); // 실패 시에도 목록 새로고침 (원래 이름으로 복구)
    });
}

// --- 프로젝트 삭제 요청 핸들러 ---
function deleteProjectHandler(id) {
     // id 유효성 검사
     if (typeof id !== 'number' || isNaN(id)) {
         console.error("Invalid project ID for deletion:", id);
         alert("프로젝트 삭제 중 오류가 발생했습니다. (ID 오류)");
         return;
     }
     if (!listEl) return; // listEl 없으면 중단

     // 프로젝트 이름 가져오기 (없을 수도 있음)
     const projectNameElement = listEl.querySelector(`.project-tab[data-id='${id}'] .project-name`);
     const projectName = projectNameElement ? projectNameElement.textContent : '해당';

    if (!confirm(`"${projectName}" 프로젝트를 정말 삭제하시겠습니까?\n(관련된 모든 데이터가 삭제됩니다)`)) return;

    fetch(`/delete/${id}`, { // ProjectController의 DELETE /delete/{id} 매핑 가정
        method: "DELETE",
        credentials: "same-origin",
         headers: {
             // 'X-CSRF-TOKEN': csrfToken // 필요시 CSRF 토큰 추가
         }
    })
    .then(res => {
        if (!res.ok) {
             // 오류 처리 강화
             return res.text().then(text => {
                  let errorMsg = `프로젝트 삭제 실패 (${res.status})`;
                  try {
                      const errorJson = JSON.parse(text);
                      if (errorJson && errorJson.message) errorMsg = errorJson.message;
                      else if (text) errorMsg = text;
                  } catch (e) { if (text) errorMsg = text; }
                  throw new Error(errorMsg);
             });
        }
        // 성공 시 (응답 본문 없을 수 있음)
        console.log("Project deleted, reloading list...");
         // 삭제된 프로젝트가 현재 선택된 프로젝트였다면 선택 해제
         if (selectedProjectId === id) {
             selectedProjectId = null;
         }
        loadProjects(); // 목록 새로고침
    })
    .catch(err => {
        console.error("프로젝트 삭제 오류:", err);
        alert(`프로젝트 삭제 실패: ${err.message}`);
        loadProjects(); // 실패 시에도 목록 새로고침
    });
}