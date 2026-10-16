package org.viverobd.services;

import org.viverobd.models.Zona;
import org.viverobd.models.Vivero;
import java.util.List;
import java.util.Map;

public interface ZonaService {
    boolean agregarZona(Zona zona);
    boolean modificarZona(Zona zona);
    boolean eliminarZona(Zona zona);
    Zona buscarPorId(String id);
    List<Zona> buscarPorAtributos(Map<String, Object> atributos);
    List<Zona> buscarTodas();
    List<Vivero> obtenerTodosViveros();
}
