package com.Movies.catalog.dao;

import com.Movies.catalog.Logger;
import com.Movies.catalog.model.Movie;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO { // Data Access Object
    public boolean saveMovie(Movie movie) throws SQLException{

        String addNewMovieToDB = """
                INSERT INTO movies (tmdb_id, title, overview, release_date, vote_average, poster_path)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(addNewMovieToDB)){
            ps.setInt(1, movie.getTmdbId());
            ps.setString(2, movie.getTitle());
            ps.setString(3, movie.getOverview());
            ps.setObject(4, movie.getReleaseDate()); // LocalDate in Java will be converted into SQL DATE
            ps.setDouble(5, movie.getVoteAverage());
            ps.setString(6, movie.getPosterPath());
            int effectedRows = ps.executeUpdate();

            return effectedRows > 0; // check if any rows were effected
        }
    }

    public boolean existsByTmdbId(int tmdbId) throws SQLException{
        String checkIfAlreadySaved = "SELECT COUNT(*) FROM movies WHERE tmdb_id = ?";
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(checkIfAlreadySaved)){
            ps.setInt(1, tmdbId);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Movie> findAllMovies() throws SQLException{
        List<Movie> movies = new ArrayList<>();
        String findMovies = """
                SELECT id, tmdb_id, title, overview, release_date, vote_average, poster_path
                FROM movies
                ORDER BY release_date DESC
                """;
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(findMovies)){
            try(ResultSet resultSet = ps.executeQuery()){
                while(resultSet.next()){
                    movies.add(mapRowToMovie(resultSet));
                }
            }
            return movies;
        }
    }

    public List<Movie> searchByTitle(String query) throws SQLException{
        String sql = """
                SELECT id, tmdb_id, title, overview, release_date, vote_average, poster_path
                FROM movies 
                WHERE title ILIKE ? ORDER BY release_date DESC
                """;

        List<Movie> movies = new ArrayList<>();

        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setString(1, "%" + query + "%");
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    movies.add(mapRowToMovie(resultSet));
                }
            }
            Logger.info("Find " + movies.size() + " movies");
            return movies;
        }
    }

    public Movie findByTmdbId(int tmdbId) throws SQLException{
        String sql = """
                SELECT id, tmdb_id, title, overview, release_date, vote_average, poster_path
                FROM movies
                WHERE tmdb_id = ?
                """;
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setInt(1, tmdbId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return mapRowToMovie(resultSet);
                }
            }
            return null;
        }
    }

    public boolean deleteMovie(int id) throws SQLException, IllegalArgumentException{
        if(id <= 0){
            throw new IllegalArgumentException("Id can be only a natural number!");
        }
        String sql = """
                DELETE FROM movies WHERE id = ?
                """;
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setInt(1, id);
            int effectedRows = preparedStatement.executeUpdate();
            return effectedRows > 0;
        }
    }

    private @NotNull Movie mapRowToMovie(@NotNull ResultSet resultSet) throws SQLException{
        java.sql.Date sqlDate = resultSet.getDate("release_date");
        LocalDate localDate = sqlDate != null ? sqlDate.toLocalDate() : null;
        Movie movie = new Movie(
                resultSet.getInt("tmdb_id"),
                resultSet.getString("title"),
                resultSet.getString("overview"),
                localDate,
                resultSet.getDouble("vote_average"),
                resultSet.getString("poster_path")
        );
        movie.setId(resultSet.getInt("id"));
        movie.setSaved(true);
        return movie;
    }
}
