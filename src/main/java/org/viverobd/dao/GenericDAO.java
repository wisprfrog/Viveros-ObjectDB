package org.viverobd.dao;

import java.util.*;

public interface GenericDAO<T> {
    enum TipoOperacion {
        BUSCAR("buscado"),
        CREAR("creado"),
        ACTUALIZAR("actualizado"),
        ELIMINAR("eliminado");

        private final String operacion;

        public String getOperacion() { return operacion; }

        TipoOperacion(String operacion) { this.operacion = operacion; }
    }

    void create(T entity);
    List<T> readByAttributes(Map<String, Object> attributes);
    List<T> readByNumberRange(String attribute, Double min, Double max);
    void update(T entity);
    void delete(T entity);
    void showOperationStatus(TipoOperacion operacion, boolean success, String... causa_error);
}
