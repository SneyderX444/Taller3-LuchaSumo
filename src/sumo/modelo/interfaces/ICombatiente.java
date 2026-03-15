package sumo.modelo.interfaces;

import sumo.modelo.Kimarite;

/**
 * Contrato que define las capacidades de un combatiente de sumo (rikishi).
 * <p>
 * Siguiendo el principio de Segregación de Interfaces (ISP) de SOLID,
 * esta interfaz define únicamente las operaciones relacionadas con la
 * participación en un combate de sumo.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 */
public interface ICombatiente {

    /**
     * Retorna el nombre del combatiente.
     *
     * @return nombre del rikishi
     */
    String getNombre();

    /**
     * Retorna el peso del combatiente en kilogramos.
     *
     * @return peso en kg
     */
    double getPeso();

    /**
     * Retorna el número acumulado de combates ganados.
     *
     * @return cantidad de victorias
     */
    int getCombatesGanados();

    /**
     * Indica si el combatiente se encuentra dentro del dohyō.
     *
     * @return {@code true} si está dentro, {@code false} si fue expulsado
     */
    boolean isDentroDelDohyo();

    /**
     * Actualiza el estado del combatiente respecto al dohyō.
     *
     * @param dentro {@code true} para colocarlo dentro, {@code false} para expulsarlo
     */
    void setDentroDelDohyo(boolean dentro);

    /**
     * Selecciona aleatoriamente una técnica (kimarite) del repertorio del combatiente.
     *
     * @return kimarite seleccionado aleatoriamente, o {@code null} si no tiene técnicas
     */
    Kimarite seleccionarKimariteAleatorio();

    /**
     * Incrementa en uno el contador de victorias del combatiente.
     */
    void incrementarVictorias();

    /**
     * Restablece el estado del combatiente al inicio de un combate.
     * Coloca al luchador dentro del dohyō nuevamente.
     */
    void resetearEstado();
}
