package sumo.modelo.interfaces;

import sumo.modelo.Rikishi;

/**
 * Contrato que define el comportamiento del árbitro y del dohyō (ring de sumo).
 * <p>
 * Esta interfaz aplica el principio de Inversión de Dependencias (DIP) de SOLID,
 * permitiendo que los controladores dependan de esta abstracción en lugar de
 * depender de la implementación concreta {@link sumo.modelo.Dohyo}.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 */
public interface IArbitro {

    /**
     * Sube un luchador al dohyō en el índice especificado.
     *
     * @param rikishi luchador que ingresa al ring
     * @param indice  posición (0 o 1) donde se registra el luchador
     */
    void subirLuchador(Rikishi rikishi, int indice);

    /**
     * Bloquea el hilo actual hasta que ambos luchadores hayan ingresado al dohyō.
     *
     * @throws InterruptedException si el hilo es interrumpido durante la espera
     */
    void esperarAmbosLuchadores() throws InterruptedException;

    /**
     * Ejecuta el turno de combate del luchador en el índice dado.
     * Si no es el turno del luchador, el hilo espera hasta un máximo de
     * {@link sumo.modelo.Dohyo#MAX_ESPERA_MS} milisegundos.
     *
     * @param indiceLuchador índice (0 o 1) del luchador que ejecuta el turno
     * @throws InterruptedException si el hilo es interrumpido durante la espera
     */
    void ejecutarTurno(int indiceLuchador) throws InterruptedException;

    /**
     * Indica si el combate ya terminó.
     *
     * @return {@code true} si hay un ganador y el combate finalizó
     */
    boolean isCombateTerminado();

    /**
     * Retorna el luchador ganador del combate.
     *
     * @return el {@link Rikishi} ganador, o {@code null} si el combate no ha terminado
     */
    Rikishi getGanador();
}
