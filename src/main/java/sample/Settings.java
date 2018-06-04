package sample;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

//Полустатический класс настроек. Настройки ведь одни, ведь так? (Переделать в Singleton)
public class Settings {
    private static float samplerate;
    public static boolean isSignal = false;
    private int saplesize;
    private short channels;
    private boolean signed;
    private boolean bigEndian;
    private static AudioFormat format;
    private static TargetDataLine line;
    private static int buffer;
    private static boolean update;
    public static boolean isRunning = false;
    //602
    private static int high[] = {0, 0, 0, 1, 1, 1};
    private static int highF[] = {1200, 3000, 4000, 5000, 8000, 10000};
    //291
    private static int medium[] = {0, 1, 1, 1, 1, 1};
    private static int mediumF[] = {900, 2000, 3000, 4000, 7000, 12000};
    //170
    private static int low[] = {2, 2, 2, 3, 3, 3};
    private static int lowF[] = {700, 1200, 2000, 3000, 5000, 9000};

    private static int chooseAccuracyF[][] = {highF, mediumF, lowF};
    private static int chooseAccuracyD[][] = {high, medium, low};

    private static int chooseIndex = 0;
    private static String path = "C:\\Users\\1\\IdeaProjects\\untitled1\\src\\main\\resources\\screens\\";

    public Settings() {
        samplerate = 44100;
        saplesize = 16;
        channels = 2;
        signed = true;
        bigEndian = false;
        format = new AudioFormat(samplerate, saplesize, channels, signed, bigEndian);
        System.out.println(format.getFrameRate());
        System.out.println(format.getFrameSize());
        System.out.println("Установлен стандартный аудио-формат");
        buffer = 8192;
        System.out.println("Буфер = " + buffer);
    }

    public Settings(float sr, int ss, short ch, boolean bE) {
        samplerate = sr;
        saplesize = ss;
        channels = ch;
        signed = true;
        bigEndian = bE;
        try {
            format = new AudioFormat(samplerate, saplesize, channels, signed, bigEndian);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Будет установлено значение аудиоформата по умолчанию!");
            format = new AudioFormat(44100, 16, 2, true, false);
        }
    }

    public static String getPath(){return path;}
    public static int[][] getAccuracyF(){return chooseAccuracyF;}
    public static int[][] getAccuracyD(){return chooseAccuracyD;}
    public static void setChooseIndex(int index){chooseIndex = index;}
    public static int getChooseIndex(){return chooseIndex;}
    public static void setAccuracyF(int i, int j, int value){chooseAccuracyF[i][j] = value;}
    public static void setAccuracyD(int i, int j, int value){chooseAccuracyD[i][j] = value;}
    public static AudioFormat getFormat() {
        return format;
    }

    public static void setBuffer(int buffer) {
        Settings.buffer = buffer;
        System.out.println(Settings.buffer);
    }

    public static int getBuffer() {
        return buffer;
    }

    public static float getSampleRate() {
        return samplerate;
    }

    public static TargetDataLine getLine() {
        return line;
    }

    public static void setLine(TargetDataLine ln) {
        line = ln;
    }

    public static void setUpdate(boolean update) {
        Settings.update = update;
    }

    public static boolean getUpdate() {
        return update;
    }
}
