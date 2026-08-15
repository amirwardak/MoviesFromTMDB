package manual;

import com.Movies.catalog.Logger;
import com.Movies.catalog.api.TmdbApiClient;
import com.Movies.catalog.model.Movie;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TmdbApiClient client = new TmdbApiClient();

        Logger.info("Поиск фильма 'Матрица'...");
        List<Movie> movies = client.searchMovies("Матрица");

        Logger.info("Найдено фильмов: " + movies.size());
        for (Movie m : movies) {
            System.out.printf("[%d] %s (%.1f) — %s%n",
                    m.getTmdbId(), m.getTitle(), m.getVoteAverage(), m.getReleaseDate());
        }
    }
}