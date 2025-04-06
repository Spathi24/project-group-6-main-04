package ca.mcgill.ecse321.boardgame.dto;

import jakarta.validation.constraints.NotNull;

public class EventRegistrationRequestDto {

    @NotNull(message = "Participant ID is required")
    private Long participantId;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    public EventRegistrationRequestDto(Long participantId, Long eventId) {
        this.participantId = participantId;
        this.eventId = eventId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public Long getEventId() {
        return eventId;
    }
}
