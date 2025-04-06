package ca.mcgill.ecse321.boardgame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.boardgame.dto.ReviewCreationDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewCreationDto.ReviewKeyDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewResponseDto;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.Review;
import ca.mcgill.ecse321.boardgame.model.Review.ReviewKey;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.ReviewRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

@SpringBootTest
public class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameService gameService;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    public void testCreateReview() {
        // Arrange
        Long reviewerId = 1L;
        String gameTitle = "Catan";
        int rating = 5;
        String comment = "Great game!";
        Date currentDate = new Date(System.currentTimeMillis());

        // Create a spy for the reviewer so we can stub getUserAccountID()
        UserAccount reviewer = spy(new UserAccount("Alice", "pass", "alice@mail.com", null));
        when(reviewer.getUserAccountID()).thenReturn(reviewerId);

        Game game = new Game(gameTitle, "Strategy board game", "Board Game");

        when(userAccountRepository.findUserAccountByUserAccountID(reviewerId)).thenReturn(reviewer);
        when(gameService.getGameByTitle(gameTitle)).thenReturn(game);

        ReviewKeyDto reviewKeyDto = new ReviewKeyDto(gameTitle, reviewerId);
        ReviewCreationDto reviewCreationDto = new ReviewCreationDto(rating, comment, reviewKeyDto);

        ReviewKey reviewKey = new ReviewKey(reviewer, game);
        Review review = new Review(reviewKey, rating, comment, currentDate);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        Review createdReview = reviewService.createReview(reviewCreationDto);

        // Assert
        assertNotNull(createdReview);
        assertEquals(rating, createdReview.getRating());
        assertEquals(comment, createdReview.getComment());
        assertEquals(gameTitle, createdReview.getReviewKey().getGameToReview().getTitle());
        assertEquals(reviewerId, createdReview.getReviewKey().getReviewer().getUserAccountID());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    public void testCreateReviewWithInvalidUser() {
        // Arrange
        Long reviewerId = 2L;
        String gameTitle = "Catan";
        int rating = 4;
        String comment = "Fun game!";
        ReviewKeyDto reviewKeyDto = new ReviewKeyDto(gameTitle, reviewerId);
        ReviewCreationDto reviewCreationDto = new ReviewCreationDto(rating, comment, reviewKeyDto);

        when(userAccountRepository.findUserAccountByUserAccountID(reviewerId)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(reviewCreationDto);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("UserAccount with ID " + reviewerId + " not found", exception.getMessage());
    }

    @Test
    public void testCreateReviewWithInvalidGame() {
        // Arrange
        Long reviewerId = 1L;
        String gameTitle = "Nonexistent Game";
        int rating = 3;
        String comment = "Not so good.";
        ReviewKeyDto reviewKeyDto = new ReviewKeyDto(gameTitle, reviewerId);
        ReviewCreationDto reviewCreationDto = new ReviewCreationDto(rating, comment, reviewKeyDto);

        UserAccount reviewer = spy(new UserAccount("Alice", "pass", "alice@mail.com", null));
        when(reviewer.getUserAccountID()).thenReturn(reviewerId);

        when(userAccountRepository.findUserAccountByUserAccountID(reviewerId)).thenReturn(reviewer);
        when(gameService.getGameByTitle(gameTitle)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(reviewCreationDto);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Game with title " + gameTitle + " not found", exception.getMessage());
    }

    @Test
    public void testGetReviewsByUserId() {
        // Arrange
        Long reviewerId = 1L;
        UserAccount reviewer = spy(new UserAccount("Alice", "pass", "alice@mail.com", null));
        when(reviewer.getUserAccountID()).thenReturn(reviewerId);

        Game game1 = new Game("Catan", "Strategy game", "Board Game");
        Game game2 = new Game("Carcassonne", "Tile-laying game", "Board Game");

        ReviewKey key1 = new ReviewKey(reviewer, game1);
        ReviewKey key2 = new ReviewKey(reviewer, game2);
        Review review1 = new Review(key1, 5, "Excellent!", new Date(System.currentTimeMillis()));
        Review review2 = new Review(key2, 4, "Pretty good", new Date(System.currentTimeMillis()));

        Iterable<Review> reviewIterable = Arrays.asList(review1, review2);
        when(reviewRepository.findAll()).thenReturn(reviewIterable);

        // Act
        List<ReviewResponseDto> responseDtos = reviewService.getReviewsByUserId(reviewerId);

        // Assert
        assertEquals(2, responseDtos.size());
        List<Integer> ratings = responseDtos.stream().map(ReviewResponseDto::getRating).collect(Collectors.toList());
        assertTrue(ratings.contains(5));
        assertTrue(ratings.contains(4));
    }

    @Test
    public void testGetReviewsByUserIdNotFound() {
        // Arrange
        Long reviewerId = 99L;
        when(reviewRepository.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReviewsByUserId(reviewerId);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("No reviews found for user with ID " + reviewerId, exception.getMessage());
    }

    @Test
    public void testGetReviewsByGameTitle() {
        // Arrange
        String gameTitle = "Catan";
        UserAccount reviewer1 = spy(new UserAccount("Alice", "pass", "alice@mail.com", null));
        when(reviewer1.getUserAccountID()).thenReturn(1L);
        UserAccount reviewer2 = spy(new UserAccount("Bob", "pass", "bob@mail.com", null));
        when(reviewer2.getUserAccountID()).thenReturn(2L);

        Game game = new Game(gameTitle, "Strategy game", "Board Game");

        ReviewKey key1 = new ReviewKey(reviewer1, game);
        ReviewKey key2 = new ReviewKey(reviewer2, game);
        Review review1 = new Review(key1, 5, "Loved it", new Date(System.currentTimeMillis()));
        Review review2 = new Review(key2, 3, "It was okay", new Date(System.currentTimeMillis()));

        Iterable<Review> reviewIterable = Arrays.asList(review1, review2);
        when(reviewRepository.findAll()).thenReturn(reviewIterable);

        // Act
        List<ReviewResponseDto> responseDtos = reviewService.getReviewsByGameTitle(gameTitle);

        // Assert
        assertEquals(2, responseDtos.size());
        List<String> comments = responseDtos.stream().map(ReviewResponseDto::getComment).collect(Collectors.toList());
        assertTrue(comments.contains("Loved it"));
        assertTrue(comments.contains("It was okay"));
    }

    @Test
    public void testGetReviewsByGameTitleNotFound() {
        // Arrange
        String gameTitle = "UnknownGame";
        when(reviewRepository.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReviewsByGameTitle(gameTitle);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("No reviews found for game with title " + gameTitle, exception.getMessage());
    }

    @Test
    public void testGetReviewByReviewKey() {
        // Arrange
        Long reviewerId = 1L;
        String gameTitle = "Catan";
        UserAccount reviewer = spy(new UserAccount("Alice", "pass", "alice@mail.com", null));
        when(reviewer.getUserAccountID()).thenReturn(reviewerId);
        Game game = new Game(gameTitle, "Strategy game", "Board Game");

        ReviewKey key = new ReviewKey(reviewer, game);
        Review review = new Review(key, 5, "Amazing game!", new Date(System.currentTimeMillis()));

        when(userAccountRepository.findUserAccountByUserAccountID(reviewerId)).thenReturn(reviewer);
        when(gameService.getGameByTitle(gameTitle)).thenReturn(game);
        when(reviewRepository.findReviewByReviewKey(any(ReviewKey.class))).thenReturn(review);

        // Act
        ReviewResponseDto responseDto = reviewService.getReviewByReviewKey(reviewerId, gameTitle);

        // Assert
        assertNotNull(responseDto);
        assertEquals(5, responseDto.getRating());
        assertEquals("Amazing game!", responseDto.getComment());
        assertEquals(gameTitle, responseDto.getGameTitle());
        assertEquals(reviewerId, responseDto.getReviewerId());
    }

    @Test
    public void testDeleteReview() {
        // Arrange
        Long reviewerId = 1L;
        String gameTitle = "Catan";
        UserAccount reviewer = spy(new UserAccount("Alice", "pass", "alice@mail.com", null));
        when(reviewer.getUserAccountID()).thenReturn(reviewerId);
        Game game = new Game(gameTitle, "Strategy game", "Board Game");

        ReviewKey key = new ReviewKey(reviewer, game);
        Review review = new Review(key, 4, "Good game", new Date(System.currentTimeMillis()));

        when(userAccountRepository.findUserAccountByUserAccountID(reviewerId)).thenReturn(reviewer);
        when(gameService.getGameByTitle(gameTitle)).thenReturn(game);
        when(reviewRepository.findReviewByReviewKey(any(ReviewKey.class))).thenReturn(review);

        // Act
        reviewService.deleteReview(gameTitle, reviewerId);

        // Assert
        verify(reviewRepository, times(1)).delete(review);
        verify(reviewRepository, times(1)).findReviewByReviewKey(any(ReviewKey.class));
    }
}
