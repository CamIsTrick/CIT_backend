package cit.camistrick.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomEntryGeneratorTest {

    @Test
    void testRandomCodeGeneratesValidCode() {
        int roomCode = RoomEntryGenerator.randomCode();
        System.out.println(roomCode);
        assertTrue(roomCode >= 0, "non-negative.");
        assertTrue(Integer.toString(roomCode).length() <= 6, "length.");
    }

    @Test
    void testRandomCodeUniqueness() {
        final int numberOfTests = 1000;
        Set<Integer> codes = new HashSet<>();

        for (int i = 0; i < numberOfTests; i++) {
            int code = RoomEntryGenerator.randomCode();
            codes.add(code);
        }

        assertEquals(numberOfTests, codes.size(), "unique.");
    }

    @Test
    void testRoomURLGeneratesProperly() {
        int roomCode = 123456;
        String expectedPrefix = "/room/";
        String roomURL = RoomEntryGenerator.roomURL(roomCode);

        assertNotNull(roomURL, "not-null.");
        assertTrue(roomURL.startsWith(expectedPrefix), "prefix.");
    }

//    @Test
//    void testReleaseCodeRemovesCodeSuccessfully() {
//        int roomCode = RoomEntryGenerator.randomCode();
//        assertTrue(RoomEntryGenerator.usedCodes.containsKey(roomCode), "created");
//
//        RoomEntryGenerator.releaseCode(roomCode);
//        assertFalse(RoomEntryGenerator.usedCodes.containsKey(roomCode), "removed");
//    }

    @Test
    void testRoomURLFormat() {
        int roomCode = RoomEntryGenerator.randomCode();
        String roomURL = RoomEntryGenerator.roomURL(roomCode);

        System.out.println(roomCode);
        System.out.println(roomURL);
        assertNotNull(roomURL, "not-null.");
        assertTrue(roomURL.matches("^/room/[a-f0-9]{64}$"), "expected format.");
    }
}
