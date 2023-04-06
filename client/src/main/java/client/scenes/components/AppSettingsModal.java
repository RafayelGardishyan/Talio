package client.scenes.components;

import client.Main;
import client.scenes.HomePageCtrl;
import client.services.BoardService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;

public class AppSettingsModal extends Modal {

    private final HomePageCtrl parentCtrl;

    @FXML
    private Text serverURL;

    @FXML
    private Button disconnectFromServer;

    @FXML
    private Button clearCache;

    @FXML
    private Button adminButton;

    @FXML
    private Button exit;


    /**
     * Constructor for AppSettingsModal
     * @param boardService BoardService
     * @param parentScene Parent Scene
     * @param parentCtrl Parent ctrl (HomePageCtrl)
     */
    public AppSettingsModal (final BoardService boardService, final Scene parentScene, final HomePageCtrl parentCtrl) {
        super(boardService, parentScene);
        this.parentCtrl = parentCtrl;

        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("/components/AppSettingsModal.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the settings modal
     */
    @Override
    @FXML
    public void initialize() {
        super.initialize();
        this.serverURL.setText("Server URL"); // TBD - replace with actual url
        this.adminButton.setText(Main.isAdmin() ? "Disable God Mode" : "Enable God Mode");
    }

    /**
     * overriden closeModal method that also refreshes Homepage to reflect any changes made
     */
    @Override
    @FXML
    public void closeModal() {
        super.closeModal();
        parentCtrl.refresh();
    }

    /**
     * Disconnect from server
     */
    @FXML
    public void onDisconnectButtonClick () {
        parentCtrl.onDisconnectButtonClick();
    }

    /**
     * deleted saved-boards
     */
    @FXML
    public void deleteSavedBoards () {

    }

    /**
     * switch to god mode
     */
    @FXML
    public void adminMode () {
        if (Main.isAdmin()) { // If user is already in admin mode, then disable it
            Main.setAdmin(false);
            adminButton.setText("Enable God Mode");
        }
        else {
            final AdminPasswordModal adminPasswordModal = new AdminPasswordModal(boardService,
                    parentCtrl.getMainCtrl().getCurrentScene(), parentCtrl);
            adminPasswordModal.showModal();
        }
    }

    /**
     * close the application
     */
    @FXML
    public void exitApplication () {

    }

    /**
     * copy server url to clipboard
     */
    @FXML
    public void onUrlClick() {
        final Point2D p = this.serverURL.localToScreen(110, -32);

        final Tooltip customTooltip = new Tooltip("Copied server URL to clipboard!");
        customTooltip.setStyle("-fx-font-size: 11px");
        customTooltip.setAutoHide(false);
        customTooltip.show(this.serverURL,p.getX(),p.getY());

        final PauseTransition pt = new PauseTransition(Duration.millis(750));
        pt.setOnFinished(e -> {
            customTooltip.hide();
        });
        pt.play();

        final StringSelection serverUrlSelection = new StringSelection("TBD - server url");
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(serverUrlSelection, null);

    }
}
