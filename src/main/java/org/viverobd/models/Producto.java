package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import jakarta.persistence.*;

@Entity
public class Producto implements Serializable {
    @Serial private static final long serialVersionUID=1L;

    public enum TipoProducto {
        tipo_accesorio("Accesorios de jardinería"),
        tipo_decoracion("Decoración para jardín"),
        tipo_planta("Plantas");

        public final String nombre;

        TipoProducto(String nombre){
            this.nombre = nombre;
        }

        public static TipoProducto fromNombre(String nombre) {
            for (TipoProducto tipo : TipoProducto.values()) {
                if (tipo.nombre.equalsIgnoreCase(nombre)) {
                    return tipo;
                }
            }
            return null;
        }
    }

    @Id private String prod_nombre;
    private String pro_descripcion;
    private float pro_precio;
    private TipoProducto pro_tipo;

    @OneToOne
    @JoinColumn(name = "pla_producto", nullable=false)
        private Planta pro_planta;

    @OneToMany
    @JoinColumn(name = "stock_producto", nullable = false)
    private List<Stock> prod_stock = new ArrayList<>();

    public Producto(){}

    public Producto(String nombre, String desc, float precio, TipoProducto tipo){
        this.prod_nombre=nombre;
        this.pro_descripcion=desc;
        this.pro_tipo=tipo;
        this.pro_precio=precio;

    }

    public void setProd_nombre(String prod_nombre) {
        this.prod_nombre = prod_nombre;
    }

    public String getProd_nombre() {
        return prod_nombre;
    }


    public void setPro_descripcion(String pro_descripcion) {
        this.pro_descripcion = pro_descripcion;
    }

    public String getPro_descripcion() {
        return pro_descripcion;
    }


    public void setPro_precio(float pro_precio) {
        this.pro_precio = pro_precio;
    }

    public float getPro_precio() {
        return pro_precio;
    }

    public TipoProducto getPro_tipo(){return pro_tipo;}

    public void setPro_tipo(TipoProducto pro_tipo) {
        this.pro_tipo = pro_tipo;
    }

    public Planta getPro_planta(){return pro_planta;}
    public void formPro_planta(Planta planta){this.pro_planta = planta;}
    public void dropPro_planta(Planta planta){this.pro_planta = planta;}

    public List<Stock> getProd_stock() {return prod_stock;}
    public void formPro_stock(Stock stock){this.prod_stock.add(stock);}
    public void dropPro_stock(Stock stock){this.prod_stock.remove(stock);}
}
