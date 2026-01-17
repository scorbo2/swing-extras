package ca.corbett.extras.dirtree;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
}
