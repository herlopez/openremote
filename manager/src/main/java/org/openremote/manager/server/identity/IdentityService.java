package org.openremote.manager.server.identity;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hubrick.vertx.rest.RestClientOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.openremote.manager.server.Constants.*;

public class IdentityService {

    private static final Logger LOG = Logger.getLogger(IdentityService.class.getName());

    public static final String DISABLE_API_SECURITY  = "DISABLE_API_SECURITY";
    public static final boolean DISABLE_API_SECURITY_DEFAULT = false;
    public static final String IDENTITY_PROVIDER_HOST = "IDENTITY_PROVIDER_HOST";
    public static final String IDENTITY_PROVIDER_HOST_DEFAULT = "192.168.99.100";
    public static final String IDENTITY_PROVIDER_PORT = "IDENTITY_PROVIDER_PORT";
    public static final int IDENTITY_PROVIDER_PORT_DEFAULT = 8081;
    public static final String IDENTITY_PROVIDER_CONNECT_TIMEOUT_MS = "IDENTITY_PROVIDER_CONNECT_TIMEOUT_MS";
    public static final int IDENTITY_PROVIDER_CONNECT_TIMEOUT_MS_DEFAULT = 2000;
    public static final String IDENTITY_PROVIDER_REQUEST_TIMEOUT_MS = "IDENTITY_PROVIDER_REQUEST_TIMEOUT_MS";
    public static final int IDENTITY_PROVIDER_REQUEST_TIMEOUT_MS_DEFAULT = 10000;
    public static final String IDENTITY_PROVIDER_MAX_POOL_SIZE = "IDENTITY_PROVIDER_MAX_POOL_SIZE";
    public static final int IDENTITY_PROVIDER_MAX_POOL_SIZE_DEFAULT = 20;

    protected class ClientInstallKey {
        public final String realm;
        public final String clientId;

        public ClientInstallKey(String realm, String clientId) {
            this.realm = realm;
            this.clientId = clientId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClientInstallKey that = (ClientInstallKey) o;

            if (!realm.equals(that.realm)) return false;
            return clientId.equals(that.clientId);

        }

        @Override
        public int hashCode() {
            int result = realm.hashCode();
            result = 31 * result + clientId.hashCode();
            return result;
        }
    }

    protected boolean disableAPISecurity;
    protected boolean configNetworkSecure;
    protected String configNetworkHost;
    protected int configNetworkWebserverPort;
    protected KeycloakClient keycloakClient;
    protected LoadingCache<ClientInstallKey, ClientInstall> clientInstallCache;

    public void start(Vertx vertx, JsonObject config) {
        this.disableAPISecurity = config.getBoolean(DISABLE_API_SECURITY, DISABLE_API_SECURITY_DEFAULT);
        this.configNetworkSecure = config.getBoolean(NETWORK_SECURE, NETWORK_SECURE_DEFAULT);
        this.configNetworkHost = config.getString(NETWORK_HOST, NETWORK_HOST_DEFAULT);
        this.configNetworkWebserverPort = config.getInteger(NETWORK_WEBSERVER_PORT, NETWORK_WEBSERVER_PORT_DEFAULT);

        String host = config.getString(IDENTITY_PROVIDER_HOST, IDENTITY_PROVIDER_HOST_DEFAULT);
        int port = config.getInteger(IDENTITY_PROVIDER_PORT, IDENTITY_PROVIDER_PORT_DEFAULT);
        LOG.info("Starting identity service for Keycloak server: " + host + ":" + port);

        RestClientOptions clientOptions = new RestClientOptions()
            .setConnectTimeout(config.getInteger(IDENTITY_PROVIDER_CONNECT_TIMEOUT_MS, IDENTITY_PROVIDER_CONNECT_TIMEOUT_MS_DEFAULT))
            .setGlobalRequestTimeout(config.getInteger(IDENTITY_PROVIDER_REQUEST_TIMEOUT_MS, IDENTITY_PROVIDER_REQUEST_TIMEOUT_MS_DEFAULT))
            .setDefaultHost(host)
            .setDefaultPort(port)
            .setKeepAlive(true)
            .setMaxPoolSize(config.getInteger(IDENTITY_PROVIDER_MAX_POOL_SIZE, IDENTITY_PROVIDER_MAX_POOL_SIZE_DEFAULT));

        keycloakClient = new KeycloakClient(vertx, clientOptions);

        clientInstallCache = createClientInstallCache();
    }

    public void stop() {
        LOG.info("Stopping identity service...");
        if (keycloakClient != null) {
            keycloakClient.close();
        }
    }

    public boolean isDisableAPISecurity() {
        return disableAPISecurity;
    }

    public boolean isConfigNetworkSecure() {
        return configNetworkSecure;
    }

    public String getConfigNetworkHost() {
        return configNetworkHost;
    }

    public int getConfigNetworkWebserverPort() {
        return configNetworkWebserverPort;
    }

    public KeycloakClient getKeycloakClient() {
        return keycloakClient;
    }

    public ClientInstall getClientInstall(String realm, String clientId) {
        try {
            return clientInstallCache.get(new ClientInstallKey(realm, clientId));
        } catch (Exception ex) {
            LOG.log(
                Level.INFO,
                "Error loading client '" + clientId + "' install for realm '" + realm + "' from identity provider",
                ex
            );
            return null;
        }
    }

    protected LoadingCache<ClientInstallKey, ClientInstall> createClientInstallCache() {
        CacheLoader<ClientInstallKey, ClientInstall> loader =
            new CacheLoader<ClientInstallKey, ClientInstall>() {
                public ClientInstall load(ClientInstallKey key) {
                    LOG.fine("Loading client '" + key.clientId + "' install details for realm '" + key.realm+ "'");
                    return keycloakClient
                        .getClientInstall(key.realm, key.clientId)
                        .toBlocking().single();
                }
            };

        // TODO configurable?
        return CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, MINUTES)
            .build(loader);
    }
}