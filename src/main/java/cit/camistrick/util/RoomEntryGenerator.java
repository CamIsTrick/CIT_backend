package cit.camistrick.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class RoomEntryGenerator {

    private static final int ENTRY_CODE_LENGTH = 6;
    private static final String ROOM_URL_PREFIX = "/room/";
    private static final ConcurrentHashMap<Integer, Boolean> usedCodes = new ConcurrentHashMap<>();

    public static int randomCode() {
        int roomCode;
        do {
            roomCode = generateCode();
        } while (usedCodes.putIfAbsent(roomCode, Boolean.TRUE) != null);
        return roomCode;
    }

    private static int generateCode() {
        int roomCode = 0;
        for (int i = 0; i < ENTRY_CODE_LENGTH; i++) {
            roomCode += (int)(Math.random() * 10) * Math.pow(10, ENTRY_CODE_LENGTH - i - 1);
        }
        return roomCode;
    }

    public static String roomURL(int roomCode) {
        String roomCodeStr = String.valueOf(roomCode);
        MessageDigest md = getMessageDigestInstance("SHA-256");
        byte[] messageDigest = md.digest(roomCodeStr.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return ROOM_URL_PREFIX + sb;
    }

    private static MessageDigest getMessageDigestInstance(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to get MessageDigest instance for " + algorithm, e);
        }
    }

    public static void releaseCode(int roomCode) {
        usedCodes.remove(roomCode);
    }
}
