package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.TextInputDialog;
import ca.corbett.extras.actionpanel.ActionPanel;
import ca.corbett.extras.actionpanel.BorderOptions;
import ca.corbett.extras.actionpanel.CardAction;
import ca.corbett.extras.actionpanel.ColorTheme;
import ca.corbett.extras.actionpanel.ExpandCollapseOptions;
import ca.corbett.extras.actionpanel.ExpandListener;
import ca.corbett.extras.actionpanel.ToolBarOptions;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.SwingFormsResources;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.validators.ValidationResult;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * To show off the new ActionPanel component.
 * New in swing-extras 2.8, the ActionPanel is a great way to group related
 * actions together into collapsible sections. This is great for presenting otherwise
 * long and complex navigation menus in a compact way.
 * There are many styling options! This demo panel shows off some of the things
 * you can do with ActionPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ActionPanelDemoPanel extends PanelBuilder implements ExpandListener {

    private static final Logger log = Logger.getLogger(ActionPanelDemoPanel.class.getName());

    // A nice example header background color:
    private static final Color STEEL_BLUE = new Color(70, 130, 180);

    // Sound effects, just for fun:
    private static final String EXPAND_CLIP = "/swing-extras/audio/swish.wav";
    private static final String COLLAPSE_CLIP = "/swing-extras/audio/splok.wav";

    // Our action names will double as lookups for their form panel contents:
    private static final String TAB_INTRO = "Intro to ActionPanel";
    private static final String TAB_COMPONENTS = "Buttons or labels?";
    private static final String TAB_TOOLBAR = "Toolbar options";
    private static final String TAB_COLORS = "Colors";
    private static final String TAB_FONTS = "Fonts";
    private static final String TAB_BORDERS = "Borders";
    private static final String TAB_ICONS = "Icons";
    private static final String TAB_SORTING = "Sorting";
    private static final String TAB_MARGINS = "Margins/Padding";
    private static final String TAB_EXPAND = "Expand/Collapse options";
    private static final String TAB_ANIMATION = "Animation options";

    // We'll have an ActionPanel on the left and a content panel on the right:
    private ActionPanel actionPanel;
    private final JPanel cardPanel = new JPanel(new CardLayout());

    // And we'll use a map of action names to form panels for showing content:
    private final Map<String, FormPanel> panelMap = new HashMap<>();

    private Clip expandClip;
    private Clip collapseClip;
    private ComboField<BorderOption> actionPanelBorderField;
    private ComboField<String> useLabelsField;
    private ComboField<ToolBarOptions.ButtonPosition> toolBarButtonPositionField;
    private NumberField toolBarIconSizeField;
    private CheckBoxField toolBarAllowAddField;
    private CheckBoxField toolBarAllowRenameField;
    private CheckBoxField toolBarAllowItemReorderField;
    private CheckBoxField toolBarAllowItemRemovalField;
    private CheckBoxField toolBarAllowRemoveField;
    private CheckBoxField sortGroupsByNameField;
    private CheckBoxField sortActionsByNameField;
    private CheckBoxField showGroupHeaderIconsField;
    private CheckBoxField showActionIconsField;
    private NumberField headerIconSizeField;
    private NumberField actionIconSizeField;
    private ComboField<BorderOption> groupBorderField;
    private ComboField<BorderOption> headerBorderField;
    private ComboField<BorderOption> actionTrayBorderField;
    private ComboField<BorderOption> toolBarBorderField;
    private NumberField externalPaddingField;
    private NumberField headerInternalPaddingField;
    private NumberField actionInternalPaddingField;
    private NumberField toolBarInternalPaddingField;
    private NumberField actionIndentField;
    private ComboField<String> animationField;
    private ComboField<String> colorSourceField;
    private ComboField<String> fontSourceField;
    private ButtonField setFromThemeField;
    private ColorField actionPanelBackgroundField;
    private ColorField actionForegroundField;
    private ColorField actionBackgroundField;
    private ColorField actionButtonBackgroundField;
    private ColorField groupHeaderForegroundField;
    private ColorField groupHeaderBackgroundField;
    private CheckBoxField toolBarButtonsTransparentField;
    private ColorField toolBarButtonBackgroundField;
    private FontField headerFontField;
    private FontField actionFontField;

    public ActionPanelDemoPanel() {
        actionPanel = new ActionPanel(); // We need this before we try to build our content panels
        panelMap.put(TAB_INTRO, buildIntroPanel());
        panelMap.put(TAB_COMPONENTS, buildComponentTypePanel());
        panelMap.put(TAB_TOOLBAR, buildToolBarOptionsPanel());
        panelMap.put(TAB_COLORS, buildColorPanel());
        panelMap.put(TAB_FONTS, buildFontPanel());
        panelMap.put(TAB_BORDERS, buildBorderPanel());
        panelMap.put(TAB_ICONS, buildIconPanel());
        panelMap.put(TAB_SORTING, buildSortingPanel());
        panelMap.put(TAB_MARGINS, buildMarginsPanel());
        panelMap.put(TAB_EXPAND, buildExpandCollapsePanel());
        panelMap.put(TAB_ANIMATION, buildAnimationPanel());
        for (String key : panelMap.keySet()) {
            cardPanel.add(PropertiesDialog.buildScrollPane(panelMap.get(key)), key);
        }

        // Let's start with the Welcome tab visible:
        cardPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        ((CardLayout)cardPanel.getLayout()).show(cardPanel, TAB_INTRO);

        // Just for fun, load some sound effects:
        try {
            expandClip = AudioSystem.getClip();
            expandClip.open(AudioSystem.getAudioInputStream(getClass().getResourceAsStream(EXPAND_CLIP)));
            collapseClip = AudioSystem.getClip();
            collapseClip.open(AudioSystem.getAudioInputStream(getClass().getResourceAsStream(COLLAPSE_CLIP)));
        }
        catch (Exception e) {
            // No fun for you!
            log.log(Level.SEVERE, "Could not load sound effects :(", e);
        }

        // Register for L&F updates so we can keep our demo panels updated if the user changes L&F:
        LookAndFeelManager.addChangeListener(e -> {
            for (FormPanel fp : panelMap.values()) {
                SwingUtilities.updateComponentTreeUI(fp);
            }
        });
    }

    @Override
    public String getTitle() {
        return "ActionPanel";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("ActionPanel demo");

        String sb = "<html>The <b>ActionPanel</b> component provides a way to very easily<br>" +
                "group actions into customizable groups within a vertically-stacked list.<br>" +
                "ActionPanel is highly customizable! Try it out below.</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(16);
        formPanel.add(labelField);

        // We'll use a PanelField to show the ActionPanel and the content panel side-by-side:
        actionPanel.setCardContainer(cardPanel);
        actionPanel.getToolBarOptions().setAllowGroupRemoval(false); // disabled by default as we have no "undo" for it.
        configureToolBarAdd(true); // enabled by default to show off the feature
        populateActionPanel();
        PanelField exampleContainer = new PanelField(new BorderLayout());
        exampleContainer.setShouldExpand(true);
        exampleContainer.getPanel().add(actionPanel, BorderLayout.WEST);
        exampleContainer.getPanel().add(cardPanel, BorderLayout.CENTER);
        formPanel.add(exampleContainer);

        // Set initial styling options for the ActionPanel based on default field values:
        actionPanel.getBorderOptions().setHeaderBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        colorSourceFieldChanged(); // set our cool custom colors
        actionPanel.setActionIndent(4);

        return formPanel;
    }

    /**
     * Enable or disable the toolbar options fields based on whether the toolbar is enabled.
     */
    private void toolBarEnabledChanged(boolean isEnabled) {
        actionPanel.setToolBarEnabled(isEnabled);
        toolBarButtonPositionField.setEnabled(isEnabled);
        toolBarIconSizeField.setEnabled(isEnabled);
        toolBarAllowAddField.setEnabled(isEnabled);
        toolBarAllowRenameField.setEnabled(isEnabled);
        toolBarAllowItemReorderField.setEnabled(isEnabled);
        toolBarAllowItemRemovalField.setEnabled(isEnabled);
        toolBarAllowRemoveField.setEnabled(isEnabled);
    }

    /**
     * Enable or disable the custom color fields based on selection.
     * Also applies the selected colors to the ActionPanel.
     */
    private void colorSourceFieldChanged() {
        boolean isCustom = colorSourceField.getSelectedIndex() == 1;
        setFromThemeField.setEnabled(isCustom);
        actionPanelBackgroundField.setEnabled(isCustom);
        actionForegroundField.setEnabled(isCustom);
        actionBackgroundField.setEnabled(isCustom);
        actionButtonBackgroundField.setEnabled(isCustom);
        groupHeaderForegroundField.setEnabled(isCustom);
        groupHeaderBackgroundField.setEnabled(isCustom);
        actionPanel.setBackground(isCustom ? actionPanelBackgroundField.getColor() : null);
        actionPanel.getColorOptions().setActionForeground(isCustom ? actionForegroundField.getColor() : null);
        actionPanel.getColorOptions().setActionBackground(isCustom ? actionBackgroundField.getColor() : null);
        actionPanel.getColorOptions()
                   .setActionButtonBackground(isCustom ? actionButtonBackgroundField.getColor() : null);
        actionPanel.getColorOptions().setGroupHeaderForeground(isCustom ? groupHeaderForegroundField.getColor() : null);
        actionPanel.getColorOptions().setGroupHeaderBackground(isCustom ? groupHeaderBackgroundField.getColor() : null);

        // ToolBar button customization has an extra layer of enabled/disabled:
        if (isCustom) {
            toolBarButtonsTransparentField.setEnabled(true);
            toolBarButtonBackgroundField.setEnabled(!toolBarButtonsTransparentField.isChecked());
        }
        else {
            toolBarButtonsTransparentField.setEnabled(false);
            toolBarButtonBackgroundField.setEnabled(false);
        }

        // And special handling for updating the toolbar button color:
        if (!isCustom) {
            actionPanel.getColorOptions().setToolBarButtonBackground(null);
        }
        else {
            if (toolBarButtonsTransparentField.isChecked()) {
                actionPanel.getColorOptions().setToolBarButtonsTransparent();
            }
            else {
                actionPanel.getColorOptions().setToolBarButtonBackground(toolBarButtonBackgroundField.getColor());
            }
        }
    }

    /**
     * Enable or disable the custom font fields based on selection.
     * Also applies the selected fonts to the ActionPanel.
     */
    private void fontSourceFieldChanged() {
        boolean isCustom = fontSourceField.getSelectedIndex() == 1;
        headerFontField.setEnabled(isCustom);
        actionFontField.setEnabled(isCustom);
        actionPanel.setGroupHeaderFont(isCustom ? headerFontField.getSelectedFont() : null);
        actionPanel.setActionFont(isCustom ? actionFontField.getSelectedFont() : null);
    }

    /**
     * Invoked internally to alphabetize the action groups by name if the sort
     * checkbox is selected (otherwise will sort by add order, which is deliberately not alphabetical).
     */
    private void setGroupComparator() {
        if (sortGroupsByNameField.isChecked()) {
            actionPanel.setGroupComparator(String::compareToIgnoreCase);
        }
        else {
            actionPanel.setGroupComparator(null);
        }
    }

    /**
     * Invoked internally to alphabetize the actions by name if the sort
     * checkbox is selected (otherwise will sort by add order, which is deliberately not alphabetical).
     * Note that you can sort actions by any of their properties! This example
     * sorts them by name, but you could choose to sort them based on whether they have an
     * icon, or by some other criteria within your application.
     */
    private void setActionComparator() {
        if (sortActionsByNameField.isChecked()) {
            actionPanel.setActionComparator((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        }
        else {
            actionPanel.setActionComparator(null);
        }
    }

    /**
     * Adds some example actions to our action panel.
     */
    private void populateActionPanel() {
        // Our action names double as cardIds for simplicity's sake,
        // but in a real application, you can handle this however you like.
        ImageIcon icon = SwingFormsResources.getCopyIcon(SwingFormsResources.NATIVE_SIZE); // arbitrary
        actionPanel.add("Welcome to ActionPanel!", new CardAction(TAB_INTRO, TAB_INTRO, icon));
        actionPanel.add("Welcome to ActionPanel!", new CardAction(TAB_COMPONENTS, TAB_COMPONENTS, icon));
        actionPanel.add("Welcome to ActionPanel!", new CardAction(TAB_TOOLBAR, TAB_TOOLBAR, icon));
        actionPanel.add("Styling options", new CardAction(TAB_COLORS, TAB_COLORS, icon));
        actionPanel.add("Styling options", new CardAction(TAB_FONTS, TAB_FONTS, icon));
        actionPanel.add("Styling options", new CardAction(TAB_BORDERS, TAB_BORDERS, icon));
        actionPanel.add("Styling options", new CardAction(TAB_ICONS, TAB_ICONS, icon));
        actionPanel.add("Layout options", new CardAction(TAB_MARGINS, TAB_MARGINS, icon));
        actionPanel.add("Layout options", new CardAction(TAB_SORTING, TAB_SORTING, icon));
        actionPanel.add("Behavior options", new CardAction(TAB_EXPAND, TAB_EXPAND, icon));
        actionPanel.add("Behavior options", new CardAction(TAB_ANIMATION, TAB_ANIMATION, icon));

        // Add header icons for demonstration:
        final int size = SwingFormsResources.NATIVE_SIZE;
        actionPanel.setGroupIcon("Welcome to ActionPanel!", SwingFormsResources.getValidIcon(size));
        actionPanel.setGroupIcon("Styling options", SwingFormsResources.getValidIcon(size));
        actionPanel.setGroupIcon("Layout options", SwingFormsResources.getValidIcon(size));
        actionPanel.setGroupIcon("Behavior options", SwingFormsResources.getValidIcon(size));
    }

    /**
     * Builds the Intro to ActionPanel content panel.
     */
    private FormPanel buildIntroPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Intro to ActionPanel"));
        String label = """
                <html>The <b>ActionPanel</b> component provides a way to<br>
                very easily group actions into customizable, expandable<br>
                groups within a vertically-stacked list.<br><br>
                ActionPanel is extremely customizable!<br><br>
                Navigate through the ActionPanel to the left to see all<br>
                the ways in which you can modify the appearance and<br>
                behavior of the ActionPanel component!</html>
                """;
        formPanel.add(new LabelField(label));
        formPanel.add(new LabelField("")); // Cheesy spacer for aesthetics

        return formPanel;
    }

    /**
     * Builds a demo panel for switching between button and label styles for actions.
     */
    private FormPanel buildComponentTypePanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Buttons or labels?"));
        String label = """
                <html>ActionPanel can display each action either as a<br>
                clickable label, or as a button.<br><br>
                You can choose the style that best fits<br>
                your application's UI!</html>
                """;
        formPanel.add(new LabelField(label));

        List<String> options = List.of("Show actions as clickable labels", "Show actions as buttons");
        useLabelsField = new ComboField<>("Action style:", options, 0);
        useLabelsField.addValueChangedListener(f -> {
            if (useLabelsField.getSelectedIndex() == 0) {
                // Re-enable left indent for labels, if it was set:
                actionIndentField.setEnabled(true);
                actionPanel.setActionIndent(actionIndentField.getCurrentValue().intValue());
                actionPanel.setUseLabels();
            }
            else {
                // Left indent only makes sense for labels, disable it for buttons:
                actionIndentField.setEnabled(false);
                actionPanel.setActionIndent(0);
                actionPanel.setUseButtons();
            }
        });
        formPanel.add(useLabelsField);

        return formPanel;
    }

    /**
     * Builds a demo panel for showing off the optional toolbar within each action group,
     * and the various options for customizing it.
     */
    private FormPanel buildToolBarOptionsPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Toolbar options"));
        String label = """
                <html>Each action group can optionally have a toolbar<br>
                with buttons for adding, removing, and reordering<br>
                actions within the group, as well as buttons for<br>
                renaming or removing the group itself.<br><br>
                You can enable or disable any of these buttons,<br>
                and you can even add your own custom buttons!<br><br>
                Try it out with the options below!</html>
                """;
        formPanel.add(new LabelField(label));

        CheckBoxField enableField = new CheckBoxField("Enable toolbar", false);
        enableField.addValueChangedListener(f -> toolBarEnabledChanged(enableField.isChecked()));
        formPanel.add(enableField);

        toolBarButtonPositionField = new ComboField<>("Button position:",
                                                      Arrays.asList(ToolBarOptions.ButtonPosition.values()),
                                                      3);
        toolBarButtonPositionField.setEnabled(false);
        toolBarButtonPositionField.addValueChangedListener(
                f -> actionPanel.getToolBarOptions().setButtonPosition(toolBarButtonPositionField.getSelectedItem()));
        formPanel.add(toolBarButtonPositionField);

        toolBarIconSizeField = new NumberField("Icon size:", ActionPanel.DEFAULT_ICON_SIZE, 8, 64, 1);
        toolBarIconSizeField.addValueChangedListener(
                f -> actionPanel.getToolBarOptions().setIconSize(toolBarIconSizeField.getCurrentValue().intValue()));
        toolBarIconSizeField.setEnabled(false);
        formPanel.add(toolBarIconSizeField);

        toolBarAllowAddField = new CheckBoxField("Allow creation of new actions per group", true);
        toolBarAllowAddField.addValueChangedListener(f -> configureToolBarAdd(toolBarAllowAddField.isChecked()));
        toolBarAllowAddField.setEnabled(false);
        formPanel.add(toolBarAllowAddField);

        toolBarAllowRenameField = new CheckBoxField("Allow renaming groups", true);
        toolBarAllowRenameField.addValueChangedListener(
                f -> actionPanel.getToolBarOptions().setAllowGroupRename(toolBarAllowRenameField.isChecked()));
        toolBarAllowRenameField.setEnabled(false);
        formPanel.add(toolBarAllowRenameField);

        toolBarAllowItemReorderField = new CheckBoxField("Allow reordering actions within groups", true);
        toolBarAllowItemReorderField.addValueChangedListener(
                f -> actionPanel.getToolBarOptions().setAllowItemReorder(toolBarAllowItemReorderField.isChecked()));
        toolBarAllowItemReorderField.setEnabled(false);
        formPanel.add(toolBarAllowItemReorderField);

        toolBarAllowItemRemovalField = new CheckBoxField("Allow removing actions from groups", true);
        toolBarAllowItemRemovalField.addValueChangedListener(
                f -> actionPanel.getToolBarOptions().setAllowItemRemoval(toolBarAllowItemRemovalField.isChecked()));
        toolBarAllowItemRemovalField.setEnabled(false);
        formPanel.add(toolBarAllowItemRemovalField);

        toolBarAllowRemoveField = new CheckBoxField("Allow removing groups", false);
        toolBarAllowRemoveField.setHelpText("<html><b>Use with caution!</b><br>" +
                                                    "This demo app doesn't have an \"undo\" for this option.<br>" +
                                                    "You'll have to restart the demo app to restore removed groups.<br>" +
                                                    "(Client applications can of course implement their own \"undo\".)</html>");
        toolBarAllowRemoveField.addValueChangedListener(
                f -> actionPanel.getToolBarOptions().setAllowGroupRemoval(toolBarAllowRemoveField.isChecked()));
        toolBarAllowRemoveField.setEnabled(false);
        formPanel.add(toolBarAllowRemoveField);

        return formPanel;
    }

    /**
     * Builds a form panel for messing with color options.
     */
    private FormPanel buildColorPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Color options"));
        String label = """
                <html>You can rely on the current Look and Feel,<br>
                so that ActionPanel fits in well with the rest of<br>
                your application's UI, or you can customize<br>
                the colors used in the ActionPanel to suit<br>
                your application's theme!</html>
                """;
        formPanel.add(new LabelField(label));

        List<String> options = List.of("Use Look and Feel defaults", "Override with custom styling");
        colorSourceField = new ComboField<>("ActionPanel styling:", options, 1);
        colorSourceField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(colorSourceField);

        setFromThemeField = new ButtonField(List.of(new SetFromThemeAction()));
        setFromThemeField.getFieldLabel().setText("Set from theme:");
        setFromThemeField.getMargins().setLeft(16); // indent a bit to show that these are styling options
        formPanel.add(setFromThemeField);

        actionPanelBackgroundField = new ColorField("Panel background:", ColorSelectionType.SOLID);
        actionPanelBackgroundField.setColor(Color.DARK_GRAY);
        actionPanelBackgroundField.getMargins().setLeft(16); // indent a bit to show that these are styling options
        actionPanelBackgroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(actionPanelBackgroundField);

        actionForegroundField = new ColorField("Action foreground:", ColorSelectionType.SOLID);
        actionForegroundField.setColor(Color.BLACK);
        actionForegroundField.getMargins().setLeft(16); // indent a bit to show that these are styling options
        actionForegroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(actionForegroundField);

        actionBackgroundField = new ColorField("Action background:", ColorSelectionType.SOLID);
        actionBackgroundField.setColor(Color.LIGHT_GRAY);
        actionBackgroundField.getMargins().setLeft(16);
        actionBackgroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(actionBackgroundField);

        actionButtonBackgroundField = new ColorField("Action buttons:", ColorSelectionType.SOLID);
        actionButtonBackgroundField.setColor(new Color(180, 180, 180)); // arbitrary, but I like it
        actionButtonBackgroundField.getMargins().setLeft(16);
        actionButtonBackgroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(actionButtonBackgroundField);

        groupHeaderForegroundField = new ColorField("Header foreground:", ColorSelectionType.SOLID);
        groupHeaderForegroundField.setColor(Color.WHITE);
        groupHeaderForegroundField.getMargins().setLeft(16);
        groupHeaderForegroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(groupHeaderForegroundField);

        groupHeaderBackgroundField = new ColorField("Header background:", ColorSelectionType.SOLID);
        groupHeaderBackgroundField.setColor(STEEL_BLUE);
        groupHeaderBackgroundField.getMargins().setLeft(16);
        groupHeaderBackgroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(groupHeaderBackgroundField);

        toolBarButtonBackgroundField = new ColorField("Toolbar buttons:", ColorSelectionType.SOLID);
        toolBarButtonBackgroundField.setColor(new Color(160, 160, 160)); // arbitrary, but I like it
        toolBarButtonBackgroundField.getMargins().setLeft(16);
        toolBarButtonBackgroundField.addValueChangedListener(f -> colorSourceFieldChanged());
        formPanel.add(toolBarButtonBackgroundField);

        toolBarButtonsTransparentField = new CheckBoxField("Make toolbar buttons transparent", false);
        toolBarButtonsTransparentField.addValueChangedListener(f -> colorSourceFieldChanged());
        toolBarButtonsTransparentField.getMargins().setLeft(16);
        formPanel.add(toolBarButtonsTransparentField);

        return formPanel;
    }

    /**
     * Builds a form panel for messing with font options.
     */
    private FormPanel buildFontPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Font options"));
        String label = """
                <html>Similar to the color options, you can either<br>
                rely on the Look and Feel defaults, or set your own<br>
                custom fonts for use in the header and for actions.</html>
                """;
        formPanel.add(new LabelField(label));

        List<String> options = List.of("Use Look and Feel defaults", "Override with custom styling");
        fontSourceField = new ComboField<>("Styling:", options, 0);
        fontSourceField.addValueChangedListener(f -> fontSourceFieldChanged());
        formPanel.add(fontSourceField);

        Font starterFont = new Font(Font.DIALOG, Font.BOLD, 12);
        headerFontField = new FontField("Header font:", starterFont);
        headerFontField.setShowSizeField(true);
        headerFontField.setEnabled(false);
        headerFontField.addValueChangedListener(f -> fontSourceFieldChanged());
        formPanel.add(headerFontField);

        starterFont = starterFont.deriveFont(Font.PLAIN);
        actionFontField = new FontField("Action font:", starterFont);
        actionFontField.setShowSizeField(true);
        actionFontField.setEnabled(false);
        actionFontField.addValueChangedListener(f -> fontSourceFieldChanged());
        formPanel.add(actionFontField);

        return formPanel;
    }

    /**
     * Builds a form panel for messing with border options.
     */
    private FormPanel buildBorderPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Border options"));
        String label = """
                <html>You can set optional borders around various<br>
                components within an ActionPanel.<br>
                Try it out below!</html>
                """;
        formPanel.add(new LabelField(label));

        actionPanelBorderField = new ComboField<>("ActionPanel border:", Arrays.asList(BorderOption.values()), 0);
        actionPanelBorderField.addValueChangedListener(
                f -> actionPanel.setBorder(actionPanelBorderField.getSelectedItem().getBorder()));
        formPanel.add(actionPanelBorderField);

        final BorderOptions borderOptions = actionPanel.getBorderOptions();
        groupBorderField = new ComboField<>("Group border:", Arrays.asList(BorderOption.values()), 0);
        groupBorderField.addValueChangedListener(
                f -> borderOptions.setGroupBorder(groupBorderField.getSelectedItem().getBorder()));
        formPanel.add(groupBorderField);

        headerBorderField = new ComboField<>("Header border:", Arrays.asList(BorderOption.values()), 1);
        headerBorderField.addValueChangedListener(
                f -> borderOptions.setHeaderBorder(headerBorderField.getSelectedItem().getBorder()));
        formPanel.add(headerBorderField);

        actionTrayBorderField = new ComboField<>("Action tray border:", Arrays.asList(BorderOption.values()), 0);
        actionTrayBorderField.addValueChangedListener(
                f -> borderOptions.setActionTrayBorder(actionTrayBorderField.getSelectedItem().getBorder()));
        formPanel.add(actionTrayBorderField);

        toolBarBorderField = new ComboField<>("Toolbar border:", Arrays.asList(BorderOption.values()), 0);
        toolBarBorderField.addValueChangedListener(
                f -> borderOptions.setToolBarBorder(toolBarBorderField.getSelectedItem().getBorder()));
        formPanel.add(toolBarBorderField);

        return formPanel;
    }

    /**
     * Builds a form panel for messing with icon options.
     */
    private FormPanel buildIconPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Icon options"));
        String label = """
                <html>You can assign optional icons for both<br>
                the header of each group, and for each<br>
                individual action.<br><br>
                Additionally, you can change the size of<br>
                icons, or disable them entirely.</html>
                """;
        formPanel.add(new LabelField(label));

        showActionIconsField = new CheckBoxField("Show action icons", true);
        showActionIconsField.addValueChangedListener(
                f -> actionPanel.setShowActionIcons(showActionIconsField.isChecked()));
        formPanel.add(showActionIconsField);

        showGroupHeaderIconsField = new CheckBoxField("Show group header icons", true);
        showGroupHeaderIconsField.addValueChangedListener(
                f -> actionPanel.setShowGroupIcons(showGroupHeaderIconsField.isChecked()));
        formPanel.add(showGroupHeaderIconsField);

        actionIconSizeField = new NumberField("Action icon size:", ActionPanel.DEFAULT_ICON_SIZE, 8, 64, 1);
        actionIconSizeField.addValueChangedListener(
                f -> actionPanel.setActionIconSize(actionIconSizeField.getCurrentValue().intValue()));
        formPanel.add(actionIconSizeField);

        headerIconSizeField = new NumberField("Header icon size:", ActionPanel.DEFAULT_ICON_SIZE, 8, 64, 1);
        headerIconSizeField.addValueChangedListener(
                f -> actionPanel.setHeaderIconSize(headerIconSizeField.getCurrentValue().intValue()));
        formPanel.add(headerIconSizeField);

        ComboField<String> expandCollapseIconField = new ComboField<>("Expand/collapse icon style:",
                                                                      List.of("Plus/minus", "Arrow up/down"),
                                                                      0);
        expandCollapseIconField.addValueChangedListener(f -> {
            if (expandCollapseIconField.getSelectedIndex() == 0) {
                actionPanel.setExpandIcon(SwingFormsResources.getPlusIcon(SwingFormsResources.NATIVE_SIZE));
                actionPanel.setCollapseIcon(SwingFormsResources.getMinusIcon(SwingFormsResources.NATIVE_SIZE));
            }
            else {
                actionPanel.setExpandIcon(SwingFormsResources.getMoveDownIcon(SwingFormsResources.NATIVE_SIZE));
                actionPanel.setCollapseIcon(SwingFormsResources.getMoveUpIcon(SwingFormsResources.NATIVE_SIZE));
            }
        });
        formPanel.add(expandCollapseIconField);

        return formPanel;
    }

    /**
     * Builds a panel to show off the different sorting options.
     */
    private FormPanel buildSortingPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Sorting options"));
        String label = """
                <html>By default, actions and groups are<br>
                presented in the order they are added.<br><br>
                You can opt to keep groups sorted by name,<br>
                and you can provide a custom Comparator<br>
                for keeping actions sorted as well.<br><br>
                Try it out below!</html>
                """;
        formPanel.add(new LabelField(label));

        sortGroupsByNameField = new CheckBoxField("Sort groups by name", false);
        sortGroupsByNameField.addValueChangedListener(f -> setGroupComparator());
        formPanel.add(sortGroupsByNameField);

        sortActionsByNameField = new CheckBoxField("Sort actions by name", false);
        sortActionsByNameField.addValueChangedListener(f -> setActionComparator());
        formPanel.add(sortActionsByNameField);

        return formPanel;
    }

    /**
     * Builds a panel to show how to customize margins and padding.
     */
    private FormPanel buildMarginsPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Margins and padding options"));
        String label = """
                <html>You can control the spacing around each<br>
                action group, and the spacing around the<br>
                actions themselves.</html>
                """;
        formPanel.add(new LabelField(label));

        externalPaddingField = new NumberField("External padding:", ActionPanel.DEFAULT_EXTERNAL_PADDING, 0, 32, 1);
        externalPaddingField.addValueChangedListener(
                f -> actionPanel.setExternalPadding(externalPaddingField.getCurrentValue().intValue()));
        externalPaddingField.setHelpText("<html>Sets the space between each group and the edges<br>" +
                                                 "of the panel, and also the space between groups.</html>");
        formPanel.add(externalPaddingField);

        headerInternalPaddingField = new NumberField("Header padding:", ActionPanel.DEFAULT_INTERNAL_PADDING, 0, 32, 1);
        headerInternalPaddingField.addValueChangedListener(
                f -> actionPanel.setHeaderInternalPadding(headerInternalPaddingField.getCurrentValue().intValue()));
        headerInternalPaddingField.setHelpText(
                "<html>Sets the space between components within the header of an ActionGroup,<br>" +
                        "as well as the space between those components and the inner edge of the group.</html>");
        formPanel.add(headerInternalPaddingField);

        actionInternalPaddingField = new NumberField("Action padding:", ActionPanel.DEFAULT_INTERNAL_PADDING, 0, 32, 1);
        actionInternalPaddingField.addValueChangedListener(
                f -> actionPanel.setActionInternalPadding(actionInternalPaddingField.getCurrentValue().intValue()));
        actionInternalPaddingField.setHelpText(
                "<html>Sets the space between action buttons/labels in an ActionGroup,<br>" +
                        "as well as the space between those components and the inner edge of the group.</html>");
        formPanel.add(actionInternalPaddingField);

        toolBarInternalPaddingField = new NumberField("Toolbar padding:", ActionPanel.DEFAULT_INTERNAL_PADDING, 0, 32,
                                                      1);
        toolBarInternalPaddingField.addValueChangedListener(
                f -> actionPanel.setToolBarInternalPadding(toolBarInternalPaddingField.getCurrentValue().intValue()));
        toolBarInternalPaddingField.setHelpText(
                "<html>Sets the space between components within the toolbar of an ActionGroup,<br>" +
                        "as well as the space between those components and the inner edge of the group.<br>" +
                        "(ToolBar must be enabled and not in \"Stretch\" mode for this value to work).</html>");
        formPanel.add(toolBarInternalPaddingField);

        actionIndentField = new NumberField("Action left indent:", 4, 0, 32, 1);
        actionIndentField.addValueChangedListener(
                f -> actionPanel.setActionIndent(actionIndentField.getCurrentValue().intValue()));
        actionIndentField.setHelpText("<html>When using labels instead of buttons, you can<br>" +
                                              "set a left indent for the action labels to give them a bit<br>" +
                                              "of separation from the group header.</html>");
        formPanel.add(actionIndentField);

        return formPanel;
    }

    /**
     * Builds a panel to show how to customize expand/collapse options.
     */
    private FormPanel buildExpandCollapsePanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Expand/collapse options"));
        String label = """
                <html>By default, each group in an ActionPanel<br>
                is expandable and collapsible.<br><br>
                You can prevent groups from being collapsed,<br>
                or start with certain groups expanded or<br>
                collapsed by default.</html>
                """;
        formPanel.add(new LabelField(label));

        final ExpandCollapseOptions options = actionPanel.getExpandCollapseOptions();
        CheckBoxField allowCollapseField = new CheckBoxField("Allow groups to be collapsed", true);
        allowCollapseField.addValueChangedListener(f -> options.setExpandable(allowCollapseField.isChecked()));
        formPanel.add(allowCollapseField);

        CheckBoxField allowDoubleClickField = new CheckBoxField(
                "Allow double-click on header to toggle expand/collapse", false);
        allowDoubleClickField.addValueChangedListener(
                f -> options.setAllowHeaderDoubleClick(allowDoubleClickField.isChecked()));
        formPanel.add(allowDoubleClickField);

        return formPanel;
    }

    /**
     * Builds a panel to show how to customize animation options.
     */
    private FormPanel buildAnimationPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel("Animation options"));
        String label = """
                <html>The expand/collapse of groups, if enabled,<br>
                is animated by default, for a smooth experience.<br><br>
                You can customize the animation speed, or disable<br>
                animation entirely.</html>
                """;
        formPanel.add(new LabelField(label));

        List<String> options = List.of("No animation (instant)",
                                       "Slow animation",
                                       "Medium animation",
                                       "Fast animation");
        animationField = new ComboField<>("Expand/collapse animation:", options, 2);
        ExpandCollapseOptions expandOptions = actionPanel.getExpandCollapseOptions();
        animationField.addValueChangedListener(f -> {
            switch (animationField.getSelectedIndex()) {
                case 0 -> expandOptions.setAnimationEnabled(false);
                case 1 -> {
                    expandOptions.setAnimationEnabled(true);
                    expandOptions.setAnimationDurationMs(600);
                }
                case 2 -> {
                    expandOptions.setAnimationEnabled(true);
                    expandOptions.setAnimationDurationMs(200);
                }
                case 3 -> {
                    expandOptions.setAnimationEnabled(true);
                    expandOptions.setAnimationDurationMs(100);
                }
            }
        });
        formPanel.add(animationField);

        CheckBoxField playSoundField = new CheckBoxField("Play sound effects on expand/collapse", false);
        playSoundField.addValueChangedListener(f -> {
            if (playSoundField.isChecked()) {
                actionPanel.addExpandListener(this);
            }
            else {
                actionPanel.removeExpandListener(this);
            }
        });
        formPanel.add(playSoundField);

        return formPanel;
    }

    /**
     * Invoked from ActionPanel when a group is expanded or collapsed.
     * We'll play a sound effect if available, just for fun.
     *
     * @param groupName  The name of the group that changed.
     * @param isExpanded True if the group is now expanded, false if collapsed.
     */
    @Override
    public void groupExpandedChanged(String groupName, boolean isExpanded) {
        if (expandClip == null || collapseClip == null) {
            return; // no sound effects loaded
        }
        if (isExpanded) {
            expandClip.setFramePosition(0); // rewind
            expandClip.start();
        }
        else {
            collapseClip.setFramePosition(0); // rewind
            collapseClip.start();
        }
    }

    private void configureToolBarAdd(boolean allowAdd) {
        actionPanel.getToolBarOptions().setAllowItemAdd(allowAdd);
        if (allowAdd) {
            actionPanel.getToolBarOptions().setNewActionSupplier((a, g) -> {
                TextInputDialog dialog = new TextInputDialog(DemoApp.getInstance(), "New action");
                dialog.setAllowBlank(false);
                // Ensure our name is unique:
                dialog.addValidator(field -> {
                    String newName;
                    if (field instanceof ShortTextField) {
                        newName = ((ShortTextField)field).getText();
                    }
                    else {
                        newName = ((LongTextField)field).getText();
                    }
                    if (panelMap.containsKey(newName)) {
                        return ValidationResult.invalid("That name is already in use.");
                    }
                    return ValidationResult.valid();
                });
                dialog.setInitialText("New action");
                dialog.setVisible(true);
                String actionName = dialog.getResult();
                if (actionName != null) {
                    panelMap.put(actionName, buildExampleNewItemPanel(actionName));
                    cardPanel.add(PropertiesDialog.buildScrollPane(panelMap.get(actionName)), actionName);
                    return new CardAction(actionName, actionName); // our actionName doubles as cardId
                }
                return null;
            });
        }
        else {
            actionPanel.getToolBarOptions().setNewActionSupplier(null);
        }
    }

    private FormPanel buildExampleNewItemPanel(String name) {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.add(LabelField.createBoldHeaderLabel(name));
        String label = """
                <html>You can enable the creation of new items in an ActionPanel<br>
                by providing a supplier for new actions.<br><br>
                In this demo, you just created an example action that<br>
                brings you to this panel when clicked. This is just<br>
                one simple example of what you can do - your<br>
                application could pop a custom dialog to gather information<br>
                for the new action, or it could pull one from a database<br>
                or external service of some kind. Whatever you need!</html>
                """;
        formPanel.add(new LabelField(label));

        return formPanel;
    }

    /**
     * Represents a few different options for setting borders.
     * These are just examples - you can of course create your own borders
     * and set them on the ActionPanel as desired.
     */
    private enum BorderOption {
        NONE("None", null),
        THIN_BLACK_LINE("Thin black line border", BorderFactory.createLineBorder(Color.BLACK, 1)),
        THICK_BLACK_LINE("Thick black line border", BorderFactory.createLineBorder(Color.BLACK, 3)),
        THIN_WHITE_LINE("Thin white line border", BorderFactory.createLineBorder(Color.WHITE, 1)),
        THICK_WHITE_LINE("Thick white line border", BorderFactory.createLineBorder(Color.WHITE, 3)),
        THIN_BLUE_LINE("Thin blue line border", BorderFactory.createLineBorder(Color.BLUE, 1)),
        THICK_BLUE_LINE("Thick blue line border", BorderFactory.createLineBorder(Color.BLUE, 3)),
        ETCHED("Etched border", BorderFactory.createEtchedBorder()),
        RAISED_BEVELED("Raised beveled border", BorderFactory.createRaisedBevelBorder()),
        LOWERED_BEVELED("Lowered beveled border", BorderFactory.createLoweredBevelBorder());

        private final String label;
        private final Border border;

        BorderOption(String label, Border border) {
            this.label = label;
            this.border = border;
        }

        @Override
        public String toString() {
            return label;
        }

        public Border getBorder() {
            return border;
        }
    }

    /**
     * A simple action to show an input dialog to select one of our built-in color themes and
     * apply it to all the relevant color fields at once.
     */
    private class SetFromThemeAction extends AbstractAction {
        public SetFromThemeAction() {
            super("Choose...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object input = JOptionPane.showInputDialog(DemoApp.getInstance(),
                                                       "Select color theme:",
                                                       "Choose theme",
                                                       JOptionPane.PLAIN_MESSAGE,
                                                       null,
                                                       ColorTheme.values(),
                                                       ColorTheme.values()[0]);
            if (input != null) {
                ColorTheme selectedTheme = (ColorTheme)input;
                actionPanel.setAutoRebuildEnabled(false);
                try {
                    // Each one of these would trigger a full rebuild of our ActionPanel,
                    // because of the change listeners attached to our form fields.
                    // We can avoid a lot of unnecessary rebuilds by temporarily disabling auto-rebuild
                    // while we set all the colors:
                    actionPanelBackgroundField.setColor(selectedTheme.getPanelBackground());
                    actionBackgroundField.setColor(selectedTheme.getActionBackground());
                    actionForegroundField.setColor(selectedTheme.getActionForeground());
                    groupHeaderBackgroundField.setColor(selectedTheme.getGroupHeaderBackground());
                    groupHeaderForegroundField.setColor(selectedTheme.getGroupHeaderForeground());
                    actionButtonBackgroundField.setColor(selectedTheme.getActionButtonBackground());
                    toolBarButtonBackgroundField.setColor(selectedTheme.getToolBarButtonBackground());
                }
                finally {
                    // Re-enabling auto-rebuild will trigger a single rebuild now that all colors are set:
                    actionPanel.setAutoRebuildEnabled(true);
                }
            }
        }
    }
}
