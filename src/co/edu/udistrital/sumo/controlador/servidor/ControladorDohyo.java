package co.edu.udistrital.sumo.controlador.servidor;

import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.cliente.Kimarite;
import co.edu.udistrital.sumo.modelo.cliente.Rikishi;
import co.edu.udistrital.sumo.modelo.servidor.Dohyo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controlador del combate de sumo: lógica, turnos y sincronización.
 *
 * <p>
 * Esta clase es el corazón del combate. Es el <b>monitor de sincronización</b>
 * compartido entre los dos {@link HiloLuchador}: todos sus métodos públicos
 * son {@code synchronized}, lo que garantiza que solo un hilo pueda modificar
 * el estado del dohyō a la vez.
 * </p>
 *
 * <p>
 * Centraliza tres responsabilidades del combate:
 * <ul>
 *   <li>Selección aleatoria de kimarites del repertorio de cada luchador.</li>
 *   <li>Cálculo del resultado de cada técnica con probabilidad sesgada (15%).</li>
 *   <li>Gestión de turnos con {@code wait()} y {@code notifyAll()}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Los eventos del combate se notifican a los observadores registrados
 * (patrón Observer), lo que permite que {@link ControladorServidor}
 * actualice la vista sin que esta clase la conozca directamente.
 * </p>
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li>S — solo maneja la lógica y sincronización del combate.</li>
 *   <li>D — notifica a través de {@link ICombateObservador} (interfaz), no a la vista concreta.</li>
 * </ul>
 *
 * <p><b>¡Importante!</b> Debe existir UNA SOLA instancia compartida entre ambos
 * {@link HiloLuchador}. Si cada hilo tuviera la suya, los locks serían sobre
 * objetos distintos y la sincronización no funcionaría.</p>
 *
 * <p><b>Restricciones:</b> esta clase no debe tener sockets, SQL ni componentes Swing.</p>
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see Dohyo
 * @see HiloLuchador
 * @see ICombateObservador
 */
public class ControladorDohyo {

    /**
     * Tiempo máximo que un luchador espera su turno (en milisegundos).
     * Según el enunciado del taller: máximo 500 ms de espera entre turnos.
     */
    public static final int MAX_ESPERA_MS = 500;

    /**
     * Probabilidad de que un kimarite expulse al oponente del dohyō (sobre 100).
     * Con 15%, la mayoría de los ataques no terminan el combate de inmediato,
     * pero en algún momento sí ocurre la expulsión.
     */
    private static final int PROBABILIDAD_EXPULSION = 15;

    /** Estado del ring: luchadores, turno actual, ganador y banderas de control. */
    private final Dohyo dohyo;

    /** Generador de números aleatorios para seleccionar kimarites y calcular resultados. */
    private final Random random;

    /** Lista de observadores que reciben notificaciones de los eventos del combate. */
    private final List<ICombateObservador> observadores;

    /**
     * Crea el controlador del combate con el dohyō compartido.
     * Esta instancia se debe pasar a <b>ambos</b> {@link HiloLuchador}.
     *
     * @param dohyo estado compartido del ring de sumo
     */
    public ControladorDohyo(Dohyo dohyo) {
        this.dohyo        = dohyo;
        this.random       = new Random();
        this.observadores = new ArrayList<>();
    }

    /**
     * Registra un objeto que quiere recibir notificaciones de los eventos del combate.
     * Normalmente el único observador es el {@link ControladorServidor}.
     *
     * @param observador objeto que implementa {@link ICombateObservador}
     */
    public synchronized void agregarObservador(ICombateObservador observador) {
        if (observador != null) {
            observadores.add(observador);
        }
    }

    /**
     * Sube un luchador al dohyō en la posición indicada.
     *
     * <p>Coloca al luchador en el slot correspondiente del dohyō,
     * lo marca como "dentro" y notifica a los observadores de su llegada.
     * Llama a {@code notifyAll()} para despertar al hilo que espera
     * en {@link #esperarAmbosLuchadores()}.</p>
     *
     * @param rikishi luchador que entra al ring
     * @param indice  posición en el dohyō (0 = primero, 1 = segundo)
     */
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        rikishi.setDentroDelDohyo(true);
        dohyo.setLuchador(rikishi, indice);
        notifyAll();
        notificarLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
    }

    /**
     * Bloquea el hilo actual hasta que los dos luchadores estén en el dohyō.
     *
     * <p>La asignación de rivales y el anuncio del inicio del combate
     * ocurren una sola vez gracias a la bandera {@code combateAnunciado}
     * del dohyō, incluso si los dos hilos llegan aquí casi al mismo tiempo.</p>
     *
     * @throws InterruptedException si el hilo es interrumpido mientras espera
     */
    public synchronized void esperarAmbosLuchadores() throws InterruptedException {
        while (!dohyo.ambosLuchadoresPresentes()) {
            wait();
        }
        // Solo el primer hilo en pasar por aquí anuncia el inicio
        if (!dohyo.isCombateAnunciado()) {
            dohyo.setCombateAnunciado(true);
            dohyo.getLuchador(0).setRival(dohyo.getLuchador(1));
            dohyo.getLuchador(1).setRival(dohyo.getLuchador(0));
            notificarCombateIniciado(
                dohyo.getLuchador(0).getNombre(),
                dohyo.getLuchador(1).getNombre());
        }
    }

    /**
     * Ejecuta el turno del luchador indicado.
     *
     * <p>Flujo del turno:</p>
     * <ol>
     *   <li>Si no es el turno de este luchador, espera hasta {@value #MAX_ESPERA_MS} ms.</li>
     *   <li>Selecciona un kimarite aleatorio de su repertorio.</li>
     *   <li>Calcula si hay expulsión (probabilidad {@value #PROBABILIDAD_EXPULSION}%).</li>
     *   <li>Si hay expulsión: marca el combate como terminado y notifica.</li>
     *   <li>Si no: cede el turno al oponente y notifica.</li>
     * </ol>
     *
     * @param indiceLuchador índice (0 o 1) del luchador que ataca
     * @throws InterruptedException si el hilo es interrumpido mientras espera el turno
     */
    public synchronized void ejecutarTurno(int indiceLuchador)
            throws InterruptedException {

        long tiempoInicio = System.currentTimeMillis();

        // Esperar hasta que sea el turno de este luchador o el combate ya terminó
        while (dohyo.getTurnoActual() != indiceLuchador
                && !dohyo.isCombateTerminado()) {
            long transcurrido = System.currentTimeMillis() - tiempoInicio;
            long restante = MAX_ESPERA_MS - transcurrido;
            if (restante <= 0) return; // Se agotó el tiempo: ceder el control
            wait(restante);
        }

        if (dohyo.isCombateTerminado()) return;

        Rikishi atacante = dohyo.getLuchador(indiceLuchador);

        // Elegir una técnica aleatoria del repertorio del luchador
        Kimarite kimarite = seleccionarKimariteAleatorio(atacante);
        if (kimarite == null) {
            // El luchador no tiene técnicas: ceder el turno sin atacar
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            return;
        }

        // Número aleatorio entre 0 y 99 — expulsión si cae por debajo del umbral
        boolean expulsado = random.nextInt(100) < PROBABILIDAD_EXPULSION;

        if (expulsado) {
            // El oponente sale del dohyō: el atacante gana
            int indiceOponente = 1 - indiceLuchador;
            Rikishi oponente = dohyo.getLuchador(indiceOponente);
            oponente.setDentroDelDohyo(false);
            atacante.setCombatesGanados(atacante.getCombatesGanados() + 1);
            dohyo.setGanador(atacante);
            dohyo.setCombateTerminado(true);
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite.getNombre(), true);
            notificarCombateTerminado(atacante.getNombre(), atacante.getCombatesGanados());
        } else {
            // El oponente resiste: cambiar el turno
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite.getNombre(), false);
        }
    }

    /**
     * Indica si el combate ya terminó con un ganador.
     *
     * @return {@code true} si el combate terminó
     */
    public synchronized boolean isCombateTerminado() {
        return dohyo.isCombateTerminado();
    }

    /**
     * Retorna el luchador que ganó el combate.
     *
     * @return el {@link Rikishi} ganador, o {@code null} si el combate aún no terminó
     */
    public synchronized Rikishi getGanador() {
        return dohyo.getGanador();
    }

    // ── Lógica de selección de kimarite ───────────────────────────────────────

    /**
     * Elige aleatoriamente un kimarite del repertorio del luchador.
     *
     * @param rikishi luchador del que se selecciona la técnica
     * @return kimarite elegido, o {@code null} si el luchador no tiene técnicas
     */
    private Kimarite seleccionarKimariteAleatorio(Rikishi rikishi) {
        if (rikishi.getKimarites() == null || rikishi.getKimarites().isEmpty()) {
            return null;
        }
        int indice = random.nextInt(rikishi.getKimarites().size());
        return rikishi.getKimarites().get(indice);
    }

    // ── Notificaciones a observadores ─────────────────────────────────────────

    /** Avisa a los observadores que un luchador llegó al dohyō. */
    private void notificarLuchadorLlego(String nombre, double peso, int indice) {
        for (ICombateObservador obs : observadores) {
            obs.onLuchadorLlego(nombre, peso, indice);
        }
    }

    /** Avisa a los observadores que el combate comenzó. */
    private void notificarCombateIniciado(String nombreL1, String nombreL2) {
        for (ICombateObservador obs : observadores) {
            obs.onCombateIniciado(nombreL1, nombreL2);
        }
    }

    /** Avisa a los observadores el resultado de un kimarite. */
    private void notificarKimariteEjecutado(String nombreLuchador,
                                              String nombreKimarite,
                                              boolean expulsado) {
        for (ICombateObservador obs : observadores) {
            obs.onKimariteEjecutado(nombreLuchador, nombreKimarite, expulsado);
        }
    }

    /** Avisa a los observadores que el combate terminó con un ganador. */
    private void notificarCombateTerminado(String nombreGanador, int victorias) {
        for (ICombateObservador obs : observadores) {
            obs.onCombateTerminado(nombreGanador, victorias);
        }
    }
}