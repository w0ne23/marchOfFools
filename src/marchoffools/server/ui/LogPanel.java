package marchoffools.server.ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 서버 로그 표시 패널
 */
public class LogPanel extends JPanel {
    
    private JTextArea logArea;
    private JComboBox<String> filterCombo;
    private String currentFilter = "전체";
    
    private static final int MAX_LOG_LINES = 1000;
    private int lineCount = 0;
    
    private java.util.List<LogEntry> allLogs = new java.util.ArrayList<>();
    
    private static class LogEntry {
        String timestamp;
        String level;
        String message;
        
        LogEntry(String timestamp, String level, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }
    }
    
    public LogPanel(String title) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("📝 " + title));
        
        initComponents();
    }
    
    private void initComponents() {
        // 로그 영역
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setCaretColor(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        // 하단 컨트롤 패널
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlPanel.setBackground(new Color(245, 245, 245));
        
        // 필터 콤보박스
        filterCombo = new JComboBox<>(new String[]{"전체", "INFO", "WARN", "ERROR", "DEBUG"});
        filterCombo.addActionListener(e -> {
            currentFilter = (String) filterCombo.getSelectedItem();
            refreshLogDisplay();
        });
        
        // 버튼들
        JButton saveBtn = new JButton("💾 저장");
        saveBtn.addActionListener(e -> saveLog());
        
        JButton clearBtn = new JButton("🗑 지우기");
        clearBtn.addActionListener(e -> {
            logArea.setText("");
            allLogs.clear();
            lineCount = 0;
        });
        
        JButton scrollBtn = new JButton("🔽 맨 아래");
        scrollBtn.addActionListener(e -> {
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        
        controlPanel.add(new JLabel("필터:"));
        controlPanel.add(filterCombo);
        controlPanel.add(scrollBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(clearBtn);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    public void appendLog(String timestamp, String level, String message) {
    	// 모든 로그 저장
    	LogEntry entry = new LogEntry(timestamp, level, message);
        allLogs.add(entry);
        
        // 로그 수 제한
        if (allLogs.size() > MAX_LOG_LINES) {
            allLogs.remove(0);
        }
        
        // 필터 적용
        if (currentFilter.equals("전체") || level.equals(currentFilter)) {
            appendToDisplay(timestamp, level, message);
        }
    }
    
    private void appendToDisplay(String timestamp, String level, String message) {
        String prefix = switch (level) {
	        case "ERROR" -> "❌ [ERROR]";
	        case "WARN"  -> "⚠️ [WARN]";
	        case "INFO"  -> "💬️ [INFO]";
	        case "DEBUG" -> "🔧 [DEBUG]";
            default      -> "   [" + level + "]";
        };
        
        String formatted = String.format("[%s] %s %s%n", timestamp, prefix, message);
        logArea.append(formatted);
        
        // 자동 스크롤
        JScrollBar scrollBar = ((JScrollPane) logArea.getParent().getParent()).getVerticalScrollBar();
        boolean atBottom = scrollBar.getValue() + scrollBar.getVisibleAmount() >= scrollBar.getMaximum() - 50;
        if (atBottom) {
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
    
    private void refreshLogDisplay() {
        logArea.setText("");
        
        for (LogEntry entry : allLogs) {
            if (currentFilter.equals("전체") || entry.level.equals(currentFilter)) {
                appendToDisplay(entry.timestamp, entry.level, entry.message);
            }
        }
    }
    
    private void trimLog() {
        try {
        	Document doc = logArea.getDocument();
            int lineCount = logArea.getLineCount();
            if (lineCount > MAX_LOG_LINES) {
                int end = logArea.getLineEndOffset(lineCount - MAX_LOG_LINES);
                doc.remove(0, end); 
            }
        } catch (BadLocationException e) {
        }
    }
    
    private void saveLog() {
        JFileChooser chooser = new JFileChooser();
        
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        String defaultName = "server_log_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        chooser.setSelectedFile(new File(defaultName));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        	File saveFile = chooser.getSelectedFile();
            
            System.out.println("저장 시도 경로: " + saveFile.getAbsolutePath());
            
            try (PrintWriter writer = new PrintWriter(chooser.getSelectedFile(), "UTF-8")) {
                writer.print(logArea.getText());
                writer.flush();
                
                if (saveFile.exists()) {
                    JOptionPane.showMessageDialog(this, 
                        "로그가 저장되었습니다.\n" + saveFile.getAbsolutePath(),
                        "저장 완료", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "파일 저장에 실패했습니다.", 
                        "오류", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "저장 실패: " + e.getMessage(), 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}