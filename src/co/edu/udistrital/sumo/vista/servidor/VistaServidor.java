package co.edu.udistrital.sumo.vista.servidor;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Esta es la vista del servidor donde se muestra todo el combate de sumo.
 *
 * Aquí se ve:
 * - Los luchadores
 * - El dohyo
 * - Las técnicas (kimarites)
 * - El log del combate
 * - El ganador
 *
 * IMPORTANTE:
 * Esta clase SOLO maneja la parte visual.
 * No hay lógica del combate, ni uso de modelo directamente.
 */
public class VistaServidor extends JFrame {

    // ── rutas de imágenes ──
    private static final String REC  = "Data/Recursos/";
    private static final String KTEC = "Data/Imagenes_tecnicas/";

    // ── colores usados en la interfaz ──
    private static final Color C_ROJO      = new Color(237, 85, 90);
    private static final Color C_AZUL      = new Color(55, 80, 170);
    private static final Color C_ROJO_DARK = new Color(130, 30, 30);
    private static final Color C_DORADO    = new Color(220, 185, 40);
    private static final Color C_BLANCO    = Color.WHITE;
    private static final Color C_TEXTO     = new Color(20, 10, 5);

    // controla si el combate ya empezó (para evitar cerrar la ventana)
    private boolean combateEnCurso = false;

    // ── componentes de la interfaz ──
    private final JLabel lblNombreL1a;
    private final JLabel lblNombreL1b;
    private final JLabel lblNombreL2a;
    private final JLabel lblNombreL2b;
    private final JPanel panelGanador;
    private final JLabel lblGanador;
    private final JTextArea areaLog;
    private final JLabel lblEstado;
    private final PanelCombate panelCombate;

    public VistaServidor() {
        super("Servidor - Combate de Sumo | Dohyo");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        // controla el cierre dependiendo si el combate está activo
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!combateEnCurso) {
                    // si aún no empieza, se puede cerrar
                    System.exit(0);
                }
                // si ya empezó, no deja cerrar
            }
        });

        // labels iniciales de los luchadores
        lblNombreL1a = crearLblLuchador("LUCHADOR 1", Font.BOLD, 14);
        lblNombreL1b = crearLblLuchador("(Esperando...)", Font.ITALIC, 12);
        lblNombreL2a = crearLblLuchador("LUCHADOR 2", Font.BOLD, 14);
        lblNombreL2b = crearLblLuchador("(Esperando...)", Font.ITALIC, 12);

        // label del ganador
        lblGanador = new JLabel(" ", SwingConstants.CENTER);
        lblGanador.setFont(new Font("Serif", Font.BOLD, 17));
        lblGanador.setForeground(C_TEXTO);

        panelGanador = construirPanelGanador();
        areaLog = crearAreaLog();
        lblEstado = crearLblEstado();
        panelCombate = new PanelCombate();

        construirUI();
    }

    // construye toda la interfaz principal
    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.BLACK);
        setContentPane(raiz);
        raiz.add(construirHeader(), BorderLayout.NORTH);
        raiz.add(construirCentro(), BorderLayout.CENTER);
    }

    // parte superior (título, logo, bandera)
    private JPanel construirHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_ROJO);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel pContenido = new JPanel();
        pContenido.setLayout(new BoxLayout(pContenido, BoxLayout.X_AXIS));
        pContenido.setOpaque(false);

        JLabel lblIcono = new JLabel();
        lblIcono.setIcon(escalarIcono(REC + "Logo_Sumo.png", 58, 58));
        pContenido.add(lblIcono);
        pContenido.add(Box.createRigidArea(new Dimension(12, 0)));

        JLabel lblTitulo = new JLabel("¡COMBATE DE SUMO!");
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 36));
        lblTitulo.setForeground(C_BLANCO);
        pContenido.add(lblTitulo);
        pContenido.add(Box.createRigidArea(new Dimension(12, 0)));

        JLabel lblBandera = new JLabel();
        lblBandera.setIcon(escalarIcono(REC + "japones.png", 58, 58));
        pContenido.add(lblBandera);

        JPanel pCentrador = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pCentrador.setOpaque(false);
        pCentrador.add(pContenido);

        p.add(pCentrador, BorderLayout.CENTER);
        return p;
    }

    // parte central (combate + log)
    private JPanel construirCentro() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel pIzq = new JPanel(new BorderLayout());
        pIzq.setOpaque(false);
        pIzq.add(panelCombate, BorderLayout.CENTER);
        pIzq.add(construirInfoLuchadores(), BorderLayout.SOUTH);

        p.add(pIzq, BorderLayout.CENTER);
        p.add(construirPanelLog(), BorderLayout.EAST);
        return p;
    }

    // zona donde se muestran los nombres de los luchadores
    private JPanel construirInfoLuchadores() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);

        JPanel pFila = new JPanel(new GridLayout(1, 2, 2, 0));
        pFila.setOpaque(false);
        pFila.add(construirPanelNombre(lblNombreL1a, lblNombreL1b, C_AZUL));
        pFila.add(construirPanelNombre(lblNombreL2a, lblNombreL2b, C_ROJO_DARK));

        p.add(pFila, BorderLayout.NORTH);
        p.add(panelGanador, BorderLayout.CENTER);
        return p;
    }

    // panel de cada luchador
    private JPanel construirPanelNombre(JLabel lbl1, JLabel lbl2, Color fondo) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(fondo);
        p.setBorder(new EmptyBorder(10, 16, 10, 16));
        p.add(lbl1);
        p.add(lbl2);
        return p;
    }

    // panel donde aparece el ganador
    private JPanel construirPanelGanador() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_DORADO);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.add(lblGanador, BorderLayout.CENTER);
        return p;
    }

    // panel del log y estado
    private JPanel construirPanelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(360, 0));
        p.setBackground(C_ROJO);

        JLabel lblTitLog = new JLabel("  Log de combate:", SwingConstants.LEFT);
        lblTitLog.setFont(new Font("Serif", Font.BOLD, 18));
        lblTitLog.setForeground(C_BLANCO);
        lblTitLog.setBorder(new EmptyBorder(16, 16, 8, 16));
        lblTitLog.setBackground(C_ROJO);
        lblTitLog.setOpaque(true);

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(C_ROJO);

        JPanel pEstado = new JPanel(new BorderLayout());
        pEstado.setBackground(C_AZUL);
        pEstado.setBorder(new EmptyBorder(10, 16, 10, 16));
        pEstado.add(lblEstado, BorderLayout.CENTER);

        p.add(lblTitLog, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(pEstado, BorderLayout.SOUTH);
        return p;
    }

    // crea un label para los luchadores
    private JLabel crearLblLuchador(String txt, int estilo, int tam) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(new Font("Serif", estilo, tam));
        l.setForeground(C_BLANCO);
        return l;
    }

    // crea el área donde se muestran los mensajes
    private JTextArea crearAreaLog() {
        JTextArea a = new JTextArea();
        a.setBackground(new Color(200, 70, 75));
        a.setForeground(C_BLANCO);
        a.setFont(new Font("Monospaced", Font.PLAIN, 12));
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 12, 8, 12));
        return a;
    }

    // label que muestra el estado del combate
    private JLabel crearLblEstado() {
        JLabel l = new JLabel("*MENSAJE CON ESTADO ACTUAL E INSTRUCCIONES*", SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 13));
        l.setForeground(C_BLANCO);
        return l;
    }

    // carga una imagen desde archivo
    private Image cargarImagen(String ruta, int ancho, int alto) {
        try {
            File f = new File(ruta);
            if (!f.exists()) return null;
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            if (ancho == -1) return raw.getImage();
            return raw.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        } catch (Exception e) { return null; }
    }

    // escala una imagen a icono
    private ImageIcon escalarIcono(String ruta, int ancho, int alto) {
        Image img = cargarImagen(ruta, ancho, alto);
        return img != null ? new ImageIcon(img) : new ImageIcon();
    }

    // agrega mensaje al log
    public void mostrarMensaje(String msg) {
        areaLog.append(msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    // actualiza el estado mostrado
    public void actualizarEstado(String msg) {
        lblEstado.setText(msg);
    }

    // muestra los datos del luchador que llega
    public void mostrarLuchadorEnDohyo(String nombre, double peso, int indice) {
        String linea2 = nombre + "  (" + String.format("%.1f", peso) + " kg)";
        if (indice == 0) {
            lblNombreL1a.setText("LUCHADOR 1");
            lblNombreL1b.setText(linea2);
        } else {
            lblNombreL2a.setText("LUCHADOR 2");
            lblNombreL2b.setText(linea2);
        }
        panelCombate.repaint();
    }

    // inicia visualmente el combate
    public void mostrarInicioCombate(String n1, String n2) {
        combateEnCurso = true;
        actualizarEstado("¡COMBATE INICIADO!  " + n1 + "  VS  " + n2);

        setState(Frame.NORMAL);
        toFront();
        requestFocus();
    }

    // muestra la técnica ejecutada
    public void mostrarKimarite(String nombreLuchador, String nombreKimarite, boolean expulsado) {
        panelCombate.cargarKimarite(nombreKimarite, expulsado);
        String res = expulsado ? " EXPULSADO!" : " El oponente resiste";
        actualizarEstado(nombreLuchador + " usa [" + nombreKimarite + "] - " + res);
    }

    // muestra el ganador final
    public void mostrarGanador(String nombreGanador, int victorias) {
        lblGanador.setText("GANADOR : " + nombreGanador
            + "  |  Numero de victorias : " + victorias);
        actualizarEstado("Combate finalizado. Ganador: " + nombreGanador);
    }

    // cierra la ventana del servidor
    public void cerrar() {
        dispose();
        System.exit(0);
    }

    // ── panel interno donde se dibuja el combate ──
    private class PanelCombate extends JPanel {

        private final Image imgFondo;
        private final Image imgL1;
        private final Image imgL2;
        private Image imgKimarite = null;
        private String nombreKimarite = "";
        private Color colorKimarite = Color.WHITE;

        public PanelCombate() {
            setOpaque(true);
            imgFondo = cargarImagen(REC + "Dohyo.png", -1, -1);
            imgL1 = cargarImagen(REC + "Luchador1.png", -1, -1);
            imgL2 = cargarImagen(REC + "Luchador2.png", -1, -1);
        }

        // carga la imagen del kimarite
        public void cargarKimarite(String nombre, boolean expulsado) {
            nombreKimarite = nombre;
            colorKimarite = expulsado ? new Color(255, 80, 80) : new Color(100, 240, 130);
            imgKimarite = null;

            for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
                File f = new File(KTEC + nombre + ext);
                if (f.exists()) {
                    imgKimarite = cargarImagen(KTEC + nombre + ext, -1, -1);
                    if (imgKimarite != null) break;
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            int w = getWidth(), h = getHeight();

            // pinta el fondo
            if (imgFondo != null)
                g2.drawImage(imgFondo, 0, 0, w, h, null);

            // pinta luchadores
            pintarLuchador(g2, imgL1, w, h, false);
            pintarLuchador(g2, imgL2, w, h, true);

            // pinta técnica
            if (imgKimarite != null)
                pintarKimarite(g2, w, h);

            g2.dispose();
        }

        private void pintarLuchador(Graphics2D g2, Image img, int w, int h, boolean derecho) {
            if (img == null) return;
            int mitad = w / 2;
            int cx = derecho ? (mitad + mitad / 2) : (mitad / 2);
            g2.drawImage(img, cx - 100, (int)(h * 0.2), 200, 300, null);
        }

        private void pintarKimarite(Graphics2D g2, int w, int h) {
            int cx = w / 2 - 100;
            int cy = h / 2 - 100;
            g2.drawImage(imgKimarite, cx, cy, 200, 200, null);

            g2.setColor(colorKimarite);
            g2.drawString(nombreKimarite, w / 2 - 50, cy + 220);
        }
    }
}