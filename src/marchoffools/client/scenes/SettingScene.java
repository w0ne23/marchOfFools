package marchoffools.client.scenes;

import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class SettingScene extends Scene {

    private JPanel pEmotionContent, pAudioContent, pControlContent;
    private JLabel lContentTitle;
    
    private static final String MENU_EMOTION = "EMOTION";
    private static final String MENU_AUDIO = "AUDIO";
    private static final String MENU_CONTROL = "CONTROL";
    
    public SettingScene() {
        super(DEFAULT);
        setLayout(null);
        
        createFixedUiElements(); 
        createMenuPanel();      
        createContentPanels();
        
        showContent(MENU_EMOTION);
        
        setFocusable(false);
    }
    
    private void createFixedUiElements() {
        JLabel lMainTitle = new JLabel("설정");
        lMainTitle.setFont(getFont().deriveFont(42f));
        lMainTitle.setForeground(BLACK);
        lMainTitle.setSize(lMainTitle.getPreferredSize().width + 50, 60);
        lMainTitle.setLocation(72, 48);
        add(lMainTitle);

        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setBounds(WINDOW_WIDTH - 172, 40, 100, 50);
        bExit.addActionListener(e -> {
            // TODO: 서버 나가기 요청
            goBack();
        });
        add(bExit);

        lContentTitle = new JLabel("", SwingConstants.CENTER);
        lContentTitle.setFont(getFont().deriveFont(32f));
        lContentTitle.setBounds(320, 140, WINDOW_WIDTH - 400, 50);
        add(lContentTitle);
    }
    
    private void createMenuPanel() {
        JPanel mp = new JPanel(new GridLayout(3, 1, 0, 20));
        mp.setBounds(100, WINDOW_HEIGHT / 3, 200, 300);
        mp.setOpaque(false);
        
        mp.add(createMenuButton("오디오", e -> showContent(MENU_AUDIO)));
        mp.add(createMenuButton("조작", e -> showContent(MENU_CONTROL)));
        mp.add(createMenuButton("게임", e -> showContent(MENU_EMOTION)));
        
        add(mp);
    }
    
    private Button createMenuButton(String text, java.awt.event.ActionListener action) {
        Button btn = new Button(text);
        btn.addActionListener(action);
        return btn;
    }
    
    private void createContentPanels() {
    	int contentX = 320;
        int contentY = 220; 
        int contentW = WINDOW_WIDTH - 400;
        int contentH = WINDOW_HEIGHT - 300; 

        // 3-1. 감정표현 패널 
        pEmotionContent = new JPanel(new GridBagLayout()); 
        pEmotionContent.setBounds(contentX, contentY, contentW, contentH);
        pEmotionContent.setOpaque(false);
        
        JPanel emojiWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        emojiWrapper.setOpaque(false);
        
        String[] emojis = {"👍", "👎", "😡", "🤗", "👏", "👊", "🤮", "💩"};
        for (String emoji : emojis) {
            Button bEmoji = new Button(emoji);
            bEmoji.setFont(getFont().deriveFont(40f));
            bEmoji.setPreferredSize(new Dimension(90, 90));
            bEmoji.addActionListener(e -> {
                System.out.println("감정 표현 선택: " + emoji);
            });
            emojiWrapper.add(bEmoji);
        }
        
        pEmotionContent.add(emojiWrapper);
        add(pEmotionContent);

        // 3-2. 오디오 패널 
        pAudioContent = createPlaceholderPanel("오디오 설정 내용이 여기에 표시됩니다", contentX, contentY, contentW, contentH);
        add(pAudioContent);

        // 3-3. 조작 패널 
        pControlContent = createPlaceholderPanel("조작 설정 내용이 여기에 표시됩니다", contentX, contentY, contentW, contentH);
        add(pControlContent);
    }
    
    private JPanel createPlaceholderPanel(String text, int x, int y, int w, int h) {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setBounds(x, y, w, h);
        p.setOpaque(false);
        
        JLabel lPlaceholder = new JLabel(text);
        lPlaceholder.setFont(getFont().deriveFont(24f));
        p.add(lPlaceholder);
        
        return p;
    }
    
    // 4. 화면 전환 로직
    private void showContent(String menuType) {
        pEmotionContent.setVisible(false);
        pAudioContent.setVisible(false);
        pControlContent.setVisible(false);
        
        switch (menuType) {
            case MENU_EMOTION:
                lContentTitle.setText("감정표현 설정");
                pEmotionContent.setVisible(true);
                break;
            case MENU_AUDIO:
                lContentTitle.setText("오디오 설정");
                pAudioContent.setVisible(true);
                break;
            case MENU_CONTROL:
                lContentTitle.setText("조작 설정");
                pControlContent.setVisible(true);
                break;
        }
        
        revalidate();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}
