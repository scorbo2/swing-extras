# Copilot Instructions for swing-extras

## Repository Overview

**swing-extras** is a Java Swing component library providing custom UI components and utilities for Java desktop applications. The library includes form fields, property management, extensions system, image/audio utilities, and a comprehensive demo application.

- **Type**: Maven-based Java library
- **Language**: Java 17
- **Size**: ~219 source files, ~88 test files
- **Main Output**: JAR library + runnable demo application
- **Version**: 2.7.0 is in production
- **License**: MIT

## Key Dependencies and Frameworks

- **Java**: 17 (required - do not change)
- **Maven**: 3.9.11+
- **Testing**: JUnit 5.12.1, Mockito 5.14.2
- **UI Libraries**: FlatLaf 3.6, JTattoo 1.6.13
- **Serialization**: Gson 2.12.1

## Build and Test Instructions

### Prerequisites

**CRITICAL**: Tests require a graphical environment. The project uses Swing components that need X11/display access.

### Building the Project

**Always run commands in this order:**

1. **Clean the project**:
   ```bash
   mvn clean
   ```
   Takes ~5 seconds. Always succeeds.

2. **Compile the code**:
   ```bash
   mvn compile
   ```
   Takes ~10 seconds. Compiles 189 Java source files to `target/classes/`.

3. **Run tests** (requires xvfb-run):
   ```bash
   xvfb-run mvn test
   ```
   - Takes ~12 seconds
   - Runs 1325 tests (3 tests are skipped - this is normal)
   - **MUST** use `xvfb-run` prefix or tests will fail with "No X11 DISPLAY variable was set" errors
   - **WITHOUT** xvfb-run: many errors occur due to headless environment
   - Adding `-Djava.awt.headless=true` does NOT fix the issue - still causes failures

4. **Package the project** (without tests):
   ```bash
   mvn package -DskipTests
   ```
   Takes ~15 seconds. Creates:
   - `target/swing-extras-2.7.0.jar` (1MB - library only)
   - `target/swing-extras-2.7.0-jar-with-dependencies.jar` (3.7MB - runnable with demo)
   - `target/swing-extras-2.7.0-sources.jar` (794KB)
   - `target/swing-extras-2.7.0-javadoc.jar` (1.8MB)

5. **Full build with tests**:
   ```bash
   xvfb-run mvn clean package
   ```
   Takes ~23 seconds. This is the recommended full validation command.

### Common Issues and Workarounds

**Issue**: Tests fail with "No X11 DISPLAY variable was set"
**Solution**: Always use `xvfb-run` prefix: `xvfb-run mvn test` or `xvfb-run mvn package`

**Issue**: `mvn verify` fails with GPG signing errors
**Solution**: This is expected - signing is for Maven Central publishing. Use `mvn verify -Dgpg.skip=true` or just use `mvn package` for local builds.

**Issue**: Compilation warnings about "unchecked or unsafe operations"
**Solution**: This is normal and does not affect the build. The warnings are from generic type usage in FormField.java.

### Running the Demo Application

After building with `mvn package`:
```bash
java -jar target/swing-extras-2.7.0-jar-with-dependencies.jar
```
Note: Requires a graphical environment. Main class: `ca.corbett.extras.demo.DemoAppLauncher`

### No Linting or Code Style Tools

This project does NOT have:
- Checkstyle configurations
- SpotBugs configurations  
- PMD configurations
- Any automated linting tools

Code style is controlled by `.editorconfig` with these key settings:
- Indent: 4 spaces
- Max line length: 120 characters
- Charset: UTF-8
- End of line: LF

## Project Structure

### Root Directory Files
```
.editorconfig       - Code style configuration (IntelliJ format)
.gitignore         - Excludes target/, .idea/, out/, build/
LICENSE            - MIT License
README.md          - Project documentation
pom.xml            - Maven build configuration
demo-app.png       - Screenshot of demo application
```

### Source Directory Structure

```
src/
├── main/
│   ├── java/ca/corbett/
│   │   ├── forms/              - Form components and validators (core)
│   │   │   ├── actions/        - Pre-built Actions for ListField (4 files)
│   │   │   ├── fields/         - FormField implementations (46 files)
│   │   │   ├── validators/     - Field validation logic (11 files)
│   │   │   └── demo/           - Form demo examples
│   │   ├── extensions/         - Extension system (pluggable modules)
│   │   │   ├── ui/             - Extension management UI
│   │   │   └── demo/           - Extension demos
│   │   ├── extras/             - Utility components
│   │   │   ├── about/          - About dialog
│   │   │   ├── audio/          - Audio waveform visualization
│   │   │   ├── crypt/          - Cryptography utilities (hashing, signing)
│   │   │   ├── demo/           - Main demo application
│   │   │   ├── dirtree/        - Directory tree component
│   │   │   ├── gradient/       - Gradient utilities
│   │   │   ├── image/          - Image utilities and panels
│   │   │   ├── io/             - File system utilities
│   │   │   ├── logging/        - Log console component
│   │   │   ├── progress/       - Progress dialogs
│   │   │   └── properties/     - Properties management system
│   │   └── updates/            - Update manager for extensions
│   └── resources/
│       ├── swing-extras/       - Images, audio files, release notes
│       └── ca/corbett/swing-forms/ - Form field icons and resources
└── test/
    └── java/ca/corbett/        - JUnit 5 tests (88 test classes)
```

### Key Source Files

**Entry Points**:
- `src/main/java/ca/corbett/extras/demo/DemoAppLauncher.java` - Main demo application (36 lines)
- `src/main/java/ca/corbett/extras/demo/DemoApp.java` - Demo UI implementation

**Largest/Most Complex Files**:
- `AudioWaveformPanel.java` (1,202 lines) - Audio visualization
- `ImagePanel.java` (934 lines) - Image display component
- `ExtensionManager.java` (914 lines) - Extension loading/management
- `UpdateManager.java` (588 lines) - Extension update system
- `ImageUtil.java` (635 lines) - Image manipulation utilities

**Core Abstractions**:
- `FormField.java` - Base class for all form fields
- `FormPanel.java` - Container for form fields with layout
- `AbstractProperty.java` (573 lines) - Base for property management
- `FieldValidator.java` - Interface for field validation

### Configuration Files

**Maven** (`pom.xml`):
- Compiler source/target: Java 17
- Main plugins: maven-compiler-plugin, maven-surefire-plugin, maven-assembly-plugin, maven-javadoc-plugin, maven-source-plugin, maven-gpg-plugin
- Assembly creates jar-with-dependencies for demo app
- Main class: `ca.corbett.extras.demo.DemoAppLauncher`

**Git** (`.gitignore`):
- Excludes: `target/`, `.idea/` (partial), `out/`, `build/`, `.DS_Store`
- IDE configs for IntelliJ, Eclipse, NetBeans, VS Code

**No CI/CD**: This repository has NO GitHub Actions workflows or CI configuration files.

## Architecture and Design Patterns

### Form System (`ca.corbett.forms`)
- **Pattern**: Builder/Factory pattern for forms
- **Key Classes**: `FormPanel` (container), `FormField` (base), field implementations
- **Validators**: Pluggable validation with `FieldValidator` interface
- **Layout**: Automatic grid layout based on field metadata

### Properties System (`ca.corbett.extras.properties`)
- **Pattern**: Observer pattern for property changes
- **Key Classes**: `AbstractProperty` (base), `PropertiesManager` (registry)
- **Integration**: Properties can auto-generate FormFields
- **Persistence**: Properties save/load to/from files

### Extension System (`ca.corbett.extensions`, `ca.corbett.updates`)
- **Pattern**: Plugin architecture with dynamic loading
- **Discovery**: JAR scanning at runtime from configured directories
- **Updates**: Automatic update checking and downloading via `UpdateManager`
- **Security**: Digital signature verification for downloaded extensions

## Common Development Patterns

### Adding New FormField Types
1. Extend `FormField<T>` in `ca.corbett.forms.fields/`
2. Implement `getValue()`, `setValue()`, `render()` methods
3. Add corresponding test in `src/test/java/ca/corbett/forms/fields/`
4. Tests should extend `FormFieldBaseTests` for standard behavior

### Adding New Properties
1. Extend `AbstractProperty<T>` in `ca.corbett.extras.properties/`
2. Override `generateFormField()` to specify UI representation
3. Add test extending `AbstractPropertyBaseTests`

### Testing GUI Components
- All Swing component tests require `xvfb-run` in CI/headless environments
- Tests use JUnit 5 with `@BeforeEach`, `@Test` annotations
- Mockito available for mocking dependencies

## Important Notes

### Maven Behavior
- Maven downloads dependencies on first run (~5-10 seconds)
- Subsequent builds use local Maven cache (~/.m2/repository)
- Use `-DskipTests` to skip tests during development for faster builds
- GPG signing (verify phase) will fail without configured keys - this is expected for local development

### Java Version Requirements
- **Java 17 is required** - set in pom.xml as `maven.compiler.source` and `maven.compiler.target`
- Do not change Java version without testing all Swing components
- Modern Java features (records, pattern matching) are NOT widely used in this codebase

### Test Execution
- **ALWAYS** use `xvfb-run mvn test` for running tests
- Never use `-Djava.awt.headless=true` - it doesn't work for Swing tests
- 3 skipped tests is normal (likely environment-dependent tests)
- Test execution time: ~12 seconds

### Code Modification Guidelines
- Follow existing code style (4-space indents, 120-char lines)
- Add Javadoc comments for public APIs (existing standard)
- Include unit tests for new FormFields and Properties
- Release notes are in `src/main/resources/swing-extras/releaseNotes.txt`

## Trust These Instructions

These instructions have been validated by running all commands successfully. If you encounter different behavior:
1. Verify you're using Java 17 and Maven 3.9.11+
2. Ensure you're in the project root directory
3. Check that `xvfb-run` is available for test execution
4. Only search for additional information if these instructions are incomplete or incorrect for your specific task
