package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ProductoDAO;
import org.viverobd.models.Planta;
import org.viverobd.models.Producto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoServiceImpl implements ProductoService {
    private final EntityManagerFactory emf;

    public ProductoServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void agregarProducto(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            dao.create(producto);
        } finally {
            em.close();
        }
    }

    @Override
    public void modificarProducto(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            dao.update(producto);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarProducto(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            // Aseguramos que el objeto esté gestionado antes de borrar
            Producto managed = em.find(Producto.class, producto.getProd_nombre());
            if (managed != null) {
                dao.delete(managed);
            }
        } finally {
            em.close();
        }
    }

    @Override
    public Producto buscarPorId(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Producto.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Producto> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            return dao.readByAttributes(new HashMap<>());
        } finally {
            em.close();
        }
    }

    @Override
    public List<Producto> buscarPorRangoPrecio(double min, double max) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            return dao.readByNumberRange("pro_precio", min, max);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Producto> buscarPorAtributos(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            return dao.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }

    @Override
    public Planta buscarPlantaPorId(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Planta.class, id);
        } finally {
            em.close();
        }
    }
}
