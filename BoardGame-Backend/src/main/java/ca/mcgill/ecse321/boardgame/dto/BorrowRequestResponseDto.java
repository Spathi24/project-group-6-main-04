package ca.mcgill.ecse321.boardgame.dto;

import java.sql.Date;

import ca.mcgill.ecse321.boardgame.model.BorrowRequest;
import ca.mcgill.ecse321.boardgame.model.RequestStatus;

public class BorrowRequestResponseDto {

    private long id;
    private RequestStatus status;
    private Date requestDate;
    private Date decisionDate;
    private Date startDate;
    private Date endDate;
    private long borrowerId;
    private long ownerId;
    private String gameTitle;

    public BorrowRequestResponseDto() {}

    public BorrowRequestResponseDto(BorrowRequest br) {
        this.id = br.getId();
        this.status = br.getRequestStatus();
        this.requestDate = br.getRequestDate();
        this.decisionDate = br.getDecisionDate();
        this.startDate = br.getStartDate();
        this.endDate = br.getEndDate();
        this.borrowerId = br.getBorrower().getUserAccountID();
        this.ownerId = br.getGameToBorrow().getGameCopyKey().getOwner().getUserAccountID();
        this.gameTitle = br.getGameToBorrow().getGameCopyKey().getGame().getTitle();
    }

    public long getId() { return id; }
    public RequestStatus getStatus() { return status; }
    public Date getRequestDate() { return requestDate; }
    public Date getDecisionDate() { return decisionDate; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public long getBorrowerId() { return borrowerId; }
    public long getOwnerId() { return ownerId; }
    public String getGameTitle() { return gameTitle; }
}
