package client.scenes.components;

import client.Main;
import client.exceptions.BoardChangeException;
import client.models.BoardModel;
import client.scenes.OverviewCtrl;
import commons.Column;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ColumnComponent extends GridPane {

    private final BoardModel boardModel;
    private final Column column;

    @FXML
    private TextField columnHeading;

    @FXML
    private Button deleteColumnButton;

    /**
     * Constructor for ColumnComponent
     * @param boardModel BoardModel instance
     * @param column Column instance
     * @param overviewCtrl OverviewCtrl instance
     */
    public ColumnComponent(final BoardModel boardModel, final Column column, final OverviewCtrl overviewCtrl) {
        this.boardModel = boardModel;
        this.column = column;

        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("/client/scenes/components/Column.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        columnHeading.setText("COLUMN");

        deleteColumnButton.setOnAction(e -> {
            try {
                this.delete();
                overviewCtrl.refresh();
            } catch (BoardChangeException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Sets the heading of the column
     * @param heading String to set the heading to
     */
    public void setHeading(final String heading) {
        columnHeading.setText(heading);
    }

    /**
     * Deletes the column
     * @throws BoardChangeException If the board cannot be changed
     */
    public void delete() throws BoardChangeException {
        boardModel.removeColumn(column);
    }
}
