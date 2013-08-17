package net.freifunk.autodeploy.ui.pi.peripherals;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Implementation of {@link ButtonDriver} that uses Raspberry Pi's GPIO pins.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class GPIOButtonDriverImpl implements ButtonDriver {

    private static final Pin BUTTON_PIN = RaspiPin.GPIO_02;
    private static final PinPullResistance RESISTANCE = PinPullResistance.PULL_DOWN;

    private final GpioController _gpio;
    private ButtonListener _listener;

    public GPIOButtonDriverImpl() {
        _gpio = GpioFactory.getInstance();
        _listener = null;
    }

    @Override
    public synchronized void init() {
        final GpioPinDigitalInput button = _gpio.provisionDigitalInputPin(BUTTON_PIN, RESISTANCE);
        button.addListener(new GpioPinListenerDigital() {

            @Override
            public void handleGpioPinDigitalStateChangeEvent(
                final GpioPinDigitalStateChangeEvent event
            ) {
                synchronized (GPIOButtonDriverImpl.this) {
                    if (_listener == null) {
                        return;
                    }

                    if (!isRelevantPin(event.getPin().getPin())) {
                        return;
                    }

                    final PinState state = event.getState();
                    switch (state) {
                        case HIGH:
                            _listener.pressed();
                        break;

                        case LOW:
                            _listener.released();
                        break;

                        default:
                            throw new IllegalStateException("Unexpected button state: " + state);
                    }
                }
            }

            private boolean isRelevantPin(final Pin pin) {
                return pin.getAddress() == BUTTON_PIN.getAddress() && pin.getName().equals(BUTTON_PIN.getName());
            }
        });
    }

    @Override
    public synchronized void shutdown() {
        unlisten();
        _gpio.removeAllListeners();
        _gpio.shutdown();
    }

    @Override
    public synchronized void listen(final ButtonListener listener) {
        _listener = listener;
    }

    @Override
    public synchronized void unlisten() {
        _listener = null;
    }
}
