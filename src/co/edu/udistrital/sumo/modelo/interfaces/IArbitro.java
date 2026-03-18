package co.edu.udistrital.sumo.modelo.interfaces;

import co.edu.udistrital.sumo.modelo.Rikishi;

/**
 * Contrato que define el comportamiento del árbitro del combate de sumo.
 *
 * Propósito: Definir las operaciones de coordinación del combate que
 * {@link co.edu.udistrital.sumo.controlador.ControladorDohyo} debe implementar.
 * Permite que {@link co.edu.udistrital.sumo.controlador.HiloLuchador} dependa
 * de esta abstracción y no de la implementación concreta.
 * Principio SOLID:
 * D — las capas superiores dependen de esta abstracción, no del ControladorDohyo concreto.
 * I — interfaz específica para las operaciones de arbitraje del combate.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see co.edu.udistrital.sumo.controlador.ControladorDohyo
 */
public interface IArbitro {

    /**
     * Registra un luchador en el dohyō en la posición indicada.
     *
     * @param rikishi luchador que sube al ring
     * @param indice  posición en el dohyō (0 o 1)
     */
    void subirLuchador(Rikishi rikishi, int indice);

    /**
     * Bloquea el hilo actual hasta que ambos luchadores estén en el dohyō.
     *
     * @throws InterruptedException si el hilo es interrumpido durante la espera
     */
    void esperarAmbosLuchadores() throws InterruptedException;

    /**
     * Ejecuta el turno del luchador indicado.
     * Si no es su turno, el hilo espera hasta un máximo de 500ms.
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
     * @return el {@link Rikishi} ganador, o {@code null} si aún no terminó
     */
    Rikishi getGanador();
}
