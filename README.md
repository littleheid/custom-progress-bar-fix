<div align="center">
    <img src="./src/main/resources/META-INF/gitHubIcon.svg" width="1280" height="640"/>
</div>

# Transform your IDE experience with the 'Custom Progress Bar Fix' plugin for IDEA based IDEs. Pick your colors, make progress unique, and code with style!

## Notice
- This repository is a modified/fix variant based on the original open source project by Drew Underwood.
- Original project: https://github.com/Drewzillawood/intellij-custom-progress-bar
- This repository keeps the original MIT License and original copyright notice.
- This repository is not published on JetBrains Marketplace. Install it by building from source and loading the generated plugin ZIP manually.

## Changelog
- [Change log](https://github.com/Drewzillawood/intellij-custom-progress-bar/blob/main/CHANGELOG.md)

## Build and install
1. Clone this repository.
2. Build the plugin ZIP:
   ```bash
   bash ./gradlew buildPlugin
   ```
3. Find the generated ZIP under `build/distributions/`.
4. Install it in IntelliJ IDEA from disk:
   - Windows & Linux: <kbd>File</kbd> | <kbd>Settings</kbd> | <kbd>Plugins</kbd> | <kbd>⚙</kbd> | <kbd>Install Plugin from Disk...</kbd>
   - Mac: <kbd>IntelliJ IDEA</kbd> | <kbd>Preferences</kbd> | <kbd>Plugins</kbd> | <kbd>⚙</kbd> | <kbd>Install Plugin from Disk...</kbd>
5. Select the generated ZIP and restart the IDE.
