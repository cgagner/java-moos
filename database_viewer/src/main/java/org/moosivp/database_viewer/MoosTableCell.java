package org.moosivp.database_viewer;

import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * TableCell for displaying {@link MoosData} objects. The main reason for 
 * creating a custom table cell is to add the animation that sets the text fill
 * to red when a value changes and fades it to black.
 * 
 * This class could get modified to add right-click menus.
 * 
 * @author cgagner
 */
public class MoosTableCell<T> extends TableCell<MoosData, Object> {

    /**
     * Timeline for controlling the text fill animation.
     */
    protected Timeline timeline = new Timeline();
    /**
     * Tooltip for the current item.
     */
    protected Tooltip tooltip = new Tooltip();
    /**
     * Store the index of the current item because cells are request when
     * scrolling. This index is used to see if a new object is being placed in
     * the cell, or the value of the cell changed.
     */
    protected int index = -1;

    public MoosTableCell() {
        super();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(textFillProperty(), Color.RED)),
                new KeyFrame(Duration.millis(400), new KeyValue(textFillProperty(), Color.RED)),
                new KeyFrame(Duration.millis(500), new KeyValue(textFillProperty(), Color.BLACK))
        );
        tooltip.textProperty().bind(textProperty());
        tooltip.setMaxWidth(1000);
        tooltip.setWrapText(true);
        
        setTooltip(tooltip);
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            if (!Objects.equals(item.toString(), getText()) && index == getIndex()) {
                // Restart the animation if the value has changed.
                timeline.playFromStart();
            }
            setText(item.toString());
            index = getIndex();
        }
    }

}
