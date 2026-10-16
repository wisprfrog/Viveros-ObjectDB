package org.viverobd.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EliminacionRulesTest {

    @Test
    void productoSoloPuedeEliminarseSinStockNiRelacionesDeZona() {
        assertAll(
                () -> assertFalse(EliminacionRules.productoTieneRelaciones(0, 0)),
                () -> assertTrue(EliminacionRules.productoTieneRelaciones(1, 0)),
                () -> assertTrue(EliminacionRules.productoTieneRelaciones(0, 1)),
                () -> assertTrue(EliminacionRules.productoTieneRelaciones(3, 2))
        );
    }

    @Test
    void zonaNoPuedeEliminarseSiTieneCualquierRelacion() {
        assertAll(
                () -> assertFalse(EliminacionRules.zonaTieneRelaciones(0, 0, 0)),
                () -> assertTrue(EliminacionRules.zonaTieneRelaciones(1, 0, 0)),
                () -> assertTrue(EliminacionRules.zonaTieneRelaciones(0, 1, 0)),
                () -> assertTrue(EliminacionRules.zonaTieneRelaciones(0, 0, 1))
        );
    }

    @Test
    void viveroYEmpleadoNoPuedenEliminarseConRelaciones() {
        assertAll(
                () -> assertFalse(EliminacionRules.viveroTieneRelaciones(0, 0)),
                () -> assertTrue(EliminacionRules.viveroTieneRelaciones(1, 0)),
                () -> assertTrue(EliminacionRules.viveroTieneRelaciones(0, 1)),
                () -> assertFalse(EliminacionRules.empleadoTieneRelaciones(0, 0)),
                () -> assertTrue(EliminacionRules.empleadoTieneRelaciones(1, 0)),
                () -> assertTrue(EliminacionRules.empleadoTieneRelaciones(0, 1))
        );
    }

    @Test
    void stockSoloPuedeEliminarseCuandoEstaVacio() {
        assertAll(
                () -> assertTrue(EliminacionRules.stockPuedeEliminarse(0)),
                () -> assertFalse(EliminacionRules.stockPuedeEliminarse(1)),
                () -> assertFalse(EliminacionRules.stockPuedeEliminarse(100))
        );
    }
}
