document.addEventListener('DOMContentLoaded', function() {
	// 디바운싱 함수
	function debounce(func, wait) {
		let timeout;
		return function executedFunction(...args) {
			const later = () => {
				clearTimeout(timeout);
				func(...args);
			};
			clearTimeout(timeout);
			timeout = setTimeout(later, wait);
		};
	}

	// 체크박스 상태 업데이트 함수
	const updateTodoStatus = debounce(function(checkbox) {
		const todoId = checkbox.dataset.id;
		const completed = checkbox.checked;

		fetch('/todo/status', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: `todoId=${todoId}&completed=${completed}`
		})
			.then(response => response.text())
			.then(result => {
				if (result === 'success') {
					const span = checkbox.closest('li').querySelector('span');
					if (completed) {
						span.classList.add('completed');
					} else {
						span.classList.remove('completed');
					}
				} else {
					checkbox.checked = !completed; // 실패 시 원래 상태로 복구
				}
			})
			.catch(() => {
				checkbox.checked = !completed; // 오류 시 원래 상태로 복구
			});
	}, 300); // 300ms 디바운스

	// 체크박스 이벤트 리스너
	const checkboxes = document.querySelectorAll('.todo-checkbox');
	checkboxes.forEach(checkbox => {
		checkbox.addEventListener('change', function() {
			updateTodoStatus(this);
		});
	});
});