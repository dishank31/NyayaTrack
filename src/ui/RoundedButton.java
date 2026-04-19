package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A modern, custom-drawn button with rounded corners, gradient backgrounds,
 * hover animations, and optional icon support.
 * <p>
 * Draws entirely via {@link Graphics2D} — no platform L&F dependency.
 */
public class RoundedButton extends JButton {

    private Color bgColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color fgColor;
    private int   arcRadius;
    private boolean hovered  = false;
    private boolean pressed  = false;
    private float  animAlpha = 0f;

    /* ── Builder-style factory methods ─────────────────────────────────────── */

    /** Creates a primary-themed (blue) button. */
    public static RoundedButton primary(String text) {
        return new RoundedButton(text,
                UIConstants.PRIMARY_LIGHT, UIConstants.PRIMARY, UIConstants.PRIMARY_DARK,
                UIConstants.TEXT_ON_DARK, UIConstants.BUTTON_ARC);
    }

    /** Creates a gold-accent themed button. */
    public static RoundedButton accent(String text) {
        return new RoundedButton(text,
                UIConstants.ACCENT_GOLD, UIConstants.ACCENT_GOLD_DARK, new Color(130, 100, 40),
                UIConstants.TEXT_ON_DARK, UIConstants.BUTTON_ARC);
    }

    /** Creates a success (green) themed button. */
    public static RoundedButton success(String text) {
        return new RoundedButton(text,
                UIConstants.SUCCESS, new Color(30, 140, 76), new Color(22, 110, 58),
                UIConstants.TEXT_ON_DARK, UIConstants.BUTTON_ARC);
    }

    /** Creates a danger (red) themed button. */
    public static RoundedButton danger(String text) {
        return new RoundedButton(text,
                UIConstants.DANGER, new Color(175, 45, 45), new Color(140, 35, 35),
                UIConstants.TEXT_ON_DARK, UIConstants.BUTTON_ARC);
    }

    /** Creates a subtle/ghost button (light background). */
    public static RoundedButton ghost(String text) {
        return new RoundedButton(text,
                new Color(0,0,0,0), new Color(46, 95, 170, 20), new Color(46, 95, 170, 40),
                UIConstants.PRIMARY_LIGHT, UIConstants.BUTTON_ARC);
    }

    /* ── Constructors ──────────────────────────────────────────────────────── */

    public RoundedButton(String text, Color bg, Color hover, Color pressed,
                         Color fg, int arcRadius) {
        super(text);
        this.bgColor      = bg;
        this.hoverColor   = hover;
        this.pressedColor = pressed;
        this.fgColor      = fg;
        this.arcRadius    = arcRadius;
        setup();
    }

    private void setup() {
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(fgColor);
        setFont(UIConstants.FONT_BUTTON);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyRenderingHints(g2);

        int w = getWidth();
        int h = getHeight();

        // Determine current fill color
        Color fill;
        if (!isEnabled()) {
            fill = UIConstants.TEXT_MUTED;
        } else if (pressed) {
            fill = pressedColor;
        } else if (hovered) {
            fill = hoverColor;
        } else {
            fill = bgColor;
        }

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fill(new RoundRectangle2D.Double(2, 3, w - 4, h - 3, arcRadius, arcRadius));

        // Draw body
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 3, arcRadius, arcRadius));

        // Draw subtle top highlight for depth
        if (isEnabled() && !pressed) {
            GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 35),
                    0, h / 2f, new Color(255, 255, 255, 0));
            g2.setPaint(shine);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h / 2.0, arcRadius, arcRadius));
        }

        g2.dispose();
        super.paintComponent(g); // draws text on top
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 20, Math.max(d.height, 38));
    }

    /* ── Setters for dynamic re-theming ────────────────────────────────────── */
    public void setBgColor(Color c) { bgColor = c; repaint(); }
    public void setHoverColor(Color c) { hoverColor = c; repaint(); }
    public void setArcRadius(int r) { arcRadius = r; repaint(); }
}
