package org.viverobd.services;

import org.viverobd.models.ZonaPlanta;
import java.util.List;
import java.util.Map;

public interface ZonaPlantaService {
    boolean registrarZonaPlanta(ZonaPlanta zp);
    boolean actualizarZonaPlanta(ZonaPlanta zp);
    boolean eliminarZonaPlanta(ZonaPlanta zp);
    List<ZonaPlanta> listarTodos();
    List<ZonaPlanta> buscarPorAtributos(Map<String, Object> atributos);
}
