package org.viverobd.services;

import org.viverobd.models.Vivero;
import java.util.List;
import java.util.Map;

public interface ViveroService {
    boolean agregarVivero(Vivero vivero);
    boolean modificarVivero(Vivero vivero);
    boolean eliminarVivero(Vivero vivero);
    Vivero buscarPorId(String id);
    List<Vivero> buscarPorAtributos(Map<String, Object> atributos);
    List<Vivero> buscarTodos();
}
