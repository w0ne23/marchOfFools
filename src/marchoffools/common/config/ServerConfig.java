package marchoffools.common.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ServerConfig {
    
    private static final String CONFIG_FILE = "server.txt";
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;
    
    private String host;
    private int port;
    
    public ServerConfig() {
        loadConfig();
    }
    
    private void loadConfig() {
        File configFile = findConfigFile();
        
        if (configFile == null || !configFile.exists()) {
            System.out.println("server.txt 파일을 찾을 수 없습니다. 기본값 사용: " + DEFAULT_HOST + ":" + DEFAULT_PORT);
            this.host = DEFAULT_HOST;
            this.port = DEFAULT_PORT;
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String hostLine = reader.readLine();
            if (hostLine != null && !hostLine.trim().isEmpty()) {
                this.host = hostLine.trim();
            } else {
                this.host = DEFAULT_HOST;
            }
            
            String portLine = reader.readLine();
            if (portLine != null && !portLine.trim().isEmpty()) {
                try {
                    this.port = Integer.parseInt(portLine.trim());
                } catch (NumberFormatException e) {
                    System.err.println("포트 번호 파싱 오류. 기본값 사용: " + DEFAULT_PORT);
                    this.port = DEFAULT_PORT;
                }
            } else {
                this.port = DEFAULT_PORT;
            }
            
            System.out.println("서버 설정 로드 완료: " + this.host + ":" + this.port);
            
        } catch (IOException e) {
            System.err.println("server.txt 읽기 오류: " + e.getMessage());
            System.out.println("기본값 사용: " + DEFAULT_HOST + ":" + DEFAULT_PORT);
            this.host = DEFAULT_HOST;
            this.port = DEFAULT_PORT;
        }
    }
    
    /**
     * server.txt 파일 위치 탐색
     * 1순위: 현재 작업 디렉토리
     * 2순위: JAR 파일이 있는 디렉토리
     * 3순위: 사용자 홈 디렉토리
     */
    private File findConfigFile() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            System.out.println("설정 파일 발견: " + file.getAbsolutePath());
            return file;
        }
        
        try {
            String jarPath = ServerConfig.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();
            File jarDir = new File(jarPath).getParentFile();
            file = new File(jarDir, CONFIG_FILE);
            if (file.exists()) {
                System.out.println("설정 파일 발견: " + file.getAbsolutePath());
                return file;
            }
        } catch (Exception e) {
        }
        
        file = new File(System.getProperty("user.home"), CONFIG_FILE);
        if (file.exists()) {
            System.out.println("설정 파일 발견: " + file.getAbsolutePath());
            return file;
        }
        
        return null;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    private static ServerConfig instance;
    
    public static ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }
    
    public static String getServerHost() {
        return getInstance().getHost();
    }
    
    public static int getServerPort() {
        return getInstance().getPort();
    }
}