package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
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
    }

    @Id private String id_producto;
    private String pro_descripcion;
    private float pro_precio;
    private TipoProducto pro_tipo;

    @OneToMany
    @JoinColumn(name = "pla_producto", nullable=false)
        private Set<Planta> pro_planta;

    @OneToMany
    @JoinColumn(name = "stock_producto", nullable = false)
    private Set<Stock> prod_stock;

    public Producto(){}

    public Producto(String id, String desc, float precio, TipoProducto tipo){
        this.id_producto=id;
        this.pro_descripcion=desc;
        this.pro_tipo=tipo;
        this.pro_precio=precio;

    }

    public void setId_producto(String id_producto) {
        this.id_producto = id_producto;
    }

    public String getId_producto() {
        return id_producto;
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

    public Set<Planta> getPro_planta(){return pro_planta;}
    public void formPro_planta(Planta planta){this.pro_planta.add(planta);}
    public void dropPro_planta(Planta planta){this.pro_planta.remove(planta);}

    public Set<Stock> getProd_stock() {return prod_stock;}
    public void formPro_stock(Stock stock){this.prod_stock.add(stock);}
    public void dropPro_stock(Stock stock){this.prod_stock.remove(stock);}
}
