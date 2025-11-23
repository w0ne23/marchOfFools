package marchoffools.server;

import marchoffools.server.network.GameServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 12345;
        
        System.out.println("=================================");
        System.out.println("바보들의 행진 게임 서버");
        System.out.println("=================================");
        
        new GameServer(port);
    }
}