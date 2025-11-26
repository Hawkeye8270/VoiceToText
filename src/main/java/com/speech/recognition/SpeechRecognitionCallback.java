package com.speech.recognition;

public interface SpeechRecognitionCallback {
    void onTextRecognized(String text);
    void onPartialResult(String text);
    void onFinalResult(String text);
    void onError(String error);
    void onRecordingStarted();
    void onRecordingStopped();
}
