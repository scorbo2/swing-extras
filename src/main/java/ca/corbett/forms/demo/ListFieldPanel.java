package ca.corbett.forms.demo;

import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.SnippetAction;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.ImageListField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.ListSubsetField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A demo panel to show off the List and Panel related FormFields.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ListFieldPanel extends PanelBuilder {
    private static final Logger log = Logger.getLogger(ListFieldPanel.class.getName());

    @Override
    public String getTitle() {
        return "Forms: lists and panels";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("Lists and panels");

        // Let's start with a CollapsiblePanelField, which can be expanded or contracted
        // to show its grouped contents:
        formPanel.add(LabelField.createBoldHeaderLabel("Panel fields"));
        CollapsiblePanelField panelField = new CollapsiblePanelField(
                "Panel fields are great for grouping components together!",
                true,
                new BorderLayout());
        panelField.setShouldExpandHorizontally(true); // fill the width of the form panel
        FormPanel miniFormPanel = new FormPanel(Alignment.TOP_LEFT); // Yes, form panels can contain other form panels!
        miniFormPanel.getBorderMargin().setLeft(24);
        miniFormPanel.add(new CheckBoxField("Example field 1", true));
        miniFormPanel.add(new CheckBoxField("Example field 2", true));
        miniFormPanel.add(new ComboField<>("Select:",
                                           List.of("These fields belong together",
                                                   "You can collapse this panel!",
                                                   "That hides these grouped fields."),
                                           0));
        panelField.getPanel().add(miniFormPanel, BorderLayout.CENTER);
        formPanel.add(panelField);

        // Let's also show that CollapsiblePanelFields can be started in their collapsed state.
        // This is handy for optional extra instructions that you don't want getting in the
        // way unless the user needs to expand and read them, or for optional fields that
        // are only rarely needed on a given form.
        panelField = new CollapsiblePanelField("Panels can be collapsed by default!",
                                               false,
                                               new FlowLayout(FlowLayout.LEFT));
        panelField.setShouldExpandHorizontally(true);
        panelField.getPanel().add(new JLabel("    Told you so!"));
        panelField.getMargins().setBottom(18);
        formPanel.add(panelField);

        // Let's also show off the regular PanelField - this component is deceptively powerful.
        // It basically just adds a blank panel to a form, and lets you add whatever
        // custom controls in whatever custom layout you want. So, if you need a custom
        // form component, but don't want to write your own FormField implementation for it,
        // you can always just throw together a PanelField for it instead:
        formPanel.add(new LabelField("You can also use PanelField to create custom field layouts:"));
        PanelField panelField1 = new PanelField(new BorderLayout());
        panelField1.getPanel().add(buildExamplePanel());
        formPanel.add(panelField1);
        formPanel.add(createSnippetLabel(new PanelFieldSnippetAction(), 18));

        // Now let's move on to list fields, which are often handier than a combobox:
        formPanel.add(LabelField.createBoldHeaderLabel("List fields"));
        ListField<String> listField1 = new ListField<>("Simple list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField1.setFixedCellWidth(80);
        listField1.setVisibleRowCount(4);
        formPanel.add(listField1);

        ListField<String> listField2 = new ListField<>("Wide list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField2.setLayoutOrientation(JList.VERTICAL_WRAP);
        listField2.setFixedCellWidth(80);
        listField2.setVisibleRowCount(3);
        formPanel.add(listField2);

        // New in swing-extras 2.6, let's show the ListSubsetField:
        formPanel.add(new ListSubsetField<>("List subset:",
                                            List.of("Apple", "Banana", "Date", "Elderberry",
                                                    "Grape", "Honeydew", "Kiwi"),
                                            List.of("Cherry", "Fig", "Lemon")).setFixedCellWidth(120));

        // And a new addition in swing-extras 2.5, let's add an ImageListField:
        ImageListField imageListField = new ImageListField("Image list:", 5, 75);
        try {
            imageListField.addImage(loadImage("media-playback-start.png"));
            imageListField.addImage(loadImage("media-playback-pause.png"));
            imageListField.addImage(loadImage("media-playback-stop.png"));
            imageListField.addImage(loadImage("media-record.png"));
            imageListField.addImage(loadImage("icon-copy.png"));
            imageListField.addImage(loadImage("icon-cut.png"));
            imageListField.addImage(loadImage("icon-paste.png"));
            imageListField.addImage(loadImage("swing-extras-icon.jpg"));
        }
        catch (IOException | IllegalArgumentException ioe) {
            log.log(Level.SEVERE, "Problem loading image resources: " + ioe.getMessage(), ioe);
        }

        // We can add a help icon to this field as its usage may not be intuitive at first glance:
        imageListField.setHelpText("<html><b>USAGE:</b><br>Try double-clicking the images in the image list!"
                                           + "<br>Click and drag left/right to scroll the list!"
                                           + "<br>You can drag and drop images from your file system onto the list!</html>");
        imageListField.setShouldExpand(true); // fill the width of the form panel
        imageListField.getImageListPanel().setOwnerWindow(DemoApp.getInstance()); // for ownership of the preview popup
        formPanel.add(imageListField);
        formPanel.add(createSnippetLabel(new ListFieldSnippetAction()));

        return formPanel;
    }

    /**
     * Invoked internally to try to load an image file from resources.
     */
    private BufferedImage loadImage(String resourceName) throws IOException {
        return ImageUtil.loadImage(getClass().getResource("/swing-extras/images/" + resourceName));
    }

    /**
     * A very simple action handler for our dummy buttons:
     */
    private void dummyButtonHandler() {
        JOptionPane.showMessageDialog(DemoApp.getInstance(), "Just an example... these buttons do nothing.");
    }

    /**
     * Invoked internally to lay out some custom form component to render inside a PanelField.
     */
    private JPanel buildExamplePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton leftButton = new JButton("Left");
        leftButton.addActionListener(e -> dummyButtonHandler());
        panel.add(leftButton, BorderLayout.WEST);
        JButton rightButton = new JButton("Right");
        rightButton.addActionListener(e -> dummyButtonHandler());
        panel.add(rightButton, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        for (int i = 1; i < 5; i++) {
            JButton button = new JButton("Center " + i);
            button.setMaximumSize(new Dimension(220, 25));
            button.addActionListener(e -> dummyButtonHandler());
            centerPanel.add(button);

            // Add a big one right in the middle:
            if (i == 2) {
                button = new JButton("This is all one big form field!");
                button.setMaximumSize(new Dimension(220, 25));
                button.addActionListener(e -> dummyButtonHandler());
                centerPanel.add(button);
            }
        }
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Shows a code snippet for creating PanelFields and CollapsiblePanelFields.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class PanelFieldSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Create a blank CollapsiblePanelField:
                    panelField = new CollapsiblePanelField("Panels can be collapsed by default!",
                                                           false,
                                                           new FlowLayout(FlowLayout.LEFT));
                    
                    // Tell it to fill the width of the FormField (this is optional):
                    panelField.setShouldExpandHorizontally(true);
                    
                    // We can add whatever components we want to this panel...
                    // We could even add another FormPanel if we wish!
                    panelField.getPanel().add(new JLabel("    Told you so!"));
                    
                    // PanelField is a similar option that allows us to embed
                    // whatever custom content with whatever layout we choose:
                    PanelField panelField1 = new PanelField(new BorderLayout());
                    panelField1.getPanel().add(buildExamplePanel()); // This can be anything
                    """;
        }
    }

    /**
     * Shows a code snippet for creating ListFields.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class ListFieldSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Create a simple ListField with a default vertical list:
                    List<String> options = List.of("One","Two","Three","Four","Five","Six");
                    ListField<String> listField1 = new ListField<>("Simple list:", options);
                    listField1.setFixedCellWidth(80); // We can optionally control the width of each list cell
                    listField1.setVisibleRowCount(4); // And also how many rows are displayed
                    
                    // Wide lists are also an option (using VERTICAL_WRAP):
                    ListField<String> listField2 = new ListField<>("Wide list:", options);
                    listField2.setLayoutOrientation(JList.VERTICAL_WRAP);
                    listField2.setFixedCellWidth(80);
                    listField2.setVisibleRowCount(3);
                    
                    // And a new addition in swing-extras 2.5, let's add an ImageListField:
                    ImageListField imageListField = new ImageListField("Image list:", 5, 75);
                    try {
                        // You can programmatically add images, like this, or you
                        // can start with a blank ImageListField and let the user
                        // drag images onto it.
                        imageListField.addImage(loadImage("media-playback-start.png"));
                        imageListField.addImage(loadImage("media-playback-pause.png"));
                        imageListField.addImage(loadImage("media-playback-stop.png"));
                        imageListField.addImage(loadImage("media-record.png"));
                        imageListField.addImage(loadImage("icon-copy.png"));
                        imageListField.addImage(loadImage("icon-cut.png"));
                        imageListField.addImage(loadImage("icon-paste.png"));
                        imageListField.addImage(loadImage("swing-extras-icon.jpg"));
                    }
                    catch (IOException | IllegalArgumentException ioe) {
                        log.log(Level.SEVERE, "Problem loading image resources: " + ioe.getMessage(), ioe);
                    }
                    
                    // Tell it to fill the width of the form:
                    imageListField.setShouldExpand(true);
                    
                    // We can optionally disable image add/remove:
                    imageListField.setEnabled(false);
                    
                    // And we can put an upper limit on image count, if we want:
                    imageListField.setMaxImageCount(10);
                    """;
        }
    }
}
