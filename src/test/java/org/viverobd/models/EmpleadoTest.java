package org.viverobd.models;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmpleadoTest {

    @Test
    void constructorInicializaLosDatosBasicos() {
        Empleado empleado = new Empleado("5551234567", "Ana López", "LOPA900101");

        assertAll(
                () -> assertEquals("5551234567", empleado.getEmp_telefono()),
                () -> assertEquals("Ana López", empleado.getEmp_nombre()),
                () -> assertEquals("LOPA900101", empleado.getEmp_ine()),
                () -> assertNull(empleado.getEmp_viv()),
                () -> assertNotNull(empleado.getEmp_zonae()),
                () -> assertTrue(empleado.getEmp_zonae().isEmpty())
        );
    }

    @Test
    void constructorVacioPermiteCrearEntidadParaPersistencia() {
        Empleado empleado = new Empleado();

        assertNotNull(empleado);
        assertNotNull(empleado.getEmp_zonae());
        assertTrue(empleado.getEmp_zonae().isEmpty());
    }

    @Test
    void settersActualizanLosAtributosBasicos() {
        Empleado empleado = new Empleado();

        empleado.setEmp_telefono("5559876543");
        empleado.setEmp_nombre("Carlos Méndez");
        empleado.setEmp_ine("MECA880202");

        assertEquals("5559876543", empleado.getEmp_telefono());
        assertEquals("Carlos Méndez", empleado.getEmp_nombre());
        assertEquals("MECA880202", empleado.getEmp_ine());
    }

    @Test
    void formYDropEmpleadoViveroGestionanLaRelacionOpcional() {
        Empleado empleado = new Empleado();
        Vivero vivero = new Vivero("5550000000", "Vivero Central", "Av. Principal 10");

        empleado.formEmp_viv(vivero);
        assertSame(vivero, empleado.getEmp_viv());

        empleado.dropEmp_viv();
        assertNull(empleado.getEmp_viv());
    }

    @Test
    void formYDropZonaEmpleadoGestionanLaColeccionDeAsignaciones() {
        Empleado empleado = new Empleado();
        ZonaEmpleado asignacion = new ZonaEmpleado(
                "ZE-001",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 10, 8, 0)),
                null,
                "08:00"
        );

        empleado.formEmp_zonae(asignacion);
        assertEquals(1, empleado.getEmp_zonae().size());
        assertSame(asignacion, empleado.getEmp_zonae().get(0));

        empleado.dropEmp_zonae(asignacion);
        assertTrue(empleado.getEmp_zonae().isEmpty());
    }

    @Test
    void relacionesDeDistintasEntidadesSeMantienenIndependientes() {
        Empleado primero = new Empleado("5551111111", "Primero", "PRIM900101");
        Empleado segundo = new Empleado("5552222222", "Segundo", "SEGU900202");
        ZonaEmpleado asignacion = new ZonaEmpleado();

        primero.formEmp_zonae(asignacion);

        assertTrue(primero.getEmp_zonae().contains(asignacion));
        assertTrue(segundo.getEmp_zonae().isEmpty());
        assertNotSame(primero.getEmp_zonae(), segundo.getEmp_zonae());
    }
}
