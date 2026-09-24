package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import jakarta.persistence.*;

@Entity
public class Vivero implements Serializable{
    @Serial private static final long serialVersionUID = 1L;

    @Id private String viv_telefono;
    private String viv_nombre;
    private String viv_direccion;

    @OneToOne
    @JoinColumn(name = "emp_viv", nullable = false)
        private Empleado viv_emp;

    @OneToMany
    @JoinColumn(name = "zona_viv", nullable = false)
        private List<Zona> viv_zona = new ArrayList<>();

    public Vivero() {}

    public Vivero(String viv_telefono, String viv_nombre, String viv_direccion) {
        this.viv_telefono = viv_telefono;
        this.viv_nombre = viv_nombre;
        this.viv_direccion = viv_direccion;
    }

    public String getViv_telefono() { return viv_telefono; }
    public void setViv_telefono(String viv_telefono) { this.viv_telefono = viv_telefono; }

    public String getViv_nombre() { return viv_nombre; }
    public void setViv_nombre(String viv_nombre) { this.viv_nombre = viv_nombre; }

    public String getViv_direccion() { return viv_direccion; }
    public void setViv_direccion(String viv_direccion) { this.viv_direccion = viv_direccion; }

    public Empleado getViv_emp() { return viv_emp; }
    public void formViv_emp(Empleado viv_emp) { this.viv_emp = viv_emp; }
    public void dropViv_emp() { this.viv_emp = null; }

    public List<Zona> getViv_zona() { return viv_zona; }
    public void formViv_zona(Zona viv_zona) { this.viv_zona.add(viv_zona); }
    public void dropViv_zona(Zona viv_zona) { this.viv_zona.remove(viv_zona); }
}
