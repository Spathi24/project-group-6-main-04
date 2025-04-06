package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.dto.EventRegistrationRequestDto;
import ca.mcgill.ecse321.boardgame.dto.EventRegistrationResponseDto;
import ca.mcgill.ecse321.boardgame.exception.BoardGameException;
import ca.mcgill.ecse321.boardgame.model.Event;
import ca.mcgill.ecse321.boardgame.model.EventRegistration;
import ca.mcgill.ecse321.boardgame.model.EventRegistration.EventRegistrationKey;
import ca.mcgill.ecse321.boardgame.model.ParticipationStatus;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.EventRegistrationRepository;
import ca.mcgill.ecse321.boardgame.repo.EventRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventRegistrationService {

        @Autowired
        private EventRegistrationRepository eventRegistrationRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private UserAccountRepository userAccountRepository;

        /**
         * Registers a user for an event.
         *
         * @param dto the registration details
         * @return the registered EventRegistration
         */
        @Transactional
        public EventRegistrationResponseDto register(@Valid EventRegistrationRequestDto dto) {
                // Validate input
                if (dto.getParticipantId() == null || dto.getEventId() == null) {
                        throw new BoardGameException(HttpStatus.BAD_REQUEST,
                                        "Participant ID and Event ID are required.");
                }

                // Fetch the user
                UserAccount user = userAccountRepository.findUserAccountByUserAccountID(dto.getParticipantId());
                if (user == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "User not found.");
                }

                // Fetch the event
                Event event = eventRepository.findEventByEventID(dto.getEventId());
                if (event == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "Event not found.");
                }

                // Check if the event is full
                long currentRegistrations = eventRegistrationRepository.findAllByEventRegistrationKeyEvent(event)
                                .size();
                if (currentRegistrations >= event.getMaxParticipant()) {
                        throw new BoardGameException(HttpStatus.BAD_REQUEST, "Event is already full.");
                }

                // Check if the event has already started
                Timestamp eventStartTime = Timestamp
                                .valueOf(LocalDateTime.of(event.getDate().toLocalDate(),
                                                event.getTime().toLocalTime()));
                if (LocalDateTime.now().isAfter(eventStartTime.toLocalDateTime())) {
                        throw new BoardGameException(HttpStatus.BAD_REQUEST,
                                        "Cannot register for an event that has already started.");
                }

                // Check if the user is already registered for this event
                EventRegistrationKey registrationKey = new EventRegistrationKey(user, event);
                if (eventRegistrationRepository.findByEventRegistrationKey(registrationKey) != null) {
                        throw new BoardGameException(HttpStatus.BAD_REQUEST,
                                        "User is already registered for this event.");
                }

                // Register the user
                EventRegistration registration = new EventRegistration(registrationKey, ParticipationStatus.PENDING);
                eventRegistrationRepository.save(registration);

                return new EventRegistrationResponseDto(registration);
        }

        /**
         * Retrieves an event registration.
         *
         * @param participantId the ID of the participant
         * @param eventId       the ID of the event
         * @return the EventRegistrationResponseDto
         */
        @Transactional
        public EventRegistrationResponseDto getRegistration(Long participantId, Long eventId) {
                UserAccount user = userAccountRepository.findUserAccountByUserAccountID(participantId);
                if (user == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "User not found.");
                }

                Event event = eventRepository.findEventByEventID(eventId);
                if (event == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "Event not found.");
                }

                EventRegistration registration = eventRegistrationRepository
                                .findByEventRegistrationKey(new EventRegistrationKey(user, event));
                if (registration == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "Registration not found.");
                }

                return new EventRegistrationResponseDto(registration);
        }

        /**
         * Retrieves all registrations for a specific event.
         *
         * @param eventId the ID of the event
         * @return a list of EventRegistrationResponseDto
         */
        @Transactional
        public List<EventRegistrationResponseDto> getAllRegistrationsByEvent(Long eventId) {
                Event event = eventRepository.findEventByEventID(eventId);
                if (event == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "Event not found.");
                }

                List<EventRegistration> registrations = eventRegistrationRepository
                                .findAllByEventRegistrationKeyEvent(event);
                return registrations.stream().map(EventRegistrationResponseDto::new).collect(Collectors.toList());
        }

        /**
         * Retrieves all registrations for a specific user.
         *
         * @param userId the ID of the user
         * @return a list of EventRegistrationResponseDto
         */
        @Transactional
        public List<EventRegistrationResponseDto> getAllRegistrationsByUser(Long userId) {
                UserAccount user = userAccountRepository.findUserAccountByUserAccountID(userId);
                if (user == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "User not found.");
                }

                List<EventRegistration> registrations = eventRegistrationRepository
                                .findAllByEventRegistrationKeyRegistrant(user);
                return registrations.stream().map(EventRegistrationResponseDto::new).collect(Collectors.toList());
        }

        /**
         * Retrieves all event registrations.
         *
         * @return a list of EventRegistrationResponseDto
         */
        public List<EventRegistrationResponseDto> getAllRegistrations() {
                List<EventRegistration> registrations = (List<EventRegistration>) eventRegistrationRepository.findAll();
                return registrations.stream()
                                .map(EventRegistrationResponseDto::new)
                                .collect(Collectors.toList());
        }

        /**
         * Cancels an event registration.
         *
         * @param participantId the ID of the participant
         * @param eventId       the ID of the event
         */
        @Transactional
        public void cancelRegistration(Long participantId, Long eventId) {
                UserAccount user = userAccountRepository.findUserAccountByUserAccountID(participantId);
                if (user == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "User not found.");
                }

                Event event = eventRepository.findEventByEventID(eventId);
                if (event == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "Event not found.");
                }

                EventRegistrationKey key = new EventRegistrationKey(user, event);
                EventRegistration registration = eventRegistrationRepository.findByEventRegistrationKey(key);
                if (registration == null) {
                        throw new BoardGameException(HttpStatus.NOT_FOUND, "Registration not found.");
                }

                Timestamp eventStartTime = Timestamp
                                .valueOf(LocalDateTime.of(event.getDate().toLocalDate(),
                                                event.getTime().toLocalTime()));
                if (LocalDateTime.now().isAfter(eventStartTime.toLocalDateTime())) {
                        throw new BoardGameException(HttpStatus.BAD_REQUEST,
                                        "Cannot cancel registration for an event that has already started.");
                }

                eventRegistrationRepository.delete(registration);
        }
}
