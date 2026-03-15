package sumo.modelo;

import sumo.modelo.interfaces.IArbitro;
import sumo.controlador.interfaces.ICombateObservador;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa el ring sagrado del sumo (dohyō) y actúa como monitor de sincronización
 * entre los dos hilos de luchadores.
 * <p>
 * El dohyō coordina los turnos de combate usando mecanismos de concurrencia Java
 * ({@code synchronized}, {@code wait()}, {@code notifyAll()}). Asegura que:
 * <ul>
 *   <li>Cada luchador espera a que sea su turno antes de ejecutar un kimarite.</li>
 *   <li>El tiempo máximo de espera por turno es {@value #MAX_ESPERA_MS} ms.</li>
 *   <li>El combate termina cuando un luchador expulsa al oponente del dohyō.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Implementa {@link IArbitro} siguiendo el principio de Inversión de Dependencias (DIP).
 * Los observadores {@link ICombateObservador} son notificados de cada evento del combate.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see Rikishi
 * @see Kimarite
 */
public class Dohyo implements IArbitro {

    /**
     * Tiempo máximo de espera en milisegundos para que el oponente lance su kimarite.
     * Conforme al enunciado del taller.
     */
    public static final int MAX_ESPERA_MS = 500;

    /**
     * Probabilidad de expulsión del oponente por cada kimarite (sobre 100).
     * Se fija en 20 para que la mayoría de los kimarites no expulsen al oponente,
     * pero en algún momento (con menor probabilidad) sí lo haga.
     */
    private static final int PROBABILIDAD_EXPULSION = 20;

    /** Arreglo con los dos luchadores que participan en el combate. */
    private final Rikishi[] luchadores;

    /** Índice del luchador que tiene el turno actual (0 o 1). */
    private int turnoActual;

    /** Indica si el combate ha finalizado. */
    private boolean combateTerminado;

    /** Luchador ganador del combate, o {@code null} si aún no ha terminado. */
    private Rikishi ganador;

    /** Último kimarite ejecutado en el combate, para notificación de observadores. */
    private Kimarite ultimoKimarite;

    /** Luchador que ejecutó el último kimarite. */
    private Rikishi ultimoAtacante;

    /** Indica si el último kimarite resultó en expulsión. */
    private boolean ultimaExpulsion;

    /** Generador de números aleatorios para determinar el resultado de cada kimarite. */
    private final Random random;

    /** Lista de observadores del combate (patrón Observer). */
    private final List<ICombateObservador> observadores;

    /**
     * Construye un nuevo Dohyō listo para recibir luchadores.
     * Inicializa el estado del combate y el generador aleatorio.
     */
    public Dohyo() {
        this.luchadores = new Rikishi[2];
        this.turnoActual = 0;
        this.combateTerminado = false;
        this.ganador = null;
        this.random = new Random();
        this.observadores = new ArrayList<>();
    }

    /**
     * Registra un observador para recibir eventos del combate.
     * Siguiendo el patrón Observer para desacoplar modelo de controlador/vista.
     *
     * @param observador observador a registrar
     */
    public synchronized void agregarObservador(ICombateObservador observador) {
        if (observador != null) {
            observadores.add(observador);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Registra al luchador en el slot indicado, resetea su estado y notifica
     * a los observadores. Usa {@code notifyAll()} para despertar a hilos que
     * esperan en {@link #esperarAmbosLuchadores()}.
     * </p>
     */
    @Override
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        luchadores[indice] = rikishi;
        rikishi.resetearEstado();
        notifyAll();
        notificarLuchadorLlego(rikishi, indice);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Bloquea el hilo hasta que ambos luchadores estén presentes en el dohyō.
     * Una vez ambos están listos, se asignan mutuamente como rivales y se
     * notifica a los observadores el inicio del combate.
     * </p>
     */
    @Override
    public synchronized void esperarAmbosLuchadores() throws InterruptedException {
        while (luchadores[0] == null || luchadores[1] == null) {
            wait();
        }
        luchadores[0].setRival(luchadores[1]);
        luchadores[1].setRival(luchadores[0]);
        notificarCombateIniciado();
    }

    /**
     * {@inheritDoc}
     * <p>
     * El luchador en {@code indiceLuchador} espera su turno (máximo {@value #MAX_ESPERA_MS} ms).
     * Al obtenerlo, selecciona un kimarite aleatorio, calcula el resultado con probabilidad
     * aleatoria sesgada, y notifica a los observadores. Si hay expulsión, el combate termina.
     * </p>
     *
     * @param indiceLuchador índice (0 o 1) del luchador que ejecuta el turno
     * @throws InterruptedException si el hilo es interrumpido esperando su turno
     */
    @Override
    public synchronized void ejecutarTurno(int indiceLuchador) throws InterruptedException {
        long tiempoInicio = System.currentTimeMillis();

        // Esperar hasta que sea el turno de este luchador o el combate termine
        while (turnoActual != indiceLuchador && !combateTerminado) {
            long transcurrido = System.currentTimeMillis() - tiempoInicio;
            long restante = MAX_ESPERA_MS - transcurrido;
            if (restante <= 0) {
                return; // Timeout: cede el control
            }
            wait(restante);
        }

        if (combateTerminado) {
            return;
        }

        // Seleccionar kimarite aleatorio del repertorio del luchador
        Rikishi atacante = luchadores[indiceLuchador];
        Kimarite kimarite = atacante.seleccionarKimariteAleatorio();

        if (kimarite == null) {
            // Sin técnicas: no puede atacar, cede el turno
            turnoActual = 1 - indiceLuchador;
            notifyAll();
            return;
        }

        // Determinar resultado: número aleatorio 0-99, expulsión si < PROBABILIDAD_EXPULSION
        int resultado = random.nextInt(100);
        boolean expulsado = resultado < PROBABILIDAD_EXPULSION;

        this.ultimoKimarite = kimarite;
        this.ultimoAtacante = atacante;
        this.ultimaExpulsion = expulsado;

        if (expulsado) {
            // El oponente es expulsado del dohyō
            int indiceOponente = 1 - indiceLuchador;
            luchadores[indiceOponente].setDentroDelDohyo(false);
            ganador = atacante;
            ganador.incrementarVictorias();
            combateTerminado = true;
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite.getNombre(), true);
            notificarCombateTerminado();
        } else {
            // Cambiar turno al oponente
            turnoActual = 1 - indiceLuchador;
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite.getNombre(), false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isCombateTerminado() {
        return combateTerminado;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Rikishi getGanador() {
        return ganador;
    }

    /**
     * Retorna el luchador en el índice especificado.
     *
     * @param indice posición (0 o 1) en el dohyō
     * @return el Rikishi en esa posición, o {@code null} si aún no llegó
     */
    public Rikishi getLuchador(int indice) {
        return luchadores[indice];
    }

    /**
     * Retorna el índice del turno actual.
     *
     * @return 0 si es el turno del primer luchador, 1 si es del segundo
     */
    public synchronized int getTurnoActual() {
        return turnoActual;
    }

    // ─── Métodos privados de notificación ──────────────────────────────────────

    /**
     * Notifica a todos los observadores que un luchador llegó al dohyō.
     *
     * @param rikishi luchador que llegó
     * @param indice  slot que ocupa (0 o 1)
     */
    private void notificarLuchadorLlego(Rikishi rikishi, int indice) {
        for (ICombateObservador obs : observadores) {
            obs.onLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
        }
    }

    /**
     * Notifica a todos los observadores que el combate comenzó.
     */
    private void notificarCombateIniciado() {
        String n0 = luchadores[0].getNombre();
        String n1 = luchadores[1].getNombre();
        for (ICombateObservador obs : observadores) {
            obs.onCombateIniciado(n0, n1);
        }
    }

    /**
     * Notifica a todos los observadores que se ejecutó un kimarite.
     *
     * @param nombreLuchador nombre del atacante
     * @param nombreKimarite nombre de la técnica usada
     * @param expulsado      {@code true} si el oponente fue expulsado
     */
    private void notificarKimariteEjecutado(String nombreLuchador,
                                             String nombreKimarite,
                                             boolean expulsado) {
        for (ICombateObservador obs : observadores) {
            obs.onKimariteEjecutado(nombreLuchador, nombreKimarite, expulsado);
        }
    }

    /**
     * Notifica a todos los observadores que el combate terminó con un ganador.
     */
    private void notificarCombateTerminado() {
        for (ICombateObservador obs : observadores) {
            obs.onCombateTerminado(ganador.getNombre(), ganador.getCombatesGanados());
        }
    }
}
