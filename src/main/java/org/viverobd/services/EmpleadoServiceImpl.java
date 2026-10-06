package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.EmpleadoDAO;
import org.viverobd.dao.GenericDAO;
import org.viverobd.models.Empleado;

import java.util.List;
import java.util.Map;

public class EmpleadoServiceImpl implements EmpleadoService {
    private final EntityManagerFactory emf;

    public EmpleadoServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void agregarEmpleado(Empleado empleado) {
        EntityManager em = emf.createEntityManager();
        try {
            EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
            dao.create(empleado);
        } finally {
            em.close();
        }
    }

    @Override
    public void modificarEmpleado(Empleado empleado) {
        EntityManager em = emf.createEntityManager();
        try {
            EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
            dao.update(empleado);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarEmpleado(Empleado empleado) {
        EntityManager em = emf.createEntityManager();
        EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
        try {
            // Verificar si tiene zonas asignadas antes de borrar (Regla de integridad)
            Empleado managed = em.find(Empleado.class, empleado.getEmp_telefono());
            if (managed != null && managed.getEmp_zonae() != null && !managed.getEmp_zonae().isEmpty()) {
                throw new RuntimeException("No se puede eliminar un empleado con zonas asignadas.");
            }
            dao.delete(empleado);
        }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            dao.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false, e.getMessage());
        }
        finally {
            em.close();
        }
    }

    @Override
    public Empleado buscarPorTelefono(String telefono) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Empleado.class, telefono);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Empleado> buscarEmpleados(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
            return dao.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Empleado> obtenerTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM Empleado e";
            return em.createQuery(jpql, Empleado.class).getResultList();
        } finally {
            em.close();
        }
    }
}
