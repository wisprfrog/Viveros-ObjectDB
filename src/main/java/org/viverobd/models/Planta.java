package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Planta implements Serializable{
    @Serial private static final long serialVersionUID=1L;

    @Id private String pla_nombre;
    private double pla_clima;
    private double pla_humedad;
    private double pla_luz;
    private String pla_cuidados;

    @ManyToOne
    @JoinColumn(name="pro_planta", nullable = false)
        private Producto pla_producto;

    @OneToMany
    @JoinColumn(name="zonap_pla", nullable = false)
    private List<ZonaPlanta> pla_zonap = new ArrayList<>();

    public Planta(){}

    public Planta(String nom, double clima, double humedad, double luz, String cuidados){
        this.pla_nombre=nom;
        this.pla_clima = clima;
        this.pla_humedad=humedad;
        this.pla_luz=luz;
        this.pla_cuidados=cuidados;
    }

    public void setPla_nombre(String pla_nombre) {
        this.pla_nombre = pla_nombre;
    }
    public String getPla_nombre() {
        return pla_nombre;
    }


    public void setPla_clima(double pla_clima) {
        this.pla_clima = pla_clima;
    }
    public double getPla_clima() {
        return pla_clima;
    }

    public void setPla_humedad(double pla_humedad) {
        this.pla_humedad = pla_humedad;
    }
    public double getPla_humedad() {
        return pla_humedad;
    }


    public void setPla_luz(double pla_luz) {
        this.pla_luz = pla_luz;
    }
    public double getPla_luz() {
        return pla_luz;
    }

    public void setPla_cuidados(String pla_cuidados) {
        this.pla_cuidados = pla_cuidados;
    }

    public String getPla_cuidados() {
        return pla_cuidados;
    }

    public Producto getPla_prod() {
        return pla_producto;
    }
    public void formPla_prod(Producto producto){this.pla_producto = producto;}
    public void dropPla_prod(){this.pla_producto = null;}

    public List<ZonaPlanta> getPla_zonap() { return pla_zonap; }
    public void formPla_zonap(ZonaPlanta zonap) { this.pla_zonap.add(zonap); }
    public void dropPla_zonap(ZonaPlanta zonap) { this.pla_zonap.remove(zonap); }
}
