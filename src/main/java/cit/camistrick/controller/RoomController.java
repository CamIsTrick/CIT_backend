package cit.camistrick.controller;

import cit.camistrick.dto.RoomResponse.RoomDto;
import cit.camistrick.service.RoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomManager roomManager;

    @GetMapping()
    public ResponseEntity<RoomDto> checkRoom(@RequestParam("roomId") String roomId) {
        boolean isExist = roomManager
                .findRoom(roomId)
                .isPresent();

        RoomDto roomDto = RoomDto
                .builder()
                .isValid(isExist)
                .build();

        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }
}
