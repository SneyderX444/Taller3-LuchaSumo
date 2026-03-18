package co.edu.udistrital.sumo.modelo.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Gestiona la conexión del cliente de sumo al servidor vía socket.
 *
 * Propósito: Abrir el {@link Socket} hacia el servidor, exponer los métodos
 * para enviar datos y recibir la respuesta del combate, y cerrar la conexión
 * limpiamente al finalizar.
 * Reside en el modelo porque es una conexión de red, igual que
 * {@code ConexionBD} gestiona la conexión a la base de datos.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorCliente}
 * (único consumidor de esta conexión).
 * Principio SOLID:
 * S — única responsabilidad: gestionar el socket cliente y sus streams.
 *
 * PROHIBIDO en esta clase: lógica de combate, hilos, Swing, objetos del modelo
 * distintos a esta misma conexión.
 *
 * @author Grupo Taller 3
 * @version 1.0
 */
public class ConexionCliente {

    //Dirección IP o hostname del servidor al que conectarse
    private final String host;

    //Puerto del servidor al que conectarse
    private final int puerto;

    //Socket de la conexión activa con el servidor
    private Socket socket;

    //Stream de escritura hacia el servidor
    private PrintWriter salida;

    //Stream de lectura desde el servidor
    private BufferedReader entrada;

    /**
     * Construye la conexión del cliente con host y puerto del servidor.
     * No abre el socket hasta que se llame a {@link #conectar()}.
     *
     * @param host   dirección IP o hostname del servidor
     * @param puerto puerto del servidor
     */
    public ConexionCliente(String host, int puerto) {
        this.host   = host;
        this.puerto = puerto;
    }

    /**
     * Abre el socket hacia el servidor e inicializa los streams de
     * entrada y salida. Debe llamarse antes de {@link #enviar(String)}
     * y {@link #recibirRespuesta()}.
     *
     * @throws IOException si no se puede conectar al servidor
     */
    public void conectar() throws IOException {
        socket  = new Socket(host, puerto);
        salida  = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(
                      new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Envía un mensaje de texto al servidor a través del socket.
     * El controlador es responsable de formatear el mensaje
     * antes de llamar a este método.
     *
     * @param mensaje texto a enviar al servidor
     */
    public void enviar(String mensaje) {
        salida.println(mensaje);
    }

    /**
     * Bloquea el hilo actual hasta que el servidor envíe una línea de texto
     * y la retorna. Usado para esperar el resultado del combate
     * ({@code "GANASTE"} o {@code "PERDISTE"}).
     *
     * @return línea de texto recibida del servidor
     * @throws IOException si hay error de red o la conexión se cerró
     */
    public String recibirRespuesta() throws IOException {
        return entrada.readLine();
    }

    /**
     * Cierra el socket y sus streams de forma segura.
     * Debe llamarse cuando el cliente ya no necesite la conexión.
     *
     * @throws IOException si hay error al cerrar
     */
    public void cerrar() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    //Retorna true si el socket está conectado y abierto
    public boolean isConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    //Retorna el host configurado
    public String getHost() { return host; }

    //Retorna el puerto configurado
    public int getPuerto() { return puerto; }
}
