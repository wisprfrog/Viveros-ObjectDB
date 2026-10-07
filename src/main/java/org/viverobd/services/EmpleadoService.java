package org.viverobd.services;

import org.viverobd.models.Empleado;
import java.util.List;
import java.util.Map;

public interface EmpleadoService {
    void agregarEmpleado(Empleado empleado);
    void modificarEmpleado(Empleado empleado);
    void eliminarEmpleado(Empleado empleado);
    Empleado buscarPorTelefono(String telefono);
    List<Empleado> buscarEmpleados(Map<String, Object> atributos);
    List<Empleado> obtenerTodos();
}
