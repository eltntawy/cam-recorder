package com.cam.recorder;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CamRecorder extends JFrame {

    private JButton settingBtn = new JButton("Setting");
    private JButton control;
    private JLabel text1;
    private JPanel canvas;


    private static final long serialVersionUID = 1L;
    private VideoRecordingThread videoRecordingThread;
    private Thread catcher;

    public CamRecorder() {


        JPanel mainPanel = new JPanel();
        getContentPane().add(mainPanel);

        mainPanel.setLayout(new BorderLayout());

        setTitle("Camera Recorder");
        setSize(1000, 1100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        control = new JButton("Start");
        text1 = new JLabel("  ");
        canvas = new JPanel();

        videoRecordingThread = new VideoRecordingThread(canvas);


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
                } catch (FrameGrabber.Exception ex) {
                    Logger.getLogger(CamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CamRecorder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        settingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                final JFrame settingFram = new JFrame("Setting");


                // get all available audio devices
                Mixer.Info[] audioDevices = AudioSystem.getMixerInfo();
                List<String> audioDevicesNames = new ArrayList<String>();
                for (Mixer.Info info : audioDevices) {
                    Mixer appMixer = AudioSystem.getMixer(info);
                    audioDevicesNames.add(info.getName());
                }

                // get all available cam devices
                try {
                    String videoDeviceOption[] = VideoInputFrameGrabber.getDeviceDescriptions();
                    System.out.println(Arrays.toString(videoDeviceOption));
                }catch (java.lang.Exception e1) {
                    JOptionPane.showMessageDialog(settingFram, e1.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }



                settingFram.setSize(300, 200);
                settingFram.setLocationRelativeTo(CamRecorder.this);

                settingFram.getContentPane().setLayout(new GridLayout(3, 2));

                settingFram.getContentPane();

                JComboBox camSettingsComboBox = new JComboBox();
                JComboBox micSettingsComboBox = new JComboBox(audioDevicesNames.toArray());

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

    private void controlActionPerformed(ActionEvent evt) throws Exception, FrameGrabber.Exception, InterruptedException {
        if (control.getText().equals("Stop")) {
            catcher.stop();
            videoRecordingThread.getRecorder().stop();
            videoRecordingThread.getGrabber().stop();
            control.setText("Start");

            text1.setText("");
        } else {
            control.setText("Stop");
            catcher = new Thread(videoRecordingThread);
            catcher.start();
            text1.setText("<html><font color='red'>Recording ...</font></html>");
            control.setText("Stop");
        }
    }


    public static void main(String[] args) {
        new CamRecorder();

    }

}
