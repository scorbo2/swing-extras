package ca.corbett.extras.config;

import ca.corbett.extras.properties.Properties;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for all ConfigPanel subclasses.
 *
 * @param <T> The type of model object to configure.
 * @author scorbo2
 * @since 2018-01-25
 */
public abstract class ConfigPanel<T> extends JPanel {

    protected T modelObject;
    protected boolean isModified;
    protected final List<ChangeListener> changeListeners;

    /**
     * Creates an empty ConfigPanel that is not bound to any model object.
     * Use setModelObject to associate this panel with a model object.
     * The specified title string is used to create a TitledBorder around this panel.
     *
     * @param title A title for the TitledBorder of this panel.
     */
    public ConfigPanel(String title) {
        changeListeners = new ArrayList<>();
        this.modelObject = null;
        isModified = false;
        setBorder(new TitledBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createBevelBorder(BevelBorder.RAISED)),
                                   title));
    }

    /**
     * Associates this ConfigPanel with the given model object. This will overwrite any
     * previous settings with the settings from the given object, and will clear the
     * isModified flag.
     *
     * @param obj The model object to associate this Config panel with.
     */
    public void setModelObject(T obj) {
        this.modelObject = obj;
        load(obj);
        isModified = false;
    }

    /**
     * Reports whether the model object has been modified by user input.
     *
     * @return Whether the user has made any changes to the model object.
     */
    public boolean isModified() {
        return isModified;
    }

    /**
     * Clears the isModified flag. Invoked automatically save() is invoked, but can also
     * be invoked by the caller to acknowledge user input.
     */
    public void clearIsModified() {
        isModified = false;
    }

    /**
     * Saves settings to the specified Properties instance, using the specified
     * parameter name prefix (may be null or empty). This will clear the isModified flag.
     *
     * @param props  A Properties instance to which to save all settings.
     * @param prefix An optional property name prefix to use with prop names.
     */
    public abstract void save(Properties props, String prefix);

    /**
     * Loads settings from the specified Properties instance, using the specified
     * parameter name prefix (may be null or empty). This will set all UI fields to reflect
     * whatever is loaded from the Properties instance, clear the isModified flag, and
     * will overwrite whatever settings were contained previously.
     *
     * @param props  A Properties instance from which to load all settings.
     * @param prefix An optional property name prefix to use with prop names.
     */
    public abstract void load(Properties props, String prefix);

    /**
     * Loads settings from the given model object. Clears the isModified flag.
     *
     * @param obj The model object from which to load.
     */
    public abstract void load(T obj);

    /**
     * Allows listeners to subscribe to this panel to receive notifications
     * when changes are made.
     *
     * @param listener The ChangeListener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Allows listeners to unsubscribe from change events.
     *
     * @param listener The ChangeListener to remove.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Can be invoked by a subclass to notify all listeners that something
     * in this panel has changed.
     */
    protected void notifyChangeListeners() {
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

}
