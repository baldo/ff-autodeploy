package net.freifunk.autodeploy.printing;

import static com.google.common.base.Charsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.freifunk.autodeploy.device.DetailedDevice;
import net.freifunk.autodeploy.firmware.FirmwareConfiguration;
import net.freifunk.autodeploy.firmware.FreifunkNordFirmwareConfiguration;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

/**
 * Default implementation of {@link LabelPrintingService}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class LabelPrintingServiceImpl implements LabelPrintingService {

    private static final String LABLE_PRINTER = "DYMO_LabelWriter_450";

    @Override
    public void printLabel(final DetailedDevice device, final FirmwareConfiguration configuration) {
        final String svg = createFilledInSVG(device, configuration);
        final File png = toPng(svg);

        try {
            printPng(png);
        }
        finally {
            if (png.exists()) {
                png.delete();
            }
        }
    }

    private final String createFilledInSVG(final DetailedDevice device, final FirmwareConfiguration configuration) {
        if (configuration instanceof FreifunkNordFirmwareConfiguration) {
            final FreifunkNordFirmwareConfiguration freifunkNordConfiguration = (FreifunkNordFirmwareConfiguration) configuration;
            final CharSource svgSource = Resources.asCharSource(Resources.getResource("ffnord-label.svg"), UTF_8);
            final String svgString;
            try {
                svgString = svgSource.read();
            } catch (final IOException e) {
                throw new IllegalStateException("Could not read SVG.", e);
            }

            final String vpnKey = freifunkNordConfiguration.getVpnKey();
            final int vpnKeyLength = vpnKey.length();
            final int vpnKeyMiddle = vpnKeyLength / 2;
            final String vpnKey1 = vpnKey.substring(0, vpnKeyMiddle);
            final String vpnKey2 = vpnKey.substring(vpnKeyMiddle, vpnKeyLength);

            return svgString
                .replace("{nodename}", freifunkNordConfiguration.getNodename())
                .replace("{password}", freifunkNordConfiguration.getPassword())
                .replace("{device}", device.getDevice().getModel() + " " + device.getDevice().getVersion())
                .replace("{mac}", device.getMac())
                .replace("{vpnKey1}", vpnKey1)
                .replace("{vpnKey2}", vpnKey2);
        } else {
            throw new IllegalArgumentException("Unsupported configuration: " + configuration.getClass());
        }
    }

    private File toPng(final String svg) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("label", ".png");
            final TranscoderInput transcoderInput = new TranscoderInput(new StringReader(svg));
            final PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(1051));
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(425));

            final FileOutputStream outputstream = new FileOutputStream(tempFile);

            final TranscoderOutput output = new TranscoderOutput(outputstream);

            transcoder.transcode(transcoderInput, output);
            outputstream.flush();
            outputstream.close();

            return tempFile;
        } catch (final IOException | TranscoderException e) {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            throw new IllegalStateException("Could not create PNG.", e);
        }
    }

    private void printPng(final File png) {
        final PrintRequestAttributeSet printRequestAttributes = new HashPrintRequestAttributeSet();
        final DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;
        final PrintService[] printServices = PrintServiceLookup.lookupPrintServices(flavor, printRequestAttributes);

        final PrintService printService = Iterables.getOnlyElement(Iterables.filter(
            ImmutableList.copyOf(printServices),
            new Predicate<PrintService>() {

                @Override
                public boolean apply(final PrintService input) {
                    return LABLE_PRINTER.equals(input.getName());
                }
            }
        ));

        final DocPrintJob job = printService.createPrintJob();
        final DocAttributeSet docAttributes = new HashDocAttributeSet();
        try {
            final Doc document = new SimpleDoc(new FileInputStream(png), flavor, docAttributes);
            job.print(document, printRequestAttributes);
        } catch (final PrintException | IOException e) {
            throw new IllegalStateException("Could not print label.", e);
        }
    }
}
