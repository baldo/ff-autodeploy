package net.freifunk.autodeploy.ui.pi;

import static java.util.concurrent.TimeUnit.SECONDS;
import net.freifunk.autodeploy.ui.pi.peripherial.LCDDriver;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * Main class for running the auto deployer on the Raspberry Pi.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class RaspberryPiMain {

    @Inject private LCDDriver _lcdDriver;

    public RaspberryPiMain() {
        Guice.createInjector(new RaspberryPiUIModule()).injectMembers(this);
    }

    public static void main(final String args[]) throws InterruptedException {
        new RaspberryPiMain().run(args);
    }

    private void run(final String[] args) {
        try {
            _lcdDriver.init();
            for (int i = 0; i <= 120; i ++) {
                _lcdDriver.writeString("0123456789abcdef" + RandomStringUtils.randomAlphanumeric(16));
                try {
                    SECONDS.sleep(1);
                } catch (final InterruptedException e) {
                    throw new IllegalStateException("Interrupted.", e);
                }
            }
        }
        finally {
            _lcdDriver.shutdown();
        }
    }
}
