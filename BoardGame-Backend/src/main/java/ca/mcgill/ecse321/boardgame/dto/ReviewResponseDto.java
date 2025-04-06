package ca.mcgill.ecse321.boardgame.dto;

import java.sql.Date;
import ca.mcgill.ecse321.boardgame.model.Review;
import ca.mcgill.ecse321.boardgame.model.Review.ReviewKey;

public class ReviewResponseDto {
    private String gameTitle;
    private Long reviewerId;
    private int rating;
    private String comment;
    private Date date;

    /**
     * Constructs a ReviewResponseDto from a Review entity.
     *
     * @param review the Review entity
     */
    public ReviewResponseDto(Review review) {
        // Assuming your ReviewKey contains a reference to the Game as "gameToReview"
        // and a reference to the UserAccount as "reviewer"
        ReviewKey reviewKey = review.getReviewKey();
        this.gameTitle = reviewKey.getGameToReview().getTitle();
        this.reviewerId = reviewKey.getReviewer().getUserAccountID();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.date = review.getDate();
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ReviewResponseDto() {

    }

}
