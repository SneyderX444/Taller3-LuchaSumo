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
 * HiloLuchador v2 — atiende a un cliente por socket.
 *
 * CORRECCIÓN: recibe un CountDownLatch y llama countDown() en el finally,
 * es decir, después de recibir "LISTO" del cliente. Esto permite que
 * ControladorServidor sepa cuándo ambos clientes terminaron y cierre
 * el servidor en el momento correcto.
 *
 * @author Grupo Taller 3
 * @version 2.0
 */
public class HiloLuchador extends Thread {

    private static final int PAUSA_MIN_MS = 300;
    private static final int PAUSA_MAX_MS = 500;

    private final Socket           socketCliente;
    private final ControladorDohyo controladorDohyo;
    private final int              indice;
    /** Latch compartido con ControladorServidor — countDown al terminar */
    private final CountDownLatch   latchCierre;
    private Rikishi rikishi;

    public HiloLuchador(Socket socketCliente,
                         ControladorDohyo controladorDohyo,
                         int indice,
                         CountDownLatch latchCierre) {
        super("HiloLuchador-" + indice);
        this.socketCliente    = socketCliente;
        this.controladorDohyo = controladorDohyo;
        this.indice           = indice;
        this.latchCierre      = latchCierre;
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(
                socketCliente.getOutputStream(), true)
        ) {
            // Paso 1: Recibir datos del luchador
            String lineaDatos = entrada.readLine();
            if (lineaDatos == null || lineaDatos.isEmpty()) return;

            // Paso 2: Construir el Rikishi
            rikishi = parsearRikishi(lineaDatos);

            // Paso 3: Subir al dohyo
            controladorDohyo.subirLuchador(rikishi, indice);

            // Paso 4: Esperar al oponente
            controladorDohyo.esperarAmbosLuchadores();

            // Paso 5: Bucle de combate
            while (!controladorDohyo.isCombateTerminado()) {
                int pausa = PAUSA_MIN_MS
                    + (int)(Math.random() * (PAUSA_MAX_MS - PAUSA_MIN_MS));
                Thread.sleep(pausa);
                if (!controladorDohyo.isCombateTerminado()) {
                    controladorDohyo.ejecutarTurno(indice);
                }
            }

            // Paso 6: Enviar resultado al cliente
            Rikishi ganador = controladorDohyo.getGanador();
            if (ganador != null && ganador.getNombre().equals(rikishi.getNombre())) {
                salida.println("GANASTE");
            } else {
                salida.println("PERDISTE");
            }

            // Paso 7: Esperar confirmación LISTO del cliente
            entrada.readLine();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Error de red: el finally limpia
        } finally {
            cerrarSocket();
            // Notificar al servidor que este cliente terminó (fix #3)
            latchCierre.countDown();
        }
    }

    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");
        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";
        double peso = 0.0;
        if (partes.length > 1) {
            try { peso = Double.parseDouble(partes[1].trim()); }
            catch (NumberFormatException ignored) {}
        }
        Rikishi nuevo = new Rikishi(nombre, peso);
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            for (String nombreK : partes[2].trim().split(",")) {
                if (!nombreK.trim().isEmpty())
                    nuevo.getKimarites().add(new Kimarite(nombreK.trim()));
            }
        }
        return nuevo;
    }

    private void cerrarSocket() {
        try {
            if (socketCliente != null && !socketCliente.isClosed())
                socketCliente.close();
        } catch (IOException ignored) {}
    }

    public Rikishi getRikishi() { return rikishi; }
}
