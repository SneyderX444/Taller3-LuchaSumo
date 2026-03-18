package co.edu.udistrital.sumo.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Acción desencadenada al presionar "Conectar al Servidor" en la vista del cliente.
 *
 * Propósito: Desacoplar el evento del botón de la lógica de conexión,
 * delegando completamente al {@link ControladorCliente}.
 * Cumple la separación evento/listener/performed exigida por el taller (regla r).
 * Se comunica con: {@link ControladorCliente} (único receptor de la acción).
 * Principio SOLID:
 * S — única responsabilidad: delegar la conexión al servidor al controlador.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see ControladorCliente
 * @see AccionCargarKimarites
 */
public class AccionConectar implements ActionListener {

    //Controlador del cliente al que se delega la acción
    private final ControladorCliente controlador;

    /**
     * Construye la acción con referencia al controlador del cliente.
     *
     * @param controlador controlador que ejecutará la lógica de conexión
     */
    public AccionConectar(ControladorCliente controlador) {
        this.controlador = controlador;
    }

    /**
     * Invocado por Swing cuando el usuario presiona el botón.
     * Delega al controlador para validar datos e iniciar la conexión al servidor.
     *
     * @param e evento de acción generado por el botón
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.conectarAlServidor();
    }
}
