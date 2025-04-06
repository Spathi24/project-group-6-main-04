package ca.mcgill.ecse321.boardgame.service;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.BorrowRequest;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.GameStatus;
import ca.mcgill.ecse321.boardgame.model.RequestStatus;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.BorrowRequestRepository;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

@Service
public class BorrowRequestService {

    @Autowired
    private BorrowRequestRepository borrowRequestRepo;
    @Autowired
    private UserAccountRepository userAccountRepo;
    @Autowired
    private GameCopyRepository gameCopyRepo;

    @Transactional
    public BorrowRequest createBorrowRequest(long borrowerId, long ownerId, String gameTitle,
            Date startDate, Date endDate) {
        UserAccount borrower = userAccountRepo.findUserAccountByUserAccountID(borrowerId);
        if (borrower == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Borrower " + borrowerId + " not found");
        }
        GameCopy copy = findGameCopy(ownerId, gameTitle);
        BorrowRequest br = new BorrowRequest(
                RequestStatus.PENDING,
                new Date(System.currentTimeMillis()),
                null,
                startDate,
                endDate,
                borrower,
                copy);
        return borrowRequestRepo.save(br);
    }

    @Transactional
    public BorrowRequest acceptRequest(long requestId) {
        BorrowRequest br = findRequestOrThrow(requestId);
        setDecisionDate(br, new Date(System.currentTimeMillis()));
        setRequestStatus(br, RequestStatus.ACCEPTED);
        setGameCopyStatus(br.getGameToBorrow(), GameStatus.BORROWED);
        return borrowRequestRepo.save(br);
    }

    @Transactional
    public BorrowRequest declineRequest(long requestId) {
        BorrowRequest br = findRequestOrThrow(requestId);
        setDecisionDate(br, new Date(System.currentTimeMillis()));
        setRequestStatus(br, RequestStatus.DECLINED);
        return borrowRequestRepo.save(br);
    }

    @Transactional
    public BorrowRequest getBorrowRequest(long requestId) {
        return findRequestOrThrow(requestId);
    }

    @Transactional
    public List<BorrowRequest> getAllBorrowRequests() {
        return (List<BorrowRequest>) borrowRequestRepo.findAll();
    }

    @Transactional
    public List<BorrowRequest> getBorrowRequestsByUserId(long userId) {
        UserAccount user = userAccountRepo.findUserAccountByUserAccountID(userId);
        if (user == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, "User " + userId + " not found");
        }
        return borrowRequestRepo.findByBorrowerUserAccountID(userId);
    }

    private BorrowRequest findRequestOrThrow(long requestId) {
        Optional<BorrowRequest> opt = borrowRequestRepo.findById(requestId);
        if (opt.isEmpty()) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, "BorrowRequest not found");
        }
        return opt.get();
    }

    private GameCopy findGameCopy(long ownerId, String gameTitle) {
        UserAccount owner = userAccountRepo.findUserAccountByUserAccountID(ownerId);
        if (owner == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Owner " + ownerId + " not found");
        }
        return gameCopyRepo.findAllByGameCopyKeyOwner(owner).stream()
                .filter(gc -> gc.getGameCopyKey().getGame().getTitle().equals(gameTitle))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatus.NOT_FOUND, "Game copy not found"));
    }

    private void setGameCopyStatus(GameCopy copy, GameStatus newStatus) {
        try {
            Field f = GameCopy.class.getDeclaredField("status");
            f.setAccessible(true);
            f.set(copy, newStatus);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set game copy status via reflection", e);
        }
    }

    private void setDecisionDate(BorrowRequest br, Date date) {
        try {
            Field f = BorrowRequest.class.getDeclaredField("decisionDate");
            f.setAccessible(true);
            f.set(br, date);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set decisionDate via reflection", e);
        }
    }

    private void setRequestStatus(BorrowRequest br, RequestStatus rs) {
        try {
            Field f = BorrowRequest.class.getDeclaredField("status");
            f.setAccessible(true);
            f.set(br, rs);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set requestStatus via reflection", e);
        }
    }
}
