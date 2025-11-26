package com.speech.recognition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechRecognitionService {
    private VoskModelManager modelManager;
    private MicrophoneManager microphoneManager;
    private SpeechRecognitionCallback callback;
    private ExecutorService executor;
    private AtomicBoolean isRecognizing;
    private String modelPath;

    public SpeechRecognitionService(String modelPath) {
        this.modelPath = modelPath;
        this.modelManager = new VoskModelManager();
        this.microphoneManager = new MicrophoneManager();
        this.executor = Executors.newSingleThreadExecutor();
        this.isRecognizing = new AtomicBoolean(false);
    }

    public void initialize() {
        try {
            modelManager.loadModel(modelPath);
            System.out.println("Сервис распознавания инициализирован");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации сервиса распознавания", e);
        }
    }

    public void startRecognition(SpeechRecognitionCallback callback) {
        if (isRecognizing.get()) {
            callback.onError("Распознавание уже запущено");
            return;
        }

        this.callback = callback;
        isRecognizing.set(true);

        executor.execute(() -> {
            try {
                callback.onRecordingStarted();

                microphoneManager.startRecording((audioData, bytesRead) -> {
                    if (isRecognizing.get()) {
                        String result = modelManager.processAudio(audioData, bytesRead);

                        if (result.contains("\"partial\" : \"") || result.contains("\"text\" : \"")) {
                            if (result.contains("\"text\" : \"")) {
                                String text = extractTextFromResult(result);
                                if (!text.isEmpty()) {
                                    callback.onFinalResult(text);
                                }
                            } else {
                                String partialText = extractPartialTextFromResult(result);
                                if (!partialText.isEmpty()) {
                                    callback.onPartialResult(partialText);
                                }
                            }
                        }
                    }
                });

            } catch (Exception e) {
                callback.onError("Ошибка записи: " + e.getMessage());
                stopRecognition();
            }
        });
    }

    public void stopRecognition() {
        if (!isRecognizing.get()) {
            return;
        }

        isRecognizing.set(false);
        microphoneManager.stopRecording();

        String finalResult = modelManager.getFinalResult();
        String text = extractTextFromResult(finalResult);
        if (!text.isEmpty() && callback != null) {
            callback.onFinalResult(text);
        }

        if (callback != null) {
            callback.onRecordingStopped();
        }

        System.out.println("Распознавание остановлено");
    }

    private String extractTextFromResult(String jsonResult) {
        try {
            int startIndex = jsonResult.indexOf("\"text\" : \"") + 10;
            int endIndex = jsonResult.indexOf("\"", startIndex);
            if (startIndex >= 10 && endIndex > startIndex) {
                return jsonResult.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга результата: " + jsonResult);
        }
        return "";
    }

    private String extractPartialTextFromResult(String jsonResult) {
        try {
            int startIndex = jsonResult.indexOf("\"partial\" : \"") + 13;
            int endIndex = jsonResult.indexOf("\"", startIndex);
            if (startIndex >= 13 && endIndex > startIndex) {
                return jsonResult.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга частичного результата: " + jsonResult);
        }
        return "";
    }

    public void shutdown() {
        stopRecognition();
        modelManager.close();
        executor.shutdown();
    }
}