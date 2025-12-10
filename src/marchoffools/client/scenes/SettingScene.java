package marchoffools.client.scenes;

import marchoffools.client.core.AudioManager;
import marchoffools.client.core.Config;
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

        pAudioContent = createAudioPanel();
        
        pControlContent = createPlaceholderPanel("조작 설정 내용이 여기에 표시됩니다");
    }
    
    private JPanel createAudioPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 50, 40, 50); 

        // 배경음악 (BGM)
        panel.add(createVolumeControl("배경음악 (BGM)", Config.BGM_VOLUME, val -> {
            Config.BGM_VOLUME = val;
            AudioManager.getInstance().setBgmVolume(val);
        }), gbc);

        // 효과음 (SFX)
        gbc.gridy++;
        panel.add(createVolumeControl("효과음 (SFX)", Config.SFX_VOLUME, val -> {
            Config.SFX_VOLUME = val;
            // AudioManager.getInstance().playSfx(Assets.Sounds.SFX_CLICK);
        }), gbc);
        
        panel.add(new JLabel(), gbc);

        return panel;
    }
    
    private JPanel createVolumeControl(String title, int initialValue, java.util.function.IntConsumer onValueChange) {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);
        
        // 제목 라벨
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(getFont().deriveFont(24f));
        lTitle.setForeground(BLACK);
        container.add(lTitle, BorderLayout.NORTH);
        
        // 슬라이더 및 값 표시 패널
        JPanel controlPanel = new JPanel(new BorderLayout(15, 0));
        controlPanel.setOpaque(false);
        
        // 현재 값 표시 라벨
        JLabel lValue = new JLabel(String.valueOf(initialValue));
        lValue.setFont(getFont().deriveFont(Font.BOLD, 20f));
        lValue.setForeground(GRAY);
        lValue.setPreferredSize(new Dimension(50, 30));
        lValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // 슬라이더 (0 ~ 100)
        JSlider slider = new JSlider(0, 100, initialValue);
        slider.setOpaque(false);
        slider.setFocusable(false);
        slider.setBackground(LIGHT_GRAY); // 슬라이더 트랙 색상 등은 UI 매니저나 별도 커스텀 필요할 수 있음
        
        slider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            // 드래그 중에도 값이 바뀌게 하려면 if (!source.getValueIsAdjusting()) 제거
            int value = source.getValue();
            lValue.setText(String.valueOf(value));
            onValueChange.accept(value);
        });
        
        controlPanel.add(slider, BorderLayout.CENTER);
        controlPanel.add(lValue, BorderLayout.EAST);
        
        container.add(controlPanel, BorderLayout.CENTER);
        
        return container;
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