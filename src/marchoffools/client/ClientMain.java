package marchoffools.client;

import marchoffools.client.core.ResourceManager;

public class ClientMain {
    public static void main(String[] args) {
    	ResourceManager.loadAllResources();
    	
        new Frame();
    }
}