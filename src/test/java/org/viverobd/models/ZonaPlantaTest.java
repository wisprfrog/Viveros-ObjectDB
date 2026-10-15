package org.viverobd.models;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ZonaPlantaTest {

    @Test
    void constructorInicializaElRegistroAmbiental() {
        Date fecha = new Date();
        Time hora = Time.valueOf("09:30:00");
        ZonaPlanta registro = new ZonaPlanta(3, 24.5, 70.0, fecha, hora);

        assertAll(
                () -> assertEquals(3, registro.getZonap_id()),
                () -> assertEquals(24.5, registro.getZonap_temperatura()),
                () -> assertEquals(70.0, registro.getZonap_humedad()),
                () -> assertSame(fecha, registro.getZonap_fechaRegistro()),
                () -> assertSame(hora, registro.getZonap_horaRegistro()),
                () -> assertNull(registro.getZonap_zona()),
                () -> assertNull(registro.getZonap_planta())
        );
    }

    @Test
    void relacionesConZonaYPlantaSePuedenGestionar() {
        ZonaPlanta registro = new ZonaPlanta();
        Zona zona = new Zona();
        Planta planta = new Planta();

        registro.formZonap_zona(zona);
        registro.formZonap_planta(planta);
        assertSame(zona, registro.getZonap_zona());
        assertSame(planta, registro.getZonap_planta());

        registro.dropZonap_zona();
        registro.dropZonap_planta();
        assertNull(registro.getZonap_zona());
        assertNull(registro.getZonap_planta());
    }
}
