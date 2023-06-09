package client.scenes.components.modals;

import client.Main;
import client.scenes.OverviewCtrl;
import client.scenes.Refreshable;
import client.scenes.components.ColorPresetComponent;
import client.scenes.components.UIComponent;
import client.services.BoardService;
import commons.Board;
import commons.Color;
import commons.ColorScheme;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.Set;

public class ColorPresetsOverviewModal extends Modal implements UIComponent, Refreshable {


    @FXML
    private VBox colorPresetsContainer;

    @FXML
    private Button addColorPresetsButton;

    /**
     * Constructor for BoardSettingsModal
     * @param boardService boardService instance
     * @param parentScene parent scene (displayed under modal)
     */
    public ColorPresetsOverviewModal(final BoardService boardService, final Scene parentScene) {
        super(boardService, parentScene);

        loadSource(Main.class.getResource("/components/ColorPresetsOverviewModal.fxml"));

        this.refreshLock();
    }

    /**
     * Initialize modal
     */
    @FXML
    public void initialize() {
        super.initialize();
        this.renderColorPresets();
    }

    @FXML
    private void onAddColorPresetButtonClick() {
        final ColorScheme colorPreset = new ColorScheme("New Color Preset", new Color(colorGenerator()), new Color(colorGenerator()));
        boardService.addColorPresetToCurrentBoard(colorPreset);

        refresh();
    }

    /**
     * Refreshes the modal
     */
    public void refresh() {
        this.renderColorPresets();
        this.refreshLock();
    }

    private void refreshLock() {
        if (OverviewCtrl.isLocked()) {
            addColorPresetsButton.setDisable(true);
            addColorPresetsButton.setVisible(false);
        } else {
            addColorPresetsButton.setDisable(false);
            addColorPresetsButton.setVisible(true);
        }
    }

    private void renderColorPresets() {
        colorPresetsContainer.getChildren().clear();

        final Board currentBoard = boardService.getCurrentBoard();
        final Set<ColorScheme> colorPresets = currentBoard.getColorPresets();

        for (final ColorScheme colorPreset : colorPresets) {
            final ColorPresetComponent colorPresetComponent = new ColorPresetComponent(boardService, parentScene, this, colorPreset);
            colorPresetsContainer.getChildren().add(colorPresetComponent);
        }
    }

    /**
     * Returns a random color for a tag
     * @return String containing hex code
     */
    private String colorGenerator() {
        final String [] colors = {"#2196F3", "#92D36E", "#FF3823", "#92D36E",
            "#00FFFF", "#FF0000", "#ff9a00", "#694130", "#5A5A82" };

        return colors [(int) (Math.random() * colors.length)];
    }
}
