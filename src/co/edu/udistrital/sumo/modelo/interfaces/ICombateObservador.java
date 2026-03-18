package co.edu.udistrital.sumo.modelo.interfaces;

/**
 * Interfaz del patrón Observer para los eventos del combate de sumo.
 *
 * Propósito: Definir el contrato de notificación de eventos del combate.
 * {@link co.edu.udistrital.sumo.controlador.ControladorDohyo} notifica a todos
 * los objetos que implementen esta interfaz cuando ocurre un evento relevante,
 * permitiendo que {@link co.edu.udistrital.sumo.controlador.ControladorServidor}
 * actualice la vista sin que el controlador del combate conozca la vista.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorDohyo}
 * (emisor) y {@link co.edu.udistrital.sumo.controlador.ControladorServidor}
 * (implementador).
 * Principio SOLID:
 * D — las capas dependen de esta abstracción, no de implementaciones concretas.
 * I — interfaz específica para eventos del combate.
 *
 * IMPORTANTE: Las implementaciones deben actualizar la UI usando
 * {@code SwingUtilities.invokeLater()} para respetar el EDT de Swing.
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see co.edu.udistrital.sumo.controlador.ControladorDohyo
 * @see co.edu.udistrital.sumo.controlador.ControladorServidor
 */
public interface ICombateObservador {

    /**
     * Invocado cuando un luchador llega y sube al dohyō.
     *
     * @param nombre nombre del luchador que llegó
     * @param peso   peso del luchador en kg
     * @param indice posición en el dohyō (0 = primero, 1 = segundo)
     */
    void onLuchadorLlego(String nombre, double peso, int indice);

    /**
     * Invocado cuando ambos luchadores están en el dohyō y el combate comienza.
     *
     * @param nombreLuchador1 nombre del primer luchador
     * @param nombreLuchador2 nombre del segundo luchador
     */
    void onCombateIniciado(String nombreLuchador1, String nombreLuchador2);

    /**
     * Invocado cuando un luchador ejecuta un kimarite durante su turno.
     *
     * @param nombreLuchador nombre del luchador que atacó
     * @param nombreKimarite nombre de la técnica ejecutada
     * @param expulsado      true si el oponente fue expulsado del dohyō
     */
    void onKimariteEjecutado(String nombreLuchador,
                              String nombreKimarite,
                              boolean expulsado);

    /**
     * Invocado cuando el combate finaliza y hay un ganador.
     *
     * @param nombreGanador    nombre del luchador ganador
     * @param victoriasGanador número total de victorias del ganador
     */
    void onCombateTerminado(String nombreGanador, int victoriasGanador);
}
