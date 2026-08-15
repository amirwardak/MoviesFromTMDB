package com.Movies.catalog;

import com.Movies.catalog.dao.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var url = getClass().getResource("/com/Movies/catalog/view/main-view.fxml");
        if (url == null) throw new IllegalStateException("FXML не найден! Проверь регистр в пути");
        FXMLLoader loader = new FXMLLoader(url);

        Parent root = loader.load();


        primaryStage.setTitle("Каталог фильмов");
        primaryStage.setScene(new Scene(root, 900, 600));
        if (getClass().getResourceAsStream("/images/icon-square.png") == null)
            throw new IllegalStateException("Иконка не найдена! Проверь регистр в пути");
        primaryStage.getIcons().addAll(
                new Image(getClass().getResourceAsStream("/images/icon-square.png"), 16, 16, true, true),
                new Image(getClass().getResourceAsStream("/images/icon-square.png"), 32, 32, true, true),
                new Image(getClass().getResourceAsStream("/images/icon-square.png"), 48, 48, true, true),
                new Image(getClass().getResourceAsStream("/images/icon-square.png"), 256, 256, true, true)
        );
        primaryStage.show();
    }
    public static void main(String[] args){
        DatabaseConfig.initDatabase(); // create database and table if they don't exist yet
        launch(args);
    }
}
