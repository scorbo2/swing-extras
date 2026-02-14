package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static ca.corbett.extras.testutils.TestConstants.TEST_DOMAIN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HyperlinkUtil.
 */
class HyperlinkUtilTest {

    private HyperlinkUtil.DesktopBrowser mockBrowser;
    private Transferable clipboardContents;

    @BeforeEach
    void setUp() {
        // Make a note of whatever is currently in the system clipboard,
        // because some of these tests will copy stuff to it:
        try {
            clipboardContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        }
        catch (IllegalStateException ignored) {
            // Clipboard is currently unavailable; treat as no prior contents.
            clipboardContents = null;
        }

        mockBrowser = Mockito.mock(HyperlinkUtil.DesktopBrowser.class);
        HyperlinkUtil.setDesktopBrowser(mockBrowser);
    }

    @AfterEach
    void cleanUp() {
        // Restore the original clipboard contents after each test:
        if (clipboardContents != null) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardContents, null);
            }
            catch (IllegalStateException ignored) {
                // Clipboard is currently unavailable
                // just ignore this.
            }
        }
    }

    @AfterEach
    void tearDown() {
        HyperlinkUtil.resetDesktopBrowser();
    }

    @Test
    void testIsValidUrl_validHttpUrl() {
        assertTrue(HyperlinkUtil.isValidUrl("http://" + TEST_DOMAIN));
    }

    @Test
    void testIsValidUrl_validHttpsUrl() {
        assertTrue(HyperlinkUtil.isValidUrl("https://" + TEST_DOMAIN));
    }

    @Test
    void testIsValidUrl_validUrlWithPath() {
        assertTrue(HyperlinkUtil.isValidUrl("https://" + TEST_DOMAIN + "/path/to/page"));
    }

    @Test
    void testIsValidUrl_validUrlWithQuery() {
        assertTrue(HyperlinkUtil.isValidUrl("https://" + TEST_DOMAIN + "?query=value"));
    }

    @Test
    void testIsValidUrl_invalidUrl() {
        assertFalse(HyperlinkUtil.isValidUrl("not a url"));
    }

    @Test
    void testIsValidUrl_emptyString() {
        assertFalse(HyperlinkUtil.isValidUrl(""));
    }

    @Test
    void testIsValidUrl_null() {
        assertFalse(HyperlinkUtil.isValidUrl(null));
    }

    @Test
    void testIsValidUrl_malformedProtocol() {
        assertFalse(HyperlinkUtil.isValidUrl("ht!tp://" + TEST_DOMAIN));
    }

    @Test
    void testIsBrowsingSupported_whenSupported() {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        assertTrue(HyperlinkUtil.isBrowsingSupported());
    }

    @Test
    void testIsBrowsingSupported_whenNotSupported() {
        when(mockBrowser.isBrowsingSupported()).thenReturn(false);
        assertFalse(HyperlinkUtil.isBrowsingSupported());
    }

    @Test
    void testOpenHyperlink_withURI_whenBrowsingSupported() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URI testUri = URI.create("https://" + TEST_DOMAIN);

        HyperlinkUtil.openHyperlink(testUri, null);

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testOpenHyperlink_withURL_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URL testUrl = new URL("https://" + TEST_DOMAIN);

        HyperlinkUtil.openHyperlink(testUrl, null);

        verify(mockBrowser).browse(testUrl.toURI());
    }

    @Test
    void testOpenHyperlink_withString_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        String testUrlString = "https://" + TEST_DOMAIN;

        HyperlinkUtil.openHyperlink(testUrlString, null);

        verify(mockBrowser).browse(new URL(testUrlString).toURI());
    }

    @Test
    void testOpenHyperlink_withURI_noOwner_whenBrowsingSupported() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URI testUri = URI.create("https://" + TEST_DOMAIN);

        HyperlinkUtil.openHyperlink(testUri);

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testOpenHyperlink_withURL_noOwner_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URL testUrl = new URL("https://" + TEST_DOMAIN);

        HyperlinkUtil.openHyperlink(testUrl);

        verify(mockBrowser).browse(testUrl.toURI());
    }

    @Test
    void testOpenHyperlink_withString_noOwner_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        String testUrlString = "https://" + TEST_DOMAIN;

        HyperlinkUtil.openHyperlink(testUrlString);

        verify(mockBrowser).browse(new URL(testUrlString).toURI());
    }

    @Test
    void testOpenHyperlink_whenBrowsingNotSupported_copiesToClipboard() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(false);
        URI testUri = URI.create("https://" + TEST_DOMAIN);

        // This should not throw and should not call browse
        HyperlinkUtil.openHyperlink(testUri, null);

        verify(mockBrowser, never()).browse(any());

        // The test URL should now be in the system clipboard as a String:
        Object something = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        assertInstanceOf(String.class, something);
        String clipboardText = (String)something;
        assertEquals(testUri.toString(), clipboardText);
    }

    @Test
    void testOpenHyperlink_whenBrowseThrowsException_doesNotThrow() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        doThrow(new IOException("Browser failed")).when(mockBrowser).browse(any());
        URI testUri = URI.create("https://" + TEST_DOMAIN);

        // Should not throw - errors are caught and logged
        assertDoesNotThrow(() -> HyperlinkUtil.openHyperlink(testUri, null));

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testOpenHyperlink_withInvalidString_doesNotThrow() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);

        // Should not throw - malformed URL is caught
        assertDoesNotThrow(() -> HyperlinkUtil.openHyperlink("not a valid url", null));

        // browse should not be called with invalid URL
        verify(mockBrowser, never()).browse(any());
    }

    @Test
    void testBrowseHyperlinkAction_ofURI_createsAction() {
        URI testUri = URI.create("https://" + TEST_DOMAIN);
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUri);
        assertNotNull(action);
        assertEquals(testUri, action.uri);
        assertNull(action.url);
        assertNull(action.urlString);
        assertNull(action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofURL_createsAction() throws Exception {
        URL testUrl = new URL("https://" + TEST_DOMAIN);
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrl);
        assertNotNull(action);
        assertNull(action.uri);
        assertEquals(testUrl, action.url);
        assertNull(action.urlString);
        assertNull(action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofString_createsAction() {
        String testUrlString = "https://" + TEST_DOMAIN;
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrlString);
        assertNotNull(action);
        assertNull(action.uri);
        assertNull(action.url);
        assertEquals(testUrlString, action.urlString);
        assertNull(action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofURI_withOwner_createsAction() {
        URI testUri = URI.create("https://" + TEST_DOMAIN);
        Component owner = Mockito.mock(Component.class);
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUri, owner);
        assertNotNull(action);
        assertEquals(testUri, action.uri);
        assertNull(action.url);
        assertNull(action.urlString);
        assertEquals(owner, action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofURL_withOwner_createsAction() throws Exception {
        URL testUrl = new URL("https://" + TEST_DOMAIN);
        Component owner = Mockito.mock(Component.class);
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrl, owner);
        assertNotNull(action);
        assertNull(action.uri);
        assertEquals(testUrl, action.url);
        assertNull(action.urlString);
        assertEquals(owner, action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofString_withOwner_createsAction() {
        Component owner = Mockito.mock(Component.class);
        String testUrlString = "https://" + TEST_DOMAIN;
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrlString, owner);
        assertNotNull(action);
        assertNull(action.uri);
        assertNull(action.url);
        assertEquals(testUrlString, action.urlString);
        assertEquals(owner, action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_actionPerformed_withURI_callsBrowse() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URI testUri = URI.create("https://" + TEST_DOMAIN);
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUri);

        action.actionPerformed(null);

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testBrowseHyperlinkAction_actionPerformed_withURL_callsBrowse() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URL testUrl = new URL("https://" + TEST_DOMAIN);
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrl);

        action.actionPerformed(null);

        verify(mockBrowser).browse(testUrl.toURI());
    }

    @Test
    void testBrowseHyperlinkAction_actionPerformed_withString_callsBrowse() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        String testUrlString = "https://" + TEST_DOMAIN;
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrlString);

        action.actionPerformed(null);

        verify(mockBrowser).browse(new URL(testUrlString).toURI());
    }

    @Test
    void testResetDesktopBrowser_restoresSystemBrowser() {
        // Set a mock browser
        HyperlinkUtil.setDesktopBrowser(mockBrowser);
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        assertTrue(HyperlinkUtil.isBrowsingSupported());

        // Reset to system browser
        HyperlinkUtil.resetDesktopBrowser();

        // Now it should use the real system browser (behavior depends on environment)
        // We just verify it doesn't throw
        assertDoesNotThrow(HyperlinkUtil::isBrowsingSupported);
    }

    @Test
    void testSystemDesktopBrowser_isBrowsingSupported() {
        // Create a real SystemDesktopBrowser to verify it doesn't throw
        HyperlinkUtil.SystemDesktopBrowser browser = new HyperlinkUtil.SystemDesktopBrowser();
        assertDoesNotThrow(browser::isBrowsingSupported);
    }

    @Test
    void of_shouldSetNameCorrectly() throws Exception {
        final String expected = "https://" + TEST_DOMAIN;
        assertEquals(expected, HyperlinkUtil.BrowseAction.of(expected).getName());
        assertEquals(expected, HyperlinkUtil.BrowseAction.of(new URL(expected)).getName());
        assertEquals(expected, HyperlinkUtil.BrowseAction.of(URI.create(expected)).getName());
    }

    @Test
    void setName_shouldChangeName() throws Exception {
        final String expected = "https://" + TEST_DOMAIN;
        HyperlinkUtil.BrowseAction action1 = HyperlinkUtil.BrowseAction.of(expected);
        HyperlinkUtil.BrowseAction action2 = HyperlinkUtil.BrowseAction.of(new URL(expected));
        HyperlinkUtil.BrowseAction action3 = HyperlinkUtil.BrowseAction.of(URI.create(expected));
        assertEquals(expected, action1.getName());
        assertEquals(expected, action2.getName());
        assertEquals(expected, action3.getName());
        action1.setName("Action1");
        action2.setName("Action2");
        action3.setName("Action3");
        assertEquals("Action1", action1.getName());
        assertEquals("Action2", action2.getName());
        assertEquals("Action3", action3.getName());
    }
}

