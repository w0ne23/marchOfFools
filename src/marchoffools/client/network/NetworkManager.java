package marchoffools.client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import marchoffools.client.Frame;
import marchoffools.client.core.Scene;
import marchoffools.client.scenes.LobbyScene;
import marchoffools.common.protocol.MessageType;
import marchoffools.common.protocol.Packet;
import marchoffools.server.game.Room;
import marchoffools.common.message.*;

public class NetworkManager {
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private NetworkThread networkThread;
    private NetworkListener listener;
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
            
            System.out.println("===== 연결 시도 시작 ====="); 
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
                
            case GAME_INPUT:
                handleGameInput((GameInputMessage) packet.getData());
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
    	System.out.println("=== RoomInfo 수신 ===");
        System.out.println("Room ID: " + msg.getRoomId());
        System.out.println("Status: " + msg.getStatus());
        System.out.println("Players: " + msg.getPlayers().size());
        System.out.println("Can Start: " + msg.isCanStart());
        
        // 게임 시작 상태 체크
        if (msg.getStatus() == Room.STATUS_PLAYING) {
            handleGameStartFromRoomInfo(msg);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            if (listener != null) {
                // listener가 있으면 전달
                listener.onRoomInfo(msg);
            } else {
                // listener가 없으면 Scene 전환 시도
                System.out.println("Warning: No NetworkListener, transitioning to LobbyScene and delivering RoomInfo");
                
                Scene currentScene = frame.getCurrentScene();
                
                if (!(currentScene instanceof LobbyScene)) {
                    // LobbyScene이 아니면 전환
                    LobbyScene lobbyScene = new LobbyScene();
                    frame.switchScene(lobbyScene);
                    // Scene 전환 직후 RoomInfo 전달
                    lobbyScene.updateRoomInfo(msg);
                } else {
                    // 이미 LobbyScene인데 listener가 없는 경우
                    System.err.println("Error: LobbyScene exists but listener not set");
                    ((LobbyScene) currentScene).updateRoomInfo(msg);
                }
            }
        });
    }
    
    private void handleGameStartFromRoomInfo(RoomInfoMessage msg) {
        System.out.println("=== Game Starting (from RoomInfo) ===");
        
        if (listener != null) {
            SwingUtilities.invokeLater(() -> {
                // LobbyScene이면 onGameStart 호출 (별도 처리)
                if (listener instanceof marchoffools.client.scenes.LobbyScene) {
                    ((marchoffools.client.scenes.LobbyScene) listener).onGameStart();
                }
            });
        }
    }
    
    private void handleChat(ChatMessage msg) {
    	System.out.println("Chat 수신: " + msg.getSenderName() + ": " + msg.getContent());
        
    	if (listener != null) {
            SwingUtilities.invokeLater(() -> {
                listener.onChat(msg);
            });
        } else {
            System.out.println("Warning: No NetworkListener set for Chat");
        }
    }
    
    private void handleGameInput(GameInputMessage msg) {
        System.out.println("GameInput 수신: type=" + msg.getInputType());
        
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onGameInput(msg));
        } else {
            System.out.println("Warning: No NetworkListener set for GameInput");
        }
    }
    
    private void handleGameState(GameStateMessage msg) {
        System.out.println("GameState 수신: distance=" + msg.getDistance());
        
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onGameState(msg));
        } else {
            System.out.println("Warning: No NetworkListener set for GameState");
        }
    }
    
    private void handleGameResult(GameResultMessage msg) {
        System.out.println("GameResult 수신: score=" + msg.getTotalScore());
        
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onGameResult(msg));
        } else {
            System.out.println("Warning: No NetworkListener set for GameResult");
        }
    }
    
    private void showError(int code, String message) {
        String title = "오류";
        
        // 오류 코드별 제목 설정
        if (code >= ResponseMessage.CATEGORY_ROOM_ERROR_START && 
                code < ResponseMessage.CATEGORY_ROOM_ERROR_END) {
                title = "방 오류";
                
        } else if (code >= ResponseMessage.CATEGORY_GAME_ERROR_START && 
                   code < ResponseMessage.CATEGORY_GAME_ERROR_END) {
            title = "게임 오류";
                
        } else if (code >= ResponseMessage.CATEGORY_ROLE_ERROR_START && 
                   code < ResponseMessage.CATEGORY_ROLE_ERROR_END) {
            title = "역할 오류";
                
        } else if (code >= ResponseMessage.CATEGORY_SERVER_ERROR_START) {
            title = "서버 오류";
                
        } else if (code == ResponseMessage.NO_AVAILABLE_ROOM || 
                   code == ResponseMessage.MATCHMAKING_TIMEOUT) {
            title = "매칭 오류";
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
    
    public NetworkListener getListener() {
        return listener;
    }

    public void setListener(NetworkListener listener) {
        this.listener = listener;
        System.out.println("NetworkListener set: " + (listener != null ? listener.getClass().getSimpleName() : "null"));
    }
}