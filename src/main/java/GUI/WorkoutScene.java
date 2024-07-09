package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static GUI.FitnessView.*;

public class WorkoutScene extends Application{

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
     * Adds an exercise row and adds to SQL database
     * @param grid the main gridPane
     */
    public void addExercise(GridPane grid, TextField date, TextField title){

        //FOR SQL
        AtomicReference<String> rep_string = new AtomicReference<>();
        AtomicReference<String> set_string = new AtomicReference<>();
        AtomicReference<String> weight_string = new AtomicReference<>();
        AtomicBoolean progressiveOverload = new AtomicBoolean(false);

        int numRows = grid.getRowCount();

        if (grid.getRowCount() != 0){
            //If num rows doesn't equal 0, we have to add TextFields not labels
            TextField nameTextField = new TextField()
            {{
                setPromptText("Name");
                setMinHeight(EXERCISE_BUTTON_HEIGHT);
                setPrefWidth((double) WINDOW_WIDTH / 4);
                setAlignment(Pos.CENTER);
                setFont(new Font(EXERCISE_FONT_SIZE));
                getStyleClass().add("exercise-name-button");

                Platform.runLater(this::requestFocus);
                //When the field is created, autofocus
                Platform.runLater(() -> exercise_field = this);

                //Adds to SQL
                focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) { // When the TextField loses focus
                        exercise_field = this;
                        //ADD TO SQL DATABASE
                        if (rep_string.get() != null && set_string.get() != null && weight_string.get() != null && date.getText() != null && title.getText() != null) {
                            sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                        }
                    }
                });
            }};

            grid.add(nameTextField, 0, grid.getRowCount());

            TextField setTextField = new TextField()
            {{
                setText("1");
                setMinHeight(EXERCISE_BUTTON_HEIGHT);
                setPrefWidth((double) WINDOW_WIDTH /4);
                setAlignment(Pos.CENTER);
                setFont(new Font(EXERCISE_FONT_SIZE));
                getStyleClass().add("exercise-button");
                set_string.set(this.getText());
                //Adds to SQL
                focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) { // When the TextField loses focus
                        //ADD TO SQL DATABASE
                        if (rep_string.get() != null && exercise_field!=null && weight_string.get() != null && date.getText() != null && title.getText() != null) {
                            sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                        }
                    }
                });
            }};

            grid.add(setTextField, 1, numRows);

            TextField repTextField = new TextField()
            {{
                setPromptText("Number of Reps");
                setMinHeight(EXERCISE_BUTTON_HEIGHT);
                setPrefWidth((double) WINDOW_WIDTH /4);
                setAlignment(Pos.CENTER);
                setFont(new Font(EXERCISE_FONT_SIZE));
                getStyleClass().add("exercise-button");

                //Adds to SQL
                focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) { // When the TextField loses focus
                        rep_string.set(this.getText());
                        //ADD TO SQL DATABASE
                        if (exercise_field!=null && set_string.get() != null && weight_string.get() != null && date.getText() != null && title.getText() != null) {
                            sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                        }
                    }
                });
            }};

            grid.add(repTextField, 2, numRows);

            TextField weightTextField = getWeightTextField(weight_string, rep_string, set_string, date, title, progressiveOverload, setTextField, repTextField);

            grid.add(weightTextField, 3, numRows);
            grid.setAlignment(Pos.TOP_CENTER);

            detectTab(weightTextField, grid, date, title);
        }
        else {
            addExerciseLabels(grid);
        }
    }

    /**
     * Observes when tab is pressed on the 'WEIGHT' text field, so it creates a new set.
     */
    public void detectTab(TextField textField, GridPane grid, TextField date, TextField title){
        textField.setOnKeyPressed((event -> {
            if (event.getCode() == KeyCode.TAB){
                Platform.runLater(() -> addSet(grid, date, title));
            }
        }));
    }

    /**
     * Adds the labels to exercise screen
     * @param grid the grid pane
     */
    public void addExerciseLabels(GridPane grid){
        grid.add(new Label("NAME")
        {{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-label");
        }}, 0, 0);

        grid.add(new Label("SET")
        {{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-label");
        }}, 1, 0);

        grid.add(new Label("REPS")
        {{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-label");
        }}, 2, 0);

        grid.add(new Label("WEIGHT")
        {{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-label");
        }}, 3, 0);

    }

    /**
     *
     * @param grid the grid pane
     * @param rowSpan rowspan of the current name textfield
     */
    public void push (GridPane grid, int rowSpan){
        int currentIndex = GridPane.getRowIndex(exercise_field) + rowSpan - 1;
        ObservableList<Node> children = grid.getChildren();
//        //for every row past the current one
        for (int i = 1; i < children.size(); i++){
            //If the row of the child is after the main row, increase the row index by the rowspan
            if (GridPane.getRowIndex(children.get(i)) > currentIndex){
                GridPane.setRowIndex((children.get(i)), GridPane.getRowIndex(children.get(i)) + rowSpan);
            }
        }
    }

    /**
     * Add set button
     * @param grid the grid pane
     */
    public void addSet(GridPane grid, TextField date, TextField title){
        //If the user selected nothing
        if (exercise_field == null){
            HelperClass.createDialog("SELECT AN EXERCISE TO ADD SET");
            return;
        }

        //For SQL
        AtomicReference<String> set_string = new AtomicReference<>();
        AtomicReference<String> rep_string = new AtomicReference<>();
        AtomicReference<String> weight_string = new AtomicReference<>();
        AtomicBoolean progressiveOverload = new AtomicBoolean(false);

        //Sets default value for rowspan, so we don't have to deal with null
        int rowSpan = 1;
        int numRows = grid.getRowCount();
        //If the row span is not null, account for it in the calculation.
        if (GridPane.getRowSpan(exercise_field) != null){
            //updates row span
            rowSpan = GridPane.getRowSpan(exercise_field);
            if (GridPane.getRowIndex(exercise_field) + rowSpan != numRows){
                //makes room
                push(grid, rowSpan);
            }
        }
        //If it is null, don't account for it
        else if (GridPane.getRowIndex(exercise_field) != numRows - 1){
            push(grid, rowSpan);
        }

        //Edit the button that the mouse is clicked on
        GridPane.setRowSpan(exercise_field, rowSpan + 1);
        grid.setGridLinesVisible(false);
        exercise_field.setMinHeight(EXERCISE_BUTTON_HEIGHT * (rowSpan + 1));
        int finalRowSpan = rowSpan + 1;
        TextField setTextField = new TextField()
        {{
            setText(finalRowSpan + "");
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH / 4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-button");
            set_string.set(this.getText());

            focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // When the TextField loses focus
                    //ADD TO SQL DATABASE
                    if (rep_string.get() != null && exercise_field != null && weight_string.get() != null && date.getText() != null && title.getText() != null) {
                        sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                    }
                }
            });
        }};

        //Place in the row after
        grid.add(setTextField, 1, GridPane.getRowIndex(exercise_field) + rowSpan);

        TextField repTextField = new TextField()
        {{
            setPromptText("Number of Reps");
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-button");
            //When the field is created, autofocus
            Platform.runLater(this::requestFocus);

            focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // When the TextField loses focus
                    rep_string.set(this.getText());
                    //ADD TO SQL DATABASE
                    if (exercise_field != null && set_string.get() != null && weight_string.get() != null && date.getText() != null && title.getText() != null) {
                        sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                    }
                }
            });
        }};
        grid.add(repTextField, 2, GridPane.getRowIndex(exercise_field) + rowSpan);

        TextField weightTextField = getWeightTextField(weight_string, rep_string, set_string, date, title, progressiveOverload, setTextField, repTextField);

        detectTab(weightTextField, grid, date, title);
        grid.add(weightTextField, 3, GridPane.getRowIndex(exercise_field) + rowSpan);
    }

    /**
     * Helper function for workoutScene
     * @param weight_string weight
     * @param rep_string reps
     * @param set_string sets
     * @param date dateTextField
     * @param title titleTextField
     * @param progressiveOverload true or false
     * @param setTextField setTextField
     * @param repTextField repTextField
     * @return TextField
     */
    private TextField getWeightTextField(AtomicReference<String> weight_string, AtomicReference<String> rep_string, AtomicReference<String> set_string, TextField date, TextField title, AtomicBoolean progressiveOverload, TextField setTextField, TextField repTextField){
        return new TextField()
        {{
            setPromptText("Weight");
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH / 4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-button");

            focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // When the TextField loses focus
                    weight_string.set(this.getText());
                    //ADD TO SQL DATABASE
                    if (rep_string.get() != null && exercise_field != null && set_string.get() != null && date.getText() != null && title.getText() != null) {
                        if (sql.isProgressiveOverload(exercise_field.getText(), date.getText(), Integer.parseInt(set_string.get()), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()))) {
                            progressiveOverload.set(true);
                            if (progressiveOverload.get()) {
                                setTextField.getStyleClass().add("overload-style");
                                getStyleClass().add("overload-style");
                                repTextField.getStyleClass().add("overload-style");
                            }
                        }
                        sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                    }
                }
            });
        }};
    }

    /**
     * @param grid the grid pane
     * @param date the date
     * @param title the title
     * @param set_id the set id
     * @param rep_num the rep number
     * @param weight_num the weight number
     */
    public void addSetforLoad(GridPane grid, TextField date, TextField title, int set_id, int rep_num, double weight_num) {

        //For SQL
        AtomicReference<String> set_string = new AtomicReference<>();
        AtomicReference<String> rep_string = new AtomicReference<>();
        AtomicReference<String> weight_string = new AtomicReference<>();
        AtomicBoolean progressiveOverload = new AtomicBoolean(false);

        //Sets default value for rowspan, so we don't have to deal with null
        int rowSpan = 1;
        int numRows = grid.getRowCount();
        //If the row span is not null, account for it in the calculation.
        if (GridPane.getRowSpan(exercise_field) != null) {
            //updates row span
            rowSpan = GridPane.getRowSpan(exercise_field);
            if (GridPane.getRowIndex(exercise_field) + rowSpan != numRows) {
                //makes room
                push(grid, rowSpan);
            }
        }
        //If it is null, don't account for it
        else if (GridPane.getRowIndex(exercise_field) != numRows - 1) {
            push(grid, rowSpan);
        }

        //Edit the button that the mouse is clicked on
        GridPane.setRowSpan(exercise_field, rowSpan + 1);
        grid.setGridLinesVisible(false);
        exercise_field.setMinHeight(EXERCISE_BUTTON_HEIGHT * (rowSpan + 1));
        TextField setTextField = new TextField()
        {{
            setText(set_id + "");
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH / 4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-button");
            set_string.set(this.getText());

            focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // When the TextField loses focus
                    //ADD TO SQL DATABASE
                    if (rep_string.get() != null && exercise_field != null && weight_string.get() != null) {
                        sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                    }
                }
            });
        }};

        //Place in the row after
        grid.add(setTextField, 1, GridPane.getRowIndex(exercise_field) + rowSpan);

        TextField repTextField = new TextField()
        {{
            setText(rep_num + "");
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-button");

            focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // When the TextField loses focus
                    rep_string.set(this.getText());
                    //ADD TO SQL DATABASE
                    if (exercise_field != null && set_string.get() != null && weight_string.get() != null) {
                        sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                    }
                }
            });
        }};
        grid.add(repTextField, 2, GridPane.getRowIndex(exercise_field) + rowSpan);

        TextField weightTextField = new TextField()
        {{
            setText(weight_num + "");
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH / 4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("exercise-button");

            focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // When the TextField loses focus
                    weight_string.set(this.getText());
                    //ADD TO SQL DATABASE
                    if (rep_string.get() != null && exercise_field != null && set_string.get() != null) {
                        sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                    }
                }
            });
        }};
        detectTab(weightTextField, grid, date, title);

        //SET TO GREEN IF PROGRESSIVE OVERLOAD
        if (sql.isProgressiveOverload(exercise_field.getText(), date.getText(), Integer.parseInt(setTextField.getText()), Integer.parseInt(repTextField.getText()), Double.parseDouble(weightTextField.getText()))){
            progressiveOverload.set(true);
            if (progressiveOverload.get()) {
                setTextField.getStyleClass().add("overload-style");
                weightTextField.getStyleClass().add("overload-style");
                repTextField.getStyleClass().add("overload-style");
            }
        }
        grid.add(weightTextField, 3, GridPane.getRowIndex(exercise_field) + rowSpan);
    }

    /**
     * Deletes the selected row and moves everything back into place
     * @param grid grid pane
     *
     */
    public void delete(GridPane grid, TextField dateTextField, TextField titleTextField) throws SQLException {

        //If the user selected nothing
        if (exercise_field == null){
            HelperClass.createDialog("SELECT AN EXERCISE TO DELETE");
            return;
        }


        //The row index
        int rowIndex = GridPane.getRowIndex(exercise_field);
        //Row span
        int rowSpan;
        if (GridPane.getRowSpan(exercise_field) != null && GridPane.getRowSpan(exercise_field) != 1){
            rowSpan = GridPane.getRowSpan(exercise_field);
            //If the rowspan is not detected, this will change nothing
            rowIndex = rowIndex + rowSpan - 1;
            GridPane.setRowSpan(exercise_field, rowSpan - 1);
            exercise_field.setMinHeight(EXERCISE_BUTTON_HEIGHT * (rowSpan - 1));
        }




        //List of the children
        ArrayList<Node> children = new ArrayList<>(grid.getChildren());

        //DELETE FROM SQL DATABASE
        if (dateTextField != null && titleTextField != null){
            sql.deleteSetFromExercise(exercise_field.getText(), dateTextField.getText(), sql.retrieveSet(dateTextField.getText(), exercise_field.getText()));
        }

        for (int i = 1; i < children.size(); i++){
            //If it's the row index, remove it from the grid pane
            if (GridPane.getRowIndex(children.get(i)) == rowIndex){
                grid.getChildren().remove(children.get(i));
            }
            //If the row index is greater, move it down 1
            else if (GridPane.getRowIndex(children.get(i)) > rowIndex){
                GridPane.setRowIndex((children.get(i)), GridPane.getRowIndex(children.get(i)) - 1);
            }
        }

        exercise_field = null;
    }

    /**
     * Opens the main workout creator scene
     * @param primaryStage the primary stage
     * @param date the date (if any)
     */
    public void makeWorkoutScene(Stage primaryStage, String date, int month, int year)  {
        BorderPane main = new BorderPane();
        //TOP LABELS

        BorderPane topBorderPane = new BorderPane();

        ImageView backButton = HelperClass.createArrowButton("/main/java/IMAGES/angle-double-small-left (workout).png"); //BACK BUTTON
        backButton.getStyleClass().add("back-button");

        backButton.setOnMouseClicked((event -> Platform.runLater(() -> calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH))));
        //Back button brings you back to the current month
        backButton.setOnMouseClicked((event) -> {
            //If the user didn't load a date, go back to today's month
            if (date == null){
                Platform.runLater(() -> calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH));
            } else {
                calendarScene(primaryStage, year, month);
            }
        });
        topBorderPane.setLeft(backButton);

        //Title and Date
        TextField titleTextField = new TextField(){{
            setPromptText("Title");
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            setAlignment(Pos.CENTER);
            getStyleClass().add("workout-top-field");
        }};

        TextField dateTextField = new TextField(){{
            if (date != null){
                setText(month + 1 +  "/" + date + "/" + year);
            } else {
                setPromptText("Date");
            }
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            setAlignment(Pos.CENTER);
            getStyleClass().add("workout-top-field");
        }};

        //If there's existing information in the exercise database, load it
        try {
            if (date != null) {
                ResultSet data = sql.loadFromWorkout(month + 1 + "/" + date + "/" + year);
                if (data.next()) {
                    titleTextField.setText(data.getString(1));
                    dateTextField.setText(month + 1 + "/" + date + "/" + year);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        topBorderPane.setCenter(new HBox(){{
            setAlignment(Pos.CENTER);
            setMinHeight((double) WINDOW_HEIGHT /7);
            getChildren().addAll(titleTextField, dateTextField);
        }});

        main.setTop(topBorderPane);
        BorderPane.setAlignment(topBorderPane.getCenter(), Pos.CENTER);
        topBorderPane.getStyleClass().add("workout-hbox");

        //EXERCISE GRID PANE
        GridPane exerciseGridPane = new GridPane();
        exerciseGridPane.getStyleClass().add("workout-pane");
        exerciseGridPane.setGridLinesVisible(true);

        //If there's existing information in the exercise database, load it
        addExercise(exerciseGridPane, dateTextField, titleTextField);
        exerciseGridPane.setAlignment(Pos.TOP_CENTER);
        try {
            if (date != null) {
                if (sql.loadFromExercise(dateTextField.getText()).next()) {
                    loadFromDatabase(exerciseGridPane, dateTextField, titleTextField);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //Adds exercise pane to scroll pane

        ScrollPane scrollPane = new ScrollPane(exerciseGridPane);
        scrollPane.getStyleClass().add("scroll-bar");
        scrollPane.setFitToWidth(true); // Optional: to fit the width of the GridPane to the ScrollPane's width
        scrollPane.setFitToHeight(true); // Optional: to fit the height of the GridPane to the ScrollPane's height
        main.setCenter(scrollPane);

        //Graphics for the buttons
        VBox addExerciseGraphic = new VBox();
        Label label1 = new Label("Add Exercise");
        Label label2 = new Label("(ENTER)");
        label2.getStyleClass().add("bottom-workout-instruction-label");
        addExerciseGraphic.setAlignment(Pos.CENTER);
        addExerciseGraphic.getChildren().addAll(label1, label2);

        VBox addSetGraphic = new VBox();
        Label label3 = new Label("Add Set");
        Label label4 = new Label("(TAB)");
        label4.getStyleClass().add("bottom-workout-instruction-label");
        addSetGraphic.getChildren().addAll(label3, label4);
        addSetGraphic.setAlignment(Pos.CENTER);

        VBox deleteGraphic = new VBox();
        Label label5 = new Label("Delete");
        Label label6 = new Label("(ALT)");
        label6.getStyleClass().add("bottom-workout-instruction-label");
        deleteGraphic.setAlignment(Pos.CENTER);
        deleteGraphic.getChildren().addAll(label5, label6);

        HBox bottom = new HBox();
        bottom.getChildren().addAll(new Button(){{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            getStyleClass().add("bottom-workout-button");
            setGraphic(addExerciseGraphic);
            setOnAction((event) -> addExercise(exerciseGridPane, dateTextField, titleTextField));
        }}, new Button(){{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            setGraphic(addSetGraphic);
            getStyleClass().add("bottom-workout-button");
            setOnAction((event) -> addSet(exerciseGridPane, dateTextField, titleTextField));
        }}, new Button(){{
            setMinHeight(EXERCISE_BUTTON_HEIGHT);
            setPrefWidth((double) WINDOW_WIDTH /4);
            setAlignment(Pos.CENTER);
            setFont(new Font(EXERCISE_FONT_SIZE));
            setGraphic(deleteGraphic);
            getStyleClass().add("bottom-workout-button");
            setOnAction((event) -> {
                try {
                    delete(exerciseGridPane, dateTextField, titleTextField);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }});


        bottom.setAlignment(Pos.BOTTOM_CENTER);
        bottom.getStyleClass().add("bottom-exercise-pane");
        main.setBottom(bottom);

        Scene workout = new Scene(main);

        workout.setOnKeyPressed((event -> {
            //If the user pressed enter, add exercise
            if (event.getCode() == KeyCode.ENTER){
                Platform.runLater(() -> addExercise(exerciseGridPane, dateTextField, titleTextField));
            }
            //If the user pressed alt, delete exercise
            else if (event.getCode() == KeyCode.ALT){
                Platform.runLater(() -> {
                    try {
                        delete(exerciseGridPane, dateTextField, titleTextField);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }));

        workout.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/main/java/CSS/style.css")).toExternalForm());
        primaryStage.setScene(workout);
    }

    /**
     * Loads in the information from the exercise database
     * @param date The date
     */
    public void loadFromDatabase(GridPane grid, TextField date, TextField title) {

        //FOR SQL
        AtomicReference<String> rep_string = new AtomicReference<>();
        AtomicReference<String> set_string = new AtomicReference<>();
        AtomicReference<String> weight_string = new AtomicReference<>();
        AtomicBoolean progressiveOverload = new AtomicBoolean(false);

        try (ResultSet exerciseData = sql.loadFromExercise(date.getText())) {

            while (exerciseData.next()) {
                int numRows = grid.getRowCount();

                //FORMATTING FOR MULTIPLE SETS
                if (exerciseData.getInt(2) > 1) {
                    addSetforLoad(grid, date, title, exerciseData.getInt(2), exerciseData.getInt(3), exerciseData.getDouble(4));
                } else {
                    TextField nameTextField = new TextField() {{
                        setText(exerciseData.getString(1));
                        setMinHeight(EXERCISE_BUTTON_HEIGHT);
                        setPrefWidth((double) WINDOW_WIDTH / 4);
                        setAlignment(Pos.CENTER);
                        setFont(new Font(EXERCISE_FONT_SIZE));
                        getStyleClass().add("exercise-name-button");

                        //Used in case a set needs to be added.
                        exercise_field = this;

                        //Adds to SQL
                        focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue) { // When the TextField loses focus
                                exercise_field = (this);
                                //ADD TO SQL DATABASE
                                if (rep_string.get() != null && exercise_field != null && set_string.get() != null && weight_string.get() == null) {
                                    sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                                }
                            }
                        });
                    }};

                    grid.add(nameTextField, 0, grid.getRowCount());

                    TextField setTextField = new TextField() {{
                        setText(exerciseData.getInt(2) + "");
                        setMinHeight(EXERCISE_BUTTON_HEIGHT);
                        setPrefWidth((double) WINDOW_WIDTH / 4);
                        setAlignment(Pos.CENTER);
                        setFont(new Font(EXERCISE_FONT_SIZE));
                        getStyleClass().add("exercise-button");
                        //Adds to SQL
                        focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue) { // When the TextField loses focus
                                set_string.set(this.getText());
                                //ADD TO SQL DATABASE
                                if (rep_string.get() != null && exercise_field != null && set_string.get() != null && weight_string.get() != null) {
                                    sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                                }
                            }
                        });
                    }};

                    grid.add(setTextField, 1, numRows);

                    TextField repsTextField = new TextField() {{
                        setText(exerciseData.getInt(3) + "");
                        setMinHeight(EXERCISE_BUTTON_HEIGHT);
                        setPrefWidth((double) WINDOW_WIDTH / 4);
                        setAlignment(Pos.CENTER);
                        setFont(new Font(EXERCISE_FONT_SIZE));
                        getStyleClass().add("exercise-button");

                        //Adds to SQL
                        focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue) { // When the TextField loses focus
                                rep_string.set(this.getText());
                                //ADD TO SQL DATABASE
                                if (rep_string.get() != null && exercise_field != null && set_string.get() != null && weight_string.get() != null) {
                                    sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                                }
                            }
                        });
                    }};

                    grid.add(repsTextField, 2, numRows);

                    TextField weightTextField = new TextField() {{
                        setText(exerciseData.getDouble(4) + "");
                        setMinHeight(EXERCISE_BUTTON_HEIGHT);
                        setPrefWidth((double) WINDOW_WIDTH / 4);
                        setAlignment(Pos.CENTER);
                        setFont(new Font(EXERCISE_FONT_SIZE));
                        getStyleClass().add("exercise-button");

                        //Adds to SQL
                        focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue) { // When the TextField loses focus
                                weight_string.set(this.getText());
                                //ADD TO SQL DATABASE
                                if (rep_string.get() != null && exercise_field != null && set_string.get() != null && weight_string.get() != null) {
                                    sql.addToExercise(title.getText(), exercise_field.getText(), Integer.parseInt(set_string.get()), date.getText(), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()));
                                }
                            }
                        });
                    }};
                    detectTab(weightTextField, grid, date, title);

                    //SET TO GREEN IF PROGRESSIVE OVERLOAD
                    if (sql.isProgressiveOverload(exercise_field.getText(), date.getText(), Integer.parseInt(setTextField.getText()), Integer.parseInt(repsTextField.getText()), Double.parseDouble(weightTextField.getText()))) {
                        progressiveOverload.set(true);
                        if (progressiveOverload.get()) {
                            setTextField.getStyleClass().add("overload-style");
                            weightTextField.getStyleClass().add("overload-style");
                            repsTextField.getStyleClass().add("overload-style");
                        }
                    }

                    grid.add(weightTextField, 3, numRows);
                }
                grid.setAlignment(Pos.TOP_CENTER);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
