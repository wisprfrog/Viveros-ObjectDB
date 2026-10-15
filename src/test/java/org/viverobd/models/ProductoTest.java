package org.viverobd.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void constructorInicializaLosDatosDelProducto() {
        Producto producto = new Producto(
                "Maceta",
                "Maceta de barro",
                125.50f,
                Producto.TipoProducto.tipo_accesorio
        );

        assertAll(
                () -> assertEquals("Maceta", producto.getProd_nombre()),
                () -> assertEquals("Maceta de barro", producto.getPro_descripcion()),
                () -> assertEquals(125.50f, producto.getPro_precio()),
                () -> assertEquals(Producto.TipoProducto.tipo_accesorio, producto.getPro_tipo()),
                () -> assertNull(producto.getPro_planta()),
                () -> assertNotNull(producto.getProd_stock()),
                () -> assertTrue(producto.getProd_stock().isEmpty())
        );
    }

    @Test
    void relacionesDePlantaYStockSePuedenGestionar() {
        Producto producto = new Producto();
        Planta planta = new Planta();
        Stock stock = new Stock();

        producto.formPro_planta(planta);
        producto.formPro_stock(stock);
        assertSame(planta, producto.getPro_planta());
        assertTrue(producto.getProd_stock().contains(stock));

        producto.dropPro_planta();
        producto.dropPro_stock(stock);
        assertNull(producto.getPro_planta());
        assertTrue(producto.getProd_stock().isEmpty());
    }

    @Test
    void tipoProductoSeBuscaPorNombreVisible() {
        assertEquals(
                Producto.TipoProducto.tipo_decoracion,
                Producto.TipoProducto.fromNombre("Decoración para jardín")
        );
        assertNull(Producto.TipoProducto.fromNombre("Tipo inexistente"));
    }
}
