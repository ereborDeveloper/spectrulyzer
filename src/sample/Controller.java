package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Controller {
    //Device ChoiceBox
    @FXML
    private ChoiceBox dev;
    //Fade ChoiceBox
    @FXML
    private ChoiceBox fadeSpeed;
    @FXML
    private TextField txt;
    @FXML
    private Rectangle peakL;
    @FXML
    private Rectangle rmsL;
    @FXML
    private Rectangle rmsR;
    @FXML
    private Rectangle peakR;
    @FXML
    private Label rmsValue;
    private TargetDataLine line;
    private float[] fadeArray = {0.97f, 0.989f, 0.996f, 0.999f};
    private float fade;
    private Thread startCapturing;
    private boolean isRunning = false;
    private float[] spectrum;

    public void ShowMenu(ContextMenuEvent contextMenuEvent) {
        Device.InitialazeMixer();
    }

    @FXML
    public void initialize() {
        startCapturing = new Thread(new Recorder());
        Settings settings = new Settings();
        Device.InitialazeMixer();
        ObservableList<String> list = Device.GetAll();
        list.add("none");
        dev.setValue("none");
        dev.setItems(list);
        nullMeters();
        //Fade
        fade = fadeArray[1];
        ObservableList<String> fadeList = FXCollections.observableArrayList("fast", "medium", "slow", "very slow");
        ;

        fadeSpeed.setItems(fadeList);
        fadeSpeed.setValue("medium");
        fadeSpeed.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                // if(dev.getValue().toString()!="none") {
                int index = fadeSpeed.getSelectionModel().getSelectedIndex();
                fade = fadeArray[index];
            }
        });
        //Device

        dev.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                int index = dev.getSelectionModel().getSelectedIndex();
                if (index == 3) {
                    System.out.println("yes");
                    if (line != null) {
                        if (line.isOpen()) {
                            line.stop();
                            line.close();
                            line = null;
                            nullMeters();
                        }
                    }

                } else {
                    Device.ChooseDevice(index);
                    if (!isRunning) {
                        isRunning = true;
                        try {
                            if (line != null) {
                                if (line.isOpen()) {
                                    line.stop();
                                    //line.close();
                                    //nullMeters();
                                }
                            }
                            line = AudioSystem.getTargetDataLine(Settings.getFormat(), Device.GetDevice());
                            startCapturing.start();
                        } catch (LineUnavailableException e) {
                            e.printStackTrace();
                            System.err.println(e);
                        }
                    }
                }
            }
        });
    }

    public void nullMeters() {
        peakL.setY(335);
        rmsL.setY(335);
        peakR.setY(335);
        rmsR.setY(335);
        peakL.setHeight(0);
        rmsL.setHeight(0);
        rmsR.setHeight(0);
        peakR.setHeight(0);
    }

    public void setPeakL(double peak) {
        peakL.setY((peakL.getY() + peakL.getHeight() - peak * 335) % 336);
        peakL.setHeight((peak * 335) % 336);
    }

    public void setRmsL(double rms) {
        rmsL.setY((rmsL.getY() + rmsL.getHeight() - rms * 335));
        rmsL.setHeight(((rms * 335)));
    }

    private void setValue(float maxRms) {
        String value = String.valueOf(maxRms);
        //rmsValue.setText(value);
    }

    class Recorder implements Runnable {
        Recorder() {

        }

        @Override
        public void run() {
            final int bufferByteSize = Settings.getBuffer();
            //running recording thread
            try {
                System.out.println("It works!");
                line.open(Settings.getFormat(), Settings.getBuffer());
            } catch (Exception e) {
                e.printStackTrace();
            }
            double maxFreq = 0;
            byte[] buf = new byte[bufferByteSize];
            float[] samples = new float[bufferByteSize];
            Complex[] fftBuffer = new Complex[bufferByteSize];
            float lastPeak = 0f;
            float lastRms = 0f;
            float maxRms = 0;
            line.start();
            {
                for (int b; (b = line.read(buf, 0, buf.length)) > -1; ) {

                    //converting bytes to samples
                    for (int i = 0, s = 0; i < b; ) {
                        int sample = 0;

                        sample += buf[i++] & 0xFF; // (reverse these two lines
                        sample += buf[i++] << 8;   //  if the format is big endian)

                        // Normalize from -1 to +1 float
                        samples[s++] = sample / 32768f;

                    }
                    int i = 0;
                    float rms = 0f;
                    float peak = 0f;
                    for (float sample : samples) {
                        //filling fftBuffer
                        fftBuffer[i] = new Complex(sample, 0);
                        i++;
                        float abs = Math.abs(sample);
                        if (abs > peak) {
                            peak = abs;
                        }

                        rms += sample * sample;
                    }
                    //setting METERS
                    rms = (float) Math.sqrt(rms / samples.length);
                    if (rms > maxRms) {
                        maxRms = rms;
                        //setValue(maxRms);
                    }
                    if (lastRms > rms) {
                        rms = lastRms * (fade);
                    }
                    if (lastPeak > peak) {
                        peak = lastPeak * (fade) * fade;
                    }
                    if (rms < maxRms) {
                        maxRms -= (1 - fade);
                        lastRms = maxRms;
                    } else {
                        lastRms = rms;
                    }
                    lastPeak = peak;
                    setPeakL(peak);
                    setRmsL(maxRms);
                    System.out.println();
                    System.out.println(i);
                    System.out.println();
                    //
                    int index = 0;
                    int l=0;
                    for (l = 0; l < bufferByteSize / 2 - 1; l++) {
                        double freq = FFT.fft(fftBuffer)[l].abs();
                        System.out.println(freq);
                        if (freq > maxFreq) {
                            maxFreq = freq;
                            index=l;
                        }
                    }
                    float max=index*44100.0f/bufferByteSize;
                    System.out.println();
                    System.out.println("Основная частота "+index+" сигнала - "+max);
                    try {
                        Thread.sleep(2000);
                    }
                    catch (Exception e)
                    {

                    }
                    System.out.println();
                    //нужно отмасштабировать и разобраться с коэффициентами
                }
            }
        }


    }


}
