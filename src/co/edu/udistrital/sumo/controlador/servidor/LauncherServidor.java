package co.edu.udistrital.sumo.controlador.servidor;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación servidor del Combate de Sumo.
 *
 * Propósito: Contener únicamente el método {@code main} e invocar
 * {@link ControladorServidor#iniciar()} en el EDT de Swing.
 * Restricción del taller (regla q): esta clase NO debe contener
 * creación de objetos, asignación de valores ni creación de interfaces.
 * Orden de ejecución: este servidor debe iniciarse ANTES que los clientes.
 * Solo acepta exactamente dos conexiones.
 * Se comunica con: {@link ControladorServidor} (único receptor del inicio).
 * Principio SOLID:
 * S — única responsabilidad: ser el punto de entrada del servidor.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see ControladorServidor
 */
public class LauncherServidor {

    /**
     * Método de entrada de la aplicación servidor.
     * Inicia la interfaz gráfica del servidor en el EDT de Swing.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ControladorServidor::iniciar);
    }
}
