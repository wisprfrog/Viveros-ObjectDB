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
import org.viverobd.models.Empleado;
import org.viverobd.models.Zona;
import org.viverobd.models.ZonaEmpleado;
import org.viverobd.services.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class ZonaEmpleadoController {

    @FXML private TextField codigoZon;
    @FXML private ChoiceBox<String> criterio;
    @FXML private DatePicker fechaAsigZonE;
    @FXML private DatePicker fechaSalEmpE;
    @FXML private TextField horaAsigZonE;
    @FXML private TextField nombreZon;
    @FXML private TextField parametro;
    @FXML private TableView<ZonaEmpleado> tablaZonaE;
    @FXML private TextField telefonoEmp;

    private static EntityManagerFactory emf;
    private ZonaEmpleadoService zonaEmpleadoService;
    private EmpleadoService empleadoService;
    private ZonaService zonaService;

    @FXML
    void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        }
        zonaEmpleadoService = new ZonaEmpleadoServiceImpl(emf);
        empleadoService = new EmpleadoServiceImpl(emf);
        zonaService = new ZonaServiceImpl(emf);

        configurarTabla();

        criterio.getItems().addAll("Código", "Nombre Zona", "Teléfono Empleado");
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);
    }

    private void configurarTabla() {
        TableColumn<ZonaEmpleado, String> codigoZonaET = new TableColumn<>("CÓDIGO");
        codigoZonaET.setCellValueFactory(new PropertyValueFactory<>("zonae_codigo"));

        TableColumn<ZonaEmpleado, String> nombreZonaT = new TableColumn<>("NOMBRE DE LA ZONA");
        nombreZonaT.setCellValueFactory(cellData -> {
            Zona z = cellData.getValue().getZonae_zona();
            return new SimpleStringProperty(z != null ? z.getZona_nombre() : "");
        });

        TableColumn<ZonaEmpleado, String> telefonoEmpT = new TableColumn<>("TELÉFONO DEL EMPLEADO");
        telefonoEmpT.setCellValueFactory(cellData -> {
            Empleado e = cellData.getValue().getZonae_emp();
            return new SimpleStringProperty(e != null ? e.getEmp_telefono() : "");
        });

        TableColumn<ZonaEmpleado, String> fechaAsigZonaET = new TableColumn<>("FECHA DE ASIGNACIÓN");
        fechaAsigZonaET.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getZonae_fecha_asignacion();
            return new SimpleStringProperty(ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "");
        });

        TableColumn<ZonaEmpleado, String> fechaSalidaZonaET = new TableColumn<>("FECHA DE SALIDA");
        fechaSalidaZonaET.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getZonae_fecha_salida();
            return new SimpleStringProperty(ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "");
        });

        TableColumn<ZonaEmpleado, String> horaAsignacionZonET = new TableColumn<>("HORA DE ASIGNACIÓN");
        horaAsignacionZonET.setCellValueFactory(new PropertyValueFactory<>("zonae_hora_asignacion"));

        tablaZonaE.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaZonaE.getColumns().clear();
        tablaZonaE.getColumns().addAll(codigoZonaET, nombreZonaT, telefonoEmpT, fechaAsigZonaET, fechaSalidaZonaET, horaAsignacionZonET);

        tablaZonaE.setRowFactory(tv -> {
            TableRow<ZonaEmpleado> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ZonaEmpleado seleccionado = row.getItem();
                    cargarZonaEmpleadoEnFormulario(seleccionado);
                }
            });
            return row;
        });
    }

    private void cargarZonaEmpleadoEnFormulario(ZonaEmpleado ze) {
        if (ze == null) return;

        codigoZon.setText(ze.getZonae_codigo());

        if (ze.getZonae_zona() != null) {
            nombreZon.setText(ze.getZonae_zona().getZona_nombre());
        }

        if (ze.getZonae_emp() != null) {
            telefonoEmp.setText(ze.getZonae_emp().getEmp_telefono());
        }

        if (ze.getZonae_fecha_asignacion() != null) {
            fechaAsigZonE.setValue(ze.getZonae_fecha_asignacion().toLocalDateTime().toLocalDate());
        } else {
            fechaAsigZonE.setValue(null);
        }

        if (ze.getZonae_fecha_salida() != null) {
            fechaSalEmpE.setValue(ze.getZonae_fecha_salida().toLocalDateTime().toLocalDate());
        } else {
            fechaSalEmpE.setValue(null);
        }

        horaAsigZonE.setText(ze.getZonae_hora_asignacion());
    }

    @FXML
    void agregarZonE(ActionEvent event) {
        if (camposCorrectos()) {
            ZonaEmpleado tempZE = construirZonaEmpleado();
            if (tempZE == null) return;

            ZonaEmpleado existente = zonaEmpleadoService.buscarPorId(tempZE.getZonae_codigo());
            if (existente != null) {
                mostrarAlerta("Error", "Esta asignación ya existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            zonaEmpleadoService.agregarZonaEmpleado(tempZE);
            limpiarForm(null);
        }
    }

    @FXML
    void modificarZonE(ActionEvent event) {
        if (camposCorrectos()) {
            ZonaEmpleado tempZE = construirZonaEmpleado();
            if (tempZE == null) return;

            ZonaEmpleado existente = zonaEmpleadoService.buscarPorId(tempZE.getZonae_codigo());
            if (existente == null) {
                mostrarAlerta("Error", "La asignación no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar actualización", "¿Está seguro de actualizar la asignación seleccionada?")) return;

            zonaEmpleadoService.modificarZonaEmpleado(tempZE);
            limpiarForm(null);
        }
    }

    @FXML
    void eliminarZonE(ActionEvent event) {
        if (camposCorrectos()) {
            ZonaEmpleado tempZE = construirZonaEmpleado();
            if (tempZE == null) return;

            ZonaEmpleado zeAEliminar = zonaEmpleadoService.buscarPorId(tempZE.getZonae_codigo());
            if (zeAEliminar == null) {
                mostrarAlerta("Error", "La asignación no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar eliminación", "¿Está seguro de eliminar la asignación seleccionada?")) return;

            zonaEmpleadoService.eliminarZonaEmpleado(zeAEliminar);
            limpiarForm(null);
        }
    }

    @FXML
    void buscarZonE(ActionEvent event) {
        String crit = criterio.getValue();
        List<ZonaEmpleado> resultados;

        if (crit == null) {
            resultados = zonaEmpleadoService.buscarTodos();
        } else {
            if (validarParametroBusqueda(crit)) {
                Map<String, Object> attrs = new HashMap<>();
                if ("Código".equals(crit)) {
                    attrs.put("zonae_codigo", parametro.getText());
                } else if ("Nombre Zona".equals(crit)) {
                    attrs.put("zonae_zona.zona_nombre", parametro.getText());
                } else {
                    attrs.put("zonae_emp.emp_telefono", parametro.getText());
                }
                resultados = zonaEmpleadoService.buscarPorAtributos(attrs);
            } else {
                return;
            }
        }

        if (resultados != null) cargarDatosTabla(resultados);
    }

    @FXML
    void limpiarForm(ActionEvent event) {
        codigoZon.clear();
        nombreZon.clear();
        telefonoEmp.clear();
        fechaAsigZonE.setValue(null);
        fechaSalEmpE.setValue(null);
        horaAsigZonE.clear();
        parametro.clear();
        criterio.getSelectionModel().clearSelection();
        tablaZonaE.getSelectionModel().clearSelection();
        tablaZonaE.getItems().clear();
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        if (codigoZon.getText().isEmpty() || codigoZon.getText().trim().isEmpty()) {
            errores.append("- El código de la asignación es obligatorio.\n");
            if (errorNode == null) errorNode = codigoZon;
        }

        if (nombreZon.getText().isEmpty()) {
            errores.append("- El nombre de la zona es obligatorio.\n");
            if (errorNode == null) errorNode = nombreZon;
        }

        if (telefonoEmp.getText().isEmpty()) {
            errores.append("- El teléfono del empleado es obligatorio.\n");
            if (errorNode == null) errorNode = telefonoEmp;
        }

        if (fechaAsigZonE.getValue() == null) {
            errores.append("- La fecha de asignación es obligatoria.\n");
            if (errorNode == null) errorNode = fechaAsigZonE;
        }

        if (horaAsigZonE.getText().isEmpty()) {
            errores.append("- La hora de asignación es obligatoria.\n");
            if (errorNode == null) errorNode = horaAsigZonE;
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de llenado", "Por favor corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) errorNode.requestFocus();
            return false;
        }
        return true;
    }

    private ZonaEmpleado construirZonaEmpleado() {
        Empleado emp = empleadoService.buscarPorTelefono(telefonoEmp.getText());
        Zona zon = zonaService.buscarPorId(nombreZon.getText());

        if (emp == null || zon == null) {
            mostrarAlerta("Error", "Empleado o Zona no encontrados en la base de datos.", Alert.AlertType.ERROR);
            return null;
        }

        ZonaEmpleado ze = new ZonaEmpleado();
        ze.setZonae_codigo(codigoZon.getText());
        ze.formZonae_emp(emp);
        ze.formZonae_zona(zon);
        if (fechaAsigZonE.getValue() != null) {
            ze.setZonae_fecha_asignacion(Timestamp.valueOf(fechaAsigZonE.getValue().atStartOfDay()));
        }
        if (fechaSalEmpE.getValue() != null) {
            ze.setZonae_fecha_salida(Timestamp.valueOf(fechaSalEmpE.getValue().atStartOfDay()));
        }
        ze.setZonae_hora_asignacion(horaAsigZonE.getText());

        return ze;
    }

    private boolean validarParametroBusqueda(String criterio) {
        String val = parametro.getText();
        if (val == null || val.trim().isEmpty()) {
            mostrarErrorBusqueda("- El parámetro de búsqueda no puede estar vacío.", parametro);
            return false;
        }
        return true;
    }

    private void mostrarErrorBusqueda(String mensaje, Node node) {
        mostrarAlerta("Error en búsqueda", mensaje, Alert.AlertType.ERROR);
        if (node != null) node.requestFocus();
    }

    private void cargarDatosTabla(List<ZonaEmpleado> asignaciones) {
        tablaZonaE.getItems().clear();
        tablaZonaE.getItems().addAll(asignaciones);
        tablaZonaE.refresh();
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
