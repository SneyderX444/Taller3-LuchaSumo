package sumo.controlador.interfaces;

/**
 * @deprecated Esta interfaz fue reubicada en {@link sumo.modelo.interfaces.ICombateObservador}
 * para respetar la arquitectura MVC: el Modelo no puede importar del Controlador.
 * Use {@code sumo.modelo.interfaces.ICombateObservador} directamente.
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see sumo.modelo.interfaces.ICombateObservador
 */
@Deprecated
public interface ICombateObservador extends sumo.modelo.interfaces.ICombateObservador {
    // Redirige a sumo.modelo.interfaces.ICombateObservador
}
