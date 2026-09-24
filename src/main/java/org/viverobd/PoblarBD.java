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

            long currentTime = System.currentTimeMillis();
            long threeMonthsAgo = currentTime - (90L * 24 * 60 * 60 * 1000);
            long twoYearsAgo = currentTime - (2L * 365 * 24 * 60 * 60 * 1000);

            // 1. Crear 7 Viveros
            List<Vivero> viveros = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                Vivero v = new Vivero("90000000" + i, "Vivero "+i, "Calle Falsa " + i);
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

            // 3. Crear Zonas
            List<Zona> zonas = new ArrayList<>();
            // Zonas específicas para cumplir con las consultas
            String[] nombresZonas = {"Zona Regadio A", "Zona Climatizada B", "Zona Almacen C", "Zona Siembra D", "Zona Crecimiento E", "Zona Aclimatacion F", "Zona Sustrato G"};
            Zona.TipoZona[] tiposZonas = {Zona.TipoZona.zona_regadio, Zona.TipoZona.zona_climatizada, Zona.TipoZona.zona_almacenamiento, Zona.TipoZona.zona_siembra, Zona.TipoZona.zona_crecimiento, Zona.TipoZona.zona_aclimatacion, Zona.TipoZona.zona_preparacion_sustrato};
            
            for (int i = 1; i <= 7; i++) {
                Zona z = new Zona(nombresZonas[i-1], 100.0f * i, tiposZonas[i-1]);
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

            // 5. Crear 7 Plantas
            List<Planta> plantas = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                // Planta 1 y 2 serán de clima frío (pla_clima bajo, ej: 5.0)
                double clima = (i <= 2) ? 5.0 : 25.0;
                Planta pla = new Planta("Planta " + i, clima, 60.0, 80.0, "Cuidados planta " + i);
                // Asociar con un producto de tipo planta (usamos los primeros 4 de forma cíclica)
                Producto p = productos.get((i - 1) % 4);
                pla.formPla_prod(p);
                p.formPro_planta(pla);
                plantas.add(pla);
                em.persist(pla);
            }

            // 6. Relaciones: ZonaEmpleado (Asignar empleados a zonas e historial)
            for (int i = 1; i <= 7; i++) {
                // Registro actual
                ZonaEmpleado ze = new ZonaEmpleado("ZE-ACT-" + i, new Timestamp(currentTime), null, "08:00");
                Empleado e = empleados.get(i - 1);
                Zona z = zonas.get(i - 1);
                
                ze.formZonae_emp(e);
                e.formEmp_zonae(ze);
                z.formZona_zonae(ze);
                ze.formZonae_zona(z);
                em.persist(ze);

                // Registro histórico (hace un año, para consulta 6)
                if (i == 3) { // Empleado 3 estuvo en Zona 3 (Almacén) hace un año
                     ZonaEmpleado zeH = new ZonaEmpleado("ZE-HIST-" + i, new Timestamp(twoYearsAgo + 1000), new Timestamp(currentTime - 100000), "09:00");
                     zeH.formZonae_emp(e);
                     e.formEmp_zonae(zeH);
                     z.formZona_zonae(zeH);
                     zeH.formZonae_zona(z);
                     em.persist(zeH);
                }
            }

            // 7. Relaciones: ZonaPlanta (Ubicación de plantas en zonas y registro de temperatura histórico)
            int zpId = 1;
            for (int i = 1; i <= 7; i++) {
                Planta pla = plantas.get(i - 1);
                Zona z = zonas.get(i - 1);

                // Registro actual
                ZonaPlanta zp = new ZonaPlanta(zpId++, 22.5, 55.0, new java.util.Date(currentTime), new Time(currentTime));
                zp.formZonap_planta(pla);
                pla.formPla_zonap(zp);
                zp.formZonap_zona(z);
                z.formZona_zonap(zp);
                em.persist(zp);

                // Registros históricos de temperatura (últimos 3 meses, para consulta 4)
                if (z.getZona_tipo() == Zona.TipoZona.zona_climatizada) {
                    for (int m = 1; m <= 3; m++) {
                        long timeOffset = currentTime - (m * 30L * 24 * 60 * 60 * 1000);
                        ZonaPlanta zpH = new ZonaPlanta(zpId++, 18.0 + m, 50.0, new java.util.Date(timeOffset), new Time(timeOffset));
                        zpH.formZonap_planta(pla);
                        pla.formPla_zonap(zpH);
                        zpH.formZonap_zona(z);
                        z.formZona_zonap(zpH);
                        em.persist(zpH);
                    }
                }
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
