package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.SliderField;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SliderProperty extends AbstractProperty {

    private static final Logger log = Logger.getLogger(SliderProperty.class.getName());

    private int minValue;
    private int maxValue;
    private int currentValue;
    private final List<Color> colorStops;
    private final List<String> labels;
    private boolean showNumericValueInLabel;
    private boolean showValueLabel;

    public SliderProperty(String name, String label, int min, int max, int value) {
        super(name, label);
        this.minValue = min;
        this.maxValue = max;
        this.currentValue = value;
        colorStops = new ArrayList<>();
        labels = new ArrayList<>();
        showValueLabel = true;
    }

    public int getMinValue() {
        return minValue;
    }

    public SliderProperty setMinValue(int minValue) {
        this.minValue = minValue;
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public SliderProperty setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public int getValue() {
        return currentValue;
    }

    public SliderProperty setValue(int currentValue) {
        this.currentValue = currentValue;
        return this;
    }

    public boolean isShowValueLabel() {
        return showValueLabel;
    }

    public SliderProperty setShowValueLabel(boolean showValueLabel) {
        this.showValueLabel = showValueLabel;
        return this;
    }

    public List<Color> getColorStops() {
        return new ArrayList<>(colorStops);
    }

    public List<String> getLabels() {
        return new ArrayList<>(labels);
    }

    public boolean isAllowNumericValueInLabel() {
        return showNumericValueInLabel;
    }

    public SliderProperty setColorStops(List<Color> stops) {
        colorStops.clear();
        if (stops != null && ! stops.isEmpty()) {
            colorStops.addAll(stops);
        }
        return this;
    }

    public SliderProperty setLabels(List<String> labels, boolean alsoShowNumericValueInLabel) {
        this.labels.clear();
        if (labels != null && ! labels.isEmpty()) {
            this.labels.addAll(labels);
        }
        this.showNumericValueInLabel = alsoShowNumericValueInLabel;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setInteger(fullyQualifiedName + ".min", minValue);
        props.setInteger(fullyQualifiedName + ".max", maxValue);
        props.setInteger(fullyQualifiedName + ".value", currentValue);
        props.setBoolean(fullyQualifiedName + ".showNumericValueInLabel", showNumericValueInLabel);
        props.setBoolean(fullyQualifiedName + ".showValueLabel", showValueLabel);
        props.setString(fullyQualifiedName + ".colorStops", colorStops.stream()
                                                                      .map(Properties::encodeColor)
                                                                      .collect(Collectors.joining(",")));
        props.setString(fullyQualifiedName + ".labels", String.join(",", labels));
    }

    @Override
    public void loadFromProps(Properties props) {
        minValue = props.getInteger(fullyQualifiedName + ".min", minValue);
        maxValue = props.getInteger(fullyQualifiedName + ".max", maxValue);
        currentValue = props.getInteger(fullyQualifiedName + ".value", currentValue);
        showNumericValueInLabel = props.getBoolean(fullyQualifiedName + ".showNumericValueInLabel", showNumericValueInLabel);
        showValueLabel = props.getBoolean(fullyQualifiedName + ".showValueLabel", showValueLabel);

        // Parse out our color stops:
        String rawValue = props.getString(fullyQualifiedName + ".colorStops", colorStops.stream()
                                                                                        .map(Properties::encodeColor)
                                                                                        .collect(Collectors.joining(",")));
        String[] parsed = rawValue.split(",");
        colorStops.clear();
        for (String colorString : parsed) {
            if (colorString == null || colorString.isBlank()) {
                continue;
            }
            try {
                colorStops.add(Properties.decodeColor(colorString.trim()));
            }
            catch (NumberFormatException nfe) {
                log.warning("SliderProperty: properties file contained an invalid color value \""+colorString+"\"");
            }
        }

        // Parse out our labels:
        rawValue = props.getString(fullyQualifiedName + ".labels", String.join(",", labels));
        parsed = rawValue.split(",");
        labels.clear();
        for (String label : parsed) {
            if (label != null && !label.isBlank()) {
                labels.add(label.trim());
            }
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        SliderField field = new SliderField(this.propertyLabel, minValue, maxValue, currentValue);
        field.setShowValueLabel(showValueLabel);
        if (! colorStops.isEmpty()) {
            field.setColorStops(colorStops);
        }
        if (! labels.isEmpty()) {
            field.setLabels(labels, showNumericValueInLabel);
        }
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof SliderField)) {
            log.log(Level.SEVERE, "SliderProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        currentValue = ((SliderField)field).getValue();
    }
}
