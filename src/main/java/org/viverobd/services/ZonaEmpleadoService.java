package org.viverobd.services;

import org.viverobd.models.ZonaEmpleado;
import java.util.List;
import java.util.Map;

public interface ZonaEmpleadoService {
    void agregarZonaEmpleado(ZonaEmpleado zonaEmpleado);
    void modificarZonaEmpleado(ZonaEmpleado zonaEmpleado);
    void eliminarZonaEmpleado(ZonaEmpleado zonaEmpleado);
    ZonaEmpleado buscarPorId(String id);
    List<ZonaEmpleado> buscarPorAtributos(Map<String, Object> atributos);
    List<ZonaEmpleado> buscarTodos();
}
