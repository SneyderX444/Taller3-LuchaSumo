package co.edu.udistrital.sumo.modelo.servidor;

import co.edu.udistrital.sumo.modelo.cliente.Rikishi;

/**
 * Clase que representa el dohyo (ring de pelea del sumo).
 *
 * Aquí básicamente se guarda todo el estado del combate:
 * quiénes son los luchadores, de quién es el turno,
 * si ya terminó la pelea y quién ganó.
 *
 * IMPORTANTE:
 * Esta clase NO tiene lógica del combate, solo guarda datos.
 * El controlador es el que se encarga de toda la lógica.
 */
public class Dohyo {

    // arreglo donde se guardan los dos luchadores del combate (posiciones 0 y 1)
    private final Rikishi[] luchadores;

    // indica de quién es el turno actualmente (0 o 1)
    private int turnoActual;

    // indica si el combate ya terminó
    // volatile se usa para que los hilos vean el cambio inmediatamente
    private volatile boolean combateTerminado;

    // guarda el luchador ganador (puede ser null si aún no termina)
    private Rikishi ganador;

    // bandera para saber si ya se anunció el inicio del combate
    private boolean combateAnunciado;

    /**
     * Constructor que inicializa el estado del dohyo.
     */
    public Dohyo() {
        this.luchadores       = new Rikishi[2];
        this.turnoActual      = 0;
        this.combateTerminado = false;
        this.combateAnunciado = false;
        this.ganador          = null;
    }

    // guarda un luchador en la posición indicada (0 o 1)
    public void setLuchador(Rikishi rikishi, int indice) {
        luchadores[indice] = rikishi;
    }

    // devuelve el luchador en la posición indicada
    public Rikishi getLuchador(int indice) {
        return luchadores[indice];
    }

    // verifica si ya hay dos luchadores listos en el dohyo
    public boolean ambosLuchadoresPresentes() {
        return luchadores[0] != null && luchadores[1] != null;
    }

    // devuelve de quién es el turno actual
    public int getTurnoActual() {
        return turnoActual;
    }

    // cambia el turno al luchador indicado
    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    // indica si el combate ya terminó
    public boolean isCombateTerminado() {
        return combateTerminado;
    }

    // marca si el combate terminó o no
    public void setCombateTerminado(boolean combateTerminado) {
        this.combateTerminado = combateTerminado;
    }

    // devuelve el ganador del combate
    public Rikishi getGanador() {
        return ganador;
    }

    // asigna el ganador del combate
    public void setGanador(Rikishi ganador) {
        this.ganador = ganador;
    }

    // indica si ya se anunció el inicio del combate
    public boolean isCombateAnunciado() {
        return combateAnunciado;
    }

    // marca que el combate ya fue anunciado
    public void setCombateAnunciado(boolean combateAnunciado) {
        this.combateAnunciado = combateAnunciado;
    }
}