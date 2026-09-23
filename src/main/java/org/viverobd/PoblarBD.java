package org.viverobd;

import jakarta.persistence.*;
import org.viverobd.models.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PoblarBD {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // 1. Crear 7 Viveros
            List<Vivero> viveros = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                Vivero v = new Vivero("90000000" + i, "Calle Falsa " + i);
                viveros.add(v);
                em.persist(v);
            }

            // 2. Crear 7 Empleados y asociar con Viveros (Relación 1:1)
            List<Empleado> empleados = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                Empleado e = new Empleado("60000000" + i, "Empleado " + i, "12345678" + (char)('A' + i));
                Vivero v = viveros.get(i - 1);
                e.formEmp_viv(v);
                v.formViv_emp(e);
                empleados.add(e);
                em.persist(e);
            }

            // 3. Crear 7 Zonas (una por vivero para cumplir con el mínimo de 7 objetos)
            List<Zona> zonas = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                Zona z = new Zona("Zona " + i, 100.0f * i, Zona.TipoZona.values()[i % Zona.TipoZona.values().length]);
                Vivero v = viveros.get(i - 1);
                z.formZona_viv(v);
                v.formViv_zona(z);
                zonas.add(z);
                em.persist(z);
            }

            // 4. Crear 7 Productos
            List<Producto> productos = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                Producto.TipoProducto tipo = (i <= 4) ? Producto.TipoProducto.tipo_planta : Producto.TipoProducto.tipo_accesorio;
                Producto p = new Producto("PROD" + i, "Descripcion Producto " + i, 10.5f * i, tipo);
                productos.add(p);
                em.persist(p);
            }

            // 5. Crear 7 Plantas (asociadas a los primeros 4 productos tipo planta y otros nuevos si es necesario)
            List<Planta> plantas = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                Planta pla = new Planta("Planta " + i, 20.0, 60.0, 80.0, "Cuidados planta " + i);
                // Asociar con un producto de tipo planta (usamos los primeros 4 de forma cíclica)
                Producto p = productos.get((i - 1) % 4);
                pla.formPla_prod(p);
                p.formPro_planta(pla);
                plantas.add(pla);
                em.persist(pla);
            }

            // 6. Relaciones: ZonaEmpleado (Asignar empleados a zonas)
            for (int i = 1; i <= 7; i++) {
                ZonaEmpleado ze = new ZonaEmpleado("ZE" + i, new Timestamp(System.currentTimeMillis()), "08:00");
                Empleado e = empleados.get(i - 1);
                Zona z = zonas.get(i - 1);
                
                ze.formZonae_emp(e);
                e.formEmp_zonae(ze);
                
                z.formZona_zonae(ze);
                ze.formZonae_zona(z);
                
                em.persist(ze);
            }

            // 7. Relaciones: ZonaPlanta (Ubicación de plantas en zonas)
            for (int i = 1; i <= 7; i++) {
                ZonaPlanta zp = new ZonaPlanta(i, 22.5, 55.0, new java.util.Date(), new Time(System.currentTimeMillis()));
                Planta pla = plantas.get(i - 1);
                Zona z = zonas.get(i - 1);
                
                zp.formZonap_planta(pla);
                pla.formPla_zonap(zp);
                zp.formZonap_zona(z);
                z.formZona_zonap(zp);
                
                em.persist(zp);
            }

            // 8. Relaciones: Stock (Productos en zonas)
            for (int i = 1; i <= 7; i++) {
                Stock s = new Stock(i, 50 * i);
                Producto p = productos.get(i - 1);
                Zona z = zonas.get(i - 1);
                
                s.formStock_producto(p);
                p.formPro_stock(s);
                s.formStock_zona(z);
                z.formZona_stock(s);
                
                em.persist(s);
            }

            em.getTransaction().commit();
            System.out.println("Base de datos poblada con éxito.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
