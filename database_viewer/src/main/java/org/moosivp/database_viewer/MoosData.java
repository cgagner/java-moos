package org.moosivp.database_viewer;

import com.github.moos_ivp.moosbeans.MOOSMsg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * MOOS data to display in the table. This data is essentially the same as a
 * MOOS messages. However, JavaFX properties are added so observers can see when
 * the data has changed. This allows the table to dynamically update without
 * needing to put a refresh timer on the table.
 *
 * @author cgagner
 */
public class MoosData {

    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty time = new SimpleDoubleProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty source = new SimpleStringProperty();
    private final StringProperty community = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();

    public MoosData(String name, double time, String type, String source, String community, String value) {
        setName(name);
        setTime(time);
        setType(type);
        setSource(source);
        setCommunity(community);
        setValue(value);
    }

    public MoosData(MOOSMsg data) {
        update(data);
    }

    /**
     * Update the data with the specified {@link MOOSMsg}.
     *
     * @param data {@link MOOSMsg}
     */
    public final void update(MOOSMsg data) {
        setName(data.getKey());
        setTime(data.getTime());
        setType("");
        setSource(data.getSource());
        setCommunity(data.getCommunity());
        setValue(data.isDouble() ? Double.toString(data.getDoubleData()) : data.getStringData());
    }

    /**
     * Name property of the data.
     *
     * @return Name property of the data.
     */
    public final StringProperty nameProperty() {
        return name;
    }

    /**
     * Name of the data.
     *
     * @return Name of the data.
     */
    public final String getName() {
        return nameProperty().get();
    }

    /**
     * Set the name of the data.
     *
     * @param name Name of the data.
     */
    public final void setName(String name) {
        nameProperty().set(name);
    }

    /**
     * Property for the time the message was received.
     *
     * @return Property for the time the message was received.
     */
    public final DoubleProperty getTimeProperty() {
        return time;
    }

    /**
     * Time the message was received.
     *
     * @return Time the message was received. This value is in MOOS Time so it
     * has the warp value factored into it.
     */
    public final double getTime() {
        return getTimeProperty().get();
    }

    /**
     * Set the time the message was received.
     *
     * @param time Time the message was received. This value is in MOOS Time so
     * it has the warp value factored into it.
     */
    public final void setTime(double time) {
        getTimeProperty().set(time);
    }

    /**
     * Property for the type of message.
     * @return Property for the type of message.
     */
    public final StringProperty typeProperty() {
        return type;
    }

    /**
     * Type of the message.
     * @return Type of the message.
     */
    public final String getType() {
        return typeProperty().get();
    }

    /**
     * Set the type of the message.
     * @param type Type of the message.
     */
    public final void setType(String type) {
        typeProperty().set(type);
    }

    /**
     * Property for the source of the message.
     * @return Property for the source of the message.
     */
    public final StringProperty sourceProperty() {
        return source;
    }

    /**
     * Source of the message.
     * @return Source of the message.
     */
    public final String getSource() {
        return sourceProperty().get();
    }

    /**
     * Set the source of the message.
     * @param source Source of the message.
     */
    public final void setSource(String source) {
        sourceProperty().set(source);
    }

    /**
     * Property for the community of the message.
     * @return Property for the community of the message.
     */
    public final StringProperty communityProperty() {
        return community;
    }

    /**
     * Community of the message.
     * @return Community of the message.
     */
    public final String getCommunity() {
        return communityProperty().get();
    }

    /**
     * Set the community of the message.
     * @param community Community of the message.
     */
    public final void setCommunity(String community) {
        communityProperty().set(community);
    }

    /**
     * Property for the value stored in the message.
     * @return Property for the value stored in the message.
     */
    public final StringProperty valueProperty() {
        return value;
    }

    /**
     * Value stored in the message. All values are converted to strings for 
     * this application.
     * @return Value stored in the message.
     */
    public final String getValue() {
        return valueProperty().get();
    }

    /**
     * Set the value stored in the message. All values are converted to strings
     * for this application. 
     * @param value Value stored in the message.
     */
    public final void setValue(String value) {
        valueProperty().set(value);
    }

}
