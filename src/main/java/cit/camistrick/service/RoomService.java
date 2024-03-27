package cit.camistrick.service;

import cit.camistrick.domain.Room;
import cit.camistrick.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final KurentoClient kurento;
    private final RoomRepository roomRepository;

    public Optional<Room> findRoom(String roomId) {
        log.info("Searching for room {}", roomId);
        return roomRepository.findByRoomId(roomId);
    }

    public Room createRoom() {
        String roomId = UUID.randomUUID().toString();
        log.debug("Room {} not exist. Creating now!", roomId);
        Room room = new Room(roomId, kurento.createMediaPipeline());
        roomRepository.add(room);
        return room;
    }

    public void removeRoom(String roomId) {
        roomRepository.findByRoomId(roomId).ifPresent(room -> {
            log.info("Removing room {}", room.getRoomId());
            roomRepository.remove(room);
            log.info("Room {} removed and closed", room.getRoomId());
        });
    }
}
