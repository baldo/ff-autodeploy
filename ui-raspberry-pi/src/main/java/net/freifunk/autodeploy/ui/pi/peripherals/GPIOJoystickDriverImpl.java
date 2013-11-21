package net.freifunk.autodeploy.ui.pi.peripherals;

import static com.pi4j.io.gpio.PinState.HIGH;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Implementation of {@link JoystickDriver} that uses Raspberry Pi's GPIO pins to read e.g. from
 * a Competition Pro joystick.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class GPIOJoystickDriverImpl implements JoystickDriver {

    private static final Pin UP_PIN = RaspiPin.GPIO_00;
    private static final Pin DOWN_PIN = RaspiPin.GPIO_01;
    private static final Pin LEFT_PIN = RaspiPin.GPIO_02;
    private static final Pin RIGHT_PIN = RaspiPin.GPIO_03;
    private static final Pin BUTTON_PIN = RaspiPin.GPIO_04;

    private static final Set<Pin> PINS = ImmutableSet.of(UP_PIN, DOWN_PIN, LEFT_PIN, RIGHT_PIN, BUTTON_PIN);

    private static final PinPullResistance RESISTANCE = PinPullResistance.PULL_UP;

    private final GpioController _gpio;
    private JoystickListener _listener;

    public GPIOJoystickDriverImpl() {
        _gpio = GpioFactory.getInstance();
        _listener = null;
    }

    @Override
    public synchronized void init() {
        final GpioPinListenerDigital pinListener = new GpioPinListenerDigital() {

            @Override
            public void handleGpioPinDigitalStateChangeEvent(
                final GpioPinDigitalStateChangeEvent event
            ) {
                synchronized (GPIOJoystickDriverImpl.this) {
                    if (_listener == null) {
                        return;
                    }

                    if (event.getState() != HIGH) {
                        return;
                    }

                    final int pinAddress = event.getPin().getPin().getAddress();

                    if (pinAddress == UP_PIN.getAddress()) {
                        _listener.up();
                        return;
                    }

                    if (pinAddress == DOWN_PIN.getAddress()) {
                        _listener.down();
                        return;
                    }

                    if (pinAddress == LEFT_PIN.getAddress()) {
                        _listener.left();
                        return;
                    }

                    if (pinAddress == RIGHT_PIN.getAddress()) {
                        _listener.right();
                        return;
                    }

                    if (pinAddress == BUTTON_PIN.getAddress()) {
                        _listener.button();
                        return;
                    }
                }
            }
        };

        for (final Pin pin: PINS) {
            _gpio.provisionDigitalInputPin(pin, RESISTANCE).addListener(pinListener);
        }
    }

    @Override
    public synchronized void shutdown() {
        unlisten();
        _gpio.removeAllListeners();
        _gpio.shutdown();
    }

    @Override
    public synchronized void listen(final JoystickListener listener) {
        _listener = listener;
    }

    @Override
    public synchronized void unlisten() {
        _listener = null;
    }
}
