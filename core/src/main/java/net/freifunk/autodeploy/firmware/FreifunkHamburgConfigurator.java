package net.freifunk.autodeploy.firmware;

import java.net.URI;
import java.util.Set;

import net.freifunk.autodeploy.selenium.Actor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Configures the Freifunk Hamburg firmware.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FreifunkHamburgConfigurator extends AbstractFreifunkNordConfigurator {

    public static final Set<Firmware> SUPPORTED_FIRMWARES = ImmutableSet.of(
        new Firmware("ffhh", "Freifunk Hamburg", URI.create("http://wiki.freifunk.net/Freifunk_Hamburg/Firmware#Download"))
    );

    @Inject
    public FreifunkHamburgConfigurator(
        final Actor actor
    ) {
        super(actor);
    }
}
