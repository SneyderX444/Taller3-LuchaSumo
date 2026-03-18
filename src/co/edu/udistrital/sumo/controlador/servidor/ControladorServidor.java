package co.edu.udistrital.sumo.controlador.servidor;


import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.servidor.ConexionServidor;
import co.edu.udistrital.sumo.modelo.servidor.Dohyo;
import co.edu.udistrital.sumo.vista.servidor.VistaServidor;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Controlador del servidor — Combate de Sumo v2.
 *
 * CORRECCIÓN: usa un CountDownLatch(2) para detectar cuando ambos
 * HiloLuchador terminaron (es decir, cuando ambos clientes enviaron LISTO).
 * Solo entonces llama a vista.cerrar() — nunca antes.
 *
 * PROHIBIDO: ServerSocket directo, lógica de combate, SQL.
 *
 * @author Grupo Taller 3
 * @version 2.0
 */
public class ControladorServidor implements ICombateObservador {

    private static final int PUERTO = 9999;

    private final VistaServidor    vista;
    private final ConexionServidor conexionServidor;
    private final Dohyo            dohyo;
    private final ControladorDohyo controladorDohyo;

    /**
     * Latch que cuenta hacia atrás cada vez que un HiloLuchador termina.
     * Cuando llega a 0, ambos clientes enviaron LISTO → cerrar servidor.
     */
    private final CountDownLatch latchCierre = new CountDownLatch(2);

    public ControladorServidor() {
        this.dohyo            = new Dohyo();
        this.controladorDohyo = new ControladorDohyo(dohyo);
        this.conexionServidor = new ConexionServidor(PUERTO);
        this.vista            = new VistaServidor();
        this.controladorDohyo.agregarObservador(this);
        this.vista.setVisible(true);
    }

    public static void iniciar() {
        ControladorServidor servidor = new ControladorServidor();
        Thread hilo = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
        hilo.setDaemon(false);
        hilo.start();
    }

    public void iniciarServidor() {
        actualizarVista("Servidor de Sumo iniciado en el puerto " + PUERTO);
        actualizarVista("Esperando a los dos luchadores...");

        try {
            conexionServidor.iniciar();

            for (int i = 0; i < conexionServidor.getMaxLuchadores(); i++) {
                Socket socketCliente = conexionServidor.aceptarConexion();
                actualizarVista("Luchador " + (i + 1) + " conectado desde "
                    + socketCliente.getInetAddress().getHostAddress());

                // Se pasa el latch al hilo — cuando el hilo termina llama latch.countDown()
                HiloLuchador hilo = new HiloLuchador(
                    socketCliente, controladorDohyo, i, latchCierre);
                hilo.start();
            }

            conexionServidor.cerrar();

            // Esperar a que AMBOS clientes envíen LISTO
            // (esto bloquea el hilo del servidor, no el EDT)
            latchCierre.await();

            // Ambos clientes confirmaron — cerrar el servidor en el EDT
            javax.swing.SwingUtilities.invokeLater(() -> vista.cerrar());

        } catch (IOException e) {
            actualizarVista("Error en el servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ─── ICombateObservador ───────────────────────────────────────────────────

    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        String msg = "Luchador " + (indice + 1) + " llego: " + nombre
            + " (" + String.format("%.1f", peso) + " kg)";
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    @Override
    public void onCombateIniciado(String n1, String n2) {
        actualizarVista("COMBATE INICIADO: " + n1 + " vs " + n2);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarInicioCombate(n1, n2));
    }

    @Override
    public void onKimariteEjecutado(String nombreLuchador,
                                     String nombreKimarite,
                                     boolean expulsado) {
        String res = expulsado ? "EXPULSADO" : "resiste";
        actualizarVista(nombreLuchador + " [" + nombreKimarite + "] -> " + res);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarKimarite(nombreLuchador, nombreKimarite, expulsado));
    }

    @Override
    public void onCombateTerminado(String nombreGanador, int victorias) {
        actualizarVista("GANADOR: " + nombreGanador + " | Victorias: " + victorias);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarGanador(nombreGanador, victorias));
    }

    private void actualizarVista(String msg) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(msg));
    }
}
