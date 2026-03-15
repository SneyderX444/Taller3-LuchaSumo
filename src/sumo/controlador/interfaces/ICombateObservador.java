package sumo.controlador.interfaces;

/**
 * Interfaz del patrón Observer para los eventos del combate de sumo.
 * <p>
 * El {@link sumo.modelo.Dohyo} notifica a todos los objetos que implementen
 * esta interfaz cada vez que ocurre un evento relevante en el combate.
 * Esto permite que el {@link sumo.controlador.ControladorServidor} actualice
 * la vista sin que el modelo conozca la vista directamente (principio DIP de SOLID).
 * </p>
 *
 * <p>
 * <b>Importante:</b> Las implementaciones deben garantizar que las actualizaciones
 * de UI se realicen en el Event Dispatch Thread (EDT) usando
 * {@code SwingUtilities.invokeLater()}.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see sumo.modelo.Dohyo
 * @see sumo.controlador.ControladorServidor
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
     * @param expulsado      {@code true} si el oponente fue expulsado del dohyō
     */
    void onKimariteEjecutado(String nombreLuchador, String nombreKimarite, boolean expulsado);

    /**
     * Invocado cuando el combate finaliza y hay un ganador.
     *
     * @param nombreGanador    nombre del luchador ganador
     * @param victoriasGanador número total de victorias acumuladas por el ganador
     */
    void onCombateTerminado(String nombreGanador, int victoriasGanador);
}
