package client.models;

import client.exceptions.BoardChangeException;
import commons.Board;
import commons.Card;
import commons.Column;
import commons.exceptions.ColumnNotFoundException;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class BoardModel {

    @Getter
    @Setter
    private List<Board> boardList;

    @Getter
    @Setter
    private Board currentBoard;


    /**
     * Constructor for BoardModel
     */
    public BoardModel() {
        this.boardList = new LinkedList<>();
        this.currentBoard = null;
    }


    /**
     * Adds card to column
     * @param card Card to be added to column
     * @param col Column to be added to
     */
    public void addCard(final Card card, final Column col) throws BoardChangeException {
        if (!col.addCard(card)) {
            throw new BoardChangeException("Failed to add card : " + card);
        }
    }

    /**
     * Removes card from column
     * @param card Card to be removed from column
     * @param col Column to be removed from
     * @throws BoardChangeException if card is not removed
     */
    public void removeCard(final Card card, final Column col) throws BoardChangeException {
        if (!col.removeCard(card)) {
            throw new BoardChangeException("Failed to remove card : " + card);
        }
    }

    /**
     * Adds column to board in overview
     * @param col Column to be added
     */
    public void addColumn(final Column col) throws BoardChangeException {
        if (!currentBoard.addColumn(col)) {
            throw new BoardChangeException("Failed to add column : " + col);
        }
    }

    /**
     * Deletes column from board
     * @param column Column to be removed
     * @throws BoardChangeException if Column is not deleted
     */
    public void removeColumn(final Column column) throws BoardChangeException {
        if (!currentBoard.removeColumn(column)) {
            throw new BoardChangeException("Failed to delete column : " + column);
        }
    }

    /**
     * Adds board to boardList
     * @param board Board to add
     */
    public void addBoard(final Board board) throws BoardChangeException {
        if (!boardList.add(board)) {
            throw new BoardChangeException("Failed to add board : " + board);
        }
    }

    /**
     * Moves card from one column to another (IMPORTANT: This method is temporary
     * and will be replaced by a method that sends the move to the server and then
     * updates the board accordingly.)
     * @param cardId ID of card to be moved
     * @param columnIdx Index of column to be moved from
     * @param newColumnIdx Index of column to be moved to
     * @param priority Priority of card in new column
     */
    public void moveCard(final long cardId, final long columnIdx, final long newColumnIdx, final int priority) throws BoardChangeException {
        try {
            final Card card = currentBoard.getCard(cardId);
            final Column column = currentBoard.getColumnById(columnIdx);
            final Column newColumn = currentBoard.getColumnById(newColumnIdx);

            if (card != null && column != null && newColumn != null) {
                column.removeCard(card);
                card.setPriority(Integer.MAX_VALUE);
                newColumn.addCard(card);
                newColumn.updateCardPosition(card, priority);
            }
        }
        catch (ColumnNotFoundException e) {
            throw new BoardChangeException(e.getMessage());
        }
    }


    /**
     * Updates column in place (IMPORTANT: This method is temporary
     * and will be replaced by a method that sends the update to
     * the server and then updates the board accordingly.)
     * @param column Column to be updated
     */
    public void updateColumn(final Column column) {
        final Column oldColumn = currentBoard.getColumn(column.getIndex());

        if (oldColumn != null) {
            oldColumn.setHeading(column.getHeading());
            oldColumn.setIndex(column.getIndex());
            oldColumn.setCards(column.getCards());
        }
    }

    /**
     * Rename the board by changing title
     * @param newName String new title
     */
    public void renameBoard(final String newName) {
        currentBoard.setTitle(newName);
    }

    /**
     * Renames column to new name
     * @param column Column to rename
     * @param newName String new name
     */
    public void renameColumn(final Column column, final String newName) {
        final Column oldColumn = currentBoard.getColumn(column.getIndex());
        if (oldColumn != null) oldColumn.setHeading(column.getHeading());
    }
}
