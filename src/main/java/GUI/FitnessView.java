package GUI;
import SQL.SQLconnector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class FitnessView extends Application {
    //Instance of the SQL database

    private SQLconnector sql;

    //the window width
    private static final int WINDOW_WIDTH = 800;

    //the window height
    private static final int WINDOW_HEIGHT = 800;

    //the size of the top labels
    private static final int TOP_LABEL_SIZE = 25;

    //the font of the top labels
    private static final String TOP_LABEL_FONT = "Helvetica";

    //the current year
    private static int CURRENT_YEAR;

    //the current month
    private static int CURRENT_MONTH;

    //Height of "name" "reps" etc.
    private static final int EXERCISE_BUTTON_HEIGHT = WINDOW_HEIGHT / 15;

    private static final int EXERCISE_FONT_SIZE = TOP_LABEL_SIZE - 8;

    //The textfield that is altered
    private TextField exercise_field;

    public void init(){
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Thebringa-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-BoldItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Allan-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-Italic.ttf"), 14);

        sql = new SQLconnector();
    }

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
        //GETS THE DATE
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        CURRENT_YEAR = calendar.get(Calendar.YEAR);
        CURRENT_MONTH = calendar.get(Calendar.MONTH);
        calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH);
        primaryStage.setTitle("MY FITNESS CALENDAR");
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * Creates the main calendar scene
     * @param primaryStage the primary scene
     * @param year the year 
     * @param month the month
     */
    public void calendarScene(Stage primaryStage, int year, int month){
        //MAIN BORDERPANE
        BorderPane main = new BorderPane();

        //TOP FLOWPANE CONTAINING TOP LABELS
        FlowPane top = new FlowPane();

        Label monthLabel = new Label(month + 1 +  "/" + year){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("top-label-text");
        }};

        //DROP DOWN MENU AFTER CLICKING +
        Label headerMenuItem = new Label("Tools");
        CustomMenuItem headerItem = new CustomMenuItem(headerMenuItem, false);
        headerItem.getStyleClass().add("category-menu-header");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("context-menu");
        MenuItem createWorkout = new MenuItem("Create Workout");
        createWorkout.getStyleClass().add("menu-option");
        MenuItem manageGoals = new MenuItem("Manage Goals");
        manageGoals.getStyleClass().add("menu-option");
        MenuItem progress = new MenuItem("Progress");
        progress.getStyleClass().add("menu-option");
        contextMenu.getItems().addAll(headerItem, createWorkout, manageGoals, progress);

        createWorkout.setOnAction((event -> makeWorkoutScene(primaryStage, null, 0, 0)));
        manageGoals.setOnAction((event -> makeGoalsScene(primaryStage)));
        progress.setOnAction((event -> makeProgressScene(primaryStage)));
        Label plus = new Label("+"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("top-label-text");
            setOnMouseClicked((event) -> contextMenu.show(this, 800, 150));
        }};

        Label title = new Label("My Fitness Calendar"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("top-label-text");
        }};
        Label rightArrow = new Label(">"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("arrows");
            setOnMouseClicked((event) -> {
                //If it's the december
                if (month + 1 == 12){
                    calendarScene(primaryStage, year + 1, 0);
                } else {
                    calendarScene(primaryStage, year, month + 1);
                }
                
            });
        }};
        Label leftArrow = new Label("<"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("arrows");
            setOnMouseClicked((event) -> {
                //If it's january
                if(month == 0){
                    calendarScene(primaryStage, year - 1, 11);
                } else {
                    calendarScene(primaryStage, year, month - 1);
                }
            });
        }};
        top.getChildren().addAll(leftArrow, monthLabel, title, plus, rightArrow);
        top.setMinHeight((double) WINDOW_HEIGHT /5);
        top.setAlignment(Pos.CENTER);
        top.setHgap((double) WINDOW_WIDTH / 19);
        top.getStyleClass().add("topFlowPane");
        main.setTop(top);

        GridPane calendarGrid = new GridPane();
        //creates the calendar grid

        for (int i = 0; i < 7; i++){
            //ADD THE WEEK LABELS
            Label l = new Label();
            if (i == 0 || i == 6){
                l.setText("S");
            }
            else if (i == 1){
                l.setText("M");
            }
            else if (i == 2 || i == 4){
                l.setText("T");
            }
            else if (i == 3){
                l.setText("W");
            }
            else if (i == 5){
                l.setText("F");
            }
            l.setPrefSize((double) WINDOW_WIDTH /10, (double) WINDOW_HEIGHT /20);
            l.setFont(new Font(TOP_LABEL_FONT, 10));
            l.setAlignment(Pos.CENTER);
            l.getStyleClass().add("week-label");
            calendarGrid.add(l, i, 0);
        }

        LocalDate today = LocalDate.of(year, month + 1, 1);
        int dayOfWeek = today.getDayOfWeek().getValue();
        if (dayOfWeek == 7){
            //Fuck sunday last day of the week
            dayOfWeek = 0;
        }

        //Create a new button for the first day of the month
        Button first = new Button("1");
        if (sql.workoutExists(month + 1 + "/" + 1 + "/" + year)) {
            first.setText(sql.getWorkoutName(month + 1 + "/" + 1 + "/" + year));
            first.getStyleClass().add("special-button");
        } else {
            first.getStyleClass().add("button-custom");
        }
        first.setMinWidth((double) (WINDOW_WIDTH) / 10);
        first.setMinHeight((double) (WINDOW_HEIGHT) / 11);
        first.setOnAction((event) -> makeWorkoutScene(primaryStage, 1 + "", month, year));
        //This ensures the calendar is correctly formatted.
        calendarGrid.add(first, dayOfWeek, 1);

        int dayOfPreviousMonth;
        if (month == 0){
            dayOfPreviousMonth = LocalDate.of(year-1, 12, 1).lengthOfMonth();
        } else {
            dayOfPreviousMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        }

        for (int i = dayOfWeek; i > 0; i--){
            //Backtrack from the day of the week until we hit sunday (i = 0)
            Button button = new Button(dayOfPreviousMonth + "");
            //If the month is january, set the month to december and year - 1
            int actualMonth1;
            int actualYear1;
            if (month + 1 == 1){
                actualMonth1 = 12;
                actualYear1 = year - 1;
            } else {
                actualMonth1 = month;
                actualYear1 = year;
            }
            if (sql.workoutExists(actualMonth1 + "/" + dayOfPreviousMonth + "/" + actualYear1)){
                button.setText(sql.getWorkoutName(actualMonth1 + "/" + dayOfPreviousMonth + "/" + actualYear1));
                button.getStyleClass().add("special-gray-button");
            } else {
                button.setText(dayOfPreviousMonth + "");
                button.getStyleClass().add("extra_button-custom");
            }
            button.setMinWidth((double) (WINDOW_WIDTH) / 10);
            button.setMinHeight((double) (WINDOW_HEIGHT) / 11);
            calendarGrid.add(button, i - 1, 1);
            dayOfPreviousMonth--;
        }

        //Do the first iteration separately.
        int day = 2;
        for (int j = dayOfWeek + 1; j < 7; j++){
            Button b = new Button(day + "");
            if (sql.workoutExists(month + 1 + "/" + day + "/" + year)) {
                b.setText(sql.getWorkoutName(month + 1 + "/" + day + "/" + year));
                b.getStyleClass().add("special-button");
            } else {
                b.getStyleClass().add("button-custom");
            }
            b.setMinWidth((double) (WINDOW_WIDTH) / 10);
            b.setMinHeight((double) (WINDOW_HEIGHT) / 11);
            int finalDay = day;
            b.setOnAction((event) -> makeWorkoutScene(primaryStage, finalDay + "", month, year));
            calendarGrid.add(b, j, 1);
            day++;
        }
        boolean gray = false;
        for (int i = 2; i < 7; i++){
             for (int j = 0; j < 7; j++) {
                 if (day > YearMonth.of(year, month + 1).lengthOfMonth()) {
                     //Generate the gray days of the next month
                     day = 1;
                     gray = true;
                     //No more rows than necessary
                 }
                 //Don't create a new row for no reason
                 if (j == 0 && i > 5 && gray){
                     break;
                 }
                 Button b = new Button(day + "");

                 if (!gray) {
                     //If a workout exists, change the text of the button to the first letter of the workout
                     if (sql.workoutExists(month + 1 + "/" + day + "/" + year)){
                         b.setText(sql.getWorkoutName(month + 1 + "/" + day + "/" + year));
                         b.getStyleClass().add("special-button");
                     } else {
                         b.getStyleClass().add("button-custom");
                     }
                     int finalDay1 = day;
                     b.setOnAction((event) -> makeWorkoutScene(primaryStage, finalDay1 + "", month, year));
                 } else {
                     int actualMonth;
                     int actualYear;
                     //If the month is december, set it to january and the year + 1
                     if (month + 1== 12){
                         actualMonth = 1;
                         actualYear = year + 1;
                     } else {
                         actualMonth = month + 2;
                         actualYear = year;
                     }
                     if (sql.workoutExists(actualMonth + "/" + day + "/" + actualYear)){
                         b.setText(sql.getWorkoutName(month + 2 + "/" + day + "/" + year));
                         b.getStyleClass().add("special-gray-button");
                     } else {
                         b.setText(day + "");
                         b.getStyleClass().add("extra_button-custom");
                     }
                 }
                 b.setMinWidth((double) (WINDOW_WIDTH) / 10);
                 b.setMinHeight((double) (WINDOW_HEIGHT) / 11);
                 calendarGrid.add(b, j, i);
                 day++;
             }
        }

        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setPrefWidth((double) (WINDOW_WIDTH * 9) /10);
        main.setCenter(calendarGrid);
        main.getStyleClass().add("calendar-window");



        Scene mainScene = new Scene(main);
        mainScene.getStylesheets().add(getClass().getResource("/main/java/CSS/style.css").toExternalForm());
        primaryStage.setScene(mainScene);
    }

    /**
     * Populations the weight graph with points given an exercise name, start and end date
     * @param exercise exercise name
     * @param start start date
     * @param end end date
     */
    private List<XYChart.Data<String, Number>> populateWeightGraph(String exercise, String start, String end){
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        ResultSet results = sql.getWeightGraphData(exercise, start, end);

        try {
            while(results.next()){
                System.out.println(results.getString(1));
                System.out.println(results.getDouble(2));
                data.add(new XYChart.Data<>(results.getString(1), results.getDouble(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    private void makeProgressScene (Stage primaryStage) {
        BorderPane mainPane = new BorderPane();
        HBox bottomBox = new HBox();
        mainPane.setBottom(bottomBox);

        Button newGoalButton = new Button("Select Graph Type");
        newGoalButton.setMinHeight((double) WINDOW_HEIGHT /9);
        newGoalButton.getStyleClass().add("bottom-workout-button");
        bottomBox.getChildren().add(newGoalButton);
        bottomBox.getStyleClass().add("goal-bottom-section");
        bottomBox.setAlignment(Pos.CENTER);

        //GRAPH TYPE MENU
        ContextMenu graphTypeMenu = new ContextMenu();
        graphTypeMenu.getStyleClass().add("context-menu");

        Label headerMenuItem = new Label("Select a graph type");
        CustomMenuItem headerItem = new CustomMenuItem(headerMenuItem, false);
        headerItem.getStyleClass().add("category-menu-header");

        MenuItem item1 = new MenuItem("Progressive Overlead");
        MenuItem item2 = new MenuItem("Weight");
        item1.getStyleClass().add("category-menu-option");
        item2.getStyleClass().add("category-menu-option");

        //Weight graph menu
        ContextMenu weightGraphMenu = new ContextMenu();
        //Create a gridpane containing some labels and a text field and a submit button
        //Put the gridpane in the menu item
        //Put the menu item in the menu
        GridPane weightGraphGridPane = new GridPane();
        weightGraphGridPane.add(new Label("Choose Exercise:"), 0, 0);

        //To show options instead of a textfield
        ComboBox<String> comboExercise = new ComboBox<>();
        comboExercise.setEditable(false);
        sql.addComboBoxExercises(comboExercise);
        weightGraphGridPane.add(comboExercise, 1, 0);

        weightGraphGridPane.add(new Label("Starting:"), 0,1);
        TextField startingDate = new TextField();
        weightGraphGridPane.add(startingDate, 1, 1);

        TextField endingDate = new TextField();
        weightGraphGridPane.add(new Label("Ending:"), 0,2);
        weightGraphGridPane.add(endingDate, 1, 2);

        Button submit = new Button("Submit");
        weightGraphGridPane.add(submit, 0,3);
        //SUBMIT
        submit.setOnAction((event -> {
            //If one of the fields is empty, print an error
            if (comboExercise.getValue() == null || startingDate.getText() == null || endingDate.getText() == null) {
                System.out.println("GOOFY");
            } else {
                weightGraphMenu.hide(); //Close the menu

                //CREATE A LINECHART
                List<LineChart.Data<String, Number>> data;
                data = populateWeightGraph(comboExercise.getValue(), startingDate.getText(), endingDate.getText());
                var xAxis = new CategoryAxis();
                xAxis.setLabel("DATE");
                xAxis.setTickLabelRotation(70);
                //The min and max weights
                int[] minMaxPoints = sql.getMinMaxWeight(comboExercise.getValue(), startingDate.getText(), endingDate.getText());
                var yAxis = new NumberAxis("WEIGHT", minMaxPoints[0] - 10, minMaxPoints[1] + 10, 10);

                XYChart.Series<String, Number> series = new XYChart.Series<>(FXCollections.observableList(data));
                LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis, FXCollections.singletonObservableList(series));

                lineChart.setTitle(comboExercise.getValue() + " Weight vs Time");
                lineChart.setAnimated(true);
                mainPane.setCenter(lineChart);

                //TOOL TIPS
                for (XYChart.Data<?,?> dataPoint : series.getData()) {
                    Tooltip tooltip = new Tooltip(
                            "Date: " + dataPoint.getXValue() + "\nWeight: " + dataPoint.getYValue()
                    );
                    tooltip.setShowDelay(Duration.ZERO); //Set the duration to 0
                    Tooltip.install(dataPoint.getNode(), tooltip);
                    // Make the node visible when hovering
                    dataPoint.getNode().setOnMouseEntered(event2 -> dataPoint.getNode().setStyle("-fx-background-color: blue, white; -fx-background-insets: 0, 2;"));
                    dataPoint.getNode().setOnMouseExited(event2 -> dataPoint.getNode().setStyle(""));
                }
            }

        }));


        CustomMenuItem weightMenuItem = new CustomMenuItem(weightGraphGridPane, false);
        weightGraphMenu.getItems().add(weightMenuItem);
        item2.setOnAction((event -> weightGraphMenu.show(newGoalButton, 800, 150)));




        graphTypeMenu.getItems().addAll(headerItem, item1, item2);
        newGoalButton.setOnAction((event) -> graphTypeMenu.show(newGoalButton, 800, 150));
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }

    /**
     * Creates the goal management scene.
     * Allows you to make different types of goals and track it.
     * @param primaryStage the primary stage
     */
    private void makeGoalsScene(Stage primaryStage) {

        BorderPane mainBorderPane = new BorderPane();

        BorderPane subBorderPane = new BorderPane();
        mainBorderPane.setTop(subBorderPane);

        Button backButton = new Button("<"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            setAlignment(Pos.TOP_LEFT);
            getStyleClass().add("back-button");
            //Back button brings you back to the current month
            setOnMouseClicked((event) -> {
                Platform.runLater(() ->calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH));

            });
        }};

        subBorderPane.setLeft(backButton);

        Label currentGoalsLabel = new Label("Current Goals:"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            setAlignment(Pos.CENTER);
            getStyleClass().add("goal-top-section-label");
        }};

        subBorderPane.setCenter(currentGoalsLabel);
        BorderPane.setAlignment(subBorderPane.getCenter(), Pos.CENTER);
        subBorderPane.getStyleClass().add("goal-top-section");

        subBorderPane.setRight(new Button(""){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            setVisible(false);
        }});

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
        newGoalButton.setOnAction((event -> categoryMenu.show(newGoalButton, 650, 500)));

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

        submitItem.getStyleClass().add("consistency-label");

        contextMenu.getItems().addAll(formItem, submitItem);

        item1.setOnAction((event -> contextMenu.show(newGoalButton, 500,500)));

        //UPDATES CURRENT GOALS
        updateGoals(mainBorderPane);


        Scene mainScene = new Scene(mainBorderPane);
        mainScene.getStylesheets().add(getClass().getResource("/main/java/CSS/style.css").toExternalForm());
        primaryStage.setScene(mainScene);
    }

    public void updateGoals (BorderPane main){
        VBox currentGoals = new VBox();
        if (sql.hasConsistencyGoal()){
            //Updates the progress of the goal
            int progress = sql.updateConsistencyGoal(CURRENT_YEAR);
            ResultSet cGoal = sql.getConsistencyGoal();
            try {
                if (cGoal.next()){
                    VBox consistencyCard = new VBox();

                    Label consistencyHeader = new Label("CONSISTENCY GOAL");
                    VBox.setMargin(consistencyHeader,new Insets(3, 0, 20, 0));
                    consistencyHeader.getStyleClass().add("card-header");

                    Label currentConsistencyGoal = new Label("I want to workout " + cGoal.getInt(1) + " times a week.");
                    currentConsistencyGoal.getStyleClass().add("card-goal");
                    currentConsistencyGoal.setWrapText(true);

                    Label progressLabel = new Label ("Progress: " + progress + "/52");
                    progressLabel.getStyleClass().add("card-progress");

                    consistencyCard.getStyleClass().add("card");
                    consistencyCard.getChildren().addAll(consistencyHeader, currentConsistencyGoal, progressLabel);
                    consistencyCard.setMaxWidth((double) WINDOW_WIDTH /3);
                    consistencyCard.setPadding(new Insets(10));

                    VBox.setMargin(consistencyCard, new Insets(10));
                    consistencyCard.setAlignment(Pos.CENTER);
                    currentGoals.getChildren().add(consistencyCard);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        main.setCenter(currentGoals);
        main.getCenter().getStyleClass().add("current-goals-container");
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

            TextField weightTextField = new TextField()
            {{
                setPromptText("Weight");
                setMinHeight(EXERCISE_BUTTON_HEIGHT);
                setPrefWidth((double) WINDOW_WIDTH /4);
                setAlignment(Pos.CENTER);
                setFont(new Font(EXERCISE_FONT_SIZE));
                getStyleClass().add("exercise-button");

                //Adds to SQL
                focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) { // When the TextField loses focus
                        weight_string.set(this.getText());
                        //ADD TO SQL DATABASE
                        if (rep_string.get() != null && exercise_field!=null && set_string.get() != null && date.getText() != null && title.getText() != null) {
                            //SET TO GREEN IF PROGRESSIVE OVERLOAD
                            if (sql.isProgressiveOverload(exercise_field.getText(), date.getText(), Integer.parseInt(set_string.get()), Integer.parseInt(rep_string.get()), Double.parseDouble(weight_string.get()))){
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
            createDialog("SELECT AN EXERCISE TO ADD SET");
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

        TextField weightTextField = new TextField()
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

        detectTab(weightTextField, grid, date, title);
        grid.add(weightTextField, 3, GridPane.getRowIndex(exercise_field) + rowSpan);
    }

    /**
     * Creates an error dialog given a message input
     * @param message error message
     */
    public void createDialog(String message){
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/main/java/CSS/style.css").toExternalForm());
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
    public void delete(GridPane grid, TextField dateTextField, TextField titleTextField) throws SQLException{
        
        //If the user selected nothing
        if (exercise_field == null){
            createDialog("SELECT AN EXERCISE TO DELETE");
            return;
        }


         //The row index
         int rowIndex = GridPane.getRowIndex(exercise_field);
         //Row span
        int rowSpan = 1;
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
    public void makeWorkoutScene(Stage primaryStage, String date, int month, int year){
        BorderPane main = new BorderPane();
        //TOP LABELS
        
        BorderPane topBorderPane = new BorderPane();

        //Back label
        topBorderPane.setTop(new Label("<"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            setAlignment(Pos.TOP_LEFT);
            getStyleClass().add("back-button");
            //Back button brings you back to the current month
            setOnMouseClicked((event) -> {
                //If the user didn't load a date, go back to today's month
                if (date == null){
                    Platform.runLater(() -> calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH));
                } else {
                    calendarScene(primaryStage, year, month);
                }
            });
        }});
        topBorderPane.getStyleClass().add("top-pane");

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

        topBorderPane.setBottom(new HBox(){{
            setAlignment(Pos.CENTER);
            setMinHeight((double) WINDOW_HEIGHT /7);
            getChildren().addAll(titleTextField, dateTextField);
            getStyleClass().add("workout-hbox");
        }});
        main.setTop(topBorderPane);

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

        workout.getStylesheets().add(getClass().getResource("/main/java/CSS/style.css").toExternalForm());
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

    public static void main(String[] args) {
        Application.launch();
    }
}