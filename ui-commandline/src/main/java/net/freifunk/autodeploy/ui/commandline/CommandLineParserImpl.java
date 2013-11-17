package net.freifunk.autodeploy.ui.commandline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import net.freifunk.autodeploy.AutoDeployOptions;
import net.freifunk.autodeploy.PhaseOptions;
import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.device.DeviceService;
import net.freifunk.autodeploy.firmware.Firmware;
import net.freifunk.autodeploy.firmware.FirmwareService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Default implementation of {@link CommandLineParser}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class CommandLineParserImpl implements CommandLineParser {

    private static final String HELP_OPTION = "h";
    private static final String LIST_MODELS_OPTION = "M";
    private static final String LIST_FIRMWARES_OPTION = "F";
    private static final String DEPLOY_OPTION = "d";
    private static final String CONFIGURE_OPTION = "c";
    private static final String FIRMWARE_OPTION = "f";
    private static final String FIRMWARE_IMAGE_OPTION = "i";
    private static final String PASSWORD_OPTION = "p";
    private static final String GENERATE_PASSWORD_OPTION = "P";
    private static final String NODENAME_OPTION = "n";
    private static final String MODEL_OPTION = "m";
    private static final String AUTODETECT_MODEL_OPTION = "a";

    private final DeviceService _deviceService;
    private final FirmwareService _firmwareService;

    @Inject
    public CommandLineParserImpl(
        final DeviceService deviceService,
        final FirmwareService firmwareService
    ) {
        _deviceService = deviceService;
        _firmwareService = firmwareService;
    }

    private Options createOptions() {
        final Options options = new Options();
        options.addOption(new Option(HELP_OPTION, "help", false, "Show this help."));
        options.addOption(new Option(LIST_MODELS_OPTION, "list-models", false, "List supported models."));
        options.addOption(new Option(LIST_FIRMWARES_OPTION, "list-firmwares", false, "List supported firmwares."));
        options.addOption(new Option(DEPLOY_OPTION, "deploy", false, "Deploy the firmware to the device."));
        options.addOption(new Option(CONFIGURE_OPTION, "configure", false, "Configure the firmware on the device."));
        options.addOption(new Option(FIRMWARE_OPTION, "firmware", true, "The firmware to configure."));
        options.addOption(new Option(FIRMWARE_IMAGE_OPTION, "image", true, "The firmware image."));
        options.addOption(new Option(PASSWORD_OPTION, "password", true, "The new root password for the device."));
        options.addOption(new Option(GENERATE_PASSWORD_OPTION, "gen-password", false, "The root password for the device will be generated."));
        options.addOption(new Option(NODENAME_OPTION, "nodename", true, "The name for the node. Will also be the hostname."));
        options.addOption(new Option(MODEL_OPTION, "model", true, "The model."));
        options.addOption(new Option(AUTODETECT_MODEL_OPTION, "autodetect-model", false, "Autodetect the model."));

        return options;
    }

    private String getLongOption(final Options options, final String shortOption) {
        return Preconditions.checkNotNull(
            options.getOption(shortOption).getLongOpt(),
            "No long option defined for: " + shortOption
        );
    }

    private static String getArgValue(
        final CommandLine commandLine,
        final String option,
        final String errorMessage
    ) throws CommandLineParsingException {
        final String value = Strings.nullToEmpty(commandLine.getOptionValue(option)).trim();

        if (value.isEmpty()) {
            throw new CommandLineParsingException(errorMessage);
        }

        return value;
    }

    @Override
    public AutoDeployOptions parse(final String[] args) throws CommandLineParsingException {
        final Options options = createOptions();
        final PosixParser parser = new PosixParser();

        final CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (final ParseException e) {
            throw new CommandLineParsingException(e.getMessage(), e);
        }

        if (commandLine.hasOption(HELP_OPTION)) {
            return AutoDeployOptions.forHelp();
        }

        final boolean listFirmwares = commandLine.hasOption(LIST_FIRMWARES_OPTION);
        final boolean listModels = commandLine.hasOption(LIST_MODELS_OPTION);

        if (listFirmwares || listModels) {
            return AutoDeployOptions.forListings(listFirmwares, listModels);
        }

        final ImmutableSet.Builder<PhaseOptions> phases = ImmutableSet.builder();

        final boolean deploy = commandLine.hasOption(DEPLOY_OPTION);
        if (deploy) {
            final boolean deviceGiven = commandLine.hasOption(MODEL_OPTION);
            final boolean autodetectDevice = commandLine.hasOption(AUTODETECT_MODEL_OPTION);

            if (deviceGiven && autodetectDevice) {
                throw new CommandLineParsingException(
                    "Specifying both --" + getLongOption(options, MODEL_OPTION) +
                    " and --" + getLongOption(options, AUTODETECT_MODEL_OPTION) + " is not supported."
                );
            }

            if (!deviceGiven && !autodetectDevice) {
                throw new CommandLineParsingException(
                    "Neither --" + getLongOption(options, MODEL_OPTION) +
                    " nor --" + getLongOption(options, AUTODETECT_MODEL_OPTION) + " is given."
                );
            }

            final String deviceString = autodetectDevice ? null : getArgValue(
                commandLine,
                MODEL_OPTION,
                "Model not set."
            );

            final Device device;

            if (deviceGiven) {
                device = _deviceService.findSupportedDevice(deviceString);

                if (device == null) {
                    throw new CommandLineParsingException("Unknown device: " + deviceString);
                }
            } else {
                device = null;
            }

            final String firmwareFileString = getArgValue(
                commandLine,
                FIRMWARE_IMAGE_OPTION,
                "No firmware image specified."
            );

            final File firmwareImage = new File(firmwareFileString);
            phases.add(PhaseOptions.forDeployPhase(
                device,
                autodetectDevice,
                firmwareImage
            ));
        }

        final boolean configure = commandLine.hasOption(CONFIGURE_OPTION);
        if (configure) {
            final String firmwareString = getArgValue(
                commandLine,
                FIRMWARE_OPTION,
                "No firmware specified."
            );
            final Firmware firmware = _firmwareService.findSupportedFirmware(firmwareString);

            if (firmware == null) {
                throw new CommandLineParsingException("Unknown firmware: " + firmwareString);
            }

            final String nodename = getArgValue(
                commandLine,
                NODENAME_OPTION,
                "Node name not set."
            );

            final boolean passwordGiven = commandLine.hasOption(PASSWORD_OPTION);
            final boolean generatePassword = commandLine.hasOption(GENERATE_PASSWORD_OPTION);

            if (passwordGiven && generatePassword) {
                throw new CommandLineParsingException(
                    "Specifying both --" + getLongOption(options, PASSWORD_OPTION) +
                    " and --" + getLongOption(options, GENERATE_PASSWORD_OPTION) + " is not supported."
                );
            }

            if (!passwordGiven && !generatePassword) {
                throw new CommandLineParsingException(
                    "Neither --" + getLongOption(options, PASSWORD_OPTION) +
                    " nor --" + getLongOption(options, GENERATE_PASSWORD_OPTION) + " is given."
                );
            }

            if (passwordGiven) {
                final String password = getArgValue(
                    commandLine,
                    PASSWORD_OPTION,
                    "Root password not set."
                );

                phases.add(PhaseOptions.forConfigurePhase(
                    firmware,
                    nodename,
                    password
                ));
            } else {
                phases.add(PhaseOptions.forConfigurePhase(
                    firmware,
                    nodename
                ));
            }
        }

        return AutoDeployOptions.forPhases(phases.build());
    }

    @Override
    public String getHelpText() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(outputStream);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
            writer,
            helpFormatter.getWidth(),
            "ff-autodeploy",
            null, // no header
            createOptions(),
            helpFormatter.getLeftPadding(),
            helpFormatter.getDescPadding(),
            null, // no footer
            false // no auto usage
        );

        writer.flush();
        return new String(outputStream.toByteArray());
    }
}
