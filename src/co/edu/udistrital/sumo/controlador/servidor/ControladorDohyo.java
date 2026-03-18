package co.edu.udistrital.sumo.controlador.servidor;


import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.Dohyo;
import co.edu.udistrital.sumo.modelo.Kimarite;
import co.edu.udistrital.sumo.modelo.Rikishi;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ControladorCombate — Controlador de la lógica de negocio del combate de sumo.
 *
 * Propósito: Coordinar el combate entre los dos luchadores. Es el monitor de
 * sincronización compartido entre los dos {@link HiloLuchador}. Centraliza:
 * la selección aleatoria de kimarites, el cálculo del resultado de cada técnica,
 * la gestión de turnos con {@code synchronized}/{@code wait}/{@code notifyAll},
 * y la notificación de eventos a los observadores (patrón Observer).
 *
 * Se comunica con: {@link Dohyo} (estado del ring), {@link HiloLuchador} (hilos
 * que llaman sus métodos synchronized) y {@link ICombateObservador} (vista/servidor).
 * Principio SOLID:
 * S — única responsabilidad: lógica y sincronización del combate.
 * D — depende de {@link ICombateObservador} (abstracción), no de la vista concreta.
 *
 * IMPORTANTE: debe existir UNA SOLA instancia compartida entre ambos HiloLuchador.
 * Si cada hilo tuviera su propia instancia, los locks serían sobre objetos distintos
 * y la sincronización fallaría completamente.
 *
 * PROHIBIDO en esta clase: SQL, sockets, componentes gráficos Swing.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see Dohyo
 * @see HiloLuchador
 */
public class ControladorDohyo {

    /**
     * Tiempo máximo de espera por turno en milisegundos.
     * Según el enunciado del taller: máximo 500ms de espera.
     */
    public static final int MAX_ESPERA_MS = 500;

    /**
     * Probabilidad de expulsión del oponente por cada kimarite (sobre 100).
     * 20% — la mayoría de las veces no expulsa, pero en algún momento sí.
     */
    private static final int PROBABILIDAD_EXPULSION = 20;

    //Estado del ring compartido entre los dos hilos
    private final Dohyo dohyo;

    //Generador de números aleatorios para selección de kimarite y resultado
    private final Random random;

    //Lista de observadores del combate (patrón Observer)
    private final List<ICombateObservador> observadores;

    /**
     * Construye el controlador del combate con el dohyō compartido.
     * Esta instancia debe ser pasada a AMBOS HiloLuchador.
     *
     * @param dohyo instancia compartida del dohyō (estado del ring)
     */
    public ControladorDohyo(Dohyo dohyo) {
        this.dohyo       = dohyo;
        this.random      = new Random();
        this.observadores = new ArrayList<>();
    }

    //Registra un observador para recibir eventos del combate
    public synchronized void agregarObservador(ICombateObservador observador) {
        if (observador != null) {
            observadores.add(observador);
        }
    }

    /**
     * Registra al luchador en el dohyō en el slot indicado, resetea su estado
     * y notifica a los observadores de su llegada.
     * Usa notifyAll() para despertar a hilos esperando en esperarAmbosLuchadores().
     *
     * @param rikishi luchador que sube al ring
     * @param indice  posición en el dohyō (0 o 1)
     */
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        rikishi.setDentroDelDohyo(true);
        dohyo.setLuchador(rikishi, indice);
        notifyAll();
        notificarLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
    }

    /**
     * Bloquea el hilo actual hasta que ambos luchadores estén en el dohyō.
     * La asignación de rivales y el anuncio de inicio ocurren solo una vez,
     * aunque ambos hilos llamen a este método.
     *
     * @throws InterruptedException si el hilo es interrumpido durante la espera
     */
    public synchronized void esperarAmbosLuchadores() throws InterruptedException {
        while (!dohyo.ambosLuchadoresPresentes()) {
            wait();
        }
        //Solo el primer hilo en llegar anuncia el inicio
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
     * Ejecuta el turno del luchador en el índice dado.
     *
     * Flujo:
     * 1. Espera hasta que sea su turno (máximo MAX_ESPERA_MS ms).
     * 2. Selecciona un kimarite aleatorio de su repertorio.
     * 3. Calcula el resultado con probabilidad sesgada (20% expulsión).
     * 4. Si hay expulsión, marca el combate como terminado y notifica.
     * 5. Si no, cede el turno al oponente y notifica.
     *
     * @param indiceLuchador índice (0 o 1) del luchador que ejecuta el turno
     * @throws InterruptedException si el hilo es interrumpido durante la espera
     */
    public synchronized void ejecutarTurno(int indiceLuchador)
            throws InterruptedException {

        long tiempoInicio = System.currentTimeMillis();

        //Esperar hasta que sea el turno de este luchador o el combate termine
        while (dohyo.getTurnoActual() != indiceLuchador
                && !dohyo.isCombateTerminado()) {
            long transcurrido = System.currentTimeMillis() - tiempoInicio;
            long restante = MAX_ESPERA_MS - transcurrido;
            if (restante <= 0) return; //Timeout: cede el control
            wait(restante);
        }

        if (dohyo.isCombateTerminado()) return;

        Rikishi atacante = dohyo.getLuchador(indiceLuchador);

        //Seleccionar kimarite aleatorio del repertorio
        Kimarite kimarite = seleccionarKimariteAleatorio(atacante);
        if (kimarite == null) {
            //Sin técnicas: cede el turno sin atacar
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            return;
        }

        //Calcular resultado: expulsión si el número aleatorio < PROBABILIDAD_EXPULSION
        boolean expulsado = random.nextInt(100) < PROBABILIDAD_EXPULSION;

        if (expulsado) {
            //El oponente es expulsado del dohyō
            int indiceOponente = 1 - indiceLuchador;
            Rikishi oponente = dohyo.getLuchador(indiceOponente);
            oponente.setDentroDelDohyo(false);
            atacante.setCombatesGanados(atacante.getCombatesGanados() + 1);
            dohyo.setGanador(atacante);
            dohyo.setCombateTerminado(true);
            notifyAll();
            notificarKimariteEjecutado(
                atacante.getNombre(), kimarite.getNombre(), true);
            notificarCombateTerminado(
                atacante.getNombre(), atacante.getCombatesGanados());
        } else {
            //Cambiar turno al oponente
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            notificarKimariteEjecutado(
                atacante.getNombre(), kimarite.getNombre(), false);
        }
    }

    //Retorna true si el combate ya terminó
    public boolean isCombateTerminado() {
        return dohyo.isCombateTerminado();
    }

    //Retorna el luchador ganador, o null si el combate no ha terminado
    public Rikishi getGanador() {
        return dohyo.getGanador();
    }

    // ─── Lógica de selección de kimarite ─────────────────────────────────────

    /**
     * Selecciona aleatoriamente un kimarite del repertorio del luchador.
     * Genera un índice aleatorio sobre la lista de técnicas.
     *
     * @param rikishi luchador del que se selecciona la técnica
     * @return kimarite seleccionado, o null si el repertorio está vacío
     */
    private Kimarite seleccionarKimariteAleatorio(Rikishi rikishi) {
        if (rikishi.getKimarites() == null || rikishi.getKimarites().isEmpty()) {
            return null;
        }
        int indice = random.nextInt(rikishi.getKimarites().size());
        return rikishi.getKimarites().get(indice);
    }

    // ─── Notificaciones a observadores ───────────────────────────────────────

    //Notifica que un luchador llegó al dohyō
    private void notificarLuchadorLlego(String nombre, double peso, int indice) {
        for (ICombateObservador obs : observadores) {
            obs.onLuchadorLlego(nombre, peso, indice);
        }
    }

    //Notifica que el combate fue iniciado con los dos luchadores
    private void notificarCombateIniciado(String nombreL1, String nombreL2) {
        for (ICombateObservador obs : observadores) {
            obs.onCombateIniciado(nombreL1, nombreL2);
        }
    }

    //Notifica que se ejecutó un kimarite con su resultado
    private void notificarKimariteEjecutado(String nombreLuchador,
                                              String nombreKimarite,
                                              boolean expulsado) {
        for (ICombateObservador obs : observadores) {
            obs.onKimariteEjecutado(nombreLuchador, nombreKimarite, expulsado);
        }
    }

    //Notifica que el combate terminó con un ganador y sus victorias
    private void notificarCombateTerminado(String nombreGanador, int victorias) {
        for (ICombateObservador obs : observadores) {
            obs.onCombateTerminado(nombreGanador, victorias);
        }
    }
}
