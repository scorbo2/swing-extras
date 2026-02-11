package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

/**
 * An action that flips to a specific card in a CardLayout when triggered.
 * You can create these manually and add them to an ActionPanel, or you can
 * use the convenience method in ActionPanel to create them for you.
 * The following two approaches are equivalent:
 * <pre>
 *     // Given some card container and an actionPanel...
 *     cardContainer.setLayout(new CardLayout());
 *     cardContainer.add(new JPanel(), "card1");
 *     actionPanel = new ActionPanel();
 *
 *     // First, associate the Card Container with the ActionPanel:
 *     actionPanel.setCardContainer(cardContainer);
 *
 *     // Option 1: create a CardAction manually:
 *     CardAction action = new CardAction("Show Card 1", "card1");
 *     actionPanel.add("Group 1", action);
 *
 *     // Option 2: let ActionPanel do it for you:
 *     actionPanel.add("Group 1", "Show Card 1", "card1");
 *
 *     // In both cases, ActionPanel will let the CardAction know
 *     // about the Card Container. You don't have to manually associate that.
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class CardAction extends EnhancedAction {

    protected final String cardId;
    protected Container cardContainer;

    /**
     * Creates a CardAction with the given name and card ID.
     * The card ID is the name of the card to show when this action is triggered.
     * <p>
     * <b>Note:</b> Due to the design of CardLayout, we can't validate the given cardId.
     * If it doesn't match the name of a card that was given to your layout, then
     * nothing will happen when the action is triggered. It's up to calling code
     * to make sure the cardId given here matches the name of a card in the card container.
     * </p>
     *
     * @param name   the name of this action (e.g. the text to show on a button)
     * @param cardId the name of the card to show when this action is triggered. This should match the name of a card in the card container's CardLayout.
     */
    public CardAction(String name, String cardId) {
        super(name);
        this.cardId = cardId;

        if (cardId == null || cardId.isBlank()) {
            throw new IllegalArgumentException("Card ID cannot be null or blank");
        }
    }

    /**
     * Associates this action with a card container. This is handled for you by ActionPanel
     * and so should never have to be called manually.
     * Note that if the given Container does not have a CardLayout, this action will do nothing.
     *
     * @param container the card container that this action should flip when triggered. This should be a container whose layout is a CardLayout.
     * @return this CardAction, for chaining
     */
    CardAction setCardContainer(Container container) {
        this.cardContainer = container;
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (cardContainer == null || !(cardContainer.getLayout() instanceof CardLayout cardLayout)) {
            return; // just ignore - we are effectively disabled
        }

        // Flip to the requested card
        cardLayout.show(cardContainer, cardId);
    }
}
