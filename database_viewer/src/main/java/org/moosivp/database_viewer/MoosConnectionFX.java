package org.moosivp.database_viewer;

import com.github.moos_ivp.moosbeans.MOOSCommClient;
import com.github.moos_ivp.moosbeans.MoosMessageHandler;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 * JavaFX wrapper around a {@link MOOSCommClient}. This wrapper only exposes the
 * ability to start and stop the client. It also exposes the ability to set the
 * hostanme and port for the client to use. Finally, it exposes a
 * {@link ReadOnlyBooleanProperty} that JavaFX controls can use to determine if
 * the client is connected. This property is safe to bind to in the JavaFX
 * thread.
 *
 * @author cgagner
 */
public final class MoosConnectionFX {

    /**
     * Default host used by the {@link MOOSCommClient}
     */
    public static final String DEFAULT_HOST = "localhost";
    /**
     * Default port used by the {@link MOOSCommClient}
     */
    public static final int DEFAULT_PORT = 9000;
    /**
     * Client
     */
    private final MOOSCommClient client;
    /**
     * Task to periodically check if the {@link MOOSCommClient} is connected.
     */
    private final TimerTask task;
    /**
     * Timer for controlling the {@link #task}.
     */
    private final Timer timer = new Timer();

    private final BooleanProperty connected = new SimpleBooleanProperty(false);
    private final StringProperty hostname = new SimpleStringProperty(DEFAULT_HOST);
    private final IntegerProperty port = new SimpleIntegerProperty(DEFAULT_PORT);

    /**
     * Create a {@link MOOSCommClient} with the specified name.
     *
     * @param name Name of the client.
     */
    public MoosConnectionFX(String name) {
        this(name, DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Create a {@link MOOSCommClient} with the specified name, and initialize
     * the host name.
     *
     * @param name Name of the client.
     * @param hostname Host name to initialize the client with.
     */
    public MoosConnectionFX(String name, String hostname) {
        this(name, hostname, DEFAULT_PORT);
    }

    /**
     * Create a {@link MOOSCommClient} with the specified name, and initialize
     * the host name and port.
     *
     * @param name Name of the client.
     * @param hostname Host name to initialize the client with.
     * @param port Port to initialized the client with.
     */
    public MoosConnectionFX(String name, String hostname, int port) {
        client = new MOOSCommClient(name, hostname, port);
        client.setAutoReconnect(true);
        task = new TimerTask() {
            @Override
            public void run() {
                // Make sure we are on the JavaFX thread when modifying 
                // properties.
                if (!Platform.isFxApplicationThread()) {
                    Platform.runLater(() -> run());
                    return;
                }
                connected.set(client.isConnected());
            }
        };
        timer.scheduleAtFixedRate(task, 100, 100);
        connectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                onConnected();
            }
        });
    }

    /**
     * Set the {@link MoosMessageHandler} that will be used to process MOOS
     * messages.
     *
     * @param handler {@link MoosMessageHandler} that will be used to process
     * MOOS messages.
     */
    public void setMessageHandler(MoosMessageHandler handler) {
        client.setMessageHandler(handler);
    }

    /**
     * Start the {@link MOOSCommClient} if it hasn't already been started.
     */
    public void start() {
        if (client.isConnected()) {
            return;
        }
        client.setHostname(getHostname());
        client.setPort(getPort());
        client.setEnable(true);
    }

    /**
     * Stop the {@link MOOSCommClient}.
     */
    public void stop() {
        client.setEnable(false);
    }

    /**
     * Read-only property that indicates if the {@link MOOSCommClient} is
     * connected.
     *
     * @return Read-only property that indicates if the {@link MOOSCommClient}
     * is connected.
     */
    public ReadOnlyBooleanProperty connectedProperty() {
        return connected;
    }

    /**
     * Check if the client is connected.
     *
     * @return true if the client is connected; false otherwise.
     */
    public boolean isConnected() {
        return connectedProperty().get();
    }

    /**
     * Called when the client is connected to the database. By default, this
     * subscribes to all messages using wild carding.
     */
    protected void onConnected() {
        client.register("DB_UPTIME", 0.0);
        client.register("DB_CLIENTS", 0.0);
        client.register("DB_RWSUMMARY", 0.0);
        client.register("*", "*", 0.0);
    }

    /**
     * Property for the hostname. Updating this value will not take effect until
     * the client is restarted.
     *
     * @return Property for the hostname.
     */
    public StringProperty hostnameProperty() {
        return hostname;
    }

    /**
     * Hostname for the client to use.
     *
     * @return Hostname for the client to use.
     */
    public String getHostname() {
        return hostnameProperty().get();
    }

    /**
     * Set the hostanme for the client to use. Updating this value will not take
     * effect until the client is restarted.
     *
     * @param hostname
     */
    public void setHostname(String hostname) {
        hostnameProperty().set(hostname);
    }

    /**
     * Property for the port the client will use. Updating this value will not
     * take effect until the client is restarted.
     *
     * @return Property for the port the client will use.
     */
    public IntegerProperty portProperty() {
        return port;
    }

    /**
     * Port the client will use.
     *
     * @return Port the client will use.
     */
    public Integer getPort() {
        return portProperty().get();
    }

    /**
     * Port the client will use. Updating this value will not take effect until
     * the client is restarted.
     *
     * @param port Port the client will use.
     */
    public void setPort(Integer port) {
        portProperty().setValue(port);
    }
}
