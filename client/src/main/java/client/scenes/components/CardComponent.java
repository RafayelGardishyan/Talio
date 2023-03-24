package client.scenes.components;

import client.Main;
import client.exceptions.BoardChangeException;
import client.models.BoardModel;
import commons.Card;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import lombok.Getter;

import java.io.IOException;

public class CardComponent extends GridPane {
    private final BoardModel boardModel;
    @Getter
    private final Card card;
    @Getter
    private final ColumnComponent columnParent;

    @FXML
    private TextArea cardText;

    @FXML
    private Button editCardButton;

    /**
     * Constructor for CardComponent
     *
     * @param boardModel   BoardModel instance
     * @param card         Card instance
     * @param columnParent ColumnComponent instance
     */
    public CardComponent(final BoardModel boardModel, final Card card, final ColumnComponent columnParent) {
        this.boardModel = boardModel;
        this.card = card;
        this.columnParent = columnParent;

        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("/client/scenes/components/Card.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cardText.setText(card.getTitle());

        editCardButton.setOnAction(e -> cardText.setDisable(false)); // Temporarily enable editing of card text

        cardText.setOnKeyReleased(e -> { // Disable editing of card text when enter is pressed
            if (e.getCode() == KeyCode.ENTER) {
                cardText.setDisable(true);
                card.setTitle(cardText.getText());
                boardModel.updateCard(card);
            }
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                card.setTitle(cardText.getText());
                boardModel.updateCard(card);
                cardText.setDisable(true);
            }
        });

        cardText.textProperty().addListener((observable, oldValue, newValue) -> {
            card.setTitle(cardText.getText());
            boardModel.updateCard(card);
        });


        cardText.setDisable(true); // Disable editing of card text by default

        setUpDragAndDrop();

    }

    private void setUpDragAndDrop() {
        setOnDragDetected(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Start drag-and-drop gesture
                startFullDrag();
                final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                final ClipboardContent content = new ClipboardContent();
                content.putString(
                        String.format("%s:%s", card.getId(), columnParent.getColumn().getIndex()));
                dragboard.setContent(content);
                event.consume();
            }
        });

        setOnDragEntered(event -> {
            // Check if the dragged element is being dragged over the top half of the target element
            if (event.getY() < this.getHeight()) {
                this.setStyle("-fx-border-color: transparent; -fx-border-width: 25px 0 0 0;");
            }
        });

        setOnDragExited(event -> {
            // Remove the top margin from the target element
            this.setStyle("-fx-border-color: transparent; -fx-border-width: 0;");
        });
    }

    /**
     * Deletes the card
     *
     * @throws BoardChangeException If the card could not be deleted
     */
    public void delete() throws BoardChangeException {
        boardModel.removeCard(card, columnParent.getColumn());
    }
}