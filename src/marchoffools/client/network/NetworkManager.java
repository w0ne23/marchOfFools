package marchoffools.client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;

import javax.swing.JOptionPane;

import marchoffools.client.Frame;
import marchoffools.common.protocol.MessageType;
import marchoffools.common.protocol.Packet;
import marchoffools.common.message.*;

public class NetworkManager {
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private NetworkThread networkThread;
    private Frame frame;
    
    private String playerId;
    private String playerName;
    private boolean connected;
    
    public NetworkManager(Frame frame) {
        this.frame = frame;
        this.playerId = UUID.randomUUID().toString();
        this.connected = false;
    }
    
    public boolean connect(String host, int port, String playerName) {
        try {
            // 소켓 생성 및 연결
            socket = new Socket();
            SocketAddress sa = new InetSocketAddress(host, port);
            
            System.out.println("===== 연결 시도 시작 =====");  // ✅ 추가
            System.out.println("서버 주소: " + host);
            System.out.println("포트: " + port);
            System.out.println("플레이어 이름: " + playerName);
            System.out.println("========================");
            
            System.out.println("서버 연결 시도: " + host + ":" + port);
            
            socket.connect(sa, 3000);  // 3초 타임아웃
            
            this.playerName = playerName;
            
            System.out.println("✓ 소켓 연결 성공!");
            
            // 스트림 초기화
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("서버 연결 성공: " + host + ":" + port);
            
            // 수신 스레드 시작
            networkThread = new NetworkThread(this, in);
            networkThread.start();

            connected = true;
            
            // CONNECT 메시지 전송
            RoomActionMessage connectMsg = new RoomActionMessage(
                playerId, 
                RoomActionMessage.CONNECT
            );
            connectMsg.setPlayerName(playerName);
            
            sendMessage(MessageType.ROOM_ACTION, connectMsg);
            
            return true;
            
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
            
            JOptionPane.showMessageDialog(
                frame,
                "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인하세요.",
                "연결 실패",
                JOptionPane.ERROR_MESSAGE
            );
            
            return false;
        }
    }
    
    public void disconnect() {
        if (!connected) return;
        
        try {
            // DISCONNECT 메시지 전송
            RoomActionMessage disconnectMsg = new RoomActionMessage(
                playerId, 
                RoomActionMessage.DISCONNECT
            );
            disconnectMsg.setPlayerName(playerName);
            
            sendMessage(MessageType.ROOM_ACTION, disconnectMsg);
            
            // 잠시 대기 (메시지 전송 완료)
            Thread.sleep(100);
            
            // 연결 종료
            if (networkThread != null) {
                networkThread.stopThread();
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            
            connected = false;
            System.out.println("서버 연결 종료");
            
        } catch (IOException | InterruptedException e) {
            System.err.println("연결 종료 오류: " + e.getMessage());
        }
    }
    
    public void sendMessage(MessageType type, Object data) {
        if (!connected) {
            System.err.println("서버에 연결되어 있지 않습니다");
            return;
        }
        
        try {
            // Packet 생성 (2-파라미터 생성자)
            Packet packet = new Packet(type, data);
            out.writeObject(packet);
            out.flush();
            
            System.out.println("메시지 전송: " + type);
            
        } catch (IOException e) {
            System.err.println("메시지 전송 실패: " + e.getMessage());
            disconnect();
        }
    }
    
    public void handlePacket(Packet packet) {
        System.out.println("패킷 수신: " + packet.getType());
        
        MessageType type = packet.getType();
        
        switch (type) {
            case RESPONSE:
                handleResponse((ResponseMessage) packet.getData());
                break;
                
            case ROOM_INFO:
                handleRoomInfo((RoomInfoMessage) packet.getData());
                break;
                
            case CHAT:
                handleChat((ChatMessage) packet.getData());
                break;
                
            case GAME_STATE:
                handleGameState((GameStateMessage) packet.getData());
                break;
                
            case GAME_RESULT:
                handleGameResult((GameResultMessage) packet.getData());
                break;
                
            default:
                System.out.println("알 수 없는 메시지 타입: " + type);
        }
    }
    
    private void handleResponse(ResponseMessage msg) {
        if (msg.isSuccess()) {
            System.out.println("✓ 성공: " + msg.getMessage());
        } else {
            System.err.println("✗ 오류 [" + msg.getCode() + "]: " + msg.getMessage());
            
            // 사용자에게 오류 표시
            showError(msg.getCode(), msg.getMessage());
        }
    }
    
    private void handleRoomInfo(RoomInfoMessage msg) {
        System.out.println("방 정보 수신: " + msg);
        // TODO: Phase 2 - Scene에 전달
    }
    
    private void handleChat(ChatMessage msg) {
        System.out.println("채팅 수신: " + msg.getSenderName() + ": " + msg.getContent());
        // TODO: Phase 3 - 채팅창에 표시
    }
    
    private void handleGameState(GameStateMessage msg) {
        System.out.println("게임 상태 수신: 거리=" + msg.getDistance());
        // TODO: Phase 4 - 게임 화면 업데이트
    }
    
    private void handleGameResult(GameResultMessage msg) {
        System.out.println("게임 결과 수신: 점수=" + msg.getTotalScore());
        // TODO: Phase 4 - 결과 화면 표시
    }
    
    private void showError(int code, String message) {
        String title = "오류";
        
        // 오류 코드별 제목 설정
        if (code >= 420 && code < 430) {
            title = "방 오류";
        } else if (code >= 430 && code < 440) {
            title = "게임 오류";
        } else if (code >= 440 && code < 450) {
            title = "역할 오류";
        } else if (code >= 500) {
            title = "서버 오류";
        }
        
        JOptionPane.showMessageDialog(
            frame,
            message,
            title,
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    // Getters
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public Frame getFrame() {
        return frame;
    }
}