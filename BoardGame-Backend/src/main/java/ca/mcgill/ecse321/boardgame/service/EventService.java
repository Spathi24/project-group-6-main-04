package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.dto.EventCreationDto;
import ca.mcgill.ecse321.boardgame.dto.EventResponseDto;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.*;
import ca.mcgill.ecse321.boardgame.model.GameCopy.GameCopyKey;
import ca.mcgill.ecse321.boardgame.repo.BorrowRequestRepository;
import ca.mcgill.ecse321.boardgame.repo.EventRepository;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import ca.mcgill.ecse321.boardgame.repo.EventRegistrationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing board game events
 */
@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    /**
     * Creates a new game event.
     *
     * @param eventCreationDto the DTO containing event creation details
     * @param userAccountId    the ID of the user creating the event
     * @return the created event
     * @throws ResourceNotFoundException if the user or game is not found, or the
     *                                   user is not authorized to create the event
     * @throws IllegalArgumentException  if the event details are invalid or if the
     *                                   game is not available at the specified
     *                                   date/time
     */
    @Transactional
    public Event createEvent(@Valid EventCreationDto eventCreationDto, long userAccountId) {
        UserAccount user = userAccountRepository.findUserAccountByUserAccountID(userAccountId);
        Game game = gameRepository.findGameByTitle(eventCreationDto.getGameTitle());

        if (user == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "User with ID " + userAccountId + " not found.");
        }

        if (game == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Game with title " + eventCreationDto.getGameTitle() + " not found.");
        }

        if (user.getAccountType() != AccountType.GAMEOWNER) {
            // verify if the user is owner or borrower
            List<BorrowRequest> borrowRequests = borrowRequestRepository.findByBorrowerAndGameTitle(user,
                    game.getTitle());

            if (borrowRequests.isEmpty()) {
                throw new ResourceNotFoundException(HttpStatus.FORBIDDEN,
                        "User is not authorized to create an event for this game.");
            }
        }

        // Check the details
        if (eventCreationDto.getEventName() == null || eventCreationDto.getLocation() == null
                || eventCreationDto.getDescription() == null
                || eventCreationDto.getMaxParticipants() <= 0 || eventCreationDto.getEventDate() == null
                || eventCreationDto.getEventTime() == null) {
            throw new ResourceNotFoundException(HttpStatus.BAD_REQUEST,
                    "Invalid event details.");
        }

        // Check if the game is available at the specified date/time
        if (!isGameAvailableAtTime(game, eventCreationDto.getEventDate(),
                eventCreationDto.getEventTime(), user)) {

            throw new ResourceNotFoundException(HttpStatus.BAD_REQUEST,
                    "The game: " + game.getTitle() + " is not available at the specified date/time.");
        }

        // Create the event object
        Event event = new Event(eventCreationDto.getEventDate(), eventCreationDto.getEventTime(),
                eventCreationDto.getLocation(),
                eventCreationDto.getDescription(), eventCreationDto.getMaxParticipants(), game, user,
                eventCreationDto.getEventName());

        // Associate the user with the event as the creator
        event.setCreator(user);

        // Save the event to the database
        Event savedEvent = eventRepository.save(event);

        return savedEvent;

    }

    /**
     * Helper method to check if the game is available at the specified date/time.
     *
     * @param game      the game to check availability for
     * @param eventDate the date of the event
     * @param eventTime the time of the event
     * @param user      the user who is creating the event
     * @return true if the game is available at the specified date/time, false
     *         otherwise
     */
    private boolean isGameAvailableAtTime(Game game, Date eventDate, Time eventTime, UserAccount user) {
        GameCopyKey gameCopyKey = new GameCopyKey(user, game);
        GameCopy gameCopy = gameCopyRepository.findGameCopyByGameCopyKey(gameCopyKey);

        if (gameCopy != null) {
            return true;
        }

        List<BorrowRequest> borrowRequests = borrowRequestRepository.findByBorrowerAndGameTitle(user, game.getTitle());

        for (BorrowRequest borrowRequest : borrowRequests) {
            Date startDate = borrowRequest.getStartDate();
            Date endDate = borrowRequest.getEndDate();

            // Check if the eventDate is within the borrowRequest time frame
            if ((eventDate.equals(startDate) || eventDate.after(startDate))
                    && (eventDate.equals(endDate) || eventDate.before(endDate))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the description of an existing event.
     *
     * @param eventId        the ID of the event to update
     * @param newDescription the new description to be set
     * @throws ResourceNotFoundException if the event with the given ID is not found
     */
    @Transactional
    public void updateEventDescription(long eventId, String newDescription) {
        Event event = eventRepository.findEventByEventID(eventId);
        if (event == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Event with ID " + eventId + " not found");
        }
        event.setDescription(newDescription);
        eventRepository.save(event);
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param eventId the ID of the event to retrieve
     * @return the event object
     * @throws ResourceNotFoundException if the event with the given ID is not found
     */
    @Transactional
    public Event getEventById(long eventId) {
        Event event = eventRepository.findEventByEventID(eventId);

        if (event == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return event;

    }

    /**
     * Retrieves all events.
     *
     * @return a list of all events
     */
    @Transactional
    public List<EventResponseDto> getAllEvents() {
        List<Event> events = (List<Event>) eventRepository.findAll();
        return events.stream()
                .map(event -> new EventResponseDto(event)).collect(Collectors.toList());
    }

    /**
     * Deletes an event by its ID.
     *
     * @param eventId the ID of the event to be deleted
     */
    @Transactional
    public void deleteEvent(long eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
    }

    /**
     * Deletes expired events automatically.
     * Runs once a day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *") // At midnight every day
    @Transactional
    public void deleteExpiredEvents() {
        java.util.Date now = new java.util.Date();
        java.sql.Date sqlNow = new java.sql.Date(now.getTime());

        // Find events where date has passed
        List<Event> expiredEvents = eventRepository.findByDateBefore(now);
        if (!expiredEvents.isEmpty()) {
            eventRepository.deleteAll(expiredEvents);
            System.out.println(expiredEvents.size() + " expired events deleted");
        }
    }

    /**
     * Retrieves all events that a user is registered for.
     *
     * @param userAccountId the ID of the user
     * @return a list of events the user is registered for
     * @throws ResourceNotFoundException if the user with the given ID is not found
     */
    @Transactional
    public List<EventResponseDto> getEventsByUserRegistration(long userAccountId) {
        UserAccount user = userAccountRepository.findUserAccountByUserAccountID(userAccountId);

        if (user == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "User with ID " + userAccountId + " not found");
        }

        // Get all events the user is registered for
        List<Event> events = eventRegistrationRepository.findEventsByEventRegistrationKeyRegistrant(user);

        return events.stream()
                .map(event -> new EventResponseDto(event))
                .collect(Collectors.toList());
    }

}
