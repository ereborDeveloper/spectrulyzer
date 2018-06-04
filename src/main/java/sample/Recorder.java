package sample;

import javafx.application.Platform;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;

class Recorder implements Runnable {
    private Controller controller;
    private double[] buffer;

    Recorder(Controller fx) {
        controller = fx;
    }

    @Override
    public void run() {
        while (true) {
            if (Settings.getLine() == null) {
                try {
                    //System.out.println("Waiting for signal..");
                    //Thread.sleep(1000);
                } catch (Exception e) {

                }
            } else {
                System.out.println(Settings.getLine().isOpen() + " " + Settings.getLine().isRunning());
                TargetDataLine line = Settings.getLine();
                try {
                    System.out.println(line.isOpen());
                    line.open(Settings.getFormat(), Settings.getBuffer());
                    System.out.println("Линия открыта!");
                } catch (LineUnavailableException e) {
                    System.out.println("Ошибка! Линия недоступна");
                    //line.close();
                    Settings.setLine(null);
                    Settings.isRunning = false;
                    e.printStackTrace();
                }
                double maxFreq = 0;
                double lastPeak = 0;
                double lastRms = 0;
                double maxRms = 0;
                line.start();
                {

                    double rms = 0f;
                    double peak = 0f;
                    Complex[] fftBuffer = new Complex[Settings.getBuffer()];
                    float[] samples = new float[Settings.getBuffer()];
                    buffer = new double[Settings.getBuffer() / 2];
                    byte[] buf = new byte[Settings.getBuffer() * 2];
                    int N = fftBuffer.length;

                    System.out.println("Hooray");
                    Settings.isRunning = true;
                    int b;
                    while ((Settings.isRunning)) {

                        if (line != Settings.getLine() && Settings.getLine() != null) {
                            Settings.getLine().stop();
                            Settings.getLine().close();
                            line.stop();
                            line.close();
                            break;}
                        b = Settings.getBuffer() * 2;
                        if (Settings.getLine() == null) {
                            for (int i = 0; i < buffer.length; i++) {
                                buffer[i] = 0.00000000;
                            }
                            try {
                                Thread.sleep(40);
                                Settings.isSignal = false;
                            } catch (Exception e) {
                            }

                        } else {
                            Settings.getLine().read(buf, 0, buf.length);
                            try {
                                float fade = controller.getFade();
                                //Переводим байты в сэмплы (для 16 битной PCM)
                                if (!Settings.getFormat().isBigEndian()) {
                                    for (int i = 0; i < b - 1; i += 2) {
                                        int sample = 0;
                                        // Если Little-endian, то следующий байт ставим вперди текущего
                                        //например buf[i]=10001000 sample = 10001000
                                        //sample = ((buf[i] & 0xff) << 8 | buf[i+1] & 0xff) - 0x8000;
                                        sample |= buf[i+1] << 8; //
                                        sample |= buf[i] &0xff;

                                        //например buf[i+1]=00001111 sample = 00001111 10001000
                                        //Нормализуем значение в диапазоне от -1 до 1
                                        samples[i / 2] = (sample / 32768f);
                                    }

                                } else {
                                    for (int i = 0; i < b - 1; i += 2) {
                                        int sample = 0;
                                        sample |= buf[i] << 8; // Если Big-endian, то текущий сдвигаем, а следующий дописываем
                                        //например buf[i]=10001000 sample = 10001000
                                        sample |= buf[i + 1];//
                                        //например buf[i+1]=10001111 sample = 10001000 00001111
                                        samples[i / 2] = (sample / 32768f);

                                    }
                                }
                                maxFreq = 0;
                        /*
                        System.out.println("//////");

                    for (int h = 0; h < samples.length; h++) {
                        //samples[h] = (float) Math.sin(1024*h*Math.PI/samples.length);
                        System.out.println(samples[h]);
                    }
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {

                    }
*/
                                int i = 0;
                                peak = 0;
                                rms = 0;
                                for (float sample : samples) {
                                    //filling fftBuffer
                                    fftBuffer[i] = new Complex(sample, 0);
                                    i++;
                                    double abs = Math.abs(sample);
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

                                int windowKey = controller.getWinIndex();

                                //System.out.println();
                                //System.out.println(i);
                                //System.out.println();

                                //Rectangular
                                if (windowKey == 1) {
                                    for (int k = 0; k < N / 4; k++) {
                                        fftBuffer[k].setRe(fftBuffer[k].getRe());
                                    }
                                    for (int k = N / 4; k < 3 * N / 4; k++) {
                                        fftBuffer[k].setRe(0);
                                    }
                                    for (int k = 3 * N / 4; k < N; k++) {
                                        fftBuffer[k].setRe(fftBuffer[k].getRe());
                                    }
                                }
                                //Triangular
                                if (windowKey == 2) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe(fftBuffer[k].getRe() * (1 - Math.abs((k - (N - 1) / 2) / (N - 1))));
                                    }
                                }
                                //Parzen
                                if (windowKey == 3) {
                                    for (int k = 0; k < N / 4; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 2 * Math.pow((1 - k / N * 2), 3)));
                                    }
                                    for (int k = N / 4; k < 3 * N / 4; k++) {
                                        fftBuffer[k].setRe(fftBuffer[k].getRe() * (1 - 6 * (k / N / 2) ^ 2 * (1 - k / N * 2)));
                                    }
                                    for (int k = 3 * N / 4; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 2 * Math.pow((1 - k / N * 2), 3)));
                                    }
                                }
                                //Welch
                                if (windowKey == 4) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe(fftBuffer[k].getRe() * (1 - ((k - (N - 1) / 2) / (N - 1) / 2) ^ 2));
                                    }
                                }
                                //Sine
                                if (windowKey == 5) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * Math.sin(Math.PI * k / (N - 1))));
                                    }
                                }
                                //Hann
                                if (windowKey == 6) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * Math.pow(Math.sin(Math.PI * k / (N - 1)), 2)));
                                    }
                                }
                                //Hamming
                                if (windowKey == 7) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * (0.54 - 0.46 * Math.cos(2 * Math.PI * k / N))));
                                    }
                                }
                                //Blackman
                                if (windowKey == 8) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * ((1 - 0.16) / 2 - 1 / 2 * Math.cos(2 * Math.PI * k / (N - 1)) + 0.08 * Math.cos(4 * Math.PI * k / (N - 1)))));
                                    }
                                }
                                //Nuttall
                                if (windowKey == 9) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * (0.355768 - 0.487396 * Math.cos(2 * Math.PI * k / (N - 1)) + 0.144232 * Math.cos(4 * Math.PI * k / (N - 1)) - 0.0012604 * Math.cos(6 * Math.PI * k / (N - 1)))));
                                    }
                                }
                                //Blackman-Nuttall
                                if (windowKey == 10) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * (0.3635819 - 0.4891775 * Math.cos(2 * Math.PI * k / (N - 1)) + 0.1365995 * Math.cos(4 * Math.PI * k / (N - 1)) - 0.0106411 * Math.cos(6 * Math.PI * k / (N - 1)))));
                                    }
                                }
                                //Blackman-Harris
                                if (windowKey == 11) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * (0.356 - 0.488 * Math.cos(2 * Math.PI * k / (N - 1)) + 0.144 * Math.cos(4 * Math.PI * k / (N - 1)) - 0.012 * Math.cos(6 * Math.PI * k / (N - 1)))));
                                    }
                                }
                                //Flat-top
                                if (windowKey == 12) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * (1 - 1.93 * Math.cos(2 * Math.PI * k / (N - 1)) + 1.29 * Math.cos(4 * Math.PI * k / (N - 1)) - 0.388 * Math.cos(6 * Math.PI * k / (N - 1))) + 0.028 * Math.cos(8 * Math.PI * k / (N - 1))));
                                    }
                                }
                                //Gaussian
                                if (windowKey == 13) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * Math.exp(-1 / 2 * ((k - (N - 1) / 2) / (0.4 * (N - 1) / 2)))));
                                    }
                                }
                                //Approximate confined Gaussian
                                if (windowKey == 14) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * Math.exp(-1 / 2 * ((k - (N - 1) / 2) / (0.4 * (N - 1) / 2)))));
                                    }
                                }
                                //Tukey
                                if (windowKey == 15) {
                                    for (int k = 0; k < (N - 1) / 4; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 1 / 2 * (1 + Math.cos(Math.PI * (4 * k / (N - 1) - 1)))));
                                    }
                                    for (int k = (int) ((N - 1) * 0.75 + 1); k < N - 1; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 1 / 2 * (1 + Math.cos(Math.PI * (4 * k / (N - 1) - 4 + 1)))));
                                    }
                                }
                                //Planck-taper
                                if (windowKey == 16) {
                                    for (int k = 1; k < 0.1 * (N - 1); k++) {
                                        try {
                                            fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 1 / (Math.exp(2 * 0.1 * (1 / (1 + (2 * k / (N - 1) - 1)) + 1 / (1 - 2 * 0.1 + (2 * k / (N - 1) - 1)))) + 1)));
                                        } catch (Exception e) {
                                            continue;
                                        }
                                    }
                                    fftBuffer[(int) Math.ceil(0.1 * (N - 1))].setRe(0);
                                    for (int k = (int) ((1 - 0.1) * (N - 1) + 1); k < (1 - 0.1) * (N - 1); k++) {
                                        try {
                                            fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 1 / (Math.exp(2 * 0.1 * (1 / (1 - (2 * k / (N - 1) - 1)) + 1 / (1 - 2 * 0.1 - (2 * k / (N - 1) - 1)))) + 1)));
                                        } catch (Exception e) {
                                            continue;
                                        }
                                    }
                                    fftBuffer[(int) Math.ceil((1 - 0.1) * (N - 1))].setRe(0);
                                }
                                //Exp
                                if (windowKey == 17) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * Math.exp(-Math.abs(k - (N - 1) / 2) / ((N / 2) / (60 / 8.69)))));
                                    }
                                }
                                //Barlett-Hann
                                if (windowKey == 18) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * (0.62 - 0.48 * Math.abs(k / (N - 1) - 0.5)) - 0.38 * Math.cos(2 * Math.PI * k / (N - 1))));
                                    }
                                }
                                //Hann-Poison
                                if (windowKey == 19) {
                                    for (int k = 0; k < N; k++) {
                                        fftBuffer[k].setRe((float) (fftBuffer[k].getRe() * 0.5 * (1 - Math.cos(2 * Math.PI * k / (N - 1))) * Math.exp(-2 * Math.abs(N - 1 - 2 * k) / (N - 1))));
                                    }
                                }

                                double fft[] = new double[fftBuffer.length];
                                double zero[] = new double[fftBuffer.length];

                                for (int z = 0; z < N; z++) {
                                    zero[z] = 0f;
                                    fft[z] = fftBuffer[z].getRe();
                                }
                                FFT.transform(fft, zero);
                                for (int z = 0; z < N; z++) {
                                    zero[z] = 0f;
                                    fftBuffer[z].setRe((float) fft[z]);
                                }

                                int index = 0;
                                int l = 0;
                                double freq = 0f;
                                for (l = 0; l < fftBuffer.length / 2; l++) {
                                    freq = fftBuffer[l].abs();
                                    buffer[l] = freq;
                                    //System.out.println(freq);
                                    if (freq > maxFreq) {
                                        maxFreq = freq;
                                        controller.setMaxFFT(maxFreq);
                                    }

                                }
                                //System.out.println();
                                //System.out.println("Основная частота " + index + " сигнала - " + maxFreq);


                                //System.out.println();
                                //нужно отмасштабировать и разобраться с коэффициентами
                            } catch (Exception e) {
                                e.printStackTrace();
                                //nothing
                            }
                        }
                        double finalPeak = peak;
                        double finalMaxRms = maxRms;
                        if (samples.length == Settings.getBuffer()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    controller.setPeakL(finalPeak);
                                    controller.setRmsL(finalMaxRms);
                                    controller.spectrumUpdate(buffer);
                                }
                            });
                        }
                        else{
                            buffer = new double[Settings.getBuffer() / 2];
                            buf = new byte[Settings.getBuffer() * 2];
                            samples = new float[Settings.getBuffer()];
                            fftBuffer = new Complex[Settings.getBuffer()];
                            N = fftBuffer.length;
                        }
                    }
                }
            }
        }
    }
}

