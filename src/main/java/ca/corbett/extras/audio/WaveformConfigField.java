package ca.corbett.extras.audio;

import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.Margins;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A ConfigPanel instance that allows viewing and editing settings for a WaveformConfig object.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2018-01-25
 */
public final class WaveformConfigField extends FormField {

    private final JPanel wrapperPanel;
    private final FormPanel formPanel;
    private String labelText;
    private boolean useTitleBorder;
    private boolean shouldExpand = true;

    private ColorField bgColorField;
    private ColorField waveformColorField;
    private ColorField outlineColorField;
    private ColorField baselineColorField;

    private NumberField baselineWidthField;
    private NumberField outlineWidthField;

    private CheckBoxField enableBaselineField;
    private CheckBoxField enableOutlineField;

    private ComboField<String> compressionField;
    private ComboField<String> widthLimitField;

    public enum Compression {
        XXHIGH(8192, 768, "xx-high"),
        XHIGH(4096, 512, "x-high"),
        HIGH(2048, 256, "high"),
        NORMAL(1024, 128, "normal"),
        LOW(512, 64, "low"),
        XLOW(256, 32, "x-low"),
        XXLOW(128, 16, "xx-low");

        private final int xValue;
        private final int yValue;
        private final String label;

        Compression(int xValue, int yValue, String label) {
            this.xValue = xValue;
            this.yValue = yValue;
            this.label = label;
        }

        public int getXValue() {
            return xValue;
        }

        public int getYValue() {
            return yValue;
        }

        @Override
        public String toString() {
            return label;
        }

        public static List<String> getLabels() {
            return Arrays.stream(values())
                         .map(Enum::toString)
                         .collect(Collectors.toList());
        }

        public static Optional<Compression> fromLabel(String label) {
            return Arrays.stream(values())
                         .filter(e -> e.toString().equals(label))
                         .findFirst();
        }
    }

    public enum WidthLimit {
        NO_LIMIT(Integer.MAX_VALUE, "No limit"),
        XSMALL(300, "300px"),
        SMALL(500, "500px"),
        NORMAL(800, "800px"),
        LARGE(1200, "1200px"),
        XLARGE(1600, "1600px"),
        XXLARGE(2400, "2400px");

        private final int limit;
        private final String label;

        WidthLimit(int limit, String label) {
            this.limit = limit;
            this.label = label;
        }

        public int getLimit() {
            return limit;
        }

        @Override
        public String toString() {
            return label;
        }

        public static List<String> getLabels() {
            return Arrays.stream(values())
                         .map(Enum::toString)
                         .collect(Collectors.toList());
        }

        public static Optional<WidthLimit> fromLabel(String label) {
            return Arrays.stream(values())
                         .filter(e -> e.toString().equals(label))
                         .findFirst();
        }
    }

    public WaveformConfigField(String label) {
        labelText = label;
        wrapperPanel = new JPanel(new BorderLayout());
        formPanel = createFormPanel();
        wrapperPanel.add(formPanel, BorderLayout.CENTER);
        wrapperPanel.setBorder(BorderFactory.createTitledBorder(label));
        useTitleBorder = true;
        fieldComponent = wrapperPanel;
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    public WaveformConfigField setShouldExpand(boolean should) {
        shouldExpand = should;
        return this;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    /**
     * This field itself typically will not show a validation label, as we delegate field validation
     * to our embedded form fields. But, in keeping with the swing-forms general contract, you can
     * still assign FieldValidators to instances of this field if you wish.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    @Override
    public boolean validate() {
        // ask the parent to validate also, in case we have any FieldValidators assigned here
        return super.validate() && formPanel.isFormValid();
    }

    /**
     * Decides whether to use a titled border around this field component (the default), or
     * to use a traditional FormField field label instead.
     */
    public WaveformConfigField setUseTitleBorder(boolean use) {
        if (use == useTitleBorder) {
            return this; // ignore no-op requests
        }
        if (use) {
            wrapperPanel.setBorder(BorderFactory.createTitledBorder(labelText));
            fieldLabel.setText("");
        }
        else {
            wrapperPanel.setBorder(null);
            fieldLabel.setText(labelText);
        }
        useTitleBorder = use;
        return this;
    }

    public boolean isUseTitleBorder() {
        return useTitleBorder;
    }

    /**
     * Returns the text that is either showing in the field label OR in the
     * field's title border, depending on the value of useTitleBorder.
     */
    public String getFieldLabelText() {
        return labelText;
    }

    public WaveformConfigField setFieldLabelText(String text) {
        labelText = text;
        if (useTitleBorder && (wrapperPanel.getBorder() instanceof TitledBorder)) {
            ((TitledBorder)wrapperPanel.getBorder()).setTitle(labelText);
        }
        else if (!useTitleBorder) {
            fieldLabel.setText(labelText);
        }
        return this;
    }

    public WaveformConfigField setBgColor(Color col) {
        if (Objects.equals(getBgColor(), col)) {
            return this; // reject no-op calls
        }
        if (col == null) {
            return this; // reject null;
        }
        bgColorField.setColor(col);
        return this;
    }

    public Color getBgColor() {
        return bgColorField.getColor();
    }

    public WaveformConfigField setWaveformColor(Color col) {
        if (Objects.equals(getWaveformColor(), col)) {
            return this; // reject no-op calls
        }
        if (col == null) {
            return this; // reject null
        }
        waveformColorField.setColor(col);
        return this;
    }

    public Color getWaveformColor() {
        return waveformColorField.getColor();
    }

    public WaveformConfigField setOutlineColor(Color col) {
        if (Objects.equals(getOutlineColor(), col)) {
            return this; // reject no-op calls
        }
        if (col == null) {
            return this; // reject null
        }
        outlineColorField.setColor(col);
        return this;
    }

    public Color getOutlineColor() {
        return outlineColorField.getColor();
    }

    public WaveformConfigField setBaselineColor(Color col) {
        if (Objects.equals(getBaselineColor(), col)) {
            return this; // reject no-op calls
        }
        if (col == null) {
            return this; // reject null
        }
        baselineColorField.setColor(col);
        return this;
    }

    public Color getBaselineColor() {
        return baselineColorField.getColor();
    }

    public WaveformConfigField setBaselineWidth(int width) {
        if (getBaselineWidth() == width) {
            return this; // reject no-op requests
        }
        baselineWidthField.setCurrentValue(width);
        return this;
    }

    public int getBaselineWidth() {
        return (int)baselineWidthField.getCurrentValue();
    }

    public WaveformConfigField setOutlineWidth(int width) {
        if (getOutlineWidth() == width) {
            return this; // reject no-op requests
        }
        outlineWidthField.setCurrentValue(width);
        return this;
    }

    public int getOutlineWidth() {
        return (int)outlineWidthField.getCurrentValue();
    }

    public WaveformConfigField setEnableDrawBaseline(boolean enable) {
        if (isEnableDrawBaseline() == enable) {
            return this; // reject no-op requests
        }
        enableBaselineField.setChecked(enable);
        return this;
    }

    public boolean isEnableDrawBaseline() {
        return enableBaselineField.isChecked();
    }

    public WaveformConfigField setEnableDrawOutline(boolean enable) {
        if (isEnableDrawOutline() == enable) {
            return this; // reject no-op requests
        }
        enableOutlineField.setChecked(enable);
        return this;
    }

    public boolean isEnableDrawOutline() {
        return enableOutlineField.isChecked();
    }

    public WaveformConfigField setCompression(Compression compression) {
        if (getCompression() == compression) {
            return this; // reject no-op requests
        }
        compressionField.setSelectedItem(compression.toString());
        return this;
    }

    public Compression getCompression() {
        return Compression.fromLabel(compressionField.getSelectedItem()).orElse(Compression.NORMAL);
    }

    public WaveformConfigField setWidthLimit(WidthLimit limit) {
        if (getWidthLimit() == limit) {
            return this; // reject no-op requests
        }
        widthLimitField.setSelectedItem(limit.toString());
        return this;
    }

    public WidthLimit getWidthLimit() {
        return WidthLimit.fromLabel(widthLimitField.getSelectedItem()).orElse(WidthLimit.NORMAL);
    }

    private FormPanel createFormPanel() {
        FormPanel formPanel = new FormPanel();

        ValueChangedListener listener = field -> fireValueChangedEvent();
        Margins fieldMargins = new Margins(6, 2, 6, 2, 4);

        bgColorField = new ColorField("Background color:", ColorSelectionType.SOLID);
        bgColorField.addValueChangedListener(listener);
        bgColorField.setMargins(fieldMargins);
        formPanel.add(bgColorField);

        waveformColorField = new ColorField("Waveform color:", ColorSelectionType.SOLID);
        waveformColorField.addValueChangedListener(listener);
        waveformColorField.setMargins(fieldMargins);
        formPanel.add(waveformColorField);

        outlineColorField = new ColorField("Outline color:", ColorSelectionType.SOLID);
        outlineColorField.addValueChangedListener(listener);
        outlineColorField.setMargins(fieldMargins);
        formPanel.add(outlineColorField);

        baselineColorField = new ColorField("Baseline color:", ColorSelectionType.SOLID);
        baselineColorField.addValueChangedListener(listener);
        baselineColorField.setMargins(fieldMargins);
        formPanel.add(baselineColorField);

        baselineWidthField = new NumberField("Baseline width:", 1, 0, 10, 1);
        baselineWidthField.addValueChangedListener(listener);
        baselineWidthField.setMargins(fieldMargins);
        formPanel.add(baselineWidthField);

        outlineWidthField = new NumberField("Baseline width:", 1, 0, 10, 1);
        outlineWidthField.addValueChangedListener(listener);
        outlineWidthField.setMargins(fieldMargins);
        formPanel.add(outlineWidthField);

        enableBaselineField = new CheckBoxField("Draw zero baseline", true);
        enableBaselineField.addValueChangedListener(listener);
        enableBaselineField.setMargins(fieldMargins);
        formPanel.add(enableBaselineField);

        enableOutlineField = new CheckBoxField("Draw wave outline", true);
        enableOutlineField.addValueChangedListener(listener);
        enableOutlineField.setMargins(fieldMargins);
        formPanel.add(enableOutlineField);

        compressionField = new ComboField<>("Compression:", Compression.getLabels(), 4);
        compressionField.addValueChangedListener(listener);
        compressionField.setMargins(fieldMargins);
        formPanel.add(compressionField);

        widthLimitField = new ComboField<>("Width limit:", WidthLimit.getLabels(), 0);
        widthLimitField.addValueChangedListener(listener);
        widthLimitField.setMargins(fieldMargins);
        formPanel.add(widthLimitField);

        return formPanel;
    }

}
