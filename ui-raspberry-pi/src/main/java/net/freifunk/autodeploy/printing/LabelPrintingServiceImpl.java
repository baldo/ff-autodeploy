/*
 * Freifunk Auto Deployer
 * Copyright (C) 2013, 2014 by Andreas Baldeau <andreas@baldeau.net>
 *
 *
 * For contributers see file CONTRIB.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * Uses Logback (http://logback.qos.ch/) which is dual licensed under EPL v1.0 and LGPL v2.1.
 * See http://logback.qos.ch/license.html for details.
 */
package net.freifunk.autodeploy.printing;

import static com.google.common.base.Charsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

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
import net.freifunk.autodeploy.firmware.Firmware;
import net.freifunk.autodeploy.firmware.FirmwareConfiguration;
import net.freifunk.autodeploy.firmware.FreifunkNordFirmwareConfiguration;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceImpl.class);
    private static final String LABLE_PRINTER = "DYMO_LabelWriter_450";

    @Override
    public void printLabel(
        final Firmware firmware,
        final DetailedDevice device,
        final FirmwareConfiguration configuration,
        final String updateToken,
        final URI updateUri
    ) {
        final String svg = createFilledInSVG(firmware, device, configuration, updateToken, updateUri);
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

    private final String createFilledInSVG(
        final Firmware firmware,
        final DetailedDevice device,
        final FirmwareConfiguration configuration,
        final String updateToken,
        final URI updateUri
    ) {
        if (configuration instanceof FreifunkNordFirmwareConfiguration) {
            final FreifunkNordFirmwareConfiguration freifunkNordConfiguration = (FreifunkNordFirmwareConfiguration) configuration;
            final CharSource svgSource = Resources.asCharSource(Resources.getResource(getSVGFilename(firmware)), UTF_8);
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
                .replace("{vpnKey2}", vpnKey2)
                .replace("{token}", updateToken == null ? "---" : updateToken)
                .replace("{updateUri}", updateUri.toString());
        } else {
            throw new IllegalArgumentException("Unsupported configuration: " + configuration.getClass());
        }
    }

    private String getSVGFilename(final Firmware firmware) {
        switch (firmware.getName()) {
            case "ffhh":
                return "ffhh-label.svg";
            default:
                return "ffnord-label.svg";
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

        final Iterable<PrintService> filteredPrintServices = Iterables.filter(
            ImmutableList.copyOf(printServices),
            new Predicate<PrintService>() {

                @Override
                public boolean apply(final PrintService input) {
                    return LABLE_PRINTER.equals(input.getName());
                }
            }
        );

        if (Iterables.isEmpty(filteredPrintServices)) {
            LOG.warn("No printer found. Aborting.");
            return;
        }

        final PrintService printService = Iterables.getOnlyElement(filteredPrintServices);

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
