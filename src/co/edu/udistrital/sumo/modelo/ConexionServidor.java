package co.edu.udistrital.sumo.modelo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Gestiona la conexión del servidor de sumo vía sockets.
 *
 * Propósito: Abrir el {@link ServerSocket} en el puerto configurado y
 * aceptar las conexiones entrantes de los clientes (luchadores), retornando
 * el {@link Socket} de cada uno para que el controlador lo procese.
 * Reside en el modelo porque es una conexión de red, igual que
 * {@code ConexionBD} gestiona la conexión a la base de datos.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorServidor}
 * (único consumidor de esta conexión).
 * Principio SOLID:
 * S — única responsabilidad: gestionar el ServerSocket y aceptar conexiones.
 *
 * PROHIBIDO en esta clase: lógica de combate, hilos, Swing, objetos del modelo
 * distintos a esta misma conexión.
 *
 * @author Grupo Taller 3
 * @version 1.0
 */
public class ConexionServidor {

    //Número máximo de luchadores (conexiones) que acepta el servidor
    private static final int MAX_LUCHADORES = 2;

    //Puerto en el que el servidor escucha conexiones entrantes
    private final int puerto;

    //Socket del servidor — se abre en iniciar() y se cierra en cerrar()
    private ServerSocket serverSocket;

    /**
     * Construye la conexión del servidor en el puerto indicado.
     * No abre el socket hasta que se llame a {@link #iniciar()}.
     *
     * @param puerto puerto en el que escuchará el servidor
     */
    public ConexionServidor(int puerto) {
        this.puerto = puerto;
    }

    /**
     * Abre el {@link ServerSocket} en el puerto configurado.
     * Debe llamarse antes de {@link #aceptarConexion()}.
     *
     * @throws IOException si el puerto está ocupado o hay error de red
     */
    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
    }

    /**
     * Bloquea el hilo actual hasta que un cliente se conecte y retorna
     * el {@link Socket} de esa conexión.
     * El controlador llama a este método dos veces para obtener los
     * dos sockets de los luchadores.
     *
     * @return socket del cliente conectado
     * @throws IOException si hay error al aceptar la conexión
     */
    public Socket aceptarConexion() throws IOException {
        return serverSocket.accept();
    }

    /**
     * Cierra el {@link ServerSocket} de forma segura.
     * Debe llamarse cuando el servidor ya no necesite aceptar conexiones.
     *
     * @throws IOException si hay error al cerrar
     */
    public void cerrar() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    //Retorna el número máximo de luchadores que acepta el servidor
    public int getMaxLuchadores() {
        return MAX_LUCHADORES;
    }

    //Retorna el puerto configurado para el servidor
    public int getPuerto() {
        return puerto;
    }

    //Retorna true si el ServerSocket está abierto y listo para aceptar conexiones
    public boolean isAbierto() {
        return serverSocket != null && !serverSocket.isClosed();
    }
}
