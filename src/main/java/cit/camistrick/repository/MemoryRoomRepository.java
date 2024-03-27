package cit.camistrick.repository;

import cit.camistrick.domain.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class MemoryRoomRepository implements RoomRepository {

    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public Optional<Room> findByRoomId(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    @Override
    public Room add(Room room) {
        rooms.put(room.getRoomId(), room);
        return room;
    }

    @Override
    public void remove(Room room) {
        rooms.remove(room.getRoomId());
        room.close();
    }
}