package org.viverobd.services;

import org.viverobd.models.ZonaPlanta;
import java.util.List;
import java.util.Map;

public interface ZonaPlantaService {
    void registrarZonaPlanta(ZonaPlanta zp);
    void actualizarZonaPlanta(ZonaPlanta zp);
    void eliminarZonaPlanta(ZonaPlanta zp);
    List<ZonaPlanta> listarTodos();
    List<ZonaPlanta> buscarPorAtributos(Map<String, Object> atributos);
}
