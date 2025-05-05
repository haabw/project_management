// admin.js - 관리자 페이지 전용 JavaScript

// ───────────── 전역 변수 설정 ─────────────
// pendingIds: 초대 대기 상태인 유저 ID 리스트
// approvedIds: 이미 프로젝트에 승인된 유저 ID 리스트
// projectId: 현재 프로젝트 ID (HTML 요소에 data-project-id로 전달되어야 함)
const pendingIds = window.pendingIds || [];
const approvedIds = window.approvedIds || [];
const projectId = document.getElementById("projectInfo")?.dataset?.projectId || 1;

// ───────────── 초대 모달 열기 ─────────────
function openInviteUserModal() {
	fetch('/admin/invite/users') // 초대 가능한 사용자 목록 요청
		.then(res => res.json())
		.then(data => {
			const list = document.getElementById('inviteUserList');
			list.innerHTML = ''; // 목록 초기화

			data.forEach(user => {
				const isPending = pendingIds.includes(user.id);     // 초대 대기 중인지 여부
				const isApproved = approvedIds.includes(user.id);   // 이미 승인된 멤버인지 여부

				let actionHtml;
				if (isApproved) {
					actionHtml = `<span style="color:gray;">멤버</span>`;
				} else if (isPending) {
					actionHtml = `<span style="color:gray;">대기중</span>`;
				} else {
					// 초대 가능 사용자
					actionHtml = `<button class="btn btn-sm btn-primary" onclick="sendInvite(${user.id})">초대</button>`;
				}

				// 테이블 row 구성
				const row = document.createElement('tr');
				row.innerHTML = `
          <td style='padding:.5rem;border:1px solid #ddd;'>${user.nickname}</td>
          <td style='padding:.5rem;border:1px solid #ddd;'>${user.email}</td>
          <td style='padding:.5rem;border:1px solid #ddd;text-align:center;'>${actionHtml}</td>`;
				list.appendChild(row);
			});

			// 모달 열기
			document.getElementById('inviteModal').style.display = 'block';
		});
}

// ───────────── 초대 모달 닫기 ─────────────
function closeInviteModal() {
	document.getElementById('inviteModal').style.display = 'none';
}

// ───────────── 초대 전송 기능 ─────────────
function sendInvite(userId) {
	if (!confirm('정말 초대하시겠습니까?')) return;

	fetch('/admin/invite/send', {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: new URLSearchParams({ receiverId: userId, projectId: projectId })
	})
		.then(r => {
			if (!r.ok) throw new Error();
			alert('초대 완료!');
			location.reload();
		})
		.catch(() => alert('초대 실패'));
}

// ───────────── 권한 변경 기능 ─────────────
function submitChangeRole(event, userId) {
	const selectEl = document.getElementById('roleSelect' + userId);
	const newRole = selectEl.value;

	if (!confirm('권한을 변경하시겠습니까?')) return;

	fetch(`/admin/permissions`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: new URLSearchParams({ userId, role: newRole, projectId })
	})
		.then(r => {
			if (!r.ok) throw new Error();
			alert('권한 변경 완료!');
			location.reload();
		})
		.catch(() => alert('권한 변경 실패'));
}

// ───────────── 팀원 추방 기능 ─────────────
function confirmKick(projectMemberId) {
	if (!confirm('정말로 이 사용자를 추방하시겠습니까?')) return;

	fetch('/admin/kick', {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: new URLSearchParams({ projectMemberId })
	})
		.then(r => {
			if (!r.ok) throw new Error();
			alert('추방 완료되었습니다.');
			location.reload();
		})
		.catch(() => alert('추방 실패'));
}

// ───────────── 초대 수락 기능 ─────────────
function handleAcceptInvite(button) {
	const inviteId = button.getAttribute('data-invite-id');

	fetch('/admin/invite/accept', {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: new URLSearchParams({ inviteId })
	})
		.then(r => {
			if (!r.ok) throw new Error();
			return r.text();
		})
		.then(msg => {
			alert(msg);
			location.reload();
		})
		.catch(() => alert('초대 수락 실패'));
}

// ───────────── 초대 거절 기능 ─────────────
function handleDeclineInvite(button) {
	const inviteId = button.getAttribute('data-invite-id');

	fetch('/admin/invite/decline', {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: new URLSearchParams({ inviteId })
	})
		.then(r => {
			if (!r.ok) throw new Error();
			return r.text();
		})
		.then(msg => {
			alert(msg);
			location.reload();
		})
		.catch(() => alert('초대 거절 실패'));
}


// 프로젝트 관리 관련 --------------------------------------------------------------
// ——————————————————————————————————————————
// 0) 모달 관련 엘리먼트 & Bootstrap Modal 인스턴스
// ——————————————————————————————————————————
const editModalEl = document.getElementById('editProjectModal');
const editModal = new bootstrap.Modal(editModalEl);
const editNameInput = document.getElementById('editProjectName');
const editIdInput = document.getElementById('editProjectId');
const confirmEditBtn = document.getElementById('confirmEditBtn');

// ——————————————————————————————————————————
// 1) DOMContentLoaded → URL 탭 체크 → 목록 로드
// ——————————————————————————————————————————
document.addEventListener('DOMContentLoaded', () => {
	if (window.currentAdminSection !== 'projects') return;
	loadProjectList();
});

// ——————————————————————————————————————————
// 2) 단일 프로젝트만 필터 → 테이블 렌더링
// ——————————————————————————————————————————
function loadProjectList() {
	const pid = window.currentAdminProjectId;
	if (!pid) return;

	fetch('/projects/list')
		.then(res => res.json())
		.then(projects => {
			const tbody = document.querySelector('#projectTable tbody');
			tbody.innerHTML = '';

			const project = projects.find(p => p.projectId === pid);
			if (!project) {
				tbody.innerHTML = '<tr><td colspan="4">프로젝트를 찾을 수 없습니다.</td></tr>';
				return;
			}

			// 한 줄짜리 tr 생성 (버튼은 .edit-btn, .delete-btn)
			const tr = document.createElement('tr');
			tr.innerHTML = `
        <td>${project.projectId}</td>
        <td>
          <input
            type="text"
            class="form-control-plaintext text-center"
            data-id="${project.projectId}"
            value="${project.projectName}">
        </td>
        <td>${project.createDate.replace('T', ' ').substring(0, 19)}</td>
        <td>
          <button type="button" 
                  class="btn btn-sm btn-outline-primary edit-btn" 
                  data-id="${project.projectId}">수정</button>
          <button type="button" 
                  class="btn btn-sm btn-outline-danger delete-btn" 
                  data-id="${project.projectId}">삭제</button>
        </td>
      `;
			tbody.appendChild(tr);

			// ——————————————————————————
			// 3) 수정 버튼 → 모달 띄우기
			// ——————————————————————————
			tr.querySelector('.edit-btn').addEventListener('click', () => {
				const nameInput = tr.querySelector('input.form-control-plaintext');
				if (!nameInput) {
					console.warn(`이름 input을 찾을 수 없습니다. id=${project.projectId}`);
					return;
				}
				const currentName = nameInput.value.trim();
				editIdInput.value = project.projectId;
				editNameInput.value = currentName;
				editNameInput.classList.remove('is-invalid');
				editModal.show();
			});

			// ——————————————————————————
			// 4) 삭제 버튼
			// ——————————————————————————
			tr.querySelector('.delete-btn').addEventListener('click', () => {
				if (!confirm('정말 삭제하시겠습니까?')) return;
				deleteProject(project.projectId);
			});
		})
		.catch(err => console.error('프로젝트 관리 로드 실패', err));
}

// ——————————————————————————————————————————
// 5) 모달 “수정” 클릭 → 실제 업데이트 호출
// ——————————————————————————————————————————
confirmEditBtn.addEventListener('click', async () => {
	const id = parseInt(editIdInput.value, 10);
	const name = editNameInput.value.trim();

	if (!name) {
		editNameInput.classList.add('is-invalid');
		return;
	}

	try {
		// 모달 닫기 전에 서버에 갱신 요청
		const updated = await updateProject(id, name);
		// 모달 닫기
		editModal.hide();

		// 테이블 행의 input을 찾아 값 갱신
		const rowInput = document.querySelector(
			`input.form-control-plaintext[data-id="${id}"]`
		);
		if (rowInput) {
			rowInput.value = updated.projectName;
		}

		// 2) 사이드바도 갱신
		if (typeof window.loadSidebarProjects === 'function') {
			window.loadSidebarProjects();
		}

	} catch (e) {
		console.error('프로젝트 수정 실패', e);
		alert('프로젝트 이름 수정에 실패했습니다.');
	}
});

// ——————————————————————————————————————————
// 6) 기존 updateProject / deleteProject 함수
// ——————————————————————————————————————————
// 서버에 이름 변경 요청
async function updateProject(id, newName) {
	const res = await fetch(`/projects/update`, {
		method: 'PUT',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ projectId: id, projectName: newName })
	});
	if (!res.ok) throw new Error(`HTTP ${res.status}`);
	return await res.json();
}



function deleteProject(id) {
	fetch(`/projects/delete/${id}`, { method: 'DELETE' })
		.then(res => {
			if (!res.ok) throw new Error('삭제 실패');
			alert('프로젝트가 삭제되었습니다.');
			loadProjectList();
			if (typeof window.loadSidebarProjects === 'function') {
				window.loadSidebarProjects();
			}
		})
		.catch(err => {
			console.error('삭제 중 오류', err);
			alert('삭제 중 오류가 발생했습니다.');
		});
}
