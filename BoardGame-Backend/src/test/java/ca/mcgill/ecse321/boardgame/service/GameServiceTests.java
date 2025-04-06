package ca.mcgill.ecse321.boardgame.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.mcgill.ecse321.boardgame.dto.EventRegistrationResponseDto;
import ca.mcgill.ecse321.boardgame.dto.GameRequestDto;
import ca.mcgill.ecse321.boardgame.dto.GameResponseDto;
import ca.mcgill.ecse321.boardgame.exception.BoardGameException;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class GameServiceTests {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    private final String TITLE = "Chess";
    private final String DESCRIPTION = "A strategic board game.";
    private final String CATEGORY = "Strategy";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // CREATE GAME TESTS

    @Test
    void testCreateValidGame() {
        GameRequestDto dto = new GameRequestDto(TITLE, DESCRIPTION, CATEGORY);
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(null);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.createGame(dto);

        assertNotNull(result);
        assertEquals(TITLE, result.getTitle());
        assertEquals(DESCRIPTION, result.getDescription());
        assertEquals(CATEGORY, result.getCategory());
    }

    @Test
    void testCreateGameWithExistingTitle() {
        GameRequestDto dto = new GameRequestDto(TITLE, DESCRIPTION, CATEGORY);
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(new Game(TITLE, DESCRIPTION, CATEGORY));

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> gameService.createGame(dto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testCreateGameWithEmptyTitle() {
        GameRequestDto dto = new GameRequestDto("", DESCRIPTION, CATEGORY);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> gameService.createGame(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    // GET GAME TESTS

    @Test
    void testGetGameByValidTitle() {
        Game game = new Game(TITLE, DESCRIPTION, CATEGORY);
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(game);

        Game result = gameService.getGameByTitle(TITLE);

        assertNotNull(result);
        assertEquals(TITLE, result.getTitle());
        assertEquals(DESCRIPTION, result.getDescription());
        assertEquals(CATEGORY, result.getCategory());
    }

    @Test
    void testGetGameByInvalidTitle() {
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gameService.getGameByTitle(TITLE));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testGetGameByEmptyTitle() {
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> gameService.getGameByTitle(""));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    @Test
    void testGetAllGames_EmptyList() {
        when(gameRepository.findAll()).thenReturn(Collections.emptyList());

        List<GameResponseDto> result = gameService.getAllGames();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllGames_MultipleGames() {
        Game game1 = new Game("Chess", "Strategy game", "Strategy");
        Game game2 = new Game("Checkers", "Board game for two players", "Classic");
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2));

        List<GameResponseDto> result = gameService.getAllGames();

        assertEquals(2, result.size());
        GameResponseDto result1 = result.get(0);
        assertEquals("Chess", result1.getTitle());
        assertEquals("Strategy game", result1.getDescription());
        assertEquals("Strategy", result1.getCategory());

        GameResponseDto result2 = result.get(1);
        assertEquals("Checkers", result2.getTitle());
        assertEquals("Board game for two players", result2.getDescription());
        assertEquals("Classic", result2.getCategory());
    }

    // UPDATE GAME TESTS

    @Test
    void testUpdateGameWithValidTitle() {
        Game existingGame = new Game(TITLE, "Old Description", "Old Category");
        GameRequestDto dto = new GameRequestDto(TITLE, DESCRIPTION, CATEGORY);
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(existingGame);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.updateGame(TITLE, dto);

        assertNotNull(result);
        assertEquals(TITLE, result.getTitle());
        assertEquals(DESCRIPTION, result.getDescription());
        assertEquals(CATEGORY, result.getCategory());
    }

    @Test
    void testUpdateGameWithInvalidTitle() {
        GameRequestDto dto = new GameRequestDto(TITLE, DESCRIPTION, CATEGORY);
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gameService.updateGame(TITLE, dto));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testUpdateGameWithEmptyTitle() {
        GameRequestDto dto = new GameRequestDto(TITLE, DESCRIPTION, CATEGORY);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> gameService.updateGame("", dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    // DELETE GAME TESTS

    @Test
    void testDeleteGameWithValidTitle() {
        Game game = new Game(TITLE, DESCRIPTION, CATEGORY);
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(game);
        doNothing().when(gameRepository).delete(game);

        assertDoesNotThrow(() -> gameService.deleteGame(TITLE));

        verify(gameRepository, times(1)).delete(game);
    }

    @Test
    void testDeleteGameWithInvalidTitle() {
        when(gameRepository.findGameByTitle(TITLE)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gameService.deleteGame(TITLE));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testDeleteGameWithEmptyTitle() {
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> gameService.deleteGame(""));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }
}
