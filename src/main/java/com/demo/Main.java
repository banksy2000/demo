package com.demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Main extends Application {

    // get screensize of monitor
    public static Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
    public static Controller controller = new Controller();
    public static String styleSheet = Main.class.getResource("css/style.css").toExternalForm();
    public static Stage stage;

    //Load FXML file names
    public static URL fxmlWin = Main.class.getResource("gameWinScene.fxml");
    public static URL fxmlLoose = Main.class.getResource("gameOverScene.fxml");
    public static URL fxmlHome = Main.class.getResource("homeScene.fxml");
    public static URL fxmlUserReg = Main.class.getResource("userReg.fxml");
    public static URL fxmlLevels = Main.class.getResource("levelsScene.fxml");
    public static URL fxmlUsers = Main.class.getResource("usersScene.fxml");

    public static Level level;
    public static int currentLevel;
    public static final int LEVELS = 6;
    public static Profile currentProfile;
    public static ArrayList<Profile> profiles = new ArrayList<>();




    /**
     * Setup the new application.
     * @param mainStage The stage that is to be used for the application.
     */
    @Override
    public void start(Stage mainStage) throws IOException {

        stage = mainStage;
        stage.setTitle("Chasing Jewels");
        setProfiles();

        stage.setScene(createScene(new FXMLLoader(fxmlHome)));

        stage.setMaximized(true);
        stage.show();
    }


    public static void openLevel(int n) throws IOException {
        level = new Level(n);
        playLevel(n);
    }

    public static void playLevel(int n) throws IOException {
        currentLevel = n;
        stage.setScene(level.getScene());
        level.setDrawTimeline(new Timeline(new KeyFrame(Duration.millis(400), event -> {
            level.play();
            if (level.isWon) {
                level.stopGame();
                try {
                    stage.setScene(createScene(new FXMLLoader(fxmlWin)));
                    controller.updateScore();
                    controller.setLevelNumber(n);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (level.isLost) {
                level.stopGame();
                try {
                    stage.setScene(createScene(new FXMLLoader(fxmlLoose)));
                    controller.updateScore();
                    controller.setLevelNumber(n);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        })));
        level.getDrawTimeline().setCycleCount(Animation.INDEFINITE);
        level.getDrawTimeline().play();
    }

    public static void createNewProfile(String userName) throws IOException {
        Profile profile = new Profile(userName);
        profiles.add(profile);
        currentProfile = profile;
        stage.setScene(Main.createScene(new FXMLLoader(Main.fxmlLevels)));
        controller.setLevelBtns();
        profile.setLevelBtns(controller);
    }

    public static void setProfiles() throws IOException {
        ArrayList<String> data = FileIO.readProfiles();
        profiles = new ArrayList<>();
        if (!data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                profiles.add(new Profile(data.get(i).split(" ")[0],
                        Integer.parseInt(data.get(i).split(" ")[1]),
                        Integer.parseInt(data.get(i).split(" ")[2])));
            }
        }
    }

    public static void openProfileLevels(String username) throws IOException {
        for (Profile profile: profiles) {
            if (username.equals(profile.getName())) {
                stage.setScene(Main.createScene(new FXMLLoader(Main.fxmlLevels)));
                controller.setLevelBtns();
                profile.setLevelBtns(controller);
                currentProfile = profile;
                break;
            }
        }
    }

    public static void deleteProfile(String username) throws IOException {
        FileIO.removeProfile(username);
        setProfiles();
    }

    public static void setProfilesList(ListView listView) {
        for (int i = profiles.size()-1; i >= 0; i--) {
            listView.getItems().add(profiles.get(i).getName());
        }
    }

    public static void saveGameState() {
        level.saveLevelState(currentProfile);
    }

    public static void playGameFromSavedState(File file, int levelNum) throws IOException {
        ArrayList<String> data = FileIO.readLevelState(file);
        level = new Level(levelNum, data);
        playLevel(levelNum);
    }

    public static Scene createScene(FXMLLoader fxmlLoader) throws IOException {
        fxmlLoader.setController(controller);
        BorderPane root = new BorderPane();
        root.setCenter(fxmlLoader.load());
        root.setId("root");
        Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
        scene.getStylesheets().add(styleSheet);
        return scene;
    }



    public static void main(String[] args) {
        launch();
    }
}