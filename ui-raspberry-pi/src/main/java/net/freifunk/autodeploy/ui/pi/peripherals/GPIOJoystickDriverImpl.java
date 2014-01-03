/*
 * Freifunk Auto Deployer
 * Copyright (C) 2013, 2014 by Andreas Baldeau <andreas@baldeau.net>
 *
 *
 * For contributers see file CONTRIB.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * Uses Logback (http://logback.qos.ch/) which is dual licensed under EPL v1.0 and LGPL v2.1.
 * See http://logback.qos.ch/license.html for details.
 */
package net.freifunk.autodeploy.ui.pi.peripherals;

import static com.pi4j.io.gpio.PinState.HIGH;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.BUTTON;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.DOWN;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.LEFT;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.RIGHT;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.UP;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.openqa.selenium.browserlaunchers.Sleeper;

import com.google.common.collect.ImmutableMap;
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

    private class PinListener implements GpioPinListenerDigital {
        private final Pin _pin;
        private final JoystickEvent _event;

        public PinListener(final Pin pin, final JoystickEvent event) {
            _pin = pin;
            _event = event;
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(
            final GpioPinDigitalStateChangeEvent event
        ) {
            synchronized (GPIOJoystickDriverImpl.this) {
                if (event.getState() != HIGH) {
                    return;
                }

                final int pinAddress = event.getPin().getPin().getAddress();

                if (pinAddress != _pin.getAddress()) {
                    return;
                }

                GPIOJoystickDriverImpl.this.push(_event);
            }
        }
    }

    private static final Pin UP_PIN = RaspiPin.GPIO_00;
    private static final Pin DOWN_PIN = RaspiPin.GPIO_01;
    private static final Pin LEFT_PIN = RaspiPin.GPIO_02;
    private static final Pin RIGHT_PIN = RaspiPin.GPIO_03;
    private static final Pin BUTTON_PIN = RaspiPin.GPIO_04;

    private static final Set<Pin> PINS = ImmutableSet.of(UP_PIN, DOWN_PIN, LEFT_PIN, RIGHT_PIN, BUTTON_PIN);
    private static final Map<Integer, JoystickEvent> EVENT_BY_PIN_ADDRESS = ImmutableMap.of(
        UP_PIN.getAddress(), UP,
        DOWN_PIN.getAddress(), DOWN,
        LEFT_PIN.getAddress(), LEFT,
        RIGHT_PIN.getAddress(), RIGHT,
        BUTTON_PIN.getAddress(), BUTTON
    );

    private static final PinPullResistance RESISTANCE = PinPullResistance.PULL_UP;

    private final GpioController _gpio;
    private final AtomicReference<JoystickEvent> _buffer;

    public GPIOJoystickDriverImpl() {
        _gpio = GpioFactory.getInstance();
        _buffer = new AtomicReference<JoystickEvent>();
    }

    @Override
    public synchronized void init() {
        for (final Pin pin: PINS) {
            final JoystickEvent event = EVENT_BY_PIN_ADDRESS.get(Integer.valueOf(pin.getAddress()));
            _gpio.provisionDigitalInputPin(pin, RESISTANCE).addListener(new PinListener(pin, event));
        }
    }

    @Override
    public synchronized void shutdown() {
        _gpio.removeAllListeners();
        _gpio.shutdown();
    }

    private void push(final JoystickEvent event) {
        synchronized (_buffer) {
            _buffer.set(event);
            _buffer.notifyAll();
        }
    }

    private JoystickEvent pop() {
        synchronized (_buffer) {
            JoystickEvent joystickEvent = _buffer.getAndSet(null);
            while (joystickEvent == null) {
                try {
                    _buffer.wait();
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }

                joystickEvent = _buffer.getAndSet(null);
            }
            return joystickEvent;
        }
    }

    @Override
    public JoystickEvent read() {
        Sleeper.sleepTight(200);
        flush();
        return pop();
    }

    @Override
    public void flush() {
        synchronized (_buffer) {
            _buffer.set(null);
        }
    }
}
