package co.edu.udistrital.sumo.vista.cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Vista del cliente en la arquitectura MVC del Combate de Sumo.
 *
 * Propósito: Definir y organizar ÚNICAMENTE los componentes visuales del cliente.
 * Expone componentes con getters para que {@link co.edu.udistrital.sumo.controlador.ControladorCliente}
 * asigne los listeners. Centraliza el JFileChooser (pertenece a la Vista: SRP).
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.ControladorCliente}.
 * Principio SOLID:
 * S — única responsabilidad: construcción y layout de la interfaz del cliente.
 *
 * ESTRUCTURA DE IMÁGENES ESPERADA en data/Recursos/:
 * - fondo_cliente.jpg   → imagen de fondo de la ventana
 * - luchador.png        → imagen del luchador que aparece en el panel derecho
 * - icono_sumo.png      → ícono pequeño del título (opcional)
 *
 * PROHIBIDO en esta clase: objetos Rikishi/Kimarite, lógica de negocio, SQL, sockets.
 *
 * @author Grupo Taller 3
 * @version 2.0
 * @see co.edu.udistrital.sumo.controlador.ControladorCliente
 */
public class VistaCliente extends JFrame {

    // ── Rutas de recursos ─────────────────────────────────────────────────────
    /** Carpeta de imágenes generales — cambiar si la estructura cambia */
    private static final String RUTA_RECURSOS = "data/Recursos/";

    // ── Paleta de colores temática japonesa ───────────────────────────────────
    /** Fondo oscuro principal */
    private static final Color C_FONDO   = new Color(15, 12, 25);
    /** Rojo torii japonés — botón principal */
    private static final Color C_ROJO    = new Color(190, 20, 20);
    /** Dorado imperial — acentos y títulos */
    private static final Color C_DORADO  = new Color(212, 170, 45);
    /** Blanco cálido — texto */
    private static final Color C_BLANCO  = new Color(240, 232, 215);
    /** Panel semitransparente */
    private static final Color C_PANEL   = new Color(20, 16, 35, 220);
    /** Fondo de campos de texto */
    private static final Color C_CAMPO   = new Color(30, 25, 45);
    /** Texto secundario grisáceo */
    private static final Color C_GRIS    = new Color(150, 145, 160);
    /** Azul del botón cargar */
    private static final Color C_AZUL    = new Color(30, 80, 160);

    // ── Imagen de fondo ───────────────────────────────────────────────────────
    /** Cargada desde data/Recursos/fondo_cliente.jpg */
    private BufferedImage imgFondo;

    // ── Componentes de la UI ──────────────────────────────────────────────────
    /** Campo nombre del luchador */
    private final JTextField txtNombre;
    /** Campo peso del luchador */
    private final JTextField txtPeso;
    /** Lista de kimarites disponibles con selección múltiple */
    private final JList<String> listKimarites;
    /** Modelo de datos de la lista */
    private final DefaultListModel<String> modeloLista;
    /** Botón para cargar el archivo .properties */
    private final JButton btnCargarKimarites;
    /** Botón para conectarse al servidor */
    private final JButton btnConectar;
    /** Etiqueta de estado en la barra inferior */
    private final JLabel lblEstado;
    /** Panel derecho con imagen del luchador */
    private final PanelLuchador panelLuchador;
    /** Diálogo de selección de archivos (pertenece a la Vista: SRP) */
    private final JFileChooser fileChooser;

    /**
     * Construye y configura la ventana del cliente con el tema visual de sumo.
     */
    public VistaCliente() {
        super("Combate de Sumo - Registro de Luchador");
        cargarFondo();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        // Inicializar componentes
        txtNombre          = crearCampo();
        txtPeso            = crearCampo();
        modeloLista        = new DefaultListModel<>();
        listKimarites      = crearLista();
        btnCargarKimarites = crearBoton("Cargar Kimarites", C_AZUL);
        btnConectar        = crearBoton("ENTRAR AL DOHYO", C_ROJO);
        lblEstado          = crearEtiqEstado();
        panelLuchador      = new PanelLuchador();

        // JFileChooser configurado para .properties (Vista gestiona el diálogo: SRP)
        fileChooser = new JFileChooser(new File(RUTA_RECURSOS));
        fileChooser.setFileFilter(
            new FileNameExtensionFilter("Archivos de propiedades (*.properties)", "properties"));
        fileChooser.setDialogTitle("Seleccionar archivo de kimarites");

        construirLayout();
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    /** Construye el layout principal de la ventana */
    private void construirLayout() {
        // Panel raíz con imagen de fondo dibujada en paintComponent
        JPanel raiz = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imgFondo != null) {
                    g.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), null);
                    // Overlay oscuro para legibilidad
                    g.setColor(new Color(8, 5, 18, 185));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setPaint(new GradientPaint(
                        0, 0, new Color(10, 8, 20),
                        getWidth(), getHeight(), new Color(28, 8, 8)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        raiz.setOpaque(false);
        setContentPane(raiz);

        raiz.add(construirEncabezado(), BorderLayout.NORTH);
        raiz.add(construirCentro(),     BorderLayout.CENTER);
        raiz.add(construirEstado(),     BorderLayout.SOUTH);
    }

    /** Encabezado con ícono, kanji y título */
    private JPanel construirEncabezado() {
        JPanel p = new JPanel(new BorderLayout(10, 4));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 25, 10, 25));

        // Kanji 相撲 grande
        JLabel lblKanji = new JLabel("相撲", SwingConstants.CENTER);
        lblKanji.setFont(new Font("Serif", Font.BOLD, 52));
        lblKanji.setForeground(C_DORADO);

        // Título principal
        JLabel lblTitulo = new JLabel("REGISTRO DEL RIKISHI", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 17));
        lblTitulo.setForeground(C_BLANCO);
        lblTitulo.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Ícono opcional desde data/Recursos/icono_sumo.png
        JLabel lblIcono = new JLabel();
        cargarImagenEnLabel(lblIcono, RUTA_RECURSOS + "icono_sumo.png", 55, 55);
        lblIcono.setBorder(new EmptyBorder(0, 0, 0, 15));

        JPanel pTitulos = new JPanel(new BorderLayout());
        pTitulos.setOpaque(false);
        pTitulos.add(lblKanji,  BorderLayout.CENTER);
        pTitulos.add(lblTitulo, BorderLayout.SOUTH);

        // Separador dorado
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(C_DORADO.getRed(), C_DORADO.getGreen(),
                                     C_DORADO.getBlue(), 100));

        p.add(lblIcono,  BorderLayout.WEST);
        p.add(pTitulos,  BorderLayout.CENTER);
        p.add(sep,       BorderLayout.SOUTH);
        return p;
    }

    /** Panel central: formulario izquierdo + imagen del luchador derecho */
    private JPanel construirCentro() {
        JPanel p = new JPanel(new GridLayout(1, 2, 15, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 25, 8, 25));
        p.add(construirFormulario());
        p.add(panelLuchador);
        return p;
    }

    /** Formulario de registro con campos, lista de kimarites y botón */
    private JPanel construirFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(C_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(C_DORADO.getRed(), C_DORADO.getGreen(),
                           C_DORADO.getBlue(), 140), 1),
            new EmptyBorder(18, 18, 14, 18)));

        // Campos nombre y peso
        JPanel pCampos = new JPanel(new GridBagLayout());
        pCampos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.weightx=0;
        pCampos.add(etiq("Nombre del Rikishi:"), g);
        g.gridx=1; g.weightx=1;
        pCampos.add(txtNombre, g);

        g.gridx=0; g.gridy=1; g.weightx=0;
        pCampos.add(etiq("Peso (kg):"), g);
        g.gridx=1; g.weightx=1;
        pCampos.add(txtPeso, g);

        g.gridx=0; g.gridy=2; g.gridwidth=2; g.insets=new Insets(12,0,4,0);
        pCampos.add(btnCargarKimarites, g);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(70, 60, 90));
        g.gridy=3; g.insets=new Insets(6,0,6,0);
        pCampos.add(sep, g);

        p.add(pCampos, BorderLayout.NORTH);

        // Lista de kimarites con borde titulado
        JScrollPane scroll = new JScrollPane(listKimarites);
        scroll.setBackground(new Color(12, 10, 22));
        scroll.getViewport().setBackground(new Color(12, 10, 22));
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 60, 90), 1),
            "Seleccionar Kimarites (Ctrl+clic)");
        tb.setTitleColor(C_DORADO);
        tb.setTitleFont(new Font("Serif", Font.BOLD, 11));
        scroll.setBorder(tb);
        p.add(scroll, BorderLayout.CENTER);

        // Botón conectar al fondo
        JPanel pBot = new JPanel(new BorderLayout());
        pBot.setOpaque(false);
        pBot.setBorder(new EmptyBorder(8, 0, 0, 0));
        pBot.add(btnConectar, BorderLayout.CENTER);
        p.add(pBot, BorderLayout.SOUTH);

        return p;
    }

    /** Barra de estado inferior */
    private JPanel construirEstado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(5, 4, 10, 220));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0,
                new Color(C_DORADO.getRed(), C_DORADO.getGreen(),
                           C_DORADO.getBlue(), 90)),
            new EmptyBorder(7, 18, 7, 18)));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // ─── Fábricas de componentes ──────────────────────────────────────────────

    /** Crea un JTextField con el estilo del tema */
    private JTextField crearCampo() {
        JTextField t = new JTextField();
        t.setBackground(C_CAMPO);
        t.setForeground(C_BLANCO);
        t.setCaretColor(C_DORADO);
        t.setFont(new Font("Serif", Font.PLAIN, 13));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(65, 55, 85), 1),
            new EmptyBorder(5, 9, 5, 9)));
        return t;
    }

    /** Crea un JList de selección múltiple con el estilo del tema */
    private JList<String> crearLista() {
        JList<String> l = new JList<>(modeloLista);
        l.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        l.setBackground(new Color(12, 10, 22));
        l.setForeground(C_BLANCO);
        l.setFont(new Font("Serif", Font.PLAIN, 12));
        l.setSelectionBackground(new Color(180, 20, 20, 200));
        l.setSelectionForeground(Color.WHITE);
        l.setFixedCellHeight(26);
        l.setBorder(new EmptyBorder(4, 8, 4, 8));
        return l;
    }

    /** Crea un JButton estilizado */
    private JButton crearBoton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Serif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(11, 20, 11, 20));
        return b;
    }

    /** Crea una etiqueta de campo con el estilo dorado */
    private JLabel etiq(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(C_DORADO);
        l.setFont(new Font("Serif", Font.BOLD, 13));
        return l;
    }

    /** Crea la etiqueta de estado inferior */
    private JLabel crearEtiqEstado() {
        JLabel l = new JLabel("Cargue el archivo de kimarites para comenzar.");
        l.setForeground(new Color(130, 190, 130));
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return l;
    }

    // ─── Carga de imágenes ────────────────────────────────────────────────────

    /** Carga el fondo desde data/Recursos/fondo_cliente.jpg */
    private void cargarFondo() {
        try {
            imgFondo = ImageIO.read(new File(RUTA_RECURSOS + "fondo_cliente.jpg"));
        } catch (IOException e) {
            imgFondo = null; // usa degradado como fallback
        }
    }

    /**
     * Carga una imagen en un JLabel escalada al tamaño dado.
     * Falla silenciosamente si el archivo no existe.
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

    // ─── Métodos públicos de la vista ─────────────────────────────────────────

    /**
     * Abre el JFileChooser para que el usuario seleccione el archivo .properties.
     * El controlador recibe solo la ruta — la Vista gestiona el diálogo (SRP).
     *
     * @return ruta absoluta del archivo seleccionado, o null si canceló
     */
    public String seleccionarRutaProperties() {
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    /** Carga y muestra la lista de kimarites en el componente de selección */
    public void cargarListaKimarites(List<String> nombres) {
        modeloLista.clear();
        for (String n : nombres) modeloLista.addElement(n);
    }

    /** Retorna los kimarites seleccionados por el usuario */
    public List<String> getKimaritesSeleccionados() {
        List<String> sel = new ArrayList<>();
        for (String k : listKimarites.getSelectedValuesList()) sel.add(k);
        return sel;
    }

    /** Retorna el nombre ingresado */
    public String getNombreLuchador() { return txtNombre.getText(); }

    /** Retorna el peso ingresado */
    public String getPesoLuchador() { return txtPeso.getText(); }

    /** Actualiza la barra de estado */
    public void mostrarEstado(String msg) { lblEstado.setText(msg); }

    /**
     * Muestra un diálogo informativo.
     * Permitido por el enunciado para mensajes indicativos.
     */
    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Muestra el resultado final del combate en el panel del luchador */
    public void mostrarResultadoCombate(boolean gano) {
        panelLuchador.mostrarResultado(gano);
        String titulo = gano ? "¡GANASTE!" : "Perdiste";
        String msg    = gano
            ? "¡Felicitaciones! Tu rikishi gano el combate de sumo."
            : "Tu rikishi fue expulsado del dohyo. Mejor suerte la proxima vez.";
        JOptionPane.showMessageDialog(this, msg, titulo,
            gano ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        dispose();
    }

    /** Habilita o deshabilita el botón de conexión */
    public void setBtnConectarHabilitado(boolean h) { btnConectar.setEnabled(h); }

    /** Retorna el botón cargar kimarites para asignar ActionListener */
    public JButton getBtnCargarKimarites() { return btnCargarKimarites; }

    /** Retorna el botón conectar para asignar ActionListener */
    public JButton getBtnConectar() { return btnConectar; }

    // ─── Inner class: Panel del luchador ──────────────────────────────────────

    /**
     * Panel derecho que muestra la imagen del luchador (data/Recursos/luchador.png)
     * y el resultado del combate al finalizar.
     */
    private static class PanelLuchador extends JPanel {

        /** Imagen del luchador cargada desde data/Recursos/luchador.png */
        private BufferedImage imgLuchador;
        /** null = esperando, true = ganó, false = perdió */
        private Boolean resultado = null;

        /** Construye el panel y carga la imagen del luchador */
        public PanelLuchador() {
            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(
                new Color(212, 170, 45, 100), 1));
            try {
                imgLuchador = ImageIO.read(new File("data/Recursos/luchador.png"));
            } catch (IOException e) {
                imgLuchador = null;
            }
        }

        /** Muestra el resultado del combate en el panel */
        public void mostrarResultado(boolean gano) {
            this.resultado = gano;
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
            int cx = w / 2;

            // Fondo del panel
            g2.setColor(new Color(15, 12, 25, 210));
            g2.fillRect(0, 0, w, h);

            if (resultado == null) {
                // Mostrar imagen del luchador o figura fallback
                if (imgLuchador != null) {
                    // Centrar imagen preservando proporción
                    int iw = (int)(h * 0.65);
                    int ih = (int)(h * 0.78);
                    g2.drawImage(imgLuchador,
                        cx - iw / 2, (int)(h * 0.08), iw, ih, null);
                } else {
                    pintarFiguraFallback(g2, cx, h / 2 - 20, h);
                }

                // Texto DOHYO al fondo
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                g2.setColor(new Color(212, 170, 45, 140));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "DOHYO";
                g2.drawString(txt, cx - fm.stringWidth(txt) / 2, h - 20);

            } else if (resultado) {
                g2.setColor(new Color(0, 120, 50, 180));
                g2.fillRect(0, 0, w, h);
                g2.setFont(new Font("Serif", Font.BOLD, 26));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String t = "¡GANASTE!";
                g2.drawString(t, cx - fm.stringWidth(t) / 2, h / 2);
            } else {
                g2.setColor(new Color(140, 15, 15, 180));
                g2.fillRect(0, 0, w, h);
                g2.setFont(new Font("Serif", Font.BOLD, 24));
                g2.setColor(new Color(245, 210, 210));
                FontMetrics fm = g2.getFontMetrics();
                String t = "PERDISTE";
                g2.drawString(t, cx - fm.stringWidth(t) / 2, h / 2);
            }
            g2.dispose();
        }

        /** Dibuja una figura de luchador simplificada como fallback si no hay imagen */
        private void pintarFiguraFallback(Graphics2D g2, int cx, int cy, int h) {
            int escala = h / 8;
            Color piel = new Color(218, 185, 145);
            Color faja = new Color(190, 20, 20);
            g2.setColor(piel);
            g2.fillOval(cx - escala, cy - escala * 2, escala * 2, escala * 3);
            g2.fillOval(cx - (int)(escala * 0.7), cy - escala * 4,
                        (int)(escala * 1.4), (int)(escala * 1.4));
            g2.setColor(faja);
            g2.fillRoundRect(cx - escala, cy - (int)(escala * 0.6),
                             escala * 2, (int)(escala * 0.9), 4, 4);
        }
    }
}
