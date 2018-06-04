package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Set;

public class SettingsController {
    @FXML
    private ChoiceBox buffSize;
    @FXML
    private ChoiceBox mode;
    @FXML
    private TextField fr1;
    @FXML
    private TextField fr2;
    @FXML
    private TextField fr3;
    @FXML
    private TextField fr4;
    @FXML
    private TextField fr5;
    @FXML
    private TextField fr6;
    @FXML
    private TextField d1;
    @FXML
    private TextField d2;
    @FXML
    private TextField d3;
    @FXML
    private TextField d4;
    @FXML
    private TextField d5;
    @FXML
    private TextField d6;
    @FXML
    private TextField path;
    private ObservableList<Integer> buff;
    private int currentSize;
    private int i = 0;

    @FXML
    private Button closeButton;

    @FXML
    private void close(ActionEvent event)
    {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void initialize() {
        //BUFFER
        path.setText(Settings.getPath());
        buff = FXCollections.observableArrayList(2048, 4096, 8192, 16384, 32768, 65536);
        buffSize.setItems(buff);
        buffSize.setValue(Settings.getBuffer());
        buffSize.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                int k = buffSize.getSelectionModel().getSelectedIndex();
                currentSize = buff.get(k);
                System.out.println(currentSize);
            }
        });
        //MODE
        ObservableList<String> modeLst = FXCollections.observableArrayList("high", "medium", "low");
        mode.setItems(modeLst);
        i = Settings.getChooseIndex();
        mode.setValue(modeLst.get(i));
        fr1.setText(String.valueOf(Settings.getAccuracyF()[i][0]));
        fr2.setText(String.valueOf(Settings.getAccuracyF()[i][1]));
        fr3.setText(String.valueOf(Settings.getAccuracyF()[i][2]));
        fr4.setText(String.valueOf(Settings.getAccuracyF()[i][3]));
        fr5.setText(String.valueOf(Settings.getAccuracyF()[i][4]));
        fr6.setText(String.valueOf(Settings.getAccuracyF()[i][5]));
        d1.setText(String.valueOf(Settings.getAccuracyD()[i][0]));
        d2.setText(String.valueOf(Settings.getAccuracyD()[i][1]));
        d3.setText(String.valueOf(Settings.getAccuracyD()[i][2]));
        d4.setText(String.valueOf(Settings.getAccuracyD()[i][3]));
        d5.setText(String.valueOf(Settings.getAccuracyD()[i][4]));
        d6.setText(String.valueOf(Settings.getAccuracyD()[i][5]));
        mode.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                int i = mode.getSelectionModel().getSelectedIndex();
                System.out.println(i);
                fr1.setText(String.valueOf(Settings.getAccuracyF()[i][0]));
                fr2.setText(String.valueOf(Settings.getAccuracyF()[i][1]));
                fr3.setText(String.valueOf(Settings.getAccuracyF()[i][2]));
                fr4.setText(String.valueOf(Settings.getAccuracyF()[i][3]));
                fr5.setText(String.valueOf(Settings.getAccuracyF()[i][4]));
                fr6.setText(String.valueOf(Settings.getAccuracyF()[i][5]));
                d1.setText(String.valueOf(Settings.getAccuracyD()[i][0]));
                d2.setText(String.valueOf(Settings.getAccuracyD()[i][1]));
                d3.setText(String.valueOf(Settings.getAccuracyD()[i][2]));
                d4.setText(String.valueOf(Settings.getAccuracyD()[i][3]));
                d5.setText(String.valueOf(Settings.getAccuracyD()[i][4]));
                d6.setText(String.valueOf(Settings.getAccuracyD()[i][5]));
            }
        });
    }

    @FXML
    public void confirm() {
        Settings.setUpdate(true);
        Settings.setBuffer((int)buffSize.getValue());
        ArrayList<Integer> valuesF = new ArrayList<Integer>();
        valuesF.add(Integer.parseInt(fr1.getText()));
        valuesF.add(Integer.parseInt(fr2.getText()));
        valuesF.add(Integer.parseInt(fr3.getText()));
        valuesF.add(Integer.parseInt(fr4.getText()));
        valuesF.add(Integer.parseInt(fr5.getText()));
        valuesF.add(Integer.parseInt(fr6.getText()));
        ArrayList<Integer> valuesD = new ArrayList<Integer>();
        valuesD.add(Integer.parseInt(d1.getText()));
        valuesD.add(Integer.parseInt(d2.getText()));
        valuesD.add(Integer.parseInt(d3.getText()));
        valuesD.add(Integer.parseInt(d4.getText()));
        valuesD.add(Integer.parseInt(d5.getText()));
        valuesD.add(Integer.parseInt(d6.getText()));
        System.out.println(i);
        for (int j = 0; j < 6; j++)
        {
            Settings.setAccuracyF(i,j,valuesF.get(j));
            Settings.setAccuracyD(i,j,valuesD.get(j));
        }
    }
}
