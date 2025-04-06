package ca.mcgill.ecse321.boardgame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewCreationDto {

    @NotNull(message = "Rating is required")
    private Integer rating;
    private String comment;

    @NotNull(message = "Review key is required")
    private ReviewKeyDto reviewKey;

    public ReviewCreationDto(Integer rating, String comment, ReviewKeyDto reviewKey) {
        this.rating = rating;
        this.comment = comment;
        this.reviewKey = reviewKey;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReviewKeyDto getReviewKey() {
        return reviewKey;
    }

    public void setReviewKey(ReviewKeyDto reviewKey) {
        this.reviewKey = reviewKey;
    }

    // Nested DTO for the composite review key
    public static class ReviewKeyDto {

        @NotBlank(message = "Game title is required")
        private String gameTitle;

        @NotNull(message = "Reviewer id is required")
        private Long reviewerId;

        public ReviewKeyDto(String gameTitle, Long reviewerId) {
            this.gameTitle = gameTitle;
            this.reviewerId = reviewerId;
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
    }
}
