package sample;

import java.io.*;
import java.util.ArrayList;

public class IO {
    private String mainPath;

    public IO() {

    }

    public IO(String path) {
        mainPath = path;
    }

    public ArrayList spectrumFromFile(String path) throws IOException {
        File file = new File(path);
        file.createNewFile();
        BufferedReader in = null;
        ArrayList values = new ArrayList();
        String line;
        try {
            int i = 0;
            in = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
            while ((line = in.readLine()) != null) {
                values.add(line);
                i++;
            }
        } catch (IOException a) {
            a.printStackTrace();
        } finally {
            in.close();
        }
        return values;
    }

    public void spectrumToFile(String path, double[] values) throws IOException {
        File file = new File(mainPath + path);
        file.createNewFile();
        BufferedWriter in = null;
        try {
            in = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mainPath + path), "CP1251"));
            for (int i = 0; i < values.length; i++) {
                in.write(String.valueOf(values[i]));
                in.newLine();
            }
        } catch (IOException a) {
            a.printStackTrace();
        } finally {
            in.close();
        }
    }
}
