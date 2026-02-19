package ca.corbett.extras.properties.dialog;

import ca.corbett.extras.ScrollUtil;
import ca.corbett.extras.actionpanel.ActionPanel;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A PropertiesDialog implementation that uses an ActionPanel on the left to show the categories and subcategories,
 * and a CardLayout on the right to show the corresponding FormPanel for the selected category/subcategory.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ActionPanelPropertiesDialog extends PropertiesDialog {

    private ActionPanel actionPanel;
    private JPanel cardPanel;
    private Map<String, FormPanel> formPanelsByCardId;
    private final boolean addPanelHeaders;

    /**
     * It's generally preferable to use the create factory methods in the parent PropertiesDialog class,
     * but you can directly instantiate this class if you want.
     *
     * @param owner           the parent window for this dialog
     * @param title           the title to show in the dialog header
     * @param properties      the list of properties to show in this dialog.
     * @param addPanelHeaders true to auto-generate a header label for each FormPanel.
     */
    public ActionPanelPropertiesDialog(Window owner, String title, List<AbstractProperty> properties, boolean addPanelHeaders) {
        super(owner, title, properties);
        this.addPanelHeaders = addPanelHeaders;
    }

    /**
     * There are a crazy number of options for ActionPanel, so rather than write wrapper methods
     * for each of them, we just expose the ActionPanel directly. Refer to the ActionPanel Javadocs
     * for details on what you can do with it, or refer to the built-in demo application to see
     * examples of it in action! You can make cosmetic/styling changes to the ActionPanel before or
     * after showing the dialog. Adding or removing actions or action groups, however, is not
     * recommended - better to leave that to this class.
     *
     * @return The ActionPanel used in this dialog, which you can customize as needed.
     */
    public ActionPanel getActionPanel() {
        return actionPanel;
    }

    /**
     * In this PropertiesDialog implementation, we will generate a FormPanel for each subcategory, then group
     * those FormPanels together in ActionGroups named after the top-level categories. This is a better
     * style than the "classic" tabbed pane style, particularly for very large properties dialogs
     * with many categories and subcategories. Clicking any subcategory in the ActionPanel on the left
     * will show the corresponding FormPanel on the right.
     */
    @Override
    protected void populateFormPanels() {
        formPanelsByCardId = new HashMap<>();
        for (String category : categories) {
            List<String> subCategories = subcategoriesByCategory.get(category);
            for (String subCategory : subCategories) {
                FormPanel formPanel = new FormPanel(alignment);
                formPanel.setBorderMargin(borderMargin);

                // Auto-generate a header label if requested.
                // This can be a lazy way of adding some visual context to your FormPanels.
                // But you have the option of adding header/info labels to your own properties list instead.
                if (addPanelHeaders) {
                    formPanel.add(LabelField.createBoldHeaderLabel(subCategory));
                }

                // Get all properties for this category/subcategory and render them to the FormPanel:
                List<AbstractProperty> propList = PropertiesManager.getProperties(properties, category, subCategory);
                for (AbstractProperty prop : propList) {
                    formPanel.add(prop.generateFormField(formPanel));
                }

                // Add this one to our parent class's list:
                formPanels.add(formPanel);

                // And make a note of the cardId for it:
                String cardId = category + "." + subCategory;
                formPanelsByCardId.put(cardId, formPanel);
            }
        }
    }

    @Override
    protected void initLayout() {
        // Create our ActionPanel and associate it with our CardLayout panel:
        actionPanel = new ActionPanel();
        cardPanel = new JPanel(new CardLayout());
        actionPanel.setCardContainer(cardPanel);
        actionPanel.setHighlightLastActionEnabled(true); // visual clue to show what's selected
        actionPanel.setAutoRebuildEnabled(false);

        String firstCardId = null; // we'll use this to show the first card by default at the end
        try {
            // Now go through what we have and add our cards:
            for (String category : categories) {
                List<String> subCategories = subcategoriesByCategory.get(category);
                for (String subCategory : subCategories) {
                    String cardId = category + "." + subCategory;
                    if (firstCardId == null) {
                        firstCardId = cardId;
                    }
                    FormPanel formPanel = formPanelsByCardId.get(cardId);
                    cardPanel.add(ScrollUtil.buildScrollPane(formPanel), cardId);
                    actionPanel.add(category, subCategory, cardId);
                }
            }
        }
        finally {
            // We only want to build once after adding everything.
            // Setting auto-rebuild back to true will trigger an immediate build:
            actionPanel.setAutoRebuildEnabled(true);
        }

        if (firstCardId != null) {
            ((CardLayout)cardPanel.getLayout()).show(cardPanel, firstCardId);
            actionPanel.setHighlightedAction(firstCardId);
        }

        add(actionPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    @Override
    protected void makeFormPanelVisible(FormPanel formPanel) {
        // Do a reverse lookup to find the cardId for this FormPanel, then show that card:
        String cardId = null;
        for (Map.Entry<String, FormPanel> entry : formPanelsByCardId.entrySet()) {
            if (entry.getValue() == formPanel) {
                cardId = entry.getKey();
                break;
            }
        }

        // Flip to that card and mark it as highlighted in the ActionPanel:
        ((CardLayout)cardPanel.getLayout()).show(cardPanel, cardId);
        actionPanel.setHighlightedAction(cardId);
    }
}
