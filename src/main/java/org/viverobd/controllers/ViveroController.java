package org.viverobd.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import jakarta.persistence.Persistence;
import jakarta.persistence.EntityManagerFactory;
import javafx.beans.value.ObservableValue;
import org.viverobd.models.Vivero;
import org.viverobd.services.ViveroService;
import org.viverobd.services.ViveroServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ViveroController {

    @FXML private TextField nombreViv;
    @FXML private TextField telefonoViv;
    @FXML private TextField direccionViv;
    @FXML private TableView<Vivero> tablaViv;
    @FXML private ChoiceBox<String> criterio;
    @FXML private TextField parametro;

    private ViveroService viveroService;
    private static EntityManagerFactory emf;

    @FXML
    public void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        }
        viveroService = new ViveroServiceImpl(emf);

        configurarTabla();

        criterio.getItems().addAll("Nombre", "Teléfono", "Dirección");
        
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);

    }

    private void configurarTabla() {
        TableColumn<Vivero, String> nombreVivT = new TableColumn<>("Nombre");
        nombreVivT.setCellValueFactory(new PropertyValueFactory<>("viv_nombre"));

        TableColumn<Vivero, String> telefonoVivT = new TableColumn<>("Teléfono");
        telefonoVivT.setCellValueFactory(new PropertyValueFactory<>("viv_telefono"));

        TableColumn<Vivero, String> direccionVivT = new TableColumn<>("Dirección");
        direccionVivT.setCellValueFactory(new PropertyValueFactory<>("viv_direccion"));

        tablaViv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaViv.getColumns().clear();
        tablaViv.getColumns().addAll(nombreVivT, telefonoVivT, direccionVivT);

        // Configurar RowFactory para doble clic
        tablaViv.setRowFactory(tv -> {
            TableRow<Vivero> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Vivero seleccionado = row.getItem();
                    cargarViveroEnFormulario(seleccionado);
                }
            });
            return row;
        });
    }

    private void cargarViveroEnFormulario(Vivero v) {
        nombreViv.setText(v.getViv_nombre());
        telefonoViv.setText(v.getViv_telefono());
        direccionViv.setText(v.getViv_direccion());
    }

    @FXML
    void agregarViv(ActionEvent event) {
        if (camposCorrectos()) {
            Vivero nuevo = construirVivero();
            Vivero existente = viveroService.buscarPorId(nuevo.getViv_telefono());
            if (existente != null) {
                mostrarAlerta("Error", "Ya existe un vivero con ese número de teléfono.", Alert.AlertType.ERROR);
                return;
            }

            viveroService.agregarVivero(nuevo);
            limpiarForm(null);
        }
    }

    @FXML
    void modificarViv(ActionEvent event) {
        if (camposCorrectos()) {
            Vivero tempViv = construirVivero();
            Vivero existente = viveroService.buscarPorId(tempViv.getViv_telefono());
            if (existente == null) {
                mostrarAlerta("Error", "El vivero no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar actualización", "¿Está seguro de actualizar el objeto Vivero seleccionado?")) return;

            viveroService.modificarVivero(tempViv);
            limpiarForm(null);
        }
    }

    @FXML
    void eliminarViv(ActionEvent event) {
        if (camposCorrectos()) {
            Vivero tempViv = construirVivero();
            Vivero existente = viveroService.buscarPorId(tempViv.getViv_telefono());
            if (existente == null) {
                mostrarAlerta("Error", "El vivero no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar eliminación", "¿Está seguro de eliminar el objeto Vivero seleccionado?")) return;

            viveroService.eliminarVivero(existente);
            limpiarForm(null);
        }
    }

    @FXML
    void buscarViv(ActionEvent event) {
        String crit = criterio.getValue();
        List<Vivero> resultados;

        if (crit == null) {
            resultados = viveroService.buscarTodos();
        } else {
            if (validarParametroBusqueda()) {
                Map<String, Object> atributos = new HashMap<>();
                if ("Nombre".equals(crit)) atributos.put("viv_nombre", parametro.getText());
                else if ("Teléfono".equals(crit)) atributos.put("viv_telefono", parametro.getText());
                else if ("Dirección".equals(crit)) atributos.put("viv_direccion", parametro.getText());
                resultados = viveroService.buscarPorAtributos(atributos);
            } else {
                return;
            }
        }

        if (resultados != null) cargarDatosTabla(resultados);
    }

    @FXML
    void menuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Interfaces/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el menú principal: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void limpiarForm(ActionEvent event) {
        nombreViv.clear();
        telefonoViv.clear();
        direccionViv.clear();
        parametro.clear();
        criterio.getSelectionModel().clearSelection();
        tablaViv.getSelectionModel().clearSelection();
        tablaViv.getItems().clear();
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        if (nombreViv.getText().isEmpty() || nombreViv.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
            if (errorNode == null) errorNode = nombreViv;
        }
        if (telefonoViv.getText().isEmpty() || !telefonoViv.getText().matches("\\d+")) {
            errores.append("- El teléfono es obligatorio y debe ser numérico.\n");
            if (errorNode == null) errorNode = telefonoViv;
        }
        if (direccionViv.getText().isEmpty() || direccionViv.getText().trim().isEmpty()) {
            errores.append("- La dirección es obligatoria.\n");
            if (errorNode == null) errorNode = direccionViv;
        }

        if (errores.length() > 0) {
            mostrarAlerta("Validación", "Por favor, corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) errorNode.requestFocus();
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private Vivero construirVivero() {
        return new Vivero(telefonoViv.getText(), nombreViv.getText(), direccionViv.getText());
    }

    private boolean validarParametroBusqueda() {
        String val = parametro.getText();
        if (val == null || val.trim().isEmpty()) {
            mostrarErrorBusqueda("- El parámetro de búsqueda no puede estar vacío.", parametro);
            return false;
        }
        return true;
    }

    private void mostrarErrorBusqueda(String mensaje, Node node) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error en búsqueda");
        alert.setHeaderText("Criterios de búsqueda incorrectos");
        alert.setContentText(mensaje);
        alert.showAndWait();
        if (node != null) node.requestFocus();
    }

    private void cargarDatosTabla(List<Vivero> viveros) {
        tablaViv.getItems().clear();
        tablaViv.getItems().addAll(viveros);
        tablaViv.refresh();
    }

    private void listenerCriterioBusqueda(ObservableValue<? extends String> obs, String oldVal, String newVal) {
        if (newVal == null) {
            parametro.setDisable(true);
            parametro.clear();
        } else {
            parametro.setDisable(false);
        }
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
