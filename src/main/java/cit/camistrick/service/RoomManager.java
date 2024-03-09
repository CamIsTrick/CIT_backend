package cit.camistrick.service;

import cit.camistrick.domain.Room;
import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RoomManager {

    private final Logger log = LoggerFactory.getLogger(RoomManager.class);

    @Autowired
    private KurentoClient kurento;

    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    public Room getRoom(String roomId) {
        log.debug("Searching for room {}", roomId);
        Room room = rooms.get(roomId);

        if (room == null) {
            room = createRoom(roomId);
        }

        log.debug("Room {} found!", roomId);
        return room;
    }

    private Room createRoom(String roomId) {
        log.debug("Room {} not existent. Creating now!", roomId);
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
