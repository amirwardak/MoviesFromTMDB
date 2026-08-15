package com.Movies.catalog.api;

import com.Movies.catalog.Logger;
import com.Movies.catalog.model.Movie;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class TmdbApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public TmdbApiClient(){
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
        this.apiKey = TmdbConfig.getApiKey();
        this.baseUrl = TmdbConfig.getBaseUrl();
    }

    public List<Movie> searchMovies(String query){
        try{
            // preparing URL look to create a request
            String url = String.format("%s/search/movie?query=%s&language=ru-RU&page=1", baseUrl,
                    java.net.URLEncoder.encode(query, StandardCharsets.UTF_8));
            // creating Http Request for searching movie
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            // getting server response after sending request;
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // checking response status to catch error responses
            if(response.statusCode() != 200){
                Logger.error("API TMDB error: " + response.statusCode() + " | Body:  " + response.body());
                return Collections.emptyList();
            }
            JsonNode root = objectMapper.readTree(response.body());
            // retrieving "results" from response in JSON format
            JsonNode results = root.get("results");

            if(results == null || results.isEmpty()) return Collections.emptyList();
            // converting JSON into Movie objects
            List<Movie> movies = objectMapper.readValue(results.toString(), new TypeReference<List<Movie>>() {});

            return movies != null ? movies : Collections.emptyList();

        } catch (Exception e) {
            Logger.error("Critical error while requesting TMDB: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}
