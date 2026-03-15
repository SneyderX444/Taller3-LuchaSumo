package sumo.vista.servidor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.border.TitledBorder;

/**
 * Vista del servidor en la arquitectura MVC del Combate de Sumo.
 * <p>
 * Muestra visualmente el desarrollo completo del combate de sumo:
 * <ul>
 *   <li>Estado de llegada de cada luchador al dohyō.</li>
 *   <li>Animación del ring (dohyō) con los luchadores representados.</li>
 *   <li>Log detallado de cada kimarite ejecutado en tiempo real.</li>
 *   <li>Anuncio del ganador al finalizar el combate.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Principio de Responsabilidad Única (SRP): esta clase solo gestiona la presentación
 * del servidor. No contiene objetos del modelo ni lógica de negocio.
 * El controlador invoca sus métodos públicos con datos primitivos/String.
 * </p>
 *
 * @author Grupo Taller 3
 * @version 1.0
 * @see sumo.controlador.ControladorServidor
 */
public class VistaServidor extends JFrame {

    // ── Colores del tema servidor (más oscuro y solemne) ─────────────────────
    /** Color de fondo principal. */
    private static final Color FONDO        = new Color(10, 10, 20);
    /** Color de acento dorado. */
    private static final Color ACENTO       = new Color(212, 175, 55);
    /** Color del panel de log. */
    private static final Color FONDO_LOG    = new Color(18, 18, 30);
    /** Color del texto del log. */
    private static final Color TEXTO_LOG    = new Color(200, 200, 190);
    /** Color de victoria. */
    private static final Color COLOR_WIN    = new Color(50, 180, 50);
    /** Color de derrota. */
    private static final Color COLOR_LOSE   = new Color(180, 50, 50);
    /** Color del luchador 1 (azul). */
    static final Color COLOR_L1      = new Color(80, 130, 200);
    /** Color del luchador 2 (rojo). */
    static final Color COLOR_L2      = new Color(200, 80, 80);

    // ── Componentes principales ───────────────────────────────────────────────
    /** Panel visual animado del dohyō. */
    private final PanelDohyoServidor panelDohyo;
    /** Área de texto para el log de combate. */
    private final JTextArea areaLog;
    /** Etiqueta del luchador 1 (nombre y peso). */
    private final JLabel lblLuchador1;
    /** Etiqueta del luchador 2 (nombre y peso). */
    private final JLabel lblLuchador2;
    /** Etiqueta del estado general del combate. */
    private final JLabel lblEstadoCombate;
    /** Panel de anuncio del ganador (inicialmente oculto). */
    private final JPanel panelGanador;
    /** Etiqueta con el nombre del ganador. */
    private final JLabel lblNombreGanador;

    /**
     * Construye y configura la ventana del servidor.
     * Inicializa todos los paneles con el tema visual del combate.
     */
    public VistaServidor() {
        super("🥋 Servidor - Combate de Sumo | Dohyō");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(FONDO);

        // Inicializar componentes
        panelDohyo        = new PanelDohyoServidor();
        areaLog           = crearAreaLog();
        lblLuchador1      = crearEtiquetaLuchador("Esperando luchador 1...", COLOR_L1);
        lblLuchador2      = crearEtiquetaLuchador("Esperando luchador 2...", COLOR_L2);
        lblEstadoCombate  = crearEtiquetaEstado("⏳ Aguardando la llegada de los luchadores...");
        panelGanador      = crearPanelGanador();
        lblNombreGanador  = (JLabel) panelGanador.getComponent(0);

        construirLayout();
    }

    // ─── Construccion del layout ──────────────────────────────────────────────

    /**
     * Construye y organiza todos los paneles de la ventana del servidor.
     */
    private void construirLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        add(crearPanelEncabezado(), BorderLayout.NORTH);
        add(crearPanelContenido(), BorderLayout.CENTER);
        add(crearPanelEstadoBar(), BorderLayout.SOUTH);
    }

    /**
     * Crea el encabezado con el tiitulo y los paneles de los luchadores.
     *
     * @return panel de encabezado
     */
    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblTitulo = new JLabel("相撲 — EL COMBATE", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 28));
        lblTitulo.setForeground(ACENTO);

        JPanel panelLuchadores = new JPanel(new GridLayout(1, 3, 10, 0));
        panelLuchadores.setBackground(FONDO);

        JPanel p1 = envolverEtiqueta(lblLuchador1, COLOR_L1);
        JLabel vs = new JLabel("VS", SwingConstants.CENTER);
        vs.setFont(new Font("Serif", Font.BOLD, 24));
        vs.setForeground(new Color(200, 200, 100));
        vs.setOpaque(true);
        vs.setBackground(new Color(30, 30, 50));
        JPanel p2 = envolverEtiqueta(lblLuchador2, COLOR_L2);

        panelLuchadores.add(p1);
        panelLuchadores.add(vs);
        panelLuchadores.add(p2);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelLuchadores, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea el panel central con el dohyō visual y el log del combate.
     *
     * @return panel central con dos columnas
     */
    private JPanel crearPanelContenido() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBackground(FONDO);

        // Panel izquierdo: dohyō visual + ganador
        JPanel panelIzq = new JPanel(new BorderLayout(0, 8));
        panelIzq.setBackground(FONDO);
        panelIzq.add(panelDohyo, BorderLayout.CENTER);
        panelIzq.add(panelGanador, BorderLayout.SOUTH);

        // Panel derecho: log del combate
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBackground(FONDO_LOG);
        scrollLog.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACENTO, 1),
            BorderFactory.createEmptyBorder()
        ));
        scrollLog.getViewport().setBackground(FONDO_LOG);
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACENTO), "📋 Log del Combate");
        tb.setTitleColor(ACENTO);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        scrollLog.setBorder(tb);

        panel.add(panelIzq);
        panel.add(scrollLog);
        return panel;
    }

    /**
     * Crea la barra de estado inferior.
     *
     * @return panel con la etiqueta de estado
     */
    private JPanel crearPanelEstadoBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(8, 8, 18));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACENTO, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        panel.add(lblEstadoCombate, BorderLayout.CENTER);
        return panel;
    }

    // ─── Fábrica de componentes ───────────────────────────────────────────────

    /**
     * Crea el área de texto para el log del combate.
     *
     * @return área de texto configurada y no editable
     */
    private JTextArea crearAreaLog() {
        JTextArea area = new JTextArea();
        area.setBackground(FONDO_LOG);
        area.setForeground(TEXTO_LOG);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return area;
    }

    /**
     * Crea una etiqueta de luchador con el color asignado.
     *
     * @param texto  texto inicial de la etiqueta
     * @param color  color del borde e indicador
     * @return etiqueta estilizada
     */
    private JLabel crearEtiquetaLuchador(String texto, Color color) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setForeground(color);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        return lbl;
    }

    /**
     * Crea la etiqueta de estado del combate en la barra inferior.
     *
     * @param texto texto inicial
     * @return etiqueta de estado
     */
    private JLabel crearEtiquetaEstado(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.LEFT);
        lbl.setForeground(new Color(150, 210, 150));
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return lbl;
    }

    /**
     * Crea el panel de anuncio del ganador (inicialmente oculto).
     *
     * @return panel con fondo llamativo y etiqueta del ganador
     */
    private JPanel crearPanelGanador() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 100, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACENTO, 2),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(lbl, BorderLayout.CENTER);
        panel.setVisible(false);
        return panel;
    }

    /**
     * Envuelve una etiqueta en un panel con borde del color indicado.
     *
     * @param lbl   etiqueta a envolver
     * @param color color del borde
     * @return panel contenedor
     */
    private JPanel envolverEtiqueta(JLabel lbl, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(25, 25, 40));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(8, 10, 8, 10)
        ));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ─── Métodos públicos de actualización de la vista ────────────────────────

    /**
     * Agrega una línea de texto al log del combate y hace scroll automático al final.
     * Debe invocarse desde el EDT.
     *
     * @param mensaje texto a agregar al log
     */
    public void mostrarMensaje(String mensaje) {
        areaLog.append(mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
        lblEstadoCombate.setText(mensaje);
    }

    /**
     * Actualiza el panel del luchador indicado con su nombre y peso.
     * Debe invocarse desde el EDT.
     *
     * @param nombre nombre del luchador
     * @param peso   peso del luchador en kg
     * @param indice posición (0 = luchador 1, 1 = luchador 2)
     */
    public void mostrarLuchadorEnDohyo(String nombre, double peso, int indice) {
        String texto = String.format("🥋 %s (%.1f kg)", nombre, peso);
        if (indice == 0) {
            lblLuchador1.setText(texto);
            panelDohyo.setNombreLuchador(nombre, indice);
        } else {
            lblLuchador2.setText(texto);
            panelDohyo.setNombreLuchador(nombre, indice);
        }
    }

    /**
     * Actualiza el estado visual para mostrar el inicio del combate.
     * Debe invocarse desde el EDT.
     *
     * @param nombreL1 nombre del luchador 1
     * @param nombreL2 nombre del luchador 2
     */
    public void mostrarInicioCombate(String nombreL1, String nombreL2) {
        lblEstadoCombate.setText("⚔ ¡COMBATE INICIADO! " + nombreL1 + " vs " + nombreL2);
        panelDohyo.setEstado(PanelDohyoServidor.Estado.COMBATE);
    }

    /**
     * Muestra en el dohyō visual el kimarite ejecutado y su resultado.
     * Debe invocarse desde el EDT.
     *
     * @param nombreLuchador nombre del luchador atacante
     * @param nombreKimarite nombre de la técnica ejecutada
     * @param expulsado      {@code true} si el oponente fue expulsado
     */
    public void mostrarKimarite(String nombreLuchador, String nombreKimarite, boolean expulsado) {
        panelDohyo.setUltimoAtaque(nombreLuchador, nombreKimarite, expulsado);
    }

    /**
     * Muestra el panel del ganador con su nombre y victorias.
     * Actualiza el estado visual del dohyō.
     * Debe invocarse desde el EDT.
     *
     * @param nombreGanador    nombre del luchador ganador
     * @param victoriasGanador número de victorias acumuladas
     */
    public void mostrarGanador(String nombreGanador, int victoriasGanador) {
        panelDohyo.setEstado(PanelDohyoServidor.Estado.TERMINADO);
        panelDohyo.setNombreGanador(nombreGanador);

        lblNombreGanador.setText("🏆 GANADOR: " + nombreGanador + " | " + victoriasGanador + " victoria(s)");
        panelGanador.setVisible(true);
        lblEstadoCombate.setText("🏆 Combate finalizado. Ganador: " + nombreGanador);
    }

    // ─── Inner class: Panel visual animado del Dohyō ─────────────────────────

    /**
     * Panel personalizado que dibuja el dohyō con los luchadores y el estado del combate.
     * Se actualiza en cada evento del combate mediante {@code repaint()}.
     */
    static class PanelDohyoServidor extends JPanel {

        /**
         * Estados visuales del dohyō.
         */
        enum Estado {
            /** Esperando a que lleguen los luchadores. */
            ESPERA,
            /** Combate en curso. */
            COMBATE,
            /** Combate finalizado. */
            TERMINADO
        }

        /** Estado actual del dohyō. */
        private Estado estado = Estado.ESPERA;
        /** Nombres de los luchadores (índices 0 y 1). */
        private final String[] nombres = {"?", "?"};
        /** Nombre del último luchador en atacar. */
        private String ultimoAtacante = "";
        /** Nombre del último kimarite usado. */
        private String ultimoKimarite = "";
        /** Si el último ataque resultó en expulsión. */
        private boolean ultimaExpulsion = false;
        /** Nombre del ganador del combate. */
        private String nombreGanador = "";

        /**
         * Construye el panel del dohyō con fondo oscuro.
         */
        public PanelDohyoServidor() {
            setBackground(FONDO);
            setBorder(BorderFactory.createLineBorder(ACENTO, 1));
        }

        /**
         * Actualiza el nombre de un luchador y repinta.
         *
         * @param nombre nombre del luchador
         * @param indice posición (0 o 1)
         */
        public void setNombreLuchador(String nombre, int indice) {
            nombres[indice] = nombre;
            repaint();
        }

        /**
         * Actualiza el estado del dohyō y repinta.
         *
         * @param nuevoEstado nuevo estado visual
         */
        public void setEstado(Estado nuevoEstado) {
            this.estado = nuevoEstado;
            repaint();
        }

        /**
         * Registra el último ataque para mostrarlo en el panel.
         *
         * @param atacante  nombre del atacante
         * @param kimarite  nombre de la técnica
         * @param expulsado si resultó en expulsión
         */
        public void setUltimoAtaque(String atacante, String kimarite, boolean expulsado) {
            this.ultimoAtacante = atacante;
            this.ultimoKimarite = kimarite;
            this.ultimaExpulsion = expulsado;
            repaint();
        }

        /**
         * Establece el nombre del ganador para la pantalla final.
         *
         * @param ganador nombre del ganador
         */
        public void setNombreGanador(String ganador) {
            this.nombreGanador = ganador;
            repaint();
        }

        /**
         * Renderiza el dohyō con antialiasing, el ring circular, los luchadores
         * y el estado actual del combate.
         *
         * @param g contexto gráfico
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2 - 20;
            int radio = Math.min(w, h - 80) / 2 - 15;

            dibujarRing(g2, cx, cy, radio);

            switch (estado) {
                case ESPERA:
                    dibujarEstadoEspera(g2, cx, cy, radio);
                    break;
                case COMBATE:
                    dibujarEstadoCombate(g2, cx, cy, radio);
                    break;
                case TERMINADO:
                    dibujarEstadoTerminado(g2, cx, cy, radio);
                    break;
            }

            // Mostrar último kimarite en la parte inferior
            if (!ultimoKimarite.isEmpty()) {
                dibujarInfoKimarite(g2, cx, h - 30);
            }

            g2.dispose();
        }

        /**
         * Dibuja la estructura visual del ring (dohyō).
         *
         * @param g2    contexto gráfico
         * @param cx    centro horizontal
         * @param cy    centro vertical
         * @param radio radio del ring
         */
        private void dibujarRing(Graphics2D g2, int cx, int cy, int radio) {
            // Sombra del ring
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(cx - radio + 5, cy - radio + 5, radio * 2, radio * 2);

            // Piso de arena
            GradientPaint gradArena = new GradientPaint(
                cx - radio, cy - radio, new Color(220, 185, 115),
                cx + radio, cy + radio, new Color(190, 155, 85));
            g2.setPaint(gradArena);
            g2.fillOval(cx - radio, cy - radio, radio * 2, radio * 2);

            // Borde exterior de paja (tawara)
            g2.setColor(new Color(140, 80, 20));
            g2.setStroke(new BasicStroke(10));
            g2.drawOval(cx - radio, cy - radio, radio * 2, radio * 2);

            // Borde dorado
            g2.setColor(ACENTO);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - radio - 6, cy - radio - 6, (radio + 6) * 2, (radio + 6) * 2);

            // Líneas shikiri-sen
            g2.setColor(new Color(100, 60, 20));
            g2.setStroke(new BasicStroke(3));
            int ls = 18;
            g2.drawLine(cx - ls, cy + 5, cx - 5, cy + 5);
            g2.drawLine(cx + 5,  cy + 5, cx + ls, cy + 5);
        }

        /**
         * Dibuja el estado de espera: textos indicando la posición de cada luchador.
         *
         * @param g2    contexto gráfico
         * @param cx    centro horizontal
         * @param cy    centro vertical
         * @param radio radio del ring
         */
        private void dibujarEstadoEspera(Graphics2D g2, int cx, int cy, int radio) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(COLOR_L1);
            g2.drawString(nombres[0], cx - radio + 10, cy - 10);
            g2.setColor(COLOR_L2);
            g2.drawString(nombres[1], cx + radio - 70, cy - 10);

            g2.setColor(new Color(200, 200, 100));
            g2.setFont(new Font("Serif", Font.ITALIC, 14));
            String espera = "Esperando luchadores...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(espera, cx - fm.stringWidth(espera) / 2, cy + 20);
        }

        /**
         * Dibuja el estado de combate: figuras de los luchadores enfrentados.
         *
         * @param g2    contexto gráfico
         * @param cx    centro horizontal
         * @param cy    centro vertical
         * @param radio radio del ring
         */
        private void dibujarEstadoCombate(Graphics2D g2, int cx, int cy, int radio) {
            // Luchador 1 (izquierda)
            dibujarRikishi(g2, cx - 35, cy, COLOR_L1, false);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(COLOR_L1);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nombres[0], cx - 35 - fm.stringWidth(nombres[0]) / 2, cy + 45);

            // Luchador 2 (derecha)
            dibujarRikishi(g2, cx + 35, cy, COLOR_L2, true);
            g2.setColor(COLOR_L2);
            g2.drawString(nombres[1], cx + 35 - fm.stringWidth(nombres[1]) / 2, cy + 45);

            // Indicador de último atacante
            if (!ultimoAtacante.isEmpty()) {
                Color colorAtk = ultimoAtacante.equals(nombres[0]) ? COLOR_L1 : COLOR_L2;
                g2.setColor(colorAtk.brighter());
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                if (ultimaExpulsion) {
                    g2.drawString("💥", cx - 8, cy - radio + 25);
                }
            }
        }

        /**
         * Dibuja el estado final del combate con el ganador destacado.
         *
         * @param g2    contexto gráfico
         * @param cx    centro horizontal
         * @param cy    centro vertical
         * @param radio radio del ring
         */
        private void dibujarEstadoTerminado(Graphics2D g2, int cx, int cy, int radio) {
            // Overlay semitransparente dorado
            g2.setColor(new Color(212, 175, 55, 60));
            g2.fill(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));

            g2.setFont(new Font("Serif", Font.BOLD, 20));
            g2.setColor(ACENTO);
            FontMetrics fm = g2.getFontMetrics();
            String txt1 = "🏆 GANADOR";
            String txt2 = nombreGanador;
            g2.drawString(txt1, cx - fm.stringWidth(txt1) / 2, cy - 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Serif", Font.BOLD, 17));
            fm = g2.getFontMetrics();
            g2.drawString(txt2, cx - fm.stringWidth(txt2) / 2, cy + 20);
        }

        /**
         * Dibuja la información del último kimarite en la barra inferior del panel.
         *
         * @param g2 contexto gráfico
         * @param cx centro horizontal
         * @param y  posición vertical de la barra
         */
        private void dibujarInfoKimarite(Graphics2D g2, int cx, int y) {
            Color colorInfo = ultimaExpulsion ? COLOR_LOSE : new Color(100, 180, 100);
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(cx - 160, y - 16, 320, 22, 8, 8);
            g2.setColor(colorInfo);
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            String info = ultimoAtacante + " → [" + ultimoKimarite + "]"
                + (ultimaExpulsion ? " 💥 ¡Expulsado!" : " ↩ Resiste");
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(info, cx - fm.stringWidth(info) / 2, y);
        }

        /**
         * Dibuja la figura de un rikishi en la posición dada.
         *
         * @param g2       contexto gráfico
         * @param x        posición horizontal
         * @param y        posición vertical
         * @param colorFaja color del mawashi (cinturón)
         * @param espejado si se debe mostrar espejado (para el luchador derecho)
         */
        private void dibujarRikishi(Graphics2D g2, int x, int y, Color colorFaja, boolean espejado) {
            int dir = espejado ? -1 : 1;
            Color piel = new Color(220, 190, 155);

            // Cuerpo ovalado
            g2.setColor(piel);
            g2.fillOval(x - 16, y - 10, 32, 35);
            // Cabeza
            g2.fillOval(x - 10, y - 28, 20, 20);
            // Mawashi
            g2.setColor(colorFaja);
            g2.fillRoundRect(x - 14, y + 5, 28, 12, 4, 4);
            // Brazos extendidos
            g2.setColor(piel);
            g2.fillOval(x + dir * 14, y - 5, 12, 8);
            // Contorno
            g2.setColor(colorFaja.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x - 16, y - 10, 32, 35);
            g2.setStroke(new BasicStroke(1));
        }
    }
}
