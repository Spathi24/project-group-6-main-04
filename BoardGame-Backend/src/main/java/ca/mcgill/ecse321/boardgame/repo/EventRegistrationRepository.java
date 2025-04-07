package ca.mcgill.ecse321.boardgame.repo;

import ca.mcgill.ecse321.boardgame.model.Event;
import ca.mcgill.ecse321.boardgame.model.EventRegistration;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRegistrationRepository
        extends CrudRepository<EventRegistration, EventRegistration.EventRegistrationKey> {
    public EventRegistration findByEventRegistrationKey(EventRegistration.EventRegistrationKey eventRegistrationKey);

    List<EventRegistration> findAllByEventRegistrationKeyEvent(Event event);

    List<EventRegistration> findAllByEventRegistrationKeyRegistrant(UserAccount userAccount);

    // Custom query to find events that a user is registered for
    @Query("SELECT er.eventRegistrationKey.event FROM EventRegistration er WHERE er.eventRegistrationKey.registrant = :user")
    List<Event> findEventsByEventRegistrationKeyRegistrant(@Param("user") UserAccount userAccount);
}
