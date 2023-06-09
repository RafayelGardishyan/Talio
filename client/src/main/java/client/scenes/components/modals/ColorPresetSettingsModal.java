package client.scenes.components.modals;

import client.Main;
import client.scenes.OverviewCtrl;
import client.scenes.Refreshable;
import client.scenes.components.UIComponent;
import client.services.BoardService;
import commons.ColorScheme;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ColorPresetSettingsModal extends Modal implements UIComponent {

    @FXML
    private TextField titleTextField;

    @FXML
    private ColorPicker primaryColor;

    @FXML
    private ColorPicker secondaryColor;

    @FXML
    private Button submitButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Text deleteText;

    private final Refreshable parentCtrl;

    private final ColorScheme colorPreset;

    /**
     * Constructor for BoardSettingsModal
     * @param boardService boardService instance
     * @param parentScene parent scene (displayed under modal)
     * @param parentCtrl parent controller
     * @param colorPreset color preset to edit
     */
    public ColorPresetSettingsModal(final BoardService boardService, final Scene parentScene, final Refreshable parentCtrl,
                                    final ColorScheme colorPreset)
    {
        super(boardService, parentScene);
        this.parentCtrl = parentCtrl;
        this.colorPreset = colorPreset;

        loadSource(Main.class.getResource("/components/ColorPresetSettingsModal.fxml"));

        this.titleTextField.setText(colorPreset.getName());

        final commons.Color backgroundColor = colorPreset.getBackgroundColor();
        final commons.Color textColor = colorPreset.getTextColor();

        final Color primaryColor = Color.rgb(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
                backgroundColor.getAlpha() / 255.0);
        final Color secondaryColor = Color.rgb(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha() / 255.0);

        this.primaryColor.setValue(primaryColor);
        this.secondaryColor.setValue(secondaryColor);

        this.refreshLock();
    }

    private void refreshLock() {
        if (OverviewCtrl.isLocked()) {
            this.titleTextField.setDisable(true);
            this.primaryColor.setDisable(true);
            this.secondaryColor.setDisable(true);
            this.deleteButton.setDisable(true);
            this.submitButton.setDisable(true);

            this.deleteButton.setVisible(false);
            this.deleteText.setVisible(false);
        } else {
            this.titleTextField.setDisable(false);
            this.primaryColor.setDisable(false);
            this.secondaryColor.setDisable(false);
            this.deleteButton.setDisable(false);
            this.submitButton.setDisable(false);

            this.deleteButton.setVisible(true);
            this.deleteText.setVisible(true);
        }
    }

    /**
     * Initialize modal
     */
    @FXML
    public void initialize() {
        super.initialize();
    }


    @FXML
    private void submitModal() {
        if (!OverviewCtrl.isLocked()) {
            final String title = this.titleTextField.getText();
            final Color primaryColor = this.primaryColor.getValue();
            final Color secondaryColor = this.secondaryColor.getValue();

            final ColorScheme newColorPreset = new ColorScheme(
                new commons.Color((int) (secondaryColor.getRed() * 255), (int) (secondaryColor.getGreen() * 255), (int) (secondaryColor.getBlue() * 255),
                    (int) (secondaryColor.getOpacity() * 255)),
                new commons.Color((int) (primaryColor.getRed() * 255), (int) (primaryColor.getGreen() * 255), (int) (primaryColor.getBlue() * 255),
                    (int) (primaryColor.getOpacity() * 255))
            );

            this.colorPreset.setBackgroundColor(newColorPreset.getBackgroundColor());
            this.colorPreset.setTextColor(newColorPreset.getTextColor());
            this.colorPreset.setName(title);

            boardService.editColorPreset(this.colorPreset);
        }
        this.closeModal();
    }

    @FXML
    private void onDelete() {
        boardService.removeColorPresetFromBoard(this.colorPreset);
        parentCtrl.refresh();
        this.closeModal();
    }
}
