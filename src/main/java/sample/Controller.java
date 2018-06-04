package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import javax.sound.sampled.*;

import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Controller {
    //ROOT
    @FXML
    private BorderPane root;
    //device ChoiceBox
    @FXML
    private ChoiceBox dev;
    //window ChoiceBox
    @FXML
    private ChoiceBox window;
    //fade ChoiceBox
    @FXML
    private ChoiceBox fadeSpeed;
    //accuracy ChoiceBox
    @FXML
    private ChoiceBox accuracy;

    //METERS
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
    @FXML
    private HBox Levels;
    //
    //SPECTRUM
    @FXML
    private AreaChart<Double, Double> spectrum;
    @FXML
    private LogarithmicAxis X = new LogarithmicAxis();
    @FXML
    private NumberAxis Y = new NumberAxis();
    //
    //HZ
    @FXML
    private TextField fromHz;
    @FXML
    private TextField toHz;
    //Labels
    @FXML
    private Label fromLbl;
    @FXML
    private Label maxFreq;

    //Buttons
    @FXML
    private Button settings;
    @FXML
    private Button freezeButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button openButton;
    @FXML
    private Button showLabelsButton;
    @FXML
    private Button hideButton;
    @FXML
    private Button legendButton;
    @FXML
    private Button diffButton;
    //toolbar
    @FXML
    private ToolBar tools;
    //Legend pane
    @FXML
    private Pane labelPane;
    //FIELDS
    //spectrum
    private XYChart.Series<Double, Double> series;
    //fade
    private float[] fadeArray = {0.97f, 0.989f, 0.996f, 0.999f};
    private float fade;
    private float fadeTemp;
    //SYSTEM VARS
    private int N;
    private Thread startCapturing;
    private int accD[];
    private int accF[];
    private double[] firstArray;
    private double maxFFT;
    private int winIndex = 17;
    private IO io;
    private ArrayList<double[]> currentSpectrums;
    private Stage settingsStage;
    private String str;
    private FileChooser fileChooser;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean fir = true;
    //offset
    double maxF;
    double x;
    //HZ
    private double maxFreqDb;
    private double maxFreqAmp = 0.0;
    private int maxFreqIndex = 0;
    private int index = 0;
    //Styling in code
    private ArrayList<Label> labelArrayList;
    private String[] colors = {"#32C2CC", "#FFF674", "#FF12D8", "#00F674", "#FFFFFF"};

    //METHODS
    @FXML
    private void showLegend(ActionEvent event) {
        labelPane.setVisible(!labelPane.isVisible());
    }

    @FXML
    public void showLabels(ActionEvent event) {
        maxFreq.setVisible(!maxFreq.isVisible());
    }

    @FXML
    private void diff(ActionEvent event) {
        double[] values = new double[firstArray.length];
        if (currentSpectrums.size() > 1) {
            try {
                for (int i = 0; i < currentSpectrums.get(currentSpectrums.size() - 1).length; i++) {
                    //if(firstArray[i] - currentSpectrums.get(currentSpectrums.size() - 1)[i]<0)
                    values[i] = Math.abs(currentSpectrums.get(currentSpectrums.size() - 1)[i] - currentSpectrums.get(currentSpectrums.size() - 2)[i]);
                }
                //currentSpectrums.add(firstArray);
                XYChart.Series ser = new XYChart.Series();
                addToChart(ser, values);
                spectrum.getData().add(ser);
                currentSpectrums.add(values);
                Label newLabel = new Label("- DIFFERENCE");
                newLabel.setRotate(180);
                newLabel.setTranslateY(labelArrayList.size() * 20);
                newLabel.setStyle("-fx-text-fill:" + colors[labelArrayList.size()] + ";-fx-font-size:8;-fx-background-color:#18181877;-fx-padding:2 4;-fx-border-color:" + colors[labelArrayList.size()] + ";\n" +
                        "  -fx-border-width: 0.3;\n" +
                        "  -fx-border-style: solid;");
                labelArrayList.add(newLabel);
                labelPane.getChildren().add(newLabel);
            }
            catch(Exception e)
            {
                System.out.println("Спектры имеют разную точность снятия! Вычитание невозможно");
            }
        }
    }

    @FXML
    private void open(ActionEvent event) {
        ArrayList<String> list = new ArrayList<String>();
        File file = fileChooser.showOpenDialog(openButton.getScene().getWindow());
        if (file != null) {
            try {
                list = io.spectrumFromFile(file.getPath());
                double[] values = new double[list.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Double.valueOf(list.get(i));
                }
                XYChart.Series ser = new XYChart.Series();
                currentSpectrums.add(values);
                addToChart(ser, values);
                spectrum.getData().add(ser);
                Label newLabel = new Label("-" + file.getPath().substring(Settings.getPath().length()).split("-")[0].split(".sa")[0].toUpperCase());
                newLabel.setRotate(180);
                newLabel.setTranslateY(labelArrayList.size() * 20);
                newLabel.setStyle("-fx-text-fill:" + colors[labelArrayList.size()] + ";-fx-font-size:8;-fx-background-color:#18181877;-fx-padding:2 4;-fx-border-color:" + colors[labelArrayList.size()] + ";\n" +
                        "  -fx-border-width: 0.3;\n" +
                        "  -fx-border-style: solid;");
                labelArrayList.add(newLabel);
                labelPane.getChildren().add(newLabel);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Не удалось загрузить спектр");
            }
        }

    }

    @FXML
    public void delete(ActionEvent event) {
        if (io != null && spectrum.getData().size() > 1) {
            spectrum.getData().remove(spectrum.getData().size() - 1, spectrum.getData().size());
            if (labelArrayList.size() > 1) {
                labelPane.getChildren().remove(labelArrayList.get(labelArrayList.size() - 1));
                labelArrayList.remove(labelArrayList.size() - 1);
            }
        }
        if (currentSpectrums.size() != 0) currentSpectrums.remove(currentSpectrums.size() - 1);
    }

    @FXML
    public void save(ActionEvent event) {
        if (spectrum.getData().size() < 5) {
            double[] span = new double[firstArray.length];

            for (int i = 0; i < span.length; i++) {
                span[i] = firstArray[i];
            }
            currentSpectrums.add(span);
            XYChart.Series ser = new XYChart.Series();
            addToChart(ser, span);
            spectrum.getData().add(ser);
            if (fade == 1) {
                fade = fadeTemp;
            }
            if (io != null) {
                try {
                    String path;
                    TextInputDialog dialog = new TextInputDialog("spectrum");
                    dialog.initStyle(StageStyle.UNDECORATED);
                    dialog.setHeaderText("Saving spectrum");
                    dialog.setContentText("Spectrum info:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        path = result.get() + ".sa@" + Settings.getBuffer() / 2;
                        ser.setName(result.get());
                        io.spectrumToFile(path, span);
                        Label newLabel = new Label("-" + result.get().toUpperCase());
                        newLabel.setRotate(180);
                        newLabel.setTranslateY(labelArrayList.size() * 20);
                        newLabel.setStyle("-fx-text-fill:" + colors[labelArrayList.size()] + ";-fx-font-size:8;-fx-background-color:#18181877;-fx-padding:2 4;-fx-border-color:" + colors[labelArrayList.size()] + ";\n" +
                                "  -fx-border-width: 0.3;\n" +
                                "  -fx-border-style: solid;");
                        labelArrayList.add(newLabel);
                        labelPane.getChildren().add(newLabel);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Спектр не сохранен");
                }
            }
        }
    }

    @FXML
    public void freeze(ActionEvent event) {
        if (fade == 1) {
            fade = fadeTemp;
        } else {
            fadeTemp = fade;
            fade = 1;
        }
    }

    @FXML
    public void openSettings(ActionEvent event) {
        if (!settingsStage.isShowing()) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/settings.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                settingsStage.setScene(new Scene(root1));
                settingsStage.show();
                root1.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        xOffset = event.getSceneX();
                        yOffset = event.getSceneY();
                    }
                });
                root1.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        settingsStage.setX(event.getScreenX() - xOffset);
                        settingsStage.setY(event.getScreenY() - yOffset);
                    }
                });
            } catch (Exception e) {
                System.out.println("Ошибка загрузки окна настроек!");
                e.printStackTrace();
            }
        } else {
            settingsStage.requestFocus();
        }
    }

    /////////////////////////
    @FXML
    public void initialize() {
        currentSpectrums = new ArrayList();
        Settings settings = new Settings();
        N = Settings.getBuffer() / 2;
        //LEGEND
        labelPane.setRotate(180);
        labelPane.setTranslateX(40);
        labelPane.setTranslateY(50);
        labelArrayList = new ArrayList<Label>();
        Label lbl = new Label("- INPUT");
        lbl.setStyle("-fx-text-fill:" + colors[0] + ";-fx-font-size:8;-fx-background-color:#18181877;-fx-padding:2 4;-fx-border-color:" + colors[0] + ";\n" +
                "  -fx-border-width: 0.3;\n" +
                "  -fx-border-style: solid;");
        lbl.setRotate(180);
        labelArrayList.add(lbl);
        labelPane.getChildren().add(lbl);
        //
        //CACHING
        spectrum.setCache(false);
        spectrum.setCacheShape(false);
        spectrum.setCacheHint(CacheHint.SPEED);
        spectrum.setAnimated(false);
        //SETTINGS STAGE
        settingsStage = new Stage();
        settingsStage.initStyle(StageStyle.UNDECORATED);
        settingsStage.setOpacity(0.85);
        //OPEN SPECTRUM SETTINGS
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spectrum", "*.sa*"));
        fileChooser.setInitialDirectory(new File(Settings.getPath()));
        //INIT SCALING
        maxFFT = N;
        //IO
        io = new IO(Settings.getPath());
        //Medium resolution by default
        accD = Settings.getAccuracyD()[1];
        accF = Settings.getAccuracyF()[1];
        //WINDOW FUNCTIONS
        //EXP BY DEFAULT
        ObservableList<String> windowFunctions = FXCollections.observableArrayList("none", "Rectangular", "Triangular", "Parzen", "Welch", "Sine", "Hann", "Hamming", "Blackman", "Nuttall", "Blackman–Nuttall", "Blackman–Harris", "Flat top", "Gaussian", "Approximate confined Gaussian", "Tukey", "Planck-taper", "Exponential", "Bartlett–Hann", "Hann–Poisson");
        window.setItems(windowFunctions);
        window.setValue(windowFunctions.get(17));
        window.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                winIndex = window.getSelectionModel().getSelectedIndex();
                System.out.println(winIndex);
                series.getData().clear();
                addToChart(series, firstArray);
            }
        });
        ////////////
        //RESOLUTION
        ObservableList<String> resolution = FXCollections.observableArrayList("high", "medium", "low");
        accuracy.setItems(resolution);
        accuracy.setValue(resolution.get(1));
        accuracy.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                series.getData().clear();
                index = accuracy.getSelectionModel().getSelectedIndex();
                accD = Settings.getAccuracyD()[index];
                accF = Settings.getAccuracyF()[index];
                addToChart(series, firstArray);
                Settings.setChooseIndex(index);
            }
        });
        ////////////
        //ARRAY INIT
        firstArray = new double[N];
        series = new XYChart.Series();
        //CAPTURE THREAD INIT
        startCapturing = new Thread(new Recorder(this));
        Settings.setLine(null);
        startCapturing.start();
        //GET DEVICES
        Device.InitialazeMixer();
        ObservableList<String> list = Device.GetAll();
        list.add("none");
        dev.setValue("none");
        dev.setItems(list);
        nullMeters();
        //SPECTRUM SETTINGS
        spectrum.setCreateSymbols(false);
        spectrum.getData().add(series);
        //AXIS SETTINGS
        Y.setLabel("dB");
        X.setLabel("Hz");
        Y.setAutoRanging(false);
        Y.setUpperBound(0);
        Y.setLowerBound(-90);
        X.setTickLabelsVisible(true);
        Y.setTickLabelsVisible(true);
        //INIT ARR VALUES
        nullArray();
        //ADDING INPUT TO SPECTRUM
        addToChart(series, firstArray);
        ///////////
        //FADE INIT
        fade = fadeArray[1];
        ObservableList<String> fadeList = FXCollections.observableArrayList("fast", "medium", "slow", "very slow");
        fadeSpeed.setItems(fadeList);
        fadeSpeed.setValue("medium");
        //FADE CHANGE
        fadeSpeed.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                int index = fadeSpeed.getSelectionModel().getSelectedIndex();
                fade = fadeArray[index];
            }
        });
        ///////////////
        //DEVICE CHANGE
        dev.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //Сделать проверку на доступность линии, и пока линия недоступна, ждать
                int index = dev.getSelectionModel().getSelectedIndex();
                //Choose "none"
                if (index == dev.getItems().size() - 1) {
                    if (Settings.getLine() != null) {
                        Settings.getLine().stop();
                        Settings.getLine().close();
                        Settings.setLine(null);
                        nullMeters();
                    }
                }
                //not "none"
                else {
                    Device.ChooseDevice(index);
                    try {
                        Settings.setLine(AudioSystem.getTargetDataLine(Settings.getFormat(), Device.GetDevice()));
                        Settings.isRunning = true;
                        Settings.isSignal = true;
                        System.out.println(Settings.getLine());
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                        System.err.println(e);
                    }
                }
            }
        });
    }

    /***********INIT*END************/
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

    public void nullArray() {
        for (int i = 0; i < firstArray.length; i++) {
            firstArray[i] = 0.0000001;
        }
    }

    public void nullSeries() {
        for (int i = 0; i < series.getData().size(); i++) {
            series.getData().set(i, new XYChart.Data(1, Y.getLowerBound()));
        }
    }

    public void spectrumUpdate(double buffer[]) {
        if (Settings.getUpdate()) {
            N = Settings.getBuffer() / 2;
            firstArray = new double[N];
            series.getData().clear();
            index = accuracy.getSelectionModel().getSelectedIndex();
            accD = Settings.getAccuracyD()[index];
            accF = Settings.getAccuracyF()[index];
            for (int z = 0; z < 6; z++) {
                System.out.println("Freq: " + accF[z] + "\t dots: " + accD[z] + "\t d:" + Settings.getBuffer() / 2);
            }
            addToChart(series, firstArray);
            Settings.setUpdate(false);
        }
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > maxFreqAmp) {
                maxFreqAmp = buffer[i];
                maxFreqIndex = i % N;
                setMaxFreq();
            }
            if (buffer[i] > firstArray[i]) {
                firstArray[i] = buffer[i];
            } else {
                //K - коэффициент спада (зависит от частоты, ибо низкие частоты всегда самые громкие)
                if (firstArray[i] > 2 * (1 - fade) * Math.pow(firstArray[i], 1.5)) ;
                firstArray[i] -= 2 * (1 - fade) * Math.pow(firstArray[i], 1.5);
            }
        }
        if (maxFreqAmp > 2 * (1 - fade) * Math.pow(maxFreqAmp, 1.5)) {
            maxFreqAmp -= 2 * (1 - fade) * Math.pow(maxFreqAmp, 1.5);
        }
        int r = 0;
        series.getData().set(0, new XYChart.Data(10, 20 * Math.log10(firstArray[0] / maxFFT)));
        for (int i = 2; i < series.getData().size() + 1; i++) {
            r++;
            float max = r * 44100.0f / N;
            int k = 0;
            if (max > accF[0]) k += accD[0];
            if (max > accF[1]) k += accD[1];
            if (max > accF[2]) k += accD[2];
            if (max > accF[3]) k += accD[3];
            if (max > accF[4]) k += accD[4];
            if (max > accF[5]) k += accD[5];
            if (max >= 22050) {
                max = 22050;
                System.out.println(series.getData().size());
                break;
            }
            double db = 20 * Math.log10(firstArray[r] / maxFFT);
            int t = 0;
            for (int j = 0; j < k; j++) {
                double temp = 20 * Math.log10(firstArray[r] / maxFFT);
                if (temp > db) {
                    db = temp;
                    t = k - j;
                }
                r++;
            }
            max = (r - t) * 44100.0f / N;
            //System.out.println(((double)i/series.getData().size()));
            //(1+0*((double)i/series.getData().size()))
            series.getData().set(i - 1, new XYChart.Data(max, db));
        }
        maxFreq.setTranslateY(spectrum.getHeight() - Math.log10(maxFreqAmp) / Math.log10(maxFFT) * spectrum.getHeight());
        str = String.valueOf(20 * Math.log10(maxFreqAmp / maxFFT));
        if (str.length() > 5) str = str.substring(0, 5);
        maxFreq.setText(String.valueOf(maxF).split("\\.")[0] + "Hz | " + str + "dB");
    }

    void addToChart(XYChart.Series series, double[] array) {
        series.getData().add(new XYChart.Data(10, 20 * Math.log10(firstArray[0] / maxFFT)));
        for (int r = 1; r < array.length; r += 1) {
            float max = r * 44100.0f / array.length;
            int k = 0;
            if (max > accF[0]) k += accD[0];
            if (max > accF[1]) k += accD[1];
            if (max > accF[2]) k += accD[2];
            if (max > accF[3]) k += accD[3];
            if (max > accF[4]) k += accD[4];
            if (max > accF[5]) k += accD[5];
            if (max >= 22050) {
                max = 22050;
                double db = 20 * Math.log10(array[array.length - 1] / maxFFT);
                break;
            }
            double db = 20 * Math.log10(array[r] / maxFFT);
            int t = 0;
            for (int i = 0; i < k; i++) {
                double temp = 20 * Math.log10(array[r] / maxFFT);
                if (temp > db) {
                    db = temp;
                    t = k - i;
                }
                r++;
            }
            max = (r - t) * 44100.0f / array.length;
            series.getData().add(new XYChart.Data(max, db));
        }
        System.out.println(series.getData().size() + " точек спектра.");
    }

    public float getFade() {
        return fade;
    }

    public void setPeakL(double peak) {
        peakL.setY((peakL.getY() + peakL.getHeight() - peak * 335));
        peakL.setHeight(((peak * 335)));
    }

    public void setRmsL(double rms) {
        rmsL.setY((rmsL.getY() + rmsL.getHeight() - rms * 335));
        rmsL.setHeight(((rms * 335)));
    }

    public void setBounds(ActionEvent actionEvent) {
        int from = 10, to = 22050;
        try {
            from = Integer.valueOf(fromHz.getText());
        } catch (Exception e) {
            from = 20;
            fromHz.setText("20");
        }
        try {
            to = Integer.valueOf(toHz.getText());
        } catch (Exception e) {
            to = 22050;
            toHz.setText("22050");
        } finally {
            if (from < 10) {
                fromHz.setText("10");
                from = 10;
            }
            if (to > 22050) {
                toHz.setText("22050");
                to = 22050;
            }
            if (from < to) {
                X.setLowerBound(from);
                X.setUpperBound(to);
            }
        }
    }

    public void setMaxFreq() {
        maxF = maxFreqIndex * 44100.0f / (Settings.getBuffer() / 2);
        if (maxF > 22050) maxF = 0;
        x = (1.142 * spectrum.getWidth() * 1.15) * (Math.log10(maxF) / Math.log10(X.getUpperBound() - X.getLowerBound())) - 0.26 * Math.pow(spectrum.getWidth(), 1.03) - 65;
        if (x > 0) {
            this.maxFreq.setTranslateX(x);
        }
    }

    public int getWinIndex() {
        return winIndex;
    }

    public void setMaxFFT(double max) {
        if (max > maxFFT) {
            maxFFT = max;
            System.out.println(max);
            //Расписать про нахождение максимального коэффициента
        }
    }
}
