package org.viverobd.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Test
    void constructorInicializaIdentificadorYCantidad() {
        Stock stock = new Stock(7, 25);

        assertEquals(7, stock.getStock_id());
        assertEquals(25, stock.getStock_cantidad());
        assertNull(stock.getStock_zona());
        assertNull(stock.getStock_producto());
    }

    @Test
    void settersActualizanIdentificadorYCantidad() {
        Stock stock = new Stock();

        stock.setStock_id(12);
        stock.setStock_cantidad(40);

        assertEquals(12, stock.getStock_id());
        assertEquals(40, stock.getStock_cantidad());
    }

    @Test
    void relacionesConZonaYProductoSePuedenGestionar() {
        Stock stock = new Stock();
        Zona zona = new Zona();
        Producto producto = new Producto();

        stock.formStock_zona(zona);
        stock.formStock_producto(producto);
        assertSame(zona, stock.getStock_zona());
        assertSame(producto, stock.getStock_producto());

        stock.dropStock_zona();
        stock.dropStock_producto();
        assertNull(stock.getStock_zona());
        assertNull(stock.getStock_producto());
    }
}
