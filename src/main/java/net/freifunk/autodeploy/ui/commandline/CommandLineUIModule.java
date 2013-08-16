package net.freifunk.autodeploy.ui.commandline;

import static com.google.inject.Scopes.SINGLETON;
import net.freifunk.autodeploy.AutoDeployModule;

import com.google.inject.AbstractModule;

/**
 * Module for configuring the command line UI.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class CommandLineUIModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AutoDeployModule());
        bind(CommandLineParser.class).to(CommandLineParserImpl.class).in(SINGLETON);
    }
}
