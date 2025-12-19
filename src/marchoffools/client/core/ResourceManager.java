package marchoffools.client.core;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.*;
import javax.swing.ImageIcon;

public class ResourceManager {
    
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Map<String, byte[]> audioCache = new HashMap<>();  
    
    private static Clip bgmClip;  
    
    // 게임 시작 전에 이 메서드를 한 번만 호출
    public static void loadAllResources() {
        try {
            loadImage("player", "/assets/testCharacter.png");
            
            for (marchoffools.common.model.GameEntity entity : 
                marchoffools.common.model.GameEntity.values()) {
                loadImage(entity.name(), entity.getImagePath());
            }
            
            // 배경 이미지
            loadImage("background_default", Assets.Backgrounds.DEFAULT);
            
            // 오디오 로드
            loadAudio("bgm_lobby", Assets.Sounds.BGM_LOBBY);
            
            // 캐릭터 이미지
            loadImage("knight_warrior", Assets.Characters.KNIGHT_WARRIOR);
            loadImage("knight_archer", Assets.Characters.KNIGHT_ARCHER);
            loadImage("knight_axe", Assets.Characters.KNIGHT_AXE);
            loadImage("horse_unicorn", Assets.Characters.HORSE_UNICORN);
            loadImage("horse_griffin", Assets.Characters.HORSE_GRIFFIN);
            loadImage("horse_dragon", Assets.Characters.HORSE_DRAGON);
            
            System.out.println("All resources loaded successfully.");
        } catch (Exception e) {
            System.err.println("Resource loading failed: " + e.getMessage());
        }
    }
    
    // 이미지 로딩
    private static void loadImage(String key, String path) {
        try {
            java.net.URL url = ResourceManager.class.getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                imageCache.put(key, img);
                System.out.println("이미지 로드 성공: " + key);
            } else {
                System.err.println("이미지를 찾을 수 없음: " + path);
            }
        } catch (Exception e) {
            System.err.println("이미지 로드 오류 " + path + ": " + e.getMessage());
        }
    }
    
    // 오디오 로딩
    private static void loadAudio(String key, String path) {
        try {
            java.net.URL url = ResourceManager.class.getResource(path);
            if (url != null) {
                try (java.io.InputStream is = url.openStream();
                     java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    audioCache.put(key, baos.toByteArray());
                    System.out.println("오디오 로드 성공: " + key);
                }
            } else {
                System.err.println("오디오를 찾을 수 없음: " + path);
            }
        } catch (Exception e) {
            System.err.println("오디오 로드 오류 " + path + ": " + e.getMessage());
        }
    }
    
    // ========== 이미지 관련 ==========
    
    public static Image getImage(String key) {
        return imageCache.get(key);
    }
    
    public static ImageIcon getScaledIcon(String key, int width, int height) {
        Image img = imageCache.get(key);
        if (img != null) {
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }
    
    // ========== BGM 관련 ==========
    
    public static synchronized void playBGM(String key) {
        stopBGM();  // 기존 BGM 정지
        
        byte[] audioData = audioCache.get(key);
        if (audioData == null) {
            System.err.println("오디오를 찾을 수 없음: " + key);
            return;
        }
        
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(bais));
            
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            setBGMVolume(Config.BGM_VOLUME);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            
        } catch (Exception e) {
            System.err.println("BGM 재생 오류: " + e.getMessage());
        }
    }
    
    public static synchronized void stopBGM() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.flush();
            bgmClip.close();
            bgmClip = null;
        }
    }
    
    public static void setBGMVolume(int volume) {
        if (bgmClip != null && bgmClip.isOpen()) {
            setClipVolume(bgmClip, volume);
        }
    }
    
    // ========== SFX 관련 ==========
    
    public static void playSFX(String key) {
        byte[] audioData = audioCache.get(key);
        if (audioData == null) {
            System.err.println("오디오를 찾을 수 없음: " + key);
            return;
        }
        
        new Thread(() -> {
            try {
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(audioData);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(bais));
                
                Clip sfxClip = AudioSystem.getClip();
                sfxClip.open(audioStream);
                setClipVolume(sfxClip, Config.SFX_VOLUME);
                
                sfxClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                });
                
                sfxClip.start();
                
            } catch (Exception e) {
                System.err.println("SFX 재생 오류: " + e.getMessage());
            }
        }).start();
    }
    
    // ========== 볼륨 조절 ==========
    
    private static void setClipVolume(Clip clip, int volume) {
        if (clip == null || !clip.isOpen()) return;
        
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            if (volume <= 0) {
                gainControl.setValue(gainControl.getMinimum());
                return;
            }
            
            float db = (float) (20f * Math.log10(volume / 100.0));
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            
            db = Math.max(min, Math.min(max, db));
            gainControl.setValue(db);
        }
    }
}