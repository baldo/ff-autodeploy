package net.freifunk.autodeploy.ui.pi.peripherals;

/**
 * Driver for listening for button events.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface ButtonDriver {

    /**
     * Initializes the driver.
     */
    void init();

    /**
     * Shuts down the driver. Always call before terminating.
     */
    void shutdown();

    /**
     * Listens for state changes of the button. An existing listener will be removed.
     */
    void listen(ButtonListener listener);

    /**
     * Stops listening for the button.
     */
    void unlisten();
}
