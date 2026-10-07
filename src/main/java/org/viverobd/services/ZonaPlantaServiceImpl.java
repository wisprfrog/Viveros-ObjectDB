package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ZonaPlantaDAO;
import org.viverobd.models.ZonaPlanta;

import java.util.List;
import java.util.Map;

public class ZonaPlantaServiceImpl implements ZonaPlantaService {
    private final EntityManagerFactory emf;

    public ZonaPlantaServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void registrarZonaPlanta(ZonaPlanta zp) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
            dao.create(zp);
        } finally {
            em.close();
        }
    }

    @Override
    public void actualizarZonaPlanta(ZonaPlanta zp) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
            dao.update(zp);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarZonaPlanta(ZonaPlanta zp) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
            dao.delete(zp);
        } finally {
            em.close();
        }
    }

    @Override
    public List<ZonaPlanta> listarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
            return dao.readByAttributes(new java.util.HashMap<>());
        } finally {
            em.close();
        }
    }

    @Override
    public List<ZonaPlanta> buscarPorAtributos(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
            return dao.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }
}
