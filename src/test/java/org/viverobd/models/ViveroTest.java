package org.viverobd.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViveroTest {

    @Test
    void constructorInicializaLosDatosBasicos() {
        Vivero vivero = new Vivero("5550000000", "Vivero Central", "Av. Principal 10");

        assertAll(
                () -> assertEquals("5550000000", vivero.getViv_telefono()),
                () -> assertEquals("Vivero Central", vivero.getViv_nombre()),
                () -> assertEquals("Av. Principal 10", vivero.getViv_direccion()),
                () -> assertNull(vivero.getViv_emp()),
                () -> assertNotNull(vivero.getViv_zona()),
                () -> assertTrue(vivero.getViv_zona().isEmpty())
        );
    }

    @Test
    void relacionesDeEmpleadoYZonaSePuedenAgregarYEliminar() {
        Vivero vivero = new Vivero();
        Empleado empleado = new Empleado();
        Zona zona = new Zona();

        vivero.formViv_emp(empleado);
        vivero.formViv_zona(zona);
        assertSame(empleado, vivero.getViv_emp());
        assertTrue(vivero.getViv_zona().contains(zona));

        vivero.dropViv_emp();
        vivero.dropViv_zona(zona);
        assertNull(vivero.getViv_emp());
        assertTrue(vivero.getViv_zona().isEmpty());
    }
}
