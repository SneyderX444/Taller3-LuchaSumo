package sumo.modelo;

import sumo.modelo.interfaces.ICombatiente;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa un luchador de sumo (rikishi).
 * <p>
 * Un rikishi tiene nombre, peso, victorias acumuladas, un arreglo de técnicas
 * (kimarites) que domina, un rival asignado para el combate actual, y un estado
 * que indica si se encuentra dentro o fuera del dohyō.
 * </p>
 *
 * <p>
 * Implementa {@link ICombatiente} siguiendo el principio de Inversión de
 * Dependencias (DIP), permitiendo que el resto del sistema dependa de la
 * abstracción, no de esta clase concreta.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see Kimarite
 * @see Dohyo
 */
public class Rikishi implements ICombatiente, Serializable {

    /** Identificador de versión para serialización. */
    private static final long serialVersionUID = 1L;

    /** Nombre del luchador. */
    private String nombre;

    /** Peso del luchador en kilogramos. */
    private double peso;

    /** Contador de combates ganados. */
    private int combatesGanados;

    /** Arreglo de técnicas (kimarites) que domina el luchador. */
    private List<Kimarite> kimarites;

    /** Estado del luchador: {@code true} si está dentro del dohyō, {@code false} si fue expulsado. */
    private volatile boolean dentroDelDohyo;

    /**
     * Rival asignado para el combate actual.
     * Se marca {@code transient} para evitar ciclos de serialización.
     */
    private transient Rikishi rival;

    /** Generador de números aleatorios para selección de técnicas. */
    private final transient Random random;

    /**
     * Construye un nuevo Rikishi con nombre y peso.
     *
     * @param nombre nombre del luchador
     * @param peso   peso del luchador en kg
     */
    public Rikishi(String nombre, double peso) {
        this.nombre = nombre;
        this.peso = peso;
        this.combatesGanados = 0;
        this.kimarites = new ArrayList<>();
        this.dentroDelDohyo = true;
        this.random = new Random();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Selecciona aleatoriamente una técnica del repertorio del luchador
     * generando un índice aleatorio sobre el arreglo de kimarites.
     * </p>
     *
     * @return kimarite seleccionado aleatoriamente, o {@code null} si el repertorio está vacío
     */
    @Override
    public Kimarite seleccionarKimariteAleatorio() {
        if (kimarites == null || kimarites.isEmpty()) {
            return null;
        }
        int indice = random.nextInt(kimarites.size());
        return kimarites.get(indice);
    }

    /** {@inheritDoc} */
    @Override
    public void incrementarVictorias() {
        this.combatesGanados++;
    }

    /** {@inheritDoc} */
    @Override
    public void resetearEstado() {
        this.dentroDelDohyo = true;
    }

    /**
     * Agrega un kimarite al repertorio de técnicas del luchador.
     *
     * @param kimarite técnica a agregar
     */
    public void agregarKimarite(Kimarite kimarite) {
        if (kimarite != null) {
            this.kimarites.add(kimarite);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del luchador.
     *
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** {@inheritDoc} */
    @Override
    public double getPeso() {
        return peso;
    }

    /**
     * Establece el peso del luchador.
     *
     * @param peso nuevo peso en kg
     */
    public void setPeso(double peso) {
        this.peso = peso;
    }

    /** {@inheritDoc} */
    @Override
    public int getCombatesGanados() {
        return combatesGanados;
    }

    /**
     * Establece el número de combates ganados (usado al recibir datos por socket).
     *
     * @param combatesGanados nuevo conteo de victorias
     */
    public void setCombatesGanados(int combatesGanados) {
        this.combatesGanados = combatesGanados;
    }

    /**
     * Retorna la lista de kimarites que domina este luchador.
     *
     * @return lista de técnicas
     */
    public List<Kimarite> getKimarites() {
        return kimarites;
    }

    /**
     * Establece el repertorio completo de kimarites del luchador.
     *
     * @param kimarites nueva lista de técnicas
     */
    public void setKimarites(List<Kimarite> kimarites) {
        this.kimarites = kimarites;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDentroDelDohyo() {
        return dentroDelDohyo;
    }

    /** {@inheritDoc} */
    @Override
    public void setDentroDelDohyo(boolean dentroDelDohyo) {
        this.dentroDelDohyo = dentroDelDohyo;
    }

    /**
     * Retorna el rival asignado para el combate actual.
     *
     * @return el Rikishi rival, o {@code null} si no se ha asignado
     */
    public Rikishi getRival() {
        return rival;
    }

    /**
     * Asigna un rival para el combate actual.
     *
     * @param rival el luchador oponente
     */
    public void setRival(Rikishi rival) {
        this.rival = rival;
    }

    /**
     * Retorna una representación legible del estado del rikishi.
     *
     * @return cadena con nombre, peso, victorias y estado en el dohyō
     */
    @Override
    public String toString() {
        return String.format("Rikishi{nombre='%s', peso=%.1f kg, victorias=%d, dentroDelDohyo=%b}",
                nombre, peso, combatesGanados, dentroDelDohyo);
    }
}
