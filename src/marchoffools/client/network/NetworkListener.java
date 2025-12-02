package marchoffools.client.network;

import marchoffools.common.message.ChatMessage;
import marchoffools.common.message.RoomInfoMessage;
import marchoffools.common.message.GameInputMessage;
import marchoffools.common.message.GameStateMessage;
import marchoffools.common.message.GameResultMessage;

/**
 * 서버로부터 메시지를 받는 Scene이 구현하는 인터페이스
 */

public interface NetworkListener {
	
	// === 대기실 관련 메시지 ===
    
    // 방 정보 업데이트 수신 (플레이어 입장/퇴장, 역할 변경, 준비 상태 등)
    default void onRoomInfo(RoomInfoMessage msg) {
        // 기본 구현: 아무것도 안 함 (필요한 Scene만 오버라이드)
    }
    
    // 채팅 메시지 수신
    default void onChat(ChatMessage msg) {
        // 기본 구현: 아무것도 안 함
    }
    
    
    /* === 게임 관련 메시지 === */
    
    // 게임 입력 메시지 수신 (감정 표현, 스킬 등)
    default void onGameInput(GameInputMessage msg) {}
    
    // 게임 상태 업데이트 수신 (타이머, 점수 등)
    default void onGameState(GameStateMessage msg) {}
    
    //게임 결과 수신
    default void onGameResult(GameResultMessage msg) {}
}