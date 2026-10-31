import java.util.ArrayList;
import java.util.Arrays;

public class DataStore {
    static ArrayList<String[]> customers = new ArrayList<>();

    static String[] rooms = {"101", "102", "103", "104"};
    static boolean[] booked = new boolean[4];

    static boolean addRoom(String roomNumber) {
        String normalized = roomNumber == null ? "" : roomNumber.trim();
        if (normalized.isEmpty() || normalized.length() > 12
                || Arrays.asList(rooms).contains(normalized)) {
            return false;
        }
        rooms = Arrays.copyOf(rooms, rooms.length + 1);
        booked = Arrays.copyOf(booked, booked.length + 1);
        rooms[rooms.length - 1] = normalized;
        return true;
    }

    static boolean renameRoom(int index, String roomNumber) {
        String normalized = roomNumber == null ? "" : roomNumber.trim();
        if (index < 0 || index >= rooms.length || booked[index] || normalized.isEmpty()
                || normalized.length() > 12
                || (Arrays.asList(rooms).contains(normalized) && !rooms[index].equals(normalized))) {
            return false;
        }
        rooms[index] = normalized;
        return true;
    }

    static boolean removeRoom(int index) {
        if (index < 0 || index >= rooms.length || booked[index]) {
            return false;
        }
        String[] updatedRooms = new String[rooms.length - 1];
        boolean[] updatedBookings = new boolean[booked.length - 1];
        System.arraycopy(rooms, 0, updatedRooms, 0, index);
        System.arraycopy(rooms, index + 1, updatedRooms, index, rooms.length - index - 1);
        System.arraycopy(booked, 0, updatedBookings, 0, index);
        System.arraycopy(booked, index + 1, updatedBookings, index, booked.length - index - 1);
        rooms = updatedRooms;
        booked = updatedBookings;
        return true;
    }
}