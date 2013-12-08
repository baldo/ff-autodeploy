package net.freifunk.autodeploy.ui.pi.peripherals;

/**
 * Driver for listening for joystick events.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface JoystickDriver {

    public static enum JoystickEvent {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        BUTTON,
        ;
    }

    /**
     * Initializes the driver.
     */
    void init();

    /**
     * Shuts down the driver. Always call before terminating.
     */
    void shutdown();

    /**
     * Reads the next {@link JoystickEvent}. Will block until an event occurs.
     */
    JoystickEvent read();

    /**
     * Flushes the buffer. Old events will be discarded.
     */
    void flush();
}
