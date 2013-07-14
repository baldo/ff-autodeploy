package net.freifunk.hamburg.autodeploy;

import java.io.File;

import net.freifunk.hamburg.autodeploy.devices.DeviceDeployer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Main class for auto deployer.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class Main {

    private static final String HELP_OPTION = "h";
    private static final String FIRMWARE_OPTION = "f";
    private static final String PASSWORD_OPTION = "p";
    private static final String NODENAME_OPTION = "n";

    @Inject private DeviceDeployer _deployer;
    @Inject private WebDriver _webDriver;

    public Main() {
        final Injector injector = Guice.createInjector(new AutoDeployModule());
        injector.injectMembers(this);
    }

    /**
    * Main entry point for the auto deployer.
    */
    public static void main(final String[] args) {
        final Options options = new Options();
        options.addOption(new Option(HELP_OPTION, "help", false, "Show this help."));
        options.addOption(new Option(FIRMWARE_OPTION, "firmware", true, "The firmware image."));
        options.addOption(new Option(PASSWORD_OPTION, "password", true, "The new root password for the device."));
        options.addOption(new Option(NODENAME_OPTION, "nodename", true, "The name for the node. Will also be the hostname."));

        final CommandLineParser parser = new PosixParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            commandLine = null; // The compiler doesn't get the exit and thus complains about commandLine not being initialized.
            System.err.println(e.getMessage());
            System.exit(255);
        }

        if (commandLine.hasOption(HELP_OPTION)) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("ff-autodeploy", options);
        }

        final String firmwareFileString = getArgValue(commandLine, FIRMWARE_OPTION, "No firmware image specified.", 1);
        final File firmwareImage = new File(firmwareFileString);

        if (!firmwareImage.exists()) {
            System.err.println("File not found: " + firmwareImage.getPath());
            System.exit(2);
        }

        if (!firmwareImage.isFile()) {
            System.err.println("Not a file: " + firmwareImage.getPath());
            System.exit(3);
        }

        final String password = getArgValue(commandLine, PASSWORD_OPTION, "Root password not set.", 4);
        final String nodename = getArgValue(commandLine, NODENAME_OPTION, "Node name not set.", 5);

        new Main().run(firmwareImage, password, nodename);
    }

    private static String getArgValue(
        final CommandLine commandLine,
        final String option,
        final String errorMessage,
        final int exitCode
    ) {
        final String value = Strings.nullToEmpty(commandLine.getOptionValue(option)).trim();

        if (value.isEmpty()) {
            System.err.println(errorMessage);
            System.exit(exitCode);
        }

        return value;
    }

    private void run(final File firmwareImage, final String password, final String nodename) {
        try {
            _deployer.deploy(firmwareImage, password, nodename);
        } finally {
            // tear down selenium
            _webDriver.close();
        }
    }
}
