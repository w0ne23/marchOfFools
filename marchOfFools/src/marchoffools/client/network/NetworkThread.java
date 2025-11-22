package marchoffools.client.network;

import java.io.IOException;
import java.io.ObjectInputStream;

import marchoffools.common.protocol.Packet;

public class NetworkThread extends Thread {
    
    private ClientSocket clientSocket;
    private ObjectInputStream in;
    private boolean running;
    
    public NetworkThread(ClientSocket clientSocket, ObjectInputStream in) {
        this.clientSocket = clientSocket;
        this.in = in;
        this.running = true;
    }
    
    @Override
    public void run() {
        System.out.println("네트워크 수신 스레드 시작");
        
        try {
            while (running) {
                Packet packet = (Packet) in.readObject();
                clientSocket.handlePacket(packet);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("네트워크 수신 오류: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("패킷 역직렬화 오류: " + e.getMessage());
        }
        
        System.out.println("네트워크 수신 스레드 종료");
    }
    
    public void stopThread() {
        running = false;
    }
}