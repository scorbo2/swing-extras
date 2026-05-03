package ca.corbett.extras.io;

import ca.corbett.extras.progress.SimpleProgressWorker;

/**
 * A handy utility class that can wrap the various find methods in FileSystemUtil
 * in the form of a worker thread that can be easily wired up to a MultiProgressDialog.
 * This saves a modest amount of work for client applications that just want to find
 * files or directories in a thread-safe way (that is, without blocking the UI thread).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0
 */
public class ScannerThread extends SimpleProgressWorker {

    // TODO implement me

    @Override
    public void run() {

    }
}
