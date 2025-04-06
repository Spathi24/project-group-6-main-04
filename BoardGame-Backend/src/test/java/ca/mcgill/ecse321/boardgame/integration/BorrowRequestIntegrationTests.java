package ca.mcgill.ecse321.boardgame.integration;

import ca.mcgill.ecse321.boardgame.BoardgameApplication;
import ca.mcgill.ecse321.boardgame.dto.BorrowRequestRequestDto;
import ca.mcgill.ecse321.boardgame.dto.BorrowRequestResponseDto;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.BorrowRequest;
import ca.mcgill.ecse321.boardgame.model.RequestStatus;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.BorrowRequestRepository;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BoardgameApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BorrowRequestIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private UserAccountRepository userAccountRepo;
    @Autowired
    private GameCopyRepository gameCopyRepo;
    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private BorrowRequestRepository borrowRequestRepo;
    @Autowired
    private ObjectMapper objectMapper;

    private long borrowerId;
    private long ownerId;
    private static final String GAME_TITLE = "Chess";

    @BeforeAll
    public void setup() {
        borrowRequestRepo.deleteAll();
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
        userAccountRepo.deleteAll();

        UserAccount borrower = new UserAccount("Borrower", "borrowpass", "borrower@mail.com", AccountType.PLAYER);
        borrower = userAccountRepo.save(borrower);
        this.borrowerId = borrower.getUserAccountID();

        UserAccount owner = new UserAccount("Owner", "ownerpass", "owner@mail.com", AccountType.GAMEOWNER);
        owner = userAccountRepo.save(owner);
        this.ownerId = owner.getUserAccountID();

        Game game = new Game(GAME_TITLE, "Strategy board game", "BoardGame");
        gameRepo.save(game);

        GameCopy copy = new GameCopy(new GameCopy.GameCopyKey(owner, game), "Decent chess set");
        gameCopyRepo.save(copy);
    }

    @AfterAll
    public void teardown() {
        borrowRequestRepo.deleteAll();
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
        userAccountRepo.deleteAll();
    }

    @Test
    @Order(1)
    public void testCreateBorrowRequest() {
        BorrowRequestRequestDto dto = new BorrowRequestRequestDto(
                borrowerId,
                ownerId,
                GAME_TITLE,
                Date.valueOf("2025-05-01"),
                Date.valueOf("2025-05-10")
        );

        ResponseEntity<BorrowRequestResponseDto> response = client.postForEntity(
                "/api/borrowrequests",
                dto,
                BorrowRequestResponseDto.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RequestStatus.PENDING, response.getBody().getStatus());
    }

    @Test
    @Order(2)
    public void testAcceptBorrowRequest() {
        BorrowRequestResponseDto[] all = client.getForObject("/api/borrowrequests", BorrowRequestResponseDto[].class);
        assertTrue(all.length > 0);

        long requestId = all[0].getId();

        ResponseEntity<BorrowRequestResponseDto> response = client.exchange(
                "/api/borrowrequests/" + requestId + "/accept",
                HttpMethod.PUT,
                null,
                BorrowRequestResponseDto.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RequestStatus.ACCEPTED, response.getBody().getStatus());
    }

    @Test
    @Order(3)
    public void testDeclineBorrowRequest() {
        BorrowRequestRequestDto dto = new BorrowRequestRequestDto(
                borrowerId,
                ownerId,
                GAME_TITLE,
                Date.valueOf("2025-06-01"),
                Date.valueOf("2025-06-05")
        );
        client.postForEntity("/api/borrowrequests", dto, BorrowRequestResponseDto.class);

        BorrowRequestResponseDto[] all = client.getForObject("/api/borrowrequests", BorrowRequestResponseDto[].class);
        assertTrue(all.length >= 2);

        long secondId = all[all.length - 1].getId();

        ResponseEntity<BorrowRequestResponseDto> response = client.exchange(
                "/api/borrowrequests/" + secondId + "/decline",
                HttpMethod.PUT,
                null,
                BorrowRequestResponseDto.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(RequestStatus.DECLINED, response.getBody().getStatus());
    }

    @Test
    @Order(4)
    public void testGetAllBorrowRequests() {
        BorrowRequestResponseDto[] all = client.getForObject("/api/borrowrequests", BorrowRequestResponseDto[].class);
        assertNotNull(all);
        assertTrue(all.length >= 2);
    }
}
