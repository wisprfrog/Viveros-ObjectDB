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
    public boolean agregarEmpleado(Empleado empleado) {
        EntityManager em = emf.createEntityManager();
        try {
            EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
            if (empleado == null || empleado.getEmp_telefono() == null
                    || empleado.getEmp_nombre() == null || empleado.getEmp_ine() == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.CREAR, false,
                        "Los datos del empleado son obligatorios.");
                return false;
            }
            if (em.find(Empleado.class, empleado.getEmp_telefono()) != null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.CREAR, false,
                        "Ya existe un empleado con ese teléfono.");
                return false;
            }
            dao.create(empleado);
            return em.find(Empleado.class, empleado.getEmp_telefono()) != null;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modificarEmpleado(Empleado empleado) {
        EntityManager em = emf.createEntityManager();
        try {
            EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
            if (empleado == null || empleado.getEmp_telefono() == null
                    || empleado.getEmp_nombre() == null || empleado.getEmp_ine() == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ACTUALIZAR, false,
                        "Los datos del empleado son obligatorios.");
                return false;
            }
            if (em.find(Empleado.class, empleado.getEmp_telefono()) == null) {
                dao.showOperationStatus(GenericDAO.TipoOperacion.ACTUALIZAR, false,
                        "El empleado no existe en la base de datos.");
                return false;
            }
            dao.update(empleado);
            Empleado actualizado = em.find(Empleado.class, empleado.getEmp_telefono());
            return actualizado != null
                    && empleado.getEmp_nombre().equals(actualizado.getEmp_nombre())
                    && empleado.getEmp_ine().equals(actualizado.getEmp_ine());
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarEmpleado(Empleado empleado) {
        EntityManager em = emf.createEntityManager();
        EmpleadoDAO dao = new EmpleadoDAO(em, Empleado.class);
        try {
            if (empleado == null || empleado.getEmp_telefono() == null) {
                throw new IllegalArgumentException("El empleado a eliminar no puede ser nulo y debe tener teléfono.");
            }

            // Verificar si tiene zonas asignadas antes de borrar (Regla de integridad)
            Empleado managed = em.find(Empleado.class, empleado.getEmp_telefono());
            if (managed == null) {
                throw new IllegalArgumentException("El empleado no existe en la base de datos.");
            }

            // No depender de la colección inversa de Empleado: con este
            // mapeo puede no estar cargada aunque exista una fila
            // ZonaEmpleado que apunte al empleado.
            long asignaciones = em.createQuery(
                            "SELECT COUNT(ze) FROM ZonaEmpleado ze " +
                            "WHERE ze.zonae_emp = :empleado", Long.class)
                    .setParameter("empleado", managed)
                    .getSingleResult();
            if (asignaciones > 0) {
                throw new RuntimeException("No se puede eliminar un empleado con zonas asignadas.");
            }

            // Vivero.viv_emp es una relación obligatoria; no se puede dejar
            // un vivero apuntando a un empleado que ya no existe.
            long viverosAsignados = em.createQuery(
                            "SELECT COUNT(v) FROM Vivero v " +
                            "WHERE v.viv_emp = :empleado", Long.class)
                    .setParameter("empleado", managed)
                    .getSingleResult();
            if (viverosAsignados > 0) {
                throw new RuntimeException("No se puede eliminar un empleado asignado como responsable de un vivero.");
            }

            // Usar la instancia administrada por este EntityManager. El objeto
            // recibido proviene de otro EntityManager que ya fue cerrado.
            dao.delete(managed);
            return em.find(Empleado.class, managed.getEmp_telefono()) == null;
        }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            dao.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false, e.getMessage());
            return false;
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
