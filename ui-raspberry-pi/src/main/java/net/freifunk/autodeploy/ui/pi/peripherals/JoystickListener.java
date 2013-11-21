package net.freifunk.autodeploy.ui.pi.peripherals;

/**
 * Listener to listen for joystick button / direction state changes.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface JoystickListener {

    /**
     * Called when the joystick is moved up.
     */
    void up();

    /**
     * Called when the joystick is moved down.
     */
    void down();

    /**
     * Called when the joystick is moved left.
     */
    void left();

    /**
     * Called when the joystick is moved right.
     */
    void right();

    /**
     * Called when the joystick button is pressed.
     */
    void button();
}
