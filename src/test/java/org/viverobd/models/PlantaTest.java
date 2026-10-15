package org.viverobd.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlantaTest {

    @Test
    void constructorInicializaLosDatosDeLaPlanta() {
        Planta planta = new Planta(
                "Rosa",
                "Rosa roja",
                22.5,
                65.0,
                80.0,
                "Riego moderado",
                Planta.TipoPlanta.planta_ornamental
        );

        assertAll(
                () -> assertEquals("Rosa", planta.getProd_nombre()),
                () -> assertEquals("Rosa roja", planta.getPla_nombre()),
                () -> assertEquals(22.5, planta.getPla_clima()),
                () -> assertEquals(65.0, planta.getPla_humedad()),
                () -> assertEquals(80.0, planta.getPla_luz()),
                () -> assertEquals("Riego moderado", planta.getPla_cuidados()),
                () -> assertEquals(Planta.TipoPlanta.planta_ornamental, planta.getPla_tipo()),
                () -> assertNull(planta.getPla_prod())
        );
    }

    @Test
    void relacionConProductoSePuedeAgregarYEliminar() {
        Planta planta = new Planta();
        Producto producto = new Producto();

        planta.formPla_prod(producto);
        assertSame(producto, planta.getPla_prod());

        planta.dropPla_prod();
        assertNull(planta.getPla_prod());
    }

    @Test
    void tipoPlantaSeBuscaPorNombreVisible() {
        assertEquals(
                Planta.TipoPlanta.arbol_frutal,
                Planta.TipoPlanta.fromNombre("Arbol Frutal")
        );
        assertNull(Planta.TipoPlanta.fromNombre("Tipo inexistente"));
    }
}
