package ca.corbett.extras.dirtree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirTreeTest {

    @Test
    public void setShowHiddenDirs_withNoChange_shouldNotNotifyListeners() {
        // GIVEN a DirTree with a listener that tracks notifications:
        DirTree tree = new DirTree();
        DirTreeListener listenerMock = Mockito.mock(DirTreeListener.class);
        tree.addDirTreeListener(listenerMock);

        // WHEN we set show hidden dirs to its current value:
        tree.setShowHiddenDirs(tree.getShowHiddenDirs()); // no-op should be ignored

        // THEN the listener should not be notified:
        Mockito.verify(listenerMock, Mockito.never()).showHiddenFilesChanged(Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void setShowHiddenDirs_withChange_shouldNotifyListeners() {
        // GIVEN a DirTree with a listener that tracks notifications:
        DirTree tree = new DirTree();
        DirTreeListener listenerMock = Mockito.mock(DirTreeListener.class);
        tree.addDirTreeListener(listenerMock);

        // WHEN we change the show hidden dirs setting:
        boolean newValue = !tree.getShowHiddenDirs();
        tree.setShowHiddenDirs(newValue);

        // THEN the listener should be notified of the change:
        Mockito.verify(listenerMock, Mockito.times(1)).showHiddenFilesChanged(tree, newValue);
    }

    @Test
    public void setShowHidden_withNoChange_shouldNotNotifyListeners() {
        DirTree tree = new DirTree();
        DirTreeListener listenerMock = Mockito.mock(DirTreeListener.class);
        tree.addDirTreeListener(listenerMock);

        tree.setShowHidden(tree.getShowHidden());

        Mockito.verify(listenerMock, Mockito.never()).showHiddenFilesChanged(Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void setShowHidden_withChange_shouldNotifyListeners() {
        DirTree tree = new DirTree();
        DirTreeListener listenerMock = Mockito.mock(DirTreeListener.class);
        tree.addDirTreeListener(listenerMock);

        boolean newValue = !tree.getShowHidden();
        tree.setShowHidden(newValue);

        Mockito.verify(listenerMock, Mockito.times(1)).showHiddenFilesChanged(tree, newValue);
    }

    @Test
    public void showFiles_defaultIsFalse() {
        DirTree tree = new DirTree();
        assertFalse(tree.getShowFiles());
    }

    @Test
    public void setShowFiles_changesValue() {
        DirTree tree = new DirTree();
        tree.setShowFiles(true);
        assertTrue(tree.getShowFiles());
    }

    @Test
    public void fileFilter_defaultIsNull() {
        DirTree tree = new DirTree();
        assertNull(tree.getFileFilter());
    }

    @Test
    public void setFileFilter_changesValue() {
        DirTree tree = new DirTree();
        FileFilter filter = f -> f.getName().endsWith(".txt");
        tree.setFileFilter(filter);
        assertEquals(filter, tree.getFileFilter());
    }

    @Test
    public void dirTreeNode_isFileNode_forDirectory(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        DirTreeNode node = new DirTreeNode(dir, true, false, null);
        assertFalse(node.isFileNode());
    }

    @Test
    public void dirTreeNode_isFileNode_forFile(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("test.txt").toFile();
        assertTrue(file.createNewFile());
        DirTreeNode node = new DirTreeNode(file, true, false, null);
        assertTrue(node.isFileNode());
        assertTrue(node.isLeaf());
    }

    @Test
    public void dirTreeNode_loadChildren_includesFilesWhenEnabled(@TempDir Path tempDir) throws IOException {
        // Create a directory structure with files
        File subDir = new File(tempDir.toFile(), "subdir");
        assertTrue(subDir.mkdir());
        File file1 = new File(tempDir.toFile(), "file1.txt");
        assertTrue(file1.createNewFile());
        File file2 = new File(tempDir.toFile(), "file2.txt");
        assertTrue(file2.createNewFile());

        // With showFiles=true, loadChildren should include files
        DirTreeNode node = new DirTreeNode(tempDir.toFile(), true, true, null);
        node.loadChildren();

        // Should have 3 children: 1 dir + 2 files
        assertEquals(3, node.getChildCount());

        // First child should be the directory
        DirTreeNode firstChild = (DirTreeNode)node.getChildAt(0);
        assertFalse(firstChild.isFileNode());
        assertEquals("subdir", firstChild.getDir().getName());

        // Remaining children should be files
        DirTreeNode secondChild = (DirTreeNode)node.getChildAt(1);
        assertTrue(secondChild.isFileNode());
        DirTreeNode thirdChild = (DirTreeNode)node.getChildAt(2);
        assertTrue(thirdChild.isFileNode());
    }

    @Test
    public void dirTreeNode_loadChildren_excludesFilesWhenDisabled(@TempDir Path tempDir) throws IOException {
        File subDir = new File(tempDir.toFile(), "subdir");
        assertTrue(subDir.mkdir());
        File file1 = new File(tempDir.toFile(), "file1.txt");
        assertTrue(file1.createNewFile());

        // With showFiles=false, loadChildren should only include directories
        DirTreeNode node = new DirTreeNode(tempDir.toFile(), true, false, null);
        node.loadChildren();

        assertEquals(1, node.getChildCount());
        DirTreeNode child = (DirTreeNode)node.getChildAt(0);
        assertFalse(child.isFileNode());
    }

    @Test
    public void dirTreeNode_loadChildren_appliesFileFilter(@TempDir Path tempDir) throws IOException {
        File file1 = new File(tempDir.toFile(), "readme.txt");
        assertTrue(file1.createNewFile());
        File file2 = new File(tempDir.toFile(), "image.png");
        assertTrue(file2.createNewFile());

        FileFilter txtFilter = f -> f.getName().endsWith(".txt");
        DirTreeNode node = new DirTreeNode(tempDir.toFile(), true, true, txtFilter);
        node.loadChildren();

        // Only the .txt file should be included
        assertEquals(1, node.getChildCount());
        DirTreeNode child = (DirTreeNode)node.getChildAt(0);
        assertTrue(child.isFileNode());
        assertEquals("readme.txt", child.getDir().getName());
    }

    @Test
    public void dirTreeNode_hasChildren_considersFilesWhenEnabled(@TempDir Path tempDir) throws IOException {
        // A directory with only files (no subdirs) should have children when showFiles=true
        File file1 = new File(tempDir.toFile(), "file1.txt");
        assertTrue(file1.createNewFile());

        DirTreeNode nodeWithFiles = new DirTreeNode(tempDir.toFile(), true, true, null);
        assertTrue(nodeWithFiles.hasChildren());

        DirTreeNode nodeWithoutFiles = new DirTreeNode(tempDir.toFile(), true, false, null);
        assertFalse(nodeWithoutFiles.hasChildren());
    }

    @Test
    public void dirTreeNode_hasChildren_respectsFileFilter(@TempDir Path tempDir) throws IOException {
        File file1 = new File(tempDir.toFile(), "image.png");
        assertTrue(file1.createNewFile());

        FileFilter txtFilter = f -> f.getName().endsWith(".txt");
        DirTreeNode node = new DirTreeNode(tempDir.toFile(), true, true, txtFilter);
        assertFalse(node.hasChildren()); // only .png file, but filter is for .txt
    }

    @Test
    public void dirTreeNode_fileNode_hasNoChildren(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("test.txt").toFile();
        assertTrue(file.createNewFile());
        DirTreeNode node = new DirTreeNode(file, true, true, null);
        assertFalse(node.hasChildren());
        node.loadChildren(); // should be a no-op for files
        assertEquals(0, node.getChildCount());
    }

    @Test
    public void dirTreeNode_isShowHidden_returnsCorrectValue(@TempDir Path tempDir) {
        DirTreeNode nodeTrue = new DirTreeNode(tempDir.toFile(), true, false, null);
        assertTrue(nodeTrue.isShowHidden());
        assertTrue(nodeTrue.isShowHiddenDirs()); // deprecated method still works

        DirTreeNode nodeFalse = new DirTreeNode(tempDir.toFile(), false, false, null);
        assertFalse(nodeFalse.isShowHidden());
        assertFalse(nodeFalse.isShowHiddenDirs()); // deprecated method still works
    }

    @Test
    public void dirTree_lock_includesFilesWhenShowFilesEnabled(@TempDir Path tempDir) throws IOException {
        File subDir = new File(tempDir.toFile(), "subdir");
        assertTrue(subDir.mkdir());
        File file1 = new File(tempDir.toFile(), "file1.txt");
        assertTrue(file1.createNewFile());

        DirTree tree = new DirTree();
        tree.setShowFiles(true);
        tree.lock(tempDir.toFile());

        assertNotNull(tree.getLockDir());

        // Verify the tree model root contains both the directory and the file
        DirTreeNode root = (DirTreeNode)tree.getTree().getModel().getRoot();
        assertEquals(2, root.getChildCount());

        DirTreeNode dirChild = (DirTreeNode)root.getChildAt(0);
        assertFalse(dirChild.isFileNode());
        assertEquals("subdir", dirChild.getDir().getName());

        DirTreeNode fileChild = (DirTreeNode)root.getChildAt(1);
        assertTrue(fileChild.isFileNode());
        assertEquals("file1.txt", fileChild.getDir().getName());
    }

    @Test
    public void dirTree_lock_filtersFilesWithFileFilter(@TempDir Path tempDir) throws IOException {
        File txtFile = new File(tempDir.toFile(), "readme.txt");
        assertTrue(txtFile.createNewFile());
        File pngFile = new File(tempDir.toFile(), "image.png");
        assertTrue(pngFile.createNewFile());

        DirTree tree = new DirTree();
        tree.setShowFiles(true);
        tree.setFileFilter(f -> f.getName().endsWith(".txt"));
        tree.lock(tempDir.toFile());

        assertNotNull(tree.getLockDir());

        // Only the .txt file should be present; .png should be filtered out
        DirTreeNode root = (DirTreeNode)tree.getTree().getModel().getRoot();
        assertEquals(1, root.getChildCount());

        DirTreeNode child = (DirTreeNode)root.getChildAt(0);
        assertTrue(child.isFileNode());
        assertEquals("readme.txt", child.getDir().getName());
    }

    @Test
    public void dirTreeAdapter_fileDoubleClicked_isNoOp() {
        // Verify the adapter provides a no-op default implementation
        DirTreeAdapter adapter = new DirTreeAdapter();
        adapter.fileDoubleClicked(null, null); // should not throw
    }

    @Test
    public void deprecatedSetShowHiddenDirs_delegatesToSetShowHidden() {
        DirTree tree = new DirTree();
        tree.setShowHiddenDirs(false);
        assertFalse(tree.getShowHidden());
        assertFalse(tree.getShowHiddenDirs());

        tree.setShowHiddenDirs(true);
        assertTrue(tree.getShowHidden());
        assertTrue(tree.getShowHiddenDirs());
    }
}
