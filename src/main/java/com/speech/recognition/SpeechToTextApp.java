package com.speech.recognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SpeechToTextApp extends JFrame {
    private JTextArea textArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private SpeechRecognitionService speechService;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SpeechToTextApp().setVisible(true);
        });
    }

    public SpeechToTextApp() {
        initializeUI();
        String modelPath = "models/vosk-model-small-ru-0.22";
        speechService = new SpeechRecognitionService(modelPath);

        try {
            speechService.initialize();
            statusLabel.setText("Система готова к работе");
        } catch (Exception e) {
            statusLabel.setText("Ошибка инициализации: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки модели Vosk. Убедитесь, что:\n" +
                            "1. Модель скачана и находится в папке " + modelPath + "\n" +
                            "2. Архитектура модели соответствует вашей системе",
                    "Ошибка инициализации",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setTitle("Оффлайн Распознавание Речи - Vosk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);

        JLabel titleLabel = new JLabel("Оффлайн Распознавание Речи", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());

        startButton = new JButton("Начать запись");
        startButton.setBackground(Color.GREEN);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));

        stopButton = new JButton("Остановить запись");
        stopButton.setBackground(Color.RED);
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setEnabled(false);

        statusLabel = new JLabel("Загрузка...");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startRecognition();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopRecognition();
            }
        });

        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(statusLabel);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void startRecognition() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        textArea.setText("");
        statusLabel.setText("Запись... Можете говорить");

        speechService.startRecognition(new SpeechRecognitionCallback() {
            @Override
            public void onTextRecognized(String text) {
            }

            @Override
            public void onPartialResult(String text) {
                SwingUtilities.invokeLater(() -> {
                    String currentText = textArea.getText();
                    String[] lines = currentText.split("\n");

                    if (lines.length > 0) {
                        StringBuilder newText = new StringBuilder();
                        for (int i = 0; i < lines.length - 1; i++) {
                            newText.append(lines[i]).append("\n");
                        }
                        newText.append(text);
                        textArea.setText(newText.toString());
                    } else {
                        textArea.setText(text);
                    }

                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
            }

            @Override
            public void onFinalResult(String text) {
                SwingUtilities.invokeLater(() -> {
                    if (!text.isEmpty()) {
                        textArea.append("\n" + text + "\n");
                        statusLabel.setText("Распознано: " + text);
                    }
                });
            }

            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Ошибка: " + error);
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                });
            }

            @Override
            public void onRecordingStarted() {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Запись началась...");
                });
            }

            @Override
            public void onRecordingStopped() {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Запись остановлена");
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                });
            }
        });
    }

    private void stopRecognition() {
        speechService.stopRecognition();
    }

    @Override
    public void dispose() {
        if (speechService != null) {
            speechService.shutdown();
        }
        super.dispose();
    }
}