package co.edu.udistrital.sumo.modelo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Conexión al archivo de propiedades de kimarites.
 *
 * Propósito: Abrir y parsear el archivo {@code .properties} recibido como
 * parámetro, retornando únicamente Strings crudos para que el controlador
 * construya los objetos {@link Kimarite}.
 * Reside en el modelo porque es una conexión a un archivo, igual que
 * {@code ConexionBD} gestiona la conexión a la base de datos.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorCliente}
 * (consumidor de los datos crudos).
 * Principio SOLID:
 * S — única responsabilidad: leer y parsear el archivo de propiedades.
 * O — no crea objetos del modelo, eso es responsabilidad del controlador.
 *
 * PROHIBIDO en esta clase: JFileChooser, objetos Kimarite, lógica de negocio,
 * imports del paquete controlador o vista, rutas quemadas.
 *
 * @author Grupo Taller 3
 * @version 2.0
 */
public class CargadorPropiedades {

    //Clave del archivo que indica el número total de kimarites
    private static final String CLAVE_CONTEO = "kimarite.count";

    //Prefijo de clave para el nombre de cada kimarite
    private static final String PREFIJO_NOMBRE = "kimarite.%d.nombre";

    /**
     * Carga y retorna la lista de nombres de kimarites desde el archivo
     * en la ruta indicada. Retorna solo Strings — el controlador es quien
     * construye los objetos {@link Kimarite} a partir de estos datos.
     *
     * Formato esperado del archivo:
     * kimarite.count=15
     * kimarite.1.nombre=Yorikiri
     * kimarite.2.nombre=Hatakikomi
     * ...
     *
     * @param rutaArchivo ruta absoluta del archivo .properties
     *                    (provista por el controlador vía JFileChooser en la Vista)
     * @return lista de nombres crudos, vacía si el archivo es inválido o hay error
     * @throws IOException si no se puede leer el archivo
     */
    public List<String> cargarNombres(String rutaArchivo) throws IOException {
        List<String> nombres = new ArrayList<>();

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            props.load(fis);
        }

        int count;
        try {
            count = Integer.parseInt(props.getProperty(CLAVE_CONTEO, "0").trim());
        } catch (NumberFormatException e) {
            return nombres; //Archivo mal formado: retorna lista vacía
        }

        for (int i = 1; i <= count; i++) {
            String nombre = props.getProperty(String.format(PREFIJO_NOMBRE, i));
            if (nombre != null && !nombre.trim().isEmpty()) {
                nombres.add(nombre.trim());
            }
        }
        return nombres;
    }
}
