package ca.mcgill.ecse321.boardgame.controller;

import ca.mcgill.ecse321.boardgame.dto.EventRegistrationRequestDto;
import ca.mcgill.ecse321.boardgame.dto.EventRegistrationResponseDto;
import ca.mcgill.ecse321.boardgame.service.EventRegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventregistrations")
@CrossOrigin(origins = "http://localhost:5173")
public class EventRegistrationController {

    @Autowired
    private EventRegistrationService eventRegistrationService;

    /**
     * Registers a participant for an event.
     *
     * @param dto the details of the registration request
     * @return the created EventRegistrationResponseDto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventRegistrationResponseDto register(@RequestBody @Valid EventRegistrationRequestDto dto) {
        return eventRegistrationService.register(dto);
    }

    /**
     * Retrieves a specific event registration.
     *
     * @param participantId the ID of the participant
     * @param eventId       the ID of the event
     * @return the retrieved EventRegistrationResponseDto
     */
    @GetMapping("/{participantId}/{eventId}")
    public EventRegistrationResponseDto getRegistration(@PathVariable Long participantId, @PathVariable Long eventId) {
        return eventRegistrationService.getRegistration(participantId, eventId);
    }

    /**
     * Retrieves all event registrations for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of EventRegistrationResponseDto for the specified user
     */
    @GetMapping("/user/{userId}")
    public List<EventRegistrationResponseDto> getAllRegistrationsByUser(@PathVariable Long userId) {
        return eventRegistrationService.getAllRegistrationsByUser(userId);
    }

    /**
     * Retrieves all event registrations for a specific event.
     *
     * @param eventId the ID of the event
     * @return a list of EventRegistrationResponseDto for the specified event
     */
    @GetMapping("/event/{eventId}")
    public List<EventRegistrationResponseDto> getAllRegistrationsByEvent(@PathVariable Long eventId) {
        return eventRegistrationService.getAllRegistrationsByEvent(eventId);
    }

    /**
     * Retrieves all event registrations.
     *
     * @return a list of all EventRegistrationResponseDto
     */
    @GetMapping
    public List<EventRegistrationResponseDto> getAllRegistrations() {
        return eventRegistrationService.getAllRegistrations();
    }

    /**
     * Cancels a participant's registration for an event.
     *
     * @param participantId the ID of the participant
     * @param eventId       the ID of the event
     */
    @DeleteMapping("/{participantId}/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRegistration(@PathVariable Long participantId, @PathVariable Long eventId) {
        eventRegistrationService.cancelRegistration(participantId, eventId);
    }
}
