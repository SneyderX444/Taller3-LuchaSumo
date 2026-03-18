package co.edu.udistrital.sumo.vista.servidor;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;

/**
 * Vista del servidor — Combate de Sumo.
 *
 * CORRECCIONES v4:
 * - Sin HTML en ningún componente — solo Java puro (fix #3)
 * - Luchadores visibles desde el inicio como siluetas/imágenes (fix #1)
 * - Al terminar el combate el servidor cierra su ejecución (fix #2)
 * - Silueta negra semitransparente sobre el fondo (fix #5)
 * - Header: íconos pegados al título (fix #4)
 * - Imagen del kimarite centrada sobre el dohyo (fix #7)
 *
 * Recursos en Data/Recursos/:
 *   Dohyo.png, Logo_Sumo.png, japones.png, Luchador1.png, Luchador2.png
 *
 * Data/Imagenes_tecnicas/[NombreKimarite].png  (o .jpg)
 *
 * PROHIBIDO: Rikishi/Kimarite, lógica de negocio, sockets, SQL, HTML.
 *
 * @author Grupo Taller 3
 * @version 4.0
 */
public class VistaServidor extends JFrame {

    // ── Rutas ─────────────────────────────────────────────────────────────────
    private static final String REC  = "Data/Recursos/";
    private static final String KTEC = "Data/Imagenes_tecnicas/";

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color C_ROJO      = new Color(237, 85, 90);
    private static final Color C_AZUL      = new Color(55, 80, 170);
    private static final Color C_ROJO_DARK = new Color(130, 30, 30);
    private static final Color C_DORADO    = new Color(220, 185, 40);
    private static final Color C_BLANCO    = Color.WHITE;
    private static final Color C_TEXTO     = new Color(20, 10, 5);

    // ── Componentes ───────────────────────────────────────────────────────────
    /** Línea 1 nombre luchador 1 (sin HTML) */
    private final JLabel lblNombreL1a;
    /** Línea 2 nombre luchador 1 */
    private final JLabel lblNombreL1b;
    /** Línea 1 nombre luchador 2 */
    private final JLabel lblNombreL2a;
    /** Línea 2 nombre luchador 2 */
    private final JLabel lblNombreL2b;

    private final JPanel      panelGanador;
    private final JLabel      lblGanador;
    private final JTextArea   areaLog;
    private final JLabel      lblEstado;
    private final PanelCombate panelCombate;

    public VistaServidor() {
        super("Servidor - Combate de Sumo | Dohyo");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        // Dos JLabels por luchador en lugar de HTML (fix #3)
        lblNombreL1a = crearLblLuchador("🥋  LUCHADOR 1", Font.BOLD,   14);
        lblNombreL1b = crearLblLuchador("(Esperando...)", Font.ITALIC, 12);
        lblNombreL2a = crearLblLuchador("🥋  LUCHADOR 2", Font.BOLD,   14);
        lblNombreL2b = crearLblLuchador("(Esperando...)", Font.ITALIC, 12);

        lblGanador   = new JLabel(" ", SwingConstants.CENTER);
        lblGanador.setFont(new Font("Serif", Font.BOLD, 17));
        lblGanador.setForeground(C_TEXTO);
        panelGanador = construirPanelGanador();
        areaLog      = crearAreaLog();
        lblEstado    = crearLblEstado();
        panelCombate = new PanelCombate();

        construirUI();
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.BLACK);
        setContentPane(raiz);
        raiz.add(construirHeader(),  BorderLayout.NORTH);
        raiz.add(construirCentro(),  BorderLayout.CENTER);
    }

    /** Header rojo — íconos pegados al título (fix #4) */
    private JPanel construirHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(C_ROJO);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel pIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pIzq.setOpaque(false);
        JLabel lblIcono = new JLabel();
        lblIcono.setIcon(escalarIcono(REC + "Logo_Sumo.png", 58, 58));
        pIzq.add(lblIcono);

        JLabel lblTitulo = new JLabel("¡COMBATE DE SUMO!", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 36));
        lblTitulo.setForeground(C_BLANCO);

        JPanel pDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pDer.setOpaque(false);
        JLabel lblBandera = new JLabel();
        lblBandera.setIcon(escalarIcono(REC + "japones.png", 58, 58));
        pDer.add(lblBandera);

        p.add(pIzq,      BorderLayout.WEST);
        p.add(lblTitulo, BorderLayout.CENTER);
        p.add(pDer,      BorderLayout.EAST);
        return p;
    }

    /** Centro: panel combate (izquierda) + log (derecha) */
    private JPanel construirCentro() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel pIzq = new JPanel(new BorderLayout());
        pIzq.setOpaque(false);
        pIzq.add(panelCombate,              BorderLayout.CENTER);
        pIzq.add(construirInfoLuchadores(), BorderLayout.SOUTH);

        p.add(pIzq,               BorderLayout.CENTER);
        p.add(construirPanelLog(), BorderLayout.EAST);
        return p;
    }

    /** Fila de luchadores + panel ganador */
    private JPanel construirInfoLuchadores() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);

        JPanel pFila = new JPanel(new GridLayout(1, 2, 2, 0));
        pFila.setOpaque(false);
        pFila.add(construirPanelNombre(lblNombreL1a, lblNombreL1b, C_AZUL));
        pFila.add(construirPanelNombre(lblNombreL2a, lblNombreL2b, C_ROJO_DARK));

        p.add(pFila,        BorderLayout.NORTH);
        p.add(panelGanador, BorderLayout.CENTER);
        return p;
    }

    /**
     * Panel de nombre de un luchador usando dos JLabels apilados (fix #3 — sin HTML).
     */
    private JPanel construirPanelNombre(JLabel lbl1, JLabel lbl2, Color fondo) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(fondo);
        p.setBorder(new EmptyBorder(10, 16, 10, 16));
        p.add(lbl1);
        p.add(lbl2);
        return p;
    }

    /** Panel ganador dorado */
    private JPanel construirPanelGanador() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_DORADO);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.add(lblGanador, BorderLayout.CENTER);
        return p;
    }

    /** Panel derecho: log + barra estado */
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
        p.add(scroll,    BorderLayout.CENTER);
        p.add(pEstado,   BorderLayout.SOUTH);
        return p;
    }

    // ─── Fábricas ─────────────────────────────────────────────────────────────

    private JLabel crearLblLuchador(String txt, int estilo, int tam) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(new Font("Serif", estilo, tam));
        l.setForeground(C_BLANCO);
        return l;
    }

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

    private JLabel crearLblEstado() {
        JLabel l = new JLabel("*MENSAJE CON ESTADO ACTUAL E INSTRUCCIONES*",
                               SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 13));
        l.setForeground(C_BLANCO);
        return l;
    }

    // ─── Carga de imágenes ────────────────────────────────────────────────────

    private Image cargarImagen(String ruta, int ancho, int alto) {
        try {
            File f = new File(ruta);
            if (!f.exists()) return null;
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            if (ancho == -1) return raw.getImage();
            return raw.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        } catch (Exception e) { return null; }
    }

    private ImageIcon escalarIcono(String ruta, int ancho, int alto) {
        Image img = cargarImagen(ruta, ancho, alto);
        return img != null ? new ImageIcon(img) : new ImageIcon();
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /** Agrega una línea al log con scroll automático */
    public void mostrarMensaje(String msg) {
        areaLog.append(msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void actualizarEstado(String msg) { lblEstado.setText(msg); }

    /** Actualiza nombre del luchador usando dos JLabels (fix #3) */
    public void mostrarLuchadorEnDohyo(String nombre, double peso, int indice) {
        String linea2 = nombre + "  (" + String.format("%.1f", peso) + " kg)";
        if (indice == 0) {
            lblNombreL1a.setText("🥋  LUCHADOR 1");
            lblNombreL1b.setText(linea2);
        } else {
            lblNombreL2a.setText("🥋  LUCHADOR 2");
            lblNombreL2b.setText(linea2);
        }
        panelCombate.repaint();
    }

    public void mostrarInicioCombate(String n1, String n2) {
        actualizarEstado("¡COMBATE INICIADO!  " + n1 + "  VS  " + n2);
    }

    /**
     * Muestra imagen del kimarite sobre el dohyo.
     * Busca Data/Imagenes_tecnicas/[nombreKimarite].png (o .jpg / .jpeg).
     */
    public void mostrarKimarite(String nombreLuchador, String nombreKimarite,
                                 boolean expulsado) {
        panelCombate.cargarKimarite(nombreKimarite, expulsado);
        String res = expulsado ? " ¡EXPULSADO!" : " El oponente resiste";
        actualizarEstado(nombreLuchador + " usa [" + nombreKimarite + "] —" + res);
    }

    /**
     * Muestra el ganador y cierra el servidor (fix #2).
     * Espera 4 segundos para que el usuario pueda leer el resultado.
     */
    public void mostrarGanador(String nombreGanador, int victorias) {
        lblGanador.setText("🏆  GANADOR : " + nombreGanador
            + "  |  Numero de victorias : " + victorias);
        actualizarEstado("Combate finalizado. Ganador: " + nombreGanador);

        // Cerrar el servidor 4 segundos después (fix #2)
        Timer timer = new Timer(4000, e -> System.exit(0));
        timer.setRepeats(false);
        timer.start();
    }

    // ─── Inner class: PanelCombate ────────────────────────────────────────────

    /**
     * Panel central que muestra el fondo del dohyo, los luchadores desde el
     * inicio (fix #1) y la imagen del kimarite ejecutado superpuesta.
     */
    private class PanelCombate extends JPanel {

        /** Imagen de fondo del dojo */
        private final Image imgFondo;
        /** Luchador izquierdo — visible desde el inicio (fix #1) */
        private final Image imgL1;
        /** Luchador derecho — visible desde el inicio (fix #1) */
        private final Image imgL2;
        /** Imagen del kimarite activo */
        private Image   imgKimarite    = null;
        private String  nombreKimarite = "";
        private Color   colorKimarite  = Color.WHITE;

        public PanelCombate() {
            setOpaque(true);
            imgFondo = cargarImagen(REC + "Dohyo.png", -1, -1);
            imgL1    = cargarImagen(REC + "Luchador1.png", -1, -1);
            imgL2    = cargarImagen(REC + "Luchador2.png", -1, -1);
        }

        /** Carga la imagen del kimarite y repinta */
        public void cargarKimarite(String nombre, boolean expulsado) {
            nombreKimarite = nombre;
            colorKimarite  = expulsado
                ? new Color(255, 80, 80)
                : new Color(100, 240, 130);
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();

            // Fondo del dojo
            if (imgFondo != null)
                g2.drawImage(imgFondo, 0, 0, w, h, null);
            else {
                g2.setColor(new Color(60, 45, 30));
                g2.fillRect(0, 0, w, h);
            }

            // Silueta negra semitransparente (fix #5)
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRect(0, 0, w, h);

            // Luchadores visibles desde el inicio (fix #1)
            // Luchador 1 (izquierda)
            pintarLuchador(g2, imgL1, w, h, false);
            // Luchador 2 (derecha)
            pintarLuchador(g2, imgL2, w, h, true);

            // Imagen del kimarite centrada sobre el dohyo (fix #7)
            if (imgKimarite != null)
                pintarKimarite(g2, w, h);

            g2.dispose();
        }

        /**
         * Dibuja un luchador centrado verticalmente en su mitad del panel.
         * Los luchadores aparecen desde el inicio del servidor (fix #1).
         *
         * @param derecho true = lado derecho, false = lado izquierdo
         */
        private void pintarLuchador(Graphics2D g2, Image img,
                                     int w, int h, boolean derecho) {
            if (img == null) return;

            int mitad   = w / 2;
            int maxW    = (int)(mitad * 0.55);
            int maxH    = (int)(h     * 0.68);
            double esc  = Math.min(
                (double) maxW / img.getWidth(null),
                (double) maxH / img.getHeight(null));
            int rw = (int)(img.getWidth(null)  * esc);
            int rh = (int)(img.getHeight(null) * esc);

            // Centrar horizontalmente en su mitad, verticalmente a 15% del top
            int cx  = derecho ? (mitad + mitad / 2) : (mitad / 2);
            int lx  = cx - rw / 2;
            int ly  = (int)(h * 0.15);

            g2.drawImage(img, lx, ly, rw, rh, null);
        }

        /** Dibuja la imagen del kimarite centrada sobre el ring */
        private void pintarKimarite(Graphics2D g2, int w, int h) {
            int maxW   = (int)(w * 0.28);
            int maxH   = (int)(h * 0.38);
            int origW  = imgKimarite.getWidth(null);
            int origH  = imgKimarite.getHeight(null);
            if (origW <= 0 || origH <= 0) return;

            double esc = Math.min((double) maxW / origW, (double) maxH / origH);
            int rw = (int)(origW * esc);
            int rh = (int)(origH * esc);
            // Centro del panel, ligeramente arriba del centro vertical (fix #7)
            int kx = w / 2 - rw / 2;
            int ky = (int)(h * 0.32) - rh / 2;

            // Fondo oscuro detrás de la imagen
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(kx - 12, ky - 10, rw + 24, rh + 30, 14, 14);

            g2.drawImage(imgKimarite, kx, ky, rw, rh, null);

            // Nombre del kimarite debajo
            g2.setFont(new Font("Serif", Font.BOLD, 13));
            g2.setColor(colorKimarite);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nombreKimarite,
                w / 2 - fm.stringWidth(nombreKimarite) / 2,
                ky + rh + 18);
        }
    }
}
