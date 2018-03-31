package sample;

import javax.sound.sampled.AudioFormat;

//Полустатический класс настроек. Настройки ведь одни, ведь так? (Переделать в Singleton)
public class Settings {
    private static float samplerate;
    private int saplesize;
    private short channels;
    private boolean signed;
    private boolean bigEndian;
    private static AudioFormat format;
    private static int buffer;

    public Settings() {
        samplerate = 44100;
        saplesize = 16;
        channels = 2;
        signed = true;
        bigEndian = false;
        format = new AudioFormat(samplerate, saplesize, channels, signed, bigEndian);
        System.out.println("Установлен стандартный аудио-формат");
        buffer = 1024;
        System.out.println("Буфер = " + buffer);
    }

    public Settings(float sr, int ss, short ch, boolean sg, boolean bE) {
        samplerate = sr;
        saplesize = ss;
        channels = ch;
        signed = sg;
        bigEndian = bE;
        try {
            format = new AudioFormat(samplerate, saplesize, channels, signed, bigEndian);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Будет установлено значение аудиоформата по умолчанию!");
            format = new AudioFormat(44100, 16, 2, true, false);
        }
    }

    public static AudioFormat getFormat() {
        return format;
    }

    public static void setBuffer(int buffer) {
        Settings.buffer = buffer;
    }

    public static int getBuffer() {
        return buffer;
    }

    public static float getSampleRate() {return samplerate;}
}
