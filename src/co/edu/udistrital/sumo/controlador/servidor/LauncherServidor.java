package co.edu.udistrital.sumo.controlador.servidor;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicacion servidor del Combate de Sumo.
 *
 * <p>
 * Esta clase solo contiene el método {@code main} y lo único que hace
 * es invocar {@link ControladorServidor#iniciar()} en el EDT de Swing.
 * Nada más.
 * </p>
 *
 * <p>
 * Según la regla q del taller, el Launcher <b>no puede</b> crear objetos,
 * asignar valores ni construir interfaces directamente. Todo eso lo hace
 * {@link ControladorServidor}.
 * </p>
 *
 * <p>
 * <b>Orden de ejecución:</b> el servidor debe iniciarse <b>antes</b>
 * que los clientes. Solo acepta exactamente dos conexiones.
 * </p>
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li>S — única responsabilidad: ser el punto de entrada del servidor.</li>
 * </ul>
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see ControladorServidor
 */
public class LauncherServidor {

    /**
     * Arranca la aplicación servidor en el Event Dispatch Thread (EDT) de Swing.
     * Solo invoca el método estático del controlador — sin crear nada aquí.
     *
     * @param args argumentos de línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ControladorServidor::iniciar);
    }
}