package commons.DTOs;

import commons.Card;
import lombok.Getter;

public record CardDTO(@Getter Card cards, @Getter String password) {

    /**
     * Constructor for CardDTO
     *
     * @param card     Card to be transferred
     * @param password password of board in which card belongs for authentication
     */
    public CardDTO {
    }
}
