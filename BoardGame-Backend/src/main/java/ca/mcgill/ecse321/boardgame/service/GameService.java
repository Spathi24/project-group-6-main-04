package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.dto.EventRegistrationResponseDto;
import ca.mcgill.ecse321.boardgame.dto.GameResponseDto;
import ca.mcgill.ecse321.boardgame.model.EventRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ca.mcgill.ecse321.boardgame.dto.GameRequestDto;
import ca.mcgill.ecse321.boardgame.exception.BoardGameException;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing games.
 * Author: Panayiotis Saropulos
 */
@Service
@Validated
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    /**
     * Retrieves a game by its title.
     *
     * @param title the title of the game
     * @return the Game object
     * @throws ResourceNotFoundException if the game is not found
     */
    @Transactional
    public Game getGameByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BoardGameException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }

        Game game = gameRepository.findGameByTitle(title);
        if (game == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Game with title '" + title + "' not found");
        }
        return game;
    }

    /**
     * Retrieves all games.
     *
     * @return a list of all GameResponseDto objects
     */
    @Transactional
    public List<GameResponseDto> getAllGames() {
        List<Game> games = (List<Game>) gameRepository.findAll();
        return games.stream()
                .map(GameResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new game.
     *
     * @param gameRequestDto the details of the game to be created
     * @return the created Game object
     * @throws BoardGameException if a game with the same title already exists
     */
    @Transactional
    public Game createGame(@Valid GameRequestDto gameRequestDto) {
        if (gameRequestDto.getTitle() == null || gameRequestDto.getTitle().trim().isEmpty()) {
            throw new BoardGameException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }

        if (gameRepository.findGameByTitle(gameRequestDto.getTitle()) != null) {
            throw new BoardGameException(HttpStatus.CONFLICT,
                    "Game with title '" + gameRequestDto.getTitle() + "' already exists");
        }

        Game game = new Game(
                gameRequestDto.getTitle(),
                gameRequestDto.getDescription(),
                gameRequestDto.getCategory()
        );

        return gameRepository.save(game);
    }


    /**
     * Updates an existing game's details.
     *
     * @param title the title of the game to update
     * @param gameRequestDto the new details for the game
     * @return the updated Game object
     * @throws ResourceNotFoundException if the game is not found
     */
    @Transactional
    public Game updateGame(String title, @Valid GameRequestDto gameRequestDto) {
        if (title == null || title.trim().isEmpty()) {
            throw new BoardGameException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }

        Game game = getGameByTitle(title);

        game.setDescription(gameRequestDto.getDescription());

        try {
            java.lang.reflect.Field categoryField = Game.class.getDeclaredField("category");
            categoryField.setAccessible(true);
            categoryField.set(game, gameRequestDto.getCategory());
        } catch (Exception e) {
            throw new BoardGameException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update game category: " + e.getMessage());
        }

        return gameRepository.save(game);
    }


    /**
     * Deletes a game by its title.
     *
     * @param title the title of the game to delete
     * @throws ResourceNotFoundException if the game is not found
     */
    @Transactional
    public void deleteGame(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BoardGameException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }

        Game game = getGameByTitle(title);
        gameRepository.delete(game);
    }
}
