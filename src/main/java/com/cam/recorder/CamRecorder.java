package com.cam.recorder;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameRecorder;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacv.OpenCVFrameGrabber;


public class CamRecorder extends JFrame {

    private JButton settingBtn = new JButton("Setting");
    private JButton control;
    private JLabel text1;
    private JPanel canvas;

    private int defaultAudioMixerIndex = 0;
    private static final long serialVersionUID = 1L;
    private VideoRecordingThread videoRecordingThread;
    private Thread catcher;


    private int currentWebcamDeviceIndex = 0;
    private Mixer mixer;


    List<Mixer> mixers;
    List<String> mixersNmaes;
    String devicesArray [] ;


    public final String NO_DEVICES = "No Devices";

    public CamRecorder() {


        // get all available audio devices
        mixers = discoverMicrophones();
        mixersNmaes = new ArrayList<String>();
        for (Mixer mixer : mixers) {
            mixersNmaes.add(mixer.getMixerInfo().getName());
        }


        JPanel mainPanel = new JPanel();
        getContentPane().add(mainPanel);

        mainPanel.setLayout(new BorderLayout());

        setTitle("Camera Recorder");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        control = new JButton("Start");
        text1 = new JLabel("  ");
        canvas = new JPanel();

        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
        mixer = AudioSystem.getMixer(minfoSet[defaultAudioMixerIndex]);

        videoRecordingThread = new VideoRecordingThread(canvas, currentWebcamDeviceIndex, mixer);


        mainPanel.add(canvas, BorderLayout.CENTER);
        canvas.setBorder(BorderFactory.createEtchedBorder());

        JPanel controlPanel = new JPanel(new FlowLayout());

        controlPanel.add(control);
        controlPanel.add(settingBtn);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        mainPanel.add(text1, BorderLayout.NORTH);


        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);


        addListeners();

    }

    public void addListeners() {
        control.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    controlActionPerformed(evt);
                } catch (Exception ex) {
                    Logger.getLogger(CamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        settingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                final JFrame settingFram = new JFrame("Setting");

                // get all available cam devices
                List<String> videoDeviceOption = new ArrayList<String>();
                videoDeviceOption.add(NO_DEVICES);
                
                
                try {
                    devicesArray = VideoInputFrameGrabber.getDeviceDescriptions();
                
                    
                    videoDeviceOption = new ArrayList<String>();
                    for(String device : devicesArray) {
                        videoDeviceOption.add(device);
                    }

                } catch (FrameGrabber.Exception ex) {
                    ex.printStackTrace();
                }

                settingFram.setSize(300, 200);
                settingFram.setLocationRelativeTo(CamRecorder.this);

                settingFram.getContentPane().setLayout(new GridLayout(3, 2));

                settingFram.getContentPane();

                final JComboBox camSettingsComboBox = new JComboBox(videoDeviceOption.toArray());
                final JComboBox micSettingsComboBox = new JComboBox(mixersNmaes.toArray());

                JButton okBtn = new JButton("Ok");

                JButton cancelBtn = new JButton("Cancel");


                settingFram.getContentPane().add(new JLabel("Select Cam"));
                settingFram.getContentPane().add(camSettingsComboBox);
                settingFram.getContentPane().add(new JLabel("Select Mic"));
                settingFram.getContentPane().add(micSettingsComboBox);

                settingFram.getContentPane().add(okBtn);
                settingFram.getContentPane().add(cancelBtn);


                okBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {


                        try {

                            currentWebcamDeviceIndex = camSettingsComboBox.getSelectedIndex();
                            videoRecordingThread = new VideoRecordingThread(canvas, currentWebcamDeviceIndex, mixer);
                            
                            int audioIndex = micSettingsComboBox.getSelectedIndex();

                            mixer = mixers.get(audioIndex);

                            
            if(devicesArray != null && devicesArray.length > currentWebcamDeviceIndex) {                
                System.out.println("Selected Cam Device: "+devicesArray[currentWebcamDeviceIndex]);
            } else {
                                System.out.println("Selected Cam Device: "+NO_DEVICES);

            }
            System.out.println("Selected Audio Device: "+mixer.getMixerInfo().getName());
            
                        } catch (java.lang.Exception e) {
                            JOptionPane.showMessageDialog(settingFram, e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }


                        settingFram.dispose();
                    }
                });

                cancelBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        settingFram.dispose();
                    }
                });


                settingFram.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                settingFram.setVisible(true);
                settingFram.setResizable(false);


            }
        });
    }


    private List<Mixer> discoverMicrophones() {
        List<Mixer> microphones = new ArrayList<Mixer>();
        for (Mixer.Info mixerinfo : AudioSystem.getMixerInfo()) {

            System.out.println("mixerinfo: " + mixerinfo);
            Mixer mixer = AudioSystem.getMixer(mixerinfo);

            System.out.println("mixer:     " + mixer);

            System.out.println("mixerinfo: " + mixer.getLineInfo());
            for (Line.Info lineinfo : mixer.getTargetLineInfo()) {
                try {
                    Line line;
                    line = mixer.getLine(lineinfo);
                    if (line instanceof TargetDataLine) {

                        System.out.println("    lineinfo:   " + lineinfo);

                        System.out.println("    line:       " + line);

                        System.out.println("    lineinfo:   " + line.getLineInfo());
                        if (mixer.isLineSupported(lineinfo)) {
                            microphones.add(mixer);
                        } else {

                            System.out.println("    NOT SUPPORTED!");
                        }
                    }
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
        return microphones;
    }

    private void controlActionPerformed(ActionEvent evt) throws Exception, FrameGrabber.Exception, InterruptedException {

        if (control.getText().equals("Stop")) {
            
            
            System.out.println("Selected Cam Device: "+currentWebcamDeviceIndex);
            System.out.println("Selected Audio Device: "+mixer.getMixerInfo().getName());
            
            catcher.stop();
            videoRecordingThread.getRecorder().stop();
            videoRecordingThread.getGrabber().stop();
            control.setText("Start");

            text1.setText("");
            
            settingBtn.setEnabled(false);
        } else {
            control.setText("Stop");
            catcher = new Thread(videoRecordingThread);
            catcher.start();
            text1.setText("<html><font color='red'>Recording ...</font></html>");
            control.setText("Stop");
                        settingBtn.setEnabled(true);

        }
    }


    public static void main(String[] args) {
        new CamRecorder();

    }

}
