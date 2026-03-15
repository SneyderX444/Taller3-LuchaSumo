package sumo.controlador;

import sumo.controlador.acciones.AccionCargarKimarites;
import sumo.controlador.acciones.AccionConectar;
import sumo.controlador.util.CargadorPropiedades;
import sumo.vista.cliente.VistaCliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Controlador del lado del cliente en la arquitectura MVC del Combate de Sumo.
 * <p>
 * Gestiona la interacción entre la {@link VistaCliente} y la lógica de conexión
 * al servidor vía sockets. Sus responsabilidades son:
 * <ul>
 *   <li>Cargar el listado de kimarites desde el archivo de propiedades.</li>
 *   <li>Validar los datos ingresados por el usuario.</li>
 *   <li>Establecer la conexión con el servidor y enviar los datos del luchador.</li>
 *   <li>Esperar la respuesta del servidor (GANASTE / PERDISTE) en un hilo aparte.</li>
 *   <li>Notificar a la vista el resultado y cerrar la conexión limpiamente.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Las acciones de usuario están desacopladas mediante las clases
 * {@link AccionConectar} y {@link AccionCargarKimarites}.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see VistaCliente
 * @see HiloLuchador
 */
public class ControladorCliente {

    /** Dirección IP del servidor de sumo. */
    private static final String HOST_SERVIDOR = "localhost";

    /** Puerto en el que escucha el servidor de sumo. */
    private static final int PUERTO_SERVIDOR = 9999;

    /** Vista del cliente (formulario del luchador). */
    private final VistaCliente vista;

    /** Utilidad para cargar el archivo de propiedades de kimarites. */
    private final CargadorPropiedades cargadorPropiedades;

    /** Ruta del archivo de propiedades seleccionado por el usuario. */
    private String rutaArchivoPropiedades;

    /**
     * Construye el controlador del cliente e inicializa la vista y las dependencias.
     * Registra las acciones desacopladas (ActionListeners) en la vista.
     */
    public ControladorCliente() {
        this.cargadorPropiedades = new CargadorPropiedades();
        this.vista = new VistaCliente();
        registrarAcciones();
        this.vista.setVisible(true);
    }

    /**
     * Método estático de entrada para iniciar el controlador del cliente.
     * Es invocado desde {@link sumo.launcher.LauncherCliente} sin crear objetos allí.
     */
    public static void iniciar() {
        new ControladorCliente();
    }

    /**
     * Registra las acciones desacopladas (ActionListeners) en los botones de la vista.
     * Siguiendo la separación de eventos, listeners y performed exigida por el taller.
     */
    private void registrarAcciones() {
        vista.getBtnCargarKimarites().addActionListener(new AccionCargarKimarites(this));
        vista.getBtnConectar().addActionListener(new AccionConectar(this));
    }

    /**
     * Abre el diálogo de selección de archivo y carga los kimarites en la vista.
     * Invocado desde {@link AccionCargarKimarites#actionPerformed(java.awt.event.ActionEvent)}.
     */
    public void cargarKimarites() {
        rutaArchivoPropiedades = cargadorPropiedades.seleccionarArchivoPropiedades();
        if (rutaArchivoPropiedades == null) {
            vista.mostrarMensaje("No se seleccionó ningún archivo.");
            return;
        }

        List<String> nombres = cargadorPropiedades.cargarNombresKimarites(rutaArchivoPropiedades);
        if (nombres.isEmpty()) {
            vista.mostrarMensaje("El archivo no contiene kimarites válidos.");
            return;
        }

        vista.cargarListaKimarites(nombres);
        vista.mostrarEstado("✔ Kimarites cargados: " + nombres.size() + " técnicas disponibles");
    }

    /**
     * Valida los datos del formulario y establece la conexión con el servidor.
     * Invocado desde {@link AccionConectar#actionPerformed(java.awt.event.ActionEvent)}.
     */
    public void conectarAlServidor() {
        // ── Validaciones ──────────────────────────────────────────────────
        String nombre = vista.getNombreLuchador().trim();
        if (nombre.isEmpty()) {
            vista.mostrarMensaje("Debe ingresar el nombre del luchador.");
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(vista.getPesoLuchador().trim());
            if (peso <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            vista.mostrarMensaje("El peso debe ser un número positivo válido.");
            return;
        }

        List<String> kimaritesSeleccionados = vista.getKimaritesSeleccionados();
        if (kimaritesSeleccionados.isEmpty()) {
            vista.mostrarMensaje("Debe seleccionar al menos un kimarite.");
            return;
        }

        // ── Preparar datos a enviar ───────────────────────────────────────
        String kimaritesStr = String.join(",", kimaritesSeleccionados);
        String mensajeServidor = nombre + "|" + peso + "|" + kimaritesStr;

        vista.setBtnConectarHabilitado(false);
        vista.mostrarEstado("⏳ Conectando al servidor...");

        // ── Conectar en hilo aparte para no bloquear el EDT ──────────────
        Thread hiloConexion = new Thread(() -> ejecutarCombate(mensajeServidor), "HiloConexionCliente");
        hiloConexion.setDaemon(true);
        hiloConexion.start();
    }

    /**
     * Ejecuta el flujo completo de conexión al servidor, envío de datos y espera
     * del resultado del combate. Se ejecuta en un hilo de fondo.
     *
     * @param mensajeServidor cadena de datos del luchador a enviar al servidor
     */
    private void ejecutarCombate(String mensajeServidor) {
        try (
            Socket socket = new Socket(HOST_SERVIDOR, PUERTO_SERVIDOR);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))
        ) {
            actualizarEstadoVista("⚔ Conectado. Esperando al oponente y el inicio del combate...");

            // Enviar datos del luchador
            salida.println(mensajeServidor);

            // Esperar resultado del combate (bloqueante hasta que el servidor responda)
            String resultado = entrada.readLine();

            // Notificar resultado en la vista y enviar confirmación
            if ("GANASTE".equals(resultado)) {
                actualizarEstadoVista("🏆 ¡GANASTE EL COMBATE!");
                mostrarResultadoVista(true);
            } else {
                actualizarEstadoVista("💀 Perdiste el combate...");
                mostrarResultadoVista(false);
            }

            // Enviar confirmación al servidor antes de cerrar
            salida.println("LISTO");

        } catch (IOException e) {
            actualizarEstadoVista("❌ Error de conexión: " + e.getMessage());
        }
    }

    /**
     * Actualiza el panel de estado de la vista desde un hilo de fondo.
     * Utiliza {@code SwingUtilities.invokeLater} para respetar el EDT.
     *
     * @param mensaje texto a mostrar en el estado
     */
    private void actualizarEstadoVista(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarEstado(mensaje));
    }

    /**
     * Muestra el resultado final del combate en la vista.
     * Utiliza {@code SwingUtilities.invokeLater} para respetar el EDT.
     *
     * @param gano {@code true} si el luchador ganó, {@code false} si perdió
     */
    private void mostrarResultadoVista(boolean gano) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarResultadoCombate(gano));
    }
}
