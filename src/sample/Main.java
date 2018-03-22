package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

            primaryStage.setTitle("Spectrum Analizer");
            Scene scene = new Scene(root, 600, 400);
            scene.getStylesheets().add(0, "sample/Style.css");
            primaryStage.setMinHeight(500);
            primaryStage.setMinWidth(700);
            primaryStage.setScene(scene);
            primaryStage.show();
            AudioSpectrum spectrum = new AudioSpectrum();
            spectrum.Show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }


}
