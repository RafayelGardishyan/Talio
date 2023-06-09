package server.api;


import commons.Board;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import server.services.BoardService;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

@Controller
public class BoardController {
    private final BoardService boardService;

    private final SimpMessageSendingOperations messagingTemplate;
    private final Logger logger = LogManager.getLogger(BoardController.class);

    private final Clock clock;

    /**
     * Constructor for the Board Controller
     *
     * @param boardService      Dependency Injection for the board service
     * @param messagingTemplate Template to send updates over socket
     * @param clock             Dependency Injection for the clock
     */
    public BoardController(final BoardService boardService, final SimpMessageSendingOperations messagingTemplate, final Clock clock) {
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
    }

    /**
     * @return 200 OK if the server is running
     */
    @GetMapping("/checkConnection")
    public ResponseEntity<Void> checkConnection() {
        return ResponseEntity.ok().build();
    }

    /**
     * Returns a Board object with the given join key
     * @param joinKey Join key of the board
     * @param password Optional password of the board
     * @return The board with the right joinKey if the board has the correct password provided, otherwise a
     */
    @GetMapping("/boards/get/{joinKey}")
    public ResponseEntity<Board> getBoard(@PathVariable final String joinKey, @RequestBody(required = false) final String password) {
        try {
            final Board board = password == null ?
                    boardService.getBoardWithKey(joinKey) :
                    boardService.getBoardWithKeyAndPassword(joinKey, password);

            return ResponseEntity.ok(board);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }

    /**
     * Returns a list of boards with the given join keys
     * @param localBoards List of join keys
     * @return List of boards
     */
    @PostMapping("/boards/getAll")
    public ResponseEntity<List<Board>> getAllBoards(@RequestBody final HashMap<String, String> localBoards) {
        try {
            final List<Board> boards = new ArrayList<>();

            for (final String joinKey : localBoards.keySet()) {
                boards.add(boardService.getBoardWithKeyUnsafe(joinKey));
            }

            return ResponseEntity.ok(boards);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }

    /**
     * Creates a {@link Board}
     * @param boardDTO {@link Board} to create
     * @return The {@link Board} that was saved in the {@code BoardRepository}, so the client can ensure data integrity.
     */
    @PostMapping("/boards/create")
    public ResponseEntity<Board> createBoard(@Valid @RequestBody final Board boardDTO) {
        try {
            final String boardJoinKey = boardService.generateJoinKey();

            final Board board = new Board(boardJoinKey, boardDTO.getTitle(), boardDTO.getPassword(), new TreeSet<>(), Timestamp.from(Instant.now(clock)));

            final Board savedBoard = boardService.saveBoard(board);
            return ResponseEntity.ok(savedBoard);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }

    /**
     * Updates the password of a board
     * @param password String password for board
     * @param joinKey String join key for board
     * @return 200 OK if the board has been updated
     */
    @PostMapping("/boards/set-password/{joinKey}")
    public ResponseEntity<Board> setBoardPassword(@Valid @RequestBody final String password, @PathVariable final String joinKey) {
        try {
            final Board board = boardService.getBoardWithKey(joinKey);
            board.setPassword(password);
            boardService.saveBoard(board);

            updateBoardPassword(joinKey, password);

            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }

    /**
     * Deletes a board
     * @param joinKey join key of a board
     * @return 200 OK if the board has been deleted
     */
    @DeleteMapping("/boards/delete/{joinKey}")
    public ResponseEntity<Void> deleteBoard(@PathVariable final String joinKey) {
        final Board board = boardService.getBoardWithKey(joinKey);
        boardService.deleteBoard(board);
        logger.info("Deleted board with join key: " + joinKey);
        return ResponseEntity.ok().build();
    }

    /**
     * Renames board in repo and sends update to subscribed clients
     * @param joinKey String for board
     * @param newHeading  String for new name of board
     * @param password String password for board
     * @return Board the renamed board
     */
    @MessageMapping("/boards/rename/{joinKey}/{newHeading}")
    public Board renameBoard(@Payload(required = false) final String password, @DestinationVariable final String joinKey,
                             @DestinationVariable final String newHeading)
    {
        try {
            final Board toBeRenamed = boardService.getBoardWithKeyAndPassword(joinKey, password);

            toBeRenamed.setTitle(newHeading);
            boardService.saveBoard(toBeRenamed);

            updateBoardRenamed(joinKey, newHeading);

            return toBeRenamed;
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }

    /**
     * Sends the old and new heading of the board to all subscribed clients
     * @param joinKey String for board
     * @param newHeading  String for new name of board
     */
    public void updateBoardRenamed(final String joinKey, final String newHeading) {
        logger.info("Propagating column renamed for: " + joinKey);
        messagingTemplate.convertAndSend("/topic/boards/" + joinKey + "/rename", newHeading);
    }

    private void updateBoardPassword(final String joinKey, final String password) {
        logger.info("Propagating password update for: " + joinKey);
        messagingTemplate.convertAndSend("/topic/boards/" + joinKey + "/set-password", password);
    }

}
