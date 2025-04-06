package ca.mcgill.ecse321.boardgame.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.BorrowRequest;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.GameStatus;
import ca.mcgill.ecse321.boardgame.model.RequestStatus;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.BorrowRequestRepository;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

@SpringBootTest
public class BorrowRequestServiceTests {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private GameCopyRepository gameCopyRepository;

    @InjectMocks
    private BorrowRequestService borrowRequestService;

    private UserAccount borrower;
    private UserAccount owner;
    private GameCopy gameCopy;

    @BeforeEach
    void setUp() {
        borrower = new UserAccount("Borrower", "pass123", "borrower@mail.com", null);
        owner = new UserAccount("Owner", "pass456", "owner@mail.com", null);
        Game game = new Game("Chess", "A game", "BoardGame");
        gameCopy = new GameCopy(new GameCopy.GameCopyKey(owner, game), "Good condition");
    }

    @Test
    public void testCreateBorrowRequest_Success() {
        when(userAccountRepository.findUserAccountByUserAccountID(1L)).thenReturn(borrower);
        when(userAccountRepository.findUserAccountByUserAccountID(2L)).thenReturn(owner);
        when(gameCopyRepository.findAllByGameCopyKeyOwner(owner)).thenReturn(List.of(gameCopy));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BorrowRequest created = borrowRequestService.createBorrowRequest(
                1L, 2L, "Chess",
                Date.valueOf("2025-05-01"),
                Date.valueOf("2025-05-10")
        );
        assertNotNull(created);
        assertEquals(RequestStatus.PENDING, created.getRequestStatus());
    }

    @Test
    public void testCreateBorrowRequest_BorrowerNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(1L)).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> borrowRequestService.createBorrowRequest(
                        1L, 2L, "Chess",
                        Date.valueOf("2025-05-01"),
                        Date.valueOf("2025-05-10")
                )
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("Borrower 1 not found"));
    }

    @Test
    public void testAcceptRequest_Success() {
        BorrowRequest existing = new BorrowRequest(RequestStatus.PENDING, Date.valueOf("2025-04-01"), null,
                Date.valueOf("2025-05-01"), Date.valueOf("2025-05-10"), borrower, gameCopy);
        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowRequest updated = borrowRequestService.acceptRequest(100L);
        assertNotNull(updated);
        assertEquals(RequestStatus.ACCEPTED, updated.getRequestStatus());
    }

    @Test
    public void testAcceptRequest_NotFound() {
        when(borrowRequestRepository.findById(999L)).thenReturn(Optional.empty());
        ResourceNotFoundException e = assertThrows(
                ResourceNotFoundException.class,
                () -> borrowRequestService.acceptRequest(999L)
        );
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    public void testDeclineRequest_Success() {
        BorrowRequest existing = new BorrowRequest(RequestStatus.PENDING, Date.valueOf("2025-04-01"), null,
                Date.valueOf("2025-05-01"), Date.valueOf("2025-05-10"), borrower, gameCopy);
        when(borrowRequestRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowRequest updated = borrowRequestService.declineRequest(11L);
        assertNotNull(updated);
        assertEquals(RequestStatus.DECLINED, updated.getRequestStatus());
    }

    @Test
    public void testDeclineRequest_NotFound() {
        when(borrowRequestRepository.findById(111L)).thenReturn(Optional.empty());
        ResourceNotFoundException e = assertThrows(
                ResourceNotFoundException.class,
                () -> borrowRequestService.declineRequest(111L)
        );
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    public void testGetAllBorrowRequests() {
        BorrowRequest br1 = new BorrowRequest(RequestStatus.PENDING, Date.valueOf("2025-04-01"), null,
                Date.valueOf("2025-05-01"), Date.valueOf("2025-05-10"), borrower, gameCopy);
        BorrowRequest br2 = new BorrowRequest(RequestStatus.PENDING, Date.valueOf("2025-04-02"), null,
                Date.valueOf("2025-05-02"), Date.valueOf("2025-05-11"), borrower, gameCopy);
        when(borrowRequestRepository.findAll()).thenReturn(List.of(br1, br2));

        List<BorrowRequest> all = borrowRequestService.getAllBorrowRequests();
        assertEquals(2, all.size());
    }
}
