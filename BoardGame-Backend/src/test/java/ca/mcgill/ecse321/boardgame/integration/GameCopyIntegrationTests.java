package ca.mcgill.ecse321.boardgame.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.boardgame.dto.GameCopyCreationDto;
import ca.mcgill.ecse321.boardgame.dto.GameCopyResponseDto;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class GameCopyIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private GameRepository gameRepository;

    private static final String BASE_URL = "/api/gamecopies";
    private static final String VALID_TITLE = "Catan";
    private static final String VALID_DESCRIPTION = "A fun game";
    private static final String VALID_STATUS = "AVAILABLE";
    private static final String VALID_USERNAME = "testuser";

    private long userAccountId;

    @BeforeAll
    public void setup() {
        // Create a user account for testing
        UserAccount userAccount = new UserAccount(VALID_USERNAME, "password", "testuser@mail.com",
                AccountType.GAMEOWNER);
        userAccount = userAccountRepo.save(userAccount);
        userAccountId = userAccount.getUserAccountID();

        Game game = new Game(VALID_TITLE, VALID_DESCRIPTION, "Role Play");
        gameRepository.save(game);
    }

    @AfterAll
    public void clean() {
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
        userAccountRepo.deleteAll();
    }

    @SuppressWarnings("null")
    @Test
    @Order(1)
    public void testCreateGameCopy() {
        // ARRANGE
        GameCopyCreationDto request = new GameCopyCreationDto(VALID_TITLE, VALID_DESCRIPTION, userAccountId);

        // ACT
        System.out.println(BASE_URL + "/" + userAccountId);
        ResponseEntity<GameCopyResponseDto> response = client.postForEntity(BASE_URL + "/" + userAccountId, request,
                GameCopyResponseDto.class);

        // ASSERT
        GameCopyResponseDto createdGame = response.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(VALID_TITLE, createdGame.getTitle());
        assertEquals(VALID_DESCRIPTION, createdGame.getDescription());
        assertNotNull(createdGame.getOwner());
        assertTrue(createdGame.getOwner() > 0, "Response should have a positive ID.");
        assertEquals(VALID_STATUS, createdGame.getStatus());
        assertEquals(VALID_USERNAME, createdGame.getOwnerName());

        this.userAccountId = createdGame.getOwner();
    }

    @Test
    @Order(2)
    public void testUpdateGameCopyStatus() {
        // ARRANGE
        String newStatus = "BORROWED";
        String url = BASE_URL + "/" + userAccountId + "/status/" + VALID_TITLE;

        // ACT
        client.put(url + "?status=" + newStatus, null);

        // ASSERT
        GameCopyResponseDto updatedGameCopy = client.getForObject(BASE_URL + "/" + userAccountId,
                GameCopyResponseDto[].class)[0];
        assertEquals(newStatus, updatedGameCopy.getStatus());
    }

    @Test
    @Order(3)
    public void testUpdateGameCopyDescription() {
        // ARRANGE
        String newDescription = "An updated fun game";
        String url = BASE_URL + "/" + userAccountId + "/description/" + VALID_TITLE;

        // ACT
        client.put(url + "?newDescription=" + newDescription, null);

        // ASSERT
        GameCopyResponseDto updatedGameCopy = client.getForObject(BASE_URL + "/" + userAccountId,
                GameCopyResponseDto[].class)[0];
        assertEquals(newDescription, updatedGameCopy.getDescription());
    }

    @Test
    @Order(4)
    public void testGetUserGameCopies() {
        // ARRANGE
        // Add additional game copies
        Game game1 = new Game("Monopoly", "Classic", "Role Play");
        gameRepository.save(game1);
        GameCopyCreationDto request1 = new GameCopyCreationDto("Monopoly", "Very Old", userAccountId);

        client.postForEntity(BASE_URL + "/" + userAccountId, request1, GameCopyResponseDto.class);

        Game game2 = new Game("Chess", "Old", "Strategy");
        gameRepository.save(game2);
        GameCopyCreationDto request2 = new GameCopyCreationDto("Chess", "A strategic game", userAccountId);

        client.postForEntity(BASE_URL + "/" + userAccountId, request2, GameCopyResponseDto.class);

        String url = BASE_URL + "/" + userAccountId;

        // ACT
        ResponseEntity<GameCopyResponseDto[]> response = client.getForEntity(url, GameCopyResponseDto[].class);

        // ASSERT
        GameCopyResponseDto[] gameCopies = response.getBody();
        assertNotNull(gameCopies);
        assertTrue(gameCopies.length >= 3);
        assertEquals("Catan", gameCopies[0].getTitle());
        assertEquals("Monopoly", gameCopies[1].getTitle());
        assertEquals("Chess", gameCopies[2].getTitle());

        // Check owner name for all game copies
        for (GameCopyResponseDto gameCopy : gameCopies) {
            assertEquals(VALID_USERNAME, gameCopy.getOwnerName());
        }
    }

    @Test
    @Order(5)
    public void testDeleteGameCopy() {
        // ARRANGE
        String url = BASE_URL + "/" + userAccountId + "/" + VALID_TITLE;

        // ACT
        client.delete(url);

        // ASSERT
        ResponseEntity<GameCopyResponseDto[]> response = client.getForEntity(BASE_URL + "/" + userAccountId,
                GameCopyResponseDto[].class);
        GameCopyResponseDto[] gameCopies = response.getBody();
        assertNotNull(gameCopies);
        assertTrue(gameCopies.length == 2); // there are two games still in the db
    }

    @Test
    @Order(6)
    public void testCreateGameCopyWithInvalidData() {
        // ARRANGE
        GameCopyCreationDto request = new GameCopyCreationDto(null, null, userAccountId);

        // ACT
        ResponseEntity<GameCopyResponseDto> response = client.postForEntity(BASE_URL + "/" + userAccountId, request,
                GameCopyResponseDto.class);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testUpdateGameCopyStatusWithInvalidData() {
        // ARRANGE
        String newStatus = null;
        String url = BASE_URL + "/" + userAccountId + "/status/" + VALID_TITLE;

        // ACT
        ResponseEntity<Void> response = client.exchange(url + "?status=" + newStatus, HttpMethod.PUT, null, Void.class);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void testUpdateGameCopyDescriptionWithInvalidData() {
        // ARRANGE
        String newDescription = "";
        String url = BASE_URL + "/" + userAccountId + "/description/" + VALID_TITLE;

        // ACT
        ResponseEntity<Void> response = client.exchange(url + "?newDescription=" + newDescription, HttpMethod.PUT, null,
                Void.class);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
