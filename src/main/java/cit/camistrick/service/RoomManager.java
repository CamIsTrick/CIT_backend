package cit.camistrick.service;

import cit.camistrick.domain.Room;
import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RoomManager {

    private final Logger log = LoggerFactory.getLogger(RoomManager.class);

    @Autowired
    private KurentoClient kurento;

    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    public Optional<Room> findRoom(String roomId) {
        log.info("Searching for room {}", roomId);
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Room createRoom() {
        String roomId = UUID.randomUUID().toString();
        log.debug("Room {} not exist. Creating now!", roomId);
        Room room = new Room(roomId, kurento.createMediaPipeline());
        rooms.put(roomId, room);
        return room;
    }

    public void removeRoom(Room room) {
        log.info("Removing room {}", room.getRoomId());
        this.rooms.remove(room.getRoomId());
        room.close();
        log.info("Room {} removed and closed", room.getRoomId());
    }
}
