package ca.corbett.extras.io;

import java.io.File;

/**
 * A simple interface to be notified of progress during file searches.
 * Unfortunately, a "step X of Y" type notification isn't possible as we don't know
 * up front how many files will be found during the search. So, this notification
 * is just so that callers with a UI can be assured that something is actually going on.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-12-02
 */
public interface FileSearchListener {

    /**
     * Indicates that the given File appeared during a search. (The file may represent
     * a file or a directory, depending on the type of search being performed).
     * Return true from this callback to keep going with the search. Return false to abort
     * it and return whatever was found up to that point.
     *
     * @param file The most recently found File during the search.
     * @return True to continue with the search, false to abort it.
     */
    boolean fileFound(File file);
}
