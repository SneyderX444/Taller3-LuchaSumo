package co.edu.udistrital.sumo.controlador.servidor;

import co.edu.udistrital.sumo.modelo.interfaces.IArbitro;
import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.cliente.Kimarite;
import co.edu.udistrital.sumo.modelo.cliente.Rikishi;
import co.edu.udistrital.sumo.modelo.servidor.Dohyo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controlador del combate: logica, turnos y sincronizacion.
 *
 * Implementa IArbitro (DIP de SOLID): HiloLuchador depende de la
 * abstraccion IArbitro, no de esta clase concreta.
 *
 * Es el monitor de sincronizacion compartido entre los dos HiloLuchador:
 * todos los metodos publicos son synchronized, garantizando que solo
 * un hilo modifique el dohyo a la vez.
 *
 * CORRECCION v2 - Ley de Demeter:
 * Ya no se encadenan llamadas sobre objetos retornados por dohyo.
 * En cambio, se usan los metodos de colaboracion de Dohyo:
 *   dohyo.expulsarOponente(indice)   en lugar de dohyo.getLuchador(x).setDentroDelDohyo()
 *   dohyo.asignarRivales()           en lugar de dohyo.getLuchador(0).setRival(...)
 *   dohyo.getNombreLuchador(x)       en lugar de dohyo.getLuchador(x).getNombre()
 *   dohyo.getNombreGanador()         en lugar de dohyo.getGanador().getNombre()
 *   dohyo.getVictoriasGanador()      en lugar de dohyo.getGanador().getCombatesGanados()
 *
 * IMPORTANTE: una sola instancia compartida entre ambos HiloLuchador.
 *
 * PROHIBIDO: sockets, SQL, componentes Swing.
 *
 * @author Grupo Taller 3
 * @version 2.0
 */
public class ControladorDohyo implements IArbitro {

    // Tiempo maximo de espera por turno en ms (segun enunciado: maximo 500ms)
    public static final int MAX_ESPERA_MS = 500;

    // Probabilidad de expulsion por kimarite (10%)
    private static final int PROBABILIDAD_EXPULSION = 10;

    // Estado puro del ring
    private final Dohyo dohyo;

    // Generador de numeros aleatorios
    private final Random random;

    // Observadores del combate (patron Observer)
    private final List<ICombateObservador> observadores;

    /**
     * Crea el controlador del combate con el dohyo compartido.
     * Esta instancia DEBE pasarse a ambos HiloLuchador.
     *
     * @param dohyo estado compartido del ring
     */
    public ControladorDohyo(Dohyo dohyo) {
        this.dohyo        = dohyo;
        this.random       = new Random();
        this.observadores = new ArrayList<>();
    }

    // Registra un observador para recibir eventos del combate
    public synchronized void agregarObservador(ICombateObservador obs) {
        if (obs != null) observadores.add(obs);
    }

    /**
     * {@inheritDoc}
     * Registra al luchador, lo marca como presente y notifica a observadores.
     * notifyAll despierta al hilo esperando en esperarAmbosLuchadores().
     */
    @Override
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        rikishi.setDentroDelDohyo(true);
        dohyo.setLuchador(rikishi, indice);
        notifyAll();
        // Ley de Demeter: usamos getNombreLuchador y getPesoLuchador de Dohyo
        notificarLuchadorLlego(
            dohyo.getNombreLuchador(indice),
            dohyo.getPesoLuchador(indice),
            indice);
    }

    /**
     * {@inheritDoc}
     * Bloquea hasta que ambos luchadores esten en el dohyo.
     * La asignacion de rivales y el anuncio ocurren solo una vez.
     */
    @Override
    public synchronized void esperarAmbosLuchadores() throws InterruptedException {
        while (!dohyo.ambosLuchadoresPresentes()) {
            wait();
        }
        if (!dohyo.isCombateAnunciado()) {
            dohyo.setCombateAnunciado(true);
            // Ley de Demeter: asignarRivales() en lugar de getLuchador(0).setRival(getLuchador(1))
            dohyo.asignarRivales();
            notificarCombateIniciado(
                dohyo.getNombreLuchador(0),
                dohyo.getNombreLuchador(1));
        }
    }

    /**
     * {@inheritDoc}
     * Ejecuta el turno: espera, selecciona kimarite, calcula resultado y notifica.
     */
    @Override
    public synchronized void ejecutarTurno(int indiceLuchador)
            throws InterruptedException {

        long inicio = System.currentTimeMillis();

        while (dohyo.getTurnoActual() != indiceLuchador
                && !dohyo.isCombateTerminado()) {
            long restante = MAX_ESPERA_MS - (System.currentTimeMillis() - inicio);
            if (restante <= 0) return;
            wait(restante);
        }

        if (dohyo.isCombateTerminado()) return;

        // getLuchador es aceptable aqui: necesitamos el objeto para pasar a seleccionarKimarite
        Rikishi atacante = dohyo.getLuchador(indiceLuchador);

        // Null-check defensivo (SOLID - robustez)
        if (atacante == null) return;

        Kimarite kimarite = seleccionarKimariteAleatorio(atacante);
        if (kimarite == null) {
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            return;
        }

        boolean expulsado = random.nextInt(100) < PROBABILIDAD_EXPULSION;

        if (expulsado) {
            // Ley de Demeter: expulsarOponente() en lugar de getLuchador(x).setDentroDelDohyo()
            // y en lugar de getGanador().getCombatesGanados()
            dohyo.expulsarOponente(indiceLuchador);
            notifyAll();
            notificarKimariteEjecutado(
                dohyo.getNombreGanador(),
                kimarite.getNombre(),
                true);
            // Ley de Demeter: getNombreGanador() y getVictoriasGanador() de Dohyo
            notificarCombateTerminado(
                dohyo.getNombreGanador(),
                dohyo.getVictoriasGanador());
        } else {
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            // Ley de Demeter: getNombreLuchador en lugar de getLuchador(x).getNombre()
            notificarKimariteEjecutado(
                dohyo.getNombreLuchador(indiceLuchador),
                kimarite.getNombre(),
                false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isCombateTerminado() {
        return dohyo.isCombateTerminado();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Rikishi getGanador() {
        return dohyo.getGanador();
    }

    // Selecciona un kimarite aleatorio del repertorio del luchador
    private Kimarite seleccionarKimariteAleatorio(Rikishi rikishi) {
        // Null-check defensivo: evita NPE si rikishi o su lista son null
        if (rikishi == null
                || rikishi.getKimarites() == null
                || rikishi.getKimarites().isEmpty()) {
            return null;
        }
        return rikishi.getKimarites().get(random.nextInt(rikishi.getKimarites().size()));
    }

    // Notificaciones a observadores
    private void notificarLuchadorLlego(String nombre, double peso, int indice) {
        for (ICombateObservador obs : observadores) obs.onLuchadorLlego(nombre, peso, indice);
    }
    private void notificarCombateIniciado(String n1, String n2) {
        for (ICombateObservador obs : observadores) obs.onCombateIniciado(n1, n2);
    }
    private void notificarKimariteEjecutado(String luchador, String kimarite, boolean exp) {
        for (ICombateObservador obs : observadores) obs.onKimariteEjecutado(luchador, kimarite, exp);
    }
    private void notificarCombateTerminado(String ganador, int victorias) {
        for (ICombateObservador obs : observadores) obs.onCombateTerminado(ganador, victorias);
    }
}
