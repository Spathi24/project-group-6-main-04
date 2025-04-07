package ca.mcgill.ecse321.boardgame.repo;

import ca.mcgill.ecse321.boardgame.model.BorrowRequest;
import ca.mcgill.ecse321.boardgame.model.RequestStatus;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BorrowRequestRepository extends CrudRepository<BorrowRequest, Long> {
    public BorrowRequest findBorrowRequestById(long id);

    @Query("SELECT br FROM BorrowRequest br JOIN br.gameToBorrow gc JOIN gc.gameCopyKey gck JOIN gck.game g WHERE br.borrower = :borrower AND g.title = :gameTitle")
    List<BorrowRequest> findByBorrowerAndGameTitle(UserAccount borrower, String gameTitle);

    List<BorrowRequest> findByBorrowerUserAccountID(long borrowerId);

    @Query("SELECT br FROM BorrowRequest br JOIN br.gameToBorrow gc JOIN gc.gameCopyKey gck JOIN gck.game g WHERE br.borrower = :borrower AND g.title = :gameTitle AND br.status = :requestStatus")
    public List<BorrowRequest> findByBorrowerAndGameTitleAndRequestStatus(UserAccount borrower, String gameTitle,
            RequestStatus requestStatus);

    // Custom query to delete borrow requests by owner and game title
    @Modifying
    @Transactional
    @Query("DELETE FROM BorrowRequest br WHERE br.gameToBorrow.gameCopyKey.owner = :owner AND br.gameToBorrow.gameCopyKey.game.title = :gameTitle")
    void deleteBorrowRequestsByOwnerAndGameTitle(UserAccount owner, String gameTitle);
}
