package client.services;

import client.exceptions.BoardChangeException;
import client.models.BoardModel;
import client.scenes.MainCtrl;
import commons.Board;
import commons.Card;
import commons.Column;
import commons.exceptions.ColumnNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardServiceTest {

    private BoardService boardService;

    private BoardModel boardModel;
    private MainCtrl mainCtrl;

    class MockServerService extends ServerService {
        @Override
        public Board addBoard(Board board) {
            return board;
        }

        @Override
        public Board getBoard(String joinKey) {
            return new Board("join-key", "title", "password", new TreeSet<>());
        }

        @Override
        public Column addColumn(Board board, Column column) {
            return column;
        }

        @Override
        public Column removeColumn(Board board, Column column) {
            return column;
        }

        @Override
        public Card addCard(Board board, Column column, Card card) {
            return card;
        }

        @Override
        public Card removeCard(Board board, Column column, Card card) {
            return card;
        }
    }

    class MockMainCtrl extends MainCtrl {
        @Override
        public void refreshOverview() { return; }
    }

    @BeforeEach
    public void setup() {
        this.boardModel = new BoardModel();
        MockServerService serverService = new MockServerService();
        this.mainCtrl = new MockMainCtrl();
        this.boardService = new BoardService(boardModel, serverService, mainCtrl);
    }

    @Test
    public void testAddBoard() throws BoardChangeException {
        Board board = new Board("join-key", "title", "password", new TreeSet<>());
        Board returnedBoard = boardService.addBoard(board);
        boardModel.setCurrentBoard(board);

        assertEquals(board, returnedBoard);
        assertEquals(board, boardModel.getCurrentBoard());
    }

    @Test
    public void testFetchBoard() throws BoardChangeException {
        Board board = boardService.fetchBoard("join-key");
        boardModel.setCurrentBoard(board);

        assertEquals(board, boardModel.getCurrentBoard());
    }

    @Test
    public void testCurrentBoard() {
        Board currentBoard = new Board("join-key", "title", "password", null);
        boardModel.setCurrentBoard(currentBoard);

        assertEquals(boardModel.getCurrentBoard(), currentBoard);
    }

    @Test
    public void testAddColumn() throws BoardChangeException {
        Board currentBoard = new Board("join-key", "title", "password", new TreeSet<>());
        boardModel.setCurrentBoard(currentBoard);

        Column column = new Column("heading", 0, new TreeSet<>());
        boardService.updateAddColumnToCurrentBoard(column);

        assertEquals(currentBoard.getColumns().size(), 1);
        assertEquals(currentBoard.getColumns().first(), column);
    }

    @Test
    public void testRemoveColumn() throws BoardChangeException, ColumnNotFoundException {
        Board currentBoard = new Board("join-key", "title", "password", new TreeSet<>());
        boardModel.setCurrentBoard(currentBoard);

        Column column = new Column("heading", 0, new TreeSet<>());
        currentBoard.addColumn(column);

        boardService.updateRemoveColumnFromCurrentBoard(column.getId());

        assertEquals(currentBoard.getColumns().size(), 0);
    }

    @Test
    public void testAddCard() throws BoardChangeException {
        Board currentBoard = new Board("join-key", "title", "password", new TreeSet<>());
        boardModel.setCurrentBoard(currentBoard);

        Column column = new Column("heading", 0, new TreeSet<>());
        currentBoard.addColumn(column);

        Card card = new Card("title", 0, "description", null);
        boardService.updateAddCardToColumn(card, column);

        assertEquals(column.getCards().size(), 1);
        assertEquals(column.getCards().first(), card);
    }

    @Test
    public void testRemoveCard() throws BoardChangeException {
        Board currentBoard = new Board("join-key", "title", "password", new TreeSet<>());
        boardModel.setCurrentBoard(currentBoard);

        Column column = new Column("heading", 0, new TreeSet<>());
        currentBoard.addColumn(column);

        Card card = new Card("title", 0, "description", new TreeSet<>());
        column.addCard(card);

        boardService.updateRemoveCardFromColumn(card, column);

        assertEquals(column.getCards().size(), 0);
    }

}
