package ca.mcgill.ecse321.boardgame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Date;
import java.sql.Time;

/**
 * Data Transfer Object for creating a new event.
 */
public class EventCreationDto {

    @NotBlank(message = "Event name is required")
    private String eventName;

    @NotNull(message = "Event date is required")
    private Date eventDate;

    @NotNull(message = "Event time is required")
    private Time eventTime;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Max participants is required")
    private Integer maxParticipants;

    @NotNull(message = "Game ID is required")
    private String gameTitle;

    @SuppressWarnings("unused")
    private EventCreationDto() {
    }

    public EventCreationDto(String eventName, Date eventDate, Time eventTime, String location,
            String description, Integer maxParticipants, String gameTitle) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.gameTitle = gameTitle;
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

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }
}
