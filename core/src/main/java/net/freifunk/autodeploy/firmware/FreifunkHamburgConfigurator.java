package net.freifunk.autodeploy.firmware;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import net.freifunk.autodeploy.device.DetailedDevice;
import net.freifunk.autodeploy.selenium.Actor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Configures the Freifunk Hamburg firmware.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FreifunkHamburgConfigurator extends AbstractFreifunkNordConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(FreifunkHamburgConfigurator.class);

    private static final String DEFAULT_CONTACT_EMAIL = "kontakt@hamburg.freifunk.net";
    private static final String DEFAULT_CONTACT_NICK = "vorregistriert_beim_30c3";

    private static final URI NODE_REGISTRATION_URI = URI.create("http://gw01.hamburg.freifunk.net:8080/api/node");
    private static final URI NODE_UPDATE_URI = URI.create("http://gw01.hamburg.freifunk.net:8080/");

    public static final Set<Firmware> SUPPORTED_FIRMWARES = ImmutableSet.of(
        new Firmware("ffhh", "Freifunk Hamburg", URI.create("http://wiki.freifunk.net/Freifunk_Hamburg/Firmware#Download"))
    );

    private final HttpClient _httpClient;
    private final ObjectMapper _objectMapper;

    @Inject
    public FreifunkHamburgConfigurator(
        final Actor actor,
        final HttpClient httpClient,
        final ObjectMapper objectMapper
    ) {
        super(actor);

        _httpClient = httpClient;
        _objectMapper = objectMapper;
    }

    @Override
    public boolean supportsNodeRegistration() {
        return true;
    }

    @Override
    public String registerNode(final FirmwareConfiguration configuration, final DetailedDevice device) {
        if (!(configuration instanceof FreifunkNordFirmwareConfiguration)) {
            throw new IllegalArgumentException("Invalid firmware configuration: " + configuration.getClass().getName());
        }
        try {
            final HttpResponse response = doPost(
                NODE_REGISTRATION_URI,
                toPostData((FreifunkNordFirmwareConfiguration) configuration, device)
            );
            final int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                final Map<String, String> result = getResponseData(response);
                final String token = result.get("token");
                if (Strings.isNullOrEmpty(token)) {
                    LOG.warn("Registration successful, but got no token!?");
                    return null;
                } else {
                    return token;
                }
            } else {
                LOG.warn("Could not register node.\n  HTTP response: " + response + "\n  Data: " + EntityUtils.toString(response.getEntity()));
                return null;
            }
        } catch (final IOException e) {
            LOG.warn("Could not register node.", e);
            return null;
        }
    }

    @Override
    public URI getNodeUpdateUri() {
        return NODE_UPDATE_URI;
    }

    private HttpResponse doPost(final URI uri, final Map<String, String> postData) throws IOException {
        final HttpPost request = new HttpPost(uri);

        request.setEntity(
            new StringEntity(
                _objectMapper.writeValueAsString(postData),
                ContentType.APPLICATION_JSON
            )
        );

        return _httpClient.execute(request);
    }

    private Map<String, String> toPostData(
            final FreifunkNordFirmwareConfiguration configuration,
            final DetailedDevice device) {
        return ImmutableMap.<String, String>builder()
            .put("hostname", configuration.getNodename())
            .put("mac", device.getMac())
            .put("key", configuration.getVpnKey())
            .put("email", DEFAULT_CONTACT_EMAIL)
            .put("nickname", DEFAULT_CONTACT_NICK)
            .put("coords", "")
        .build();
    }

    private Map<String, String> getResponseData(final HttpResponse response)
            throws IOException, JsonParseException, JsonMappingException {
        return _objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, String>>() {});
    }
}
