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


    public static void InitialazeMixer() {
        System.out.println("Инициализация аудио-системы. Пожалуйста, подождите.");
        format = Settings.getFormat();
        mixersList = new ArrayList<Mixer.Info>(Arrays.asList(AudioSystem.getMixerInfo()));

        for (int i = mixersList.size() - 1; i >= 0; i--) {
            try {
                selectedMixerInfo = mixersList.get(i);
                targetLine = AudioSystem.getTargetDataLine(format, selectedMixerInfo);
                targetLine.open(Settings.getFormat(),Settings.getBuffer());
                targetLine.close();
                System.out.println(targetLine.toString());
            } catch (Exception e) {
                mixersList.remove(i);
            }
        }

    }

    //геттер
    public static Mixer.Info GetDevice() {
        return selectedMixerInfo;
    }

    public static void ChooseDevice(int index) {
        selectedMixerInfo = mixersList.get(index);
        System.out.println(selectedMixerInfo);
    }

    public static ObservableList<String> GetAll() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (int i = 0; i < mixersList.size(); i++) {
            if (i == 0) {
                list.add("Встроенный стерео-микрофон");
            }
            if (i == 1) {
                list.add("Микрофон (Realtek High Definition Audio), version Unknown Version");
            }
            if (i == 2) {
                list.add("Стерео-микшер (Realtek High Definition Audio), version Unknown Version");
            }
            if (i > 2) list.add(mixersList.get(i).toString());
        }
        return list;
    }

}
