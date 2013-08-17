package net.freifunk.autodeploy.ui.commandline;

/**
 * Exception in case parsing the command line failed.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class CommandLineParsingException extends Exception {

    public CommandLineParsingException(final String message) {
        super(message);
    }

    public CommandLineParsingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
