//package marchoffools.client.scenes;
//
//import marchoffools.client.core.Scene;
//import marchoffools.client.ui.Button;
//
//import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
//import static marchoffools.client.core.Assets.Colors.*;
//import static marchoffools.client.core.Config.*;
//
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;
//
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.SwingConstants;
//
//public class SettingScene extends Scene {
//
//    private JPanel pEmotionContent, pAudioContent, pControlContent;
//    private JLabel lContentTitle;
//    
//    private static final String MENU_EMOTION = "EMOTION";
//    private static final String MENU_AUDIO = "AUDIO";
//    private static final String MENU_CONTROL = "CONTROL";
//    
//    public SettingScene() {
//        super(DEFAULT);
//        setLayout(null);
//        
//        createFixedUiElements(); 
//        createMenuPanel();      
//        createContentPanels();
//        
//        showContent(MENU_EMOTION);
//        
//        setFocusable(false);
//    }
//    
//    private void createFixedUiElements() {
//        JLabel lMainTitle = new JLabel("설정");
//        lMainTitle.setFont(getFont().deriveFont(42f));
//        lMainTitle.setForeground(BLACK);
//        lMainTitle.setSize(lMainTitle.getPreferredSize().width + 50, 60);
//        lMainTitle.setLocation(72, 48);
//        add(lMainTitle);
//
//        Button bExit = new Button("->");
//        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
//        bExit.setBounds(WINDOW_WIDTH - 172, 40, 100, 50);
//        bExit.addActionListener(e -> {
//            // TODO: 서버 나가기 요청
//            goBack();
//        });
//        add(bExit);
//
//        lContentTitle = new JLabel("", SwingConstants.CENTER);
//        lContentTitle.setFont(getFont().deriveFont(32f));
//        lContentTitle.setBounds(320, 140, WINDOW_WIDTH - 400, 50);
//        add(lContentTitle);
//        
//        JPanel headerLine = new JPanel();
//        headerLine.setBackground(LIGHT_GRAY);
//        headerLine.setBounds(72, 110, WINDOW_WIDTH - 144, 2); 
//        add(headerLine);
//    }
//    
//    private void createMenuPanel() {
//        JPanel mp = new JPanel(new GridLayout(3, 1, 0, 20));
//        mp.setBounds(100, WINDOW_HEIGHT / 3, 200, 300);
//        mp.setOpaque(false);
//        
//        mp.add(createMenuButton("오디오", e -> showContent(MENU_AUDIO)));
//        mp.add(createMenuButton("조작", e -> showContent(MENU_CONTROL)));
//        mp.add(createMenuButton("게임", e -> showContent(MENU_EMOTION)));
//        
//        add(mp);
//    }
//    
//    private Button createMenuButton(String text, java.awt.event.ActionListener action) {
//        Button btn = new Button(text);
//        btn.addActionListener(action);
//        return btn;
//    }
//    
//    private void createContentPanels() {
//    	int contentX = 320;
//        int contentY = 220; 
//        int contentW = WINDOW_WIDTH - 400;
//        int contentH = WINDOW_HEIGHT - 300; 
//
//        // 3-1. 감정표현 패널 
//        pEmotionContent = new JPanel(new GridBagLayout()); 
//        pEmotionContent.setBounds(contentX, contentY, contentW, contentH);
//        pEmotionContent.setOpaque(false);
//        
//        JPanel emojiWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
//        emojiWrapper.setOpaque(false);
//        
//        String[] emojis = {"👍", "👎", "😡", "🤗", "👏", "👊", "🤮", "💩"};
//        for (String emoji : emojis) {
//            Button bEmoji = new Button(emoji);
//            bEmoji.setFont(getFont().deriveFont(40f));
//            bEmoji.setPreferredSize(new Dimension(90, 90));
//            bEmoji.addActionListener(e -> {
//                System.out.println("감정 표현 선택: " + emoji);
//            });
//            emojiWrapper.add(bEmoji);
//        }
//        
//        pEmotionContent.add(emojiWrapper);
//        add(pEmotionContent);
//
//        // 3-2. 오디오 패널 
//        pAudioContent = createPlaceholderPanel("오디오 설정 내용이 여기에 표시됩니다", contentX, contentY, contentW, contentH);
//        add(pAudioContent);
//
//        // 3-3. 조작 패널 
//        pControlContent = createPlaceholderPanel("조작 설정 내용이 여기에 표시됩니다", contentX, contentY, contentW, contentH);
//        add(pControlContent);
//    }
//    
//    private JPanel createPlaceholderPanel(String text, int x, int y, int w, int h) {
//        JPanel p = new JPanel(new GridBagLayout()); 
//        p.setBounds(x, y, w, h);
//        p.setOpaque(false);
//        
//        JLabel lPlaceholder = new JLabel(text);
//        lPlaceholder.setFont(getFont().deriveFont(24f));
//        p.add(lPlaceholder);
//        
//        return p;
//    }
//    
//    // 4. 화면 전환 로직
//    private void showContent(String menuType) {
//        pEmotionContent.setVisible(false);
//        pAudioContent.setVisible(false);
//        pControlContent.setVisible(false);
//        
//        switch (menuType) {
//            case MENU_EMOTION:
//                lContentTitle.setText("감정표현 설정");
//                pEmotionContent.setVisible(true);
//                break;
//            case MENU_AUDIO:
//                lContentTitle.setText("오디오 설정");
//                pAudioContent.setVisible(true);
//                break;
//            case MENU_CONTROL:
//                lContentTitle.setText("조작 설정");
//                pControlContent.setVisible(true);
//                break;
//        }
//        
//        revalidate();
//        repaint();
//    }
//
//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.setColor(TRANSLUCENT_WHITE);
//        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
//    }
//}

package marchoffools.client.scenes;

import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SettingScene extends Scene {

    private JPanel pEmotionContent, pAudioContent, pControlContent;
    
    private JPanel pContentBody; 
    private JLabel lContentTitle;

    private static final String MENU_EMOTION = "EMOTION";
    private static final String MENU_AUDIO = "AUDIO";
    private static final String MENU_CONTROL = "CONTROL";
    
    public SettingScene() {
        super(DEFAULT);
        setLayout(null);
        
        createFixedHeader(); 
        createMainLayout();
        createContentPanels();
        
        showContent(MENU_EMOTION);
        
        setFocusable(false);
    }
    
    private void createFixedHeader() {
        JLabel lMainTitle = new JLabel("설정");
        lMainTitle.setFont(getFont().deriveFont(42f));
        lMainTitle.setForeground(BLACK);
        lMainTitle.setSize(lMainTitle.getPreferredSize().width + 50, 60);
        lMainTitle.setLocation(72, 48);
        add(lMainTitle);

        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setBounds(WINDOW_WIDTH - 172, 40, 100, 50);
        bExit.addActionListener(e -> goBack());
        add(bExit);

        JPanel headerLine = new JPanel();
        headerLine.setBackground(LIGHT_GRAY);
        headerLine.setBounds(72, 110, WINDOW_WIDTH - 144, 2); 
        add(headerLine);
    }
    
    private void createMainLayout() {
        int startY = 150; 
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBounds(72, startY, WINDOW_WIDTH - 144, WINDOW_HEIGHT - startY - 50);
        mainContainer.setOpaque(false);
        
        // 메뉴 영역
        JPanel pMenuSection = new JPanel(new BorderLayout());
        pMenuSection.setPreferredSize(new Dimension(220, 0));
        pMenuSection.setOpaque(false);
        pMenuSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, LIGHT_GRAY),
            new EmptyBorder(150, 0, 0, 0)
        ));
        
        JPanel pButtonGroup = new JPanel(new GridLayout(3, 1, 0, 60));
        pButtonGroup.setOpaque(false);
        pButtonGroup.add(createMenuButton("오디오", e -> showContent(MENU_AUDIO)));
        pButtonGroup.add(createMenuButton("조작", e -> showContent(MENU_CONTROL)));
        pButtonGroup.add(createMenuButton("게임", e -> showContent(MENU_EMOTION)));
        
        pMenuSection.add(pButtonGroup, BorderLayout.NORTH);
        mainContainer.add(pMenuSection, BorderLayout.WEST);
        
        // 컨텐츠 영역        
        JPanel pRightSection = new JPanel(new GridBagLayout());
        pRightSection.setOpaque(false);
        
        lContentTitle = new JLabel("", SwingConstants.CENTER);
        lContentTitle.setFont(getFont().deriveFont(33f));
        lContentTitle.setBorder(new EmptyBorder(35, 0, 0, 0)); 
        
        GridBagConstraints gbcTitle = new GridBagConstraints();
        gbcTitle.gridx = 0;
        gbcTitle.gridy = 0;
        gbcTitle.weightx = 1.0;
        gbcTitle.fill = GridBagConstraints.HORIZONTAL;
        
        pRightSection.add(lContentTitle, gbcTitle);
        
        pContentBody = new JPanel(new BorderLayout());
        pContentBody.setOpaque(false);
        
        GridBagConstraints gbcBody = new GridBagConstraints();
        gbcBody.gridx = 0;
        gbcBody.gridy = 1;
        gbcBody.weightx = 1.0;
        gbcBody.weighty = 1.0;
        gbcBody.fill = GridBagConstraints.BOTH;
        
        pRightSection.add(pContentBody, gbcBody);
        
        mainContainer.add(pRightSection, BorderLayout.CENTER);
        
        add(mainContainer);
    }
    
    private Button createMenuButton(String text, java.awt.event.ActionListener action) {
        Button btn = new Button(text);
        btn.setPreferredSize(new Dimension(160, 60));
        btn.addActionListener(action);
        return btn;
    }
    
    private void createContentPanels() {
        pEmotionContent = new JPanel(new GridBagLayout()); 
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

        pAudioContent = createPlaceholderPanel("오디오 설정 내용이 여기에 표시됩니다");
        pControlContent = createPlaceholderPanel("조작 설정 내용이 여기에 표시됩니다");
    }
    
    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setOpaque(false);
        JLabel lPlaceholder = new JLabel(text);
        lPlaceholder.setFont(getFont().deriveFont(24f));
        p.add(lPlaceholder);
        return p;
    }
    
    private void showContent(String menuType) {
        pContentBody.removeAll();
        switch (menuType) {
            case MENU_EMOTION:
                lContentTitle.setText("감정표현 설정");
                pContentBody.add(pEmotionContent, BorderLayout.CENTER);
                break;
            case MENU_AUDIO:
                lContentTitle.setText("오디오 설정");
                pContentBody.add(pAudioContent, BorderLayout.CENTER);
                break;
            case MENU_CONTROL:
                lContentTitle.setText("조작 설정");
                pContentBody.add(pControlContent, BorderLayout.CENTER);
                break;
        }
        pContentBody.revalidate();
        pContentBody.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}