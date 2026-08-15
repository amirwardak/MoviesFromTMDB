package com.Movies.catalog.service;

import com.Movies.catalog.Logger;
import com.Movies.catalog.api.TmdbApiClient;
import com.Movies.catalog.dao.MovieDAO;
import com.Movies.catalog.model.Movie;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class MovieService {
    private final TmdbApiClient apiClient;
    private final MovieDAO movieDAO;

    public MovieService(TmdbApiClient apiClient, MovieDAO movieDAO){
        this.apiClient = apiClient;
        this.movieDAO = movieDAO;
    }

    public enum SaveResult{
        SUCCESS("Фильм успешно сохранен!"),
        ALREADY_EXISTS("Этот фильм уже есть в вашем каталоге."),
        ERROR("Произошла ошибка при сохранении. Попробуйте позже.");

        private final String message;
        SaveResult(String message) {this.message = message;}
        public String getMessage() {return this.message;}

    }

    public List<Movie> searchMoviesInTMDB(String query){
        if(query == null || query.isBlank()) return Collections.emptyList();
        List<Movie> remoteResults = apiClient.searchMovies(query);
        if(remoteResults == null || remoteResults.isEmpty()) return Collections.emptyList();
        return remoteResults;
    }

    public List<Movie> searchLocalMovies(String query){
        if(query == null || query.isBlank()) return Collections.emptyList();

        try{
            return movieDAO.searchByTitle(query);
        }
        catch(SQLException e){
            Logger.error("Local search error for \"" + query + "\": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean deleteMovie(int id){
        try{
            return movieDAO.deleteMovie(id);
        }
        catch(SQLException e){
            Logger.error("Delete movie error: " + e.getMessage());
            return false;
        }
        catch (IllegalArgumentException e){
            Logger.error("Invalid id: " + e.getMessage());
            return false;
        }
    }

    public SaveResult saveMovie(Movie movie){
        try{
            if(movieDAO.existsByTmdbId(movie.getTmdbId())){
                Logger.info("Duplicate detected: " + movie.getTitle());
                return SaveResult.ALREADY_EXISTS;
            }

            boolean saved = movieDAO.saveMovie(movie);
            if(saved){
                Logger.info("Saved successfully: " + movie.getTitle());
                return SaveResult.SUCCESS;
            }
            else{
                Logger.error("Insert returned 0 rows for: " + movie.getTitle());
                return SaveResult.ERROR;
            }
        }
        catch(SQLException e){
            Logger.error("Save movie error: " + e.getMessage());
            return SaveResult.ERROR;
        }
    }

    public Movie findLocalByTmdbId(int tmdbId){
        try {
            return movieDAO.findByTmdbId(tmdbId);
        } catch (SQLException e) {
            Logger.error("Find movie by tmdb id error: " + e.getMessage());
            return null;
        }
    }

    public List<Movie> findAllInLocalDB(){
        try {
            return movieDAO.findAllMovies();
        } catch (SQLException e) {
            Logger.error("Find all movies error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
