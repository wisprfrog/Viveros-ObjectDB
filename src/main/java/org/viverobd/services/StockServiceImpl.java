package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.viverobd.dao.StockDAO;
import org.viverobd.dao.ZonaDAO;
import org.viverobd.dao.ProductoDAO;
import org.viverobd.models.Stock;
import org.viverobd.models.Zona;
import org.viverobd.models.Producto;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class StockServiceImpl implements StockService {
    private final EntityManagerFactory emf;

    public StockServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void agregarStock(Stock stock) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            // Verificar si ya existe stock para esa zona y producto
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("stock_zona.zona_nombre", stock.getStock_zona().getZona_nombre());
            attrs.put("stock_producto.prod_nombre", stock.getStock_producto().getProd_nombre());
            
            List<Stock> existente = stockDAO.readByAttributes(attrs);
            if (existente != null && !existente.isEmpty()) {
                stockDAO.showOperationStatus(null, false, "Ya existe un registro de stock para este producto en esta zona.");
                return;
            }

            stockDAO.create(stock);
        } finally {
            em.close();
        }
    }

    @Override
    public void modificarStock(Stock stock) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            stockDAO.update(stock);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarStock(Stock stock) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            // Regla: Un stock no puede ser eliminado si tiene productos (cantidad > 0)
            Stock managedStock = em.find(Stock.class, stock.getStock_id());
            if (managedStock != null && managedStock.getStock_cantidad() > 0) {
                stockDAO.showOperationStatus(null, false, "No se puede eliminar un stock que todavía tiene existencias.");
                return;
            }
            stockDAO.delete(stock);
        } finally {
            em.close();
        }
    }

    @Override
    public Stock buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Stock.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Stock> buscarStock(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            return stockDAO.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Stock> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Stock s", Stock.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Zona> obtenerTodasZonas() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT z FROM Zona z", Zona.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Producto> obtenerTodosProductos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
        } finally {
            em.close();
        }
    }
}
