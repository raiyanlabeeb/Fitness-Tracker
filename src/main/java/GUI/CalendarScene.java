package GUI;
import SQL.SQLconnector;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
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
        contextMenu.getItems().addAll(headerItem, createWorkout, manageGoals, progress);

        createWorkout.setOnAction((event -> workoutScene.makeWorkoutScene(primaryStage, null, 0, 0)));
        manageGoals.setOnAction((event -> goalScene.makeGoalsScene(primaryStage)));
        progress.setOnAction((event -> progressScene.makeProgressScene(primaryStage)));


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
//                //If it's the december
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

        GridPane calendarGrid = new GridPane();
        //creates the calendar grid

        for (int i = 0; i < 7; i++){
            //ADD THE WEEK LABELS
            Label l = getLabel(i);
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
            b.setOnAction((event) -> workoutScene.makeWorkoutScene(primaryStage, finalDay + "", month, year));
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
                         b.setText(sql.getWorkoutName(actualMonth + "/" + day + "/" + actualYear));
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
        mainScene.getStylesheets().add(Objects.requireNonNull(CalendarScene.class.getResource("/main/java/CSS/style.css")).toExternalForm());
        primaryStage.setScene(mainScene);
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