package org.viverobd.controllers;

import org.viverobd.services.ProductoService;
import org.viverobd.services.ProductoServiceImpl;
import org.viverobd.dao.ProductoDAO;
import org.viverobd.models.Producto;
import org.viverobd.models.Producto.TipoProducto;
import org.viverobd.models.Planta;
import org.viverobd.models.Planta.TipoPlanta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.*;

public class ProductoController {
    @FXML private TextField climaPla;
    @FXML private ChoiceBox<String> criterio;
    @FXML private TextField cuidadosPla;
    @FXML private TextField descProd;
    @FXML private TextField humedadPla;
    @FXML private TextField luzPla;
    @FXML private TextField nomProd;
    @FXML private TextField nombrePla;
    @FXML private TextField parametro;
    @FXML private AnchorPane parametrosNumericos;
    @FXML private TextField precioMin;
    @FXML private TextField precioMax;
    @FXML private TextField precioProd;
    @FXML private TableView<Producto> tablaPro;
    @FXML private ChoiceBox<String> tipoPla;
    @FXML private ChoiceBox<String> tipoProd;
    @FXML private ChoiceBox<String> tipoProdBusqueda;

    private static EntityManagerFactory emf;
    private ProductoService productoService;

    @FXML void initialize(){
        if (emf == null) {
            String rutaDB = "./db/viverobd.odb";
            emf = Persistence.createEntityManagerFactory(rutaDB);
        }
        productoService = new ProductoServiceImpl(emf);
        // Cargar los ChoiceBox con los valores de los Enums
        for (TipoProducto tipo : TipoProducto.values()) {
            tipoProd.getItems().add(tipo.nombre);
            tipoProdBusqueda.getItems().add(tipo.nombre);
        }

        for (TipoPlanta tipo : TipoPlanta.values()) {
            tipoPla.getItems().add(tipo.nombre);
        }

        // Habilitar/Deshabilitar campos de planta según el tipo de producto seleccionado
        tipoProd.valueProperty().addListener(this::listenerTipoProducto);

        //Llenar choiceBox criterio
        criterio.getItems().add("Nombre");
        criterio.getItems().add("Descripcion");
        criterio.getItems().add("Tipo");
        criterio.getItems().add("Precio");

        //Habilita/Deshabilitar campos para buscar por precio en rango
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);

        configurarTablaBusqueda();
    }

    @FXML void agregarProd(ActionEvent event) {
        if (camposCorrectos()) {
            Producto tempProd = construirProducto();

            // Comprobamos si el producto ya existe en la base de datos
            Producto productoExistente = productoService.buscarPorId(tempProd.getProd_nombre());
            if (productoExistente != null) {
                mostrarAlerta("Error", "El producto ya existe en la base de datos", Alert.AlertType.ERROR);
                return;
            }

            if("Plantas".equalsIgnoreCase(tipoProd.getValue())){
                Planta nuevaPlanta = construirPlanta();
                tempProd.formPro_planta(nuevaPlanta); //Relacionamos el producto con la planta
                nuevaPlanta.formPla_prod(tempProd); //Relacionamos la planta con el producto
            }

            productoService.agregarProducto(tempProd);
            limpiarForm(null);
        }
    }

    @FXML void buscarProd(ActionEvent event) {
        String criterioSeleccionado = criterio.getValue();
        List<Producto> resultadoBusqueda = null;

        if (criterioSeleccionado == null) {
            // Caso: Sin criterio - Búsqueda completa
            resultadoBusqueda = productoService.buscarTodos();
        } else if ("Precio".equalsIgnoreCase(criterioSeleccionado)) {
            // Caso: Búsqueda por rango de precio
            if (validarRangoPrecio()) {
                double min = Double.parseDouble(precioMin.getText());
                double max = Double.parseDouble(precioMax.getText());
                resultadoBusqueda = productoService.buscarPorRangoPrecio(min, max);
            }
        } else if ("Tipo".equalsIgnoreCase(criterioSeleccionado)) {
            // Caso: Búsqueda por tipo de producto
            if (validarTipoBusqueda()) {
                TipoProducto tipo = TipoProducto.fromNombre(tipoProdBusqueda.getValue());
                Map<String, Object> attrs = new HashMap<>();
                attrs.put("pro_tipo", tipo);
                resultadoBusqueda = productoService.buscarPorAtributos(attrs);
            }
        } else {
            // Caso: Otros criterios (Nombre, Descripcion)
            if (validarParametroBusqueda(criterioSeleccionado)) {
                Map<String, Object> attrs = new HashMap<>();
                String campo = "Nombre".equalsIgnoreCase(criterioSeleccionado) ? "prod_nombre" : "pro_descripcion";
                attrs.put(campo, parametro.getText());
                resultadoBusqueda = productoService.buscarPorAtributos(attrs);
            }
        }

        if (resultadoBusqueda != null) cargarDatosTabla(resultadoBusqueda);
    }

    private boolean validarRangoPrecio() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        String minTxt = precioMin.getText();
        String maxTxt = precioMax.getText();

        if (minTxt.isEmpty() || minTxt.trim().isEmpty()) {
            errores.append("- El precio mínimo es obligatorio.\n");
            if (errorNode == null) errorNode = precioMin;
        } else {
            try {
                double min = Double.parseDouble(minTxt);
                if (min < 0) {
                    errores.append("- El precio mínimo no puede ser negativo.\n");
                    if (errorNode == null) errorNode = precioMin;
                }
            } catch (NumberFormatException e) {
                errores.append("- El precio mínimo debe ser un número válido.\n");
                if (errorNode == null) errorNode = precioMin;
            }
        }

        if (maxTxt.isEmpty() || maxTxt.trim().isEmpty()) {
            errores.append("- El precio máximo es obligatorio.\n");
            if (errorNode == null) errorNode = precioMax;
        } else {
            try {
                double max = Double.parseDouble(maxTxt);
                if (max < 0) {
                    errores.append("- El precio máximo no puede ser negativo.\n");
                    if (errorNode == null) errorNode = precioMax;
                }
            } catch (NumberFormatException e) {
                errores.append("- El precio máximo debe ser un número válido.\n");
                if (errorNode == null) errorNode = precioMax;
            }
        }

        if (errores.isEmpty()) {
            double min = Double.parseDouble(minTxt);
            double max = Double.parseDouble(maxTxt);
            if (min > max) {
                errores.append("- El precio mínimo no puede ser mayor que el máximo.\n");
                if (errorNode == null) errorNode = precioMin;
            }
        }

        if (!errores.isEmpty()) {
            mostrarErrorBusqueda(errores.toString(), errorNode);
            return false;
        }
        return true;
    }

    private boolean validarTipoBusqueda() {
        if (tipoProdBusqueda.getValue() == null) {
            mostrarErrorBusqueda("- Debe seleccionar un tipo de producto para realizar la búsqueda.", tipoProdBusqueda);
            return false;
        }
        return true;
    }

    private boolean validarParametroBusqueda(String criterio) {
        String val = parametro.getText();
        StringBuilder errores = new StringBuilder();

        if ("Nombre".equalsIgnoreCase(criterio)) {
            if (val.isEmpty() || !val.matches("[a-zA-Z0-9]+")) {
                errores.append("- El nombre de búsqueda debe ser alfanumérico y no estar vacío.\n");
            }
        } else if ("Descripcion".equalsIgnoreCase(criterio)) {
            if (val.isEmpty() || val.trim().isEmpty()) {
                errores.append("- La descripción de búsqueda no puede estar vacía.\n");
            }
        }

        if (!errores.isEmpty()) {
            mostrarErrorBusqueda(errores.toString(), parametro);
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

    @FXML void eliminarProd(ActionEvent event) {
        if(camposCorrectos()){
            // Primero se construye el objeto Producto a partir de los campos de entrada
            Producto tempProd = construirProducto();

            // Con su relacion Planta si existe
            if ("Plantas".equalsIgnoreCase(tipoProd.getValue())) {
                Planta tempPlanta = construirPlanta();
                tempProd.formPro_planta(tempPlanta);
                tempPlanta.formPla_prod(tempProd);
            }

            // Se comprueba si ese objeto introducido existe en la bd
            Producto productoAEliminar = productoService.buscarPorId(tempProd.getProd_nombre());

            if (productoAEliminar == null) {
                mostrarAlerta("Error", "El producto no existe en la base de datos", Alert.AlertType.ERROR);
                return;
            }

            String mensajeConfirmacion = "Plantas".equalsIgnoreCase(tipoProd.getValue())
                    ? "Esta seguro de eliminar los objetos Producto y Planta seleccionados?"
                    : "Esta seguro de eliminar el objeto Producto seleccionado?";

            if (!mostrarConfirmacion("Confirmar eliminacion", mensajeConfirmacion)) return;

            productoService.eliminarProducto(productoAEliminar);
            limpiarForm(null);
        }
    }

    @FXML void modificarProd(ActionEvent event) {
        if (camposCorrectos()) {
            Producto tempProd = construirProducto();
            Producto productoExistente = productoService.buscarPorId(tempProd.getProd_nombre());
            if (productoExistente == null) {
                mostrarAlerta("Error", "El producto no existe en la base de datos", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar actualizacion", "Esta seguro de actualizar el objeto Producto seleccionado?")) return;

            if ("Plantas".equalsIgnoreCase(tipoProd.getValue())) {
                Planta nuevaPlanta = construirPlanta();
                tempProd.formPro_planta(nuevaPlanta);
                nuevaPlanta.formPla_prod(tempProd);
            }

            productoService.modificarProducto(tempProd);
            limpiarForm(null);
        }
    }

    @FXML void limpiarForm(ActionEvent event) {
        nomProd.clear();
        descProd.clear();
        precioProd.clear();
        tipoProd.getSelectionModel().clearSelection();
        nombrePla.clear();
        climaPla.clear();
        humedadPla.clear();
        luzPla.clear();
        cuidadosPla.clear();
        tipoPla.getSelectionModel().clearSelection();
        criterio.getSelectionModel().clearSelection();
        parametro.clear();
        precioMin.clear();
        precioMax.clear();
        tipoProdBusqueda.getSelectionModel().clearSelection();
        tablaPro.getItems().clear();
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        javafx.scene.Node errorNode = null;

        if (nomProd.getText().trim().isEmpty() || !nomProd.getText().matches("[a-zA-Z0-9 ]+")) {
            errores.append("- El nombre del producto es obligatorio.\n");
            if (errorNode == null) errorNode = nomProd;
        }

        if (descProd.getText().isEmpty() || descProd.getText().trim().isEmpty()) {
            errores.append("- La descripción del producto es obligatoria.\n");
            if (errorNode == null) errorNode = descProd;
        }

        if (tipoProd.getValue() == null) {
            errores.append("- El tipo de producto debe ser seleccionado.\n");
            if (errorNode == null) errorNode = tipoProd;
        }

        if (precioProd.getText().isEmpty() || precioProd.getText().trim().isEmpty()) {
            errores.append("- El precio es obligatorio.\n");
            if (errorNode == null) errorNode = precioProd;
        } else {
            try {
                float precio = Float.parseFloat(precioProd.getText());
                if (precio < 0) {
                    errores.append("- El precio no puede ser negativo.\n");
                    if (errorNode == null) errorNode = precioProd;
                }
                if (precio == 0.0f) {
                    errores.append("- El precio no puede ser cero.\n");
                    if (errorNode == null) errorNode = precioProd;
                }
            } catch (NumberFormatException e) {
                errores.append("- El precio debe ser un número válido.\n");
                if (errorNode == null) errorNode = precioProd;
            }
        }

        // Validación específica si el tipo de producto seleccionado es planta
        // (Suponiendo que el valor en ChoiceBox puede ser un String o el Enum)
        String tipoSeleccionado = String.valueOf(tipoProd.getValue());
        if ("tipo_planta".equalsIgnoreCase(tipoSeleccionado) || "Plantas".equalsIgnoreCase(tipoSeleccionado)) {
            if (nombrePla.getText().isEmpty() || nombrePla.getText().trim().isEmpty()) {
                errores.append("- El nombre de la planta es obligatorio.\n");
                if (errorNode == null) errorNode = nombrePla;
            }

            if (climaPla.getText().isEmpty() || climaPla.getText().trim().isEmpty()) {
                errores.append("- El clima es obligatorio.\n");
                if (errorNode == null) errorNode = climaPla;
            } else {
                try {
                    Double.parseDouble(climaPla.getText());
                } catch (NumberFormatException e) {
                    errores.append("- El clima debe ser un valor numérico.\n");
                    if (errorNode == null) errorNode = climaPla;
                }
            }

            if (humedadPla.getText().isEmpty() || humedadPla.getText().trim().isEmpty()) {
                errores.append("- La humedad es obligatoria.\n");
                if (errorNode == null) errorNode = humedadPla;
            } else {
                try {
                    Double.parseDouble(humedadPla.getText());
                } catch (NumberFormatException e) {
                    errores.append("- La humedad debe ser un valor numérico.\n");
                    if (errorNode == null) errorNode = humedadPla;
                }
            }

            if (luzPla.getText().isEmpty() || luzPla.getText().trim().isEmpty()) {
                errores.append("- La luz es obligatoria.\n");
                if (errorNode == null) errorNode = luzPla;
            } else {
                try {
                    Double.parseDouble(luzPla.getText());
                } catch (NumberFormatException e) {
                    errores.append("- La luz debe ser un valor numérico.\n");
                    if (errorNode == null) errorNode = luzPla;
                }
            }

            if (tipoPla.getValue() == null) {
                errores.append("- El tipo de planta debe ser seleccionado.\n");
                if (errorNode == null) errorNode = tipoPla;
            }
        }

        if (!errores.isEmpty()) {
            mostrarAlerta("Error de llenado", "Por favor corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) {
                errorNode.requestFocus();
            }
            return false;
        }

        return true;
    }

    private void configurarTablaBusqueda() {
        // Columna para el Nombre del Producto
        TableColumn<Producto, String> colNombre = new TableColumn<>("ID PRODUCTO");
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prod_nombre"));

        // Columna para la Descripción
        TableColumn<Producto, String> colDescripcion = new TableColumn<>("DESCRIPCIÓN");
        colDescripcion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pro_descripcion"));

        // Columna para el Precio
        TableColumn<Producto, Float> colPrecio = new TableColumn<>("PRECIO");
        colPrecio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pro_precio"));

        // Columna para el Tipo de Producto
        TableColumn<Producto, Producto.TipoProducto> colTipo = new TableColumn<>("TIPO DE PRODUCTO");
        colTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pro_tipo"));

        // Columna para el Nombre de la Planta
        TableColumn<Producto, String> colNombrePlanta = new TableColumn<>("NOMBRE DE LA PLANTA");
        colNombrePlanta.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.getPro_planta() != null) {
                return new javafx.beans.property.SimpleStringProperty(p.getPro_planta().getPla_nombre());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Columna para el Tipo de Planta
        TableColumn<Producto, String> colTipoPlanta = new TableColumn<>("TIPO DE PLANTA");
        colTipoPlanta.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.getPro_planta() != null && p.getPro_planta().getPla_tipo() != null) {
                return new javafx.beans.property.SimpleStringProperty(p.getPro_planta().getPla_tipo().nombre);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Columna para la Humedad
        TableColumn<Producto, String> colHumedad = new TableColumn<>("HUMEDAD (%)");
        colHumedad.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.getPro_planta() != null) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(p.getPro_planta().getPla_humedad()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Columna para el Clima
        TableColumn<Producto, String> colClima = new TableColumn<>("CLIMA (°C)");
        colClima.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.getPro_planta() != null) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(p.getPro_planta().getPla_clima()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Columna para la Luz
        TableColumn<Producto, String> colLuz = new TableColumn<>("LUZ");
        colLuz.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.getPro_planta() != null) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(p.getPro_planta().getPla_luz()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        //Columna para los Cuidados
        TableColumn<Producto, String> colCuidados = new TableColumn<>("CUIDADOS");
        colCuidados.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.getPro_planta() != null) {
                return new javafx.beans.property.SimpleStringProperty(p.getPro_planta().getPla_cuidados());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        tablaPro.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Limpiar columnas existentes (si las hay) y añadir las nuevas a la tabla
        tablaPro.getColumns().clear();
        tablaPro.getColumns().addAll(colNombre, colDescripcion, colPrecio, colTipo, colNombrePlanta, colTipoPlanta, colHumedad, colClima, colLuz, colCuidados);

        // Configurar RowFactory para doble clic
        tablaPro.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Producto seleccionado = row.getItem();
                    cargarProductoEnFormulario(seleccionado);
                    // El contador se reinicia automáticamente en el siguiente clic
                }
            });
            return row;
        });
    }

    private void cargarProductoEnFormulario(Producto p) {
        limpiarForm(null);

        if (p == null) return;

        nomProd.setText(p.getProd_nombre());
        descProd.setText(p.getPro_descripcion());
        precioProd.setText(String.valueOf(p.getPro_precio()));
        if (p.getPro_tipo() != null) {
            tipoProd.setValue(p.getPro_tipo().nombre);
        }

        if (p.getPro_tipo() == TipoProducto.tipo_planta) {
            Planta planta = productoService.buscarPlantaPorId(p.getProd_nombre());
            if (planta != null) {
                nombrePla.setText(planta.getPla_nombre());
                climaPla.setText(String.valueOf(planta.getPla_clima()));
                humedadPla.setText(String.valueOf(planta.getPla_humedad()));
                luzPla.setText(String.valueOf(planta.getPla_luz()));
                cuidadosPla.setText(planta.getPla_cuidados());
                if (planta.getPla_tipo() != null) {
                    tipoPla.setValue(planta.getPla_tipo().nombre);
                }
            }
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
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private void listenerTipoProducto(ObservableValue<? extends String> obs, String oldVal, String newVal) {
        boolean esPlanta = "Plantas".equals(newVal);
        nombrePla.setDisable(!esPlanta);
        climaPla.setDisable(!esPlanta);
        humedadPla.setDisable(!esPlanta);
        luzPla.setDisable(!esPlanta);
        cuidadosPla.setDisable(!esPlanta);
        tipoPla.setDisable(!esPlanta);

        if(!esPlanta){
            nombrePla.clear();
            climaPla.clear();
            humedadPla.clear();
            cuidadosPla.clear();
            luzPla.clear();
            tipoPla.setValue(null);
        }
    }

    private void listenerCriterioBusqueda(ObservableValue<? extends String> obs, String oldVal, String newVal) {
        if(newVal == null){
            parametro.disableProperty().set(true);
            parametro.setVisible(true);
            parametrosNumericos.setVisible(false);
            tipoProdBusqueda.setVisible(false);
            parametro.clear();
            precioMin.clear();
            precioMax.clear();
            tipoProdBusqueda.getSelectionModel().clearSelection();
            return;
        }
        parametro.disableProperty().set(false);

        if("Precio".equals(newVal)){
            parametrosNumericos.setVisible(true);
            parametro.setVisible(false);
            tipoProdBusqueda.setVisible(false);
        }
        else if("Tipo".equals(newVal)){
            tipoProdBusqueda.setVisible(true);
            parametrosNumericos.setVisible(false);
            parametro.setVisible(false);
        }
        else if("Nombre".equals(newVal) || "Descripcion".equals(newVal)){
            parametro.setVisible(true);
            parametrosNumericos.setVisible(false);
            tipoProdBusqueda.setVisible(false);
        }

        precioMin.clear();
        precioMax.clear();
        parametro.clear();
        tipoProdBusqueda.getSelectionModel().clearSelection();
    }

    private void cargarDatosTabla(List<Producto> productos) {
        tablaPro.getItems().clear();
        tablaPro.getItems().addAll(productos);
        tablaPro.refresh();
    }

    private Producto construirProducto(){
        Producto producto = new Producto();
        producto.setProd_nombre(nomProd.getText());
        producto.setPro_descripcion(descProd.getText());
        producto.setPro_precio(Float.parseFloat(precioProd.getText()));
        producto.setPro_tipo(TipoProducto.fromNombre(tipoProd.getValue()));

        return producto;
    }

    private Planta construirPlanta(){
        Planta planta = new Planta();
        planta.setProd_nombre(nomProd.getText());
        planta.setPla_nombre(nombrePla.getText());
        planta.setPla_clima(Double.parseDouble(climaPla.getText()));
        planta.setPla_humedad(Double.parseDouble(humedadPla.getText()));
        planta.setPla_luz(Double.parseDouble(luzPla.getText()));
        planta.setPla_cuidados(cuidadosPla.getText());
        planta.setPla_tipo(TipoPlanta.fromNombre(tipoPla.getValue()));

        return planta;
    }


    @FXML
    void menuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Interfaces/MenuPrincipal.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la ventana del menú principal: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
