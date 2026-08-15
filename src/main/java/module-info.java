module com.Movies.catalog {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;
    requires org.jetbrains.annotations;
    requires org.postgresql.jdbc;

    exports com.Movies.catalog;
    exports com.Movies.catalog.api;
    exports com.Movies.catalog.dao;
    exports com.Movies.catalog.model;
    exports com.Movies.catalog.service;

    opens com.Movies.catalog to javafx.fxml;
    opens com.Movies.catalog.controller to javafx.fxml;
}