package net.freifunk.autodeploy.ui.pi.peripherial;

/**
 * Driver to display information on an attached LCD.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface LCDDriver {

    /**
     * Initializes the LCD.
     */
    void init();

    /**
     * Shuts down the LCD. Always call before terminating.
     */
    void shutdown();

    /**
     * Displays the given {@link String} on the display.
     */
    void writeString(String str);
}
