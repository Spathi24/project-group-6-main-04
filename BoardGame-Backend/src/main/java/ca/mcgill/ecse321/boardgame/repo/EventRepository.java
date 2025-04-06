package ca.mcgill.ecse321.boardgame.repo;

import ca.mcgill.ecse321.boardgame.model.Event;
import ca.mcgill.ecse321.boardgame.model.Game;
import org.springframework.data.repository.CrudRepository;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {
    public Event findEventByEventID(long eventID);

    // Find all events where the date has passed
    List<Event> findByDateBefore(Date date);

    List<Event> findAll();

}
