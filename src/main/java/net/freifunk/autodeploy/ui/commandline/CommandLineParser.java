package net.freifunk.autodeploy.ui.commandline;

import net.freifunk.autodeploy.AutoDeployOptions;

/**
 * Parses the command line parameters.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface CommandLineParser {

    /**
     * Parses the command line arguments and returns the set {@link AutoDeployOptions}.
     *
     * @throws CommandLineParsingException in case of parsing errors.
     */
    AutoDeployOptions parse(String[] args) throws CommandLineParsingException;

    /**
     * Gives the help text describing the command line options.
     */
    String getHelpText();
}
