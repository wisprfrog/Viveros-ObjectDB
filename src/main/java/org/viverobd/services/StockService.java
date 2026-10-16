package org.viverobd.services;

import org.viverobd.models.Stock;
import org.viverobd.models.Zona;
import org.viverobd.models.Producto;
import java.util.List;
import java.util.Map;

public interface StockService {
    boolean agregarStock(Stock stock);
    boolean modificarStock(Stock stock);
    boolean eliminarStock(Stock stock);
    Stock buscarPorId(int id);
    List<Stock> buscarStock(Map<String, Object> atributos);
    List<Stock> obtenerTodos();
    List<Zona> obtenerTodasZonas();
    List<Producto> obtenerTodosProductos();
}
