package ui;

import java.awt.*;

/**
 * Centralized design-system constants for the Nyaya Track application.
 * All colors, fonts, sizes, and shared rendering hints live here so that
 * every panel, frame, and custom component draws from a single source of truth.
 */
public final class UIConstants {

    private UIConstants() {} // non-instantiable utility class

    // ── COLOR PALETTE ────────────────────────────────────────────────────────
    // Primary (Deep Navy)
    public static final Color PRIMARY_DARK      = new Color(15, 36, 68);    // #0F2444
    public static final Color PRIMARY           = new Color(26, 60, 110);   // #1A3C6E
    public static final Color PRIMARY_LIGHT     = new Color(46, 95, 170);   // #2E5FAA
    public static final Color PRIMARY_VERY_LIGHT= new Color(79, 131, 204);  // #4F83CC

    // Accent (Muted Gold)
    public static final Color ACCENT_GOLD       = new Color(197, 160, 89);  // #C5A059
    public static final Color ACCENT_GOLD_LIGHT = new Color(218, 192, 138); // #DAC08A
    public static final Color ACCENT_GOLD_DARK  = new Color(160, 125, 55);  // #A07D37

    // Backgrounds
    public static final Color BG_MAIN           = new Color(240, 244, 250); // #F0F4FA
    public static final Color BG_CARD           = Color.WHITE;
    public static final Color BG_SIDEBAR        = PRIMARY_DARK;
    public static final Color BG_HEADER         = PRIMARY;
    public static final Color BG_INPUT          = new Color(248, 250, 254); // #F8FAFE

    // Status Colors
    public static final Color STATUS_PENDING    = new Color(230, 126, 34);  // Orange
    public static final Color STATUS_IN_PROGRESS= new Color(52, 152, 219); // Blue
    public static final Color STATUS_RESOLVED   = new Color(39, 174, 96);  // Green
    public static final Color STATUS_DISMISSED  = new Color(149, 165, 166);// Gray

    // Semantic Colors
    public static final Color SUCCESS           = new Color(39, 174, 96);
    public static final Color DANGER            = new Color(205, 60, 60);
    public static final Color WARNING           = new Color(241, 196, 15);
    public static final Color INFO              = new Color(52, 152, 219);

    // Text Colors
    public static final Color TEXT_PRIMARY      = new Color(33, 37, 41);
    public static final Color TEXT_SECONDARY    = new Color(108, 117, 125);
    public static final Color TEXT_LIGHT        = new Color(180, 200, 230);
    public static final Color TEXT_ON_DARK      = Color.WHITE;
    public static final Color TEXT_MUTED        = new Color(160, 170, 180);

    // Border Colors
    public static final Color BORDER_LIGHT      = new Color(220, 228, 240);
    public static final Color BORDER_MEDIUM     = new Color(190, 200, 215);

    // ── TYPOGRAPHY ───────────────────────────────────────────────────────────
    public static final String FONT_FAMILY       = "Segoe UI";
    public static final String FONT_FAMILY_MONO  = "Consolas";

    public static final Font FONT_TITLE          = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font FONT_HEADING        = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_SUBHEADING     = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY           = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD      = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL          = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD     = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_STAT_VALUE     = new Font(FONT_FAMILY, Font.BOLD, 36);
    public static final Font FONT_NAV            = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BUTTON         = new Font(FONT_FAMILY, Font.BOLD, 13);

    // ── SIZES & SPACING ──────────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH        = 220;
    public static final int HEADER_HEIGHT        = 56;
    public static final int CARD_ARC             = 16;
    public static final int BUTTON_ARC           = 10;
    public static final int INPUT_ARC            = 8;
    public static final int CARD_PADDING         = 20;

    // ── SHADOWS ──────────────────────────────────────────────────────────────
    public static final Color SHADOW_COLOR       = new Color(0, 0, 0, 25);
    public static final Color SHADOW_COLOR_DARK  = new Color(0, 0, 0, 50);

    // ── RENDERING HINTS ──────────────────────────────────────────────────────
    /**
     * Apply high-quality anti-aliasing hints to a Graphics2D context.
     */
    public static void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Returns the appropriate status color for the given status string.
     */
    public static Color getStatusColor(String status) {
        if (status == null) return TEXT_SECONDARY;
        return switch (status) {
            case "Pending"     -> STATUS_PENDING;
            case "In Progress" -> STATUS_IN_PROGRESS;
            case "Resolved"    -> STATUS_RESOLVED;
            case "Dismissed"   -> STATUS_DISMISSED;
            default            -> TEXT_SECONDARY;
        };
    }

    /**
     * Returns a lighter variant of a color for hover effects.
     */
    public static Color lighten(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed()   + (255 - c.getRed())   * factor));
        int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int)(c.getBlue()  + (255 - c.getBlue())  * factor));
        return new Color(r, g, b, c.getAlpha());
    }
}
