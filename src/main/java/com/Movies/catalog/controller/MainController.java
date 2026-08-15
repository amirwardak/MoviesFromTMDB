package com.Movies.catalog.controller;

import com.Movies.catalog.Logger;
import com.Movies.catalog.api.TmdbApiClient;
import com.Movies.catalog.api.TmdbConfig;
import com.Movies.catalog.dao.MovieDAO;
import com.Movies.catalog.model.Movie;
import com.Movies.catalog.service.MovieService;
import com.Movies.catalog.ui.Toast;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private TextField searchField;
    @FXML private TableView<Movie> moviesTable;
    @FXML private TableColumn<Movie, String> titleColumn;
    @FXML private TableColumn<Movie, Integer> yearColumn;
    @FXML private TableColumn<Movie, Double> ratingColumn;
    @FXML private ComboBox<String> searchModeCombo;

    @FXML private Button actionButton1;
    @FXML private Button actionButton2;

    @FXML private ImageView posterView;
    @FXML private ProgressIndicator loadingSpinner;

    @FXML private Label detailTitle, detailYear, detailRating;
    @FXML private TextArea detailOverview;
    @FXML private Label detailIsSaved;

    private final MovieService movieService;

    private void loadPosterAsync(String path){
        if(path == null || path.isBlank()){
            posterView.setImage(null);
            hideSpinner();
            return;
        }
        String url = TmdbConfig.getImageBaseUrl() + path;
        showSpinner();

        Image image = new Image(url, true); // background loading

        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.doubleValue() >= 1.0 || image.isError()){
                hideSpinner();
            }
        });
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if(Boolean.TRUE.equals(isError)){
                Logger.error("Failed to load poster: " + url);
                Toast.warning("Не удалось загрузить постер");
                hideSpinner();
            }
        });

        posterView.setImage(image);

        if(image.isError() || image.getProgress() >= 1.0){
            hideSpinner();
        }
    }

    private void showSpinner() {
        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
    }

    private void hideSpinner() {
        loadingSpinner.setVisible(false);
        loadingSpinner.setManaged(false);
    }

    public MainController(){
        this.movieService = new MovieService(new TmdbApiClient(), new MovieDAO());
    }
    // binding table columns with fields in class Movie
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        yearColumn.setCellValueFactory(cellData ->{
            var date = cellData.getValue().getReleaseDate();
            if(date != null){
                return new SimpleIntegerProperty(date
                        .getYear()).asObject();
            } else{
                return new SimpleIntegerProperty(0).asObject();
            }
        });
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("voteAverage"));

        moviesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection)
                -> onMovieSelected(newSelection));
    }

    @FXML
    public void onSearchClick(){
        String query = searchField.getText();
        String mode = searchModeCombo.getValue();
        if(query == null || query.isBlank()){
            Toast.info("Введите название фильма для поиска");
            return;
        }

        // async
        Task<List<Movie>> searchTask = new Task<List<Movie>>() { // creating a task to execute in thread
            @Override
            protected List<Movie> call() throws Exception {
                if("Найти в TMDB".equals(mode)){
                    return movieService.searchMoviesInTMDB(query);
                }
                else{
                    return movieService.searchLocalMovies(query);
                }
            }
        };

        searchTask.setOnSucceeded(e -> { // if success show movies in the table
                    List<Movie> results = searchTask.getValue();
                    moviesTable.getItems()
                            .setAll(results);
                    clearDetails();
                    if(results == null || results.isEmpty()){
                        if("Найти в TMDB".equals(mode)){
                            Toast.info("В TMDB ничего не найдено по запросу \"" + query
                                    + "\". Попробуйте изменить запрос или проверьте подключение к интернету.");
                        }
                        else{
                            Toast.info("В вашем каталоге ничего не найдено по запросу \"" + query + "\".");
                        }
                    }
                }
        );


        searchTask.setOnFailed(e -> {
            Logger.error("Search error: "
                    + searchTask.getException().getMessage());
            Toast.warning("Не удалось выполнить поиск. Попробуйте еще раз.");
        });

        new Thread(searchTask).start(); // start executing task in a thread
    }



    private void onMovieSelected(Movie movie){ // if user selected a row in the movies table
        if(movie == null){
            clearDetails();
            return;
        }

        detailTitle.setText(movie.getTitle());
        detailYear.setText(movie.getReleaseDate() != null ? String.valueOf(movie.getReleaseDate().getYear())
                        : "Год неизвестен");
        detailRating.setText("Рейтинг: " + movie.getVoteAverage());
        detailOverview.setText(
                movie.getOverview() != null ? movie.getOverview() : "Описание отсутствует");

        Movie savedMovie = movieService.findLocalByTmdbId(movie.getTmdbId());
        boolean isSaved = savedMovie != null;
        movie.setSaved(isSaved);
        if(isSaved){
            movie.setId(savedMovie.getId());
        }

        if(movie.isSaved()){
            detailIsSaved.setText("Сохранен");
            detailIsSaved.setStyle("-fx-text-fill: green");
        }
        else{
            detailIsSaved.setText("Не сохранен");
            detailIsSaved.setStyle("-fx-text-fill: red");
        }

        loadPosterAsync(movie.getPosterPath());

        if(movie.getId() > 0){ // already saved in local DB
            actionButton1.setVisible(true);
            actionButton1.setText("Удалить");
            actionButton1.setOnAction(e -> handleDelete(movie));
            actionButton2.setVisible(false);
        }
        else{
            actionButton2.setVisible(true);
            actionButton2.setText("Сохранить");
            actionButton2.setOnAction(e -> handleSave(movie));
            actionButton1.setVisible(false);
        }
    }

    private void handleSave(Movie movie) {
        MovieService.SaveResult result = movieService.saveMovie(movie);
        switch (result){
            case SUCCESS -> {
                Movie saved = movieService.findLocalByTmdbId(movie.getTmdbId());
                if(saved != null){
                    movie.setId(saved.getId());
                }
                movie.setSaved(true);
                detailIsSaved.setText("Сохранен");
                detailIsSaved.setStyle("-fx-text-fill: green");
                Toast.success(result.getMessage());
                moviesTable.refresh();
            }
            case ALREADY_EXISTS -> Toast.info(result.getMessage());
            case ERROR -> Toast.warning(result.getMessage());
        }
    }

    private void handleDelete(@NotNull Movie movie) {
        boolean success = movieService.deleteMovie(movie.getId());
        if(success){
            movie.setSaved(false);
            Toast.success("Фильм \"" + movie.getTitle() + "\" был удален из локального каталога!");

            moviesTable.getItems().remove(movie);

            moviesTable.getSelectionModel().clearSelection();

            clearDetails();
        } else {
            Toast.warning("Не удалось удалить фильм. Возможно, он уже был удален!");
        }

    }

    private void clearDetails() {
        detailTitle.setText("");
        detailYear.setText("");
        detailRating.setText("");
        detailOverview.setText("");
        detailIsSaved.setText("");
        posterView.setImage(null);
        hideSpinner();
        actionButton1.setText("Сохранить");
        actionButton1.setOnAction(null);
        actionButton2.setVisible(false);
    }

    public void onShowAllClick(){
        searchModeCombo.setValue("В моем каталоге");
        searchField.clear();

        Task<List<Movie>> searchTask = new Task<>(){

            @Override
            protected List<Movie> call() throws Exception {
                return movieService.findAllInLocalDB();
            }
        };

        searchTask.setOnSucceeded(e ->{
            List<Movie> results = searchTask.getValue();
            moviesTable.getItems()
                    .setAll(results);
            clearDetails();
            if(results == null || results.isEmpty()){
                Toast.info("В вашем каталоге пока нет сохраненных фильмов.");
            }
        });

        searchTask.setOnFailed(e -> {
            Logger.error("Search error: " +
                    searchTask.getException().getMessage());
            Toast.warning("Не удалось загрузить каталог. Попробуйте еще раз.");
        });
        new Thread(searchTask).start();
    }
}
