package gui;

import auxiliary_classes.ResponseMessage;
import functional_classes.ClientManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MoviesDisplayScene {
    FXApplication app;
    FlowPane root;
    ClientManager clientManager;
    ResponseMessage response = null;

    public MoviesDisplayScene(FXApplication app, ClientManager clientManager) {
        this.app = app;
        this.clientManager = clientManager;
    }

    public Scene openScene() throws SQLException {
        response = null;
        clientManager.commandsWithoutParam("getAllMoviesRS");
        while (response == null || !app.clientSerializer.isReadyToReturnMessage()){
            response = app.clientSerializer.getNewResponse();
        }
        app.clientSerializer.setReadyToReturnMessage(false);
        ResultSet resultSet = (ResultSet) response.getResponseData();
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPrefWidth(1000);
        gridPane.setPrefHeight(500);
        gridPane.setPadding(new Insets(1));


        String lastCreator = "";
        List<Color> colorList = new ArrayList<>();
        HashMap<String, Color> map = new HashMap<>();
        while (resultSet.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                //Iterate Column
                row.add(resultSet.getString(i));
            }
            if (!Objects.equals(row.get(row.size() - 1), lastCreator)) {
                lastCreator = row.get(row.size() - 1);
                Color newColor;
                if (!map.containsKey(lastCreator)){

                    do {
                        newColor = Color.rgb((int) Math.round(30 + (Math.random() * 225)), (int) Math.round(30 + (Math.random() * 225)), (int) Math.round(30 + (Math.random() * 225)));
                    } while (colorList.contains(newColor));
                    colorList.add(newColor);
                    map.put(lastCreator, newColor);
                } else {
                    newColor = map.get(lastCreator);
                }
            }
//            System.out.println(gridPane.getPrefWidth() + " " + gridPane.getPrefHeight());
            root = new FlowPane(Orientation.VERTICAL, 30.0, 30.0);
            root.setPrefWidth(1000);
            root.setPrefHeight(500);
            root.getChildren().add(app.navigateButtonList());
            Polygon star = new Polygon();
//            center = 100, 44
            double k = Long.parseLong(row.get(6)) / Math.pow(2, 64);
            double r = Math.min(0.5 + k * 8, 0.81);
//            System.out.println(r);
            star.getPoints().addAll(
                    100.0, 44 - 44 * r,
                    100 + 17 * r, 44.0 - 4 * r,
                    100 + 60*r, 44.0 - 4*r,
                    100 + 27*r, 44 + 26*r,
                    100 + 39*r, 44 + 78*r,
                    100.0, 44 + 45*r,
                    100 - 39*r, 44 + 78*r,
                    100 - 27*r, 44 + 26*r,
                    100 - 60*r, 44.0 - 4 * r,
                    100 - 17 * r, 44.0 - 4 * r
            );
            double posX = Math.min(Math.max(gridPane.getPrefWidth() / 10 * (0.5 + (Math.pow(Double.parseDouble(row.get(2)) / Math.pow(2, 31), 0.9))) - k, 0), 75);
            double posY = Math.min(Math.max(gridPane.getPrefHeight() / 10 * (0.25 + (Math.pow(Double.parseDouble(row.get(3)) / Math.pow(2, 34), 0.8))) - k, 0), 20);
            star.setFill(map.get(lastCreator));
            star.setStroke(Color.BLACK);
//            star.setStrokeWidth(2);
//            root.getChildren().add(star);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, event -> star.setRotate(star.getRotate() + 1)),
                    new KeyFrame(Duration.millis(10))
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            Label label = new Label(row.get(1));
            StackPane stackPane = new StackPane(star, label);
            StackPane.setAlignment(label, Pos.CENTER);
            stackPane.setOnMouseClicked(event -> {
                app.setMovieInfoScene(Integer.parseInt(row.get(0)), row.get(row.size() - 1));
            });

            gridPane.add(stackPane, (int) posX, (int) posY);
//            System.out.println(Arrays.asList((int) posX, (int) posY));
        }
//        gridPane.setGridLinesVisible(true);
        root.getChildren().add(gridPane);
        return new Scene(root, 300, 150, Color.GREEN);
    }
}