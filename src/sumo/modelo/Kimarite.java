package sumo.modelo;

import java.io.Serializable;

/**
 * Representa una técnica ganadora del sumo japonés (kimarite).
 * <p>
 * La Asociación Japonesa de Sumo reconoce actualmente 82 kimarites oficiales.
 * Cada luchador domina un subconjunto de estas técnicas, cargadas desde
 * un archivo de propiedades externo.
 * </p>
 *
 * <p>
 * Principio de Responsabilidad Única (SRP): esta clase modela exclusivamente
 * los datos de una técnica de sumo, sin lógica de combate ni de interfaz.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see sumo.modelo.Rikishi
 */
public class Kimarite implements Serializable {

    /** Identificador de versión para serialización. */
    private static final long serialVersionUID = 1L;

    /** Nombre oficial de la técnica en japonés o español. */
    private String nombre;

    /** Descripción breve de la ejecución de la técnica. */
    private String descripcion;

    /**
     * Construye un kimarite con nombre y descripción.
     *
     * @param nombre      nombre oficial de la técnica
     * @param descripcion descripción de cómo se ejecuta
     */
    public Kimarite(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Construye un kimarite únicamente con nombre.
     * La descripción queda vacía.
     *
     * @param nombre nombre oficial de la técnica
     */
    public Kimarite(String nombre) {
        this(nombre, "");
    }

    /**
     * Retorna el nombre de esta técnica.
     *
     * @return nombre del kimarite
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de esta técnica.
     *
     * @param nombre nuevo nombre del kimarite
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Retorna la descripción de esta técnica.
     *
     * @return descripción del kimarite
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción de esta técnica.
     *
     * @param descripcion nueva descripción
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna la representación en cadena de este kimarite.
     * Utilizado en componentes de la interfaz (JList, JComboBox, etc.)
     *
     * @return nombre de la técnica
     */
    @Override
    public String toString() {
        return nombre;
    }
}
