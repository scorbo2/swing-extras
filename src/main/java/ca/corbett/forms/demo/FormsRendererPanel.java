package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.forms.Alignment;
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

public class FormsRendererPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: custom renderers";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);
        LabelField headerLabel = LabelField.createBoldHeaderLabel("Lists and combos can have custom renderers!", 20, 0,
                                                                  8);
        headerLabel.getMargins().setBottom(24);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(headerLabel);

        String sb = "<html>Some fields, like ListField and ComboField, support<br/>" +
                "custom cell renderers, so you can get creative with item display!</html>";
        LabelField introLabel = LabelField.createPlainHeaderLabel(sb, 14);
        introLabel.getMargins().setBottom(18);
        formPanel.add(introLabel);

        List<String> items = List.of("Item 1", "Item 2", "Item 3", "Item 4",
                                     "Item 5", "Item 6", "Item 7", "Item 8",
                                     "Item 9");

        ListField<String> listField = new ListField<>("List:", items);
        listField.setFixedCellWidth(80);
        listField.setCellRenderer(new Renderer(90,25));
        listField.setVisibleRowCount(4);
        listField.getMargins().setBottom(12);
        formPanel.add(listField);

        ComboField<String> comboField = new ComboField<>("Combo box:", items, 0);
        comboField.setCellRenderer(new Renderer(140, 25));
        formPanel.add(comboField);

        return formPanel;
    }

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
            ImagePanel iPanel = isSelected ? selectedCells.get(value) : unselectedCells.get(value);
            if (iPanel == null) {
                iPanel = createImagePanel(value, isSelected);
            }
            return iPanel;
        }

        private ImagePanel createImagePanel(String value, boolean isSelected) {
            ImagePanel panel = new ImagePanel(ImagePanelConfig.createSimpleReadOnlyProperties());
            panel.stretchImage();

            Gradient gradient = new Gradient(GradientType.HORIZONTAL_STRIPE,
                                             isSelected ? Color.GREEN : Color.BLACK,
                                             isSelected ? Color.BLUE : Color.GREEN);

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

            if (isSelected) {
                selectedCells.put(value, panel);
            }
            else {
                unselectedCells.put(value, panel);
            }

            panel.setPreferredSize(new Dimension(cellWidth, cellHeight));

            return panel;
        }
    }
}
