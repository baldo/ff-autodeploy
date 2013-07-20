package net.freifunk.hamburg.autodeploy;

import static com.google.inject.name.Names.named;

import java.io.File;
import java.util.List;
import java.util.Set;

import net.freifunk.hamburg.autodeploy.devices.Device;
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
import com.google.common.collect.Ordering;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Main class for auto deployer.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class Main {

    private static final String HELP_OPTION = "h";
    private static final String LIST_MODELS_OPTION = "l";
    private static final String FIRMWARE_OPTION = "f";
    private static final String PASSWORD_OPTION = "p";
    private static final String NODENAME_OPTION = "n";
    private static final String MODEL_OPTION = "m";

    private final Injector _injector;

    public Main() {
        _injector = Guice.createInjector(new AutoDeployModule());
    }

    /**
    * Main entry point for the auto deployer.
    */
    public static void main(final String[] args) {
        final Options options = new Options();
        options.addOption(new Option(HELP_OPTION, "help", false, "Show this help."));
        options.addOption(new Option(LIST_MODELS_OPTION, "list-models", false, "List supported models."));
        options.addOption(new Option(FIRMWARE_OPTION, "firmware", true, "The firmware image."));
        options.addOption(new Option(PASSWORD_OPTION, "password", true, "The new root password for the device."));
        options.addOption(new Option(NODENAME_OPTION, "nodename", true, "The name for the node. Will also be the hostname."));
        options.addOption(new Option(MODEL_OPTION, "model", true, "The model."));

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
            System.exit(0);
        }

        if (commandLine.hasOption(LIST_MODELS_OPTION)) {
            new Main().listModels();
            System.exit(0);
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
        final String model = getArgValue(commandLine, MODEL_OPTION, "Model not set.", 6);

        new Main().deploy(firmwareImage, password, nodename, model);
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

    private void listModels() {
        final Set<Device> supportedDevices = getSupportedDevices();
        final List<Device> sortedDevices = Ordering.<Device>natural().sortedCopy(supportedDevices);

        final StringBuilder output = new StringBuilder();
        output.append("Supported models:");
        for (final Device device: sortedDevices) {
            output.append("\n    ");
            output.append(device.asString());
        }
        System.out.println(output.toString());
    }

    private Set<Device> getSupportedDevices() {
        return _injector.getInstance(Key.get(new TypeLiteral<Set<Device>>(){}));
    }

    private DeviceDeployer getDeployer(final String model) {
        try {
            return _injector.getInstance(Key.get(DeviceDeployer.class, named(model)));
        } catch (final ConfigurationException e) {
            System.err.println("No device deployer registered for model: " + model);
            System.exit(254);
            return null; // the compiler doesn't get the exit
        }
    }

    private void deploy(final File firmwareImage, final String password, final String nodename, final String model) {
        try {
            getDeployer(model).deploy(firmwareImage, password, nodename);
        } finally {
            // tear down selenium
            _injector.getInstance(WebDriver.class).close();
        }
    }
}
