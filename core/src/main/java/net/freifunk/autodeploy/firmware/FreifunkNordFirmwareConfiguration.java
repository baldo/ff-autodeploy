package net.freifunk.autodeploy.firmware;

/**
 * Configuration of a Freifunk Nord router.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FreifunkNordFirmwareConfiguration implements FirmwareConfiguration {

    private final String _nodename;
    private final String _password;
    private final String _vpnKey;

    public FreifunkNordFirmwareConfiguration(
        final String nodename,
        final String password,
        final String vpnKey
    ) {
        _nodename = nodename;
        _password = password;
        _vpnKey = vpnKey;
    }

    public String getNodename() {
        return _nodename;
    }

    public String getPassword() {
        return _password;
    }

    public String getVpnKey() {
        return _vpnKey;
    }
}
