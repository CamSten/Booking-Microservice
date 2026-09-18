package com.example.bookingapp.service;

import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllRoomsReturnsListOfRooms() {
        Room room = new Room();
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> rooms = roomService.getAllRooms();

        assertEquals(1, rooms.size());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void getRoomByIdReturnsRoomWhenExists() {
        Room room = new Room();
        room.setRoomNumber("101");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room foundRoom = roomService.getRoomById(1L);

        assertEquals("101", foundRoom.getRoomNumber());
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void getRoomByIdThrowsExceptionWhenNotExists() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(99L));
        verify(roomRepository, times(1)).findById(99L);
    }

    @Test
    void saveRoomReturnsSavedRoom() {
        Room roomToSave = new Room();
        roomToSave.setRoomNumber("201");
        when(roomRepository.save(any(Room.class))).thenReturn(roomToSave);

        Room savedRoom = roomService.saveRoom(new Room());

        assertEquals("201", savedRoom.getRoomNumber());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void deleteRoomCallsRepositoryDelete() {
        roomService.deleteRoom(1L);

        verify(roomRepository, times(1)).deleteById(1L);
    }
}