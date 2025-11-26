package com.speech.recognition;

import org.vosk.LibVosk;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LogLevel;

public class VoskModelManager {
    private Model model;
    private Recognizer recognizer;
    private boolean isModelLoaded;

    public VoskModelManager() {
        this.isModelLoaded = false;
        LibVosk.setLogLevel(LogLevel.WARNINGS);
    }

    public void loadModel(String modelPath) {
        try {
            this.model = new Model(modelPath);
            this.recognizer = new Recognizer(model, 16000.0f);
            this.isModelLoaded = true;
            System.out.println("Модель Vosk успешно загружена из: " + modelPath);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки модели: " + e.getMessage());
            throw new RuntimeException("Не удалось загрузить модель Vosk", e);
        }
    }

    public String processAudio(byte[] audioData, int bytesRead) {
        if (!isModelLoaded) {
            throw new IllegalStateException("Модель не загружена");
        }

        if (recognizer.acceptWaveForm(audioData, bytesRead)) {
            return recognizer.getResult();
        } else {
            return recognizer.getPartialResult();
        }
    }

    public String getFinalResult() {
        return isModelLoaded ? recognizer.getFinalResult() : "";
    }

    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
        if (model != null) {
            model.close();
        }
        isModelLoaded = false;
    }

    public boolean isModelLoaded() {
        return isModelLoaded;
    }
}
