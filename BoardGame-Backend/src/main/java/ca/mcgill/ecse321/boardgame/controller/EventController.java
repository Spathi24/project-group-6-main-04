package ca.mcgill.ecse321.boardgame.controller;

import ca.mcgill.ecse321.boardgame.dto.EventCreationDto;
import ca.mcgill.ecse321.boardgame.dto.EventResponseDto;
import ca.mcgill.ecse321.boardgame.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Creates a new event.
     *
     * @param eventCreationDto the DTO containing event creation details
     * @param userAccountId    the ID of the user who is creating the event
     * @return the response DTO of the created event
     */
    @PostMapping("/{userAccountId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto createEvent(@RequestBody @Valid EventCreationDto eventCreationDto,
            @PathVariable long userAccountId) {
        return new EventResponseDto(eventService.createEvent(eventCreationDto, userAccountId));
    }

    /**
     * Retrieves the details of a specific event by its ID.
     *
     * @param eventId the ID of the event to retrieve
     * @return the response DTO containing the event's details
     */
    @GetMapping("/{eventId}")
    public EventResponseDto getEventById(@PathVariable long eventId) {
        return new EventResponseDto(eventService.getEventById(eventId));
    }

    /**
     * Retrieves a list of all events.
     *
     * @return a list of response DTOs containing all events
     */
    @GetMapping()
    public List<EventResponseDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    /**
     * Updates the description of an existing event.
     *
     * @param eventId        the ID of the event to update
     * @param newDescription the new description to be set for the event
     */
    @PutMapping("/{eventId}/description")
    @ResponseStatus(HttpStatus.OK)
    public void updateEventDescription(@PathVariable long eventId, @RequestBody String newDescription) {
        eventService.updateEventDescription(eventId, newDescription);
    }

    /**
     * Deletes a specific event by its ID.
     *
     * @param eventId the ID of the event to delete
     */
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable long eventId) {
        eventService.deleteEvent(eventId);
    }

}
