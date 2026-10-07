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
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.viverobd.models.Empleado;
import org.viverobd.services.EmpleadoService;
import org.viverobd.services.EmpleadoServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmpleadoController {

    @FXML private TextField nombreEmp;
    @FXML private TextField telefonoEmp;
    @FXML private TextField ineEmp;
    @FXML private TextField parametro;
    @FXML private ChoiceBox<String> criterio;
    @FXML private TableView<Empleado> tablaEmp;

    private static EntityManagerFactory emf;
    private EmpleadoService empleadoService;

    @FXML
    public void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        }
        empleadoService = new EmpleadoServiceImpl(emf);
        
        configurarTabla();

        criterio.getItems().addAll("Nombre", "Teléfono", "INE");
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);
    }

    private void configurarTabla() {
        TableColumn<Empleado, String> nombreEmpT = new TableColumn<>("NOMBRE DEL EMPLEADO");
        nombreEmpT.setCellValueFactory(new PropertyValueFactory<>("emp_nombre"));

        TableColumn<Empleado, String> telefonoEmpT = new TableColumn<>("TELÉFONO");
        telefonoEmpT.setCellValueFactory(new PropertyValueFactory<>("emp_telefono"));

        TableColumn<Empleado, String> ineEmpT = new TableColumn<>("INE (CURP)");
        ineEmpT.setCellValueFactory(new PropertyValueFactory<>("emp_ine"));

        tablaEmp.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaEmp.getColumns().clear();
        tablaEmp.getColumns().addAll(nombreEmpT, telefonoEmpT, ineEmpT);

        tablaEmp.setRowFactory(tv -> {
            TableRow<Empleado> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Empleado seleccionado = row.getItem();
                    cargarEmpleadoEnFormulario(seleccionado);
                }
            });
            return row;
        });
    }

    @FXML
    void agregarEmp(ActionEvent event) {
        if (camposCorrectos()) {
            Empleado tempEmp = construirEmpleado();
            if (empleadoService.buscarPorTelefono(tempEmp.getEmp_telefono()) != null) {
                mostrarAlerta("Error", "Ya existe un empleado con ese teléfono.", Alert.AlertType.ERROR);
                return;
            }
            empleadoService.agregarEmpleado(tempEmp);
            limpiarForm(null);
        }
    }

    @FXML
    void buscarEmp(ActionEvent event) {
        String crit = criterio.getValue();
        List<Empleado> resultados;

        if (crit == null) {
            resultados = empleadoService.obtenerTodos();
        } else {
            if (validarParametroBusqueda(crit)) {
                Map<String, Object> atributos = new HashMap<>();
                String param = parametro.getText();
                if ("Nombre".equals(crit)) {
                    atributos.put("emp_nombre", param);
                } else if ("Teléfono".equals(crit)) {
                    atributos.put("emp_telefono", param);
                } else if ("INE".equals(crit)) {
                    atributos.put("emp_ine", param);
                }
                resultados = empleadoService.buscarEmpleados(atributos);
            } else {
                return;
            }
        }

        if (resultados != null) cargarDatosTabla(resultados);
    }

    private void mostrarErrorBusqueda(String mensaje, Node nodo) {
        mostrarAlerta("Error de búsqueda", mensaje, Alert.AlertType.ERROR);
        if (nodo != null) nodo.requestFocus();
    }

    private boolean validarParametroBusqueda(String criterio) {
        String val = parametro.getText();
        StringBuilder errores = new StringBuilder();

        if (val == null || val.trim().isEmpty()) {
            errores.append("- El parámetro de búsqueda no puede estar vacío.\n");
        } else {
            if ("Teléfono".equals(criterio)) {
                if (!val.matches("\\d+")) {
                    errores.append("- El teléfono de búsqueda debe contener solo números.\n");
                }
            } else if ("Nombre".equals(criterio)) {
                if (!val.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                    errores.append("- El nombre de búsqueda debe contener solo letras.\n");
                }
            }
        }

        if (errores.length() > 0) {
            mostrarErrorBusqueda(errores.toString(), parametro);
            return false;
        }
        return true;
    }

    @FXML
    void eliminarEmp(ActionEvent event) {
        if (camposCorrectos()) {
            Empleado tempEmp = construirEmpleado();
            Empleado empleadoAEliminar = empleadoService.buscarPorTelefono(tempEmp.getEmp_telefono());

            if (empleadoAEliminar == null) {
                mostrarAlerta("Error", "El empleado no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar eliminación", "¿Está seguro de que desea eliminar al empleado " + empleadoAEliminar.getEmp_nombre() + "?")) return;

            empleadoService.eliminarEmpleado(empleadoAEliminar);
            limpiarForm(null);
        }
    }

    @FXML
    void modificarEmp(ActionEvent event) {
        if (camposCorrectos()) {
            Empleado tempEmp = construirEmpleado();
            Empleado existente = empleadoService.buscarPorTelefono(tempEmp.getEmp_telefono());
            
            if (existente == null) {
                mostrarAlerta("Error", "El empleado no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar actualización", "¿Está seguro de que desea actualizar al empleado " + tempEmp.getEmp_nombre() + "?")) return;
            
            empleadoService.modificarEmpleado(tempEmp);
            limpiarForm(null);
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

    private void cargarEmpleadoEnFormulario(Empleado e) {
        if (e == null) return;
        nombreEmp.setText(e.getEmp_nombre());
        telefonoEmp.setText(e.getEmp_telefono());
        ineEmp.setText(e.getEmp_ine());
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        if (nombreEmp.getText() == null || nombreEmp.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio.\n");
            if (errorNode == null) errorNode = nombreEmp;
        }

        if (telefonoEmp.getText() == null || telefonoEmp.getText().trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio.\n");
            if (errorNode == null) errorNode = telefonoEmp;
        } else if (!telefonoEmp.getText().matches("\\d+")) {
            errores.append("- El teléfono debe contener solo números.\n");
            if (errorNode == null) errorNode = telefonoEmp;
        }

        if (ineEmp.getText() == null || ineEmp.getText().trim().isEmpty()) {
            errores.append("- El INE/CURP es obligatorio.\n");
            if (errorNode == null) errorNode = ineEmp;
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de llenado", "Por favor corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) errorNode.requestFocus();
            return false;
        }
        
        return true;
    }

    private Empleado construirEmpleado() {
        return new Empleado(telefonoEmp.getText(), nombreEmp.getText(), ineEmp.getText());
    }

    @FXML
    void limpiarForm(ActionEvent event) {
        nombreEmp.clear();
        telefonoEmp.clear();
        ineEmp.clear();
        parametro.clear();
        criterio.getSelectionModel().clearSelection();
        tablaEmp.getSelectionModel().clearSelection();
        tablaEmp.getItems().clear();
    }

    private void cargarDatosTabla(List<Empleado> empleados) {
        tablaEmp.getItems().clear();
        tablaEmp.getItems().addAll(empleados);
        tablaEmp.refresh();
    }

    private void listenerCriterioBusqueda(javafx.beans.value.ObservableValue<? extends String> obs, String oldVal, String newVal) {
        parametro.clear();
        if (newVal == null) {
            parametro.setDisable(true);
        } else {
            parametro.setDisable(false);
            parametro.setPromptText("Ingrese " + newVal);
        }
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
        java.util.Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
