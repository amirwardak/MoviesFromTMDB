package com.Movies.catalog.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

public class Toast {
    private static final int MAX_WIDTH = 400;
    private static final int MIN_WIDTH = 250;
    private static final double PADDING_H = 30;
    private static final double PADDING_V = 15;

    /**
     * @param message Message text
     * @param durationSec Show duration in seconds
     */
    public static void show(String message, int durationSec){
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        StackPane pane = new StackPane(label);
        pane.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 40, 40, 0.9),
                new CornerRadii(8),
                null
        )));
        double textWidth = label.prefWidth(-1);
        double calculatedWidth = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, textWidth + PADDING_H * 2));

        pane.setPrefWidth(calculatedWidth);
        pane.setMaxWidth(calculatedWidth);
        pane.setPadding(new Insets(PADDING_V, PADDING_H, PADDING_V, PADDING_H));

        Popup popup = new Popup();
        popup.getContent().add(pane);
        popup.setAutoHide(false);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        Window owner = Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
        if(owner == null) return;
        popup.show(owner, screenWidth - 320, screenHeight - 100);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(durationSec));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), pane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev->popup.hide());
            fadeOut.play();
        });
        delay.play();
    }

    public static void success(String msg) { show(msg, 4); }
    public static void info(String msg) { show(msg, 5); }
    public static void warning(String msg) { show(msg, 6); }
}
