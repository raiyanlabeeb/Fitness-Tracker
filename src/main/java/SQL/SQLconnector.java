package SQL;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Used to manage SQL commands
 */
public class SQLconnector {
    public Connection connection;

    public SQLconnector() {
        try {
            String password = "";
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/nice", "raiyanl", "!Green10222004");
            Statement statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Get the max reps done on a set given a date and an exercise
     * @param date date
     * @param exercise_name exercise
     * @return max reps
     */
    public int getMaxReps(String date, String exercise_name){
        try {
            Statement statement = connection.createStatement();
            //Returns the max reps done
            ResultSet data = statement.executeQuery("SELECT MIN(reps) AS min_reps\n" +
                    "FROM exercise\n" +
                    "WHERE weight = (\n" +
                    "    SELECT MAX(weight)\n" +
                    "    FROM exercise\n" +
                    "    WHERE exercise_name = '" + exercise_name + "' AND workout_date = '" + date + "'\n" +
                    ")\n" +
                    "AND workout_date = '" + date + "'\n" +
                    "GROUP BY exercise_name;");
            if (data.next()) {
                return data.getInt(1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    /**
     * Adds exercise names to comboBox
     * @param comboBox the combobox to add
     */
    public void addComboBoxExercises(ComboBox<String> comboBox){
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT DISTINCT exercise_name FROM exercise");
            while (data.next()) {
                //We don't want to include exercises that I haven't actually done.
                Statement statement2 = connection.createStatement();
                comboBox.getItems().add(data.getString(1));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Updates all progressive overload stats
     */
    public void refresh(){
        //Automatically do all the set stuff
        //1.) Loop through every single workout
        //2.) Call the isProgressiveOverload function for every set

        try {
            Statement statement = connection.createStatement();
            //Gives information on every single set of every single exercise
            ResultSet data = statement.executeQuery("SELECT * FROM exercise\n" +
                    "ORDER BY workout_date");
            while (data.next()){
                //Call the progressive overload function on each
                isProgressiveOverload(data.getString(1), data.getString(5), data.getInt(2), data.getInt(3), data.getDouble(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the minimum and maximum weight for a certain exercise from start to end date
     * @param exercise exercise name
     * @param start start date
     * @param end end date
     * @return int
     */
    public int[] getMinMaxWeight(String exercise, String start, String end){
        int[] result = new int[2];
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT \n" +
                    "    MIN(weight),\n" +
                    "    MAX(weight)\n" +
                    "FROM \n" +
                    "    exercise\n" +
                    "WHERE exercise_name = + '" + exercise + "' AND workout_date >= + '" + formatDate(start) + "' AND workout_date <= '" + formatDate(end) + "' \n");
            if (data.next()) {
                result[0] = data.getInt(1);
                result[1] = data.getInt(2);
            }
            return result;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the minimum and maximum overload for a certain exercise from start to end date
     * @param exercise exercise name
     * @param start start date
     * @param end end date
     * @return int
     */
    public int[] getMinMaxOverload(String exercise, String start, String end){
        int[] result = new int[2];
        try {
            Statement statement = connection.createStatement();
            if (!exercise.equals("All Exercises")) {
                result[0] = 0;
                result[1] = 1;
            } else {
                result[0] = 0;
                result[1] = 100;
            }
            return result;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a table of the weight's done by a certain exercise on every date.
     * @param exercise the exercise name
     * @param start the start date
     * @param end the end date
     * @return the results
     */
    public ResultSet getWeightGraphData(String exercise, String start, String end){
        try {
            Statement statement = connection.createStatement();
            start = formatDate(start);
            end = formatDate(end);
            ResultSet data = statement.executeQuery("SELECT \n" +
                    "    workout_date,\n" +
                    "    MAX(weight) AS max_weight\n" +
                    "FROM \n" +
                    "    exercise\n" +
                    "WHERE exercise_name = + '" + exercise + "' AND workout_date >= + '" + start + "' AND workout_date <= '" + end + "' \n" +
                    "GROUP BY \n" +
                    "    workout_date\n" +
                    "ORDER BY workout_date");
            if (data != null){
                return data;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Adds to the exercise and workout tables
     * @param w_title Workout title ex: Push
     * @param e_name Exercise name ex: Bench
     * @param s_id Set number
     * @param e_date Exercise date ex: 2024-05-25
     * @param re Num reps
     * @param we Num weight
     * @throws SQLException SQL exception
     */
    public void addToExercise(String w_title, String e_name, int s_id, String e_date, int re, double we) {
        try {
            if (Objects.equals(w_title, "") || Objects.equals(e_date, "") || e_name.isEmpty()){
                System.out.println("EMPTY FIELD");
                return;
            }

            e_date = formatDate(e_date);
            Statement statement = connection.createStatement();
            //Check and see if there's already an entry in the WORKOUT table for the day
            if (statement.executeQuery("SELECT * FROM workout WHERE workout_date = '" + e_date + "'").next() == false){
                statement.executeUpdate("INSERT INTO workout (workout_title, workout_date) VALUES(" + "'" + w_title + "', " + "'" + e_date + "'"+ ")");
                System.out.println("Successful workout entry.");
            }
            //Prevent duplicate entries
            if (statement.executeQuery("SELECT * FROM exercise WHERE exercise_name = '" + e_name + "' AND set_id = " + s_id + " AND workout_date = '" + e_date + "'").next() == false){
                statement.executeUpdate("INSERT INTO exercise (exercise_name, set_id, reps, weight, workout_date) VALUES(" + "'" + e_name + "', " + s_id + "," + re + "," + we + ", '" + e_date + "')");
                System.out.println("Successful database entry.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Loads the information in the exercise table given a specific date.
     * @param e_date the date given from a text_field
     * @return
     */
    public ResultSet loadFromExercise(String e_date){
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            e_date = formatDate(e_date);
            return statement.executeQuery("SELECT * FROM exercise WHERE workout_date = '" + e_date + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Formats the date in YYYY-MM-DD from MM-DD-YYYY
     * @param w_date workout date
     * @return formatted date
     */
    public String formatDate(String w_date){
        //Format the date correctly
        String[] fields = w_date.split("/");
        String month = fields[0];
        String day = fields[1];
        String year = fields[2];
        if (year.length() < 4){
            year = "20" + year;
        }
        if (fields[0].length() == 1) {
            month = "0" + fields[0];
        }
        if (fields[1].length() == 1){
            day = "0" + fields[1];
        }
        return year + "-" + month + "-" + day;
    }
    /**
     * Loads the information in the workout table given a specific date.
     * @param e_date the date given from a text_field
     * @return
     */
    public ResultSet loadFromWorkout(String e_date){
        try {
            Statement statement = connection.createStatement();
            e_date = formatDate(e_date);
            ResultSet data = statement.executeQuery("SELECT * FROM workout WHERE workout_date = '" + e_date + "'");
            return data;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteSetFromExercise(String e_name, String w_date, int set_id) {
        try {
            if (Objects.equals(w_date, "") || Objects.equals(e_name, "")){
                System.out.println("EMPTY FIELD");
                return;
            }
            Statement statement = connection.createStatement();
            w_date = formatDate(w_date);
            statement.executeUpdate("DELETE FROM exercise WHERE exercise_name = '" + e_name + "' AND workout_date = '" + w_date + "' AND set_id = " + set_id);
            //If there are no more of that exercises on that date, remove it from the workout table
            if (statement.executeQuery("SELECT * FROM exercise WHERE workout_date = '" + w_date + "'").next() == false){
                statement.executeUpdate("DELETE FROM workout WHERE workout_date = '" + w_date + "'");
                System.out.println("Successfully deleted from workout.");
            }
            System.out.println("Successfully deleted set.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the highest set given a date and exercise name
     * @param date date
     * @param e_name exercise name
     * @return greatest set
     */
    public int retrieveSet(String date, String e_name) throws SQLException {
        Statement statement = connection.createStatement();
        date = formatDate(date);
        ResultSet data = statement.executeQuery("SELECT set_id FROM exercise\n" +
                "WHERE exercise_name = '" + e_name + "' AND workout_date = '" + date + "'\n" +
                "ORDER BY set_id DESC\n" +
                "LIMIT 1");
        if (data.next()){
            return data.getInt(1);
        }
        return 0;
    }

    /**
     * Checks if the current set of the exercise has progressive overload with the LAST TIME the exercise has been done. If either reps is higher or weight is higher
     * @param e_name exercise name
     * @param w_date workout date
     * @param current_reps crurent reps
     * @param current_weight current weight
     * @param current_set current set
     * @return
     */
    public boolean isProgressiveOverload(String e_name, String w_date, int current_set, int current_reps, double current_weight){
        w_date = formatDate(w_date);

        try {
            Statement newstatement = connection.createStatement();
            ResultSet data = newstatement.executeQuery("SELECT * FROM exercise\n" +
                    "WHERE exercise_name = '" + e_name + "' AND workout_date < '" + w_date + "'\n" +
                    "AND set_id = " + current_set + " ORDER BY workout_date DESC LIMIT 1");

        //Returns the data about the most recent time you did the exercise at that set
            if (data.next()){
                boolean value = current_reps > data.getInt(3) || current_weight > data.getDouble(4);
                if (value) {
                    //Set the progressive overload in the table to true (1)
                    Statement statement = connection.createStatement();
                    statement.executeUpdate("UPDATE exercise\n" +
                            "SET progressive_overload = 1\n" +
                            "WHERE exercise_name = '" + e_name + "' AND workout_date = '" + w_date + "' AND set_id = " + current_set);
                }

                //UPDATE THE TOTAL PROGRESSIVE OVERLOAD IN WORKOUT TABLE
                // Total workout percentage = Num of sets progressively overloaded / num total sets

                Statement statement = connection.createStatement();
                //Returns the total number of sets done in the workout
                ResultSet data1 = statement.executeQuery("SELECT COUNT(set_id) FROM exercise\n" +
                        "WHERE workout_date = '" + w_date + "'");
                int total_sets = 1; //So we don't deal with divide by 0
                if (data1.next()){
                    total_sets = data1.getInt(1);
                }

                Statement statement1 = connection.createStatement();
                //Returns the total number of progressively overloaded sets in the workout
                ResultSet data2 = statement1.executeQuery("SELECT COUNT(set_id) FROM exercise\n" +
                        "WHERE workout_date = '" + w_date + "' AND progressive_overload = 1");
                int total_overload = 0;
                if (data2.next()){
                    total_overload = data2.getInt(1);
                }

                Statement statement2 = connection.createStatement();
                //UPDATES THE TOTAL PROGRESSIVE OVERLOAD
                statement2.executeUpdate("UPDATE workout\n" +
                        "SET progressive_overload_percent = " + (int) ( (double) total_overload / total_sets * 100) +
                        " WHERE workout_date = '" + w_date + "'");
                return value;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    /**
     * Adds a new consistency goal given a frequency.
     * EX: I want to work out 4 times a week.
     * @param frequency String
     */
    public void addConsistencyGoal(String frequency){
        try {
            Statement statement = connection.createStatement();
            //Make sure to delete the existing entry if any, we only want 1 entry in the table.
            statement.executeUpdate("DELETE FROM consistency_goal");
            statement.executeUpdate("INSERT INTO consistency_goal VALUES(" + frequency + ", 0)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the consistency goal
     * @return ResultSet
     */
    public ResultSet getConsistencyGoal(){
        try {
            return connection.createStatement().executeQuery("SELECT * FROM consistency_goal");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the consistency goal and returns the progress.
     */
    public int updateConsistencyGoal(int year){
        try {
            Statement statement = connection.createStatement();
            Statement statement2 = connection.createStatement();
            Statement statement3 = connection.createStatement();
            Statement statement4 = connection.createStatement();

            int progressLevel = 0;

            //The dates you worked out
            ResultSet workoutDates = statement.executeQuery("SELECT workout_date FROM workout\n" +
                    "WHERE workout_date LIKE '" + year + "%'");

            //How many times you want to work out per week
            ResultSet frequencySet = statement2.executeQuery("SELECT frequency FROM consistency_goal");
            int frequency;
            if (frequencySet.next()){
                frequency = frequencySet.getInt(1);
            } else {
                //Error case
                return 0;
            }

            LocalDate first = LocalDate.now();
            workoutDates.next();
            LocalDate second =  workoutDates.getDate(1).toLocalDate();
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            while (!workoutDates.isLast()) {
                //2 Conditions, if there is no more data, or if we go onto the next year
                if (workoutDates.getDate(1).toLocalDate().getYear() > year) {
                    break;
                }

                //Move second 1 forward
                first = second;
                workoutDates.next();
                second = workoutDates.getDate(1).toLocalDate();

                int count = 1;

                //While there is data and they are in the same week of the year
                while (!workoutDates.isLast() && first.get(weekFields.weekOfYear()) == second.get(weekFields.weekOfYear())) {
                    count++;

                    //Move forward one space
                    first = second;
                    workoutDates.next();
                    second = workoutDates.getDate(1).toLocalDate();
                }

                //If the count is greater than the number of workouts you want to hit a week, add 1 to the progress level.
                if (count >= frequency) {
                        //Increments progress by 1
                        progressLevel++;
                    }
                }

            statement3.executeUpdate("UPDATE consistency_goal SET progress = " + progressLevel);
            ResultSet progress = statement4.executeQuery("SELECT progress FROM consistency_goal");
            if (progress.next()){
                return progress.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
    /**
     * Does a workout exist on this date?
     * @param date Date
     * @return boolean
     */

    /**
     * Is there a consistency goal?
     * @return boolean
     */
    public boolean hasConsistencyGoal(){
        try {
            return connection.createStatement().executeQuery("SELECT * FROM consistency_goal").next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is there a weight goal?
     * @return boolean
     */
    public boolean hasWeightGoal(){
        try {
            return connection.createStatement().executeQuery("SELECT * FROM weight_goal").next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helps with weight goal. Returns the highest weight you've ever done given an exercise name
     * @param exerciseName exercise name
     * @return the highest weight done
     */
    public int getHighestWeight(String exerciseName){
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT MAX(weight) FROM exercise WHERE exercise_name = '" + exerciseName + "'");
            if (data.next()){return data.getInt(1);}
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
    /**
     * Returns all weight goals
     * @return
     */
    public ResultSet getWeightGoals(){
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT * FROM weight_goal");
            if (data.next()){
                return data;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public boolean workoutExists(String date){
        try {
            Statement statement = connection.createStatement();
            date = formatDate(date);
            return statement.executeQuery("SELECT workout_title FROM workout WHERE workout_date = '" + date +"'").next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the name of the workout on that date
     * This method should NOT be called if there is no workout available on the date
     * @param date the date
     * @return the workout name
     */
    public String getWorkoutName(String date){
        try {
            Statement statement = connection.createStatement();
            date = formatDate(date);
            ResultSet data = statement.executeQuery("SELECT workout_title FROM workout WHERE workout_date = '" + date +"'");
            if (data.next()){
                return data.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ResultSet getOverloadGraphData(String exercise, String start, String end) {
        try {
            Statement statement = connection.createStatement();
            start = formatDate(start);
            end = formatDate(end);

            //For progressive overload with a single exercise
            if (!exercise.equals("All Exercises")) {
                ResultSet data = statement.executeQuery("SELECT workout_date, AVG(progressive_overload)\n" +
                        "FROM exercise\n" +
                        "WHERE exercise_name = '" + exercise + "'\n" +
                        "AND workout_date >= '" + start + "' AND workout_date <= '" + end + "'" + "\n GROUP BY workout_date");
                if (data != null) {
                    return data;
                }
            } else {
                //Total progressive overload
                ResultSet data = statement.executeQuery("SELECT workout_date, progressive_overload_percent\n" +
                        "FROM workout\n" +
                        "WHERE workout_date >= '" + start + "' AND workout_date <= '" + end + "'");
                if (data != null) {
                    return data;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes the weight goal given an exercise name.
     * @param exerciseName the exercise name
     */
    public void removeWeightGoal(String exerciseName) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM weight_goal WHERE exercise_name = '" + exerciseName + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        /**
         * Removes the consistency goal from the database.
         */
        public void removeConsistencyGoal(){
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate("DELETE FROM consistency_goal");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

    }

    /**
     * Add an weight goal given an exercise name and pound amount
     * @param exerciseName exercise name
     * @param pounds pound amount
     */
    public void addWeightGoal(String exerciseName, String pounds){
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO weight_goal VALUES(" + "'" + exerciseName + "' , " + pounds + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds to the schedule goal database
     * @param splitField the split
     */
    public void addScheduleGoal(ArrayList<TextField> splitField) {
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT * FROM schedule_goal");
            //So there's only 1 schedule goal
            if (!data.next()){
                for (int i = 0; i < splitField.size(); i++) {
                    if (splitField.get(i).getText() != null){
                        statement.executeUpdate("INSERT INTO schedule_goal VALUES (" + i + " , '" + splitField.get(i).getText() + "')");
                    } else {
                        return;
                    }
                }
            } else {
                System.out.println("ALREADY EXISTING SCHEDULE");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is there a schedule goal?
     * @return boolean
     */
    public boolean hasScheduleGoal() {
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT * FROM schedule_goal");
            return data.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the schedule goal
     * @return boolean
     */
    public ResultSet getScheduleGoal() {
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT * FROM schedule_goal");
            if (data != null){
                return data;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the schedule goal
     */
    public void removeScheduleGoal() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM schedule_goal");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the next workout ON or AFTER the given day of the week.
     * @param dayOfWeek day of the week
     * @return 0: day 1: workout
     */
    public String[] getNextWorkout(int dayOfWeek){
        try {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("SELECT * FROM schedule_goal WHERE muscle_group != 'Rest' AND day_of_week >= " + dayOfWeek);
            if (data.next()){
                String[] returnVal = new String[2];
                returnVal[0] = data.getString(1);
                returnVal[1] = data.getString(2);
                return returnVal;
            } else { return null; }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
