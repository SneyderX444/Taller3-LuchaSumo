package co.edu.udistrital.sumo.modelo.cliente;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un luchador de sumo (rikishi).
 *
 * Propósito: Almacenar ÚNICAMENTE los datos de un luchador como POJO puro.
 * No contiene lógica de negocio, sincronización ni aleatoriedad.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorCombate}
 * (quien maneja la lógica del combate).
 * Principio SOLID:
 * S — única responsabilidad: representar los datos de un rikishi.
 *
 * PROHIBIDO en esta clase: Random, synchronized, lógica de negocio, validaciones.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see Kimarite
 * @see Dohyo
 */
public class Rikishi {

    //Nombre del luchador
    private String nombre;

    //Peso del luchador en kilogramos
    private double peso;

    //Contador de combates ganados
    private int combatesGanados;

    //Lista de técnicas (kimarites) que domina el luchador
    private List<Kimarite> kimarites;

    //Estado del luchador: true si está dentro del dohyō, false si fue expulsado
    //volatile garantiza visibilidad entre hilos sin necesidad de synchronized
    private volatile boolean dentroDelDohyo;

    //Rival asignado para el combate actual
    private Rikishi rival;

    /**
     * Construye un nuevo Rikishi con nombre y peso.
     *
     * @param nombre nombre del luchador
     * @param peso   peso del luchador en kg
     */
    public Rikishi(String nombre, double peso) {
        this.nombre          = nombre;
        this.peso            = peso;
        this.combatesGanados = 0;
        this.kimarites       = new ArrayList<>();
        this.dentroDelDohyo  = true;
        this.rival           = null;
    }

    //Retorna el nombre del luchador
    public String getNombre() { return nombre; }

    //Establece el nombre del luchador
    public void setNombre(String nombre) { this.nombre = nombre; }

    //Retorna el peso del luchador en kg
    public double getPeso() { return peso; }

    //Establece el peso del luchador
    public void setPeso(double peso) { this.peso = peso; }

    //Retorna el número de combates ganados
    public int getCombatesGanados() { return combatesGanados; }

    //Establece el número de combates ganados
    public void setCombatesGanados(int combatesGanados) {
        this.combatesGanados = combatesGanados;
    }

    //Retorna la lista de kimarites que domina este luchador
    public List<Kimarite> getKimarites() { return kimarites; }

    //Establece el repertorio completo de kimarites del luchador
    public void setKimarites(List<Kimarite> kimarites) {
        this.kimarites = kimarites;
    }

    //Retorna true si el luchador está dentro del dohyō
    public boolean isDentroDelDohyo() { return dentroDelDohyo; }

    //Establece si el luchador está dentro o fuera del dohyō
    public void setDentroDelDohyo(boolean dentroDelDohyo) {
        this.dentroDelDohyo = dentroDelDohyo;
    }

    //Retorna el rival asignado para el combate actual
    public Rikishi getRival() { return rival; }

    //Asigna el rival para el combate actual
    public void setRival(Rikishi rival) { this.rival = rival; }

    /**
     * Representación en texto del rikishi.
     * Usado en componentes de la interfaz (combos, listas, logs).
     *
     * @return cadena con nombre, peso y victorias
     */
    @Override
    public String toString() {
        return String.format("Rikishi{nombre='%s', peso=%.1f kg, victorias=%d}",
                nombre, peso, combatesGanados);
    }
}
