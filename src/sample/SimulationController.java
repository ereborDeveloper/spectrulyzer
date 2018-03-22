package sample;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.When;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author
 */
public class SimulationController implements Initializable {

    @FXML
    private Button abortbutton;
    @FXML
    private Button homebutton;

    private volatile Service<String> backgroundThread;
    @FXML
    private TextArea consoleLogscreen;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progress;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        backgroundThread = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    StringBuilder results = new StringBuilder();
                    @Override
                    protected String call() throws Exception {
                        long i = 1;
                        String s = null;
                        while (i < 90) {
                            if(isCancelled()){
                                break;
                            }
                            double k = Math.sqrt(Math.pow(i, 2) / Math.sqrt(i));
                            results.append("i: ").append(i).append(" Count: ").append(k).append("\n");
                            updateValue(results.toString());
                            updateProgress((100*i)/90, 90);
                            Thread.sleep(100);
                            i++;
                        }

                        return results.toString();
                    }
                };
            }
        };
        consoleLogscreen.textProperty().bind(backgroundThread.valueProperty());
        progressBar.progressProperty().bind(backgroundThread.progressProperty());
        progress.textProperty().bind(new When(backgroundThread.progressProperty().isEqualTo(-1)).then("Unknown")
                .otherwise(backgroundThread.progressProperty().multiply(100).asString("%.2f%%")));
        backgroundThread.start();
        backgroundThread.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

            }
        });
        backgroundThread.restart();
        abortbutton.setDisable(false);
        homebutton.setDisable(true);
    }

    @FXML
    private void onAbortClicked(ActionEvent event) {
        if (event.getSource() == abortbutton) {
            backgroundThread.cancel();
        }
    }

    @FXML
    private void onHomeClicked(ActionEvent event) {
    }

}