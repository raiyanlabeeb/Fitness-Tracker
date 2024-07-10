package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static GUI.CalendarScene.*;
import static GUI.HelperClass.createXButton;

public class GoalScene extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    /**
     * Creates the goal management scene.
     * Allows you to make different types of goals and track it.
     * @param primaryStage the primary stage
     */
    public void makeGoalsScene(Stage primaryStage) {

        BorderPane mainBorderPane = new BorderPane();

        BorderPane subBorderPane = new BorderPane();
        mainBorderPane.setTop(subBorderPane);

        ImageView backButton = HelperClass.createArrowButton("/main/java/IMAGES/angle-double-small-left (1).png");
        backButton.setOnMouseClicked((event -> Platform.runLater(() -> CalendarScene.calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH))));
        subBorderPane.setLeft(backButton);

        Label currentGoalsLabel = new Label("Current Goals:"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("goal-top-section-label");
        }};

        //SO EVERYTHING IS ALIGNED
        Button placeHolder = new Button("");
        placeHolder.setPrefWidth(70);
        subBorderPane.setRight(placeHolder);
        placeHolder.setVisible(false);

        HBox.setMargin(backButton, new Insets(0, 50, 0, 0));

        subBorderPane.setCenter(currentGoalsLabel);
        BorderPane.setAlignment(subBorderPane.getCenter(), Pos.CENTER);
        subBorderPane.getStyleClass().add("goal-top-section");

        HBox bottomBox = new HBox();
        mainBorderPane.setBottom(bottomBox);

        Button newGoalButton = new Button("Create New Goal");
        newGoalButton.setMinHeight((double) WINDOW_HEIGHT /9);
        newGoalButton.getStyleClass().add("bottom-workout-button");
        bottomBox.getChildren().add(newGoalButton);
        bottomBox.getStyleClass().add("goal-bottom-section");
        bottomBox.setAlignment(Pos.CENTER);

        //CATEGORY MENU
        ContextMenu categoryMenu = new ContextMenu();
        categoryMenu.getStyleClass().add("context-menu");

        Label headerMenuItem = new Label("Select a category");
        CustomMenuItem headerItem = new CustomMenuItem(headerMenuItem, false);
        headerItem.getStyleClass().add("category-menu-header");

        MenuItem item1 = new MenuItem("Consistency");
        MenuItem item2 = new MenuItem("Weight");
        MenuItem item3 = new MenuItem("Strength");
        item1.getStyleClass().add("category-menu-option");
        item2.getStyleClass().add("category-menu-option");
        item3.getStyleClass().add("category-menu-option");

        categoryMenu.getItems().addAll(headerItem, item1, item2,item3);
        newGoalButton.setOnAction((event -> categoryMenu.show(newGoalButton, 650, 600)));

        consistencyGoalMenu(mainBorderPane, item1, newGoalButton); // Creates the consistency goal menu
        weightGoalMenu(mainBorderPane, item2, newGoalButton);
        //UPDATES CURRENT GOALS
        updateGoals(mainBorderPane);


        Scene mainScene = new Scene(mainBorderPane);
        mainScene.getStylesheets().add(Objects.requireNonNull(CalendarScene.class.getResource("/main/java/CSS/style.css")).toExternalForm());
        primaryStage.setScene(mainScene);
    }

    /**
     * Creates the weight goal menu
     * @param mainBorderPane the main border pane
     * @param weightButton the weight button
     * @param newGoalButton the goal button
     */
    private void weightGoalMenu(BorderPane mainBorderPane, MenuItem weightButton, Button newGoalButton) {
        // Create the form for the context menu
        Label weightLabel = new Label("I want to ");
        ComboBox<String> exerciseNameField = new ComboBox<>();
        exerciseNameField.setPromptText("Exercise Name");
        TextField poundsTextField = new TextField();
        poundsTextField.setAlignment(Pos.CENTER);
        poundsTextField.getStyleClass().add("consistency-textfield");
        poundsTextField.setPrefWidth((double) WINDOW_WIDTH / 7);
        poundsTextField.setPromptText("Weight");
        sql.addComboBoxExercises(exerciseNameField); // Add all exercises to the combo box
        exerciseNameField.getStyleClass().add("exercise-selection");

        Label weightLabel2 = new Label("pounds.");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.add(weightLabel, 0, 0);
        formGrid.add(exerciseNameField, 1, 0);
        formGrid.add(poundsTextField, 2, 0);
        formGrid.add(weightLabel2, 3, 0);

        CustomMenuItem formItem = new CustomMenuItem(formGrid, false);
        formItem.getStyleClass().add("consistency-label");

        // Add a submit button
        Button submitButton = new Button("Submit");

        // Create the context menu
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("consistency-menu");

        CustomMenuItem submitItem = new CustomMenuItem(submitButton, false);
        //When you press submit, consistency goal is now updated and will now show in CURRENT GOALS.
        submitButton.setOnAction((event -> {
            //Adds the goal to the database
            sql.addWeightGoal(exerciseNameField.getValue(), poundsTextField.getText());
            updateGoals(mainBorderPane);
            contextMenu.hide();
        }));
        submitButton.getStyleClass().add("submit-button");

        submitItem.getStyleClass().add("consistency-label");

        contextMenu.getItems().addAll(formItem, submitItem);

        weightButton.setOnAction((event -> contextMenu.show(newGoalButton, 450,600)));
    }

    /**
     * Creates the consistencyGoalMenu
     * @param consistencyButton the consistency button
     * @param mainBorderPane the main border pane
     * @param newGoalButton the new goal button
     */
    public void consistencyGoalMenu(BorderPane mainBorderPane, MenuItem consistencyButton, Button newGoalButton){
        // Create the form for the context menu
        Label consistencyLabel = new Label("I want to workout");
        TextField consistencyField = new TextField();
        consistencyField.getStyleClass().add("consistency-textfield");
        consistencyField.setPrefWidth((double) WINDOW_WIDTH /12);
        consistencyField.setAlignment(Pos.CENTER);
        Label consistencyLabel2 = new Label("times a week.");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.add(consistencyLabel, 0, 0);
        formGrid.add(consistencyField, 1, 0);
        formGrid.add(consistencyLabel2, 2, 0);

        CustomMenuItem formItem = new CustomMenuItem(formGrid, false);
        formItem.getStyleClass().add("consistency-label");

        // Add a submit button
        Button submitButton = new Button("Submit");

        // Create the context menu
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("consistency-menu");

        CustomMenuItem submitItem = new CustomMenuItem(submitButton, false);
        //When you press submit, consistency goal is now updated and will now show in CURRENT GOALS.
        submitButton.setOnAction((event -> {
            //Adds the goal to the database
            sql.addConsistencyGoal(consistencyField.getText());
            updateGoals(mainBorderPane);
            contextMenu.hide();
            consistencyField.clear();
        }));
        submitButton.getStyleClass().add("submit-button");

        submitItem.getStyleClass().add("consistency-label");

        contextMenu.getItems().addAll(formItem, submitItem);

        consistencyButton.setOnAction((event -> contextMenu.show(newGoalButton, 650,600)));
    }

    /**
     * Updates the current goals
     * @param main mainBorderPane
     */
    public void updateGoals(BorderPane main){
        FlowPane currentGoals = new FlowPane();
        currentGoals.setHgap(15);
        currentGoals.setVgap(15);
        try {
            //ADD CONSISTENCY GOAL
            if (sql.hasConsistencyGoal()) {
                //Updates the progress of the goal
                int progress = sql.updateConsistencyGoal(CURRENT_YEAR);
                ResultSet cGoal = sql.getConsistencyGoal();
                if (cGoal.next()) {
                    VBox consistencyCard = new VBox();

                    // Load the image
                    Image image = new Image(Objects.requireNonNull(CalendarScene.class.getResourceAsStream("/main/java/IMAGES/calendar-clock.png"))); //Calendar image

                    // Create an ImageView to display the image
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);

                    //THE X BUTTON
                    ImageView xButton = createXButton("/main/java/IMAGES/circle-xmark.png");
                    VBox.setMargin(xButton, new Insets(10));
                    xButton.setOnMouseClicked((event -> {
                        currentGoals.getChildren().remove(consistencyCard); // Removes the goal on click
                        sql.removeConsistencyGoal(); // Removes goal from database
                    }));

                    VBox.setMargin(imageView, new Insets(10, 0, 30, 0));
                    imageView.getStyleClass().add("card-header");

                    Label currentConsistencyGoal = new Label("I want to workout " + cGoal.getInt(1) + " times a week.");
                    currentConsistencyGoal.getStyleClass().add("card-goal");
                    currentConsistencyGoal.setTextAlignment(TextAlignment.CENTER);
                    currentConsistencyGoal.setWrapText(true);

                    Label progressLabel = new Label("Progress: " + progress + "/52");
                    progressLabel.getStyleClass().add("card-progress");

                    consistencyCard.getStyleClass().add("card");
                    consistencyCard.getChildren().addAll(imageView, currentConsistencyGoal, progressLabel, xButton);
                    consistencyCard.setMaxWidth((double) WINDOW_WIDTH / 3);
                    consistencyCard.setPadding(new Insets(10));

                    consistencyCard.setAlignment(Pos.CENTER);
                    currentGoals.getChildren().add(consistencyCard);
                    VBox.setMargin(consistencyCard, new Insets(10));
                }
            }

            if (sql.hasWeightGoal()) {
                ResultSet weightGoals = sql.getWeightGoals();
                while (!weightGoals.isAfterLast()) {
                    createWeightGoalCard(currentGoals, weightGoals.getString(1), weightGoals.getInt(2));
                    weightGoals.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        main.setCenter(currentGoals);
        main.getCenter().getStyleClass().add("current-goals-container");
    }

    /**
     * Creates a weight goal card
     * @param currentGoals current goals flowpane
     * @param exerciseName the exercise name
     * @param weight the weight
     * @throws SQLException
     */
    public void createWeightGoalCard(FlowPane currentGoals, String exerciseName, int weight) throws SQLException {

        VBox weightCard = new VBox();
        // Load the image
        Image image = new Image(Objects.requireNonNull(CalendarScene.class.getResourceAsStream("/main/java/IMAGES/gym (1).png"))); //Calendar image

        // Create an ImageView to display the image
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(70);
        imageView.setFitWidth(70);

        VBox.setMargin(imageView, new Insets(15, 0, 30, 0));
        imageView.getStyleClass().add("card-header");

        //THE X BUTTON
        ImageView xButton = createXButton("/main/java/IMAGES/circle-xmark.png");
        VBox.setMargin(xButton, new Insets(10));
        xButton.setOnMouseClicked((event -> {
            currentGoals.getChildren().remove(weightCard); // Removes the goal on click
            sql.removeWeightGoal(exerciseName); // Removes from the database
        }));

        Label currentWeightGoal = new Label("I want to " + exerciseName + " " + weight + " pounds.");
        weightCard.getStyleClass().add("card-goal");
        currentWeightGoal.setTextAlignment(TextAlignment.CENTER);
        currentWeightGoal.setWrapText(true);

        Label progressLabel = new Label("Progress: " + sql.getHighestWeight(exerciseName) + "/" + weight);
        progressLabel.getStyleClass().add("card-progress");

        weightCard.getStyleClass().add("card");
        weightCard.getChildren().addAll(imageView, currentWeightGoal, progressLabel, xButton);
        weightCard.setMaxWidth((double) WINDOW_WIDTH / 3);
        weightCard.setPadding(new Insets(10));

        VBox.setMargin(weightCard, new Insets(10));
        weightCard.setAlignment(Pos.CENTER);
        currentGoals.getChildren().add(weightCard);
    }
}
