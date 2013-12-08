package net.freifunk.autodeploy.ui.pi.peripherals;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.base.Strings;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

/**
 * Implementation of {@link LCDDriver} for the Grove SerialLCD v1.1.
 *
 * @see <a href="http://www.seeedstudio.com/wiki/Grove_-_Serial_LCD">http://www.seeedstudio.com/wiki/Grove_-_Serial_LCD</a>
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class GroveSerialLCDDriverImpl implements LCDDriver {

    private static final String DEVICE = Serial.DEFAULT_COM_PORT;
    private static final int BAUD_RATE = 9600;

    private static final int ROWS = 2;
    private static final int COLUMNS = 16;
    private static final int CHARS = COLUMNS * ROWS;

    private static final int HEADER_CMD = 0x9F;
    private static final int HEADER_CURSOR = 0xFF;
    private static final int HEADER_CHAR = 0xFE;

    private static final int CMD_POWER_ON = 0x83;
    private static final int CMD_POWER_OFF = 0x82;
    private static final int CMD_DISPLAY_OFF = 0x63;
    private static final int CMD_BACKLIGHT_ON = 0x81;
    private static final int CMD_BACKLIGHT_OFF = 0x80;
    private static final int CMD_INIT = 0xA5;
    private static final int CMD_LTR = 0x70;
    private static final int CMD_CLEAR_SCREEN = 0x65;
    private static final int CMD_CURSOR_HOME = 0x61;

    private final Serial _serial;

    public GroveSerialLCDDriverImpl() {
        _serial = SerialFactory.createInstance();
        _serial.open(DEVICE, BAUD_RATE);
    }

    private byte[] toBytes(final int... values) {
        final byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    private void sleep(final long ms) {
        try {
            MILLISECONDS.sleep(ms);
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Got interrupted during sleep.", e);
        }
    }

    private void cmd(final int cmd, final int ... args) {
        _serial.write(toBytes(HEADER_CMD, cmd));
        _serial.write(toBytes(args));
        sleep(30);
    }

    @Override
    public void init() {
        cmd(CMD_POWER_ON);
        cmd(CMD_INIT);
        cmd(CMD_LTR);
        cmd(CMD_CLEAR_SCREEN);
        cmd(CMD_CURSOR_HOME);
        cmd(CMD_BACKLIGHT_ON);
    }

    @Override
    public void shutdown() {
        cmd(CMD_CLEAR_SCREEN);
        cmd(CMD_DISPLAY_OFF);
        cmd(CMD_BACKLIGHT_OFF);
        cmd(CMD_POWER_OFF);
        _serial.shutdown();
    }

    @Override
    public void writeString(final String str) {
        final String padded = ensureLength(str, CHARS);

        for (int row = 0; row < ROWS; row ++) {
            final String line = padded.substring(row * COLUMNS, (row + 1) * COLUMNS);
            writeLine(row, line);
        }
    }

    @Override
    public void writeLines(final String ... lines) {
        final StringBuilder builder = new StringBuilder();

        for (final String line: lines) {
            builder.append(ensureLength(line, COLUMNS));
        }

        writeString(builder.toString());
    }

    private String ensureLength(final String str, final int expectedLength) {
        final int actualLength = str.length();
        if (actualLength < expectedLength) {
            return str + Strings.repeat(" ", expectedLength - actualLength);
        }
        if (actualLength > expectedLength) {
            return str.substring(0, expectedLength);
        }
        return str;
    }

    private void writeLine(final int row, final String line) {
        setCursor(0, row);

        for (final byte b: line.getBytes()) {
            _serial.write(toBytes(HEADER_CHAR, b));
        }
    }

    private void setCursor(final int column, final int row) {
        cmd(HEADER_CURSOR, column, row);
    }
}
