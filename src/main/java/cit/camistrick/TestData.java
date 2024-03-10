package cit.camistrick;

import cit.camistrick.service.RoomManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestData {
    private final RoomManager roomManager;

    @Autowired
    public TestData(RoomManager roomManager) {
        this.roomManager = roomManager;
        initTestData();
    }

    private void initTestData() {
        roomManager.createRoom();
    }
}
