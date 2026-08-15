import com.Movies.catalog.api.TmdbApiClient;
import com.Movies.catalog.dao.MovieDAO;
import com.Movies.catalog.model.Movie;
import com.Movies.catalog.service.MovieService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MovieServiceTest {
    public static class FakeApiClient extends TmdbApiClient{
        @Override
        public List<Movie> searchMovies(String query) {
            return List.of(new Movie(999, "Тестовый фильм", "Описание",
                    LocalDate.of(2024, 1, 1), 9.0, "/test.jpg"));
        }
    }

    public static class FakeMovieDAO extends MovieDAO{
        @Override
        public boolean saveMovie(Movie movie) {
            return true; // Всегда успех
        }
    }

    @Test
    void shouldReturnMoviesWhenApiReturnsResults() {
        MovieService service = new MovieService(new FakeApiClient(), new FakeMovieDAO());

        List<Movie> result = service.searchMoviesInTMDB("Матрица");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Тестовый фильм", result.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyListWhenQueryIsBlank() {
        MovieService service = new MovieService(new FakeApiClient(), new FakeMovieDAO());

        List<Movie> result = service.searchMoviesInTMDB("");

        assertTrue(result.isEmpty());
    }
}
