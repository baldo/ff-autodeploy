package net.freifunk.autodeploy.ui.pi.peripherals;

/**
 * Listener to listen for button state changes.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface ButtonListener {

    /**
     * Called when the button is pressed.
     */
    void pressed();

    /**
     * Called when the button is released.
     */
    void released();
}
