package sumo.launcher;

import sumo.controlador.ControladorCliente;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación cliente del Combate de Sumo.
 * <p>
 * Esta clase contiene únicamente el método {@code main}, el cual delega
 * la inicialización al método estático {@link ControladorCliente#iniciar()}.
 * </p>
 *
 * <p>
 * <b>Restricción del taller:</b> Esta clase no debe contener creación de objetos,
 * asignación de valores ni creación de interfaces. Solo invoca el arranque.
 * </p>
 *
 * <p>
 * Se ejecuta <b>antes</b> de {@link LauncherServidor} debe estar corriendo.
 * Se pueden lanzar dos instancias de cliente (una por luchador).
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see ControladorCliente
 */
public class LauncherCliente {

    /**
     * Método de entrada de la aplicación cliente.
     * Inicia la interfaz gráfica en el Event Dispatch Thread (EDT) de Swing.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ControladorCliente::iniciar);
    }
}
