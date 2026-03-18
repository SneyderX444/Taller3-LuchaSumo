package co.edu.udistrital.sumo.controlador;

import co.edu.udistrital.sumo.modelo.Kimarite;
import co.edu.udistrital.sumo.modelo.Rikishi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Hilo del servidor que atiende a un luchador de sumo conectado vía socket.
 *
 * Propósito: Cada vez que un cliente se conecta, el servidor crea un
 * {@code HiloLuchador} para atenderlo independientemente. Esto permite
 * aceptar la segunda conexión mientras el primero ya está siendo atendido.
 * Protocolo de comunicación:
 * - Cliente a Servidor: "nombre|peso|k1,k2,k3,..."
 * - Servidor a Cliente: "GANASTE" o "PERDISTE"
 * - Cliente a Servidor (confirmación de cierre): "LISTO"
 * El combate se sincroniza a través de {@link ControladorDohyo}, cuya
 * instancia es compartida entre AMBOS hilos luchadores.
 * Se comunica con: {@link ControladorDohyo} (lógica y sincronización del combate).
 * Principio SOLID:
 * S — única responsabilidad: atender la comunicación socket de un luchador
 * y participar en el combate usando el controlador del dohyō.
 *
 * PROHIBIDO en esta clase: lógica de combate, Dohyo directo, componentes Swing.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see ControladorDohyo
 * @see ControladorServidor
 */
public class HiloLuchador extends Thread {

    //Pausa mínima entre turnos para que el combate sea visible en la interfaz
    private static final int PAUSA_MIN_MS = 300;

    //Pausa máxima entre turnos — según enunciado: máximo 500ms de espera
    private static final int PAUSA_MAX_MS = 500;

    //Socket de comunicación con el cliente
    private final Socket socketCliente;

    //Controlador del combate: sincronización y lógica compartida entre ambos hilos
    private final ControladorDohyo controladorDohyo;

    //Índice de este luchador en el dohyō (0 o 1)
    private final int indice;

    //Luchador construido a partir de los datos recibidos por socket
    private Rikishi rikishi;

    /**
     * Construye un HiloLuchador para atender al cliente conectado.
     *
     * @param socketCliente    socket activo de la conexión con el cliente
     * @param controladorDohyo instancia COMPARTIDA del controlador del combate
     * @param indice           posición del luchador (0 = primero, 1 = segundo)
     */
    public HiloLuchador(Socket socketCliente,
                         ControladorDohyo controladorDohyo,
                         int indice) {
        super("HiloLuchador-" + indice);
        this.socketCliente    = socketCliente;
        this.controladorDohyo = controladorDohyo;
        this.indice           = indice;
    }

    /**
     * Lógica principal del hilo:
     * 1. Lee los datos del luchador enviados por el cliente.
     * 2. Construye el objeto {@link Rikishi}.
     * 3. Sube al luchador al dohyō vía ControladorDohyo.
     * 4. Espera a que ambos estén listos.
     * 5. Ejecuta el bucle de combate con pausa aleatoria entre PAUSA_MIN y PAUSA_MAX.
     * 6. Envía el resultado al cliente.
     * 7. Espera confirmación "LISTO" antes de cerrar.
     */
    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(
                socketCliente.getOutputStream(), true)
        ) {
            //Paso 1: Recibir datos del luchador
            String lineaDatos = entrada.readLine();
            if (lineaDatos == null || lineaDatos.isEmpty()) return;

            //Paso 2: Construir el Rikishi
            rikishi = parsearRikishi(lineaDatos);

            //Paso 3: Subir al dohyō
            controladorDohyo.subirLuchador(rikishi, indice);

            //Paso 4: Esperar al oponente
            controladorDohyo.esperarAmbosLuchadores();

            //Paso 5: Bucle de combate
            while (!controladorDohyo.isCombateTerminado()) {
                //Pausa aleatoria entre PAUSA_MIN_MS y PAUSA_MAX_MS
                int pausa = PAUSA_MIN_MS
                    + (int)(Math.random() * (PAUSA_MAX_MS - PAUSA_MIN_MS));
                Thread.sleep(pausa);

                if (!controladorDohyo.isCombateTerminado()) {
                    controladorDohyo.ejecutarTurno(indice);
                }
            }

            //Paso 6: Enviar resultado al cliente
            Rikishi ganador = controladorDohyo.getGanador();
            if (ganador != null
                    && ganador.getNombre().equals(rikishi.getNombre())) {
                salida.println("GANASTE");
            } else {
                salida.println("PERDISTE");
            }

            //Paso 7: Esperar confirmación del cliente antes de cerrar
            entrada.readLine(); // "LISTO"

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            //Error de red: el finally cierra el socket limpiamente
        } finally {
            cerrarSocket();
        }
    }

    /**
     * Parsea la cadena recibida del cliente y construye un {@link Rikishi}.
     * Formato esperado: "nombre|peso|k1,k2,k3,..."
     *
     * @param linea cadena enviada por el cliente
     * @return Rikishi construido con los datos recibidos
     */
    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");
        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";
        double peso = 0.0;
        if (partes.length > 1) {
            try {
                peso = Double.parseDouble(partes[1].trim());
            } catch (NumberFormatException ignored) {}
        }

        Rikishi nuevo = new Rikishi(nombre, peso);

        //Agregar kimarites directamente a la lista (sin lógica en Rikishi)
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            for (String nombreK : partes[2].trim().split(",")) {
                if (!nombreK.trim().isEmpty()) {
                    nuevo.getKimarites().add(new Kimarite(nombreK.trim()));
                }
            }
        }
        return nuevo;
    }

    //Cierra el socket del cliente de forma segura
    private void cerrarSocket() {
        try {
            if (socketCliente != null && !socketCliente.isClosed()) {
                socketCliente.close();
            }
        } catch (IOException ignored) {}
    }

    //Retorna el luchador atendido por este hilo
    public Rikishi getRikishi() { return rikishi; }
}
