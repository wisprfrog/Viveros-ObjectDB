package org.viverobd.services;

import org.viverobd.models.Vivero;
import java.util.List;
import java.util.Map;

public interface ViveroService {
    void agregarVivero(Vivero vivero);
    void modificarVivero(Vivero vivero);
    void eliminarVivero(Vivero vivero);
    Vivero buscarPorId(String id);
    List<Vivero> buscarPorAtributos(Map<String, Object> atributos);
    List<Vivero> buscarTodos();
}
