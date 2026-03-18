package co.edu.udistrital.sumo.modelo.cliente;

/**
 * Representa una técnica ganadora del sumo japonés (kimarite).
 *
 * Propósito: Almacenar ÚNICAMENTE el nombre de una técnica como POJO puro.
 * La Asociación Japonesa de Sumo reconoce 82 kimarites oficiales.
 * Cada luchador domina un subconjunto cargado desde el archivo de propiedades.
 * Se comunica con: {@link Rikishi} (quien posee la lista de técnicas).
 * Principio SOLID:
 * S — única responsabilidad: representar el dato de una técnica de sumo.
 *
 * PROHIBIDO en esta clase: lógica de combate, Serializable, interfaz gráfica.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see Rikishi
 */
public class Kimarite {

    //Nombre oficial de la técnica en japonés
    private String nombre;

    /**
     * Construye un kimarite con su nombre oficial.
     *
     * @param nombre nombre de la técnica
     */
    public Kimarite(String nombre) {
        this.nombre = nombre;
    }

    //Retorna el nombre de esta técnica
    public String getNombre() { return nombre; }

    //Establece el nombre de esta técnica
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Representación en texto del kimarite.
     * Usado en componentes de la interfaz (JList, JComboBox).
     *
     * @return nombre de la técnica
     */
    @Override
    public String toString() { return nombre; }
}
