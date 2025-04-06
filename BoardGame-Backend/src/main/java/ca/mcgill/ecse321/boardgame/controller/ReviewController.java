package ca.mcgill.ecse321.boardgame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.boardgame.dto.ReviewCreationDto;
import ca.mcgill.ecse321.boardgame.dto.ReviewResponseDto;
import ca.mcgill.ecse321.boardgame.model.Review;
import ca.mcgill.ecse321.boardgame.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * Creates a new review for the specified reviewer.
     *
     * @param reviewerId        the ID of the reviewer creating the review
     * @param reviewCreationDto the details of the review to be created
     * @return the created ReviewResponseDto
     */
    @PostMapping("/{reviewerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponseDto createReview(@PathVariable long reviewerId,
            @RequestBody @Valid ReviewCreationDto reviewCreationDto) {

        reviewCreationDto.getReviewKey().setReviewerId(reviewerId);
        Review createdReview = reviewService.createReview(reviewCreationDto);
        return new ReviewResponseDto(createdReview);
    }

    /**
     * Retrieves all reviews written by a given user.
     *
     * @param reviewerId the ID of the reviewer
     * @return a list of ReviewResponseDto objects
     */
    @GetMapping("/user/{reviewerId}")
    public List<ReviewResponseDto> getReviewsByUserId(@PathVariable long reviewerId) {
        return reviewService.getReviewsByUserId(reviewerId);
    }

    /**
     * Retrieves all reviews for a given game title.
     *
     * @param gameTitle the title of the game
     * @return a list of ReviewResponseDto objects
     */
    @GetMapping("/game/{gameTitle}")
    public List<ReviewResponseDto> getReviewsByGameTitle(@PathVariable String gameTitle) {
        return reviewService.getReviewsByGameTitle(gameTitle);
    }

    /**
     * Retrieves a review by its composite review key.
     *
     * @param reviewerId the ID of the reviewer
     * @param gameTitle  the title of the game being reviewed
     * @return the ReviewResponseDto representing the review
     */
    @GetMapping("/{reviewerId}/{gameTitle}")
    public ReviewResponseDto getReviewByReviewKey(@PathVariable long reviewerId,
            @PathVariable String gameTitle) {
        return reviewService.getReviewByReviewKey(reviewerId, gameTitle);
    }

    /**
     * Deletes a review given the reviewer ID and game title.
     *
     * @param reviewerId the ID of the reviewer
     * @param gameTitle  the title of the game being reviewed
     */
    @DeleteMapping("/{reviewerId}/{gameTitle}")
    public void deleteReview(@PathVariable long reviewerId,
            @PathVariable String gameTitle) {
        reviewService.deleteReview(gameTitle, reviewerId);
    }
}
