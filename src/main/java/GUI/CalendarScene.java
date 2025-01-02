package GUI;
import SQL.SQLconnector;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class CalendarScene extends Application {
    //Instance of the SQL database

    public static SQLconnector sql;

    //the window width
    public static final int WINDOW_WIDTH = 800;

    //the window height
    public static final int WINDOW_HEIGHT = 800;

    //the size of the top labels
    public static final int TOP_LABEL_SIZE = 25;

    //the font of the top labels
    public static final String TOP_LABEL_FONT = "Helvetica";

    //the current year
    public static int CURRENT_YEAR;

    //the current month
    public static int CURRENT_MONTH;

    //Height of "name" "reps" etc.
    public static final int EXERCISE_BUTTON_HEIGHT = WINDOW_HEIGHT / 15;

    public static final int EXERCISE_FONT_SIZE = TOP_LABEL_SIZE - 8;

    //The textfield that is altered
    public static TextField exercise_field;

    public static WorkoutScene workoutScene; //The workout scene

    public static ProgressScene progressScene; //The progress Scene

    public static GoalScene goalScene; // The goals scene

    public void init(){
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Thebringa-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-BoldItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Allan-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/main/java/FONTS/Poppins-Italic.ttf"), 14);
        workoutScene = new WorkoutScene();
        progressScene = new ProgressScene();
        goalScene = new GoalScene();

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
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.setMaximized(true);
//        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * Creates the main calendar scene
     * @param primaryStage the primary scene
     * @param year the year 
     * @param month the month
     */
    public static void calendarScene(Stage primaryStage, int year, int month){
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
        MenuItem quickAdd = new MenuItem("QuickAdd");
        quickAdd.getStyleClass().add("menu-option");
        contextMenu.getItems().addAll(headerItem, createWorkout, manageGoals, progress, quickAdd);

        createWorkout.setOnAction((event -> workoutScene.makeWorkoutScene(primaryStage, null, 0, 0)));
        manageGoals.setOnAction((event -> goalScene.makeGoalsScene(primaryStage)));
        progress.setOnAction((event -> progressScene.makeProgressScene(primaryStage)));
        quickAdd.setOnAction((event -> {
            sql.quickAdd();
        }));


        Image plusImage = new Image(Objects.requireNonNull(CalendarScene.class.getResourceAsStream("/main/java/IMAGES/plus-small.png"))); //PLUS BUTTON
        ImageView plus = new ImageView(plusImage);
        plus.setFitWidth(60);
        plus.setFitHeight(60);
        plus.setPreserveRatio(true);
        plus.setOnMouseClicked((event) -> contextMenu.show(plus, 800, 150));
        BorderPane.setAlignment(plus, Pos.CENTER);

        Label title = new Label("My Fitness Calendar"){{
            setFont(new Font(TOP_LABEL_FONT, TOP_LABEL_SIZE));
            getStyleClass().add("top-label-text");
        }};

        ImageView rightButton = HelperClass.createArrowButton("/main/java/IMAGES/angle-double-small-right (calendar).png"); //RIGHT BUTTON

        rightButton.setOnMouseClicked((event) -> {
//                //If it's december
            if (month + 1 == 12){
                calendarScene(primaryStage, year + 1, 0);
            } else {
                calendarScene(primaryStage, year, month + 1);
            }

        });

        ImageView leftButton = HelperClass.createArrowButton("/main/java/IMAGES/angle-double-small-left (calendar).png"); //LEFT BUTTON

        leftButton.setOnMouseClicked((event) -> {
            //If it's january
            if(month == 0){
                calendarScene(primaryStage, year - 1, 11);
            } else {
                calendarScene(primaryStage, year, month - 1);
            }
        });

        top.getChildren().addAll(leftButton, monthLabel, title, plus, rightButton);
        top.setMinHeight((double) WINDOW_HEIGHT /5);
        top.setAlignment(Pos.CENTER);
        top.setHgap((double) WINDOW_WIDTH / 19);
        top.getStyleClass().add("topFlowPane");
        main.setTop(top);

        String[] reminder = new String[1]; //This goes into the function and returns the reminder we need to set.
        GridPane calendarGrid = getCalendarGrid(year, month, primaryStage, reminder);
        main.setCenter(calendarGrid);
        main.getStyleClass().add("calendar-window");

        if (reminder[0] != null){ //If there's a reminder message, add it
            Label l = new Label(reminder[0]);
            l.getStyleClass().add("reminder-label");
            BorderPane.setMargin(l, new Insets(0, 0, 15, 0));
            main.setBottom(l);
            BorderPane.setAlignment(l, Pos.CENTER);
        }

        Scene mainScene = new Scene(main);
        mainScene.getStylesheets().add(Objects.requireNonNull(CalendarScene.class.getResource("/main/java/CSS/style.css")).toExternalForm());
        primaryStage.setScene(mainScene);
    }

    /**
     * Creates and returns the calendar Grid
     * @param year year
     * @param month month
     * @param primaryStage primaryStage
     * @param reminderMessage contains the reminder message
     * @return grid pane
     */
    public static GridPane getCalendarGrid(int year, int month, Stage primaryStage, String[] reminderMessage){
        GridPane calendarGrid = new GridPane();
        //creates the calendar grid
        for (int i = 0; i < 7; i++){
            //ADD THE WEEK LABELS
            Label l = getLabel(i);
            l.getStyleClass().add("week-label");
            calendarGrid.add(l, i, 0);
        }

        LocalDate firstDay = LocalDate.of(year, month + 1, 1); // its the first of the month
        int dayOfWeek = firstDay.getDayOfWeek().getValue();
        if (dayOfWeek == 7){
            //Fuck sunday last day of the week
            dayOfWeek = 0;
        }

        LocalDate today = LocalDate.now(); //Today
        boolean checkingforSchedule = false;
        String[] nextWorkout = null;
        //Create a new button for the first day of the month
        Button first = new Button("1");
        if (isToday(year, month + 1, 1, today)){
            checkingforSchedule = true;
            nextWorkout = sql.getNextWorkout(LocalDate.of(year, month + 1, 1).getDayOfWeek().getValue());
            //If today is the next workout, print the next workout
            if (today.getDayOfWeek().getValue() == Integer.parseInt(nextWorkout[0]) + 1){
                reminderMessage[0] = ("Reminder: " + nextWorkout[1] + " on " + today);
                checkingforSchedule = false;
            }
        }
        if (sql.workoutExists(month + 1 + "/" + 1 + "/" + year)) {
            first.setText(sql.getWorkoutName(month + 1 + "/" + 1 + "/" + year));
            first.getStyleClass().add("special-button");
        } else {
            first.getStyleClass().add("button-custom");
        }
        first.setMinWidth((double) (WINDOW_WIDTH) / 10);
        first.setMinHeight((double) (WINDOW_HEIGHT) / 11);
        first.setOnAction((event) -> workoutScene.makeWorkoutScene(primaryStage, 1 + "", month, year));
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
                button.getStyleClass().add("special-gray-button");
                VBox boxx = createCalendarButtonGraphic(actualYear1, actualMonth1 - 1, dayOfPreviousMonth, button);
                for (Node s: boxx.getChildren()){
                    s.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.616);");
                }

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
            if (isToday(year, month + 1, day, today)){ //IF IT'S TODAY, WE START SEARCHING FOR THE NEXT WORKOUT
                checkingforSchedule = true;
                nextWorkout = sql.getNextWorkout(LocalDate.of(year, month + 1, day).getDayOfWeek().getValue());
            }
            //IF WE ARE LOOKING FOR THE NEXT WORKOUT
            if (checkingforSchedule){
                if (LocalDate.of(year, month + 1, day).getDayOfWeek().getValue() == Integer.parseInt(nextWorkout[0])){ //If the current day is the next workout, print a reminder.
                    reminderMessage[0] = ("Reminder: " + nextWorkout[1] + " on " + today);
                    checkingforSchedule = false;
                }
            }
            Button b = new Button(day + "");
            if (sql.workoutExists(month + 1 + "/" + day + "/" + year)) {
                b.setText(sql.getWorkoutName(month + 1 + "/" + day + "/" + year));
                createCalendarButtonGraphic(year, month, day, b);
                b.getStyleClass().add("special-button");
            } else {
                b.getStyleClass().add("button-custom");
            }
            b.setMinWidth((double) (WINDOW_WIDTH) / 10);
            b.setMinHeight((double) (WINDOW_HEIGHT) / 11);
            int finalDay = day;
            b.setOnAction((event) -> workoutScene.makeWorkoutScene(primaryStage, finalDay + "", month, year));
            calendarGrid.add(b, j, 1);
            day++;
        }
        boolean gray = false;
        for (int i = 2; i < 7; i++){
            for (int j = 0; j < 7; j++) {
                if (isToday(year, month + 1, day, today)){ // if it's today, start searching for the next workout
                    checkingforSchedule = true;
                    nextWorkout = sql.getNextWorkout(LocalDate.of(year, month + 1, day).getDayOfWeek().getValue() - 1);
                }


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

                //IF WE ARE LOOKING FOR THE NEXT WORKOUT
                if (checkingforSchedule){
                    if (LocalDate.of(year, month + 1, day).getDayOfWeek().getValue() == Integer.parseInt(nextWorkout[0])){ //If the current day is the next workout, print a reminder.
                        reminderMessage[0] = ("Reminder: " + nextWorkout[1] + " on " + (LocalDate.of(year, month + 1, day)));
                        checkingforSchedule = false;
                        b.getStyleClass().add("next-workout-button");
                    }
                }

                if (!gray) {
                    //If a workout exists, change the text of the button to the first letter of the workout
                    if (sql.workoutExists(month + 1 + "/" + day + "/" + year)){
                        createCalendarButtonGraphic(year, month, day, b);
                        b.getStyleClass().add("special-button");
                    } else {
                        b.getStyleClass().add("button-custom");
                    }
                    int finalDay1 = day;
                    b.setOnAction((event) -> workoutScene.makeWorkoutScene(primaryStage, finalDay1 + "", month, year));
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
                        b.getStyleClass().add("special-gray-button");
                        VBox box = createCalendarButtonGraphic(year, month, day, b);
                        for (Node s: box.getChildren()){
                            s.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.616);");
                        }
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
        return calendarGrid;
    }

    /**
     * Creates a calendar button graphic with the name of the workout and the date.
     * @param year year
     * @param month month
     * @param day day
     * @param b button
     */
    public static VBox createCalendarButtonGraphic(int year, int month, int day, Button b) {
        VBox buttonGraphic = new VBox();
        buttonGraphic.setMinWidth((double) (WINDOW_WIDTH) / 10);
        buttonGraphic.setMinHeight((double) (WINDOW_HEIGHT) / 11);
        buttonGraphic.setAlignment(Pos.CENTER);
        b.setText("");
        Label label = new Label(sql.getWorkoutName(month + 1 + "/" + day + "/" + year));
        Label dayLabel = new Label(day + "");
        buttonGraphic.getChildren().addAll(label, dayLabel);
        b.setGraphic(buttonGraphic);
        return buttonGraphic;
    }

    /**
     * is the given day today?
     * @param year year
     * @param month month
     * @param day day
     * @param today today
     * @return boolean
     */
    public static boolean isToday(int year, int month, int day, LocalDate today){
        return today.getDayOfMonth() == day && today.getMonthValue() == month && today.getYear() == year;
    }

    /**
     * Calendar Scene helper function
     * @param i i
     * @return label
     */
    private static Label getLabel(int i) {
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
        else {
            l.setText("F");
        }
        l.setPrefSize((double) WINDOW_WIDTH /10, (double) WINDOW_HEIGHT /20);
        l.setFont(new Font(TOP_LABEL_FONT, 10));
        l.setAlignment(Pos.CENTER);
        return l;
    }

    public static void main(String[] args) {
        Application.launch();
    }
}