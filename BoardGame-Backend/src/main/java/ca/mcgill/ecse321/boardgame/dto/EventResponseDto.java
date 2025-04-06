package ca.mcgill.ecse321.boardgame.dto;

import ca.mcgill.ecse321.boardgame.model.Event;

import java.sql.Date;
import java.sql.Time;

public class EventResponseDto {

    private String eventName;
    private Date eventDate;
    private Time eventTime;
    private String location;
    private String description;
    private Integer maxParticipants;
    private String gameTitle;
    private Long creatorId;

    protected EventResponseDto() {
    }

    public EventResponseDto(Event event) {
        this.eventName = event.getEventName();
        this.eventDate = event.getDate();
        this.eventTime = event.getTime();
        this.location = event.getLocation();
        this.description = event.getDescription();
        this.maxParticipants = event.getMaxParticipant();
        this.gameTitle = event.getGameToPlay().getTitle(); // Assuming Event has a Game relation
        this.creatorId = event.getCreator().getUserAccountID(); // Assuming Event has a User creator
    }

    // Getters and Setters
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Time getEventTime() {
        return eventTime;
    }

    public void setEventTime(Time eventTime) {
        this.eventTime = eventTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}
