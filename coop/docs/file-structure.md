coop/
├── docs/                                # 프로젝트 문서
│   ├── file-structure.md                # 파일 구조와 종속성 설명
│   ├── setup-guide.md                   # 개발 환경 설정 가이드
│   ├── project-plan.md                  # 프로젝트 계획서
│   ├── init.sql                         # MySQL 초기 테이블 스키마
├── src/                                 # 소스 코드 및 리소스
│   ├── main/                            # 메인 애플리케이션 코드
│   │   ├── java/                        # Java 소스 코드
│   │   │   ├── com/
│   │   │   │   ├── coop/                # 프로젝트 기본 패키지
│   │   │   │   │   ├── CoopApplication.java  # Spring Boot 메인 클래스
│   │   │   │   │   ├── config/          # 설정 클래스
│   │   │   │   │   │   ├── WebSocketConfig.java  # WebSocket 설정
│   │   │   │   │   ├── controller/      # 요청 처리
│   │   │   │   │   │   ├── AuthController.java   # 로그인/회원가입 처리
│   │   │   │   │   │   ├── MainController.java   # Index, Admin, Chat, Gantt, Mindmap 라우팅
│   │   │   │   │   ├── entity/          # JPA 엔티티
│   │   │   │   │   │   ├── ChatEntity.java       # 채팅 데이터
│   │   │   │   │   │   ├── GanttEntity.java      # 간트차트 데이터
│   │   │   │   │   │   ├── ProjectEntity.java    # 프로젝트 데이터
│   │   │   │   │   │   ├── UserEntity.java       # 사용자 데이터
│   │   │   │   │   ├── repository/      # JPA 레포지토리
│   │   │   │   │   │   ├── ChatRepository.java   # 채팅 레포지토리
│   │   │   │   │   │   ├── CooperationRepository.java # 간트차트, 마인드맵 레포지토리
│   │   │   │   │   │   ├── ProjectRepository.java # 프로젝트 레포지토리
│   │   │   │   │   │   ├── UserRepository.java   # 사용자 레포지토리
│   │   │   │   │   ├── service/         # 비즈니스 로직
│   │   │   │   │   │   ├── ChatService.java      # 채팅 서비스
│   │   │   │   │   │   ├── CooperationService.java # 간트차트, 마인드맵 서비스
│   │   │   │   │   │   ├── ProjectService.java   # 프로젝트 서비스
│   │   │   │   │   │   ├── UserService.java      # 사용자 서비스 (로그인, 회원가입)
│   │   │   │   │   ├── dto/             # 데이터 전송 객체
│   │   │   │   │   │   ├── ChatDTO.java          # 채팅 DTO
│   │   │   │   │   │   ├── GanttDTO.java         # 간트차트 DTO
│   │   │   │   │   │   ├── LoginDTO.java         # 로그인 요청
│   │   │   │   │   │   ├── MindmapDTO.java       # 마인드맵 DTO
│   │   │   │   │   │   ├── ProjectDTO.java       # 프로젝트 DTO
│   │   │   │   │   │   ├── SignupDTO.java        # 회원가입 요청
│   │   ├── resources/                   # 설정 및 리소스
│   │   │   ├── application.properties   # DB, 서버 설정
│   │   │   ├── static/                  # 정적 파일
│   │   │   │   ├── css/                 # 스타일시트
│   │   │   │   │   ├── style.css        # 전체 스타일
│   │   │   │   │   ├── sidebar.css      # 첫 번째 사이드바 스타일
│   │   │   │   │   ├── submenu.css      # 두 번째 사이드바 스타일
│   │   │   │   ├── js/                  # 클라이언트 스크립트
│   │   │   │   │   ├── chat.js          # WebSocket 클라이언트
│   │   │   │   │   ├── sidebar.js       # 고정 사이드바 동작
│   │   │   │   │   ├── submenu.js       # 세부 메뉴 동작
│   │   │   ├── templates/               # Thymeleaf 템플릿
│   │   │   │   ├── fragments/           # 템플릿 조각
│   │   │   │   │   ├── sidebar.html     # 첫 번째 고정 사이드바
│   │   │   │   │   ├── submenu.html     # 두 번째 사이드바
│   │   │   │   ├── layout/              # 공통 레이아웃
│   │   │   │   │   ├── main.html        # 두 사이드바 포함 레이아웃
│   │   │   │   ├── login.html           # 로그인 페이지
│   │   │   │   ├── index.html           # 메인 대시보드
│   │   │   │   ├── admin.html           # 관리자 페이지
│   │   │   │   ├── chat.html            # 채팅 페이지
│   │   │   │   ├── gantt.html           # 간트차트 페이지
│   │   │   │   ├── mindmap.html         # 마인드맵 페이지
│   ├── test/                            # 테스트 코드
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── coop/
│   │   │   │   │   ├── CoopApplicationTests.java  # 애플리케이션 테스트
├── .gitignore                           # Git 무시 파일
├── pom.xml                              # Maven 설정
├── README.md                            # 프로젝트 개요
