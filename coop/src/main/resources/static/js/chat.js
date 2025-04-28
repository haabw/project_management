'use strict';

// --- DOM 요소 가져오기 ---
const messageForm = document.querySelector('#message-form');        // 메시지 입력 폼
const messageInput = document.querySelector('#message-input');      // 메시지 입력 필드
const messageArea = document.querySelector('#chat-messages');     // 메시지 표시 영역 (ul)
const sendButton = document.querySelector('#send-button');          // 전송 버튼
const connectingElement = document.querySelector('#connecting-status'); // 연결 상태 표시 영역

// --- Stomp 클라이언트 및 사용자 정보 ---
let stompClient = null; // Stomp 클라이언트 인스턴스
// HTML 숨겨진 필드에서 프로젝트 ID와 사용자 정보 읽기
const projectId = document.querySelector('#project-id').value.trim();
const currentUserId = document.querySelector('#user-id').value.trim(); // 현재 로그인된 사용자 ID
const currentUsername = document.querySelector('#user-name').value.trim(); // 현재 로그인된 사용자 이름

// --- 핵심 기능 함수 ---

/**
 * 채팅 영역에 메시지를 표시하는 함수
 * @param {object} messageData - 서버로부터 받은 메시지 객체 (ChatDTO 형식)
 */
function displayMessage(messageData) {
    const messageElement = document.createElement('li'); // 새 리스트 아이템 생성

    // 메시지 타입에 따른 처리 (TALK, ENTER 등)
    if (messageData.type === 'TALK') {
        // 일반 채팅 메시지 스타일링
        messageElement.classList.add('message');
        // 내가 보낸 메시지인지 다른 사람이 보낸 메시지인지 구분
        if (messageData.senderId && messageData.senderId.toString() === currentUserId) {
            messageElement.classList.add('my-message'); // 내 메시지 스타일 적용
        } else {
            messageElement.classList.add('other-message'); // 다른 사용자 메시지 스타일 적용
        }

        // 메시지 내용 구성
        const senderSpan = document.createElement('span');
        senderSpan.classList.add('sender');
        // 내가 보낸 메시지면 'You', 아니면 발신자 이름 표시
        senderSpan.textContent = (messageData.senderId && messageData.senderId.toString() === currentUserId) ? 'You' : messageData.senderName || 'Unknown';

        const messagePara = document.createElement('p');
        messagePara.classList.add('message-content');
        messagePara.textContent = messageData.message;

        const timestampSmall = document.createElement('small');
        timestampSmall.classList.add('timestamp');
        timestampSmall.textContent = messageData.timestamp ? formatTimestamp(messageData.timestamp) : ''; // 타임스탬프 포맷팅

        messageElement.appendChild(senderSpan);
        messageElement.appendChild(messagePara);
        messageElement.appendChild(timestampSmall);

    } else if (messageData.type === 'ENTER' || messageData.type === 'LEAVE') {
        // 입장/퇴장 등 시스템 메시지 스타일링 (선택적 구현)
        messageElement.classList.add('event-message');
        messageElement.textContent = messageData.message;
    } else {
        // 알 수 없는 타입의 메시지는 로그만 남김
        console.warn("Unknown message type received:", messageData);
        return; // 화면에 표시하지 않음
    }

    messageArea.appendChild(messageElement); // 완성된 메시지 요소를 채팅 영역에 추가
    messageArea.scrollTop = messageArea.scrollHeight; // 항상 최신 메시지가 보이도록 스크롤을 맨 아래로 이동
}

/**
 * 서버로부터 받은 타임스탬프 문자열을 보기 좋은 형식으로 변환 (선택적)
 * 예: "2025-04-28 22:36:00" -> "오후 10:36" 또는 "어제 오후 10:36" 등
 * @param {string} timestampString - 서버에서 받은 타임스탬프 문자열 (yyyy-MM-dd HH:mm:ss)
 * @returns {string} - 포맷팅된 시간 문자열
 */
function formatTimestamp(timestampString) {
    if (!timestampString) return '';
    try {
        const date = new Date(timestampString.replace(' ', 'T')); // ISO 8601 유사 형식으로 변경
        // 간단하게 시간만 표시 (HH:mm)
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
        // 필요에 따라 더 복잡한 날짜/시간 포맷팅 로직 추가 가능
    } catch (e) {
        console.error("Error formatting timestamp:", e);
        return timestampString; // 파싱 실패 시 원본 반환
    }
}


/**
 * 서버 API를 호출하여 특정 프로젝트의 채팅 기록을 가져와 화면에 표시하는 함수
 */
async function fetchChatHistory() {
    if (!projectId) {
        console.error("Project ID is missing, cannot fetch history.");
        connectingElement.textContent = '오류: 프로젝트 ID 없음.';
        return;
    }

    connectingElement.textContent = '이전 대화 기록 로딩 중...'; // 로딩 상태 표시

    try {
        // REST API 엔드포인트 호출
        const response = await fetch(`/api/chat/history/${projectId}`);
        if (!response.ok) {
            // HTTP 오류 처리
            throw new Error(`채팅 기록 로딩 실패: ${response.status} ${response.statusText}`);
        }
        const history = await response.json(); // 응답 본문을 JSON으로 파싱

        messageArea.innerHTML = ''; // 기존 메시지 내용 초기화
        history.forEach(msg => displayMessage(msg)); // 가져온 각 메시지를 화면에 표시

        connectingElement.textContent = '연결되었습니다.'; // 성공 메시지 (잠시 후 숨김 처리됨)
        console.log("Chat history loaded successfully.");

    } catch (error) {
        console.error('채팅 기록 로딩 중 오류 발생:', error);
        connectingElement.textContent = '기록 로딩 실패. 새로고침 해주세요.';
        // 사용자에게 오류 메시지 표시
        displayMessage({ type: 'EVENT', message: "이전 대화 기록을 불러오는데 실패했습니다." });
    }
}


// --- WebSocket 연결 및 관리 ---

/**
 * WebSocket 서버에 연결하는 함수
 */
function connect() {
    // 사용자 ID와 프로젝트 ID가 모두 있어야 연결 시도
    if (currentUserId && projectId) {
        connectingElement.style.display = 'block'; // '연결 중...' 메시지 표시
        connectingElement.textContent = '서버에 연결 중...';

        // SockJS를 사용하여 WebSocket 연결 생성 (/ws는 WebSocketConfig에서 설정한 엔드포인트)
        const socket = new SockJS('/ws');
        // Stomp 클라이언트 생성
        stompClient = Stomp.over(socket);

        // Stomp 클라이언트 디버그 메시지 비활성화 (콘솔 로그 정리)
        stompClient.debug = null;

        // Stomp 연결 시도 (헤더는 필요시 추가, 콜백 함수 등록)
        stompClient.connect({}, onConnected, onError);
    } else {
        console.error("사용자 ID 또는 프로젝트 ID가 없습니다. 연결할 수 없습니다.");
        connectingElement.textContent = '연결 실패: 필수 정보 부족.';
        alert("채팅 서버에 연결하기 위한 정보(사용자 ID, 프로젝트 ID)가 부족합니다. 페이지를 새로고침하거나 관리자에게 문의하세요.");
    }
}

/**
 * WebSocket 연결 성공 시 호출되는 콜백 함수
 */
function onConnected() {
    console.log("WebSocket 연결 성공!");
    connectingElement.textContent = '연결 성공. 기록 로딩 중...';

    // 특정 프로젝트의 채팅 토픽 구독 시작
    // 서버는 이 토픽으로 메시지를 브로드캐스트함 (/topic/chat/project/{projectId})
    const subscriptionUrl = `/topic/chat/project/${projectId}`;
    stompClient.subscribe(subscriptionUrl, onMessageReceived);
    console.log(`구독 시작: ${subscriptionUrl}`);

    // 구독 설정 후 채팅 기록 로드 (메시지 누락 방지)
    fetchChatHistory().then(() => {
        // 채팅 기록 로딩 완료 후 입력 필드 및 버튼 활성화
        messageInput.disabled = false;
        sendButton.disabled = false;
        connectingElement.style.display = 'none'; // '연결 중' 메시지 숨김

        // (선택적) 사용자 입장 메시지 서버로 전송
        // 서버의 ChatController에 @MessageMapping("/chat.addUser/{projectId}") 구현 필요
        /*
        stompClient.send(`/app/chat.addUser/${projectId}`,
            {}, // 헤더 없음
            JSON.stringify({ senderId: currentUserId, type: 'ENTER' }) // 입장 메시지임을 알림
        );
        console.log("입장 메시지 전송됨.");
        */

    }).catch(error => {
        // fetchChatHistory 내에서 오류 처리되지만, 추가적인 처리 필요 시 여기에 작성
        console.error("채팅 기록 로딩 또는 후속 처리 실패:", error);
        connectingElement.textContent = '연결되었으나 초기화 실패.';
    });
}

/**
 * WebSocket 연결 실패 시 호출되는 콜백 함수
 */
function onError(error) {
    console.error('WebSocket 연결 실패:', error);
    connectingElement.textContent = '연결 실패! 인터넷 연결을 확인하거나 새로고침 해주세요.';
    connectingElement.style.color = 'red';
    // 입력 필드 및 버튼 비활성화
    messageInput.disabled = true;
    sendButton.disabled = true;
    // 사용자에게 알림
    alert("채팅 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
}

// --- 메시지 전송 및 수신 ---

/**
 * 메시지 입력 폼 제출 시 호출되는 함수 (메시지 전송)
 * @param {Event} event - 폼 제출 이벤트 객체
 */
function sendMessage(event) {
    event.preventDefault(); // 폼 기본 제출 동작(페이지 새로고침) 방지

    const messageContent = messageInput.value.trim(); // 입력된 메시지 내용 (양 끝 공백 제거)

    // 메시지 내용이 있고, Stomp 클라이언트가 연결된 상태일 때만 전송
    if (messageContent && stompClient && stompClient.connected) {
        // 서버로 보낼 메시지 객체 생성 (ChatDTO 형식 준수)
        const chatMessage = {
            senderId: currentUserId,    // 발신자 ID (필수)
            projectId: projectId,       // 프로젝트 ID (필수)
            message: messageContent,    // 메시지 내용
            type: 'TALK'                // 메시지 타입 (일반 채팅)
            // senderName과 timestamp는 서버에서 설정하므로 보내지 않음
        };

        // Stomp 클라이언트를 사용하여 메시지 전송
        // 대상 경로: /app/chat.sendMessage/{projectId} (WebSocketConfig 및 ChatController 설정과 일치)
        stompClient.send(`/app/chat.sendMessage/${projectId}`,
            {}, // 헤더 없음
            JSON.stringify(chatMessage) // 메시지 객체를 JSON 문자열로 변환하여 전송
        );

        console.log("메시지 전송:", chatMessage);

        // 메시지 전송 후 입력 필드 비우기
        messageInput.value = '';
    } else if (!stompClient || !stompClient.connected) {
        console.error("WebSocket이 연결되지 않아 메시지를 보낼 수 없습니다.");
        alert("서버와 연결이 끊어졌습니다. 메시지를 보내려면 페이지를 새로고침해주세요.");
    }
}

/**
 * 구독 중인 토픽으로부터 메시지를 수신했을 때 호출되는 콜백 함수
 * @param {object} payload - 수신된 메시지 객체 (body에 실제 메시지 내용 포함)
 */
function onMessageReceived(payload) {
    console.log("메시지 수신:", payload);
    let message;
    try {
        // 수신된 메시지 본문(JSON 문자열)을 JavaScript 객체로 파싱
        message = JSON.parse(payload.body);
    } catch (e) {
        console.error("수신 메시지 파싱 오류:", e, payload.body);
        return; // 파싱 실패 시 처리 중단
    }

    // 파싱된 메시지 객체를 화면에 표시
    displayMessage(message);
}

// --- 이벤트 리스너 등록 ---
// 메시지 폼 제출 이벤트에 sendMessage 함수 연결
messageForm.addEventListener('submit', sendMessage, true);

// --- 초기화 ---
// 페이지 로드 시 WebSocket 연결 시작
connect();
