package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom table cell renderer that draws case status values as coloured
 * "pill" badges (rounded rectangles) instead of plain text.
 * <p>
 * Each status value is rendered with its own background color, giving
 * the table an at-a-glance visual summary.
 */
public class StatusPillRenderer extends DefaultTableCellRenderer {

    private Color pillColor = UIConstants.TEXT_SECONDARY;
    private String statusText = "";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {

        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, col);

        statusText = value != null ? value.toString() : "";
        pillColor  = UIConstants.getStatusColor(statusText);

        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(UIConstants.FONT_SMALL_BOLD);
        setOpaque(true);

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            // Alternating row colors handled elsewhere; keep transparent here
            setBackground(row % 2 == 0 ? UIConstants.BG_CARD : new Color(245, 248, 255));
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Paint the default background first
        super.paintComponent(g);

        if (statusText.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyRenderingHints(g2);

        FontMetrics fm = g2.getFontMetrics(UIConstants.FONT_SMALL_BOLD);
        int textWidth  = fm.stringWidth(statusText);
        int textHeight = fm.getHeight();

        int pillW = textWidth + 20;
        int pillH = textHeight + 6;
        int x = (getWidth() - pillW) / 2;
        int y = (getHeight() - pillH) / 2;

        // Draw pill background
        g2.setColor(new Color(pillColor.getRed(), pillColor.getGreen(),
                              pillColor.getBlue(), 30));
        g2.fill(new RoundRectangle2D.Double(x, y, pillW, pillH, 14, 14));

        // Draw pill border
        g2.setColor(pillColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(x, y, pillW, pillH, 14, 14));

        // Draw text
        g2.setFont(UIConstants.FONT_SMALL_BOLD);
        g2.setColor(pillColor);
        int textX = x + (pillW - textWidth) / 2;
        int textY = y + (pillH + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(statusText, textX, textY);

        g2.dispose();
    }
}
