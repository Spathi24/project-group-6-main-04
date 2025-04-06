package ca.mcgill.ecse321.boardgame.service;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.boardgame.dto.ReviewCreationDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewResponseDto;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.Review;
import ca.mcgill.ecse321.boardgame.model.Review.ReviewKey;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.ReviewRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

/**
 * Service class for managing reviews.
 */
@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * Creates a new review.
     *
     * @param reviewCreationDto the details of the review to be created
     * @return the created Review
     */
    @Transactional
    public Review createReview(@Valid ReviewCreationDto reviewCreationDto) {

        String gameTitle = reviewCreationDto.getReviewKey().getGameTitle();
        Long reviewerId = reviewCreationDto.getReviewKey().getReviewerId();

        UserAccount reviewer = userAccountRepository.findUserAccountByUserAccountID(reviewerId);
        if (reviewer == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "UserAccount with ID " + reviewerId + " not found");
        }

        Game game = gameService.getGameByTitle(gameTitle);
        if (game == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Game with title " + gameTitle + " not found");
        }

        ReviewKey reviewKey = new ReviewKey(reviewer, game);

        Date currentDate = new Date(System.currentTimeMillis());
        Review review = new Review(reviewKey, reviewCreationDto.getRating(), reviewCreationDto.getComment(),
                currentDate);
        return reviewRepository.save(review);
    }

    /**
     * Retrieves all reviews by a given reviewer (user) ID.
     *
     * @param reviewerId the ID of the reviewer
     * @return a list of ReviewResponseDto objects
     */
    @Transactional
    public List<ReviewResponseDto> getReviewsByUserId(long reviewerId) {

        List<Review> reviews = StreamSupport
                .stream(reviewRepository.findAll().spliterator(), false)
                .filter(review -> review.getReviewKey().getReviewer().getUserAccountID() == reviewerId)
                .collect(Collectors.toList());
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "No reviews found for user with ID " + reviewerId);
        }
        return reviews.stream().map(ReviewResponseDto::new).collect(Collectors.toList());
    }

    /**
     * Retrieves all reviews for a given game title.
     *
     * @param gameTitle the title of the game
     * @return a list of ReviewResponseDto objects
     */
    @Transactional
    public List<ReviewResponseDto> getReviewsByGameTitle(String gameTitle) {

        List<Review> reviews = StreamSupport
                .stream(reviewRepository.findAll().spliterator(), false)
                .filter(review -> review.getReviewKey().getGameToReview().getTitle().equals(gameTitle))
                .collect(Collectors.toList());
        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "No reviews found for game with title " + gameTitle);
        }
        return reviews.stream().map(ReviewResponseDto::new).collect(Collectors.toList());
    }

    /**
     * Deletes a review given the reviewer ID and game title.
     *
     * @param gameTitle  the title of the game being reviewed
     * @param reviewerId the ID of the reviewer
     */
    @Transactional
    public void deleteReview(String gameTitle, long reviewerId) {
        Review review = getReviewByKey(reviewerId, gameTitle);
        reviewRepository.delete(review);
    }

    /**
     * Retrieves a review by its composite review key.
     *
     * @param reviewerId the ID of the reviewer
     * @param gameTitle  the title of the game being reviewed
     * @return a ReviewResponseDto representing the review
     */
    @Transactional
    public ReviewResponseDto getReviewByReviewKey(long reviewerId, String gameTitle) {
        Review review = getReviewByKey(reviewerId, gameTitle);
        return new ReviewResponseDto(review);
    }

    /**
     * Helper method to retrieve a review by its composite key.
     *
     * @param reviewerId the ID of the reviewer
     * @param gameTitle  the title of the game being reviewed
     * @return the Review object
     */
    private Review getReviewByKey(long reviewerId, String gameTitle) {

        UserAccount reviewer = userAccountRepository.findUserAccountByUserAccountID(reviewerId);
        if (reviewer == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "UserAccount with ID " + reviewerId + " not found");
        }

        Game game = gameService.getGameByTitle(gameTitle);
        if (game == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Game with title " + gameTitle + " not found");
        }

        ReviewKey reviewKey = new ReviewKey(reviewer, game);
        Review review = reviewRepository.findReviewByReviewKey(reviewKey);
        if (review == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Review for game " + gameTitle + " by user " + reviewerId + " not found");
        }
        return review;
    }
}
