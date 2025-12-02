package ca.corbett.forms.demo;

import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ListField;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some FormFields support custom cell renderers, which can allow you to highly customize
 * those fields - this demo panel shows an example of this in action.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class FormsRendererPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: custom renderers";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("Custom cell renderers");

        String sb = "<html>Some fields, like ListField and ComboField, support<br/>"
                + "custom cell renderers, so you can get creative with item display!</html>";
        LabelField introLabel = LabelField.createPlainHeaderLabel(sb, 14);
        introLabel.getMargins().setBottom(18);
        formPanel.add(introLabel);

        // We'll use this list of dummy items in our ListField and ComboField:
        List<String> items = List.of("Item 1", "Item 2", "Item 3", "Item 4",
                                     "Item 5", "Item 6", "Item 7", "Item 8",
                                     "Item 9");

        ListField<String> listField = new ListField<>("List:", items);
        listField.setFixedCellWidth(80);
        listField.setCellRenderer(new Renderer(90,25));
        listField.setVisibleRowCount(4);
        listField.getMargins().setBottom(12);
        formPanel.add(listField);

        formPanel.add(new ComboField<>("Combo box:", items, 0)
                              .setCellRenderer(new Renderer(140, 25)));

        // Sarcasm is the lowest form of wit, or so I'm told.
        LabelField label = new LabelField("Isn't it beautiful? :)");
        label.getMargins().setTop(16);
        formPanel.add(label);

        return formPanel;
    }

    /**
     * In case it wasn't obvious, this is NOT intended as a serious ListCellRenderer implementation.
     * I know it's very ugly. The point is that you are only limited by your own imagination
     * (and graphic design skills, of course) when implementing your custom renderers. Have fun!
     */
    private static class Renderer implements ListCellRenderer<String> {

        private final Map<String, ImagePanel> unselectedCells = new HashMap<>();
        private final Map<String, ImagePanel> selectedCells = new HashMap<>();

        private final int cellWidth;
        private final int cellHeight;

        public Renderer(int width, int height) {
            cellWidth = width;
            cellHeight = height;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            // If we've already generated an image for this one, use it, otherwise we'll create one:
            ImagePanel iPanel = isSelected ? selectedCells.get(value) : unselectedCells.get(value);
            if (iPanel == null) {
                iPanel = createImagePanel(value, isSelected);
            }
            return iPanel;
        }

        private ImagePanel createImagePanel(String value, boolean isSelected) {
            ImagePanel panel = new ImagePanel(ImagePanelConfig.createSimpleReadOnlyProperties());
            panel.stretchImage();

            // Yeah, it's godawful, but let's go with it:
            Gradient gradient = new Gradient(GradientType.HORIZONTAL_STRIPE,
                                             isSelected ? Color.GREEN : Color.BLACK,
                                             isSelected ? Color.BLUE : Color.GREEN);

            // We'll cheat a little and use LogoGenerator to draw the panel image for us:
            LogoProperty config = new LogoProperty(value);
            config.setLogoWidth(cellWidth);
            config.setLogoHeight(cellHeight);
            config.setBgColorType(LogoProperty.ColorType.GRADIENT);
            config.setBgGradient(gradient);
            config.setAutoSize(true);
            config.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            config.setTextColor(isSelected ? Color.YELLOW : Color.WHITE);
            config.setBorderWidth(0);
            panel.setImage(LogoGenerator.generateImage(value, config));

            // Add it to the appropriate lookup map:
            if (isSelected) {
                selectedCells.put(value, panel);
            }
            else {
                unselectedCells.put(value, panel);
            }

            // Keep it to our preferred cell size:
            panel.setPreferredSize(new Dimension(cellWidth, cellHeight));

            return panel;
        }
    }
}
