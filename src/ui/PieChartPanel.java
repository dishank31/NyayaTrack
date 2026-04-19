package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * A custom Swing component that renders a Pie / Donut Chart using pure Java 2D.
 * <p>
 * Displays proportional slices with labels and a built-in legend.
 */
public class PieChartPanel extends JPanel {

    private final String chartTitle;
    private final Map<String, Integer> data;
    private final Color[] sliceColors;

    /**
     * @param title  Chart title
     * @param data   ordered map of slice name → value
     * @param colors colors for each slice, or null for defaults
     */
    public PieChartPanel(String title, LinkedHashMap<String, Integer> data, Color[] colors) {
        this.chartTitle  = title;
        this.data        = data;
        this.sliceColors = colors != null ? colors : new Color[]{
                UIConstants.PRIMARY_LIGHT,
                UIConstants.ACCENT_GOLD,
                UIConstants.SUCCESS,
                UIConstants.STATUS_PENDING,
                UIConstants.DANGER,
                UIConstants.INFO,
                new Color(155, 89, 182),
                new Color(22, 160, 133)
        };
        setOpaque(false);
        setPreferredSize(new Dimension(320, 280));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyRenderingHints(g2);

        int w = getWidth();
        int h = getHeight();

        // ── Title ─────────────────────────────────────────────────────────────
        g2.setFont(UIConstants.FONT_SUBHEADING);
        g2.setColor(UIConstants.TEXT_PRIMARY);
        FontMetrics tmFm = g2.getFontMetrics();
        g2.drawString(chartTitle, (w - tmFm.stringWidth(chartTitle)) / 2, 24);

        // ── Calculate totals ──────────────────────────────────────────────────
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            g2.setFont(UIConstants.FONT_BODY);
            g2.setColor(UIConstants.TEXT_MUTED);
            g2.drawString("No data available", w / 2 - 50, h / 2);
            g2.dispose();
            return;
        }

        // ── Draw donut ───────────────────────────────────────────────────────
        int diameter = Math.min(w - 160, h - 80);
        int cx = (w - 120) / 2;
        int cy = 36 + (h - 60) / 2;
        int x = cx - diameter / 2;
        int y = cy - diameter / 2;
        int innerD = (int)(diameter * 0.52);

        double startAngle = 90;
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            double extent = 360.0 * entry.getValue() / total;
            Color color = sliceColors[i % sliceColors.length];

            // Outer arc
            g2.setColor(color);
            g2.fill(new Arc2D.Double(x, y, diameter, diameter,
                    startAngle, -extent, Arc2D.PIE));

            startAngle -= extent;
            i++;
        }

        // Inner circle (donut hole)
        g2.setColor(UIConstants.BG_CARD);
        g2.fillOval(cx - innerD / 2, cy - innerD / 2, innerD, innerD);

        // Center text
        g2.setFont(UIConstants.FONT_STAT_VALUE.deriveFont(24f));
        g2.setColor(UIConstants.TEXT_PRIMARY);
        String totalStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalStr, cx - fm.stringWidth(totalStr) / 2,
                cy + fm.getAscent() / 2 - 6);
        g2.setFont(UIConstants.FONT_SMALL);
        g2.setColor(UIConstants.TEXT_SECONDARY);
        String subtitle = "Total";
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, cx - fm.stringWidth(subtitle) / 2,
                cy + fm.getAscent() / 2 + 10);

        // ── Legend ───────────────────────────────────────────────────────────
        int legendX = w - 130;
        int legendY = 50;
        i = 0;
        g2.setFont(UIConstants.FONT_SMALL);
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Color color = sliceColors[i % sliceColors.length];
            g2.setColor(color);
            g2.fillRoundRect(legendX, legendY + i * 22, 12, 12, 3, 3);
            g2.setColor(UIConstants.TEXT_PRIMARY);
            double pct = 100.0 * entry.getValue() / total;
            g2.drawString(String.format("%s (%.0f%%)", entry.getKey(), pct),
                    legendX + 18, legendY + i * 22 + 11);
            i++;
        }

        g2.dispose();
    }
}
