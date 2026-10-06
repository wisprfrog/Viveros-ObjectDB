package org.viverobd.services;

import org.viverobd.models.Producto;
import java.util.List;
import java.util.Map;

public interface ProductoService {
    void agregarProducto(Producto producto);
    void modificarProducto(Producto producto);
    void eliminarProducto(Producto producto);
    Producto buscarPorId(String id);
    List<Producto> buscarTodos();
    List<Producto> buscarPorRangoPrecio(double min, double max);
    List<Producto> buscarPorAtributos(Map<String, Object> atributos);
    org.viverobd.models.Planta buscarPlantaPorId(String id);
}
