package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ProductoDAO;
import org.viverobd.dao.GenericDAO;
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
    public boolean agregarProducto(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            if (producto == null || producto.getProd_nombre() == null
                    || producto.getPro_descripcion() == null
                    || producto.getPro_tipo() == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.CREAR, false,
                        "Los datos del producto son obligatorios.");
                return false;
            }
            if (!Float.isFinite(producto.getPro_precio()) || producto.getPro_precio() <= 0) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.CREAR, false,
                        "El precio debe ser un número mayor que cero.");
                return false;
            }
            if (em.find(Producto.class, producto.getProd_nombre()) != null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.CREAR, false,
                        "El producto ya existe en la base de datos.");
                return false;
            }
            dao.create(producto);
            return em.find(Producto.class, producto.getProd_nombre()) != null;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modificarProducto(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            if (producto == null || producto.getProd_nombre() == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ACTUALIZAR, false,
                        "El producto debe tener un nombre.");
                return false;
            }
            if (producto.getPro_descripcion() == null || producto.getPro_tipo() == null
                    || !Float.isFinite(producto.getPro_precio()) || producto.getPro_precio() <= 0) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ACTUALIZAR, false,
                        "Los datos del producto no son válidos.");
                return false;
            }
            if (em.find(Producto.class, producto.getProd_nombre()) == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ACTUALIZAR, false,
                        "El producto no existe en la base de datos.");
                return false;
            }
            dao.update(producto);
            Producto actualizado = em.find(Producto.class, producto.getProd_nombre());
            return actualizado != null
                    && producto.getPro_descripcion().equals(actualizado.getPro_descripcion())
                    && Float.compare(producto.getPro_precio(), actualizado.getPro_precio()) == 0
                    && producto.getPro_tipo() == actualizado.getPro_tipo()
                    && ((producto.getPro_planta() == null && actualizado.getPro_planta() == null)
                    || (producto.getPro_planta() != null && actualizado.getPro_planta() != null));
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarProducto(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoDAO dao = new ProductoDAO(em, Producto.class);
            if (producto == null || producto.getProd_nombre() == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false,
                        "El producto debe tener un nombre.");
                return false;
            }
            Producto managed = em.find(Producto.class, producto.getProd_nombre());
            if (managed == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false,
                        "El producto no existe en la base de datos.");
                return false;
            }
            long stocks = em.createQuery(
                            "SELECT COUNT(s) FROM Stock s " +
                            "WHERE s.stock_producto = :producto", Long.class)
                    .setParameter("producto", managed)
                    .getSingleResult();
            long relacionesZonaPlanta = 0;
            if (managed.getPro_planta() != null) {
                relacionesZonaPlanta = em.createQuery(
                                "SELECT COUNT(zp) FROM ZonaPlanta zp " +
                                "WHERE zp.zonap_planta = :planta", Long.class)
                        .setParameter("planta", managed.getPro_planta())
                        .getSingleResult();
            }
            if (EliminacionRules.productoTieneRelaciones(stocks, relacionesZonaPlanta)) {
                String mensaje = stocks > 0
                        ? "No se puede eliminar una planta o producto con stock asociado."
                        : "No se puede eliminar una planta que tiene relaciones con zonas.";
                dao.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false,
                        mensaje);
                return false;
            }
            dao.delete(managed);
            return em.find(Producto.class, producto.getProd_nombre()) == null;
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
            if (atributos == null || atributos.isEmpty()) {
                return buscarTodosConEntityManager(em);
            }

            String campo = atributos.keySet().iterator().next();
            Object valor = atributos.get(campo);
            String jpql = "SELECT p FROM Producto p WHERE p." + campo
                    + (valor instanceof String ? " LIKE :valor" : " = :valor");
            var query = em.createQuery(jpql, Producto.class);
            query.setParameter("valor", valor instanceof String ? "%" + valor + "%" : valor);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    private List<Producto> buscarTodosConEntityManager(EntityManager em) {
        return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
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
