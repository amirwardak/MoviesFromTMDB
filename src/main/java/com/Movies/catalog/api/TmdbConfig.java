package com.Movies.catalog.api;

import java.io.InputStream;
import java.util.Properties;


public class TmdbConfig {
    private static final String PROPERTIES_FILE = "config.properties";
    private static String apiKey;
    private static String baseUrl;
    private static String imageBaseUrl;

    static {
        Properties properties = new Properties();
        try(InputStream is = TmdbConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)){
            properties.load(is);

            apiKey = properties.getProperty("tmdb.api.key");
            baseUrl = properties.getProperty("tmdb.base.url");
            imageBaseUrl = properties.getProperty("tmdb.image.base.url");
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("API key TMDB wasn't found in config.properties!" );
            }

        } catch (Exception e) {
            throw new RuntimeException("Config reading error: " + e.getMessage(), e);
        }
    }

    private TmdbConfig() {}

    public static String getApiKey(){
        return apiKey;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getImageBaseUrl() {
        return imageBaseUrl;
    }
}
