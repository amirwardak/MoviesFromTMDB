package com.Movies.catalog.dao;

import com.Movies.catalog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {
    private static final String url;
    private static final String user;
    private static final String password;

    private static final String CREATE_TABLE_SQL = readSchemaSql();

    static { // static initializer block complete one time to load DB configs
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("db.properties not found in resources! " +
                        "Copy db.properties.example to db.properties and fill in your credentials.");
            }

            Properties props = new Properties();
            props.load(input);

            // getting configs from properties file
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            if (url == null || url.isBlank()) {
                throw new RuntimeException("db.url is missing or empty in db.properties!");
            }
            if (user == null || user.isBlank()) {
                throw new RuntimeException("db.user is missing or empty in db.properties!");
            }
            if (password == null || password.isBlank()) {
                throw new RuntimeException("db.password is missing or empty in db.properties!");
            }

            Class.forName("org.postgresql.Driver"); // add PostgreSQL driver
        }
        catch(Exception e){
            throw new RuntimeException("DB config error: " + e.getMessage(), e);
        }
    }

    private DatabaseConfig() {}

    public static void initDatabase(){ // creating of the database and the table if they don't exist yet
        Logger.info("Initialization of DB ...");
        ensureDatabaseExists();

        try(Connection conn = getConnection()){
            boolean alreadyExisted = tableExists(conn, "movies");
            try(Statement stmt = conn.createStatement()){
                stmt.execute(CREATE_TABLE_SQL);
            }
            if(alreadyExisted){
                Logger.info("Table \"movies\" already exists");
            }
            else{
                Logger.info("Table \"movies\" created successfully");
            }
        }
        catch(Exception e){
            Logger.error("Critical error of connection: " + e.getMessage());
            throw new RuntimeException("Can't connect to DB", e);
        }
    }

    public static Connection getConnection() throws SQLException { // method to connect with DB
        return DriverManager.getConnection(url, user, password);
    }

    // creates the database if it doesn't exist yet; connects to the maintenance "postgres" database for that
    private static void ensureDatabaseExists(){
        String dbName = databaseName();
        if(dbName.isEmpty()){
            Logger.error("Can't extract database name from db.url: " + url);
            return;
        }

        try(Connection conn = DriverManager.getConnection(maintenanceDbUrl(), user, password);
            PreparedStatement check = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")){
            check.setString(1, dbName);
            try(ResultSet rs = check.executeQuery()){
                if(rs.next()){
                    Logger.info("Database \"" + dbName + "\" already exists");
                    return;
                }
            }
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("CREATE DATABASE \"" + dbName.replace("\"", "\"\"") + "\"");
            }
            Logger.info("Database \"" + dbName + "\" created successfully");
        }
        catch(SQLException e){
            Logger.error("Can't check/create database \"" + dbName + "\": " + e.getMessage() +
                    " Create it manually and make sure the user has rights on it.");
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException{
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, tableName);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }
    }

    // extracts the database name from db.url, e.g. "movies_db" from "jdbc:postgresql://localhost:5432/movies_db"
    private static String databaseName(){
        String clean = url.split("\\?")[0];
        int slash = clean.lastIndexOf('/');
        return clean.substring(slash + 1);
    }

    // same URL as db.url, but pointing to the maintenance database "postgres"
    private static String maintenanceDbUrl(){
        String clean = url.split("\\?")[0];
        int slash = clean.lastIndexOf('/');
        return clean.substring(0, slash + 1) + "postgres";
    }

    private static String readSchemaSql(){
        try(InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("schema.sql")){
            if(input == null){
                throw new RuntimeException("schema.sql not found in resources!");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch(IOException e){
            throw new RuntimeException("Failed to read schema.sql: " + e.getMessage(), e);
        }
    }
}
