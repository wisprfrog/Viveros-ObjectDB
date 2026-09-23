package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class ZonaEmpleado implements Serializable{
    @Serial private static final long serialVersionUID = 1L;

    @Id private String zonae_codigo;
    private Timestamp zonae_fecha_asignacion;
    private String zonae_hora_asignacion;

    @ManyToOne
    @JoinColumn(name = "emp_zonae", nullable = false)
        private Empleado zonae_emp;

    @OneToMany
    @JoinColumn(name = "zona_zonae", nullable = false)
        private List<Zona> zonae_zona = new ArrayList<>();

    public ZonaEmpleado() {}

    public ZonaEmpleado(String zonae_codigo, Timestamp zonae_fecha_asignacion, String zonae_hora_asignacion) {
        this.zonae_codigo = zonae_codigo;
        this.zonae_fecha_asignacion = zonae_fecha_asignacion;
        this.zonae_hora_asignacion = zonae_hora_asignacion;
    }

    public String getZonae_codigo() { return zonae_codigo; }
    public void setZonae_codigo(String zonae_codigo) { this.zonae_codigo = zonae_codigo; }

    public Timestamp getZonae_fecha_asignacion() { return zonae_fecha_asignacion; }
    public void setZonae_fecha_asignacion(Timestamp zonae_fecha_asignacion) { this.zonae_fecha_asignacion = zonae_fecha_asignacion; }

    public String getZonae_hora_asignacion() { return zonae_hora_asignacion; }
    public void setZonae_hora_asignacion(String zonae_hora_asignacion) { this.zonae_hora_asignacion = zonae_hora_asignacion; }

    public Empleado getZonae_emp() { return zonae_emp; }
    public void formZonae_emp(Empleado zonae_emp) { this.zonae_emp = zonae_emp; }
    public void dropZonae_emp() { this.zonae_emp = null; }

    public List<Zona> getZonae_zona() { return zonae_zona; }
    public void formZonae_zona(Zona zonae_zona) { this.zonae_zona.add(zonae_zona); }
    public void dropZonae_zona(Zona zonae_zona) { this.zonae_zona.remove(zonae_zona); }
}
