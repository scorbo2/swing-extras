package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @BeforeEach
    void setUp() {
        mockBrowser = Mockito.mock(HyperlinkUtil.DesktopBrowser.class);
        HyperlinkUtil.setDesktopBrowser(mockBrowser);
    }

    @AfterEach
    void tearDown() {
        HyperlinkUtil.resetDesktopBrowser();
    }

    @Test
    void testIsValidUrl_validHttpUrl() {
        assertTrue(HyperlinkUtil.isValidUrl("http://example.com"));
    }

    @Test
    void testIsValidUrl_validHttpsUrl() {
        assertTrue(HyperlinkUtil.isValidUrl("https://example.com"));
    }

    @Test
    void testIsValidUrl_validUrlWithPath() {
        assertTrue(HyperlinkUtil.isValidUrl("https://example.com/path/to/page"));
    }

    @Test
    void testIsValidUrl_validUrlWithQuery() {
        assertTrue(HyperlinkUtil.isValidUrl("https://example.com?query=value"));
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
        assertFalse(HyperlinkUtil.isValidUrl("ht!tp://example.com"));
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
        URI testUri = URI.create("https://example.com");

        HyperlinkUtil.openHyperlink(testUri, null);

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testOpenHyperlink_withURL_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URL testUrl = new URL("https://example.com");

        HyperlinkUtil.openHyperlink(testUrl, null);

        verify(mockBrowser).browse(testUrl.toURI());
    }

    @Test
    void testOpenHyperlink_withString_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        String testUrlString = "https://example.com";

        HyperlinkUtil.openHyperlink(testUrlString, null);

        verify(mockBrowser).browse(new URL(testUrlString).toURI());
    }

    @Test
    void testOpenHyperlink_withURI_noOwner_whenBrowsingSupported() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URI testUri = URI.create("https://example.com");

        HyperlinkUtil.openHyperlink(testUri);

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testOpenHyperlink_withURL_noOwner_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URL testUrl = new URL("https://example.com");

        HyperlinkUtil.openHyperlink(testUrl);

        verify(mockBrowser).browse(testUrl.toURI());
    }

    @Test
    void testOpenHyperlink_withString_noOwner_whenBrowsingSupported() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        String testUrlString = "https://example.com";

        HyperlinkUtil.openHyperlink(testUrlString);

        verify(mockBrowser).browse(new URL(testUrlString).toURI());
    }

    @Test
    void testOpenHyperlink_whenBrowsingNotSupported_copiestoClipboard() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(false);
        URI testUri = URI.create("https://example.com");

        // This should not throw and should not call browse
        HyperlinkUtil.openHyperlink(testUri, null);

        verify(mockBrowser, never()).browse(any());
    }

    @Test
    void testOpenHyperlink_whenBrowseThrowsException_doesNotThrow() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        doThrow(new IOException("Browser failed")).when(mockBrowser).browse(any());
        URI testUri = URI.create("https://example.com");

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
        URI testUri = URI.create("https://example.com");
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUri);
        assertNotNull(action);
        assertEquals(testUri, action.uri);
        assertNull(action.url);
        assertNull(action.urlString);
        assertNull(action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofURL_createsAction() throws Exception {
        URL testUrl = new URL("https://example.com");
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrl);
        assertNotNull(action);
        assertNull(action.uri);
        assertEquals(testUrl, action.url);
        assertNull(action.urlString);
        assertNull(action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofString_createsAction() {
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of("https://example.com");
        assertNotNull(action);
        assertNull(action.uri);
        assertNull(action.url);
        assertEquals("https://example.com", action.urlString);
        assertNull(action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_ofURI_withOwner_createsAction() {
        URI testUri = URI.create("https://example.com");
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
        URL testUrl = new URL("https://example.com");
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
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of("https://example.com", owner);
        assertNotNull(action);
        assertNull(action.uri);
        assertNull(action.url);
        assertEquals("https://example.com", action.urlString);
        assertEquals(owner, action.owner);
    }

    @Test
    void testBrowseHyperlinkAction_actionPerformed_withURI_callsBrowse() throws IOException {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URI testUri = URI.create("https://example.com");
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUri);

        action.actionPerformed(null);

        verify(mockBrowser).browse(testUri);
    }

    @Test
    void testBrowseHyperlinkAction_actionPerformed_withURL_callsBrowse() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        URL testUrl = new URL("https://example.com");
        HyperlinkUtil.BrowseAction action = HyperlinkUtil.BrowseAction.of(testUrl);

        action.actionPerformed(null);

        verify(mockBrowser).browse(testUrl.toURI());
    }

    @Test
    void testBrowseHyperlinkAction_actionPerformed_withString_callsBrowse() throws Exception {
        when(mockBrowser.isBrowsingSupported()).thenReturn(true);
        String testUrlString = "https://example.com";
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
        assertDoesNotThrow(() -> HyperlinkUtil.isBrowsingSupported());
    }

    @Test
    void testSystemDesktopBrowser_isBrowsingSupported() {
        // Create a real SystemDesktopBrowser to verify it doesn't throw
        HyperlinkUtil.SystemDesktopBrowser browser = new HyperlinkUtil.SystemDesktopBrowser();
        assertDoesNotThrow(browser::isBrowsingSupported);
    }
}

