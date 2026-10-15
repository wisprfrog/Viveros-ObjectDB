package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.StockDAO;
import org.viverobd.models.Stock;
import org.viverobd.models.Zona;
import org.viverobd.models.Producto;

import java.util.List;
import java.util.Map;

public class StockServiceImpl implements StockService {
    private final EntityManagerFactory emf;

    public StockServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public boolean agregarStock(Stock stock) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            if (stock == null || stock.getStock_zona() == null
                    || stock.getStock_producto() == null || stock.getStock_cantidad() < 0) {
                stockDAO.showOperationStatus(null, false, "Los datos del stock no son válidos.");
                return false;
            }
            Zona zona = em.find(Zona.class, stock.getStock_zona().getZona_nombre());
            Producto producto = em.find(Producto.class, stock.getStock_producto().getProd_nombre());
            if (zona == null || producto == null) {
                stockDAO.showOperationStatus(null, false, "La zona o el producto no existen.");
                return false;
            }
            long existentes = em.createQuery(
                            "SELECT COUNT(s) FROM Stock s " +
                            "WHERE s.stock_zona = :zona AND s.stock_producto = :producto", Long.class)
                    .setParameter("zona", zona)
                    .setParameter("producto", producto)
                    .getSingleResult();
            if (existentes > 0) {
                stockDAO.showOperationStatus(null, false, "Ya existe un registro de stock para este producto en esta zona.");
                return false;
            }
            stock.formStock_zona(zona);
            stock.formStock_producto(producto);
            if (stock.getStock_id() <= 0 || em.find(Stock.class, stock.getStock_id()) != null) {
                Integer maxId = em.createQuery("SELECT MAX(s.stock_id) FROM Stock s", Integer.class)
                        .getSingleResult();
                stock.setStock_id(maxId == null ? 1 : maxId + 1);
            }
            em.getTransaction().begin();
            em.persist(stock);
            em.getTransaction().commit();
            return em.find(Stock.class, stock.getStock_id()) != null;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            stockDAO.showOperationStatus(null, false, "No se pudo agregar el stock: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modificarStock(Stock stock) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            if (stock == null || stock.getStock_cantidad() < 0) {
                stockDAO.showOperationStatus(null, false, "La cantidad de stock no es válida.");
                return false;
            }
            Stock managed = em.find(Stock.class, stock.getStock_id());
            if (managed == null) {
                stockDAO.showOperationStatus(null, false, "El stock no existe en la base de datos.");
                return false;
            }
            if (stock.getStock_zona() == null || stock.getStock_producto() == null) {
                stockDAO.showOperationStatus(null, false, "La zona y el producto son obligatorios.");
                return false;
            }
            Zona zona = em.find(Zona.class, stock.getStock_zona().getZona_nombre());
            Producto producto = em.find(Producto.class, stock.getStock_producto().getProd_nombre());
            if (zona == null || producto == null) {
                stockDAO.showOperationStatus(null, false, "La zona o el producto no existen.");
                return false;
            }
            long duplicados = em.createQuery(
                            "SELECT COUNT(s) FROM Stock s " +
                            "WHERE s.stock_zona = :zona AND s.stock_producto = :producto " +
                            "AND s.stock_id <> :id", Long.class)
                    .setParameter("zona", zona)
                    .setParameter("producto", producto)
                    .setParameter("id", stock.getStock_id())
                    .getSingleResult();
            if (duplicados > 0) {
                stockDAO.showOperationStatus(null, false, "Ya existe otro registro para este producto en esta zona.");
                return false;
            }
            em.getTransaction().begin();
            managed.formStock_zona(zona);
            managed.formStock_producto(producto);
            managed.setStock_cantidad(stock.getStock_cantidad());
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            stockDAO.showOperationStatus(null, false, "No se pudo actualizar el stock: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarStock(Stock stock) {
        EntityManager em = emf.createEntityManager();
        StockDAO stockDAO = new StockDAO(em, Stock.class);
        try {
            if (stock == null) {
                stockDAO.showOperationStatus(null, false, "El stock es obligatorio.");
                return false;
            }
            // Regla: Un stock no puede ser eliminado si tiene productos (cantidad > 0)
            Stock managedStock = em.find(Stock.class, stock.getStock_id());
            if (managedStock != null && managedStock.getStock_cantidad() > 0) {
                stockDAO.showOperationStatus(null, false, "No se puede eliminar un stock que todavía tiene existencias.");
                return false;
            }
            if (managedStock == null) {
                stockDAO.showOperationStatus(null, false, "El stock no existe en la base de datos.");
                return false;
            }
            em.getTransaction().begin();
            em.remove(managedStock);
            em.getTransaction().commit();
            return em.find(Stock.class, stock.getStock_id()) == null;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            stockDAO.showOperationStatus(null, false, "No se pudo eliminar el stock: " + e.getMessage());
            return false;
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
        try {
            if (atributos == null || atributos.isEmpty()) {
                return em.createQuery("SELECT s FROM Stock s", Stock.class).getResultList();
            }
            if (atributos.size() == 1 && atributos.containsKey("stock_zona.zona_nombre")) {
                return em.createQuery(
                                "SELECT s FROM Stock s WHERE LOWER(s.stock_zona.zona_nombre) LIKE :valor",
                                Stock.class)
                        .setParameter("valor", "%" + atributos.get("stock_zona.zona_nombre").toString().toLowerCase() + "%")
                        .getResultList();
            }
            if (atributos.size() == 1 && atributos.containsKey("stock_producto.prod_nombre")) {
                return em.createQuery(
                                "SELECT s FROM Stock s WHERE LOWER(s.stock_producto.prod_nombre) LIKE :valor",
                                Stock.class)
                        .setParameter("valor", "%" + atributos.get("stock_producto.prod_nombre").toString().toLowerCase() + "%")
                        .getResultList();
            }
            return List.of();
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
