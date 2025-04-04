package com.hotel.controller;

import com.hotel.entity.Room;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Visitor;
import com.hotel.service.RoomService;
import com.hotel.service.ReservationService;
import com.hotel.service.VisitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reception")
public class ReceptionController {

    private final RoomService roomService;
    private final ReservationService reservationService;
    private final VisitorRecordService visitorRecordService;

    @Autowired
    public ReceptionController(RoomService roomService, 
                              ReservationService reservationService,
                              VisitorRecordService visitorRecordService) {
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.visitorRecordService = visitorRecordService;
    }

    /**
     * 获取房间状态统计
     */
    @GetMapping("/rooms/statistics")
    public ResponseEntity<Map<String, Long>> getRoomStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("available", roomService.countByStatus(Room.RoomStatus.AVAILABLE));
        statistics.put("occupied", roomService.countByStatus(Room.RoomStatus.OCCUPIED));
        statistics.put("cleaning", roomService.countByStatus(Room.RoomStatus.CLEANING));
        statistics.put("maintenance", roomService.countByStatus(Room.RoomStatus.MAINTENANCE));
        statistics.put("reserved", reservationService.countTodayReservations());
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取所有房间列表
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * 根据状态获取房间列表
     */
    @GetMapping("/rooms/status/{status}")
    public ResponseEntity<List<Room>> getRoomsByStatus(@PathVariable String status) {
        try {
            Room.RoomStatus roomStatus = Room.RoomStatus.valueOf(status.toUpperCase());
            List<Room> rooms = roomService.getRoomsByStatus(roomStatus);
            return ResponseEntity.ok(rooms);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取今日预订的房间
     */
    @GetMapping("/rooms/reserved-today")
    public ResponseEntity<List<Map<String, Object>>> getTodayReservations() {
        List<Map<String, Object>> reservations = reservationService.getTodayReservationsWithDetails();
        return ResponseEntity.ok(reservations);
    }

    /**
     * 根据预订号查询预订信息
     */
    @GetMapping("/booking/{bookingNo}")
    public ResponseEntity<?> getBookingByNumber(@PathVariable String bookingNo) {
        try {
            Map<String, Object> booking = reservationService.getBookingDetailsByNumber(bookingNo);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "查询预订信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 办理入住
     */
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRecord checkInRecord) {
        try {
            // 处理预订入住或直接入住
            CheckInRecord record = reservationService.processCheckIn(checkInRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "入住登记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 办理退房
     */
    @PostMapping("/checkout/{roomNumber}")
    public ResponseEntity<?> checkOut(@PathVariable String roomNumber) {
        try {
            reservationService.processCheckOut(roomNumber);
            Map<String, String> response = new HashMap<>();
            response.put("message", "退房成功，房间已标记为待清洁");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "退房失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 访客登记
     */
    @PostMapping("/visitors")
    public ResponseEntity<?> registerVisitor(@RequestBody Visitor visitor) {
        try {
            Visitor savedVisitor = visitorRecordService.registerVisitor(visitor);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVisitor);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "访客登记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 获取访客列表
     */
    @GetMapping("/visitors")
    public ResponseEntity<List<Visitor>> getVisitors() {
        List<Visitor> visitors = visitorRecordService.getAllVisitors();
        return ResponseEntity.ok(visitors);
    }
} 