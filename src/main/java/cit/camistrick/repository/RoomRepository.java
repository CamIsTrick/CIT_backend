package cit.camistrick.repository;

import cit.camistrick.domain.Room;

import java.util.Optional;

public interface RoomRepository {
    Optional<Room> findByRoomId(String roomId);

    Room add(Room room);

    void remove(Room room);
}
