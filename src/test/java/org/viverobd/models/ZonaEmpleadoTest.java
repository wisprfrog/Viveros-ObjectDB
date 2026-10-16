package org.viverobd.models;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class ZonaEmpleadoTest {

    @Test
    void constructorInicializaLosDatosDeLaAsignacion() {
        Timestamp asignacion = Timestamp.valueOf("2026-01-10 08:00:00");
        Timestamp salida = Timestamp.valueOf("2026-02-10 17:00:00");
        ZonaEmpleado zonaEmpleado = new ZonaEmpleado("ZE-001", asignacion, salida, "08:00");

        assertAll(
                () -> assertEquals("ZE-001", zonaEmpleado.getZonae_codigo()),
                () -> assertEquals(asignacion, zonaEmpleado.getZonae_fecha_asignacion()),
                () -> assertEquals(salida, zonaEmpleado.getZonae_fecha_salida()),
                () -> assertEquals("08:00", zonaEmpleado.getZonae_hora_asignacion()),
                () -> assertNull(zonaEmpleado.getZonae_emp()),
                () -> assertNull(zonaEmpleado.getZonae_zona())
        );
    }

    @Test
    void relacionesConEmpleadoYZonaSePuedenGestionar() {
        ZonaEmpleado zonaEmpleado = new ZonaEmpleado();
        Empleado empleado = new Empleado();
        Zona zona = new Zona();

        zonaEmpleado.formZonae_emp(empleado);
        zonaEmpleado.formZonae_zona(zona);
        assertSame(empleado, zonaEmpleado.getZonae_emp());
        assertSame(zona, zonaEmpleado.getZonae_zona());

        zonaEmpleado.dropZonae_emp();
        zonaEmpleado.dropZonae_zona();
        assertNull(zonaEmpleado.getZonae_emp());
        assertNull(zonaEmpleado.getZonae_zona());
    }
}
