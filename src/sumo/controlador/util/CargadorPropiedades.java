package sumo.controlador.util;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utilidad encargada de cargar el listado de kimarites (técnicas de sumo)
 * desde un archivo {@code .properties} seleccionado por el usuario mediante
 * un cuadro de diálogo {@link JFileChooser}.
 *
 * <p>
 * El archivo de propiedades esperado tiene el siguiente formato:
 * <pre>
 * kimarite.count=10
 * kimarite.1.nombre=Yorikiri
 * kimarite.1.descripcion=Forzar al oponente fuera del ring con agarre del mawashi
 * kimarite.2.nombre=Hatakikomi
 * kimarite.2.descripcion=Abofetear hacia abajo al oponente que avanza
 * ...
 * </pre>
 * </p>
 *
 * <p>
 * Principio de Responsabilidad Única (SRP): esta clase tiene una única
 * responsabilidad: cargar y parsear el archivo de propiedades de kimarites.
 * No crea objetos del modelo ni lógica de negocio.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 */
public class CargadorPropiedades {

    /** Directorio por defecto donde se buscan archivos de propiedades. */
    private static final String DIRECTORIO_DATOS = "data";

    /** Clave del archivo de propiedades que indica el número total de kimarites. */
    private static final String CLAVE_CONTEO = "kimarite.count";

    /** Prefijo de clave para el nombre de un kimarite. */
    private static final String PREFIJO_NOMBRE = "kimarite.%d.nombre";

    /** Prefijo de clave para la descripción de un kimarite. */
    private static final String PREFIJO_DESCRIPCION = "kimarite.%d.descripcion";

    /**
     * Abre un cuadro de diálogo para que el usuario seleccione el archivo
     * {@code .properties} con los kimarites.
     *
     * @return ruta absoluta del archivo seleccionado, o {@code null} si el usuario canceló
     */
    public String seleccionarArchivoPropiedades() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de kimarites (.properties)");
        fileChooser.setCurrentDirectory(new File(DIRECTORIO_DATOS));
        fileChooser.setFileFilter(
            new FileNameExtensionFilter("Archivos de propiedades (*.properties)", "properties")
        );
        fileChooser.setAcceptAllFileFilterUsed(false);

        int resultado = fileChooser.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    /**
     * Carga y retorna la lista de nombres de kimarites desde el archivo de propiedades
     * en la ruta indicada.
     * <p>
     * Solo se retornan los nombres (Strings) para que la vista pueda mostrarlos
     * en un componente sin depender de la clase {@link sumo.modelo.Kimarite}.
     * </p>
     *
     * @param rutaArchivo ruta absoluta del archivo {@code .properties}
     * @return lista con los nombres de los kimarites, vacía si hay error o el archivo es inválido
     */
    public List<String> cargarNombresKimarites(String rutaArchivo) {
        List<String> nombres = new ArrayList<>();
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return nombres;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            props.load(fis);

            int count = Integer.parseInt(props.getProperty(CLAVE_CONTEO, "0"));
            for (int i = 1; i <= count; i++) {
                String nombre = props.getProperty(String.format(PREFIJO_NOMBRE, i));
                if (nombre != null && !nombre.trim().isEmpty()) {
                    nombres.add(nombre.trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            // No se propaga al modelo; se retorna lista vacía y se notifica al controlador
        }
        return nombres;
    }

    /**
     * Carga y retorna un mapa de nombre → descripción de kimarites desde el archivo.
     * Útil para que el controlador construya objetos {@link sumo.modelo.Kimarite}.
     *
     * @param rutaArchivo ruta absoluta del archivo {@code .properties}
     * @return arreglo de dos listas paralelas: [0] = nombres, [1] = descripciones
     */
    public List<String>[] cargarKimaritesCompletos(String rutaArchivo) {
        @SuppressWarnings("unchecked")
        List<String>[] resultado = new List[2];
        resultado[0] = new ArrayList<>(); // nombres
        resultado[1] = new ArrayList<>(); // descripciones

        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return resultado;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            props.load(fis);

            int count = Integer.parseInt(props.getProperty(CLAVE_CONTEO, "0"));
            for (int i = 1; i <= count; i++) {
                String nombre = props.getProperty(String.format(PREFIJO_NOMBRE, i), "").trim();
                String desc   = props.getProperty(String.format(PREFIJO_DESCRIPCION, i), "").trim();
                if (!nombre.isEmpty()) {
                    resultado[0].add(nombre);
                    resultado[1].add(desc);
                }
            }
        } catch (IOException | NumberFormatException e) {
            // Retorna listas vacías silenciosamente
        }
        return resultado;
    }
}
