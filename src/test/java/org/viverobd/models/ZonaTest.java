package org.viverobd.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZonaTest {

    @Test
    void constructorInicializaLosDatosDeLaZona() {
        Zona zona = new Zona(
                "Zona A",
                150.5f,
                Zona.TipoZona.zona_siembra
        );

        assertAll(
                () -> assertEquals("Zona A", zona.getZona_nombre()),
                () -> assertEquals(150.5f, zona.getZona_superficie()),
                () -> assertEquals(Zona.TipoZona.zona_siembra, zona.getZona_tipo()),
                () -> assertNull(zona.getZona_viv()),
                () -> assertNotNull(zona.getZona_zonae()),
                () -> assertNotNull(zona.getZona_zonap()),
                () -> assertNotNull(zona.getZona_stock()),
                () -> assertTrue(zona.getZona_zonae().isEmpty()),
                () -> assertTrue(zona.getZona_zonap().isEmpty()),
                () -> assertTrue(zona.getZona_stock().isEmpty())
        );
    }

    @Test
    void relacionesSePuedenAgregarYEliminar() {
        Zona zona = new Zona();
        Vivero vivero = new Vivero();
        ZonaEmpleado zonaEmpleado = new ZonaEmpleado();
        ZonaPlanta zonaPlanta = new ZonaPlanta();
        Stock stock = new Stock();

        zona.formZona_viv(vivero);
        zona.formZona_zonae(zonaEmpleado);
        zona.formZona_zonap(zonaPlanta);
        zona.formZona_stock(stock);
        assertSame(vivero, zona.getZona_viv());
        assertTrue(zona.getZona_zonae().contains(zonaEmpleado));
        assertTrue(zona.getZona_zonap().contains(zonaPlanta));
        assertTrue(zona.getZona_stock().contains(stock));

        zona.dropZona_viv();
        zona.dropZona_zonae(zonaEmpleado);
        zona.dropZona_zonap(zonaPlanta);
        zona.dropZona_stock(stock);
        assertNull(zona.getZona_viv());
        assertTrue(zona.getZona_zonae().isEmpty());
        assertTrue(zona.getZona_zonap().isEmpty());
        assertTrue(zona.getZona_stock().isEmpty());
    }
}
