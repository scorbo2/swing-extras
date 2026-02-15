package ca.corbett.extras.properties;

import ca.corbett.forms.Margins;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.MarginsField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An AbstractProperty to wrap a Margins instance.
 * Will generate a MarginsField to edit the value of this property.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class MarginsProperty extends AbstractProperty {

    private static final Logger log = Logger.getLogger(MarginsProperty.class.getName());
    private Margins margins;
    private String headerLabel;

    public MarginsProperty(String name, String label) {
        this(name, label, null);
    }

    public MarginsProperty(String name, String label, Margins initialValue) {
        super(name, label);
        this.margins = initialValue == null ? new Margins() : new Margins(initialValue);
        this.headerLabel = null;
    }

    public Margins getMargins() {
        return margins;
    }

    public void setMargins(Margins margins) {
        this.margins = margins == null ? new Margins() : new Margins(margins);
    }

    public String getHeaderLabel() {
        return headerLabel;
    }

    public void setHeaderLabel(String headerLabel) {
        this.headerLabel = headerLabel;
    }

    @Override
    public void saveToProps(Properties props) {
        // Debatable maybe, but if we were given null, default to an empty Margins instance:
        props.setInteger(fullyQualifiedName + ".left", margins.getLeft());
        props.setInteger(fullyQualifiedName + ".top", margins.getTop());
        props.setInteger(fullyQualifiedName + ".right", margins.getRight());
        props.setInteger(fullyQualifiedName + ".bottom", margins.getBottom());
        props.setInteger(fullyQualifiedName + ".internalSpacing", margins.getInternalSpacing());
        props.setString(fullyQualifiedName + ".headerLabel", headerLabel == null ? "" : headerLabel);
    }

    @Override
    public void loadFromProps(Properties props) {
        margins.setLeft(props.getInteger(fullyQualifiedName + ".left", margins.getLeft()));
        margins.setTop(props.getInteger(fullyQualifiedName + ".top", margins.getTop()));
        margins.setRight(props.getInteger(fullyQualifiedName + ".right", margins.getRight()));
        margins.setBottom(props.getInteger(fullyQualifiedName + ".bottom", margins.getBottom()));
        margins.setInternalSpacing(props.getInteger(fullyQualifiedName + ".internalSpacing",
                                                    margins.getInternalSpacing()));
        String loadedText = props.getString(fullyQualifiedName + ".headerLabel", headerLabel);
        if (loadedText == null || loadedText.isBlank()) {
            headerLabel = null;
        }
        else {
            headerLabel = loadedText;
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        MarginsField marginsField = new MarginsField(propertyLabel, margins);
        marginsField.setHeaderLabel(headerLabel);
        return marginsField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof MarginsField marginField)) {
            log.log(Level.SEVERE, "MarginsProperty.loadFromFormField: received the wrong field \"{0}\"",
                    field.getIdentifier());
            return;
        }

        if (!field.isValid()) {
            log.log(Level.WARNING, "MarginsProperty.loadFromFormField: received an invalid field \"{0}\"",
                    field.getIdentifier());
            return;
        }

        // DON'T call getMargins()! That's the literal margins on the form field, not what we want.
        setMargins(marginField.getMarginsObject());
    }
}
