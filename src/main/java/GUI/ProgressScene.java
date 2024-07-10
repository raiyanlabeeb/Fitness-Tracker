package GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static GUI.CalendarScene.*;

public class ProgressScene extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    /**
     * Populates the weight graph with points given an exercise name, start and end date
     * @param exercise exercise name
     * @param start start date
     * @param end end date
     */
    private List<XYChart.Data<String, Number>> populateWeightGraph(String exercise, String start, String end){
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        ResultSet results = sql.getWeightGraphData(exercise, start, end);
        try {
            while(results.next()){
                data.add(new XYChart.Data<>(results.getString(1), results.getDouble(2)));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return data;
    }

    /**
     * Creates the overload graph
     * @param mainPane main borderPane
     * @param overloadButton overload button
     * @param newGoalButton new goal button
     */
    private void createOverloadGraph(BorderPane mainPane, MenuItem overloadButton, Button newGoalButton){
        //Weight graph menu
        ContextMenu overloadGraphMenu = new ContextMenu();
        //Create a grid pane containing some labels and a text field and a submit button
        //Put the grid pane in the menu item
        //Put the menu item in the menu
        GridPane overloadGraphGridPane = new GridPane();
        overloadGraphMenu.getStyleClass().add("graph-menu");
        overloadGraphGridPane.getStyleClass().add("graph-grid");
        overloadGraphGridPane.add(new Label("Choose Exercise:"), 0, 0);

        //To show options instead of a textfield
        ComboBox<String> comboExercise = new ComboBox<>();
        GridPane.setMargin(comboExercise, new Insets(5, 10, 5, 10));
        comboExercise.getStyleClass().add("exercise-selection");
        comboExercise.setEditable(false);
        comboExercise.getItems().add("All Exercises");
        sql.addComboBoxExercises(comboExercise);
        overloadGraphGridPane.add(comboExercise, 1, 0);

        overloadGraphGridPane.add(new Label("Starting:"), 0,1);
        TextField startingDate = new TextField();
        startingDate.getStyleClass().add("starting-date-selection");
        GridPane.setMargin(startingDate, new Insets(5, 10, 5, 10));
        overloadGraphGridPane.add(startingDate, 1, 1);

        TextField endingDate = new TextField();
        endingDate.getStyleClass().add("ending-date-selection");
        overloadGraphGridPane.add(new Label("Ending:"), 0,2);
        GridPane.setMargin(endingDate, new Insets(5, 10, 5, 10));
        overloadGraphGridPane.add(endingDate, 1, 2);

        Button submit = new Button("Submit");
        submit.getStyleClass().add("submit-button");
        GridPane.setMargin(submit, new Insets(5,0,0,0));
        overloadGraphGridPane.add(submit, 0,3);

        mainPane.setCenter(new HBox(){{
            getStyleClass().add("progress-center");
        }});
        //SUBMIT
        submit.setOnAction((event -> {
            //If one of the fields is empty, print an error
            if (comboExercise.getValue() == null || startingDate.getText() == null || endingDate.getText() == null) {
                System.out.println("GOOFY");
            } else {
                overloadGraphMenu.hide(); //Close the menu
                List<XYChart.Data<String, Number>> data;
                data = populateOverloadGraph(comboExercise.getValue(), startingDate.getText(), endingDate.getText());
                var xAxis = new CategoryAxis();
                xAxis.getStyleClass().add("x-axis");
                xAxis.setLabel("DATE");
                xAxis.setTickLabelRotation(70);
                //The min and max weights
                int[] minMaxPoints = sql.getMinMaxOverload(comboExercise.getValue(), startingDate.getText(), endingDate.getText());
                var yAxis = new NumberAxis("OVERLOAD PERCENTAGE", minMaxPoints[0] - 1, minMaxPoints[1] + 1, 10);
                yAxis.getStyleClass().add("y-axis");
                yAxis.setTickMarkVisible(true);
                yAxis.setMinorTickVisible(true);
                yAxis.setMinorTickCount(4);

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.getData().addAll(data);
                series.setName("Data");
                LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

                XYChart.Series<String, Number> bestFitLine = calculateBestFitLine(series);
                lineChart.getData().addAll(series, bestFitLine);

                bestFitLine.getNode().getStyleClass().add("best-fit-line");
                series.getNode().getStyleClass().add("data");

                lineChart.setTitle(comboExercise.getValue() + " Progressive Overload vs Time");
                lineChart.setAnimated(true);
                lineChart.getStyleClass().add("progress-line-chart");
                lineChart.setLegendVisible(false);
                mainPane.setCenter(lineChart);
                BorderPane.setAlignment(lineChart, Pos.CENTER);
                lineChart.setPadding(new Insets(10));

                //TOOL TIPS
                for (XYChart.Data<?,?> dataPoint : series.getData()) {
                    Tooltip tooltip = new Tooltip(
                            "Date: " + dataPoint.getXValue() + "\nOverload Percentage: " + dataPoint.getYValue()
                    );
                    tooltip.getStyleClass().add("tool-tip");
                    tooltip.setShowDelay(Duration.ZERO); //Set the duration to 0
                    Tooltip.install(dataPoint.getNode(), tooltip);
                    // Make the node visible when hovering
                    dataPoint.getNode().setOnMouseEntered(event2 -> dataPoint.getNode().setStyle("-fx-background-color: blue, white; -fx-background-insets: 0, 2;"));
                    dataPoint.getNode().setOnMouseExited(event2 -> dataPoint.getNode().setStyle(""));
                }
            }}));

        //Choose your weight graph
        CustomMenuItem weightMenuItem = new CustomMenuItem(overloadGraphGridPane, false);
        overloadGraphMenu.getItems().add(weightMenuItem);
        overloadButton.setOnAction((event -> overloadGraphMenu.show(newGoalButton, 800, 600)));
    }

    /**
     * Populates the progressive overload graph with data
     * @param exercise exercise name
     * @param start start date
     * @param end end date
     * @return list of datapoints
     */
    private List<XYChart.Data<String, Number>> populateOverloadGraph(String exercise, String start, String end) {
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        ResultSet results = sql.getOverloadGraphData(exercise, start, end);

        try {
            while(results.next()){
                data.add(new XYChart.Data<>(results.getString(1), results.getDouble(2)));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return data;
    }

    /**
     * Creates the weightGraph and nodes
     * @param mainPane main border pane
     * @param item2 the "weight" button
     * @param newGoalButton the "select graph types" button
     */
    private void createWeightGraph(BorderPane mainPane, MenuItem item2, Button newGoalButton){
        //Weight graph menu
        ContextMenu weightGraphMenu = new ContextMenu();
        //Create a grid pane containing some labels and a text field and a submit button
        //Put the grid pane in the menu item
        //Put the menu item in the menu
        GridPane weightGraphGridPane = new GridPane();
        weightGraphMenu.getStyleClass().add("graph-menu");
        weightGraphGridPane.getStyleClass().add("graph-grid");
        weightGraphGridPane.add(new Label("Choose Exercise:"), 0, 0);

        //To show options instead of a textfield
        ComboBox<String> comboExercise = new ComboBox<>();
        GridPane.setMargin(comboExercise, new Insets(5, 10, 5, 10));
        comboExercise.getStyleClass().add("exercise-selection");
        comboExercise.setEditable(false);
        sql.addComboBoxExercises(comboExercise);
        weightGraphGridPane.add(comboExercise, 1, 0);

        weightGraphGridPane.add(new Label("Starting:"), 0,1);
        TextField startingDate = new TextField();
        startingDate.getStyleClass().add("starting-date-selection");
        GridPane.setMargin(startingDate, new Insets(5, 10, 5, 10));
        weightGraphGridPane.add(startingDate, 1, 1);

        TextField endingDate = new TextField();
        endingDate.getStyleClass().add("ending-date-selection");
        weightGraphGridPane.add(new Label("Ending:"), 0,2);
        GridPane.setMargin(endingDate, new Insets(5, 10, 5, 10));
        weightGraphGridPane.add(endingDate, 1, 2);

        Button submit = new Button("Submit");
        submit.getStyleClass().add("submit-button");
        GridPane.setMargin(submit, new Insets(5,0,0,0));
        weightGraphGridPane.add(submit, 0,3);

        mainPane.setCenter(new HBox(){{
            getStyleClass().add("progress-center");
        }});
        //SUBMIT
        submit.setOnAction((event -> {
            //If one of the fields is empty, print an error
            if (comboExercise.getValue() == null || startingDate.getText() == null || endingDate.getText() == null) {
                System.out.println("GOOFY");
            } else {
                weightGraphMenu.hide(); //Close the menu
                List<XYChart.Data<String, Number>> data;
                data = populateWeightGraph(comboExercise.getValue(), startingDate.getText(), endingDate.getText());
                var xAxis = new CategoryAxis();
                xAxis.getStyleClass().add("x-axis");
                xAxis.setLabel("DATE");
                xAxis.setTickLabelRotation(70);
                //The min and max weights
                int[] minMaxPoints = sql.getMinMaxWeight(comboExercise.getValue(), startingDate.getText(), endingDate.getText());
                var yAxis = new NumberAxis("WEIGHT", minMaxPoints[0] - 10, minMaxPoints[1] + 10, 10);
                yAxis.getStyleClass().add("y-axis");
                yAxis.setTickMarkVisible(true);
                yAxis.setMinorTickVisible(true);
                yAxis.setMinorTickCount(4);

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
                series.getData().addAll(data);
                series.setName("Data");

                XYChart.Series<String, Number> bestFitLine = calculateBestFitLine(series);
                lineChart.getData().addAll(series, bestFitLine);

                bestFitLine.getNode().getStyleClass().add("best-fit-line");
                series.getNode().getStyleClass().add("data");

                lineChart.setTitle(comboExercise.getValue() + " Weight vs Time");
                lineChart.setAnimated(true);
                lineChart.getStyleClass().add("progress-line-chart");
                lineChart.setLegendVisible(false);
                mainPane.setCenter(lineChart);
                BorderPane.setAlignment(lineChart, Pos.CENTER);
                lineChart.setPadding(new Insets(10));

                //TOOL TIPS
                for (XYChart.Data<?,?> dataPoint : series.getData()) {
                    Tooltip tooltip = new Tooltip(
                            "Date: " + dataPoint.getXValue() + "\nWeight: " + dataPoint.getYValue() + "\nReps: " + sql.getMaxReps((String) dataPoint.getXValue(), comboExercise.getValue())
                    );
                    tooltip.getStyleClass().add("tool-tip");
                    tooltip.setShowDelay(Duration.ZERO); //Set the duration to 0
                    Tooltip.install(dataPoint.getNode(), tooltip);
                    // Make the node visible when hovering
                    dataPoint.getNode().setOnMouseEntered(event2 -> dataPoint.getNode().setStyle("-fx-background-color: blue, white; -fx-background-insets: 0, 2;"));
                    dataPoint.getNode().setOnMouseExited(event2 -> dataPoint.getNode().setStyle(""));
                }

                //TOOL TIPS FOR LINE OF BEST FIT
                for (XYChart.Data<?,?> dataPoint : bestFitLine.getData()) {
                    Tooltip tooltip = new Tooltip(
                            "Date: " + dataPoint.getXValue() + "\nWeight: " + dataPoint.getYValue() + "\nReps: " + sql.getMaxReps((String) dataPoint.getXValue(), comboExercise.getValue())
                    );
                    tooltip.getStyleClass().add("tool-tip");
                    tooltip.setShowDelay(Duration.ZERO); //Set the duration to 0
                    Tooltip.install(dataPoint.getNode(), tooltip);
                    // Make the node visible when hovering
                    dataPoint.getNode().setOnMouseEntered(event2 -> dataPoint.getNode().setStyle("-fx-background-color: blue, white; -fx-background-insets: 0, 2;"));
                    dataPoint.getNode().setOnMouseExited(event2 -> dataPoint.getNode().setStyle(""));
                }

            }}));

        //Choose your weight graph
        CustomMenuItem weightMenuItem = new CustomMenuItem(weightGraphGridPane, false);
        weightGraphMenu.getItems().add(weightMenuItem);
        item2.setOnAction((event -> weightGraphMenu.show(newGoalButton, 800, 600)));
    }

    /**
     * Returns a series of the best fit line
     * @param series series
     * @return series
     */
    private XYChart.Series<String, Number> calculateBestFitLine(XYChart.Series<String, Number> series) {
        //To get the line of best fit, place a point at the first and then place a point at the last and connect them
        XYChart.Series<String, Number> bestFitLine = new XYChart.Series<>();
        if (series.getData().size() < 2){
            return bestFitLine;
        }
        //Add the first point
        bestFitLine.getData().add(new XYChart.Data<>(series.getData().getFirst().getXValue(), series.getData().getFirst().getYValue()));
        bestFitLine.getData().add(new XYChart.Data<>(series.getData().getLast().getXValue(), series.getData().getLast().getYValue()));
        bestFitLine.setName("Line of Best Fit");
        return bestFitLine;
    }

    /**
     * Makes the progress graph scene
     * @param primaryStage primaryStage
     */
    public void makeProgressScene(Stage primaryStage) {
        BorderPane mainPane = new BorderPane();

        //The Top Box
        BorderPane topPane = new BorderPane();
        topPane.getStyleClass().add("progress-top-box");
        mainPane.setTop(topPane);
        ImageView backButton = HelperClass.createArrowButton("/main/java/IMAGES/angle-double-small-left (1).png");
        backButton.setOnMouseClicked((event -> CalendarScene.calendarScene(primaryStage, CURRENT_YEAR, CURRENT_MONTH)));
        topPane.setLeft(backButton);
        Label progressHeader = new Label("Track Your Progress");
        progressHeader.getStyleClass().add("goal-top-section-label");
        BorderPane.setMargin(progressHeader, new Insets(20));
        topPane.setRight(new Button(){{ //Filler button to keep centered
            setVisible(false);
            setPrefWidth(70);
        }});
        BorderPane.setMargin(backButton, new Insets(0,0,0,25));
        HBox.setMargin(progressHeader, new Insets(25));
        topPane.setCenter(progressHeader);
        Button filler = new Button();
        filler.setVisible(false);
        filler.setPrefWidth(70);
        topPane.getChildren().add(filler);
        topPane.setMinWidth(WINDOW_WIDTH);
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setAlignment(progressHeader, Pos.CENTER);

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

        MenuItem item1 = new MenuItem("Progressive Overload");
        MenuItem item2 = new MenuItem("Weight");
        item1.getStyleClass().add("category-menu-option");
        item2.getStyleClass().add("category-menu-option");

        createWeightGraph(mainPane, item2, newGoalButton);
        createOverloadGraph(mainPane, item1, newGoalButton);

        graphTypeMenu.getItems().addAll(headerItem, item1, item2);
        newGoalButton.setOnAction((event) -> graphTypeMenu.show(newGoalButton, 800, 600));
        Scene mainScene = new Scene(mainPane);
        mainScene.getStylesheets().add(Objects.requireNonNull(CalendarScene.class.getResource("/main/java/CSS/style.css")).toExternalForm());
        primaryStage.setScene(mainScene);
    }
}
