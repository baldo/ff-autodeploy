package net.freifunk.autodeploy.firmware;

import java.net.URI;
import java.util.Set;

import net.freifunk.autodeploy.selenium.Actor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Configures the Freifunk Lübeck firmware.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FreifunkLuebeckConfigurator extends AbstractFreifunkNordConfigurator {

    public static final Set<Firmware> SUPPORTED_FIRMWARES = ImmutableSet.of(
        new Firmware("ffhl", "Freifunk Lübeck", URI.create("http://freifunk.metameute.de/mitmachen.html"))
    );

    @Inject
    public FreifunkLuebeckConfigurator(
        final Actor actor
    ) {
        super(actor);
    }
}
