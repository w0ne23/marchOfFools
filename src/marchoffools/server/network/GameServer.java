package marchoffools.server.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import marchoffools.server.game.RoomManager;

public class GameServer {
    
    private int port;
    private ServerSocket serverSocket;
    private Vector<ClientHandler> clients;
    private Thread acceptThread;
    private RoomManager roomManager;
    
    public GameServer(int port) {
        this.port = port;
        this.clients = new Vector<>();
        this.roomManager = new RoomManager();
        
        startServer();
    }
    
    private void startServer() { // 서버 시작 및 클라이언트 수락 스레드 초기화, 시작
        acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                acceptClients();
            }
        });
        acceptThread.start();
    }
    
    private void acceptClients() { // 클라이언트 연결 수락, 처리
        Socket clientSocket = null;
        
        try {
            serverSocket = new ServerSocket(port);
            
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("서버가 시작되었습니다: " + hostAddress + ":" + port);
            System.out.println("클라이언트 연결 대기 중...\n");
            
            while (acceptThread == Thread.currentThread()) {
                clientSocket = serverSocket.accept();
                
                String clientAddr = clientSocket.getInetAddress().getHostAddress();
                System.out.println("새 클라이언트 연결: " + clientAddr);
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                
                synchronized (clients) {
                    clients.add(handler);
                    System.out.println("현재 접속자 수: " + clients.size());
                }
                
                handler.start();
            }
            
        } catch (SocketException e) {
            System.out.println("서버 소켓 종료");
            System.err.println("상세 오류: " + e.getMessage());  
            e.printStackTrace();  
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("서버 닫기 오류: " + e.getMessage());
            }
        }
    }
    
    public void stopServer() { // 서버 안전 종료
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
            
            System.out.println("서버가 종료되었습니다.");
            
        } catch (IOException e) {
            System.err.println("서버 소켓 닫기 오류: " + e.getMessage());
        }
    }
    
    public void removeClient(ClientHandler handler) {
        synchronized (clients) { // 클라이언트 목록 접근 시 동기화 처리
            clients.remove(handler);
            System.out.println("클라이언트 연결 종료. 현재 접속자 수: " + clients.size());
        }
    }
    
    public Vector<ClientHandler> getClients() {
        return clients;
    }
    
    public RoomManager getRoomManager() {
        return roomManager;
    }
}