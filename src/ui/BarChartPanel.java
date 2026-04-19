package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * A custom Swing component that renders a Bar Chart using pure Java 2D.
 * <p>
 * Displays named categories as vertical bars with labels,
 * value annotations, grid lines, and a legend.
 * No external charting library required.
 */
public class BarChartPanel extends JPanel {

    private final String chartTitle;
    private final Map<String, Integer> data;  // ordered
    private final Color[] barColors;

    private static final int PADDING       = 50;
    private static final int TOP_PADDING   = 45;
    private static final int BAR_GAP       = 18;

    /**
     * @param title  Chart title shown above bars
     * @param data   ordered map of category name → value
     * @param colors colors to cycle through for each bar, or null for defaults
     */
    public BarChartPanel(String title, LinkedHashMap<String, Integer> data, Color[] colors) {
        this.chartTitle = title;
        this.data       = data;
        this.barColors  = colors != null ? colors : new Color[]{
                UIConstants.PRIMARY_LIGHT,
                UIConstants.ACCENT_GOLD,
                UIConstants.SUCCESS,
                UIConstants.STATUS_PENDING,
                UIConstants.DANGER,
                UIConstants.INFO
        };
        setOpaque(false);
        setPreferredSize(new Dimension(400, 280));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyRenderingHints(g2);

        int w = getWidth();
        int h = getHeight();
        int chartW = w - PADDING * 2;
        int chartH = h - TOP_PADDING - PADDING;

        // ── Title ─────────────────────────────────────────────────────────────
        g2.setFont(UIConstants.FONT_SUBHEADING);
        g2.setColor(UIConstants.TEXT_PRIMARY);
        FontMetrics tmFm = g2.getFontMetrics();
        g2.drawString(chartTitle, (w - tmFm.stringWidth(chartTitle)) / 2, 28);

        // ── Determine max value for Y-axis scale ───────────────────────────────
        int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        // Round up to a nice number
        int niceMax = (int)(Math.ceil(maxVal / 5.0) * 5);
        if (niceMax == 0) niceMax = 5;

        // ── Draw grid lines ───────────────────────────────────────────────────
        g2.setFont(UIConstants.FONT_SMALL);
        g2.setStroke(new BasicStroke(1f));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int yy = TOP_PADDING + chartH - (int)((double)i / gridLines * chartH);
            int val = (int)((double)i / gridLines * niceMax);
            g2.setColor(UIConstants.BORDER_LIGHT);
            g2.drawLine(PADDING, yy, PADDING + chartW, yy);
            g2.setColor(UIConstants.TEXT_SECONDARY);
            String label = String.valueOf(val);
            g2.drawString(label, PADDING - g2.getFontMetrics().stringWidth(label) - 6, yy + 4);
        }

        // ── Draw bars ─────────────────────────────────────────────────────────
        int numBars = data.size();
        int barWidth = Math.max(20, (chartW - BAR_GAP * (numBars + 1)) / numBars);
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int barH = (int)((double)entry.getValue() / niceMax * chartH);
            int x = PADDING + BAR_GAP + i * (barWidth + BAR_GAP);
            int y = TOP_PADDING + chartH - barH;

            Color color = barColors[i % barColors.length];

            // Bar shadow
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fill(new RoundRectangle2D.Double(x + 2, y + 3, barWidth, barH, 6, 6));

            // Bar body with gradient
            GradientPaint gp = new GradientPaint(x, y, color,
                    x, y + barH, UIConstants.lighten(color, 0.3f));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(x, y, barWidth, barH, 6, 6));

            // Value label on top
            g2.setColor(UIConstants.TEXT_PRIMARY);
            g2.setFont(UIConstants.FONT_SMALL_BOLD);
            String valStr = String.valueOf(entry.getValue());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 6);

            // Category label below
            g2.setColor(UIConstants.TEXT_SECONDARY);
            g2.setFont(UIConstants.FONT_SMALL);
            fm = g2.getFontMetrics();
            String cat = entry.getKey();
            if (fm.stringWidth(cat) > barWidth + BAR_GAP) {
                cat = cat.substring(0, Math.min(cat.length(), 6)) + "…";
            }
            g2.drawString(cat, x + (barWidth - fm.stringWidth(cat)) / 2,
                    TOP_PADDING + chartH + 16);

            i++;
        }

        // ── Axis lines ───────────────────────────────────────────────────────
        g2.setColor(UIConstants.TEXT_SECONDARY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(PADDING, TOP_PADDING, PADDING, TOP_PADDING + chartH);
        g2.drawLine(PADDING, TOP_PADDING + chartH,
                    PADDING + chartW, TOP_PADDING + chartH);

        g2.dispose();
    }
}
