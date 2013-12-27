package net.freifunk.autodeploy.firmware;

import java.net.URI;
import java.util.Set;

import net.freifunk.autodeploy.selenium.Actor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Configures the Freifunk Lüneburg firmware.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FreifunkLueneburgConfigurator extends AbstractFreifunkNordConfigurator {

    public static final Set<Firmware> SUPPORTED_FIRMWARES = ImmutableSet.of(
        new Firmware("fflg", "Freifunk Lüneburg", URI.create("http://freifunk-lueneburg.de/"))
    );

    @Inject
    public FreifunkLueneburgConfigurator(
        final Actor actor
    ) {
        super(actor);
    }
}
