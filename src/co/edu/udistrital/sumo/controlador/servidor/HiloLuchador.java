package co.edu.udistrital.sumo.controlador.servidor;

import co.edu.udistrital.sumo.modelo.cliente.Kimarite;
import co.edu.udistrital.sumo.modelo.cliente.Rikishi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Hilo del servidor que atiende a un luchador conectado por socket.
 *
 * <p>
 * Cuando un cliente se conecta al servidor, se crea un {@code HiloLuchador}
 * exclusivo para atenderlo. Esto permite que el servidor acepte la segunda
 * conexión mientras el primero ya está participando en el combate.
 * </p>
 *
 * <p>
 * <b>Protocolo de comunicación con el cliente:</b></p>
 * <ol>
 * <li>Cliente → Servidor: {@code "nombre|peso|k1,k2,k3,..."}}</li>
 * <li>Servidor → Cliente: {@code "GANASTE"} o {@code "PERDISTE"}</li>
 * <li>Cliente → Servidor: {@code "LISTO"} (confirmación de cierre)</li>
 * </ol>
 *
 * <p>
 * La sincronización del combate se delega completamente al
 * {@link ControladorDohyo}, cuya instancia es compartida entre los dos hilos.
 * Al terminar (en el {@code finally}), este hilo llama a
 * {@code latchCierre.countDown()} para avisarle al servidor que este cliente ya
 * cerró.
 * </p>
 *
 * <p>
 * <b>Principio SOLID aplicado:</b></p>
 * <ul>
 * <li>S — única responsabilidad: manejar la comunicación por socket de un
 * luchador.</li>
 * </ul>
 *
 * <p>
 * <b>Restricciones:</b> esta clase no debe tener lógica de combate, acceso
 * directo al {@link co.edu.udistrital.sumo.modelo.servidor.Dohyo} ni
 * componentes Swing.</p>
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see ControladorDohyo
 * @see ControladorServidor
 */
public class HiloLuchador extends Thread {

    /**
     * Pausa mínima entre turnos para que el combate sea visible en la interfaz
     * (ms).
     */
    private static final int PAUSA_MIN_MS = 300;

    /**
     * Pausa máxima entre turnos — según el enunciado, no puede superar 500 ms.
     */
    private static final int PAUSA_MAX_MS = 500;

    /**
     * Socket de comunicación con el cliente.
     */
    private final Socket socketCliente;

    /**
     * Controlador del combate compartido entre los dos hilos. Es el monitor de
     * sincronización: contiene los métodos {@code synchronized}.
     */
    private final ControladorDohyo controladorDohyo;

    /**
     * Posición de este luchador en el dohyō: 0 = primero en llegar, 1 =
     * segundo.
     */
    private final int indice;

    /**
     * Contador compartido con {@link ControladorServidor}. Al terminar este
     * hilo, llama a {@code countDown()} para avisar que este cliente ya envió
     * "LISTO" y cerró.
     */
    private final CountDownLatch latchCierre;

    /**
     * Luchador construido a partir de los datos recibidos por socket.
     */
    private Rikishi rikishi;

    /**
     * Crea un hilo para atender al cliente conectado.
     *
     * @param socketCliente socket activo de la conexión con el cliente
     * @param controladorDohyo instancia <b>compartida</b> del controlador del
     * combate
     * @param indice posición del luchador en el dohyō (0 o 1)
     * @param latchCierre contador compartido para notificar el cierre al
     * servidor
     */
    public HiloLuchador(Socket socketCliente,
            ControladorDohyo controladorDohyo,
            int indice,
            CountDownLatch latchCierre) {
        super("HiloLuchador-" + indice);
        this.socketCliente = socketCliente;
        this.controladorDohyo = controladorDohyo;
        this.indice = indice;
        this.latchCierre = latchCierre;
    }

    /**
     * Lógica principal del hilo. Sigue estos pasos en orden:
     * <ol>
     * <li>Recibe los datos del luchador desde el socket.</li>
     * <li>Construye el objeto {@link Rikishi}.</li>
     * <li>Sube al luchador al dohyō.</li>
     * <li>Espera a que el oponente también llegue.</li>
     * <li>Ejecuta el bucle de combate con pausas aleatorias.</li>
     * <li>Envía "GANASTE" o "PERDISTE" al cliente.</li>
     * <li>Espera "LISTO" del cliente antes de cerrar.</li>
     * </ol>
     *
     * <p>
     * En el {@code finally} siempre cierra el socket y llama a
     * {@code latchCierre.countDown()} sin importar cómo terminó el hilo.</p>
     */
    @Override
    public void run() {
        try (
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socketCliente.getInputStream())); PrintWriter salida = new PrintWriter(
                        socketCliente.getOutputStream(), true)) {
            // Paso 1: Leer los datos del luchador enviados por el cliente
            String lineaDatos = entrada.readLine();
            if (lineaDatos == null || lineaDatos.isEmpty()) {
                return;
            }

            // Paso 2: Construir el Rikishi con nombre, peso y kimarites
            rikishi = parsearRikishi(lineaDatos);

            // Paso 3: Registrar al luchador en el dohyō
            controladorDohyo.subirLuchador(rikishi, indice);

            // Paso 4: Esperar a que el oponente también llegue
            controladorDohyo.esperarAmbosLuchadores();

            // Paso 5: Bucle de combate con pausa aleatoria entre turnos
            while (!controladorDohyo.isCombateTerminado()) {
                int pausa = PAUSA_MIN_MS
                        + (int) (Math.random() * (PAUSA_MAX_MS - PAUSA_MIN_MS));
                Thread.sleep(pausa);
                if (!controladorDohyo.isCombateTerminado()) {
                    controladorDohyo.ejecutarTurno(indice);
                }
            }

            // Paso 6: Enviar el resultado al cliente
            Rikishi ganador = controladorDohyo.getGanador();
            if (ganador != null && ganador.getNombre().equals(rikishi.getNombre())) {
                salida.println("GANASTE");
            } else {
                salida.println("PERDISTE");
            }

            // Paso 7: Esperar la confirmación "LISTO" del cliente antes de cerrar
            entrada.readLine();

        } catch (InterruptedException e) {
            // El hilo fue interrumpido externamente: restaurar el flag
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Error de red: el finally se encarga de limpiar
        } finally {
            cerrarSocket();
            // Avisar al servidor que este cliente ya terminó
            latchCierre.countDown();
        }
    }

    /**
     * Parsea la cadena recibida del cliente y construye un {@link Rikishi}.
     *
     * <p>
     * Formato esperado: {@code "nombre|peso|k1,k2,k3,..."}</p>
     *
     * @param linea texto enviado por el cliente
     * @return objeto {@link Rikishi} con los datos del luchador
     */
    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");
        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";
        double peso = 0.0;
        if (partes.length > 1) {
            try {
                peso = Double.parseDouble(partes[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Rikishi nuevo = new Rikishi(nombre, peso);

        // Agregar cada kimarite separado por coma a la lista del luchador
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            for (String nombreK : partes[2].trim().split(",")) {
                if (!nombreK.trim().isEmpty()) {
                    nuevo.getKimarites().add(new Kimarite(nombreK.trim()));
                }
            }
        }
        return nuevo;
    }

    /**
     * Cierra el socket del cliente de forma segura, sin lanzar excepciones.
     */
    private void cerrarSocket() {
        try {
            if (socketCliente != null && !socketCliente.isClosed()) {
                socketCliente.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Retorna el luchador atendido por este hilo.
     *
     * @return el {@link Rikishi} de este hilo, o {@code null} si aún no se
     * recibieron los datos
     */
    public Rikishi getRikishi() {
        return rikishi;
    }
}
