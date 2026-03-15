package sumo.controlador;

import sumo.modelo.Dohyo;
import sumo.modelo.Kimarite;
import sumo.modelo.Rikishi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Hilo del servidor que atiende a un luchador de sumo conectado vía socket.
 * <p>
 * Cada vez que un cliente se conecta al servidor, se crea una instancia de
 * {@code HiloLuchador} para atenderlo en un hilo independiente. Esto permite
 * que el servidor continúe aceptando la segunda conexión mientras el primero
 * ya está siendo atendido.
 * </p>
 *
 * <p>
 * <b>Protocolo de comunicación:</b>
 * <ul>
 *   <li>Cliente → Servidor: {@code "nombre|peso|k1,k2,k3,..."}
 *       donde k1,k2,k3 son los nombres de los kimarites seleccionados.</li>
 *   <li>Servidor → Cliente: {@code "GANASTE"} o {@code "PERDISTE"}.</li>
 *   <li>Cliente → Servidor (confirmación): {@code "LISTO"}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * El combate se sincroniza a través del objeto {@link Dohyo}, el cual actúa
 * como monitor (métodos {@code synchronized}, {@code wait}, {@code notifyAll}).
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see Dohyo
 * @see ControladorServidor
 */
public class HiloLuchador extends Thread {

    /** Socket de comunicación con el cliente. */
    private final Socket socketCliente;

    /** El dohyō compartido entre ambos hilos de luchadores. */
    private final Dohyo dohyo;

    /** Índice de este luchador en el dohyō (0 o 1). */
    private final int indice;

    /** Luchador de sumo (rikishi) creado a partir de los datos recibidos por socket. */
    private Rikishi rikishi;

    /**
     * Construye un nuevo HiloLuchador para atender al cliente conectado.
     *
     * @param socketCliente socket activo de la conexión con el cliente
     * @param dohyo         instancia compartida del dohyō donde se realizará el combate
     * @param indice        posición del luchador (0 = primero en llegar, 1 = segundo)
     */
    public HiloLuchador(Socket socketCliente, Dohyo dohyo, int indice) {
        super("HiloLuchador-" + indice);
        this.socketCliente = socketCliente;
        this.dohyo = dohyo;
        this.indice = indice;
    }

    /**
     * Lógica principal del hilo:
     * <ol>
     *   <li>Lee los datos del luchador enviados por el cliente.</li>
     *   <li>Construye el objeto {@link Rikishi}.</li>
     *   <li>Sube al luchador al dohyō.</li>
     *   <li>Espera a que ambos luchadores estén listos.</li>
     *   <li>Ejecuta el bucle de combate alternando turnos en el dohyō.</li>
     *   <li>Envía el resultado (GANASTE / PERDISTE) al cliente.</li>
     *   <li>Espera confirmación "LISTO" del cliente antes de cerrar.</li>
     * </ol>
     */
    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)
        ) {
            // ── Paso 1: Recibir datos del luchador ──────────────────────────
            String lineaDatos = entrada.readLine();
            if (lineaDatos == null || lineaDatos.isEmpty()) {
                return;
            }

            rikishi = parsearRikishi(lineaDatos);

            // ── Paso 2: Subir al dohyō ──────────────────────────────────────
            dohyo.subirLuchador(rikishi, indice);

            // ── Paso 3: Esperar al oponente ─────────────────────────────────
            dohyo.esperarAmbosLuchadores();

            // ── Paso 4: Bucle de combate ────────────────────────────────────
            while (!dohyo.isCombateTerminado()) {
                // Espera aleatoria antes de ejecutar el turno (máximo 500 ms)
                int espera = (int) (Math.random() * Dohyo.MAX_ESPERA_MS);
                Thread.sleep(espera);

                dohyo.ejecutarTurno(indice);
            }

            // ── Paso 5: Enviar resultado ────────────────────────────────────
            Rikishi ganador = dohyo.getGanador();
            if (ganador != null && ganador.getNombre().equals(rikishi.getNombre())) {
                salida.println("GANASTE");
            } else {
                salida.println("PERDISTE");
            }

            // ── Paso 6: Esperar confirmación del cliente ────────────────────
            String confirmacion = entrada.readLine();
            // "LISTO" recibido: el cliente terminó su ejecución

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cerrarSocket();
        }
    }

    /**
     * Parsea la cadena de datos recibida del cliente y construye un {@link Rikishi}.
     * <p>
     * Formato esperado: {@code "nombre|peso|k1,k2,k3,..."}
     * </p>
     *
     * @param linea cadena de texto enviada por el cliente
     * @return objeto Rikishi construido con los datos recibidos
     */
    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");
        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";
        double peso = 0.0;
        if (partes.length > 1) {
            try {
                peso = Double.parseDouble(partes[1].trim());
            } catch (NumberFormatException ignored) { }
        }

        Rikishi nuevo = new Rikishi(nombre, peso);

        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            String[] nombresK = partes[2].trim().split(",");
            for (String nombreK : nombresK) {
                if (!nombreK.trim().isEmpty()) {
                    nuevo.agregarKimarite(new Kimarite(nombreK.trim()));
                }
            }
        }

        return nuevo;
    }

    /**
     * Cierra el socket del cliente de forma segura.
     */
    private void cerrarSocket() {
        try {
            if (socketCliente != null && !socketCliente.isClosed()) {
                socketCliente.close();
            }
        } catch (IOException e) {
            // Ignorado en cierre
        }
    }

    /**
     * Retorna el luchador (rikishi) atendido por este hilo.
     *
     * @return el objeto Rikishi, o {@code null} si aún no se han recibido los datos
     */
    public Rikishi getRikishi() {
        return rikishi;
    }
}
