'use strict';

// --- DOM 요소 가져오기 ---
const messageForm = document.querySelector('#message-form');
const messageInput = document.querySelector('#message-input');
const messageArea = document.querySelector('#chat-messages');
const sendButton = document.querySelector('#send-button');
const connectingElement = document.querySelector('#connecting-status');

// --- Stomp 클라이언트 및 사용자/프로젝트 정보 ---
let stompClient = null;
// HTML 숨겨진 필드에서 값 읽기 (Thymeleaf가 서버에서 전달한 값)
const projectId = document.querySelector('#project-id')?.value?.trim();
const currentUserId = document.querySelector('#user-id')?.value?.trim();
const currentUsername = document.querySelector('#user-name')?.value?.trim();

// --- 핵심 기능 함수 ---

/**
 * 채팅 영역에 메시지를 표시
 * @param {object} messageData - 서버로부터 받은 메시지 객체 (ChatDTO)
 */
function displayMessage(messageData) {
    const messageElement = document.createElement('li');

    // 메시지 타입에 따라 스타일 및 내용 설정
    if (messageData.type === 'TALK') {
        messageElement.classList.add('message');
        // 내가 보낸 메시지인지 확인 (senderId 비교)
        if (messageData.senderId && messageData.senderId.toString() === currentUserId) {
            messageElement.classList.add('my-message');
        } else {
            messageElement.classList.add('other-message');
            // 다른 사람 메시지일 경우 보낸 사람 이름 표시
            const senderSpan = document.createElement('span');
            senderSpan.classList.add('sender');
            senderSpan.textContent = messageData.senderName || 'Unknown'; // 이름 없으면 Unknown
            messageElement.appendChild(senderSpan);
        }

        const messagePara = document.createElement('p');
        messagePara.classList.add('message-content');
        messagePara.textContent = messageData.message;
        messageElement.appendChild(messagePara);

        // 타임스탬프 표시
        const timestampSmall = document.createElement('small');
        timestampSmall.classList.add('timestamp');
        timestampSmall.textContent = messageData.timestamp ? formatTimestamp(messageData.timestamp) : '';
        messageElement.appendChild(timestampSmall);

    } else if (messageData.type === 'ENTER' || messageData.type === 'LEAVE') {
        // 입장/퇴장 메시지 스타일
        messageElement.classList.add('event-message');
        messageElement.textContent = messageData.message;
    } else {
        console.warn("알 수 없는 메시지 타입:", messageData);
        return; // 처리하지 않음
    }

    messageArea.appendChild(messageElement);
    // 새 메시지가 추가되면 스크롤을 맨 아래로 이동
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * 서버 타임스탬프 문자열을 'HH:mm' 형식으로 변환
 */
function formatTimestamp(timestampString) {
    if (!timestampString) return '';
    try {
        // 서버에서 'yyyy-MM-dd HH:mm:ss' 형식으로 제공한다고 가정
        const datePart = timestampString.substring(0, 10);
        const timePart = timestampString.substring(11);
        const isoString = `${datePart}T${timePart}`; // ISO 8601 유사 형식으로 변환
        const date = new Date(isoString);
        if (isNaN(date)) { // 유효하지 않은 날짜면 원본 반환
             console.warn("Invalid timestamp format:", timestampString);
             return timestampString;
        }
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    } catch (e) {
        console.error("타임스탬프 포맷팅 오류:", e);
        return timestampString; // 오류 시 원본 문자열 반환
    }
}

/**
 * 서버 API를 호출하여 채팅 기록을 가져와 화면에 표시
 */
async function fetchChatHistory() {
    if (!projectId) {
        console.error("프로젝트 ID가 없어 기록을 가져올 수 없습니다.");
        connectingElement.textContent = '오류: 프로젝트 정보 없음.';
        return;
    }

    connectingElement.textContent = '이전 대화 기록 로딩 중...';
    connectingElement.style.display = 'block'; // 로딩 메시지 표시

    try {
        // ChatController의 API 엔드포인트 호출
        const response = await fetch(`/api/chat/history/${projectId}`);

        if (!response.ok) {
             // 서버에서 4xx, 5xx 응답 시 오류 처리
             const errorText = await response.text();
             throw new Error(`채팅 기록 로딩 실패: ${response.status} ${response.statusText}. ${errorText}`);
        }

        const history = await response.json(); // 응답 본문을 JSON으로 파싱

        messageArea.innerHTML = ''; // 기존 메시지 모두 지우기
        history.forEach(msg => displayMessage(msg)); // 가져온 기록을 하나씩 화면에 표시

        console.log("채팅 기록 로딩 성공.");

    } catch (error) {
        console.error('채팅 기록 로딩 중 오류 발생:', error);
        connectingElement.textContent = '기록 로딩 실패. 새로고침 해주세요.';
        connectingElement.style.color = 'red';
        // 사용자에게 오류 메시지 표시
        displayMessage({ type: 'EVENT', message: `이전 대화 기록 로딩 실패: ${error.message}` });
    } finally {
         // 성공/실패 여부와 관계 없이 로딩 완료 후 처리 (예: 로딩 메시지 숨김)
         // 연결 성공 메시지는 onConnected에서 처리하므로 여기서는 숨기기만 함
         // connectingElement.style.display = 'none';
    }
}


// --- WebSocket 연결 및 관리 ---

/**
 * WebSocket 서버에 연결
 */
function connect() {
    // 사용자 ID와 프로젝트 ID가 모두 있어야 연결 시도
    if (currentUserId && projectId) {
        connectingElement.textContent = '서버에 연결 중...';
        connectingElement.style.display = 'block';
        connectingElement.style.color = 'var(--dark-connecting-text)'; // 기본 색상

        const socket = new SockJS('/ws'); // WebSocketConfig에 설정된 엔드포인트
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // STOMP 디버그 로그 끄기 (필요시 활성화)

        // 연결 시도 (헤더는 비워둠, 성공/실패 시 콜백 함수 지정)
        stompClient.connect({}, onConnected, onError);
    } else {
        console.error("사용자 ID 또는 프로젝트 ID가 없습니다. 연결 불가.");
        connectingElement.textContent = '연결 실패: 필수 정보 부족.';
        connectingElement.style.color = 'red';
        alert("채팅 서버 연결에 필요한 정보가 부족합니다. 페이지를 새로고침하거나 관리자에게 문의하세요.");
        // 입력/전송 버튼 비활성화 유지
        messageInput.disabled = true;
        sendButton.disabled = true;
    }
}

/**
 * WebSocket 연결 성공 시 실행될 콜백 함수
 */
function onConnected() {
    console.log("WebSocket 연결 성공!");
    connectingElement.textContent = '연결 성공! 채팅 기록 로딩 중...';

    // 1. 해당 프로젝트의 채팅 메시지를 받을 토픽 구독
    // 경로: /topic/chat/project/{projectId}
    const subscriptionUrl = `/topic/chat/project/${projectId}`;
    stompClient.subscribe(subscriptionUrl, onMessageReceived);
    console.log(`구독 시작: ${subscriptionUrl}`);

    // 2. 채팅 기록 로드 시도
    fetchChatHistory().then(() => {
        // 채팅 기록 로딩 성공 후 UI 활성화
        messageInput.disabled = false;
        sendButton.disabled = false;
        connectingElement.style.display = 'none'; // 연결 및 로딩 상태 메시지 숨김

        // 3. (선택적) 사용자 입장 메시지 서버로 전송
        // 경로: /app/chat.addUser/{projectId}
        stompClient.send(`/app/chat.addUser/${projectId}`,
            {}, // 헤더 없음
            JSON.stringify({ senderId: currentUserId, type: 'ENTER' }) // DTO 형식에 맞게 전송
        );
        console.log("입장 메시지 전송 완료.");

    }).catch(error => {
        // 채팅 기록 로딩 실패 시 처리
        console.error("채팅 기록 로딩 후 처리 중 오류:", error);
        connectingElement.textContent = '연결되었으나 기록 로딩 실패.';
        connectingElement.style.color = 'orange';
        // 입력은 가능하도록 둘 수 있음 (선택 사항)
        messageInput.disabled = false;
        sendButton.disabled = false;
    });
}

/**
 * WebSocket 연결 실패 시 실행될 콜백 함수
 */
function onError(error) {
    console.error('WebSocket 연결 실패:', error);
    connectingElement.textContent = '연결 실패! 인터넷 연결을 확인하거나 새로고침 해주세요.';
    connectingElement.style.color = 'red';
    // 입력/전송 버튼 비활성화
    messageInput.disabled = true;
    sendButton.disabled = true;
    alert("채팅 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
}

// --- 메시지 전송 및 수신 ---

/**
 * 메시지 입력 폼 제출 시 메시지 전송
 */
function sendMessage(event) {
    event.preventDefault(); // 폼 기본 제출 동작 방지
    const messageContent = messageInput.value.trim(); // 입력 내용 공백 제거

    // 메시지 내용이 있고, Stomp 클라이언트가 연결된 상태일 때만 전송
    if (messageContent && stompClient && stompClient.connected) {
        const chatMessage = {
            senderId: currentUserId,    // 현재 사용자 ID
            projectId: projectId,       // 현재 프로젝트 ID
            message: messageContent,    // 입력된 메시지
            type: 'TALK'                // 메시지 타입은 'TALK'
            // senderName, timestamp는 서버에서 처리
        };

        // 서버의 @MessageMapping 경로로 메시지 전송
        // 경로: /app/chat.sendMessage/{projectId}
        stompClient.send(`/app/chat.sendMessage/${projectId}`, {}, JSON.stringify(chatMessage));
        console.log("메시지 전송:", chatMessage);

        messageInput.value = ''; // 메시지 전송 후 입력 필드 비우기

    } else if (!stompClient || !stompClient.connected) {
        console.error("WebSocket이 연결되지 않아 메시지를 보낼 수 없습니다.");
        alert("서버와 연결이 끊어졌습니다. 메시지를 보내려면 페이지를 새로고침해주세요.");
    }
    // 입력 필드에 포커스 유지 (사용성 개선)
    messageInput.focus();
}

/**
 * 구독 중인 토픽에서 메시지를 수신했을 때 실행될 콜백 함수
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
messageForm.addEventListener('submit', sendMessage, true);

// --- 초기화 ---
// 페이지 로드 시 WebSocket 연결 시작
connect();