package ca.mcgill.ecse321.boardgame.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.Valid;
import ca.mcgill.ecse321.boardgame.dto.BorrowRequestRequestDto;
import ca.mcgill.ecse321.boardgame.dto.BorrowRequestResponseDto;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.BorrowRequest;
import ca.mcgill.ecse321.boardgame.service.BorrowRequestService;

@RestController
@RequestMapping("/api/borrowrequests")
@CrossOrigin(origins = "http://localhost:5173")
public class BorrowRequestController {

    @Autowired
    private BorrowRequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowRequestResponseDto createBorrowRequest(@RequestBody @Valid BorrowRequestRequestDto dto) {
        BorrowRequest br = service.createBorrowRequest(
                dto.getBorrowerId(),
                dto.getOwnerId(),
                dto.getGameTitle(),
                dto.getStartDate(),
                dto.getEndDate());
        return new BorrowRequestResponseDto(br);
    }

    @PutMapping("/{requestId}/accept")
    public BorrowRequestResponseDto acceptRequest(@PathVariable long requestId) {
        BorrowRequest br = service.acceptRequest(requestId);
        return new BorrowRequestResponseDto(br);
    }

    @PutMapping("/{requestId}/decline")
    public BorrowRequestResponseDto declineRequest(@PathVariable long requestId) {
        BorrowRequest br = service.declineRequest(requestId);
        return new BorrowRequestResponseDto(br);
    }

    @GetMapping("/{requestId}")
    public BorrowRequestResponseDto getBorrowRequest(@PathVariable long requestId) {
        BorrowRequest br = service.getBorrowRequest(requestId);
        return new BorrowRequestResponseDto(br);
    }

    @GetMapping
    public List<BorrowRequestResponseDto> getAllBorrowRequests() {
        return service.getAllBorrowRequests().stream()
                .map(BorrowRequestResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public List<BorrowRequestResponseDto> getBorrowRequestsByUserId(@PathVariable long userId) {
        return service.getBorrowRequestsByUserId(userId).stream()
                .map(BorrowRequestResponseDto::new)
                .collect(Collectors.toList());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("errors", List.of(ex.getMessage()));
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("errors", List.of("Invalid parameter: " + ex.getName()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
