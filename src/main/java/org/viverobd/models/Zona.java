package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
public class Zona implements Serializable{
    @Serial private static final long serialVersionUID = 1L;

    public enum TipoZona {
        zona_siembra("Zona de Siembra"),
        zona_crecimiento("Zona de Crecimiento"),
        zona_aclimatacion("Zona de Aclimatacion"),
        zona_preparacion_sustrato("Zona de Preparacion de Sustrato"),
        zona_almacenamiento("Zona de Almacenamiento"),
        oficina_administrativa("Oficina Administrativa");

        public final String nombre;

        TipoZona(String nombre){
            this.nombre = nombre;
        }
    }

    @Id private String zona_nombre;
    private float zona_superficie;
    private TipoZona zona_tipo;

    @ManyToOne
    @JoinColumn(name = "zonae_zona", nullable = false)
        private ZonaEmpleado zona_zonae;

    @ManyToOne
    @JoinColumn(name = "viv_zona", nullable = false)
        private Vivero zona_viv;

    public Zona() {}

    public Zona(String zona_nombre, float zona_superficie, TipoZona zona_tipo) {
        this.zona_nombre = zona_nombre;
        this.zona_superficie = zona_superficie;
        this.zona_tipo = zona_tipo;
    }

    public String getZona_nombre() { return zona_nombre; }
    public void setZona_nombre(String zona_nombre) { this.zona_nombre = zona_nombre; }

    public float getZona_superficie() { return zona_superficie; }
    public void setZona_superficie(float zona_superficie) { this.zona_superficie = zona_superficie; }

    public TipoZona getZona_tipo() { return zona_tipo; }
    public void setZona_tipo(TipoZona zona_tipo) { this.zona_tipo = zona_tipo; }

    public ZonaEmpleado getZona_zonae() { return zona_zonae; }
    public void formZona_zonae(ZonaEmpleado zona_zonae) { this.zona_zonae = zona_zonae; }
    public void dropZona_zonae() { this.zona_zonae = null; }

    public Vivero getZona_viv() { return zona_viv; }
    public void formZona_viv(Vivero zona_viv) { this.zona_viv = zona_viv; }
    public void dropZona_viv() { this.zona_viv = null; }

    //TODO: Agregar relacion con ZonaPlantas
    @OneToMany
    @JoinColumn(name="zonap_zona", nullable = false)
    private Set<ZonaPlanta> zona_zonap;

    public Set<ZonaPlanta> getZona_zonap() {return zona_zonap;}
    public void formZona_zonap(ZonaPlanta zonap){this.zona_zonap.add(zonap);}
    public void dropZona_zonap(ZonaPlanta zonap){this.zona_zonap.remove(zonap);}

    //TODO: Agregar relacion con StockProductos
    @OneToMany
    @JoinColumn(name="stock_zona", nullable = false)
        private Set<Stock> zona_stock;

    public Set<Stock> getZona_stock() {return zona_stock;}
    public void formZona_stock(Stock stock){this.zona_stock.add(stock);}
    public void dropZona_stock(Stock stock){this.zona_stock.remove(stock);}
}
