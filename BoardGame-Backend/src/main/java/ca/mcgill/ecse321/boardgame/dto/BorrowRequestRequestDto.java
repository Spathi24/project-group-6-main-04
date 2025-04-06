package ca.mcgill.ecse321.boardgame.dto;

import java.sql.Date;

import jakarta.validation.constraints.NotNull;

public class BorrowRequestRequestDto {

    @NotNull
    private Long borrowerId;
    @NotNull
    private Long ownerId;
    @NotNull
    private String gameTitle;
    @NotNull
    private Date startDate;
    @NotNull
    private Date endDate;

    public BorrowRequestRequestDto() {}

    public BorrowRequestRequestDto(Long borrowerId, Long ownerId, String gameTitle, Date startDate, Date endDate) {
        this.borrowerId = borrowerId;
        this.ownerId = ownerId;
        this.gameTitle = gameTitle;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getBorrowerId() { return borrowerId; }
    public Long getOwnerId() { return ownerId; }
    public String getGameTitle() { return gameTitle; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
}
