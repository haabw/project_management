$(document).ready(function() {
    // 저장 버튼 클릭 이벤트
    $("#saveProfileBtn").click(function() {
        // 에러 메시지 초기화
        $("#profileError").addClass("d-none").text("");

        // 폼 데이터 수집
        let formData = new FormData();
        let nickname = $("#nickname").val().trim();
        let newPassword = $("#newPassword").val();
        let confirmPassword = $("#confirmPassword").val();
        let profilePhoto = $("#profilePhoto")[0].files[0];

        // FormData에 필드 추가
        formData.append("nickname", nickname);
        if (newPassword) formData.append("newPassword", newPassword);
        if (confirmPassword) formData.append("confirmPassword", confirmPassword);
        if (profilePhoto) formData.append("profileImage", profilePhoto); // DTO 필드명과 일치

        // 클라이언트 측 유효성 검사
        let hasError = false;
        if (!nickname) {
            $("#nickname").addClass("is-invalid");
            $("#profileError").text("닉네임을 입력해주세요.").removeClass("d-none");
            hasError = true;
        }
        if (newPassword && newPassword !== confirmPassword) {
            $("#confirmPassword").addClass("is-invalid");
            $("#profileError").text("비밀번호가 일치하지 않습니다.").removeClass("d-none");
            hasError = true;
        }
        if (hasError) return;

        // AJAX 요청
        $.ajax({
            url: "/auth/update",
            type: "POST",
            data: formData,
            processData: false, // FormData 처리 방지
            contentType: false, // multipart/form-data로 전송
            success: function(response) {
                // 성공 시
                $("#profileModal").modal("hide");
                alert(response.success || "프로필이 성공적으로 업데이트되었습니다.");
                location.reload(); // 필요 시 페이지 새로고침
            },
            error: function(xhr) {
                // 에러 시
                let errorMessage = xhr.responseJSON?.error || "프로필 업데이트에 실패했습니다.";
                $("#profileError").text(errorMessage).removeClass("d-none");
            }
        });
    });

    // 입력 필드 변경 시 invalid 클래스 제거
    $("#nickname, #newPassword, #confirmPassword").on("input", function() {
        $(this).removeClass("is-invalid");
        $("#profileError").addClass("d-none");
    });

    // 파일 크기 제한 확인 (2MB)
    $("#profilePhoto").change(function() {
        let file = this.files[0];
        if (file && file.size > 2 * 1024 * 1024) {
            $("#profileError").text("이미지 크기는 2MB를 초과할 수 없습니다.").removeClass("d-none");
            $(this).val(""); // 파일 입력 초기화
        }
    });
});