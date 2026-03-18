package co.edu.udistrital.sumo.controlador;

import co.edu.udistrital.sumo.modelo.ConexionServidor;
import co.edu.udistrital.sumo.modelo.Dohyo;
import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.vista.servidor.VistaServidor;

import java.io.IOException;
import java.net.Socket;

/**
 * Controlador del lado del servidor en la arquitectura MVC del Combate de Sumo.
 *
 * Propósito: Gestionar el ciclo de vida completo del servidor:
 * 1. Iniciar el {@link ConexionServidor} en el puerto configurado.
 * 2. Aceptar exactamente dos conexiones de clientes (luchadores).
 * 3. Crear un {@link HiloLuchador} por cada cliente, pasándole el
 *    {@link ControladorDohyo} compartido.
 * 4. Coordinar la visualización del combate a través de {@link VistaServidor}.
 * 5. Implementar {@link ICombateObservador} para recibir eventos del combate.
 * Se comunica con: {@link VistaServidor} (vista), {@link ConexionServidor}
 * (modelo - socket servidor), {@link ControladorDohyo} (lógica del combate),
 * {@link HiloLuchador} (hilos de atención a clientes).
 * Principio SOLID:
 * S — única responsabilidad: gestionar el ciclo de vida del servidor.
 * D — depende de {@link ICombateObservador} (abstracción).
 *
 * PROHIBIDO en esta clase: ServerSocket directo, lógica de combate, SQL.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see ConexionServidor
 * @see ControladorDohyo
 * @see HiloLuchador
 */
public class ControladorServidor implements ICombateObservador {

    //Puerto en el que escucha el servidor
    private static final int PUERTO = 9999;

    //Vista del servidor que muestra el desarrollo del combate
    private final VistaServidor vista;

    //Conexión de red del servidor (modelo - maneja el ServerSocket)
    private final ConexionServidor conexionServidor;

    //Estado del ring compartido entre los hilos
    private final Dohyo dohyo;

    //Controlador del combate: sincronización y lógica de negocio
    //Una sola instancia compartida entre AMBOS HiloLuchador
    private final ControladorDohyo controladorDohyo;

    /**
     * Construye el controlador del servidor, inicializa todas las dependencias
     * y registra este controlador como observador del combate.
     */
    public ControladorServidor() {
        this.dohyo             = new Dohyo();
        this.controladorDohyo  = new ControladorDohyo(dohyo);
        this.conexionServidor  = new ConexionServidor(PUERTO);
        this.vista             = new VistaServidor();
        //Registrar como observador para recibir eventos del combate
        this.controladorDohyo.agregarObservador(this);
        this.vista.setVisible(true);
    }

    /**
     * Método estático de entrada invocado desde {@link LauncherServidor}.
     * Crea el controlador y arranca el servidor en un hilo de fondo.
     */
    public static void iniciar() {
        ControladorServidor servidor = new ControladorServidor();
        Thread hilo = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
        hilo.setDaemon(false);
        hilo.start();
    }

    /**
     * Abre el servidor, acepta exactamente dos conexiones y lanza un
     * {@link HiloLuchador} por cada una. Pasa la misma instancia de
     * {@link ControladorDohyo} a ambos hilos para garantizar la sincronización.
     */
    public void iniciarServidor() {
        actualizarVista("Servidor de Sumo iniciado en el puerto " + PUERTO);
        actualizarVista("Esperando a los dos luchadores...");

        try {
            conexionServidor.iniciar();

            for (int i = 0; i < conexionServidor.getMaxLuchadores(); i++) {
                Socket socketCliente = conexionServidor.aceptarConexion();
                actualizarVista("Luchador " + (i + 1) + " conectado desde "
                    + socketCliente.getInetAddress().getHostAddress());

                //La misma instancia de controladorDohyo para ambos hilos — obligatorio
                HiloLuchador hilo = new HiloLuchador(
                    socketCliente, controladorDohyo, i);
                hilo.start();
            }

            //Cerrar el ServerSocket: ya no necesitamos más conexiones
            conexionServidor.cerrar();

        } catch (IOException e) {
            actualizarVista("Error en el servidor: " + e.getMessage());
        }
    }

    // ─── Implementación de ICombateObservador ─────────────────────────────────

    //Notifica a la vista la llegada de un luchador al dohyō
    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        String msg = String.format("Luchador %d llego al dohyo: %s (%.1f kg)",
            indice + 1, nombre, peso);
        actualizarVista(msg);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    //Notifica a la vista que el combate comenzó
    @Override
    public void onCombateIniciado(String nombreL1, String nombreL2) {
        actualizarVista("¡COMBATE INICIADO! " + nombreL1 + " vs " + nombreL2);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarInicioCombate(nombreL1, nombreL2));
    }

    //Notifica a la vista el resultado de cada kimarite ejecutado
    @Override
    public void onKimariteEjecutado(String nombreLuchador,
                                     String nombreKimarite,
                                     boolean expulsado) {
        String res = expulsado ? "¡EXPULSADO!" : "El oponente resiste";
        actualizarVista(nombreLuchador + " usa [" + nombreKimarite + "] -> " + res);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarKimarite(nombreLuchador, nombreKimarite, expulsado));
    }

    //Notifica a la vista el ganador del combate
    @Override
    public void onCombateTerminado(String nombreGanador, int victorias) {
        actualizarVista("¡COMBATE TERMINADO! Ganador: "
            + nombreGanador + " | Victorias: " + victorias);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarGanador(nombreGanador, victorias));
    }

    //Actualiza el log de la vista de forma segura para el EDT
    private void actualizarVista(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(mensaje));
    }
}
