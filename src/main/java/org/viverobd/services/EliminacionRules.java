package org.viverobd.services;

final class EliminacionRules {

    private EliminacionRules() {
    }

    static boolean productoTieneRelaciones(long stocks, long relacionesZonaPlanta) {
        return stocks > 0 || relacionesZonaPlanta > 0;
    }

    static boolean zonaTieneRelaciones(long empleados, long plantas, long stocks) {
        return empleados > 0 || plantas > 0 || stocks > 0;
    }

    static boolean viveroTieneRelaciones(long zonas, long empleados) {
        return zonas > 0 || empleados > 0;
    }

    static boolean empleadoTieneRelaciones(long asignaciones, long viveros) {
        return asignaciones > 0 || viveros > 0;
    }

    static boolean stockPuedeEliminarse(int cantidad) {
        return cantidad == 0;
    }
}
