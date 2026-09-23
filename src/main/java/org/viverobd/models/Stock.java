package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*;
import java.util.*;


@Entity
public class Stock implements Serializable{
    @Serial private static final long serialVersionUID = 1L;

    @Id private int stock_id;
    private int stock_cantidad;

    @ManyToOne
    @JoinColumn(name="zona_stock", nullable = false)
        private Zona stock_zona;

    @ManyToOne
    @JoinColumn(name="prod_stock", nullable = false)
        private Producto stock_producto;

    public Stock(){}

    public Stock(int id, int cantidad){
        this.stock_id=id;
        this.stock_cantidad=cantidad;
    }

    public int getStock_id() {
        return stock_id;
    }
    public void setStock_id(int stock_id) {
        this.stock_id = stock_id;
    }

    public int getStock_cantidad() {
        return stock_cantidad;
    }
    public void setStock_cantidad(int stock_cantidad) {
        this.stock_cantidad = stock_cantidad;
    }

    public void setStock_producto(Producto stock_producto) {
        this.stock_producto = stock_producto;
    }

    public Producto getStock_producto() {
        return stock_producto;
    }

    public void formStock_producto(Producto producto) { this.stock_producto = producto; }
    public void dropStock_producto() { this.stock_producto = null; }

    public Zona getStock_zona() { return stock_zona; }
    public void formStock_zona(Zona zona) { this.stock_zona = zona; }
    public void dropStock_zona() { this.stock_zona = null; }
}
