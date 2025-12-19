package marchoffools.server.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Vector;

import marchoffools.server.game.Room;
import marchoffools.server.game.RoomManager;
import marchoffools.server.ui.ServerEventListener;
import marchoffools.server.ui.ServerLogger;

public class GameServer {
    
    private static final ServerLogger logger = ServerLogger.getInstance();
    
    private int port;
    private ServerSocket serverSocket;
    private Vector<ClientHandler> clients;
    private Thread acceptThread;
    private RoomManager roomManager;
    
    // UI 이벤트 리스너 (옵션)
    private ServerEventListener eventListener;
    
    // 서버 상태
    private boolean running = false;
    
    /**
     * 기존 생성자 (호환성 유지)
     */
    public GameServer(int port) {
        this(port, null);
    }
    
    /**
     * UI 리스너를 받는 생성자
     */
    public GameServer(int port, ServerEventListener listener) {
        this.port = port;
        this.eventListener = listener;
        this.clients = new Vector<>();
        this.roomManager = new RoomManager();
        
        startServer();
    }
    
    private void startServer() {
        acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                acceptClients();
            }
        });
        acceptThread.start();
    }
    
    private void acceptClients() {
        Socket clientSocket = null;
        
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            logger.info("=================================");
            logger.info("바보들의 행진 게임 서버");
            logger.info("서버 주소: " + hostAddress + ":" + port);
            logger.info("=================================");
            logger.info("클라이언트 연결 대기 중...");
            
            while (acceptThread == Thread.currentThread()) {
                clientSocket = serverSocket.accept();
                
                String clientAddr = clientSocket.getInetAddress().getHostAddress();
                logger.info("새 클라이언트 연결: " + clientAddr);
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                
                synchronized (clients) {
                    clients.add(handler);
                    logger.info("현재 접속자 수: " + clients.size());
                }
                
                handler.start();
            }
            
        } catch (SocketException e) {
            if (running) {
                logger.warn("서버 소켓 종료: " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error("서버 오류: " + e.getMessage());
        } finally {
            running = false;
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                logger.error("서버 닫기 오류: " + e.getMessage());
            }
        }
    }
    
    public void stopServer() {
        logger.info("서버 종료 요청...");
        running = false;
        
        try {
            acceptThread = null;
            
            // 모든 클라이언트 연결 종료
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.disconnect();
                }
                clients.clear();
            }
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            logger.info("서버가 종료되었습니다.");
            
        } catch (IOException e) {
            logger.error("서버 소켓 닫기 오류: " + e.getMessage());
        }
    }
    
    public void removeClient(ClientHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
            logger.info("클라이언트 연결 종료. 현재 접속자 수: " + clients.size());
        }
    }
    
    // ========== UI 이벤트 알림 메서드 ==========
    
    public void notifyClientConnected(String clientId, String clientName, String ip) {
        if (eventListener != null) {
            eventListener.onClientConnected(clientId, clientName, ip);
        }
    }
    
    public void notifyClientDisconnected(String clientId, String clientName) {
        if (eventListener != null) {
            eventListener.onClientDisconnected(clientId, clientName);
        }
    }
    
    public void notifyRoomCreated(String roomId, String hostName) {
        if (eventListener != null) {
            eventListener.onRoomCreated(roomId, hostName);
        }
    }
    
    public void notifyRoomRemoved(String roomId) {
        if (eventListener != null) {
            eventListener.onRoomRemoved(roomId);
        }
        // 방 로그도 정리
        ServerLogger.getInstance().clearRoomLogs(roomId);
    }
    
    public void notifyRoomUpdated(Room room) {
        if (eventListener != null) {
            eventListener.onRoomUpdated(room);
        }
    }
    
    public void notifyGameStarted(String roomId) {
        if (eventListener != null) {
            eventListener.onGameStarted(roomId);
        }
    }
    
    public void notifyGameEnded(String roomId, int score) {
        if (eventListener != null) {
            eventListener.onGameEnded(roomId, score);
        }
    }
    
    // ========== Getters ==========
    
    public Vector<ClientHandler> getClients() {
        return clients;
    }
    
    public RoomManager getRoomManager() {
        return roomManager;
    }
    
    public int getPort() {
        return port;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * 현재 게임 중인 방 수
     */
    public int getPlayingRoomCount() {
        int count = 0;
        for (Room room : roomManager.getAllRooms()) {
            if (room.isPlaying()) {
                count++;
            }
        }
        return count;
    }
}