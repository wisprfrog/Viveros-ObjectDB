package org.viverobd;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.viverobd.models.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class PoblarBD {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("db/viverobd.odb");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // 1. Crear Empleados
            Empleado emp1 = new Empleado("600111222", "Juan Perez", "12345678A");
            Empleado emp2 = new Empleado("600333444", "Maria Garcia", "87654321B");
            Empleado emp3 = new Empleado("600555666", "Carlos Lopez", "11223344C");

            // 2. Crear Viveros
            Vivero viv1 = new Vivero("912345678", "Vivero Central", "Calle Principal 1");
            viv1.formViv_emp(emp1); // Juan es el encargado
            emp1.formEmp_viv(viv1);

            Vivero viv2 = new Vivero("918765432", "Vivero del Sur", "Av. de la Paz 20");
            viv2.formViv_emp(emp2); // Maria es la encargada
            emp2.formEmp_viv(viv2);

            // 3. Crear Zonas
            Zona zona1 = new Zona("Invernadero A", 150.5f, Zona.TipoZona.zona_climatizada);
            zona1.formZona_viv(viv1);
            viv1.formViv_zona(zona1);

            Zona zona2 = new Zona("Zona Exterior 1", 500.0f, Zona.TipoZona.zona_siembra);
            zona2.formZona_viv(viv1);
            viv1.formViv_zona(zona2);

            Zona zona3 = new Zona("Almacen General", 80.0f, Zona.TipoZona.zona_almacenamiento);
            zona3.formZona_viv(viv2);
            viv2.formViv_zona(zona3);

            // 4. Crear Asignaciones de Empleados a Zonas (ZonaEmpleado)
            long now = System.currentTimeMillis();
            ZonaEmpleado ze1 = new ZonaEmpleado("ZE001", new Timestamp(now), null, "08:00");
            ze1.formZonae_emp(emp3);
            emp3.formEmp_zonae(ze1);
            ze1.formZonae_zona(zona1);
            
            ZonaEmpleado ze2 = new ZonaEmpleado("ZE002", new Timestamp(now), null, "09:00");
            ze2.formZonae_emp(emp1);
            emp1.formEmp_zonae(ze2);
            ze2.formZonae_zona(zona2);

            // 5. Crear Productos y Plantas
            Producto p1 = new Producto("Maceta Ceramica", "Maceta de barro cocido decorada", 12.50f, Producto.TipoProducto.tipo_decoracion);
            
            Producto p2 = new Producto("Rosa Roja", "Rosal de flores rojas intensas", 8.95f, Producto.TipoProducto.tipo_planta);
            Planta pla2 = new Planta("Rosa Roja", "Rosa Gallica", 20.0, 60.0, 80.0, "Riego moderado, sol directo", Planta.TipoPlanta.planta_ornamental);
            p2.formPro_planta(pla2);
            pla2.formPla_prod(p2);

            Producto p3 = new Producto("Tomate Cherry", "Planta de tomates cherry dulces", 4.50f, Producto.TipoProducto.tipo_planta);
            Planta pla3 = new Planta("Tomate Cherry", "Solanum lycopersicum", 25.0, 70.0, 90.0, "Mucho sol y agua", Planta.TipoPlanta.hortaliza);
            p3.formPro_planta(pla3);
            pla3.formPla_prod(p3);

            // 6. Crear Stock
            Stock s1 = new Stock(1, 50);
            s1.formStock_zona(zona1);
            zona1.formZona_stock(s1);
            s1.formStock_producto(p2);
            p2.formPro_stock(s1);

            Stock s2 = new Stock(2, 100);
            s2.formStock_zona(zona2);
            zona2.formZona_stock(s2);
            s2.formStock_producto(p3);
            p3.formPro_stock(s2);

            Stock s3 = new Stock(3, 25);
            s3.formStock_zona(zona3);
            zona3.formZona_stock(s3);
            s3.formStock_producto(p1);
            p1.formPro_stock(s3);



            // 7. Crear Registros ZonaPlanta
            ZonaPlanta zp1 = new ZonaPlanta(1, 22.5, 65.0, new Date(), new Time(now));
            zp1.formZonap_zona(zona1);
            zona1.formZona_zonap(zp1);
            zp1.formZonap_planta(pla2);
            
            ZonaPlanta zp2 = new ZonaPlanta(2, 24.0, 72.0, new Date(), new Time(now));
            zp2.formZonap_zona(zona1);
            zona1.formZona_zonap(zp2);
            zp2.formZonap_planta(pla3);

            // Persistir los objetos en la base de datos
            em.persist(emp1);
            em.persist(emp2);
            em.persist(emp3);

            em.persist(viv1);
            em.persist(viv2);

            em.persist(zona1);
            em.persist(zona2);
            em.persist(zona3);

            em.persist(ze1);
            em.persist(ze2);

            em.persist(p1);
            em.persist(p2);
            em.persist(p3);

            em.persist(s1);
            em.persist(s2);
            em.persist(s3);

            em.persist(zp1);
            em.persist(zp2);

            em.getTransaction().commit();
            System.out.println("Base de datos poblada correctamente.");

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
