package co.edu.udistrital.sumo.controlador.cliente;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación cliente del Combate de Sumo.
 *
 * Propósito: Contener únicamente el método {@code main} e invocar
 * {@link ControladorCliente#iniciar()} en el EDT de Swing.
 * Restricción del taller (regla q): esta clase NO debe contener
 * creación de objetos, asignación de valores ni creación de interfaces.
 * Se comunica con: {@link ControladorCliente} (único receptor del inicio).
 * Principio SOLID:
 * S — única responsabilidad: ser el punto de entrada del cliente.
 *
 * @author Grupo Taller 3
 * @version 2.0
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
