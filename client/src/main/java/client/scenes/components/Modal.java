package client.scenes.components;

import client.services.BoardService;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

public class Modal extends GridPane {

    private final BoardService boardService;

    private final String title;

    private final String description;

    private final Scene currentScene;


    /**
     * Default constructor for modal
     * @param boardService boardService instance
     * @param title title of modal
     * @param description description of modal
     * @param currentScene scene behind modal
     */
    public Modal(final BoardService boardService, final String title, final String description, final Scene currentScene) {
        this.boardService = boardService;
        this.title = title;
        this.description = description;
        this.currentScene = currentScene;
    }
}