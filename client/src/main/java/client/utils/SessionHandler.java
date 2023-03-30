package client.utils;

import client.exceptions.BoardChangeException;
import client.services.BoardService;
import client.services.ServerService;
import commons.Column;
import commons.DTOs.CardDTO;
import commons.DTOs.ColumnDTO;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class SessionHandler extends StompSessionHandlerAdapter {

    private Logger logger = LogManager.getLogger(SessionHandler.class);

    private StompSession session;
    private List<Subscription> subscriptions;

    private final ServerService serverService;

    private final BoardService boardService;


    /**
     * Constructor to pass serverService in. This allows the handler to pass itself to the serverService after it is connected
     *
     * @param serverService ServerService to manage sessionHandler
     * @param boardService  BoardService used to manage client boardModel and UI
     */
    public SessionHandler(final ServerService serverService, final BoardService boardService) {
        this.serverService = serverService;
        this.boardService = boardService;
        this.subscriptions = new ArrayList<>();
    }

    /**
     * After the socket connects with the server the session is saved
     * and this, the session handler, is passed to mainCtrl
     * @param session the client STOMP session
     * @param headers the STOMP CONNECTED frame headers
     */
    @Override
    public void afterConnected(@Nullable final StompSession session, @Nullable final StompHeaders headers) {
        this.session = session;
        serverService.setHandler(this);
        serverService.setSession(session);
    }

    /**
     * Subscribes socket to the board associated with joinKey and unsubscribes from previous boards
     * if there were any.
     * @param joinKey String key to subscribe to
     */
    public void subscribeToBoard(final String joinKey) {
        // Unsubscribes from previous board, so that unnecessary traffic is avoided
        for (final Subscription subscription : subscriptions) subscription.unsubscribe();
        subscriptions.clear();

        subscribeToBoardUpdates(joinKey);

        subscribeToColumnUpdates(joinKey);

        subscribeToCardChangeUpdates(joinKey);
        subscribeToCardExistenceUpdates(joinKey);

        logger.info("Subscribed to board: " + joinKey);
    }

    private void subscribeToBoardUpdates(final String joinKey) {
        final Subscription boardRenameSub = session.subscribe(
            "/topic/boards/" + joinKey + "/rename", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) {  return String.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    Platform.runLater( () -> {
                        boardService.updateRenameBoard((String) payload);
                        logger.info("Board renamed: " + payload.toString());
                    }); }
            });
        subscriptions.add(boardRenameSub);
    }

    private void subscribeToCardChangeUpdates(final String joinKey) {
        final Subscription cardRepositionedSub = session.subscribe(
            "/topic/cards/" + joinKey + "/reposition", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) { return CardDTO.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    Platform.runLater( () -> {
                        final CardDTO cardDTO = (CardDTO) payload;
                        try {
                            boardService.updateRepositionCard(cardDTO.getCard().getId(),
                                cardDTO.getColumnFromId(),
                                cardDTO.getColumnToId(), cardDTO.getNewPosition());
                            logger.info("Card repositioned: " + cardDTO.getCard().getTitle());
                        }
                        catch (BoardChangeException e) { logger.info("Couldn't reposition card : " + cardDTO.getCard().getTitle()); }
                    }); }
            });
        subscriptions.add(cardRepositionedSub);

        final Subscription cardEditedSub = session.subscribe(
            "/topic/cards/" + joinKey + "/edit", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) { return CardDTO.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    final CardDTO cardDTO = (CardDTO) payload;
                    final Column column = boardService.getCurrentBoard().getColumnById(cardDTO.getColumnFromId());
                    try {
                        boardService.updateEditCard(cardDTO.getCard(), column);
                        logger.info("Card edited: " + cardDTO.getCard().getTitle());
                    }
                    catch (BoardChangeException e) { logger.info("Couldn't edit card : " + cardDTO.getCard().getTitle()); }
                }
            });
        subscriptions.add(cardEditedSub);
    }

    /**
     * Session receives notifications regarding card addition and deletion
     * @param joinKey String for board
     */
    public void subscribeToCardExistenceUpdates(final String joinKey) {
        final Subscription cardAddedSub = session.subscribe(
            "/topic/cards/" + joinKey + "/add", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) { return CardDTO.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    Platform.runLater( () -> {
                        final CardDTO cardDTO = (CardDTO) payload;
                        final Column column = boardService.getCurrentBoard().getColumnById(cardDTO.getColumnFromId());
                        try {
                            boardService.updateAddCardToColumn(cardDTO.getCard(), column);
                            logger.info("Card added: " + cardDTO.getCard().getTitle());
                        }
                        catch (BoardChangeException e) { logger.info("Couldn't add card : " + cardDTO.getCard().getTitle()); }
                    }); }
            });
        subscriptions.add(cardAddedSub);

        final Subscription cardRemovedSub = session.subscribe(
            "/topic/cards/" + joinKey + "/remove", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) { return CardDTO.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    Platform.runLater( () -> {
                        final CardDTO cardDTO = (CardDTO) payload;
                        final Column column = boardService.getCurrentBoard().getColumnById(cardDTO.getColumnFromId());
                        try {
                            boardService.updateRemoveCardFromColumn(cardDTO.getCard(), column);
                            logger.info("Card added: " + cardDTO.getCard().getTitle());
                        }
                        catch (BoardChangeException e) { logger.info("Couldn't delete card  : " + cardDTO.getCard().getTitle()); }
                    }); }
            });
        subscriptions.add(cardRemovedSub);
    }

    private void subscribeToColumnUpdates(final String joinKey) {
        final Subscription columnAddedSub = session.subscribe(
            "/topic/columns/" + joinKey + "/add", new StompSessionHandlerAdapter()  {
                public Type getPayloadType(final StompHeaders headers) {  return Column.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    Platform.runLater(() -> {
                        final Column column = (Column) payload;
                        try {
                            boardService.updateAddColumnToCurrentBoard(column);
                            logger.info("Column added: " + ((Column) payload).getHeading());
                        }
                        catch (BoardChangeException e) { logger.info("Couldn't add column : " + column.getHeading()); }
                    }); }
            });
        subscriptions.add(columnAddedSub);

        final Subscription columnRenamedSub = session.subscribe(
            "/topic/columns/" + joinKey + "/rename", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) { return ColumnDTO.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    final ColumnDTO columnDTO = (ColumnDTO) payload;
                    final Column column = boardService.getCurrentBoard().getColumnById(columnDTO.getColumnId());
                    boardService.updateRenameColumn(column, columnDTO.getNewHeading());
                    logger.info("Column renamed: " + columnDTO.getNewHeading());
                }
            });
        subscriptions.add(columnRenamedSub);

        final Subscription columnRemovedSub = session.subscribe(
            "/topic/columns/" + joinKey + "/remove", new StompSessionHandlerAdapter() {
                public Type getPayloadType(final StompHeaders headers) { return Long.class; }

                public void handleFrame(final StompHeaders headers, final Object payload) {
                    Platform.runLater(() -> {
                        try {
                            boardService.updateRemoveColumnFromCurrentBoard((Long) payload);
                            logger.info("Column removed");
                        }
                        catch (BoardChangeException e) {
                            final String title = boardService.getCurrentBoard().getColumnById((Long) payload).getHeading();
                            logger.info("Couldn't remove column : " + title);
                        } }); }
            });
        subscriptions.add(columnRemovedSub);
    }

}
