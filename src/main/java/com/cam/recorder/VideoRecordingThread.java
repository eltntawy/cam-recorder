package com.cam.recorder;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mohamedrefaat on 6/15/17.
 */
public class VideoRecordingThread implements Runnable {

    private FFmpegFrameRecorder recorder = null;
    private OpenCVFrameGrabber grabber = null;
    private Mixer mixer;
    private final int CAPTUREWIDTH = 800;
    private final int CAPTUREHRIGHT = 600;

    private int FRAME_RATE = 30;
    private int GOP_LENGTH_IN_FRAMES = 60;
    private volatile boolean runnable = true;

    private JPanel canvas;

    private boolean stop = false;


    public VideoRecordingThread(JPanel canvas,int webcamDeviceIndex,Mixer mixer) {
        this.canvas = canvas;

        grabber = new OpenCVFrameGrabber(webcamDeviceIndex);
        this.mixer = mixer;

    }

    public void run() {
        synchronized (this) {
            try {

                String userHome = System.getProperty("user.home");

                File file = new File(userHome + "/output.mp4");
                                System.out.println("output file will saved under: "+file.getAbsolutePath());



                grabber.setImageWidth(CAPTUREWIDTH);
                grabber.setImageHeight(CAPTUREHRIGHT);
                grabber.start();
                recorder = new FFmpegFrameRecorder(file,
                        CAPTUREWIDTH, CAPTUREHRIGHT, 2);
                recorder.setInterleaved(true);

                // video options //
                recorder.setVideoOption("tune", "zerolatency");
                recorder.setVideoOption("preset", "ultrafast");
                recorder.setVideoOption("crf", "28");
                recorder.setVideoBitrate(2000000);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(FRAME_RATE);
                recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
                // audio options //
                recorder.setAudioOption("crf", "0");
                recorder.setAudioQuality(0);
                recorder.setAudioBitrate(192000);
                recorder.setSampleRate(44100);
                recorder.setAudioChannels(2);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

                recorder.start();

                new AudioRecordingThread(recorder, mixer, FRAME_RATE).start();


                Frame capturedFrame = null;
                Java2DFrameConverter paintConverter = new Java2DFrameConverter();
                long startTime = System.currentTimeMillis();
                long frame = 0;
                while ((capturedFrame = grabber.grab()) != null && runnable) {
                    BufferedImage buff = paintConverter.getBufferedImage(capturedFrame, 1);
                    Graphics g = canvas.getGraphics();
                    g.drawImage(buff, 0, 0, CAPTUREWIDTH, CAPTUREHRIGHT, 0, 0, buff.getWidth(), buff.getHeight(), null);
                    recorder.record(capturedFrame);
                    frame++;
                    long waitMillis = 1000 * frame / FRAME_RATE - (System.currentTimeMillis() - startTime);
                    while (waitMillis <= 0) {
                        // If this error appeared, better to consider lower FRAME_RATE.
                        System.out.println("[ERROR] grab image operation is too slow to encode, skip grab image at frame = " + frame + ", waitMillis = " + waitMillis);
                        recorder.record(capturedFrame);  // use same capturedFrame for fast processing.
                        frame++;
                        waitMillis = 1000 * frame / FRAME_RATE - (System.currentTimeMillis() - startTime);

                    }
                    //System.out.println("frame " + frame + ", System.currentTimeMillis() = " + System.currentTimeMillis() + ", waitMillis = " + waitMillis);
                    Thread.sleep(waitMillis);
                }
            } catch (FrameGrabber.Exception ex) {
                Logger.getLogger(CamRecorder.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(CamRecorder.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FrameRecorder.Exception ex) {
                Logger.getLogger(CamRecorder.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }



    public FFmpegFrameRecorder getRecorder() {
        return recorder;
    }

    public void setRecorder(FFmpegFrameRecorder recorder) {
        recorder = recorder;
    }

    public OpenCVFrameGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(OpenCVFrameGrabber grabber) {
        grabber = grabber;
    }
}

