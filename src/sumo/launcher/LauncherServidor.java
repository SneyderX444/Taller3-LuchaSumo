package sumo.launcher;

import sumo.controlador.ControladorServidor;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicacion servidor del Combate de Sumo.
 * <p>
 * Esta clase contiene únicamente el método {@code main}, el cual delega
 * la inicialización al método estático {@link ControladorServidor#iniciar()}.
 * </p>
 *
 * <p>
 * <b>Restricción del taller:</b> Esta clase no debe contener creación de objetos,
 * asignación de valores ni creación de interfaces. Solo invoca el arranque.
 * </p>
 *
 * <p>
 * <b>Orden de ejecución:</b> Este servidor debe iniciarse <em>antes</em> que los
 * clientes ({@link LauncherCliente}). Solo acepta exactamente dos conexiones.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see ControladorServidor
 */
public class LauncherServidor {

    /**
     * Método de entrada de la aplicación servidor.
     * Inicia la interfaz gráfica del servidor en el EDT y arranca el socket en un hilo aparte.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ControladorServidor servidor = new ControladorServidor();
            Thread hiloServidor = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
            hiloServidor.setDaemon(false);
            hiloServidor.start();
        });
    }
}
