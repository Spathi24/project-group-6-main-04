package ca.mcgill.ecse321.boardgame.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import ca.mcgill.ecse321.boardgame.dto.*;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private GameRepository gameRepo;

    private static final String BASE_URL = "/api/games";
    private final String gameTitle = "Catan";

    @BeforeAll
    public void setup() {
        Game game = new Game(gameTitle, "A fun strategy game", "Strategy");
        gameRepo.save(game);
    }

    @AfterAll
    public void clean() {
        gameRepo.deleteAll();
    }

    @Test
    @Order(1)
    public void testCreateValidGame() {
        GameRequestDto dto = new GameRequestDto("Chess", "A classic game of strategy", "Board");
        ResponseEntity<GameResponseDto> response = client.postForEntity(BASE_URL, dto, GameResponseDto.class);

        assertNotNull(response);
        assertEquals(CREATED, response.getStatusCode());
        GameResponseDto createdGame = response.getBody();
        assertNotNull(createdGame);
        assertEquals("Chess", createdGame.getTitle());
        assertEquals("A classic game of strategy", createdGame.getDescription());
    }

    @Test
    @Order(2)
    public void testCreateGameWithExistingTitle() {
        GameRequestDto dto = new GameRequestDto(gameTitle, "Duplicate game", "Strategy");
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, dto, ErrorDto.class);

        assertNotNull(response);
        assertEquals(CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getErrors().stream().anyMatch(error -> error.contains("already exists")));
    }

    @Test
    @Order(3)
    public void testCreateGameWithEmptyTitle() {
        GameRequestDto dto = new GameRequestDto("", "Description", "Category");
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, dto, ErrorDto.class);

        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void testGetGameByValidTitle() {
        ResponseEntity<GameResponseDto> response = client.getForEntity(BASE_URL + "/" + gameTitle, GameResponseDto.class);

        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        GameResponseDto game = response.getBody();
        assertNotNull(game);
        assertEquals(gameTitle, game.getTitle());
        assertEquals("A fun strategy game", game.getDescription());
        assertEquals("Strategy", game.getCategory());
    }

    @Test
    @Order(5)
    public void testGetGameByInvalidTitle() {
        ResponseEntity<ErrorDto> response = client.getForEntity(BASE_URL + "/NonexistentGame", ErrorDto.class);

        assertNotNull(response);
        assertEquals(NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().getErrors().stream()
                .anyMatch(error -> error.contains("not found")));
    }

    @Test
    @Order(6)
    public void testGetAllGames_EmptyList() {
        gameRepo.deleteAll();
        ResponseEntity<GameResponseDto[]> response = client.getForEntity(BASE_URL, GameResponseDto[].class);

        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    @Order(7)
    public void testGetAllGames_MultipleGames() {
        gameRepo.save(new Game("Monopoly", "A game about property trading", "Family"));
        gameRepo.save(new Game("Monopoly2", "A game about property trading", "Family"));
        ResponseEntity<GameResponseDto[]> response = client.getForEntity(BASE_URL, GameResponseDto[].class);

        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        GameResponseDto[] games = response.getBody();
        assertNotNull(games);
        assertEquals(2, games.length);

        List<String> gameTitles = Arrays.stream(games).map(GameResponseDto::getTitle).collect(Collectors.toList());
        assertTrue(gameTitles.contains("Monopoly"));
        assertTrue(gameTitles.contains("Monopoly2"));
    }

    @Test
    @Order(8)
    public void testDeleteGameWithValidTitle() {
        client.delete(BASE_URL + "/Chess");
        ResponseEntity<ErrorDto> response = client.getForEntity(BASE_URL + "/Chess", ErrorDto.class);

        assertNotNull(response);
        assertEquals(NOT_FOUND, response.getStatusCode());
        ErrorDto error = response.getBody();
        assertNotNull(error);
        assertTrue(error.getErrors().stream().anyMatch(msg -> msg.contains("not found")));
    }

    @Test
    @Order(9)
    public void testDeleteGameWithInvalidTitle() {
        ResponseEntity<ErrorDto> response = client.getForEntity(BASE_URL + "/Invalid", ErrorDto.class);

        assertNotNull(response);
        assertEquals(NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().getErrors().stream().anyMatch(error -> error.contains("not found")));
    }
}
