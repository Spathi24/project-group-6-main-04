package ca.mcgill.ecse321.boardgame.dto;

import ca.mcgill.ecse321.boardgame.model.GameCopy;

public class GameCopyResponseDto {
    private String title;
    private String description;
    private long owner;
    private String status;
    private String ownerName;
    private String originalDescription;

    @SuppressWarnings("unused")
    private GameCopyResponseDto() {
    }

    public GameCopyResponseDto(GameCopy gameCopy) {
        this.title = gameCopy.getGameCopyKey().getGame().getTitle();
        this.description = gameCopy.getDescription();
        this.owner = gameCopy.getGameCopyKey().getOwner().getUserAccountID();
        this.ownerName = gameCopy.getGameCopyKey().getOwner().getName();
        this.status = gameCopy.getStatus().toString();
        this.originalDescription = gameCopy.getGameCopyKey().getGame().getDescription();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getOwner() {
        return owner;
    }

    public String getStatus() {
        return status;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }
}
