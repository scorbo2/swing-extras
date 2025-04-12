package ca.corbett.forms;

import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a dialog that can be shown to the user to allow
 * selection of a font with style properties and optional
 * foreground/background color selection.
 *
 * @author scorbo2
 * @since 2025-04-07
 */
public final class FontDialog extends JDialog {

    public static final Font INITIAL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private DefaultListModel<String> fontListModel;
    private JList<String> fontList;
    private boolean wasOkayed;

    private ComboField typeField;
    private ComboField styleField;
    private NumberField sizeField;
    private boolean showSizeField = true;
    private LabelField sampleLabel;
    private ColorField textColorField;
    private ColorField bgColorField;
    private Font selectedFont;

    /**
     * Creates a new FontDialog with a default initial font and with foreground/background
     * color selection disabled.
     *
     * @param owner The owning component (for dialog positioning purposes).
     */
    public FontDialog(Component owner) {
        this(owner, null, null, null);
    }

    /**
     * Creates a new FontDialog with the given initial font and with foreground/background
     * color selection disabled.
     *
     * @param owner The owning component (for dialog positioning purposes).
     */
    public FontDialog(Component owner, Font initialFont) {
        this(owner, initialFont, null, null);
    }

    /**
     * Creates a new FontDialog with the given initial font and with foreground
     * color selection enabled, but background color selection disabled.
     *
     * @param owner The owning component (for dialog positioning purposes).
     */
    public FontDialog(Component owner, Font initialFont, Color textColor) {
        this(owner, initialFont, textColor, null);
    }

    /**
     * Creates a new FontDialog with the given initial font and with foreground
     * and background color selection enabled.
     *
     * @param owner The owning component (for dialog positioning purposes).
     */
    public FontDialog(Component owner, Font initialFont, Color textColor, Color bgColor) {
        super((getParentObject(owner) instanceof Window) ? (Window)getParentObject(owner) : (Frame)null, "Choose font");
        initComponents(initialFont == null ? INITIAL_FONT : initialFont, textColor, bgColor);
        setLocationRelativeTo((Component)getParentObject(owner));
    }

    /**
     * Reports whether the size field is visible on this dialog.
     * The size field is optional and the visibility can be set
     * before the dialog is shown, with setSizeFieldVisible().
     */
    public boolean isShowSizeField() {
        return showSizeField;
    }

    /**
     * Toggles the visibility of the size chooser - this must be
     * set before the dialog is shown. Some use cases require the
     * setting of just the font face and style, without size.
     */
    public void setShowSizeField(boolean show) {
        showSizeField = show;
        sizeField.setVisible(show);
    }

    /**
     * Reports whether the dialog was okayed by the user or not.
     *
     * @return True if the user hit OK, false if the dialog was closed any other way.
     */
    public boolean wasOkayed() {
        return wasOkayed;
    }

    /**
     * Returns the Font that was selected by the user, with style and size included.
     *
     * @return A Font object.
     */
    public Font getSelectedFont() {
        return selectedFont;
    }

    /**
     * If foreground color editing was enabled in this dialog, will return the text
     * foreground color that was selected by the user, otherwise null.
     *
     * @return A Color for text foreground, or null if foreground color editing disabled.
     */
    public Color getSelectedTextColor() {
        return textColorField == null ? null : textColorField.getColor();
    }

    /**
     * If background color editing was enabled in this dialog, will return the text
     * background color that was selected by the user, otherwise null.
     *
     * @return A Color for text background, or null if background color editing disabled.
     */
    public Color getSelectedBgColor() {
        return bgColorField == null ? null : bgColorField.getColor();
    }

    /**
     * Invoked internally when any of the font style properties have changed, so that we
     * can update the sample label with the new settings.
     */
    private void fontChanged() {
        String fontFamily = fontList.getSelectedValue() == null ? selectedFont.getFamily() : fontList.getSelectedValue();
        int size = (Integer)sizeField.getCurrentValue();
        int style;
        switch (styleField.getSelectedIndex()) {
            case 1:
                style = Font.BOLD;
                break;
            case 2:
                style = Font.ITALIC;
                break;
            case 3:
                style = Font.BOLD + Font.ITALIC;
                break;
            default:
                style = Font.PLAIN;
        }
        selectedFont = new Font(fontFamily, style, size);

        String labelText = fontFamily.length() > 11 ? fontFamily.substring(0, 11) + "..." : fontFamily;
        sampleLabel.setText(labelText);
        sampleLabel.setFont(new Font(fontFamily, style, 14));
    }

    /**
     * Invoked internally when the font selection type is changed, so we can reload the list with
     * the correct options.
     * <p>There are two scenarios here:</p>
     * <ol>
     *     <li>You switch from "built-in fonts" to "system fonts": in this case, whatever font family
     *     you had selected is guaranteed to exist in the new list, so we select it and scroll to it.</li>
     *     <li>You switch from "system fonts" to "built-in fonts": in this case, the font family that
     *     you had selected is not guaranteed to exist in the new list. We will search for it and
     *     select it if it exists. If the font you had selected is not present in the new list,
     *     we will select the first option in the new list.</li>
     * </ol>
     */
    private void fontTypeChanged() {
        fontListModel.clear();
        switch (typeField.getSelectedIndex()) {
            case 0:
                fontListModel.addAll(
                        List.of(Font.SERIF, Font.SANS_SERIF, Font.MONOSPACED, Font.DIALOG, Font.DIALOG_INPUT));

                // Not guaranteed that the selected font will exist in this list:
                if (!isBuiltInFont(selectedFont)) {
                    fontList.setSelectedIndex(0);
                    fontChanged();
                }

                // Now we can select it:
                fontList.setSelectedValue(selectedFont.getFamily(), true);
                break;
            case 1:
                fontListModel.addAll(
                        Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

                // Guaranteed that the selected font will exist in this list, so select it:
                fontList.setSelectedValue(selectedFont.getFamily(), true);
                break;
        }
    }

    /**
     * Invoked internally when any color property changes, so we can update the sample label.
     */
    private void colorChanged() {
        if (textColorField != null) {
            sampleLabel.setColor(textColorField.getColor());
        }
        if (bgColorField != null) {
            sampleLabel.getFieldComponent().setBackground(bgColorField.getColor());
        }
    }

    /**
     * Invoked internally to examine the supplied Font, break it into family, size, and style,
     * and set our UI components as needed. The given font will then become the selected font.
     *
     * @param font Any Font object.
     */
    private void populateUIFromFont(Font font) {
        selectedFont = font;
        typeField.setSelectedIndex(isBuiltInFont(selectedFont) ? 0 : 1);
        fontList.setSelectedValue(font.getFamily(), true);
        sizeField.setCurrentValue(font.getSize());
        switch (font.getStyle()) {
            case Font.PLAIN:
                styleField.setSelectedIndex(0);
                break;
            case Font.BOLD:
                styleField.setSelectedIndex(1);
                break;
            case Font.ITALIC:
                styleField.setSelectedIndex(2);
                break;
            default:
                styleField.setSelectedIndex(3);
        }
    }

    /**
     * Invoked internally to create all UI components and lay out the dialog.
     *
     * @param initialFont The font to initially select.
     * @param textColor   If not null, will create a text color chooser and set it to this value.
     * @param bgColor     If not null, will create a bg color chooser and set it to this value.
     */
    private void initComponents(Font initialFont, Color textColor, Color bgColor) {
        selectedFont = initialFont;
        setModalityType(ModalityType.APPLICATION_MODAL);
        int startingHeight = 380;
        if (textColor != null) {
            startingHeight += 22;
        }
        if (bgColor != null) {
            startingHeight += 22;
        }
        setSize(new Dimension(280, startingHeight));
        setResizable(false);
        setLayout(new BorderLayout());
        add(buildFontChooserPanel(textColor, bgColor), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        // Initial setting:
        populateUIFromFont(selectedFont);
        fontTypeChanged();
    }

    /**
     * Invoked internally to create the font chooser panel and all UI components for it.
     *
     * @param textColor If not null, will create a text color chooser and set it to this value.
     * @param bgColor   If not null, will create a bg color chooser and set it to this value.
     * @return A JPanel
     */
    private JPanel buildFontChooserPanel(Color textColor, Color bgColor) {
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);

        AbstractAction changeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fontChanged();
            }
        };

        List<String> options = new ArrayList<>();
        options.add("Java built-in fonts");
        options.add("System installed fonts");
        typeField = new ComboField("Type:", options, 0, false);
        typeField.setTopMargin(16);
        typeField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fontTypeChanged();
            }
        });
        formPanel.addFormField(typeField);

        PanelField panelField = new PanelField();
        JPanel panel = panelField.getPanel();
        panel.setLayout(new BorderLayout());
        fontListModel = new DefaultListModel<>();
        fontList = new JList<>(fontListModel);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontList.addListSelectionListener(e -> fontChanged());
        JScrollPane scrollPane = new JScrollPane(fontList);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        panel.add(scrollPane, BorderLayout.CENTER);
        formPanel.addFormField(panelField);

        options = new ArrayList<>();
        options.add("Plain");
        options.add("Bold");
        options.add("Italic");
        options.add("Bold+Italic");
        styleField = new ComboField("Style:", options, 0, false);
        styleField.addValueChangedAction(changeAction);
        formPanel.addFormField(styleField);

        sizeField = new NumberField("Size:", 12, 6, 300, 2);
        sizeField.addValueChangedAction(changeAction);
        formPanel.addFormField(sizeField);

        sampleLabel = new LabelField("Sample:", "Sample text");
        sampleLabel.setFont(INITIAL_FONT.deriveFont(16f));
        formPanel.addFormField(sampleLabel);

        if (textColor != null) {
            textColorField = new ColorField("Text color:", textColor);
            textColorField.addValueChangedAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    colorChanged();
                }
            });
            sampleLabel.setColor(textColor);
            formPanel.addFormField(textColorField);
        }

        if (bgColor != null) {
            bgColorField = new ColorField("Background:", bgColor);
            bgColorField.addValueChangedAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    colorChanged();
                }
            });
            ((JLabel)sampleLabel.getFieldComponent()).setOpaque(true);
            ((JLabel)sampleLabel.getFieldComponent()).setBackground(bgColor);
            ((JLabel)sampleLabel.getFieldComponent()).setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            formPanel.addFormField(bgColorField);
        }

        formPanel.render();
        return formPanel;
    }

    /**
     * Invoked internally to close the dialog and record whether or not OK was clicked.
     *
     * @param okayed Are we closing because OK was clicked?
     */
    private void closeDialog(boolean okayed) {
        wasOkayed = okayed;
        setVisible(false);
    }

    /**
     * Invoked internally to build the OK/Cancel button panel for the bottom of the dialog.
     *
     * @return A button panel.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buildButton("OK", e -> closeDialog(true)));
        panel.add(buildButton("Cancel", e -> closeDialog(false)));
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        return panel;
    }

    /**
     * Invoked internally to create a JButton with the given label and a consistent size.
     *
     * @param label  The label for the button
     * @param action The Action to invoke when the button is clicked.
     * @return A JButton.
     */
    private JButton buildButton(String label, ActionListener action) {
        JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(action);
        return button;
    }

    /**
     * The JRE guarantees us that certain font families will exist, no matter what kind of system
     * we're running on. These are the "safe" fonts that we can always count on, as opposed to the
     * list of system-installed fonts, which can vary wildly from system to system.
     *
     * @param font Any candidate Font object.
     * @return True if the given font is a java built-in font.
     */
    private boolean isBuiltInFont(Font font) {
        return (font.getFamily().equals(Font.DIALOG) ||
                font.getFamily().equals(Font.SANS_SERIF) ||
                font.getFamily().equals(Font.SERIF) ||
                font.getFamily().equals(Font.DIALOG_INPUT) ||
                font.getFamily().equals(Font.MONOSPACED));
    }

    /**
     * Given any Component, try to find a Frame or a Window in its hierarchy.
     * This is a little ugly, but the FormField won't be reasonably able to supply
     * us with the containing Frame, which we need to position this dialog on startup.
     * Without this, our dialog might appear quite far away from the owning
     * frame, which is ugly.
     *
     * @param component Any Component
     * @return A Frame or Window that contains that component. WARNING: might be null if we can't find one.
     */
    private static Object getParentObject(Component component) {
        if (component instanceof Window) {
            return component;
        }
        return SwingUtilities.getAncestorOfClass(Window.class, component);
    }
}
