package org.viverobd.models;

import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.*;
import java.util.*;

@Entity
public class Empleado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id private String emp_telefono;
    private String emp_nombre;
    private String emp_ine;

    @OneToOne
    @JoinColumn(name = "viv_emp", nullable = false)
        private Vivero emp_viv;

    @OneToMany
    @JoinColumn(name = "zonae_emp", nullable = false)
        private List<ZonaEmpleado> emp_zonae = new ArrayList<>();

    public Empleado() {}

    public Empleado(String emp_telefono, String emp_nombre, String emp_ine) {
        this.emp_telefono = emp_telefono;
        this.emp_nombre = emp_nombre;
        this.emp_ine = emp_ine;
    }

    public String getEmp_telefono() { return emp_telefono; }
    public void setEmp_telefono(String emp_telefono) { this.emp_telefono = emp_telefono; }

    public String getEmp_nombre() { return emp_nombre; }
    public void setEmp_nombre(String emp_nombre) { this.emp_nombre = emp_nombre; }

    public String getEmp_ine() { return emp_ine; }
    public void setEmp_ine(String emp_ine) { this.emp_ine = emp_ine; }

    public Vivero getEmp_viv(){ return emp_viv; }
    public void formEmp_viv(Vivero emp_viv) { this.emp_viv = emp_viv; }
    public void dropEmp_viv() { this.emp_viv = null; }

    public List<ZonaEmpleado> getEmp_zonae() { return emp_zonae; }
    public void formEmp_zonae(ZonaEmpleado emp_zonae) { this.emp_zonae.add(emp_zonae); }
    public void dropEmp_zonae(ZonaEmpleado emp_zonae) { this.emp_zonae.remove(emp_zonae); }
}
