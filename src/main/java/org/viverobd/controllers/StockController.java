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
import org.viverobd.models.Producto;
import org.viverobd.models.Stock;
import org.viverobd.models.Zona;
import org.viverobd.services.StockService;
import org.viverobd.services.StockServiceImpl;

import java.io.IOException;
import java.util.*;

public class StockController {
    @FXML private TextField cantidadSto;
    @FXML private ChoiceBox<String> criterio;
    @FXML private ChoiceBox<String> nomProd2;
    @FXML private ChoiceBox<String> nomZon2;
    @FXML private TextField nombreProd;
    @FXML private TextField nombreZon;
    @FXML private TextField parametro;
    @FXML private TableView<Stock> tablaZonaP;

    private static EntityManagerFactory emf;
    private StockService stockService;
    private List<Zona> listaZonas;
    private List<Producto> listaProductos;

    @FXML
    void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        }
        stockService = new StockServiceImpl(emf);

        configurarTabla();
        cargarChoiceBoxes();

        criterio.getItems().addAll("Zona", "Producto");
        criterio.valueProperty().addListener(this::listenerCriterioBusqueda);

        // Listeners para sincronizar ChoiceBox con TextField de ID
        nomZon2.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) nombreZon.setText(newVal);
        });
        nomProd2.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) nombreProd.setText(newVal);
        });
    }

    private void configurarTabla() {
        TableColumn<Stock, String> nombreZonaT = new TableColumn<>("NOMBRE DE LA ZONA");
        nombreZonaT.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStock_zona().getZona_nombre()));

        TableColumn<Stock, String> idProdT = new TableColumn<>("ID DEL PRODUCTO");
        idProdT.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStock_producto().getProd_nombre()));

        TableColumn<Stock, String> nombreProdT = new TableColumn<>("NOMBRE DEL PRODUCTO");
        nombreProdT.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStock_producto().getProd_nombre()));

        TableColumn<Stock, Integer> existenciaStockT = new TableColumn<>("CANTIDAD EN EXISTENCIA");
        existenciaStockT.setCellValueFactory(new PropertyValueFactory<>("stock_cantidad"));

        tablaZonaP.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaZonaP.getColumns().clear();
        tablaZonaP.getColumns().addAll(nombreZonaT, idProdT, nombreProdT, existenciaStockT);

        tablaZonaP.setRowFactory(tv -> {
            TableRow<Stock> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Stock seleccionado = row.getItem();
                    cargarStockEnFormulario(seleccionado);
                }
            });
            return row;
        });
    }

    private void cargarChoiceBoxes() {
        listaZonas = stockService.obtenerTodasZonas();
        listaProductos = stockService.obtenerTodosProductos();

        nomZon2.getItems().clear();
        for (Zona z : listaZonas) {
            nomZon2.getItems().add(z.getZona_nombre());
        }

        nomProd2.getItems().clear();
        for (Producto p : listaProductos) {
            nomProd2.getItems().add(p.getProd_nombre());
        }
    }

    private void cargarStockEnFormulario(Stock s) {
        nomZon2.setValue(s.getStock_zona().getZona_nombre());
        nomProd2.setValue(s.getStock_producto().getProd_nombre());
        nombreZon.setText(s.getStock_zona().getZona_nombre());
        nombreProd.setText(s.getStock_producto().getProd_nombre());
        cantidadSto.setText(String.valueOf(s.getStock_cantidad()));
    }

    @FXML
    void agregarSto(ActionEvent event) {
        if (camposCorrectos()) {
            Stock tempSto = construirStock();
            if (tempSto == null) return;

            // Verificar si ya existe stock para esa zona y producto
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("stock_zona.zona_nombre", tempSto.getStock_zona().getZona_nombre());
            attrs.put("stock_producto.prod_nombre", tempSto.getStock_producto().getProd_nombre());
            List<Stock> existente = stockService.buscarStock(attrs);

            if (existente != null && !existente.isEmpty()) {
                mostrarAlerta("Error", "Ya existe un registro de stock para este producto en esta zona.", Alert.AlertType.ERROR);
                return;
            }

            stockService.agregarStock(tempSto);
            limpiarForm(null);
        }
    }

    @FXML
    void modificarSto(ActionEvent event) {
        if (camposCorrectos()) {
            Stock tempSto = construirStock();
            if (tempSto == null) return;

            Map<String, Object> attrs = new HashMap<>();
            attrs.put("stock_zona.zona_nombre", tempSto.getStock_zona().getZona_nombre());
            attrs.put("stock_producto.prod_nombre", tempSto.getStock_producto().getProd_nombre());
            List<Stock> existente = stockService.buscarStock(attrs);

            if (existente == null || existente.isEmpty()) {
                mostrarAlerta("Error", "El registro de stock no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            if (!mostrarConfirmacion("Confirmar actualización", "¿Está seguro de actualizar el registro de stock seleccionado?")) return;

            Stock stockAModificar = existente.get(0);
            stockAModificar.setStock_cantidad(tempSto.getStock_cantidad());

            stockService.modificarStock(stockAModificar);
            limpiarForm(null);
        }
    }

    @FXML
    void eliminarSto(ActionEvent event) {
        if (camposCorrectos()) {
            Stock tempSto = construirStock();
            if (tempSto == null) return;

            Map<String, Object> attrs = new HashMap<>();
            attrs.put("stock_zona.zona_nombre", tempSto.getStock_zona().getZona_nombre());
            attrs.put("stock_producto.prod_nombre", tempSto.getStock_producto().getProd_nombre());
            List<Stock> existente = stockService.buscarStock(attrs);

            if (existente == null || existente.isEmpty()) {
                mostrarAlerta("Error", "El registro de stock no existe en la base de datos.", Alert.AlertType.ERROR);
                return;
            }

            Stock stockAEliminar = existente.get(0);

            if (!mostrarConfirmacion("Confirmar eliminación", "¿Está seguro de eliminar el registro de stock seleccionado?")) return;

            stockService.eliminarStock(stockAEliminar);
            limpiarForm(null);
        }
    }

    @FXML
    void buscarSto(ActionEvent event) {
        String crit = criterio.getValue();
        List<Stock> resultados;

        if (crit == null) {
            resultados = stockService.obtenerTodos();
        } else {
            if (validarParametroBusqueda(crit)) {
                Map<String, Object> attrs = new HashMap<>();
                if ("Zona".equals(crit)) {
                    attrs.put("stock_zona.zona_nombre", parametro.getText());
                } else {
                    attrs.put("stock_producto.prod_nombre", parametro.getText());
                }
                resultados = stockService.buscarStock(attrs);
            } else {
                return;
            }
        }

        if (resultados != null) cargarDatosTabla(resultados);
    }

    @FXML
    void limpiarForm(ActionEvent event) {
        nombreZon.clear();
        nombreProd.clear();
        cantidadSto.clear();
        nomZon2.getSelectionModel().clearSelection();
        nomProd2.getSelectionModel().clearSelection();
        parametro.clear();
        criterio.getSelectionModel().clearSelection();
        tablaZonaP.getSelectionModel().clearSelection();
        tablaZonaP.getItems().clear();
    }

    private boolean camposCorrectos() {
        StringBuilder errores = new StringBuilder();
        Node errorNode = null;

        if (nombreZon.getText().isEmpty()) {
            errores.append("- El nombre de la zona es obligatorio.\n");
            if (errorNode == null) errorNode = nombreZon;
        }

        if (nombreProd.getText().isEmpty()) {
            errores.append("- El ID del producto es obligatorio.\n");
            if (errorNode == null) errorNode = nombreProd;
        }

        if (cantidadSto.getText().isEmpty()) {
            errores.append("- La cantidad es obligatoria.\n");
            if (errorNode == null) errorNode = cantidadSto;
        } else {
            try {
                int cant = Integer.parseInt(cantidadSto.getText());
                if (cant < 0) {
                    errores.append("- La cantidad no puede ser negativa.\n");
                    if (errorNode == null) errorNode = cantidadSto;
                }
            } catch (NumberFormatException e) {
                errores.append("- La cantidad debe ser un número entero válido.\n");
                if (errorNode == null) errorNode = cantidadSto;
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de llenado", "Por favor corrija los siguientes errores:\n" + errores.toString(), Alert.AlertType.ERROR);
            if (errorNode != null) errorNode.requestFocus();
            return false;
        }
        return true;
    }

    private Stock construirStock() {
        Zona zona = buscarZona(nombreZon.getText());
        Producto producto = buscarProducto(nombreProd.getText());

        if (zona == null || producto == null) {
            mostrarAlerta("Error", "La zona o el producto no existen.", Alert.AlertType.ERROR);
            return null;
        }

        Stock stock = new Stock();
        // Usar un ID basado en hash o similar si no hay campo en el form,
        // o dejar que el service/DAO maneje la identidad si se prefiere.
        // Aquí mantenemos la lógica de generar uno si es nuevo, pero para buscar usamos zona/producto.
        stock.setStock_id((int) (System.currentTimeMillis() & 0xfffffff));
        stock.setStock_cantidad(Integer.parseInt(cantidadSto.getText()));
        stock.formStock_zona(zona);
        stock.formStock_producto(producto);

        return stock;
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

    private void cargarDatosTabla(List<Stock> stocks) {
        tablaZonaP.getItems().clear();
        tablaZonaP.getItems().addAll(stocks);
        tablaZonaP.refresh();
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

    private Zona buscarZona(String nombre) {
        return listaZonas.stream().filter(z -> z.getZona_nombre().equals(nombre)).findFirst().orElse(null);
    }

    private Producto buscarProducto(String nombre) {
        return listaProductos.stream().filter(p -> p.getProd_nombre().equals(nombre)).findFirst().orElse(null);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
