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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.boardgame.dto.ErrorDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewCreationDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewCreationDto.ReviewKeyDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewResponseDto;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.ReviewRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private static final String BASE_URL = "/api/reviews";
    private static final String VALID_GAME_TITLE = "Catan";
    private static final int VALID_RATING = 5;
    private static final String VALID_COMMENT = "Great game!";

    private long reviewerId;

    @BeforeAll
    public void setup() {
        // Create a user account to act as the reviewer
        UserAccount reviewer = new UserAccount("reviewer", "password", "reviewer@mail.com", AccountType.GAMEOWNER);
        reviewer = userAccountRepo.save(reviewer);
        reviewerId = reviewer.getUserAccountID();

        // Create a game that will be reviewed
        Game game = new Game(VALID_GAME_TITLE, "Popular strategy game", "Strategy");
        gameRepository.save(game);
    }

    @AfterAll
    public void clean() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        userAccountRepo.deleteAll();
    }

    @Test
    @Order(1)
    public void testCreateReview() {
        // ARRANGE
        ReviewKeyDto keyDto = new ReviewKeyDto(VALID_GAME_TITLE, reviewerId);
        ReviewCreationDto request = new ReviewCreationDto(VALID_RATING, VALID_COMMENT, keyDto);

        // ACT
        ResponseEntity<ReviewResponseDto> response = client.postForEntity(
                BASE_URL + "/" + reviewerId, request, ReviewResponseDto.class);

        // ASSERT
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ReviewResponseDto createdReview = response.getBody();
        assertNotNull(createdReview);
        assertEquals(VALID_RATING, createdReview.getRating());
        assertEquals(VALID_COMMENT, createdReview.getComment());
        assertEquals(VALID_GAME_TITLE, createdReview.getGameTitle());
        assertEquals(reviewerId, createdReview.getReviewerId());
    }

    @Test
    @Order(2)
    public void testGetReviewByReviewKey() {
        // ARRANGE
        String url = BASE_URL + "/" + reviewerId + "/" + VALID_GAME_TITLE;

        // ACT
        ResponseEntity<ReviewResponseDto> response = client.getForEntity(url, ReviewResponseDto.class);

        // ASSERT
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponseDto review = response.getBody();
        assertNotNull(review);
        assertEquals(VALID_RATING, review.getRating());
        assertEquals(VALID_COMMENT, review.getComment());
        assertEquals(VALID_GAME_TITLE, review.getGameTitle());
        assertEquals(reviewerId, review.getReviewerId());
    }

    @Test
    @Order(3)
    public void testGetReviewsByUserId() {
        // ARRANGE
        String url = BASE_URL + "/user/" + reviewerId;

        // ACT
        ResponseEntity<ReviewResponseDto[]> response = client.getForEntity(url, ReviewResponseDto[].class);

        // ASSERT
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponseDto[] reviews = response.getBody();
        assertNotNull(reviews);
        assertTrue(reviews.length >= 1);
        boolean found = false;
        for (ReviewResponseDto r : reviews) {
            if (VALID_GAME_TITLE.equals(r.getGameTitle())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Created review not found in reviews by user id");
    }

    @Test
    @Order(4)
    public void testGetReviewsByGameTitle() {
        // ARRANGE
        String url = BASE_URL + "/game/" + VALID_GAME_TITLE;

        // ACT
        ResponseEntity<ReviewResponseDto[]> response = client.getForEntity(url, ReviewResponseDto[].class);

        // ASSERT
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponseDto[] reviews = response.getBody();
        assertNotNull(reviews);
        assertTrue(reviews.length >= 1);
        boolean found = false;
        for (ReviewResponseDto r : reviews) {
            if (r.getReviewerId().equals(reviewerId)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Created review not found in reviews by game title");
    }

    @Test
    @Order(5)
    public void testCreateReviewWithInvalidUser() {
        // ARRANGE
        long invalidReviewerId = 9999L;
        ReviewKeyDto keyDto = new ReviewKeyDto(VALID_GAME_TITLE, invalidReviewerId);
        ReviewCreationDto request = new ReviewCreationDto(VALID_RATING, VALID_COMMENT, keyDto);
        // ACT
        ResponseEntity<String> response = client.postForEntity(BASE_URL + "/" + invalidReviewerId, request,
                String.class);
        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void testCreateReviewWithInvalidGame() {
        // ARRANGE
        String invalidGameTitle = "Nonexistent Game";
        ReviewKeyDto keyDto = new ReviewKeyDto(invalidGameTitle, reviewerId);
        ReviewCreationDto request = new ReviewCreationDto(VALID_RATING, VALID_COMMENT, keyDto);
        // ACT
        ResponseEntity<String> response = client.postForEntity(BASE_URL + "/" + reviewerId, request, String.class);
        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testDeleteReview() {
        // ARRANGE
        String url = BASE_URL + "/" + reviewerId + "/" + VALID_GAME_TITLE;
        // ACT
        client.delete(url);
        // ASSERT
        ResponseEntity<String> response = client.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void testGetReviewsByUserIdAfterDeletion() {
        // ARRANGE
        String url = BASE_URL + "/user/" + reviewerId;
        // ACT
        ResponseEntity<String> response = client.getForEntity(url, String.class);
        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(9)
    public void testGetReviewsByGameTitleNotFound() {
        // ARRANGE
        String invalidGameTitle = "Nonexistent Game";
        String url = BASE_URL + "/game/" + invalidGameTitle;
        // ACT
        ResponseEntity<String> response = client.getForEntity(url, String.class);
        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void testGetReviewByReviewKeyNotFound() {
        // ARRANGE
        String invalidGameTitle = "Another Nonexistent Game";
        String url = BASE_URL + "/" + reviewerId + "/" + invalidGameTitle;
        // ACT
        ResponseEntity<String> response = client.getForEntity(url, String.class);
        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
