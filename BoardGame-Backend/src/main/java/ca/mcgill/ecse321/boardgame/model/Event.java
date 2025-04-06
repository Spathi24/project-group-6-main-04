package ca.mcgill.ecse321.boardgame.model;

import jakarta.persistence.*;

import java.sql.*;

@Entity(name = "Event")
public class Event {

    @Id
    @GeneratedValue
    private long eventID;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.TIME)
    private Time time;

    private String location;

    private String eventName;

    private String description;

    private int maxParticipant;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Game gameToPlay;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserAccount creator;

    protected Event() {
    }

    public Event(Date date, Time time, String location, String description, int maxParticipant, Game gameToPlay,
            UserAccount creator, String eventName) {
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.maxParticipant = maxParticipant;
        this.gameToPlay = gameToPlay;
        this.creator = creator;
        this.eventName = eventName;
    }

    public Long getEventID() {
        return eventID;
    }

    public Date getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxParticipant() {
        return maxParticipant;
    }

    public Game getGameToPlay() {
        return gameToPlay;
    }

    public UserAccount getCreator() {
        return creator;
    }

    public void setCreator(UserAccount creator) {
        this.creator = creator;
    }

    public String getEventName() {
        return eventName;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setTime(Time time) {
        this.time = time;
    }

}
