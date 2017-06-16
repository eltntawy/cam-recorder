package com.cam.recorder;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameRecorder;

import java.util.Arrays;

/**
 * Created by mohamedrefaat on 6/16/17.
 */
public class Test {


    public static void main(String args[]) {

        for (int i = 0; i < 10; i++) {
            try {

                FFmpeg ffmpeg = new FFmpeg();

                System.out.println(Arrays.toString(ffmpeg.listDiveces().toArray()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
