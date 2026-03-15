package sumo.controlador.acciones;

import sumo.controlador.ControladorCliente;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Acción desencadenada al presionar el botón "Cargar Kimarites" en la vista del cliente.
 * <p>
 * Abre un {@code JFileChooser} para que el usuario seleccione el archivo
 * {@code .properties} con las técnicas de sumo disponibles, y carga los kimarites
 * en el componente de selección de la vista.
 * </p>
 *
 * <p>
 * Sigue el principio de Responsabilidad Única (SRP): su única responsabilidad es
 * delegar al {@link ControladorCliente} la carga del archivo de técnicas.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see ControladorCliente
 * @see AccionConectar
 */
public class AccionCargarKimarites implements ActionListener {

    /** Controlador del cliente al que se delega la carga del archivo de propiedades. */
    private final ControladorCliente controlador;

    /**
     * Construye la acción de carga de kimarites con referencia al controlador del cliente.
     *
     * @param controlador el controlador que gestionará la apertura del archivo
     */
    public AccionCargarKimarites(ControladorCliente controlador) {
        this.controlador = controlador;
    }

    /**
     * Invocado automáticamente por Swing cuando el usuario presiona el botón de carga.
     * Delega al controlador para abrir el diálogo de selección de archivo y cargar
     * los kimarites en la vista.
     *
     * @param e el evento de acción generado por el botón
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.cargarKimarites();
    }
}
