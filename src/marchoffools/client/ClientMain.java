package marchoffools.client;

import marchoffools.client.core.Assets;

public class ClientMain {
    public static void main(String[] args) {
    	Assets.Characters.load();
    	
        new Frame();
    }
}