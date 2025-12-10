package marchoffools.client.core;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioManager {

    private static AudioManager instance;
    private Clip bgmClip; 

    private AudioManager() {}

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    // BGM (배경음악)
    public synchronized void playBgm(String path) {
        stopBgm();

        try (InputStream rawStream = getInputStream(path);
             AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(rawStream))) {

            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);

            setBgmVolume(Config.BGM_VOLUME);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY); 
            bgmClip.start();

        } catch (UnsupportedAudioFileException e) {
            System.err.println("오디오 포맷 미지원: " + path);
        } catch (IOException e) {
            System.err.println("오디오 파일을 읽을 수 없음: " + path);
        } catch (LineUnavailableException e) {
            System.err.println("오디오 장치 사용 불가 (다른 프로그램이 사용 중일 수 있음)");
        } catch (IllegalArgumentException e) {
            System.err.println("시스템이 지원하지 않는 인코딩이거나 파일 문제: " + e.getMessage());
        }
    }

    public synchronized void stopBgm() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.flush(); 
            bgmClip.close(); 
            bgmClip = null;
        }
    }

    public void setBgmVolume(int volume) {
        if (bgmClip != null && bgmClip.isOpen()) {
            setClipVolume(bgmClip, volume);
        }
    }

	// SFX (효과음)
    public void playSfx(String path) {
        new Thread(() -> { 
            try (InputStream rawStream = getInputStream(path);
                 AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(rawStream))) {

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
                System.err.println("효과음 재생 실패 (" + path + "): " + e.getMessage());
            }
        }).start();
    }

    // 파일 로딩
    private InputStream getInputStream(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is != null) {
            return is;
        }
        
        throw new IOException("파일을 찾을 수 없습니다: " + path);
    }

    // 볼륨 변환 로직 
    private void setClipVolume(Clip clip, int volume) {
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
            
            if (db < min) db = min;
            if (db > max) db = max;

            gainControl.setValue(db);
        }
    }
}