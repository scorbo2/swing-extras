package ca.corbett.extras.dirtree;

/**
 * Immutable value object holding display options for a {@link DirTree}.
 * Use the {@link Builder} to construct instances.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public final class DirTreeOptions {

    private final boolean showHiddenFiles;
    private final boolean showFiles;

    private DirTreeOptions(Builder builder) {
        this.showHiddenFiles = builder.showHiddenFiles;
        this.showFiles = builder.showFiles;
    }

    /**
     * Whether hidden files/directories should be shown in the tree.
     * On Linux this means files/directories whose name starts with '.'.
     * On Windows this means files/directories with the hidden attribute set.
     *
     * @return true if hidden entries should be shown
     */
    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    /**
     * Whether regular files (non-directories) should be shown as leaf nodes
     * inside each directory node.
     *
     * @return true if files should be shown alongside directories
     */
    public boolean isShowFiles() {
        return showFiles;
    }

    /**
     * Returns a new {@link Builder} pre-populated with the values of this instance,
     * allowing easy "copy with modification" construction.
     *
     * @return a builder seeded with the current values
     */
    public Builder toBuilder() {
        return new Builder()
                .showHiddenFiles(showHiddenFiles)
                .showFiles(showFiles);
    }

    @Override
    public String toString() {
        return "DirTreeOptions{showHiddenFiles=" + showHiddenFiles + ", showFiles=" + showFiles + "}";
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Builder for {@link DirTreeOptions}.
     */
    public static final class Builder {

        // Defaults
        private boolean showHiddenFiles = false;
        private boolean showFiles = false;

        public Builder showHiddenFiles(boolean showHiddenFiles) {
            this.showHiddenFiles = showHiddenFiles;
            return this;
        }

        public Builder showFiles(boolean showFiles) {
            this.showFiles = showFiles;
            return this;
        }

        public DirTreeOptions build() {
            return new DirTreeOptions(this);
        }
    }
}

