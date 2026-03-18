package co.edu.udistrital.sumo.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el ring sagrado del sumo (dohyō).
 *
 * Propósito: Almacenar ÚNICAMENTE el estado del combate y los luchadores.
 * Es un POJO puro — no contiene lógica de negocio, sincronización ni hilos.
 * La coordinación del combate es responsabilidad de
 * {@link sumo.controlador.ControladorCombate}.
 * Se comunica con: {@link sumo.controlador.ControladorCombate} (consumidor del estado).
 * Principio SOLID:
 * S — única responsabilidad: representar el estado del ring.
 *
 * PROHIBIDO en esta clase: synchronized, wait, notify, Random, lógica de turnos.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see Rikishi
 * @see Kimarite
 */
public class Dohyo {

    //Arreglo con los dos luchadores que participan en el combate (índices 0 y 1)
    private final Rikishi[] luchadores;

    //Índice del luchador que tiene el turno actual (0 o 1)
    private int turnoActual;

    //Indica si el combate ha finalizado
    //volatile garantiza visibilidad entre hilos sin necesidad de synchronized
    private volatile boolean combateTerminado;

    //Luchador ganador del combate, null si aún no ha terminado
    private Rikishi ganador;

    //Bandera para que el inicio del combate solo se anuncie una vez
    private boolean combateAnunciado;

    /**
     * Construye un nuevo Dohyō con estado inicial limpio.
     */
    public Dohyo() {
        this.luchadores       = new Rikishi[2];
        this.turnoActual      = 0;
        this.combateTerminado = false;
        this.combateAnunciado = false;
        this.ganador          = null;
    }

    //Registra un luchador en el slot indicado (0 o 1)
    public void setLuchador(Rikishi rikishi, int indice) {
        luchadores[indice] = rikishi;
    }

    //Retorna el luchador en el índice especificado
    public Rikishi getLuchador(int indice) {
        return luchadores[indice];
    }

    //Retorna true si ambos luchadores ya están registrados en el dohyō
    public boolean ambosLuchadoresPresentes() {
        return luchadores[0] != null && luchadores[1] != null;
    }

    //Retorna el índice del luchador que tiene el turno actual
    public int getTurnoActual() {
        return turnoActual;
    }

    //Establece el índice del luchador que tiene el turno
    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    //Retorna true si el combate ha finalizado
    public boolean isCombateTerminado() {
        return combateTerminado;
    }

    //Marca el combate como terminado
    public void setCombateTerminado(boolean combateTerminado) {
        this.combateTerminado = combateTerminado;
    }

    //Retorna el luchador ganador, o null si el combate no ha terminado
    public Rikishi getGanador() {
        return ganador;
    }

    //Establece el luchador ganador del combate
    public void setGanador(Rikishi ganador) {
        this.ganador = ganador;
    }

    //Retorna true si el inicio del combate ya fue anunciado
    public boolean isCombateAnunciado() {
        return combateAnunciado;
    }

    //Marca el combate como anunciado para evitar anunciarlo más de una vez
    public void setCombateAnunciado(boolean combateAnunciado) {
        this.combateAnunciado = combateAnunciado;
    }
}

