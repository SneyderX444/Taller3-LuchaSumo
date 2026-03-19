package co.edu.udistrital.sumo.controlador.servidor;
 
import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.servidor.ConexionServidor;
import co.edu.udistrital.sumo.modelo.servidor.Dohyo;
import co.edu.udistrital.sumo.vista.servidor.VistaServidor;
 
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
 
/**
 * Controlador principal del servidor del Combate de Sumo.
 *
 * <p>
 * Se encarga de arrancar el servidor, recibir las dos conexiones de los
 * luchadores, coordinar el combate a través del {@link ControladorDohyo}
 * y cerrar todo limpiamente cuando ambos clientes confirmen que terminaron.
 * </p>
 *
 * <p>
 * Para detectar cuándo los dos clientes enviaron "LISTO", se usa un
 * {@link CountDownLatch} inicializado en 2. Cada {@link HiloLuchador}
 * llama a {@code countDown()} en su {@code finally}, y cuando el contador
 * llega a 0 el servidor cierra su ventana.
 * </p>
 *
 * <p><b>Principios SOLID aplicados:</b></p>
 * <ul>
 *   <li>S — solo coordina el ciclo de vida del servidor, nada más.</li>
 *   <li>D — depende de {@link ICombateObservador} (interfaz), no de la vista directamente.</li>
 * </ul>
 *
 * <p><b>Restricciones:</b> esta clase no debe tener ServerSocket directo,
 * lógica de combate ni acceso a bases de datos.</p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see ControladorDohyo
 * @see HiloLuchador
 * @see VistaServidor
 */
public class ControladorServidor implements ICombateObservador {
 
    /** Puerto en el que el servidor escucha conexiones de los luchadores. */
    private static final int PUERTO = 9999;
 
    /** Vista del servidor: muestra el dohyō y el log del combate. */
    private final VistaServidor vista;
 
    /** Maneja el ServerSocket: abre el puerto y acepta conexiones. */
    private final ConexionServidor conexionServidor;
 
    /** Estado puro del ring (POJO): guarda luchadores, turno y ganador. */
    private final Dohyo dohyo;
 
    /**
     * Controlador del combate: toda la logica de turnos, kimarites y
     * sincronizacion entre los dos hilos vive aquí.
     */
    private final ControladorDohyo controladorDohyo;
 
    /**
     * Contador que baja de 2 a 0 conforme los hilos de luchadores terminan.
     * Cuando llega a 0, significa que ambos clientes enviaron "LISTO"
     * y el servidor puede cerrar su ventana.
     */
    private final CountDownLatch latchCierre = new CountDownLatch(2);
 
    /**
     * Construye el controlador del servidor.
     * Inicializa el dohyō, el controlador del combate, la conexión de red
     * y la vista, y registra este controlador como observador del combate.
     */
    public ControladorServidor() {
        this.dohyo            = new Dohyo();
        this.controladorDohyo = new ControladorDohyo(dohyo);
        this.conexionServidor = new ConexionServidor(PUERTO);
        this.vista            = new VistaServidor();
        this.controladorDohyo.agregarObservador(this);
        this.vista.setVisible(true);
    }
 
    /**
     * Punto de entrada estatico invocado desde {@link LauncherServidor}.
     * Crea el controlador y arranca el hilo del servidor de sockets.
     * El Launcher solo llama este metodo — no crea objetos directamente.
     */
    public static void iniciar() {
        ControladorServidor servidor = new ControladorServidor();
        Thread hilo = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
        hilo.setDaemon(false);
        hilo.start();
    }
 
    /**
     * Abre el servidor, acepta exactamente dos conexiones, lanza un
     * {@link HiloLuchador} por cada una y espera a que ambos terminen
     * antes de cerrar la ventana del servidor.
     *
     * <p>Este metodo corre en el hilo {@code HiloServidorSocket},
     * no en el EDT de Swing, por eso puede bloquearse con {@code latchCierre.await()}
     * sin congelar la interfaz graafica.</p>
     */
    public void iniciarServidor() {
        actualizarVista("Servidor de Sumo iniciado en el puerto " + PUERTO);
        actualizarVista("Esperando a los dos luchadores...");
 
        try {
            conexionServidor.iniciar();
 
            for (int i = 0; i < conexionServidor.getMaxLuchadores(); i++) {
                Socket socketCliente = conexionServidor.aceptarConexion();
                actualizarVista("Luchador " + (i + 1) + " conectado desde "
                    + socketCliente.getInetAddress().getHostAddress());
 
                // Cada hilo recibe el latch — cuando termine, llama countDown()
                HiloLuchador hilo = new HiloLuchador(
                    socketCliente, controladorDohyo, i, latchCierre);
                hilo.start();
            }
 
            // Ya no se necesitan mas conexiones: cerrar el ServerSocket
            conexionServidor.cerrar();
 
            // Bloqueamos este hilo hasta que los dos clientes confirmen "LISTO"
            latchCierre.await();
 
            // Ambos confirmaron: cerrar la ventana del servidor en el EDT
            javax.swing.SwingUtilities.invokeLater(() -> vista.cerrar());
 
        } catch (IOException e) {
            actualizarVista("Error en el servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            // Si alguien interrumpe el hilo, restauramos el flag y salimos
            Thread.currentThread().interrupt();
        }
    }
 
    // ── Implementación de ICombateObservador ──────────────────────────────────
    // Cada metodo recibe el evento del dohyō y lo refleja en la vista del servidor.
    // Siempre usamos invokeLater para actualizar la UI desde el hilo correcto (EDT).
 
    /**
     * Recibe el aviso de que un luchador llego al dohyō y actualiza la vista.
     *
     * @param nombre nombre del luchador que llegó
     * @param peso   peso en kg del luchador
     * @param indice posición en el dohyō (0 = primero, 1 = segundo)
     */
    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        String msg = "Luchador " + (indice + 1) + " llegó: " + nombre
            + " (" + String.format("%.1f", peso) + " kg)";
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }
 
    /**
     * Recibe el aviso de que el combate inicio con los dos luchadores listos.
     *
     * @param n1 nombre del primer luchador
     * @param n2 nombre del segundo luchador
     */
    @Override
    public void onCombateIniciado(String n1, String n2) {
        actualizarVista("¡COMBATE INICIADO! " + n1 + " vs " + n2);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarInicioCombate(n1, n2));
    }
 
    /**
     * Recibe el aviso de que un luchador ejecuto un kimarite y muestra el resultado.
     *
     * @param nombreLuchador nombre del luchador que ataco
     * @param nombreKimarite nombre de la tecnica usada
     * @param expulsado      true si el oponente fue expulsado del dohyō
     */
    @Override
    public void onKimariteEjecutado(String nombreLuchador,
                                     String nombreKimarite,
                                     boolean expulsado) {
        String res = expulsado ? "¡EXPULSADO!" : "resiste";
        actualizarVista(nombreLuchador + " [" + nombreKimarite + "] → " + res);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarKimarite(nombreLuchador, nombreKimarite, expulsado));
    }
 
    /**
     * Recibe el aviso de que el combate terminó y muestra el ganador.
     *
     * @param nombreGanador nombre del luchador que ganó
     * @param victorias     total de victorias acumuladas por el ganador
     */
    @Override
    public void onCombateTerminado(String nombreGanador, int victorias) {
        actualizarVista("¡GANADOR: " + nombreGanador
            + " | Victorias totales: " + victorias + "!");
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarGanador(nombreGanador, victorias));
    }
 
    /**
     * Envía un mensaje al log de la vista de forma segura desde cualquier hilo.
     * Siempre usa {@code invokeLater} para respetar el EDT de Swing.
     *
     * @param msg texto a mostrar en el log del combate
     */
    private void actualizarVista(String msg) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(msg));
    }
}