package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A custom-painted panel with rounded corners, an optional soft shadow,
 * and a configurable background color — used as statistic cards,
 * content panels, and form containers throughout the application.
 */
public class RoundedPanel extends JPanel {

    private final int    arcRadius;
    private final Color  shadowColor;
    private final boolean drawShadow;

    /** Panel with rounded corners, white background, and a soft shadow. */
    public RoundedPanel() {
        this(UIConstants.CARD_ARC, UIConstants.BG_CARD, true);
    }

    /** Panel with specified arc, background, and optional shadow. */
    public RoundedPanel(int arcRadius, Color bg, boolean drawShadow) {
        this.arcRadius   = arcRadius;
        this.shadowColor = UIConstants.SHADOW_COLOR;
        this.drawShadow  = drawShadow;
        setOpaque(false);
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(
                UIConstants.CARD_PADDING, UIConstants.CARD_PADDING,
                UIConstants.CARD_PADDING, UIConstants.CARD_PADDING));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyRenderingHints(g2);

        int w = getWidth();
        int h = getHeight();

        // Draw shadow behind
        if (drawShadow) {
            g2.setColor(shadowColor);
            g2.fill(new RoundRectangle2D.Double(3, 4, w - 6, h - 4, arcRadius, arcRadius));
        }

        // Draw background
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, w - 3, h - 4, arcRadius, arcRadius));

        g2.dispose();
        super.paintComponent(g);
    }
}
