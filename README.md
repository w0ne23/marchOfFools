# marchOfFools

```
📁 전체 프로젝트 구조

marchOfFools/
└── src/
    └── marchoffools/
        │
        ├── MarchOfFools.java                    # 프로그램 진입점
        │
        ├── client/                               # 클라이언트 패키지
        │   ├── ClientMain.java                  # 클라이언트 메인
        │   │
        │   ├── Frame.java                       # 메인 프레임
        │   │
        │   ├── game/                            # 게임 로직 [역할 B]
        │   │   ├── GameState.java               # 게임 상태 관리
        │   │   ├── Horse.java                   # 기마 
        │   │   ├── Item.java                    # 아이템 
        │   │   ├── Knight.java                  # 기사
        │   │   ├── Obstacle.java                # 방해물
        │   │   ├── ObstacleManager.java         # 방해물 생성/관리
        │   │   └── Player.java                  # 플레이어
        │   │
        │   ├── handler/                         # 메시지 처리 [역할 B]
        │   │   ├── ChatHandler.java             # 채팅 처리 
        │   │   ├── GameResultHandler.java       # 게임 결과 처리 
        │   │   ├── GameStateHandler.java        # 게임 상태 처리
        │   │   ├── MessageHandler.java          # 메시지 처리 라우터
        │   │   ├── ResponseHandler.java         # 응답/에러 처리
        │   │   └── RoomInfoHandler.java         # 방 정보 처리
        │   │
        │   ├── network/                         # 네트워크 통신 [역할 A]
        │   │   ├── ClientSocket.java            # 서버 연결 관리
        │   │   ├── MessageSender.java           # 메시지 전송 유틸
        │   │   └── NetworkThread.java           # 메시지 수신 스레드
        │   │
        │   ├── scene/                           # 게임 화면
        │   │   ├── GameScene.java               # 게임 플레이 [역할 B] 
        │   │   ├── LobbyScene.java              # 대기실 [역할 A] 
        │   │   ├── ResultScene.java             # 결과 화면 [역할 B] 
        │   │   ├── RoomSelectScene.java         # 방 선택 화면 [역할 A]
        │   │   ├── TitleScene.java              # 타이틀 화면
        │   │   ├── TutorialScene.java           # 튜토리얼
        │   │   ├── Scene.java                   # Scene 기본 클래스
        │   │   ├── SceneContext.java            # Scene 인터페이스
        │   │   ├── ScoreboardScene.java         # 스코어보드 [역할 B]
        │   │   └── SettingsScene.java           # 설정
        │   │
        │   ├── ui/                              # UI 컴포넌트
        │   │   ├── Button.java                  # 버튼
        │   │   ├── ChatBox.java                 # 채팅창 [역할 A]
        │   │   ├── EmotionPanel.java            # 감정 표현 패널
        │   │   └── GameHUD.java                 # 게임 내 UI [역할 B]
        │   │
        │   └── util/                            # 유틸리티
        │       ├── Assets.java                  # 리소스 관리
        │       ├── Config.java                  # 설정
        │       └── SoundManager.java            # 사운드 관리
        │
        ├── server/                              # 서버 패키지
        │   ├── ServerMain.java                  # 서버 메인
        │   │
        │   ├── network/                         # 서버 네트워크 [역할 A]
        │   │   ├── GameServer.java              # 메인 서버
        │   │   ├── ClientHandler.java           # 클라이언트 핸들러 (Thread)
        │   │   ├── ConnectionManager.java       # 연결 관리
        │   │   └── MessageRouter.java           # 메시지 라우팅
        │   │
        │   ├── handler/                         # 메시지 처리
        │   │   ├── RoomActionHandler.java       # 방 액션 처리 [역할 A]
        │   │   ├── GameInputHandler.java        # 게임 입력 처리 [역할 B]
        │   │   └── ChatMessageHandler.java      # 채팅 처리 [역할 A]
        │   │
        │   ├── game/                            # 게임 로직
        │   │   ├── RoomManager.java             # 방 관리 [역할 A]
        │   │   ├── Room.java                    # 방 객체 [역할 A]
        │   │   ├── Matchmaking.java             # 빠른 매칭 [역할 A]
        │   │   ├── GameSession.java             # 게임 세션 [역할 B]
        │   │   ├── GameSynchronizer.java        # 게임 동기화 [역할 B]
        │   │   └── ScoreCalculator.java         # 점수 계산 [역할 B]
        │   │
        │   └── util/                            # 서버 유틸리티
        │       ├── Logger.java                  # 로그 시스템
        │       └── ServerConfig.java            # 서버 설정
        │
        └── common/                              # 공통 패키지
            ├── message/                         # 메시지 클래스
            │   ├── ChatMessage.java             # 채팅
            │   ├── GameInputMessage.java        # 게임 입력
            │   ├── GameResultMessage.java       # 게임 결과
            │   ├── GameStateMessage.java        # 게임 상태
            │   ├── ResponseMessage.java         # 응답
            │   ├── RoomActionMessage.java       # 방 액션 (연결 포함)
            │   └── RoomInfoMessage.java         # 방 정보
            │
            ├── model/                           # 공통 모델
            │   └── PlayerInfo.java              # 플레이어 정보
            │
            └── protocol/                        # 프로토콜
                ├── MessageType.java             # 메시지 타입 enum
                ├── Packet.java                  # 패킷 구조
                └── Message.java                 # 메시지 기본 클래스
            
```
