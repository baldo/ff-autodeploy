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

        final String firmwareFileString = commandLine.getOptionValue(FIRMWARE_OPTION);

        if (firmwareFileString == null) {
            System.err.println("No firmware image specified.");
            System.exit(1);
        }

        final File firmwareImage = new File(firmwareFileString);

        if (!firmwareImage.exists()) {
            System.err.println("File not found: " + firmwareImage.getPath());
            System.exit(2);
        }

        if (!firmwareImage.isFile()) {
            System.err.println("Not a file: " + firmwareImage.getPath());
            System.exit(3);
        }

        new Main().run(firmwareImage);
    }

    private void run(final File firmwareImage) {
        try {
            _deployer.deploy(firmwareImage);
        } finally {
            // tear down selenium
            _webDriver.close();
        }
    }
}
