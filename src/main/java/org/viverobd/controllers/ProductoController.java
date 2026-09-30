package org.viverobd.controllers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.viverobd.dao.ProductoDAO;
import org.viverobd.dao.PlantaDAO;
import org.viverobd.models.Producto;
import org.viverobd.models.Producto.TipoProducto;
import org.viverobd.models.Planta;
import org.viverobd.models.Planta.TipoPlanta;
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

    private final String rutaDB = "./db/viverobd.odb";

    @FXML void initialize(){
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
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(rutaDB);
            EntityManager em = emf.createEntityManager();

            if (em.isOpen()) {
                Producto productoCrear = construirProducto();

                if("Plantas".equalsIgnoreCase(tipoProd.getValue())){
                    Planta nuevaPlanta = construirPlanta();

                    productoCrear.formPro_planta(nuevaPlanta); //Relacionamos el producto con la planta
                    nuevaPlanta.formPla_prod(productoCrear); //Relacionamos la planta con el producto

                    PlantaDAO plantaGenericDAO = new PlantaDAO(em, Planta.class);
                    plantaGenericDAO.create(nuevaPlanta);
                }

                ProductoDAO productoGenericDAO = new ProductoDAO(em, Producto.class);
                productoGenericDAO.create(productoCrear);

                limpiarForm(null);
                em.close();
                emf.close();
            }
        }
    }

    @FXML void buscarProd(ActionEvent event) {
        String criterioSeleccionado = criterio.getValue();
        List<Producto> resultadoBusqueda = null;

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(rutaDB);
        EntityManager em = emf.createEntityManager();
        ProductoDAO productoDAO = new ProductoDAO(em, Producto.class);

        try {
            if (criterioSeleccionado == null) {
                // Caso: Sin criterio - Búsqueda completa
                resultadoBusqueda = productoDAO.readByAttributes(new HashMap<>());
            } else if ("Precio".equalsIgnoreCase(criterioSeleccionado)) {
                // Caso: Búsqueda por rango de precio
                if (validarRangoPrecio()) {
                    double min = Double.parseDouble(precioMin.getText());
                    double max = Double.parseDouble(precioMax.getText());
                    resultadoBusqueda = productoDAO.readByNumberRange("pro_precio", min, max);
                }
            } else if ("Tipo".equalsIgnoreCase(criterioSeleccionado)) {
                // Caso: Búsqueda por tipo de producto
                if (validarTipoBusqueda()) {
                    TipoProducto tipo = TipoProducto.fromNombre(tipoProdBusqueda.getValue());
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("pro_tipo", tipo);
                    resultadoBusqueda = productoDAO.readByAttributes(attrs);
                }
            } else {
                // Caso: Otros criterios (Nombre, Descripcion)
                if (validarParametroBusqueda(criterioSeleccionado)) {
                    Map<String, Object> attrs = new HashMap<>();
                    String campo = "Nombre".equalsIgnoreCase(criterioSeleccionado) ? "prod_nombre" : "pro_descripcion";
                    attrs.put(campo, parametro.getText());
                    resultadoBusqueda = productoDAO.readByAttributes(attrs);
                }
            }

            if (resultadoBusqueda != null) {
                cargarDatosTabla(resultadoBusqueda);
            }
        } finally {
            if (em.isOpen()) em.close();
            emf.close();
        }
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
            Producto productoEliminar = construirProducto();

            if("Plantas".equalsIgnoreCase(tipoProd.getValue())){
                productoEliminar.formPro_planta(construirPlanta());
            }

            EntityManagerFactory emf = Persistence.createEntityManagerFactory(rutaDB);
            EntityManager em = emf.createEntityManager();

            ProductoDAO productoGenericDAO = new ProductoDAO(em, Producto.class);
            productoGenericDAO.delete(productoEliminar);

            limpiarForm(null);
            em.close();
            emf.close();
        }
    }

    @FXML void modificarProd(ActionEvent event) {
        if (camposCorrectos()) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(rutaDB);
            EntityManager em = emf.createEntityManager();

            if (em.isOpen()) {
                Producto productoModificado = construirProducto();

                if("Plantas".equalsIgnoreCase(tipoProd.getValue())){
                    Planta nuevaPlanta = construirPlanta();

                    nuevaPlanta.formPla_prod(productoModificado); //Relacionamos la planta con el producto
                    productoModificado.formPro_planta(nuevaPlanta); //Relacionamos el producto con la planta

                    PlantaDAO plantaGenericDAO = new PlantaDAO(em, Planta.class);
                    plantaGenericDAO.update(nuevaPlanta);
                }

                ProductoDAO productoGenericDAO = new ProductoDAO(em, Producto.class);
                productoGenericDAO.update(productoModificado);

                limpiarForm(null);
                em.close();
                emf.close();
            }
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

        if (nomProd.getText().isEmpty() || !nomProd.getText().matches("[a-zA-Z0-9]+")) {
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de llenado");
            alert.setHeaderText("Por favor corrija los siguientes errores:");
            alert.setContentText(errores.toString());
            alert.showAndWait();
            if (errorNode != null) {
                errorNode.requestFocus();
            }
            return false;
        }

        return true;
    }

    private void configurarTablaBusqueda() {
        // 1. Definir la columna para el Nombre del Producto
        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prod_nombre"));

        // 2. Definir la columna para la Descripción
        TableColumn<Producto, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pro_descripcion"));

        // 3. Definir la columna para el Precio
        TableColumn<Producto, Float> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pro_precio"));

        // 4. Definir la columna para el Tipo de Producto
        TableColumn<Producto, Producto.TipoProducto> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pro_tipo"));

        tablaPro.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Limpiar columnas existentes (si las hay) y añadir las nuevas a la tabla
        tablaPro.getColumns().clear();
        tablaPro.getColumns().addAll(colNombre, colDescripcion, colPrecio, colTipo);

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
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(rutaDB);
            EntityManager em = emf.createEntityManager();
            try {
                Planta planta = em.find(Planta.class, p.getProd_nombre());
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
            } finally {
                em.close();
                emf.close();
            }
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
        if(criterio.getValue() == null){
            parametro.disableProperty().set(true);
            return;
        }
        parametro.disableProperty().set(false);

        if("Precio".equals(criterio.getValue())){
            parametrosNumericos.setVisible(true);
            parametro.setVisible(false);
            tipoProdBusqueda.setVisible(false);
        }
        else if("Tipo".equals(criterio.getValue())){
            tipoProdBusqueda.setVisible(true);
            parametrosNumericos.setVisible(false);
            parametro.setVisible(false);
        }
        else{
            parametro.setVisible(true);
            parametrosNumericos.setVisible(false);
            tipoProdBusqueda.setVisible(false);
        }

        precioMin.clear();
        precioMax.clear();
        parametro.clear();
        tipoProd.getSelectionModel().clearSelection();
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
}
