package co.edu.udistrital.sumo.vista.cliente;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista del cliente — Combate de Sumo.
 *
 * CORRECCIONES v4:
 * - Sin HTML en ningún componente — solo Java puro
 * - Campos de texto más altos y anchos
 * - Silueta/sombra oscura sobre el fondo floral
 * - Header: íconos pegados al título
 * - Imagen del luchador alterna entre Luchador1.png y Luchador2.png
 *   según cuántas instancias del cliente se hayan lanzado (contador estático)
 *
 * Recursos en Data/Recursos/:
 *   Fondo_Japones.png, Luchador1.png, Luchador2.png, Logo_Sumo.png,
 *   japones.png, samurai.png, flor-de-cerezo.png,
 *   gato-chino-de-la-suerte.png, japon.png
 *
 * PROHIBIDO: Rikishi/Kimarite, lógica de negocio, sockets, SQL, HTML.
 *
 * @author Grupo Taller 3
 * @version 4.0
 */
public class VistaCliente extends JFrame {

    // ── Rutas ─────────────────────────────────────────────────────────────────
    private static final String REC = "Data/Recursos/";

    // ── Contador estático para alternar imagen de luchador ───────────────────
    /** Lleva la cuenta de instancias creadas para asignar imagen diferente */
    private static int instancias = 0;

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color C_ROJO_PANEL = new Color(237, 85, 90);
    private static final Color C_AZUL       = new Color(70, 130, 210);
    private static final Color C_TEXTO      = new Color(25, 15, 10);
    private static final Color C_CAMPO      = new Color(250, 200, 200);
    private static final Color C_LISTA      = new Color(252, 215, 215);
    private static final Color C_BLANCO     = Color.WHITE;

    // ── Componentes ───────────────────────────────────────────────────────────
    private final JTextField           txtNombre;
    private final JTextField           txtPeso;
    private final DefaultListModel<String> modeloLista;
    private final JList<String>        listKimarites;
    private final JButton              btnCargarKimarites;
    private final JButton              btnConectar;
    private final JLabel               lblEstado;
    private final JFileChooser         fileChooser;

    /** Imagen de fondo floral */
    private final Image imgFondo;
    /** Imagen del luchador — alterna entre instancias */
    private final String rutaLuchador;

    public VistaCliente() {
        super("Combate de Sumo - Registro de Luchador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Alternar imagen: instancia par → Luchador1, impar → Luchador2
        instancias++;
        rutaLuchador = (instancias % 2 == 1)
            ? REC + "Luchador1.png"
            : REC + "Luchador2.png";

        imgFondo = cargarImagen(REC + "Fondo_Japones.png", -1, -1);

        txtNombre          = crearCampo();
        txtPeso            = crearCampo();
        modeloLista        = new DefaultListModel<>();
        listKimarites      = crearLista();
        btnCargarKimarites = crearBotonAzul("  Cargar Kimarites");
        btnConectar        = crearBotonAzulGrande("  ENTRAR AL DOHYO");
        lblEstado          = crearLblEstado();

        fileChooser = new JFileChooser(new File("Data/"));
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Archivos de propiedades (*.properties)", "properties"));
        fileChooser.setDialogTitle("Seleccionar archivo de kimarites");

        construirUI();
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Imagen de fondo floral
                if (imgFondo != null)
                    g2.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), this);
                else {
                    g2.setColor(new Color(245, 210, 200));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                // Silueta negra semitransparente sobre el fondo (fix #5)
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        raiz.setOpaque(true);
        setContentPane(raiz);

        raiz.add(construirHeader(),  BorderLayout.NORTH);
        raiz.add(construirCentro(),  BorderLayout.CENTER);
        raiz.add(construirEstado(),  BorderLayout.SOUTH);
    }

    /** Header rojo — íconos pegados al título (fix #4) */
    private JPanel construirHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(C_ROJO_PANEL);
        // Padding reducido para que los íconos queden cerca del texto
        p.setBorder(new EmptyBorder(10, 18, 10, 18));

        // Panel izquierdo: ícono + hueco mínimo
        JPanel pIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pIzq.setOpaque(false);
        JLabel lblIcono = new JLabel();
        lblIcono.setIcon(escalarIcono(REC + "Logo_Sumo.png", 58, 58));
        pIzq.add(lblIcono);

        // Título centrado
        JLabel lblTitulo = new JLabel("¡COMBATE DE SUMO!", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 36));
        lblTitulo.setForeground(C_BLANCO);

        // Panel derecho: bandera + hueco mínimo
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

    /** Centro: formulario (izquierda) + luchador (derecha) */
    private JPanel construirCentro() {
        JPanel p = new JPanel(new GridLayout(1, 2, 0, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 28, 12, 28));
        p.add(construirFormulario());
        p.add(construirPanelLuchador());
        return p;
    }

    /** Panel rojo izquierdo con los campos y la lista */
    private JPanel construirFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(C_ROJO_PANEL);
        p.setBorder(new EmptyBorder(18, 22, 16, 22));

        // ── Campos nombre y peso (fix #6: más altos) ────────────────────────
        JPanel pCampos = new JPanel(new GridBagLayout());
        pCampos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 8);

        // Nombre
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pCampos.add(lblCampo("Nombre del Rikishi:"), g);
        g.gridx = 1; g.weightx = 0;
        pCampos.add(new JLabel(escalarIcono(REC + "samurai.png", 36, 36)), g);
        g.gridx = 2; g.weightx = 1;
        pCampos.add(txtNombre, g);

        // Peso
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        pCampos.add(lblCampo("Peso(kg):"), g);
        g.gridx = 1; g.weightx = 0;
        pCampos.add(new JLabel(escalarIcono(REC + "flor-de-cerezo.png", 36, 36)), g);
        g.gridx = 2; g.weightx = 1;
        pCampos.add(txtPeso, g);

        // Fila botón cargar
        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; g.insets = new Insets(14, 0, 2, 4);
        pCampos.add(new JLabel(escalarIcono(REC + "gato-chino-de-la-suerte.png", 42, 42)), g);
        g.gridx = 1; g.gridwidth = 1; g.weightx = 1; g.insets = new Insets(14, 0, 2, 4);
        pCampos.add(btnCargarKimarites, g);
        g.gridx = 2; g.weightx = 0; g.insets = new Insets(14, 4, 2, 0);
        pCampos.add(new JLabel(escalarIcono(REC + "japon.png", 42, 42)), g);

        p.add(pCampos, BorderLayout.NORTH);

        // ── Lista de técnicas ──────────────────────────────────────────────
        JPanel pLista = new JPanel(new BorderLayout(0, 5));
        pLista.setOpaque(false);

        JLabel lblTec = new JLabel("Tecnicas disponibles");
        lblTec.setFont(new Font("Serif", Font.BOLD, 16));
        lblTec.setForeground(C_BLANCO);
        pLista.add(lblTec, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(listKimarites);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 60, 60), 2),
            "Seleccionar Kimarites (Ctrl + clic)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Serif", Font.BOLD, 12), new Color(60, 20, 20)));
        scroll.setBackground(C_LISTA);
        scroll.getViewport().setBackground(C_LISTA);
        pLista.add(scroll, BorderLayout.CENTER);

        p.add(pLista, BorderLayout.CENTER);

        // ── Botón conectar ─────────────────────────────────────────────────
        JPanel pBot = new JPanel(new BorderLayout());
        pBot.setOpaque(false);
        pBot.setBorder(new EmptyBorder(10, 0, 0, 0));
        pBot.add(btnConectar, BorderLayout.CENTER);
        p.add(pBot, BorderLayout.SOUTH);

        return p;
    }

    /**
     * Panel derecho — dibuja fondo floral + círculo rojo + luchador.
     * Imagen del luchador baja un poco para quedar centrada en el círculo (fix #7).
     */
    private JPanel construirPanelLuchador() {
        return new JPanel() {
            final Image imgLuchador = cargarImagen(rutaLuchador, -1, -1);
            { setOpaque(false); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Silueta negra (fix #5)
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(0, 0, w, h);

                // Círculo rojo centrado un poco más abajo
                int r  = (int)(Math.min(w, h) * 0.80);
                int ox = w / 2 - r / 2;
                int oy = h / 2 - r / 2 + 20;          // +20 baja el círculo
                g2.setColor(new Color(200, 40, 40, 210));
                g2.fillOval(ox, oy, r, r);

                // Luchador centrado sobre el círculo (fix #7)
                if (imgLuchador != null) {
                    int maxW = (int)(w * 0.82);
                    int maxH = (int)(h * 0.88);
                    double esc = Math.min(
                        (double) maxW / imgLuchador.getWidth(null),
                        (double) maxH / imgLuchador.getHeight(null));
                    int rw = (int)(imgLuchador.getWidth(null)  * esc);
                    int rh = (int)(imgLuchador.getHeight(null) * esc);
                    // Centro del luchador alineado al centro del círculo
                    int lx = w / 2 - rw / 2;
                    int ly = (oy + r / 2) - rh / 2 + 10; // centrado en el círculo
                    g2.drawImage(imgLuchador, lx, ly, rw, rh, null);
                }
                g2.dispose();
            }
        };
    }

    /** Barra azul inferior */
    private JPanel construirEstado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_AZUL);
        p.setBorder(new EmptyBorder(11, 22, 11, 22));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // ─── Fábricas ─────────────────────────────────────────────────────────────

    /** Campo de texto más alto (fix #6) */
    private JTextField crearCampo() {
        JTextField t = new JTextField();
        t.setBackground(C_CAMPO);
        t.setForeground(C_TEXTO);
        t.setFont(new Font("Serif", Font.PLAIN, 15));
        t.setCaretColor(C_TEXTO);
        t.setPreferredSize(new Dimension(220, 38));   // altura explícita
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 100, 100), 1),
            new EmptyBorder(8, 10, 8, 10)));
        return t;
    }

    private JList<String> crearLista() {
        JList<String> l = new JList<>(modeloLista);
        l.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        l.setBackground(C_LISTA);
        l.setForeground(C_TEXTO);
        l.setFont(new Font("Serif", Font.PLAIN, 13));
        l.setSelectionBackground(new Color(70, 130, 210));
        l.setSelectionForeground(C_BLANCO);
        l.setFixedCellHeight(27);
        l.setBorder(new EmptyBorder(4, 8, 4, 8));
        return l;
    }

    private JButton crearBotonAzul(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(C_AZUL);
        b.setForeground(C_BLANCO);
        b.setFont(new Font("Serif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        return b;
    }

    private JButton crearBotonAzulGrande(String txt) {
        JButton b = crearBotonAzul(txt);
        b.setFont(new Font("Serif", Font.BOLD, 18));
        b.setBorder(new EmptyBorder(14, 0, 14, 0));
        return b;
    }

    private JLabel lblCampo(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Serif", Font.BOLD, 15));
        l.setForeground(C_BLANCO);
        return l;
    }

    private JLabel crearLblEstado() {
        JLabel l = new JLabel("*MENSAJE CON ESTADO ACTUAL E INSTRUCCIONES*",
                               SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 14));
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

    public String seleccionarRutaProperties() {
        int res = fileChooser.showOpenDialog(this);
        return res == JFileChooser.APPROVE_OPTION
            ? fileChooser.getSelectedFile().getAbsolutePath()
            : null;
    }

    public void cargarListaKimarites(List<String> nombres) {
        modeloLista.clear();
        nombres.forEach(modeloLista::addElement);
    }

    public List<String> getKimaritesSeleccionados() {
        return new ArrayList<>(listKimarites.getSelectedValuesList());
    }

    public String getNombreLuchador()  { return txtNombre.getText(); }
    public String getPesoLuchador()    { return txtPeso.getText(); }

    public void mostrarEstado(String msg) { lblEstado.setText(msg); }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarResultadoCombate(boolean gano) {
        String titulo = gano ? "¡GANASTE!"  : "Perdiste";
        String msg    = gano
            ? "Felicitaciones! Tu rikishi gano el combate."
            : "Tu rikishi fue expulsado del dohyo.";
        JOptionPane.showMessageDialog(this, msg, titulo,
            gano ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        dispose();
    }

    public void setBtnConectarHabilitado(boolean h) { btnConectar.setEnabled(h); }
    public JButton getBtnCargarKimarites()          { return btnCargarKimarites; }
    public JButton getBtnConectar()                 { return btnConectar; }
}
