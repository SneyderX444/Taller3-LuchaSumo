package co.edu.udistrital.sumo.modelo.servidor;

import co.edu.udistrital.sumo.modelo.cliente.Rikishi;

/**
 * POJO del ring de sumo (dohyo).
 *
 * Proposito: Guardar UNICAMENTE el estado del combate.
 * No contiene logica de negocio ni sincronizacion.
 * La logica del combate vive en ControladorDohyo.
 *
 * CORRECCION v2 - Ley de Demeter:
 * Se anaden metodos que operan internamente sobre los luchadores
 * para que ControladorDohyo NO llame dohyo.getLuchador(x).metodo()
 * (cadena de metodos = violacion a la Ley de Demeter).
 *
 * PROHIBIDO: logica de combate, synchronized, Random, Swing.
 *
 * @author Grupo Taller 3
 * @version 2.0
 */
public class Dohyo {

    // Arreglo con los dos luchadores (posiciones 0 y 1)
    private final Rikishi[] luchadores;

    // Indice del luchador al que le toca el turno
    private int turnoActual;

    // volatile para que los hilos vean el cambio de inmediato sin synchronized
    private volatile boolean combateTerminado;

    // Luchador que gano el combate (null mientras siga en curso)
    private Rikishi ganador;

    // Bandera para anunciar el inicio solo una vez
    private boolean combateAnunciado;

    public Dohyo() {
        this.luchadores       = new Rikishi[2];
        this.turnoActual      = 0;
        this.combateTerminado = false;
        this.combateAnunciado = false;
        this.ganador          = null;
    }

    // Getters y setters basicos
    public void setLuchador(Rikishi rikishi, int indice) { luchadores[indice] = rikishi; }
    public Rikishi getLuchador(int indice) { return luchadores[indice]; }
    public boolean ambosLuchadoresPresentes() { return luchadores[0] != null && luchadores[1] != null; }
    public int getTurnoActual() { return turnoActual; }
    public void setTurnoActual(int t) { this.turnoActual = t; }
    public boolean isCombateTerminado() { return combateTerminado; }
    public void setCombateTerminado(boolean v) { this.combateTerminado = v; }
    public Rikishi getGanador() { return ganador; }
    public void setGanador(Rikishi g) { this.ganador = g; }
    public boolean isCombateAnunciado() { return combateAnunciado; }
    public void setCombateAnunciado(boolean v) { this.combateAnunciado = v; }

    // ---- Metodos de colaboracion: eliminan violaciones a la Ley de Demeter ----

    /**
     * Expulsa al oponente, incrementa victorias del ganador y cierra el combate.
     * Evita que ControladorDohyo encadene dohyo.getLuchador(x).setDentroDelDohyo()
     * (violacion a la Ley de Demeter).
     *
     * @param indiceGanador indice (0 o 1) del luchador que gano
     */
    public void expulsarOponente(int indiceGanador) {
        int indiceOponente = 1 - indiceGanador;
        luchadores[indiceOponente].setDentroDelDohyo(false);
        luchadores[indiceGanador].setCombatesGanados(
            luchadores[indiceGanador].getCombatesGanados() + 1);
        this.ganador          = luchadores[indiceGanador];
        this.combateTerminado = true;
    }

    /**
     * Asigna los rivales entre los dos luchadores.
     * Evita dohyo.getLuchador(0).setRival(dohyo.getLuchador(1)) en el controlador.
     * Solo debe llamarse cuando ambos luchadores estan presentes.
     */
    public void asignarRivales() {
        if (ambosLuchadoresPresentes()) {
            luchadores[0].setRival(luchadores[1]);
            luchadores[1].setRival(luchadores[0]);
        }
    }

    /**
     * Nombre del luchador en la posicion indicada.
     * Evita dohyo.getLuchador(x).getNombre() en el controlador.
     */
    public String getNombreLuchador(int indice) {
        return luchadores[indice] != null ? luchadores[indice].getNombre() : "";
    }

    /**
     * Peso del luchador en la posicion indicada.
     * Evita dohyo.getLuchador(x).getPeso() en el controlador.
     */
    public double getPesoLuchador(int indice) {
        return luchadores[indice] != null ? luchadores[indice].getPeso() : 0.0;
    }

    /**
     * Nombre del ganador para notificaciones.
     * Evita dohyo.getGanador().getNombre() en el controlador.
     */
    public String getNombreGanador() {
        return ganador != null ? ganador.getNombre() : "";
    }

    /**
     * Victorias del ganador para notificaciones.
     * Evita dohyo.getGanador().getCombatesGanados() en el controlador.
     */
    public int getVictoriasGanador() {
        return ganador != null ? ganador.getCombatesGanados() : 0;
    }
}
