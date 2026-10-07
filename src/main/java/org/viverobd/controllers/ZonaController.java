package org.viverobd.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.viverobd.models.Vivero;
import org.viverobd.models.Zona;
import org.viverobd.models.Zona.TipoZona;
import org.viverobd.services.ZonaService;
import org.viverobd.services.ZonaServiceImpl;

import java.io.IOException;
import java.util.*;

public class ZonaController {
    @FXML private ChoiceBox<String> criterio;
    @FXML private TextField nombreZon;
    @FXML private TextField parametro;
    @FXML private TableView<Zona> tablaZon;
    @FXML private TextField telefonoZon; // El FXML lo usa para SUPERFICIE
    @FXML private ChoiceBox<String> tipoZon;

    private static EntityManagerFactory emf;
    private ZonaService zonaService;
    private List<Vivero> listaViveros;

    @FXML
    void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        }
        zonaService = new ZonaServiceImpl(emf);

        configurarTabla();
        cargarChoiceBoxes();

        criterio.getItems().addAll("Nombre", "Superficie", "Tipo");
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);
    }

    private void configurarTabla() {
        TableColumn<Zona, String> colNombre = new TableColumn<>("NOMBRE ZONA");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("zona_nombre"));

        TableColumn<Zona, String> colSuperficie = new TableColumn<>("SUPERFICIE");
        colSuperficie.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getZona_superficie())));

        TableColumn<Zona, String> colTipo = new TableColumn<>("TIPO");
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getZona_tipo().nombre));

        tablaZon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaZon.getColumns().clear();
        tablaZon.getColumns().addAll(colNombre, colSuperficie, colTipo);

        tablaZon.setRowFactory(tv -> {
            TableRow<Zona> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Zona seleccionado = row.getItem();
                    cargarZonaEnFormulario(seleccionado);
                }
            });
            return row;
        });
    }

    private void cargarChoiceBoxes() {
        tipoZon.getItems().clear();
        for (TipoZona tipo : TipoZona.values()) {
            tipoZon.getItems().add(tipo.nombre);
        }
        
        listaViveros = zonaService.obtenerTodosViveros();
    }

    private void cargarZonaEnFormulario(Zona z) {
        if (z == null) return;
        nombreZon.setText(z.getZona_nombre());
        telefonoZon.setText(String.valueOf(z.getZona_superficie()));
        tipoZon.setValue(z.getZona_tipo().nombre);
    }

    @FXML
    void agregarZon(ActionEvent event) {
        if (camposCorrectos()) {
            Zona tempZon = construirZona();
            Zona existente = zonaService.buscarPorId(tempZon.getZona_nombre());
            if (existente != null) {
                mostrarAlerta("Error", "La zona ya existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (listaViveros.isEmpty()) {
                mostrarAlerta("Error", "No hay viveros disponibles para asignar a la zona.", Alert.AlertType.ERROR);
                return;
            }
            // Asignar el primer vivero por defecto si no hay selector en FXML
            tempZon.formZona_viv(listaViveros.get(0));

            zonaService.agregarZona(tempZon);
            limpiarForm(null);
        }
    }

    @FXML
    void modificarZon(ActionEvent event) {
        if (camposCorrectos()) {
            Zona tempZon = construirZona();
            Zona existente = zonaService.buscarPorId(tempZon.getZona_nombre());
            if (existente == null) {
                mostrarAlerta("Error", "La zona no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar actualización", "¿Está seguro de actualizar el objeto Zona seleccionado?")) return;

            tempZon.formZona_viv(existente.getZona_viv());
            zonaService.modificarZona(tempZon);
            limpiarForm(null);
        }
    }

    @FXML
    void eliminarZon(ActionEvent event) {
        if (camposCorrectos()) {
            Zona tempZon = construirZona();
            Zona zonaAEliminar = zonaService.buscarPorId(tempZon.getZona_nombre());

            if (zonaAEliminar == null) {
                mostrarAlerta("Error", "La zona no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar eliminación", "¿Está seguro de eliminar el objeto Zona seleccionado?")) return;

            try {
                zonaService.eliminarZona(zonaAEliminar);
                limpiarForm(null);
            } catch (Exception e) {
                mostrarAlerta("Error de Integridad", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void buscarZon(ActionEvent event) {
        String crit = criterio.getValue();
        List<Zona> resultados;

        if (crit == null) {
            resultados = zonaService.buscarTodas();
        } else {
            if (validarParametroBusqueda(crit)) {
                Map<String, Object> attrs = new HashMap<>();
                if ("Nombre".equals(crit)) {
                    attrs.put("zona_nombre", parametro.getText());
                } else if ("Superficie".equals(crit)) {
                    attrs.put("zona_superficie", Float.parseFloat(parametro.getText()));
                } else if ("Tipo".equals(crit)) {
                    for (TipoZona t : TipoZona.values()) {
                        if (t.nombre.equalsIgnoreCase(parametro.getText())) {
                            attrs.put("zona_tipo", t);
                            break;
                        }
                    }
                }
                resultados = zonaService.buscarPorAtributos(attrs);
            } else {
                return;
            }
        }

        if (resultados != null) cargarDatosTabla(resultados);
    }

    @FXML
    void limpiarForm(ActionEvent event) {
        nombreZon.clear();
        telefonoZon.clear();
        tipoZon.getSelectionModel().clearSelection();
        parametro.clear();
        criterio.getSelectionModel().clearSelection();
        tablaZon.getSelectionModel().clearSelection();
        tablaZon.getItems().clear();
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        if (nombreZon.getText().isEmpty()) {
            errores.append("- El nombre de la zona es obligatorio.\n");
            if (errorNode == null) errorNode = nombreZon;
        }

        if (telefonoZon.getText().isEmpty()) {
            errores.append("- La superficie es obligatoria.\n");
            if (errorNode == null) errorNode = telefonoZon;
        } else {
            try {
                float sup = Float.parseFloat(telefonoZon.getText());
                if (sup <= 0) {
                    errores.append("- La superficie debe ser mayor a cero.\n");
                    if (errorNode == null) errorNode = telefonoZon;
                }
            } catch (NumberFormatException e) {
                errores.append("- La superficie debe ser un número válido.\n");
                if (errorNode == null) errorNode = telefonoZon;
            }
        }

        if (tipoZon.getValue() == null) {
            errores.append("- El tipo de zona es obligatorio.\n");
            if (errorNode == null) errorNode = tipoZon;
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de llenado", "Por favor corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) errorNode.requestFocus();
            return false;
        }
        return true;
    }

    private Zona construirZona() {
        String nombre = nombreZon.getText();
        float superficie = Float.parseFloat(telefonoZon.getText());
        TipoZona tipo = null;
        for (TipoZona t : TipoZona.values()) {
            if (t.nombre.equals(tipoZon.getValue())) {
                tipo = t;
                break;
            }
        }
        return new Zona(nombre, superficie, tipo);
    }

    private boolean validarParametroBusqueda(String criterio) {
        String val = parametro.getText();
        if (val == null || val.trim().isEmpty()) {
            mostrarErrorBusqueda("- El parámetro de búsqueda no puede estar vacío.", parametro);
            return false;
        }
        if ("Superficie".equals(criterio)) {
            try {
                Float.parseFloat(val);
            } catch (NumberFormatException e) {
                mostrarErrorBusqueda("- La superficie debe ser un número válido.", parametro);
                return false;
            }
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

    private void cargarDatosTabla(List<Zona> zonas) {
        tablaZon.getItems().clear();
        tablaZon.getItems().addAll(zonas);
        tablaZon.refresh();
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

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
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

    private void listenerCriterioBusqueda(ObservableValue<? extends String> obs, String oldVal, String newVal) {
        parametro.clear();
        if (newVal == null) {
            parametro.setDisable(true);
        } else {
            parametro.setDisable(false);
            parametro.setPromptText("Ingrese " + newVal);
        }
    }
}
