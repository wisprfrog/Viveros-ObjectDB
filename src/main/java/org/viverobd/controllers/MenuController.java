package org.viverobd.controllers;
 
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {
    @FXML
    void empleadosInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/Empleado.fxml");
    }

    @FXML
    void productosInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/Producto.fxml");
    }

    @FXML
    void stockInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/Stock.fxml");
    }

    @FXML
    void viverosInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/Vivero.fxml");
    }

    @FXML
    void zonaeInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/ZonaEmpleado.fxml");
    }

    @FXML
    void zonapInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/ZonaPlanta.fxml");
    }

    @FXML
    void zonasInt(ActionEvent event) {
        cambiarEscena(event, "/Interfaces/Zona.fxml");
    }

    private void cambiarEscena(ActionEvent event, String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la interfaz: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

