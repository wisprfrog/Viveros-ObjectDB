package org.viverobd;

import jakarta.persistence.*;
import org.viverobd.models.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoblarBD {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            long currentTime = System.currentTimeMillis();
            long threeMonthsAgo = currentTime - (90L * 24 * 60 * 60 * 1000);
            long twoYearsAgo = currentTime - (2L * 365 * 24 * 60 * 60 * 1000);

            // 1. Crear 10 Viveros
            List<Vivero> viveros = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Vivero v = new Vivero("90000000" + (i < 10 ? "0" + i : i), "Vivero " + i, "Calle Falsa " + i);
                viveros.add(v);
                em.persist(v);
            }

            // 2. Crear 10 Empleados y asociar con Viveros (Relación 1:1)
            List<Empleado> empleados = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Empleado e = new Empleado("60000000" + (i < 10 ? "0" + i : i), "Empleado " + i, "12345678" + (char) ('A' + (i % 26)));
                Vivero v = viveros.get(i - 1);
                e.formEmp_viv(v);
                v.formViv_emp(e);
                empleados.add(e);
                em.persist(e);
            }

            // 3. Crear Zonas
            List<Zona> zonas = new ArrayList<>();
            // Zonas para cumplir con las consultas y diversidad
            String[] nombresZonasPrefijados = {"Zona Regadio A", "Zona Climatizada B", "Zona Almacen C", "Zona Siembra D", "Zona Crecimiento E", "Zona Aclimatacion F", "Zona Sustrato G", "Zona Regadio H", "Zona Climatizada I", "Zona Almacen J"};
            Zona.TipoZona[] tiposZonasPrefijados = {
                    Zona.TipoZona.zona_regadio, Zona.TipoZona.zona_climatizada, Zona.TipoZona.zona_almacenamiento,
                    Zona.TipoZona.zona_siembra, Zona.TipoZona.zona_crecimiento, Zona.TipoZona.zona_aclimatacion,
                    Zona.TipoZona.zona_preparacion_sustrato, Zona.TipoZona.zona_regadio, Zona.TipoZona.zona_climatizada,
                    Zona.TipoZona.zona_almacenamiento
            };

            for (int i = 1; i <= 10; i++) {
                Zona z = new Zona(nombresZonasPrefijados[i - 1], 100.0f * i, tiposZonasPrefijados[i - 1]);
                Vivero v = viveros.get(i - 1);
                z.formZona_viv(v);
                v.formViv_zona(z);
                zonas.add(z);
                em.persist(z);
            }

            // 4. Crear 10 Productos
            List<Producto> productos = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Producto.TipoProducto tipo;
                if (i <= 6) tipo = Producto.TipoProducto.tipo_planta;
                else if (i <= 8) tipo = Producto.TipoProducto.tipo_accesorio;
                else tipo = Producto.TipoProducto.tipo_decoracion;

                Producto p = new Producto("PROD" + i, "Descripcion Producto " + i, 10.5f * i, tipo);
                productos.add(p);
                em.persist(p);
            }

            // 5. Crear 10 Plantas
            List<Planta> plantas = new ArrayList<>();
            Random random = new Random();
            Planta.TipoPlanta[] tiposPlantas = Planta.TipoPlanta.values();
            for (int i = 1; i <= 10; i++) {
                // Planta 1, 2 y 8 serán de clima frío (pla_clima bajo, ej: 5.0)
                double clima = (i == 1 || i == 2 || i == 8) ? 5.0 : 25.0;
                Planta.TipoPlanta tipoAleatorio = tiposPlantas[random.nextInt(tiposPlantas.length)];
                Planta pla = new Planta("Planta " + i, clima, 60.0, 80.0, "Cuidados planta " + i, tipoAleatorio);
                // Asociar con un producto de tipo planta (usamos los primeros 6 de forma cíclica)
                Producto p = productos.get((i - 1) % 6);
                pla.formPla_prod(p);
                p.formPro_planta(pla);
                plantas.add(pla);
                em.persist(pla);
            }

            // 6. Relaciones: ZonaEmpleado (Asignar empleados a zonas e historial)
            int zeIdCounter = 1;
            for (int i = 1; i <= 10; i++) {
                // Registro actual: Cada empleado en su zona correspondiente
                Empleado e = empleados.get(i - 1);
                Zona z = zonas.get(i - 1);
                ZonaEmpleado ze = new ZonaEmpleado("ZE-ACT-" + (zeIdCounter++), new Timestamp(currentTime), null, "08:00");

                ze.formZonae_emp(e);
                e.formEmp_zonae(ze);
                z.formZona_zonae(ze);
                ze.formZonae_zona(z);
                em.persist(ze);

                // Relaciones extra: Algunos empleados asignados a otras zonas (movilidad)
                if (i % 3 == 0) {
                    Zona zExtra = zonas.get((i) % 10);
                    ZonaEmpleado zeExtra = new ZonaEmpleado("ZE-EXT-" + (zeIdCounter++), new Timestamp(currentTime - 3600000), null, "09:00");
                    zeExtra.formZonae_emp(e);
                    e.formEmp_zonae(zeExtra);
                    zExtra.formZona_zonae(zeExtra);
                    zeExtra.formZonae_zona(zExtra);
                    em.persist(zeExtra);
                }

                // Registro histórico (para consulta 6 y diversidad)
                if (i == 3 || i == 10) { // Empleado 3 y 10 estuvieron en Zona Almacen hace tiempo
                    Zona zAlmacen = (i == 3) ? zonas.get(2) : zonas.get(9);
                    ZonaEmpleado zeH = new ZonaEmpleado("ZE-HIST-" + (zeIdCounter++), new Timestamp(twoYearsAgo + 1000), new Timestamp(currentTime - 500000), "09:00");
                    zeH.formZonae_emp(e);
                    e.formEmp_zonae(zeH);
                    zAlmacen.formZona_zonae(zeH);
                    zeH.formZonae_zona(zAlmacen);
                    em.persist(zeH);
                }
            }

            // 7. Relaciones: ZonaPlanta (Ubicación de plantas en zonas y registro de temperatura histórico)
            int zpId = 1;
            for (int i = 1; i <= 10; i++) {
                Planta pla = plantas.get(i - 1);
                Zona z = zonas.get(i - 1);

                // Registro actual
                ZonaPlanta zp = new ZonaPlanta(zpId++, 22.5, 55.0, new java.util.Date(currentTime), new Time(currentTime));
                zp.formZonap_planta(pla);
                pla.formPla_zonap(zp);
                zp.formZonap_zona(z);
                z.formZona_zonap(zp);
                em.persist(zp);

                // Relación extra: Planta en otra zona
                if (i % 2 == 0) {
                    Zona zExtra = zonas.get((i + 2) % 10);
                    ZonaPlanta zpExtra = new ZonaPlanta(zpId++, 20.0, 60.0, new java.util.Date(currentTime), new Time(currentTime));
                    zpExtra.formZonap_planta(pla);
                    pla.formPla_zonap(zpExtra);
                    zpExtra.formZonap_zona(zExtra);
                    zExtra.formZona_zonap(zpExtra);
                    em.persist(zpExtra);
                }

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
            int stockId = 1;
            for (int i = 1; i <= 10; i++) {
                // Stock base
                Stock s = new Stock(stockId++, 50 * i);
                Producto p = productos.get(i - 1);
                Zona z = zonas.get(i - 1);

                s.formStock_producto(p);
                p.formPro_stock(s);
                s.formStock_zona(z);
                z.formZona_stock(s);
                em.persist(s);

                // Stock extra: Mismo producto en otra zona
                if (i <= 5) {
                    Zona zExtra = zonas.get(9 - i);
                    Stock sExtra = new Stock(stockId++, 20 * i);
                    sExtra.formStock_producto(p);
                    p.formPro_stock(sExtra);
                    sExtra.formStock_zona(zExtra);
                    zExtra.formZona_stock(sExtra);
                    em.persist(sExtra);
                }
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
