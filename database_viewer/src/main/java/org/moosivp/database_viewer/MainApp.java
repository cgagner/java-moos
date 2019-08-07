package org.moosivp.database_viewer;

import com.github.moos_ivp.moosbeans.MOOSMsg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);

    private final Map<String, Integer> fieldIndex = new HashMap<>();
    private final ObservableList<MoosData> data = FXCollections.observableArrayList();
    private final FilteredList<MoosData> filteredData = new FilteredList<>(data, d -> true);
    private final SortedList<MoosData> sortedData = new SortedList<>(filteredData);

    private final Set<String> clientSet = new HashSet<>();
    private final ObservableList<String> clients = FXCollections.observableArrayList();

    private final Map<String, ObservableList<String>> subscribes = new HashMap<>();
    private final Map<String, ObservableList<String>> publishes = new HashMap<>();

    boolean matches(String filter, String value) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }

        String lower = filter.toLowerCase();
        return value != null && value.toLowerCase().contains(lower);
    }

    boolean checkFilter(String filter, MoosData value) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }

        return matches(filter, value.getName());
        // || matches(filter, value.getValue());
    }

    void updateList(ObservableList<String> current, Set<String> newValues) {
        List<String> toRemove = current.stream().filter(c -> !newValues.contains(c)).collect(Collectors.toList());
        List<String> toAdd = newValues.stream().filter(c -> !current.contains(c)).collect(Collectors.toList());
        current.removeAll(toRemove);
        current.addAll(toAdd);
    }

    void handleNewMessages(ArrayList<MOOSMsg> messages) {
        // Make sure we are on the JavaFX thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> handleNewMessages(messages));
            return;
        }
        
        messages.stream().forEachOrdered(msg -> {
            String key = msg.getKey().trim();
            Integer i = fieldIndex.get(key);

            if (i != null && i >= 0) {
                data.get(i).update(msg);
            } else {
                MoosData d = new MoosData(msg);
                data.add(d);
                fieldIndex.put(key, data.lastIndexOf(d));
            }

            if (key.toUpperCase().contains("DB_VAR") || key.toUpperCase().contains("DB_CLIENT") || key.toUpperCase().contains("DB_RW")) {
                //System.out.println(key + ": " + msg.getStringData());
            }

            if (key.toUpperCase().equals("DB_CLIENTS")) {
                Set<String> clientNames = new HashSet<>(Arrays.asList(msg.getStringData().split(",")));
                clients.removeAll(clientSet.stream().filter(c -> !clientNames.contains(c)).collect(Collectors.toList()));
                clients.addAll(clientNames.stream().filter(c -> !clientSet.contains(c)).collect(Collectors.toList()));

                clientSet.clear();
                clientSet.addAll(clientNames);
            } else if (key.toUpperCase().equals("DB_RWSUMMARY")) {
                String[] allClients = msg.getStringData().split(",");
                for (String s : allClients) {
                    String[] tmp = s.split("=", 2);
                    String name = tmp[0];
                    String[] pubsub = tmp[1].split("&", 2);
                    Set<String> subs = new HashSet<>(Arrays.asList(pubsub[0].split(":")));
                    Set<String> pubs = new HashSet<>(Arrays.asList(pubsub[1].split(":")));

                    if (!publishes.containsKey(name)) {
                        publishes.put(name, FXCollections.observableArrayList());
                    }
                    updateList(publishes.get(name), pubs);

                    if (!subscribes.containsKey(name)) {
                        subscribes.put(name, FXCollections.observableArrayList());
                    }
                    updateList(subscribes.get(name), subs);
                }
            }

        });
    }

    TableColumn addColumn(TableView tableView, String title, String property) {
        TableColumn<MoosData, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(50);
        column.setPrefWidth(200);
        column.setMaxWidth(5000);

        column.setCellFactory((TableColumn<MoosData, Object> param) -> new MoosTableCell());

        tableView.getColumns().add(column);
        return column;
    }

    /**
     * Creates the HBox that will appear at the top of the window with the
     * filter {@link TextField}.
     *
     * @return Filter Box.
     */
    private HBox createFilterBox() {
        final Label filterLabel = new Label("Filter: ");
        filterLabel.setAlignment(Pos.CENTER_LEFT);
        filterLabel.setMinWidth(Label.USE_COMPUTED_SIZE);
        final TextField filterText = new TextField();
        filterText.setMaxWidth(Double.MAX_VALUE);
        // Clear the text field when escape is pressed.
        filterText.setOnKeyPressed((KeyEvent event) -> {
            if (!event.isAltDown() && !event.isConsumed() && event.getCode() == KeyCode.ESCAPE) {
                filterText.setText("");
            }
        });

        filterText.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            filteredData.setPredicate((MoosData moosData) -> {
                return checkFilter(newValue, moosData);
            });
        });

        final HBox hb = new HBox(filterLabel, filterText);
        hb.setSpacing(5.0);
        HBox.setHgrow(filterText, Priority.ALWAYS);
        return hb;
    }

    /**
     * Create the table for displaying all of the message data.
     *
     * @param connection {@link MoosConnectionFX}
     * @return Table for displaying all of the message data.
     */
    private TableView createMessageTable(MoosConnectionFX connection) {
        final TableView tv = new TableView();
        tv.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);
        // Allow the table to be sortable
        sortedData.comparatorProperty().bind(tv.comparatorProperty());
        // Set the data in the table to be the filtered/sorted list.
        tv.setItems(sortedData);

        // Add all of the columns
        addColumn(tv, "Name", "name");
        addColumn(tv, "Time", "time");
        addColumn(tv, "Source", "source");
        addColumn(tv, "Community", "community").setPrefWidth(50);
        addColumn(tv, "Value", "value").setPrefWidth(500);

        // Disable the table when the client is not connected.
        tv.disableProperty().bind(Bindings.not(connection.connectedProperty()));

        return tv;
    }

    /**
     * Creates the lower status pane.
     *
     * @param connection {@link MoosConnectionFX}
     * @return Lower status pane.
     */
    private Pane createStatusPane(MoosConnectionFX connection) {
        AnchorPane statusPane = new AnchorPane();
        HBox statusHBox = new HBox();
        statusHBox.setSpacing(5.0);
        statusPane.getChildren().add(statusHBox);
        AnchorPane.setTopAnchor(statusHBox, 0.0);
        AnchorPane.setBottomAnchor(statusHBox, 0.0);
        AnchorPane.setLeftAnchor(statusHBox, 0.0);
        AnchorPane.setRightAnchor(statusHBox, 0.0);

        // Create a table for displaying the list of clients.
        TableView<String> clientsView = new TableView<>(clients);
        TableColumn<String, String> clientColumn = new TableColumn<>("Clients");
        clientColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue()));
        clientsView.getColumns().add(clientColumn);
        clientsView.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);
        clientsView.disableProperty().bind(Bindings.not(connection.connectedProperty()));

        // Create a table for displaying the variables the selected client subscribes to.
        TableView<String> subsView = new TableView<>();
        TableColumn<String, String> subColumn = new TableColumn<>("Subscribes");
        subColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue()));
        subsView.getColumns().add(subColumn);
        subsView.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);
        subsView.disableProperty().bind(Bindings.not(connection.connectedProperty()));

        // Create a table for displaying the variables the selected client publishes.
        TableView<String> pubsView = new TableView<>();
        TableColumn<String, String> pubColumn = new TableColumn<>("Publishes");
        pubColumn.setCellValueFactory(s -> new SimpleStringProperty(s.getValue()));
        pubsView.getColumns().add(pubColumn);
        pubsView.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);
        statusHBox.getChildren().addAll(clientsView, subsView, pubsView);
        pubsView.disableProperty().bind(Bindings.not(connection.connectedProperty()));

        // Handle selection changes in the client list.
        clientsView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null && !newValue.isEmpty() && subscribes.containsKey(newValue) && publishes.containsKey(newValue)) {
                pubsView.setItems(publishes.get(newValue));
                subsView.setItems(subscribes.get(newValue));
            } else {
                pubsView.setItems(FXCollections.emptyObservableList());
                subsView.setItems(FXCollections.emptyObservableList());
            }
        });

        // Create a pane for 
        GridPane connectionBox = new GridPane();
        connectionBox.setHgap(10.0);
        connectionBox.setVgap(10.0);
        TitledPane connectionPane = new TitledPane("Connection", connectionBox);
        connectionPane.setCollapsible(false);
        TextField hostField = new TextField(connection.getHostname());
        connectionBox.addRow(0, new Label("Host:"), hostField);
        Spinner<Integer> portField = new Spinner<>(0, 65535, connection.getPort());
        connectionBox.addRow(1, new Label("Port:"), portField);
        Button connectButton = new Button("Connect");
        connectionBox.add(connectButton, 1, 2);
        GridPane.setColumnSpan(connectButton, GridPane.REMAINING);

        connection.hostnameProperty().bind(hostField.textProperty());
        connection.portProperty().bind(portField.valueProperty());

        hostField.disableProperty().bind(connection.connectedProperty());
        portField.disableProperty().bind(connection.connectedProperty());

        connectButton.setOnAction((ActionEvent event) -> {
            if (connection.isConnected()) {
                connection.stop();
            } else {
                connection.start();
            }
        });

        connection.connectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                connectButton.setText("Disconnect");
            } else {
                connectButton.setText("Connect");
            }
        });

        statusHBox.getChildren().addAll(connectionPane);

        return statusPane;
    }

    /**
     * Create the JavaFX {@link Scene}
     *
     * @param stage Stage to display the {@link Scene}
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        // Create the MOOS Client.
        MoosConnectionFX connection = new MoosConnectionFX("Moos Database Viewer");
        connection.setMessageHandler((ArrayList<MOOSMsg> messages) -> {
            handleNewMessages(messages);
            return true;
        });

        final VBox vbox = new VBox();
        vbox.setSpacing(5.0);
        final HBox filterBox = createFilterBox();
        final TableView tv = createMessageTable(connection);
        vbox.getChildren().addAll(filterBox, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);

        Pane statusPane = createStatusPane(connection);

        SplitPane split = new SplitPane(vbox, statusPane);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPosition(0, 70.0);

        final AnchorPane root = new AnchorPane();
        root.getChildren().add(split);
        AnchorPane.setTopAnchor(split, 10.0);
        AnchorPane.setBottomAnchor(split, 10.0);
        AnchorPane.setLeftAnchor(split, 10.0);
        AnchorPane.setRightAnchor(split, 10.0);

        Scene scene = new Scene(root, 1200, 600);
        stage.setTitle("MOOS Database Viewer");
        stage.setScene(scene);
        stage.setOnCloseRequest((WindowEvent t) -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
