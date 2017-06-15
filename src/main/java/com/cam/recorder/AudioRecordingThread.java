package com.cam.recorder;

import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by mohamedrefaat on 6/15/17.
 */
public class AudioRecordingThread extends Thread {

    // Thread for audio capture, this could be in a nested private class if you prefer...

    private static FFmpegFrameRecorder recorder = null;
    private int audioDeviceIndex = 0;
    private int frameRate = 30;

    public AudioRecordingThread(FFmpegFrameRecorder recorder, int audioDeviceIndex, int frameRate) {

        this.recorder = recorder;
        this.audioDeviceIndex = audioDeviceIndex;
        this.frameRate = frameRate;
    }


    public void run() {
        // Pick a format...
        // NOTE: It is better to enumerate the formats that the system supports,
        // because getLine() can error out with any particular format...
        // For us: 44.1 sample rate, 16 bits, stereo, signed, little endian
        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);



        // Get TargetDataLine with that format
        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
        Mixer mixer = AudioSystem.getMixer(minfoSet[audioDeviceIndex]);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            // Open and start capturing audio
            // It's possible to have more control over the chosen audio device with this line:
            final TargetDataLine line = (TargetDataLine)mixer.getLine(dataLineInfo);
            //final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();

            final int sampleRate = (int) audioFormat.getSampleRate();
            final int numChannels = audioFormat.getChannels();

            // Let's initialize our audio buffer...
            int audioBufferSize = sampleRate * numChannels;
            final byte[] audioBytes = new byte[audioBufferSize];

            // Using a ScheduledThreadPoolExecutor vs a while loop with
            // a Thread.sleep will allow
            // us to get around some OS specific timing issues, and keep
            // to a more precise
            // clock as the fixed rate accounts for garbage collection
            // time, etc
            // a similar approach could be used for the webcam capture
            // as well, if you wish
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(new Runnable() {

                public void run() {
                    try {
                        // Read from the line... non-blocking
                        int nBytesRead = line.read(audioBytes, 0, line.available());

                        // Since we specified 16 bits in the AudioFormat,
                        // we need to convert our read byte[] to short[]
                        // (see source from FFmpegFrameRecorder.recordSamples for AV_SAMPLE_FMT_S16)
                        // Let's initialize our short[] array
                        int nSamplesRead = nBytesRead / 2;
                        short[] samples = new short[nSamplesRead];

                        // Let's wrap our short[] into a ShortBuffer and
                        // pass it to recordSamples
                        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                        ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                        // recorder is instance of
                        // org.bytedeco.javacv.FFmpegFrameRecorder

                        recorder.recordSamples(sampleRate, numChannels, sBuff);

                    } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, (long) 1000 / frameRate, TimeUnit.MILLISECONDS);
        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
        }
    }
}