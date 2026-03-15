package sumo.vista.cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista del cliente en la arquitectura MVC del Combate de Sumo.
 * <p>
 * Presenta el formulario de registro del luchador (rikishi) con:
 * <ul>
 *   <li>Campo para el nombre del luchador.</li>
 *   <li>Campo para el peso del luchador.</li>
 *   <li>Botón para cargar kimarites desde un archivo {@code .properties}.</li>
 *   <li>Lista de selección múltiple de kimarites disponibles.</li>
 *   <li>Botón para conectarse al servidor y enviar los datos.</li>
 *   <li>Área de estado que informa al usuario sobre el progreso.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Principio de Responsabilidad Única (SRP): esta clase solo gestiona la presentación.
 * No contiene objetos del modelo ni lógica de negocio.
 * Los botones se exponen vía getters para que el controlador registre las acciones.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see sumo.controlador.ControladorCliente
 */
public class VistaCliente extends JFrame {

    // ── Colores del tema sumo ─────────────────────────────────────────────────
    /** Color de fondo principal: rojo japonés. */
    private static final Color COLOR_FONDO       = new Color(20, 20, 35);
    /** Color de acento: dorado imperial. */
    private static final Color COLOR_ACENTO      = new Color(212, 175, 55);
    /** Color de texto principal: blanco cálido. */
    private static final Color COLOR_TEXTO       = new Color(245, 245, 240);
    /** Color del panel de campos. */
    private static final Color COLOR_PANEL       = new Color(35, 35, 55);
    /** Color del botón de acción. */
    private static final Color COLOR_BTN_ACTIVO  = new Color(180, 30, 30);
    /** Color del botón de carga. */
    private static final Color COLOR_BTN_CARGA   = new Color(60, 100, 140);
    /** Color de fondo de la lista. */
    private static final Color COLOR_LISTA       = new Color(25, 25, 45);

    // ── Componentes de la UI ──────────────────────────────────────────────────
    /** Campo de texto para el nombre del luchador. */
    private final JTextField txtNombre;
    /** Campo de texto para el peso del luchador. */
    private final JTextField txtPeso;
    /** Lista de kimarites disponibles para selección múltiple. */
    private final JList<String> listKimarites;
    /** Modelo de datos de la lista de kimarites. */
    private final DefaultListModel<String> modeloListaKimarites;
    /** Botón para cargar el archivo de propiedades de kimarites. */
    private final JButton btnCargarKimarites;
    /** Botón para conectarse al servidor con los datos ingresados. */
    private final JButton btnConectar;
    /** Etiqueta de estado que informa al usuario sobre el progreso. */
    private final JLabel lblEstado;
    /** Panel central del dohyō (área visual del ring). */
    private final PanelDohyo panelDohyo;

    /**
     * Construye y configura la ventana del cliente.
     * Inicializa todos los componentes con el tema visual de sumo.
     */
    public VistaCliente() {
        super("⚔ Combate de Sumo - Registro de Luchador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);

        // Inicializar componentes
        txtNombre              = crearCampoTexto("Ej: Hakuho");
        txtPeso                = crearCampoTexto("Ej: 145.0");
        modeloListaKimarites   = new DefaultListModel<>();
        listKimarites          = crearListaKimarites();
        btnCargarKimarites     = crearBoton("📂 Cargar Kimarites", COLOR_BTN_CARGA);
        btnConectar            = crearBoton("🥋 ENTRAR AL DOHYŌ", COLOR_BTN_ACTIVO);
        lblEstado              = crearEtiquetaEstado();
        panelDohyo             = new PanelDohyo();

        construirLayout();
    }

    // ─── Construcción del layout ──────────────────────────────────────────────

    /**
     * Construye y organiza todos los paneles de la ventana.
     */
    private void construirLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        add(crearPanelTitulo(),      BorderLayout.NORTH);
        add(crearPanelCentral(),     BorderLayout.CENTER);
        add(crearPanelEstado(),      BorderLayout.SOUTH);
    }

    /**
     * Crea el panel del título con el kanji de sumo.
     *
     * @return panel de encabezado
     */
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblKanji = new JLabel("相撲", SwingConstants.CENTER);
        lblKanji.setFont(new Font("Serif", Font.BOLD, 52));
        lblKanji.setForeground(COLOR_ACENTO);

        JLabel lblSubtitulo = new JLabel("REGISTRO DEL RIKISHI", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblSubtitulo.setForeground(COLOR_TEXTO);

        panel.add(lblKanji,    BorderLayout.CENTER);
        panel.add(lblSubtitulo, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Crea el panel central con el formulario y la vista previa del dohyō.
     *
     * @return panel central dividido en formulario + dohyō visual
     */
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(COLOR_FONDO);

        panel.add(crearPanelFormulario());
        panel.add(panelDohyo);
        return panel;
    }

    /**
     * Crea el panel del formulario de registro del luchador.
     *
     * @return panel con los campos de datos y selección de kimarites
     */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // ── Datos básicos ─────────────────────────────────────────────────
        JPanel panelDatos = new JPanel(new GridLayout(4, 2, 8, 10));
        panelDatos.setBackground(COLOR_PANEL);

        panelDatos.add(crearEtiqueta("Nombre del Rikishi:"));
        panelDatos.add(txtNombre);
        panelDatos.add(crearEtiqueta("Peso (kg):"));
        panelDatos.add(txtPeso);
        panelDatos.add(crearEtiqueta(""));
        panelDatos.add(btnCargarKimarites);
        panelDatos.add(crearEtiqueta("Técnicas disponibles:"));
        panelDatos.add(new JLabel(""));

        panel.add(panelDatos, BorderLayout.NORTH);

        // ── Lista de kimarites ────────────────────────────────────────────
        JScrollPane scrollKimarites = new JScrollPane(listKimarites);
        scrollKimarites.setBackground(COLOR_LISTA);
        scrollKimarites.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO), "Seleccionar Kimarites (Ctrl+clic)");
        tb.setTitleColor(COLOR_ACENTO);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
        scrollKimarites.setBorder(tb);

        panel.add(scrollKimarites, BorderLayout.CENTER);
        panel.add(btnConectar, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de estado en la parte inferior de la ventana.
     *
     * @return panel con la etiqueta de estado
     */
    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 25));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACENTO, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        panel.add(lblEstado, BorderLayout.CENTER);
        return panel;
    }

    // ─── Fábrica de componentes ───────────────────────────────────────────────

    /**
     * Crea un campo de texto con el estilo del tema sumo.
     *
     * @param placeholder texto de sugerencia (no es placeholder real, es parte del hint visual)
     * @return campo de texto estilizado
     */
    private JTextField crearCampoTexto(String placeholder) {
        JTextField campo = new JTextField();
        campo.setBackground(new Color(45, 45, 65));
        campo.setForeground(COLOR_TEXTO);
        campo.setCaretColor(COLOR_ACENTO);
        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 100)),
            new EmptyBorder(4, 8, 4, 8)
        ));
        campo.setToolTipText(placeholder);
        return campo;
    }

    /**
     * Crea una etiqueta con el estilo del tema sumo.
     *
     * @param texto contenido de la etiqueta
     * @return etiqueta estilizada
     */
    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_ACENTO);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lbl;
    }

    /**
     * Crea un botón con el estilo del tema sumo.
     *
     * @param texto texto del botón
     * @param color color de fondo del botón
     * @return botón estilizado
     */
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    /**
     * Crea la lista de kimarites con selección múltiple y estilo del tema.
     *
     * @return lista estilizada
     */
    private JList<String> crearListaKimarites() {
        JList<String> lista = new JList<>(modeloListaKimarites);
        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lista.setBackground(COLOR_LISTA);
        lista.setForeground(COLOR_TEXTO);
        lista.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lista.setSelectionBackground(COLOR_ACENTO);
        lista.setSelectionForeground(Color.BLACK);
        lista.setFixedCellHeight(24);
        return lista;
    }

    /**
     * Crea la etiqueta de estado en la barra inferior.
     *
     * @return etiqueta de estado estilizada
     */
    private JLabel crearEtiquetaEstado() {
        JLabel lbl = new JLabel("Cargue el archivo de kimarites para comenzar.", SwingConstants.LEFT);
        lbl.setForeground(new Color(150, 200, 150));
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return lbl;
    }

    // ─── Métodos públicos de la vista ─────────────────────────────────────────

    /**
     * Carga y muestra la lista de nombres de kimarites disponibles.
     * El controlador invoca este método después de leer el archivo de propiedades.
     *
     * @param nombres lista de nombres de kimarites a mostrar
     */
    public void cargarListaKimarites(List<String> nombres) {
        modeloListaKimarites.clear();
        for (String nombre : nombres) {
            modeloListaKimarites.addElement(nombre);
        }
    }

    /**
     * Retorna la lista de kimarites seleccionados por el usuario.
     *
     * @return lista de nombres de kimarites seleccionados; vacía si ninguno fue seleccionado
     */
    public List<String> getKimaritesSeleccionados() {
        List<String> seleccionados = new ArrayList<>();
        for (String k : listKimarites.getSelectedValuesList()) {
            seleccionados.add(k);
        }
        return seleccionados;
    }

    /**
     * Retorna el nombre ingresado para el luchador.
     *
     * @return texto del campo nombre
     */
    public String getNombreLuchador() {
        return txtNombre.getText();
    }

    /**
     * Retorna el peso ingresado para el luchador.
     *
     * @return texto del campo peso
     */
    public String getPesoLuchador() {
        return txtPeso.getText();
    }

    /**
     * Actualiza la etiqueta de estado con el mensaje proporcionado.
     *
     * @param mensaje texto a mostrar en el área de estado
     */
    public void mostrarEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }

    /**
     * Muestra un diálogo de alerta informativo al usuario.
     * Permitido según el enunciado para mensajes indicativos.
     *
     * @param mensaje texto del mensaje a mostrar
     */
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra el resultado final del combate en la vista y actualiza el panel del dohyō.
     *
     * @param gano {@code true} si el luchador ganó, {@code false} si perdió
     */
    public void mostrarResultadoCombate(boolean gano) {
        panelDohyo.mostrarResultado(gano);
        String titulo = gano ? "🏆 ¡GANASTE!" : "💀 Perdiste";
        String msg    = gano
            ? "¡Felicitaciones! ¡Tu rikishi ganó el combate!"
            : "Tu rikishi fue expulsado del dohyō. Mejor suerte la próxima vez.";
        JOptionPane.showMessageDialog(this, msg, titulo, JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /**
     * Habilita o deshabilita el botón de conexión al servidor.
     *
     * @param habilitado {@code true} para habilitar, {@code false} para deshabilitar
     */
    public void setBtnConectarHabilitado(boolean habilitado) {
        btnConectar.setEnabled(habilitado);
    }

    /**
     * Retorna el botón de conexión para que el controlador registre su acción.
     *
     * @return botón de conectar al servidor
     */
    public JButton getBtnConectar() {
        return btnConectar;
    }

    /**
     * Retorna el botón de carga de kimarites para que el controlador registre su acción.
     *
     * @return botón de cargar kimarites
     */
    public JButton getBtnCargarKimarites() {
        return btnCargarKimarites;
    }

    // ─── Inner class: Panel visual del Dohyō ──────────────────────────────────

    /**
     * Panel personalizado que dibuja el dohyō (ring circular de sumo) usando {@code Graphics2D}.
     * Muestra el resultado del combate con una animación de color.
     */
    private static class PanelDohyo extends JPanel {

        /** Resultado del combate: null = sin resultado, true = ganó, false = perdió. */
        private Boolean resultado;

        /**
         * Construye el panel del dohyō con fondo oscuro.
         */
        public PanelDohyo() {
            setBackground(COLOR_FONDO);
            setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 1));
        }

        /**
         * Actualiza el panel con el resultado del combate y repinta.
         *
         * @param gano {@code true} si el luchador ganó
         */
        public void mostrarResultado(boolean gano) {
            this.resultado = gano;
            repaint();
        }

        /**
         * Dibuja el dohyō circular, las líneas shikiri-sen y las figuras de los luchadores.
         *
         * @param g contexto gráfico
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int radio = Math.min(getWidth(), getHeight()) / 2 - 20;

            // Fondo de arena del dohyō
            g2.setColor(new Color(200, 170, 100));
            g2.fillOval(cx - radio, cy - radio, radio * 2, radio * 2);

            // Borde del dohyō (paja de arroz)
            g2.setColor(new Color(139, 69, 19));
            g2.setStroke(new BasicStroke(8));
            g2.drawOval(cx - radio, cy - radio, radio * 2, radio * 2);

            // Borde exterior
            g2.setColor(COLOR_ACENTO);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - radio - 5, cy - radio - 5, (radio + 5) * 2, (radio + 5) * 2);

            // Líneas shikiri-sen (líneas de inicio)
            g2.setColor(new Color(80, 40, 10));
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(cx - 20, cy, cx - 5, cy);
            g2.drawLine(cx + 5, cy, cx + 20, cy);

            // Título dentro del dohyō
            g2.setFont(new Font("Serif", Font.BOLD, 18));
            g2.setColor(new Color(80, 40, 10));
            FontMetrics fm = g2.getFontMetrics();

            if (resultado == null) {
                // Figuras de los luchadores
                dibujarLuchador(g2, cx - 40, cy - 10, new Color(60, 60, 150));
                dibujarLuchador(g2, cx + 15, cy - 10, new Color(150, 40, 40));
                String txt = "DOHYŌ";
                g2.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + radio - 15);
            } else if (resultado) {
                g2.setColor(new Color(0, 120, 0, 180));
                g2.fillOval(cx - radio + 8, cy - radio + 8, (radio - 8) * 2, (radio - 8) * 2);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Serif", Font.BOLD, 22));
                fm = g2.getFontMetrics();
                String txt = "¡GANASTE!";
                g2.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + 8);
            } else {
                g2.setColor(new Color(150, 0, 0, 180));
                g2.fillOval(cx - radio + 8, cy - radio + 8, (radio - 8) * 2, (radio - 8) * 2);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Serif", Font.BOLD, 22));
                fm = g2.getFontMetrics();
                String txt = "PERDISTE";
                g2.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + 8);
            }

            g2.dispose();
        }

        /**
         * Dibuja una figura simplificada de un luchador de sumo.
         *
         * @param g2 contexto gráfico
         * @param x  posición horizontal del centro de la figura
         * @param y  posición vertical del centro de la figura
         * @param color color del mawashi (cinturón) del luchador
         */
        private void dibujarLuchador(Graphics2D g2, int x, int y, Color color) {
            // Cuerpo
            g2.setColor(new Color(220, 190, 150));
            g2.fillOval(x - 10, y - 5, 20, 25);
            // Cabeza
            g2.fillOval(x - 7, y - 18, 15, 15);
            // Mawashi (cinturón)
            g2.setColor(color);
            g2.fillRect(x - 9, y + 5, 18, 8);
        }
    }

}
