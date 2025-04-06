package ca.mcgill.ecse321.boardgame.repo;

import ca.mcgill.ecse321.boardgame.model.Event;
import ca.mcgill.ecse321.boardgame.model.EventRegistration;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRegistrationRepository extends CrudRepository<EventRegistration,EventRegistration.EventRegistrationKey> {
    public EventRegistration findByEventRegistrationKey(EventRegistration.EventRegistrationKey eventRegistrationKey);

    List<EventRegistration> findAllByEventRegistrationKeyEvent(Event event);

    List<EventRegistration> findAllByEventRegistrationKeyRegistrant(UserAccount userAccount);
}
