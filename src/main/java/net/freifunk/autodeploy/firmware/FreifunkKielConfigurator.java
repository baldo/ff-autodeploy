package net.freifunk.autodeploy.firmware;

import java.net.URI;
import java.util.Set;

import net.freifunk.autodeploy.selenium.Actor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Configures the Freifunk Kiel firmware.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FreifunkKielConfigurator extends AbstractFreifunkNordConfigurator {

    public static final Set<Firmware> SUPPORTED_FIRMWARES = ImmutableSet.of(
        new Firmware("ffki", "Freifunk Kiel", URI.create("http://freifunk.in-kiel.de/mitmachen.html"))
    );

    @Inject
    public FreifunkKielConfigurator(
        final Actor actor
    ) {
        super(actor);
    }
}
