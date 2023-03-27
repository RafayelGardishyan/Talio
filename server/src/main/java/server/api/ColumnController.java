package server.api;

import commons.Board;
import commons.Card;
import commons.Column;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.services.BoardService;
import java.util.TreeSet;

@Controller
@RequestMapping("/columns")
public class ColumnController {

    private final BoardService boardService;

    private final SimpMessageSendingOperations messagingTemplate;
    private final Logger logger = LogManager.getLogger(ColumnController.class);

    /**
     * Constructor for the Column Controller
     *
     * @param boardService      Dependency injection for the board service
     * @param messagingTemplate Template to send updates over socket
     */
    public ColumnController(final BoardService boardService, final SimpMessageSendingOperations messagingTemplate) {
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
    }


    /**
     * Add a Column
     * @param joinKey Key used to identify board
     * @param columnHeading Heading of column to be added
     * @param password Password to board
     * @param index Index of column to be added
     * @return The Column added to the ColumnRepository
     */
    @PostMapping("/create/{joinKey}/{columnHeading}")
    public ResponseEntity<Column> addColumn(@PathVariable final String joinKey, @PathVariable final String columnHeading,
                                            @RequestBody final String password, @RequestParam final int index)
    {

        final Board board = boardService.getBoardWithKeyAndPassword(joinKey, password);


        final Column column = new Column(columnHeading, index, new TreeSet<Card>());

        board.addColumn(column);
        boardService.saveBoard(board);

        updateColumnAdded(joinKey, column);

        return ResponseEntity.ok(column);
    }

    /**
     * Remove a Column
     * @param joinKey Key used to identify board
     * @param columnHeading Heading of column to be removed
     * @param password Password to board
     * @return The Column removed from the ColumnRepository
     */
    @PostMapping("/remove/{joinKey}/{columnHeading}")
    public ResponseEntity<Column> removeColumn(@PathVariable final String joinKey, @PathVariable final String columnHeading,
                                               @RequestBody final String password)
    {

        final Board board = boardService.getBoardWithKeyAndPassword(joinKey, password);

        final Column toBeRemoved = board.getColumnByName(columnHeading);

        board.removeColumn(toBeRemoved);
        boardService.saveBoard(board);

        updateColumnRemoved(joinKey, columnHeading);

        return ResponseEntity.ok(toBeRemoved);
    }

    /**
     * Sends the renamed Column that was added to all clients subscribed to that board and renames in repo
     * @param joinKey String for board
     * @param oldHeading String for old name of column
     * @param newHeading  String for new name of column
     * @param password String password for board
     * @return Column the renamed column
     */
    @MessageMapping("/rename/{joinKey}/{oldHeading}/{newHeading}")
    public Column renameColumn(final String password, @DestinationVariable final String joinKey, @DestinationVariable final String oldHeading,
                               @DestinationVariable final String newHeading)
    {
        final Board board = boardService.getBoardWithKeyAndPassword(joinKey, password);
        final Column toBeRenamed = board.getColumnByName(oldHeading);

        toBeRenamed.setHeading(newHeading);
        boardService.saveBoard(board);

        updateColumnRenamed(joinKey, oldHeading, newHeading);

        return toBeRenamed;
    }

    /**
     * Sends the Column that was added to all clients subscribed to that board
     * @param joinKey String for board
     * @param column Column that was added
     */
    public void updateColumnAdded(final String joinKey, final Column column) {
        logger.info("Propagating column added for: " + joinKey);
        messagingTemplate.convertAndSend("/topic/columns/" + joinKey + "/add", column);
    }

    /**
     * Sends the old and new heading of the column to all subscribed clients
     * @param joinKey String for board
     * @param oldHeading String for old name of column
     * @param newHeading  String for new name of column
     */
    public void updateColumnRenamed(final String joinKey, final String oldHeading, final String newHeading) {
        logger.info("Propagating column renamed for: " + joinKey);
        messagingTemplate.convertAndSend("/topic/columns/" + joinKey + "/rename", new Pair<String, String>(oldHeading, newHeading));
    }

    /**
     * Sends the columnHeading that was removed to all clients subscribed to that board
     * @param joinKey String for board
     * @param columnHeading Column that was removed
     */
    public void updateColumnRemoved(final String joinKey, final String columnHeading) {
        logger.info("Propagating column change to: " + joinKey);
        messagingTemplate.convertAndSend("/topic/columns/" + joinKey + "/remove", columnHeading);
    }
}
