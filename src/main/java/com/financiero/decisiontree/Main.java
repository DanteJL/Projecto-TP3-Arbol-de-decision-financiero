package com.financiero.decisiontree;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación JavaFX.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/financiero/decisiontree/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            
            // Cargar estilos CSS
            String cssPath = getClass().getResource("/com/financiero/decisiontree/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("Árbol de Decisión Financiero para Inversiones");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
