package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class ZonaPlanta implements Serializable{
    @Serial private static final long serialVersionUID = 1L;

    @Id private int zonap_id;
    private double zonap_temperatura;
    private double zonap_humedad;
    private Date zonap_fechaRegistro;
    private Time zonap_horaRegistro;

    @ManyToOne
    @JoinColumn(name = "zona_zonap", nullable = false)
        private Zona zonap_zona;

    @ManyToOne
    @JoinColumn(name = "pla_zonap", nullable = false)
        private Planta zonap_planta;

    public ZonaPlanta(){}

    public ZonaPlanta(int id, double temperatura, double humedad, Date fechaR, Time horaR){
        this.zonap_id = id;
        this.zonap_temperatura = temperatura;
        this.zonap_humedad = humedad;
        this.zonap_fechaRegistro = fechaR;
        this.zonap_horaRegistro = horaR;
    }

    public int getZonap_id() {
        return zonap_id;
    }

    public void setZonap_id(int zonap_id) {
        this.zonap_id = zonap_id;
    }

    public double getZonap_temperatura() {
        return zonap_temperatura;
    }

    public void setZonap_temperatura(double zonap_temperatura) {
        this.zonap_temperatura = zonap_temperatura;
    }

    public double getZonap_humedad() {
        return zonap_humedad;
    }

    public void setZonap_humedad(double zonap_humedad) {
        this.zonap_humedad = zonap_humedad;
    }

    public Date getZonap_fechaRegistro() {
        return zonap_fechaRegistro;
    }

    public void setZonap_fechaRegistro(Date zonap_fechaRegistro) {
        this.zonap_fechaRegistro = zonap_fechaRegistro;
    }

    public Time getZonap_horaRegistro() {
        return zonap_horaRegistro;
    }

    public void setZonap_horaRegistro(Time zonap_horaRegistro) {
        this.zonap_horaRegistro = zonap_horaRegistro;
    }

    public Zona getZonap_zona() {
        return zonap_zona;
    }

    public void setZonap_zona(Zona zonap_zona) {
        this.zonap_zona = zonap_zona;
    }

    public void formZonap_zona(Zona zona) { this.zonap_zona = zona; }
    public void dropZonap_zona() { this.zonap_zona = null; }

    public Planta getZonap_planta() { return zonap_planta; }
    public void formZonap_planta(Planta planta) { this.zonap_planta = planta; }
    public void dropZonap_planta() { this.zonap_planta = null; }
}
