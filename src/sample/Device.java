package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sun.text.normalizer.UTF16;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Device {
    private static Mixer.Info selectedMixerInfo;
    private static ArrayList<Mixer.Info> mixersList;
    //format можно двинуть в настройки в принципе
    private static AudioFormat format;
    private static TargetDataLine targetLine;

    //getter
    public static Mixer.Info GetDevice() {
        return selectedMixerInfo;
    }

    public static void ChooseDevice(int index) {

        //select mixer chanel 5 for Realtek
        selectedMixerInfo = mixersList.get(index);
    }

    public static void InitialazeMixer() {

        //Создаем аудио-формат по умолчанию

        format = Settings.getFormat();
        mixersList = new ArrayList<Mixer.Info>(Arrays.asList(AudioSystem.getMixerInfo()));
        for (int i = mixersList.size() - 1; i >= 0; i--) {
            try {
                selectedMixerInfo = mixersList.get(i);
                targetLine = AudioSystem.getTargetDataLine(format, selectedMixerInfo);
            } catch (Exception e) {
                mixersList.remove(i);
            }
        }

    }

    public static ObservableList<String> GetAll() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (int i = 0; i < mixersList.size(); i++) {
            list.add(mixersList.get(i).toString());
        }
        return list;
    }



    private static void writeBytesToFile(byte[] bFile, String fileDest) {

        try (FileOutputStream fileOuputStream = new FileOutputStream(fileDest)) {
            fileOuputStream.write(bFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static short[] shortMe(byte[] bytes) {
        short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        for (int i = 0; i < out.length; i++) {
            out[i] = bb.getShort();
        }
        return out;
    }

    public static float[] floatMe(short[] pcms) {
        float[] floaters = new float[pcms.length];
        for (int i = 0; i < pcms.length; i++) {
            floaters[i] = ((float) pcms[i]) / 0x8000;
        }
        return floaters;
    }
}
