package sumo.controlador;

import sumo.controlador.interfaces.ICombateObservador;
import sumo.modelo.Dohyo;
import sumo.vista.servidor.VistaServidor;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controlador del lado del servidor en la arquitectura MVC del Combate de Sumo.
 * <p>
 * Gestiona el ciclo de vida completo del servidor:
 * <ol>
 *   <li>Inicia el {@link ServerSocket} en el puerto configurado.</li>
 *   <li>Acepta exactamente dos conexiones de clientes (luchadores).</li>
 *   <li>Crea un {@link HiloLuchador} por cada cliente.</li>
 *   <li>Coordina la visualización del combate a través de la {@link VistaServidor}.</li>
 *   <li>Implementa {@link ICombateObservador} para recibir eventos del {@link Dohyo}.</li>
 * </ol>
 * </p>
 *
 * <p>
 * Principio de Inversión de Dependencias (DIP): depende de la abstracción
 * {@link ICombateObservador}, no de una implementación concreta de observador.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see Dohyo
 * @see HiloLuchador
 * @see VistaServidor
 */
public class ControladorServidor implements ICombateObservador {

    /** Puerto en el que el servidor escucha conexiones entrantes. */
    private static final int PUERTO = 9999;

    /** Número máximo de luchadores (conexiones) que acepta el servidor. */
    private static final int MAX_LUCHADORES = 2;

    /** Vista del servidor que muestra el desarrollo del combate. */
    private final VistaServidor vista;

    /** El dohyō compartido donde se realiza el combate. */
    private final Dohyo dohyo;

    /** Contador atómico de luchadores conectados. */
    private final AtomicInteger luchadoresConectados;

    /**
     * Construye el controlador del servidor, inicializa el dohyō y la vista,
     * y se registra como observador del dohyō.
     */
    public ControladorServidor() {
        this.dohyo = new Dohyo();
        this.vista = new VistaServidor();
        this.luchadoresConectados = new AtomicInteger(0);
        this.dohyo.agregarObservador(this);
        this.vista.setVisible(true);
    }

    /**
     * Método estático de entrada para iniciar el servidor.
     * Crea el controlador (que levanta la vista) y arranca el socket.
     * Invocado desde {@link sumo.launcher.LauncherServidor}.
     */
    public static void iniciar() {
        ControladorServidor servidor = new ControladorServidor();
        Thread hilo = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
        hilo.setDaemon(false);
        hilo.start();
    }

    /**
     * Inicia el {@link ServerSocket}, acepta exactamente dos conexiones y lanza
     * un {@link HiloLuchador} por cada una. El servidor se cierra después del combate.
     */
    public void iniciarServidor() {
        vista.mostrarMensaje("🥋 Servidor de Sumo iniciado en el puerto " + PUERTO);
        vista.mostrarMensaje("⏳ Esperando a los dos luchadores...");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            for (int i = 0; i < MAX_LUCHADORES; i++) {
                Socket socketCliente = serverSocket.accept();
                int indice = luchadoresConectados.getAndIncrement();
                vista.mostrarMensaje("📡 Luchador " + (indice + 1) + " conectado desde "
                        + socketCliente.getInetAddress().getHostAddress());

                HiloLuchador hilo = new HiloLuchador(socketCliente, dohyo, indice);
                hilo.start();
            }

            // Esperar a que ambos hilos de luchadores terminen
            // El servidor espera pasivamente; los hilos manejan el combate
            // El ServerSocket puede cerrarse ahora (no más conexiones)
        } catch (IOException e) {
            actualizarVista("❌ Error en el servidor: " + e.getMessage());
        }
    }

    // ─── Implementación de ICombateObservador ──────────────────────────────────

    /**
     * {@inheritDoc}
     * <p>
     * Notifica a la vista la llegada de un nuevo luchador al dohyō.
     * Se ejecuta desde un hilo de fondo; actualiza la UI en el EDT.
     * </p>
     */
    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        String msg = String.format("🎌 Luchador %d llegó al dohyō: %s (%.1f kg)",
                indice + 1, nombre, peso);
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(() ->
            vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifica a la vista que ambos luchadores están listos y el combate empieza.
     * </p>
     */
    @Override
    public void onCombateIniciado(String nombreLuchador1, String nombreLuchador2) {
        String msg = "⚔ ¡COMBATE INICIADO! " + nombreLuchador1 + " vs " + nombreLuchador2;
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(() ->
            vista.mostrarInicioCombate(nombreLuchador1, nombreLuchador2));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifica a la vista el resultado de cada kimarite ejecutado.
     * </p>
     */
    @Override
    public void onKimariteEjecutado(String nombreLuchador, String nombreKimarite, boolean expulsado) {
        String resultado = expulsado ? "💥 ¡EXPULSADO!" : "↩ El oponente resiste";
        String msg = String.format("  ▶ %s usa [%s] → %s", nombreLuchador, nombreKimarite, resultado);
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(() ->
            vista.mostrarKimarite(nombreLuchador, nombreKimarite, expulsado));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notifica a la vista el ganador del combate con sus victorias acumuladas.
     * </p>
     */
    @Override
    public void onCombateTerminado(String nombreGanador, int victoriasGanador) {
        String msg = String.format(
            "🏆 ¡COMBATE TERMINADO! Ganador: %s | Victorias: %d",
            nombreGanador, victoriasGanador);
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(() ->
            vista.mostrarGanador(nombreGanador, victoriasGanador));
    }

    /**
     * Actualiza el área de log de la vista de forma segura para el EDT.
     *
     * @param mensaje texto a agregar al log del combate
     */
    private void actualizarVista(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(mensaje));
    }
}
