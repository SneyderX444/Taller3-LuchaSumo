package sumo.controlador.acciones;

import sumo.controlador.ControladorCliente;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Acción desencadenada al presionar el botón "Conectar al servidor" en la vista del cliente.
 * <p>
 * Siguiendo el principio de Responsabilidad Única (SRP) y la separación entre
 * <em>evento</em>, <em>listener</em> y <em>performed</em> exigida por el taller,
 * esta clase encapsula exclusivamente la acción de iniciar la conexión al servidor.
 * </p>
 *
 * <p>
 * La lógica de conexión reside en {@link ControladorCliente#conectarAlServidor()},
 * no en esta clase.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see ControladorCliente
 * @see AccionCargarKimarites
 */
public class AccionConectar implements ActionListener {

    /** Controlador del cliente al que se delega la acción de conexión. */
    private final ControladorCliente controlador;

    /**
     * Construye la acción de conexión con referencia al controlador del cliente.
     *
     * @param controlador el controlador que ejecutará la lógica de conexión
     */
    public AccionConectar(ControladorCliente controlador) {
        this.controlador = controlador;
    }

    /**
     * Invocado automáticamente por Swing cuando el usuario presiona el botón de conectar.
     * Delega al controlador para validar los datos del luchador e iniciar la conexión
     * al servidor vía socket.
     *
     * @param e el evento de acción generado por el botón
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.conectarAlServidor();
    }
}
