package GUI;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Objects;

public class HelperClass extends Application {
    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    /**
     * Creates an error dialog given a message input
     * @param message error message
     */
    public static void createDialog(String message){
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(HelperClass.class.getResource("/main/java/CSS/style.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog");
        dialog.setTitle("Error");
        dialog.setHeaderText(message);
        // Define the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonType.OK.getButtonData());

        // Add the button types to the dialog
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType);

        // Set the result converter to close the dialog when "OK" is clicked
        dialog.setResultConverter(dialogButton -> {
            return null; // Return null to close the dialog
        });

        dialog.showAndWait();
    }

    /**
     * Creates an arrow button given a URL.
     * @param url URL to the arrow image.
     * @return ImageView
     */
    public static ImageView createArrowButton (String url){
        Image rightButtonImage = new Image(Objects.requireNonNull(HelperClass.class.getResourceAsStream(url))); //RIGHT BUTTON
        ImageView rightButton = new ImageView(rightButtonImage);

        // Create a color adjustment effect
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.2);

        // Apply hover effect using setOnMouseEntered and setOnMouseExited
        rightButton.setOnMouseEntered(event -> rightButton.setEffect(colorAdjust));
        rightButton.setOnMouseExited(event -> rightButton.setEffect(null));
        rightButton.setFitWidth(70);
        rightButton.setFitHeight(70);
        rightButton.setPreserveRatio(true);
        BorderPane.setAlignment(rightButton, Pos.CENTER);
        return rightButton;
    }

    /**
     * Creates and returns a close button
     * @param url url of image
     * @return imageview
     */
    public static ImageView createXButton(String url){
        ImageView closeButton = HelperClass.createArrowButton("/main/java/IMAGES/circle-xmark.png");
        closeButton.setFitHeight(30);
        closeButton.setFitWidth(30);
        return closeButton;
    }
}
