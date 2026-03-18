package co.edu.udistrital.sumo.vista.servidor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Vista del servidor en la arquitectura MVC del Combate de Sumo.
 *
 * Propósito: Mostrar visualmente el desarrollo del combate en tiempo real:
 * llegada de luchadores, cada kimarite ejecutado con su imagen correspondiente,
 * log del combate y anuncio del ganador.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorServidor}.
 * Principio SOLID:
 * S — única responsabilidad: presentación del servidor.
 *
 * ESTRUCTURA DE IMÁGENES ESPERADA en Data/Recursos/:
 * - Fondo_Japones.png  → imagen de fondo de la ventana
 * - Dohyo.png          → imagen del dohyō (opcional, dibujado en código)
 * - Luchador1.png      → luchador 1 (izquierda, azul)
 * - Luchador2.png      → luchador 2 (derecha, rojo)
 * - Logo_Sumo.png      → logo del encabezado
 *
 * ESTRUCTURA IMÁGENES KIMARITES en Data/Imagenes_Kimarites/:
 * - [NombreKimarite].gif  (ej: Hatakikomi.gif, Yorikiri.gif)
 * El nombre del archivo debe coincidir exactamente con el kimarite del properties.
 *
 * PROHIBIDO en esta clase: objetos Rikishi/Kimarite, lógica de negocio, sockets.
 *
 * @author Grupo Taller 3
 * @version 3.0
 * @see co.edu.udistrital.sumo.controlador.ControladorServidor
 */
public class VistaServidor extends JFrame {

    // ── Rutas de recursos ─────────────────────────────────────────────────────
    //Carpeta de imágenes generales — debe coincidir con la estructura del proyecto
    static final String RUTA_RECURSOS  = "Data/Recursos/";
    //Carpeta de imágenes de técnicas kimarite
    static final String RUTA_KIMARITES = "Data/Imagenes_Kimarites/";

    // ── Paleta de colores ─────────────────────────────────────────────────────
    static final Color C_NEGRO  = new Color(8, 6, 14);
    static final Color C_DORADO = new Color(212, 170, 45);
    static final Color C_BLANCO = new Color(240, 232, 215);
    static final Color C_ROJO   = new Color(190, 20, 20);
    static final Color C_AZUL   = new Color(55, 110, 200);
    static final Color C_GRIS   = new Color(140, 135, 155);
    static final Color C_VERDE  = new Color(50, 175, 75);

    // ── Imagen de fondo ───────────────────────────────────────────────────────
    //Cargada desde Data/Recursos/Fondo_Japones.png
    private BufferedImage imgFondo;

    // ── Componentes principales ───────────────────────────────────────────────
    //Panel central animado del dohyo
    private final PanelDohyo panelDohyo;
    //Área de log del combate
    private final JTextArea  areaLog;
    //Etiqueta nombre luchador 1
    private final JLabel lblLuchador1;
    //Etiqueta nombre luchador 2
    private final JLabel lblLuchador2;
    //Etiqueta del último kimarite ejecutado
    private final JLabel lblUltimoKimarite;
    //Etiqueta de estado general
    private final JLabel lblEstado;
    //Panel del ganador (inicialmente oculto)
    private final JPanel panelGanador;
    //Etiqueta del nombre del ganador
    private final JLabel lblGanador;

    /**
     * Construye y configura la ventana del servidor.
     */
    public VistaServidor() {
        super("Servidor - Combate de Sumo | Dohyo");
        cargarFondo();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        panelDohyo        = new PanelDohyo();
        areaLog           = crearAreaLog();
        lblLuchador1      = crearEtiqLuchador("Esperando luchador 1...", C_AZUL);
        lblLuchador2      = crearEtiqLuchador("Esperando luchador 2...", C_ROJO);
        lblUltimoKimarite = crearEtiqKimarite();
        lblEstado         = crearEtiqEstado();
        lblGanador        = new JLabel("", SwingConstants.CENTER);
        lblGanador.setFont(new Font("Serif", Font.BOLD, 18));
        lblGanador.setForeground(C_DORADO);
        panelGanador      = construirPanelGanador();

        construirLayout();
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    //Construye el layout principal de la ventana
    private void construirLayout() {
        /*
         * FIX 1: setOpaque(true) — el panel raíz DEBE ser opaco porque es el
         * contentPane. Si es no-opaco, Swing intenta pintar el padre detrás de
         * él causando artefactos visuales y corrupción de rendering.
         * El fondo se dibuja en paintComponent, que cubre todo el panel.
         */
        JPanel raiz = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                //No llamar a super para evitar el fondo gris predeterminado
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                    RenderingHints.VALUE_RENDER_QUALITY);
                if (imgFondo != null) {
                    //Dibujar imagen de fondo escalada al tamaño de la ventana
                    g2.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), null);
                    //Overlay oscuro para legibilidad del contenido
                    g2.setColor(new Color(5, 4, 12, 188));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    //Degradado fallback si no existe la imagen de fondo
                    g2.setPaint(new GradientPaint(
                        0, 0, new Color(8, 6, 14),
                        getWidth(), getHeight(), new Color(22, 8, 8)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        raiz.setOpaque(true); // FIX 1: debe ser true
        setContentPane(raiz);

        raiz.add(construirEncabezado(), BorderLayout.NORTH);
        raiz.add(construirCentro(),     BorderLayout.CENTER);
        raiz.add(construirEstado(),     BorderLayout.SOUTH);
    }

    //Encabezado con título, logo y paneles de luchadores
    private JPanel construirEncabezado() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 22, 8, 22));

        //Panel superior: logo + título
        JPanel pTop = new JPanel(new BorderLayout(12, 0));
        pTop.setOpaque(false);

        //Logo desde Data/Recursos/Logo_Sumo.png
        JLabel lblLogo = new JLabel();
        cargarImagenEnLabel(lblLogo, RUTA_RECURSOS + "Logo_Sumo.png", 55, 55);

        JPanel pTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        pTitulos.setOpaque(false);
        JLabel lbl1 = new JLabel("相撲  —  EL COMBATE", SwingConstants.CENTER);
        lbl1.setFont(new Font("Serif", Font.BOLD, 24));
        lbl1.setForeground(C_DORADO);
        JLabel lbl2 = new JLabel("Programación Avanzada  ·  UDFJC",
                                  SwingConstants.CENTER);
        lbl2.setFont(new Font("Serif", Font.PLAIN, 11));
        lbl2.setForeground(C_GRIS);
        pTitulos.add(lbl1);
        pTitulos.add(lbl2);

        pTop.add(lblLogo,   BorderLayout.WEST);
        pTop.add(pTitulos,  BorderLayout.CENTER);
        p.add(pTop, BorderLayout.NORTH);

        //Fila de luchadores: L1 | VS | L2
        JPanel pLuch = new JPanel(new GridLayout(1, 3, 12, 0));
        pLuch.setOpaque(false);
        //FIX 3 y 4: nombres correctos de las imágenes disponibles
        pLuch.add(construirPanelLuchador(lblLuchador1, C_AZUL, "Luchador1.png"));
        pLuch.add(construirVS());
        pLuch.add(construirPanelLuchador(lblLuchador2, C_ROJO, "Luchador2.png"));
        p.add(pLuch, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(C_DORADO.getRed(), C_DORADO.getGreen(),
                                     C_DORADO.getBlue(), 90));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    //Panel de un luchador en el encabezado con imagen y nombre
    private JPanel construirPanelLuchador(JLabel lblNombre, Color color,
                                           String nombreImagen) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(new Color(15, 12, 24, 200));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 130), 1),
            new EmptyBorder(8, 10, 8, 10)));

        JLabel lblImg = new JLabel();
        cargarImagenEnLabel(lblImg, RUTA_RECURSOS + nombreImagen, 48, 58);
        if (lblImg.getIcon() == null) {
            lblImg.setPreferredSize(new Dimension(48, 58));
            lblImg.setBackground(new Color(color.getRed(), color.getGreen(),
                                            color.getBlue(), 60));
            lblImg.setOpaque(true);
        }
        p.add(lblImg,    BorderLayout.WEST);
        p.add(lblNombre, BorderLayout.CENTER);
        return p;
    }

    //Panel VS central
    private JPanel construirVS() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel vs = new JLabel("VS", SwingConstants.CENTER);
        vs.setFont(new Font("Serif", Font.BOLD, 28));
        vs.setForeground(new Color(200, 195, 100));
        p.add(vs, BorderLayout.CENTER);
        return p;
    }

    //Panel central: dohyo izquierdo + log derecho
    private JPanel construirCentro() {
        JPanel p = new JPanel(new GridLayout(1, 2, 15, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 22, 8, 22));

        JPanel pIzq = new JPanel(new BorderLayout(0, 6));
        pIzq.setOpaque(false);
        pIzq.add(panelDohyo, BorderLayout.CENTER);

        JPanel pFootIzq = new JPanel(new BorderLayout(0, 4));
        pFootIzq.setOpaque(false);
        pFootIzq.add(lblUltimoKimarite, BorderLayout.NORTH);
        pFootIzq.add(panelGanador,      BorderLayout.CENTER);
        pIzq.add(pFootIzq, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBackground(new Color(8, 6, 18));
        scroll.getViewport().setBackground(new Color(8, 6, 18));
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(
                new Color(C_DORADO.getRed(), C_DORADO.getGreen(),
                           C_DORADO.getBlue(), 110), 1),
            "  Log del Combate");
        tb.setTitleColor(C_DORADO);
        tb.setTitleFont(new Font("Serif", Font.BOLD, 12));
        scroll.setBorder(tb);

        p.add(pIzq);
        p.add(scroll);
        return p;
    }

    //Barra de estado inferior
    private JPanel construirEstado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(5, 4, 10));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0,
                new Color(C_DORADO.getRed(), C_DORADO.getGreen(),
                           C_DORADO.getBlue(), 80)),
            new EmptyBorder(7, 22, 7, 22)));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    //Panel del ganador, inicialmente oculto
    private JPanel construirPanelGanador() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(28, 70, 28, 235));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_DORADO, 2),
            new EmptyBorder(10, 12, 10, 12)));
        p.add(lblGanador, BorderLayout.CENTER);
        p.setVisible(false);
        return p;
    }

    // ─── Fábricas de componentes ──────────────────────────────────────────────

    private JTextArea crearAreaLog() {
        JTextArea a = new JTextArea();
        a.setBackground(new Color(8, 6, 18));
        a.setForeground(new Color(195, 190, 175));
        a.setFont(new Font("Monospaced", Font.PLAIN, 12));
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 10, 8, 10));
        return a;
    }

    private JLabel crearEtiqLuchador(String txt, Color color) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setForeground(color);
        l.setFont(new Font("Serif", Font.BOLD, 13));
        return l;
    }

    private JLabel crearEtiqKimarite() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setForeground(C_GRIS);
        l.setFont(new Font("Serif", Font.ITALIC, 12));
        l.setBackground(new Color(15, 12, 24));
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(5, 10, 5, 10));
        return l;
    }

    private JLabel crearEtiqEstado() {
        JLabel l = new JLabel("Servidor iniciado. Esperando luchadores...");
        l.setForeground(new Color(130, 190, 130));
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return l;
    }

    // ─── Carga de imágenes ────────────────────────────────────────────────────

    //Carga el fondo desde Data/Recursos/Fondo_Japones.png
    private void cargarFondo() {
        try {
            //FIX 3: usar el nombre real de la imagen disponible
            File f = new File(RUTA_RECURSOS + "Fondo_Japones.png");
            if (f.exists()) imgFondo = ImageIO.read(f);
        } catch (IOException e) {
            imgFondo = null;
        }
    }

    /**
     * Carga una imagen en un JLabel redimensionada. Falla silenciosamente.
     *
     * @param label JLabel destino
     * @param ruta  ruta del archivo de imagen
     * @param ancho ancho deseado en píxeles
     * @param alto  alto deseado en píxeles
     */
    private void cargarImagenEnLabel(JLabel label, String ruta, int ancho, int alto) {
        try {
            File f = new File(ruta);
            if (!f.exists()) return;
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            Image img = raw.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            label.setIcon(null);
        }
    }

    // ─── Métodos públicos de actualización ───────────────────────────────────

    //Agrega una línea al log con scroll automático al final
    public void mostrarMensaje(String msg) {
        areaLog.append(msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
        lblEstado.setText(msg);
    }

    //Actualiza el nombre del luchador en el encabezado y el dohyo
    public void mostrarLuchadorEnDohyo(String nombre, double peso, int indice) {
        String txt = String.format("%s  (%.1f kg)", nombre, peso);
        if (indice == 0) {
            lblLuchador1.setText(txt);
        } else {
            lblLuchador2.setText(txt);
        }
        panelDohyo.setNombreLuchador(nombre, indice);
    }

    //Actualiza el estado visual al inicio del combate
    public void mostrarInicioCombate(String nombreL1, String nombreL2) {
        lblEstado.setText("¡COMBATE INICIADO!  " + nombreL1 + "  vs  " + nombreL2);
        panelDohyo.setEstado(PanelDohyo.Estado.COMBATE);
    }

    /**
     * Muestra el kimarite ejecutado y carga su imagen desde Data/Imagenes_Kimarites/.
     * El nombre del archivo GIF debe coincidir con el nombre del kimarite.
     *
     * @param nombreLuchador nombre del atacante
     * @param nombreKimarite nombre de la técnica (= nombre del archivo sin extensión)
     * @param expulsado      true si el oponente fue expulsado
     */
    public void mostrarKimarite(String nombreLuchador, String nombreKimarite,
                                 boolean expulsado) {
        String res = expulsado ? "¡EXPULSADO!" : "El oponente resiste";
        lblUltimoKimarite.setText(
            String.format("  %s  [%s]  —  %s", nombreLuchador, nombreKimarite, res));
        lblUltimoKimarite.setForeground(expulsado ? C_ROJO : C_VERDE);
        panelDohyo.setKimarite(nombreKimarite, expulsado);
    }

    //Muestra el ganador y actualiza el dohyo
    public void mostrarGanador(String nombreGanador, int victorias) {
        panelDohyo.setEstado(PanelDohyo.Estado.TERMINADO);
        panelDohyo.setNombreGanador(nombreGanador);
        lblGanador.setText("GANADOR:  " + nombreGanador
            + "  |  " + victorias + " victoria(s)");
        panelGanador.setVisible(true);
        lblEstado.setText("Combate finalizado. Ganador: " + nombreGanador);
    }

    // ─── Inner class: Panel del Dohyo ────────────────────────────────────────

    /**
     * Panel animado que dibuja el dohyo, los luchadores y muestra la imagen
     * de cada kimarite ejecutado en tiempo real.
     */
    static class PanelDohyo extends JPanel {

        //Estados visuales del panel
        enum Estado { ESPERA, COMBATE, TERMINADO }

        private Estado estado = Estado.ESPERA;
        private final String[] nombres = {"?", "?"};
        private String nombreGanador = "";
        private String ultimoKimarite = "";
        private boolean ultimaExpulsion = false;

        //Imágenes de los luchadores (índice 0 = izquierdo, 1 = derecho)
        private final BufferedImage[] imgLuchadores = new BufferedImage[2];

        /*
         * FIX 5: Para GIFs se usa ImageIcon en lugar de ImageIO.read().
         * ImageIcon maneja animaciones y carga asíncrona correctamente.
         * imgKimariteIcon se usa para renderizar el GIF en el panel.
         */
        private ImageIcon imgKimariteIcon;

        //Ángulo de animación del ring decorativo
        private float anguloAnim = 0f;
        //Timer a ~30fps para el anillo giratorio
        private final Timer timerAnim;
        //Pulso de impacto cuando hay expulsión
        private int pulso = 0;

        //Construye el panel, carga imágenes de luchadores e inicia la animación
        public PanelDohyo() {
            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(
                new Color(212, 170, 45, 100), 1));

            //FIX 4: nombres correctos de las imágenes disponibles
            String[] archivos = {"Luchador1.png", "Luchador2.png"};
            for (int i = 0; i < 2; i++) {
                try {
                    File f = new File(RUTA_RECURSOS + archivos[i]);
                    if (f.exists()) imgLuchadores[i] = ImageIO.read(f);
                } catch (IOException ignored) {}
            }

            timerAnim = new Timer(33, e -> {
                anguloAnim = (anguloAnim + 0.7f) % 360f;
                if (pulso > 0) pulso--;
                repaint();
            });
            timerAnim.start();
        }

        //Actualiza el nombre de un luchador en el ring
        public void setNombreLuchador(String nombre, int indice) {
            nombres[indice] = nombre;
            repaint();
        }

        //Cambia el estado visual del panel
        public void setEstado(Estado e) {
            this.estado = e;
            if (e == Estado.TERMINADO) timerAnim.stop();
            repaint();
        }

        /**
         * Carga la imagen del kimarite usando ImageIcon (soporta GIF animado).
         * Busca en Data/Imagenes_Kimarites/ con extensiones .gif, .png, .jpg.
         *
         * @param nombreKimarite nombre de la técnica = nombre del archivo sin extensión
         * @param expulsado      true si el oponente fue expulsado
         */
        public void setKimarite(String nombreKimarite, boolean expulsado) {
            this.ultimoKimarite  = nombreKimarite;
            this.ultimaExpulsion = expulsado;
            if (expulsado) pulso = 10;

            imgKimariteIcon = null;
            //FIX 5: usar ImageIcon para GIFs — soporta animación y carga confiable
            for (String ext : new String[]{".gif", ".png", ".jpg", ".jpeg"}) {
                File f = new File(RUTA_KIMARITES + nombreKimarite + ext);
                if (f.exists()) {
                    imgKimariteIcon = new ImageIcon(f.getAbsolutePath());
                    //Registrar este panel como observer para que el GIF repinte
                    imgKimariteIcon.setImageObserver(this);
                    break;
                }
            }
            repaint();
        }

        //Establece el nombre del ganador para la pantalla final
        public void setNombreGanador(String ganador) {
            this.nombreGanador = ganador;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) { g2.dispose(); return; }

            int cx = w / 2, cy = h / 2 - 20;
            int r = Math.min(w, h - 80) / 2 - 18;
            if (r <= 0) { g2.dispose(); return; }

            pintarDecoracion(g2, cx, cy, r);
            pintarRing(g2, cx, cy, r);

            switch (estado) {
                case ESPERA:    pintarEspera(g2, cx, cy, r);       break;
                case COMBATE:   pintarCombate(g2, cx, cy, r);      break;
                case TERMINADO: pintarTerminado(g2, cx, cy, r);    break;
            }

            //Imagen del kimarite superpuesta al centro durante el combate
            if (estado == Estado.COMBATE && imgKimariteIcon != null) {
                pintarImagenKimarite(g2, cx, cy, r);
            }

            g2.dispose();
        }

        //Anillos decorativos giratorios exteriores
        private void pintarDecoracion(Graphics2D g2, int cx, int cy, int r) {
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 0, new float[]{5, 9}, anguloAnim));
            g2.setColor(new Color(212, 170, 45, 55));
            int re = r + 16;
            g2.drawOval(cx - re, cy - re, re * 2, re * 2);

            g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 0, new float[]{3, 11}, -anguloAnim * 1.4f));
            g2.setColor(new Color(190, 20, 20, 40));
            int rm = r + 9;
            g2.drawOval(cx - rm, cy - rm, rm * 2, rm * 2);
            g2.setStroke(new BasicStroke(1));
        }

        //Dibuja el ring de arena con bordes, gradiente y líneas shikiri-sen
        private void pintarRing(Graphics2D g2, int cx, int cy, int r) {
            //Sombra
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillOval(cx - r + 8, cy - r + 8, r * 2, r * 2);

            //Pulso de impacto al expulsar
            if (pulso > 0) {
                g2.setColor(new Color(1f, 0.4f, 0f, (pulso / 10.0f) * 0.35f));
                int pr = pulso * 4;
                g2.fillOval(cx - r - pr, cy - r - pr, (r + pr) * 2, (r + pr) * 2);
            }

            //Arena con degradado
            g2.setPaint(new GradientPaint(
                cx - r, cy - r, new Color(220, 186, 118),
                cx + r, cy + r, new Color(178, 142, 75)));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            //Líneas concéntricas sutiles
            g2.setColor(new Color(160, 125, 65, 35));
            for (int ri = r - 20; ri > 10; ri -= 22) {
                g2.drawOval(cx - ri, cy - ri, ri * 2, ri * 2);
            }

            //Borde de paja (tawara)
            g2.setColor(new Color(125, 62, 12));
            g2.setStroke(new BasicStroke(11));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            //Highlight del borde
            g2.setColor(new Color(180, 110, 45, 140));
            g2.setStroke(new BasicStroke(4));
            g2.drawArc(cx - r + 3, cy - r + 3, (r - 3) * 2, (r - 3) * 2, 30, 120);

            //Borde dorado
            g2.setColor(new Color(212, 170, 45, 160));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2);

            //Líneas shikiri-sen
            g2.setColor(new Color(85, 45, 12));
            g2.setStroke(new BasicStroke(3.5f));
            g2.drawLine(cx - 20, cy + 8, cx - 6, cy + 8);
            g2.drawLine(cx + 6,  cy + 8, cx + 20, cy + 8);
            g2.setStroke(new BasicStroke(1));
        }

        //Dibuja el estado de espera con nombres y texto
        private void pintarEspera(Graphics2D g2, int cx, int cy, int r) {
            g2.setFont(new Font("Serif", Font.BOLD, 13));
            g2.setColor(C_AZUL);
            g2.drawString(nombres[0], cx - r + 10, cy - 8);
            g2.setColor(C_ROJO);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nombres[1], cx + r - fm.stringWidth(nombres[1]) - 10, cy - 8);
            g2.setColor(new Color(200, 190, 100, 170));
            g2.setFont(new Font("Serif", Font.ITALIC, 13));
            fm = g2.getFontMetrics();
            String esp = "Aguardando luchadores...";
            g2.drawString(esp, cx - fm.stringWidth(esp) / 2, cy + 18);
        }

        //Dibuja los luchadores enfrentados durante el combate
        private void pintarCombate(Graphics2D g2, int cx, int cy, int r) {
            //Luchador izquierdo
            if (imgLuchadores[0] != null) {
                int iw = (int)(r * 0.65), ih = (int)(r * 0.85);
                g2.drawImage(imgLuchadores[0], cx - iw - 10, cy - ih / 2, iw, ih, null);
            } else {
                pintarRikishi(g2, cx - 42, cy, C_AZUL, false);
            }
            g2.setFont(new Font("Serif", Font.BOLD, 11));
            g2.setColor(C_AZUL);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nombres[0], cx - 42 - fm.stringWidth(nombres[0]) / 2, cy + 52);

            //Luchador derecho
            if (imgLuchadores[1] != null) {
                int iw = (int)(r * 0.65), ih = (int)(r * 0.85);
                g2.drawImage(imgLuchadores[1], cx + 10, cy - ih / 2, iw, ih, null);
            } else {
                pintarRikishi(g2, cx + 42, cy, C_ROJO, true);
            }
            g2.setColor(C_ROJO);
            fm = g2.getFontMetrics();
            g2.drawString(nombres[1], cx + 42 - fm.stringWidth(nombres[1]) / 2, cy + 52);
        }

        /**
         * Muestra la imagen del kimarite superpuesta en el centro del ring.
         * FIX 2: valida dimensiones antes de calcular escala para evitar
         * división por cero que corrompía el paintComponent.
         */
        private void pintarImagenKimarite(Graphics2D g2, int cx, int cy, int r) {
            Image img = imgKimariteIcon.getImage();
            //FIX 2: validar que la imagen tenga dimensiones válidas
            int origW = imgKimariteIcon.getIconWidth();
            int origH = imgKimariteIcon.getIconHeight();
            if (origW <= 0 || origH <= 0) return;

            int maxW = (int)(r * 0.85), maxH = (int)(r * 0.85);

            //Overlay semitransparente para que la imagen destaque
            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillOval(cx - (int)(r * 0.5), cy - (int)(r * 0.5),
                        r, r);

            //Escalar manteniendo proporción
            double escala = Math.min((double) maxW / origW, (double) maxH / origH);
            int iw = (int)(origW * escala);
            int ih = (int)(origH * escala);
            if (iw <= 0 || ih <= 0) return;

            g2.drawImage(img, cx - iw / 2, cy - ih / 2, iw, ih, null);

            //Nombre del kimarite debajo de la imagen
            g2.setFont(new Font("Serif", Font.BOLD, 11));
            g2.setColor(ultimaExpulsion ? C_ROJO : C_VERDE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ultimoKimarite,
                cx - fm.stringWidth(ultimoKimarite) / 2, cy + ih / 2 + 16);
        }

        //Pantalla final con el ganador
        private void pintarTerminado(Graphics2D g2, int cx, int cy, int r) {
            g2.setColor(new Color(212, 170, 45, 50));
            g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2.0, r * 2.0));
            g2.setFont(new Font("Serif", Font.BOLD, 20));
            g2.setColor(C_DORADO);
            FontMetrics fm = g2.getFontMetrics();
            String t1 = "GANADOR";
            g2.drawString(t1, cx - fm.stringWidth(t1) / 2, cy - 10);
            g2.setFont(new Font("Serif", Font.BOLD, 17));
            g2.setColor(Color.WHITE);
            fm = g2.getFontMetrics();
            g2.drawString(nombreGanador,
                cx - fm.stringWidth(nombreGanador) / 2, cy + 16);
            g2.setFont(new Font("Serif", Font.BOLD, 30));
            g2.setColor(new Color(212, 170, 45, 110));
            g2.drawString("勝", cx - 14, cy - r + 38);
        }

        //Figura de rikishi dibujada como fallback si no hay imagen
        private void pintarRikishi(Graphics2D g2, int x, int y,
                                    Color faja, boolean miraDer) {
            Color piel = new Color(218, 188, 148);
            int d = miraDer ? -1 : 1;
            g2.setColor(piel);
            g2.fillOval(x - 15, y - 12, 30, 33);
            g2.fillOval(x - 10, y - 30, 20, 20);
            g2.setColor(faja);
            g2.fillRoundRect(x - 13, y + 6, 26, 12, 4, 4);
            g2.setColor(piel);
            g2.fillOval(x + d * 14, y - 6, 12, 8);
            g2.setColor(faja.darker());
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(x - 15, y - 12, 30, 33);
            g2.setStroke(new BasicStroke(1));
        }
    }
}
