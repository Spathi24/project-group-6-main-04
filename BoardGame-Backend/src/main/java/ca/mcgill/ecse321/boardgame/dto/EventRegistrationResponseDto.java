package ca.mcgill.ecse321.boardgame.dto;

import ca.mcgill.ecse321.boardgame.model.EventRegistration;
import ca.mcgill.ecse321.boardgame.model.ParticipationStatus;

public class EventRegistrationResponseDto {

    private Long participantId;
    private Long eventId;
    private ParticipationStatus status;
    private int maxParticipants;

    @SuppressWarnings("unused")
    private EventRegistrationResponseDto() {
    }

    public EventRegistrationResponseDto(EventRegistration registration) {
        this.participantId = registration.getEventRegistrationKey().getRegistrant().getUserAccountID();
        this.eventId = registration.getEventRegistrationKey().getEvent().getEventID();
        this.status = registration.getParticipationStatus();
    }

    public Long getParticipantId() {
        return participantId;
    }

    public Long getEventId() {
        return eventId;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

}
