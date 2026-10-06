package org.viverobd.controllers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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
import org.viverobd.dao.PlantaDAO;
import org.viverobd.models.Planta;
import org.viverobd.models.Zona;
import org.viverobd.models.ZonaPlanta;
import org.viverobd.services.*;

import java.io.IOException;
import java.sql.Time;
import java.util.*;

public class ZonaPlantaController {

    @FXML
    private ChoiceBox<String> criterio;
    @FXML
    private DatePicker fechaRegZonP;
    @FXML
    private TextField horaRegZonP;
    @FXML
    private TextField humedadZonP;
    @FXML
    private ChoiceBox<String> nomPla2;
    @FXML
    private ChoiceBox<String> nomZona2;
    @FXML
    private TextField nombrePlan;
    @FXML
    private TextField nombreZon;
    @FXML
    private TextField parametro;
    @FXML
    private TableView<ZonaPlanta> tablaZonaP;
    @FXML
    private TextField temperaturaZonP;

    private static EntityManagerFactory emf;
    private ZonaPlantaService zonaPlantaService;
    private ZonaService zonaService;
    private List<Zona> listaZonas;
    private List<Planta> listaPlantas;
    private int currentId = -1;

    @FXML
    void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        }
        zonaPlantaService = new ZonaPlantaServiceImpl(emf);
        zonaService = new ZonaServiceImpl(emf);

        configurarTabla();
        cargarChoiceBoxes();

        criterio.getItems().addAll("Zona", "Planta", "Temperatura", "Humedad");
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);

        // Listeners para sincronizar ChoiceBox con TextField
        nomZona2.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) nombreZon.setText(newVal);
        });
        nomPla2.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) nombrePlan.setText(newVal);
        });
    }

    private void configurarTabla() {
        TableColumn<ZonaPlanta, String> nombreZonaT = new TableColumn<>("NOMBRE DE LA ZONA");
        nombreZonaT.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getZonap_zona().getZona_nombre()));

        TableColumn<ZonaPlanta, String> nombrePlaT = new TableColumn<>("NOMBRE DE LA PLANTA");
        nombrePlaT.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getZonap_planta().getPla_nombre()));

        TableColumn<ZonaPlanta, Double> temperaturaZonPT = new TableColumn<>("TEMPERATURA (°C)");
        temperaturaZonPT.setCellValueFactory(new PropertyValueFactory<>("zonap_temperatura"));

        TableColumn<ZonaPlanta, Double> humendadZonPT = new TableColumn<>("HUMEDAD (%)");
        humendadZonPT.setCellValueFactory(new PropertyValueFactory<>("zonap_humedad"));

        TableColumn<ZonaPlanta, String> fechaRegZonPT = new TableColumn<>("FECHA DE REGISTRO");
        fechaRegZonPT.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getZonap_fechaRegistro().toString()));

        TableColumn<ZonaPlanta, String> horaRegZonPT = new TableColumn<>("HORA DE REGISTRO");
        horaRegZonPT.setCellValueFactory(new PropertyValueFactory<>("zonap_horaRegistro"));

        tablaZonaP.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaZonaP.getColumns().clear();
        tablaZonaP.getColumns().addAll(nombreZonaT, nombrePlaT, temperaturaZonPT, humendadZonPT, fechaRegZonPT, horaRegZonPT);

        tablaZonaP.setRowFactory(tv -> {
            TableRow<ZonaPlanta> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ZonaPlanta seleccionado = row.getItem();
                    cargarZonaPlantaEnFormulario(seleccionado);
                }
            });
            return row;
        });
    }

    private void cargarChoiceBoxes() {
        listaZonas = zonaService.buscarTodas();
        nomZona2.getItems().clear();
        for (Zona z : listaZonas) {
            nomZona2.getItems().add(z.getZona_nombre());
        }

        EntityManager em = emf.createEntityManager();
        try {
            PlantaDAO plantaDAO = new PlantaDAO(em, Planta.class);
            listaPlantas = plantaDAO.readByAttributes(new HashMap<>());
            nomPla2.getItems().clear();
            for (Planta p : listaPlantas) {
                nomPla2.getItems().add(p.getPla_nombre());
            }
        } finally {
            em.close();
        }
    }

    private void cargarZonaPlantaEnFormulario(ZonaPlanta zp) {
        if (zp == null) return;
        currentId = zp.getZonap_id();
        nombreZon.setText(zp.getZonap_zona().getZona_nombre());
        nombrePlan.setText(zp.getZonap_planta().getPla_nombre());
        temperaturaZonP.setText(String.valueOf(zp.getZonap_temperatura()));
        humedadZonP.setText(String.valueOf(zp.getZonap_humedad()));
        if (zp.getZonap_fechaRegistro() != null) {
            java.util.Date utilDate = zp.getZonap_fechaRegistro();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            fechaRegZonP.setValue(sqlDate.toLocalDate());
        }
        if (zp.getZonap_horaRegistro() != null) {
            horaRegZonP.setText(zp.getZonap_horaRegistro().toString());
        }
    }

    @FXML
    void agregarZonP(ActionEvent event) {
        if (camposCorrectos()) {
            ZonaPlanta tempZP = construirZonaPlanta();
            if (tempZP == null) return;

            // Al ser ID generado, no verificamos por ID manual,
            // pero podríamos verificar si ya existe un registro idéntico en ese instante.
            tempZP.setZonap_id(new Random().nextInt(1000000));
            zonaPlantaService.registrarZonaPlanta(tempZP);
            limpiarForm(null);
        }
    }

    @FXML
    void modificarZonP(ActionEvent event) {
        if (camposCorrectos()) {
            if (currentId == -1) {
                mostrarAlerta("Error", "Seleccione un registro de la tabla para modificar.", Alert.AlertType.ERROR);
                return;
            }

            ZonaPlanta tempZP = construirZonaPlanta();
            if (tempZP == null) return;
            tempZP.setZonap_id(currentId);

            if (!mostrarConfirmacion("Confirmar actualización", "¿Está seguro de actualizar el registro seleccionado?"))
                return;

            zonaPlantaService.actualizarZonaPlanta(tempZP);
            limpiarForm(null);
        }
    }

    @FXML
    void eliminarZonP(ActionEvent event) {
        if (currentId == -1) {
            mostrarAlerta("Error", "Seleccione un registro de la tabla para eliminar.", Alert.AlertType.ERROR);
            return;
        }

        if (!mostrarConfirmacion("Confirmar eliminación", "¿Está seguro de eliminar el registro seleccionado?")) return;

        ZonaPlanta zeAEliminar = new ZonaPlanta();
        zeAEliminar.setZonap_id(currentId);

        zonaPlantaService.eliminarZonaPlanta(zeAEliminar);
        limpiarForm(null);
    }

    @FXML
    void buscarZonP(ActionEvent event) {
        String crit = criterio.getValue();
        List<ZonaPlanta> resultados;

        if (crit == null) {
            resultados = zonaPlantaService.listarTodos();
        } else {
            if (validarParametroBusqueda(crit)) {
                Map<String, Object> attrs = new HashMap<>();
                String param = parametro.getText();
                switch (crit) {
                    case "Zona":
                        attrs.put("zonap_zona.zona_nombre", param);
                        break;
                    case "Planta":
                        attrs.put("zonap_planta.pla_nombre", param);
                        break;
                    case "Temperatura":
                        attrs.put("zonap_temperatura", Double.parseDouble(param));
                        break;
                    case "Humedad":
                        attrs.put("zonap_humedad", Double.parseDouble(param));
                        break;
                }
                resultados = zonaPlantaService.buscarPorAtributos(attrs);
            } else {
                return;
            }
        }

        if (resultados != null) cargarDatosTabla(resultados);
    }

    @FXML
    void limpiarForm(ActionEvent event) {
        nombreZon.clear();
        nombrePlan.clear();
        temperaturaZonP.clear();
        humedadZonP.clear();
        fechaRegZonP.setValue(null);
        horaRegZonP.clear();
        parametro.clear();
        criterio.getSelectionModel().clearSelection();
        tablaZonaP.getSelectionModel().clearSelection();
        tablaZonaP.getItems().clear();
        currentId = -1;
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        if (nombreZon.getText().isEmpty()) {
            errores.append("- El nombre de la zona es obligatorio.\n");
            if (errorNode == null) errorNode = nombreZon;
        }

        if (nombrePlan.getText().isEmpty()) {
            errores.append("- El nombre de la planta es obligatorio.\n");
            if (errorNode == null) errorNode = nombrePlan;
        }

        if (temperaturaZonP.getText().isEmpty()) {
            errores.append("- La temperatura es obligatoria.\n");
            if (errorNode == null) errorNode = temperaturaZonP;
        } else {
            try {
                Double.parseDouble(temperaturaZonP.getText());
            } catch (NumberFormatException e) {
                errores.append("- La temperatura debe ser un número.\n");
                if (errorNode == null) errorNode = temperaturaZonP;
            }
        }

        if (humedadZonP.getText().isEmpty()) {
            errores.append("- La humedad es obligatoria.\n");
            if (errorNode == null) errorNode = humedadZonP;
        } else {
            try {
                Double.parseDouble(humedadZonP.getText());
            } catch (NumberFormatException e) {
                errores.append("- La humedad debe ser un número.\n");
                if (errorNode == null) errorNode = humedadZonP;
            }
        }

        if (fechaRegZonP.getValue() == null) {
            errores.append("- La fecha de registro es obligatoria.\n");
            if (errorNode == null) errorNode = fechaRegZonP;
        }

        if (horaRegZonP.getText().isEmpty()) {
            errores.append("- La hora de registro es obligatoria.\n");
            if (errorNode == null) errorNode = horaRegZonP;
        } else {
            try {
                Time.valueOf(horaRegZonP.getText());
            } catch (IllegalArgumentException e) {
                errores.append("- La hora debe tener el formato HH:mm:ss.\n");
                if (errorNode == null) errorNode = horaRegZonP;
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de llenado", "Por favor corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) errorNode.requestFocus();
            return false;
        }
        return true;
    }

    private ZonaPlanta construirZonaPlanta() {
        Zona zona = zonaService.buscarPorId(nombreZon.getText());
        Planta planta = null;
        if (listaPlantas != null) {
            planta = listaPlantas.stream().filter(p -> p.getPla_nombre().equals(nombrePlan.getText())).findFirst().orElse(null);
        }

        if (zona == null || planta == null) {
            mostrarAlerta("Error", "Zona o Planta no encontradas en la base de datos.", Alert.AlertType.ERROR);
            return null;
        }

        double temp = Double.parseDouble(temperaturaZonP.getText());
        double hum = Double.parseDouble(humedadZonP.getText());
        java.sql.Date date = java.sql.Date.valueOf(fechaRegZonP.getValue());
        Time time = Time.valueOf(horaRegZonP.getText());

        ZonaPlanta zp = new ZonaPlanta(0, temp, hum, date, time);
        zp.formZonap_zona(zona);
        zp.formZonap_planta(planta);

        return zp;
    }

    private boolean validarParametroBusqueda(String criterio) {
        String val = parametro.getText();
        if (val == null || val.trim().isEmpty()) {
            mostrarErrorBusqueda("- El parámetro de búsqueda no puede estar vacío.", parametro);
            return false;
        }
        if ("Temperatura".equals(criterio) || "Humedad".equals(criterio)) {
            try {
                Double.parseDouble(val);
            } catch (NumberFormatException e) {
                mostrarErrorBusqueda("- Debe ingresar un número válido.", parametro);
                return false;
            }
        }
        return true;
    }

    private void mostrarErrorBusqueda(String mensaje, Node node) {
        mostrarAlerta("Error en búsqueda", mensaje, Alert.AlertType.ERROR);
        if (node != null) node.requestFocus();
    }

    private void cargarDatosTabla(List<ZonaPlanta> registros) {
        tablaZonaP.getItems().clear();
        tablaZonaP.getItems().addAll(registros);
        tablaZonaP.refresh();
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
