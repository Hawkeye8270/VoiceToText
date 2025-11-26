package com.speech.recognition;

import javax.sound.sampled.*;

public class MicrophoneManager {
    private TargetDataLine microphone;
    private AudioFormat audioFormat;
    private boolean isRecording;

    public MicrophoneManager() {
        this.audioFormat = getAudioFormat();
        this.isRecording = false;
    }

    private AudioFormat getAudioFormat() {
        // Формат аудио, совместимый с Vosk: 16кГц, 16-бит, моно
        float sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void startRecording(AudioDataCallback callback) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(audioFormat);
        microphone.start();

        isRecording = true;

        Thread recordingThread = new Thread(() -> {
            int bytesRead;
            byte[] buffer = new byte[4096];

            while (isRecording) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    callback.onAudioData(buffer, bytesRead);
                }
            }
        });

        recordingThread.start();
    }

    public void stopRecording() {
        isRecording = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }

    public AudioFormat getFormat() {
        return audioFormat;
    }

    public interface AudioDataCallback {
        void onAudioData(byte[] data, int bytesRead);
    }
}