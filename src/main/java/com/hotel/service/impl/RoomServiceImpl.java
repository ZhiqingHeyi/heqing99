package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.service.RoomService;
import com.hotel.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ApplicationContext applicationContext;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, 
                          RoomTypeRepository roomTypeRepository,
                          ApplicationContext applicationContext) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.applicationContext = applicationContext;
    }
    
    // 懒加载ReservationService以避免循环依赖
    private ReservationService getReservationService() {
        return applicationContext.getBean(ReservationService.class);
    }

    @Override
    public Page<Room> getRoomsWithFilters(Integer floor, String roomTypeId, String status, String keyword, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Room> query = cb.createQuery(Room.class);
        Root<Room> root = query.from(Room.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // 按楼层筛选
        if (floor != null) {
            predicates.add(cb.equal(root.get("floor"), floor));
        }
        
        // 按房间类型筛选
        if (roomTypeId != null && !roomTypeId.isEmpty()) {
            try {
                Long typeId = Long.parseLong(roomTypeId);
                predicates.add(cb.equal(root.get("roomType").get("id"), typeId));
            } catch (NumberFormatException e) {
                // 忽略无效的房间类型ID
            }
        }
        
        // 按状态筛选
        if (status != null && !status.isEmpty()) {
            try {
                Room.RoomStatus roomStatus = Room.RoomStatus.valueOf(status);
                predicates.add(cb.equal(root.get("status"), roomStatus));
            } catch (IllegalArgumentException e) {
                // 忽略无效的状态值
            }
        }
        
        // 关键词搜索(房间号)
        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(cb.like(root.get("roomNumber"), "%" + keyword + "%"));
        }
        
        // 组合所有条件
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // 计算总记录数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Room> countRoot = countQuery.from(Room.class);
        countQuery.select(cb.count(countRoot));
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // 获取分页数据
        TypedQuery<Room> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Room> rooms = typedQuery.getResultList();
        
        return new PageImpl<>(rooms, pageable, total);
    }

    @Override
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    @Override
    public Room getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber).orElse(null);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> getRoomsByStatus(Room.RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    @Override
    public List<Room> getRoomsByType(RoomType type) {
        return roomRepository.findByRoomType(type);
    }

    @Override
    public List<Room> getRoomsNeedCleaning() {
        return roomRepository.findByNeedCleaningTrue();
    }

    @Override
    public Room updateRoomStatus(Long roomId, Room.RoomStatus status) {
        Room room = getRoomById(roomId);
        if (room != null) {
            room.setStatus(status);
            return roomRepository.save(room);
        }
        return null;
    }

    @Override
    public void markRoomNeedCleaning(Long roomId) {
        Room room = getRoomById(roomId);
        if (room != null) {
            room.setNeedCleaning(true);
            roomRepository.save(room);
        }
    }

    @Override
    public void confirmRoomCleaned(Long roomId) {
        Room room = getRoomById(roomId);
        if (room != null) {
            room.setNeedCleaning(false);
            room.setStatus(Room.RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }
    }

    @Override
    public List<Room> getAvailableRoomsByType(RoomType type) {
        List<Room.RoomStatus> statuses = new ArrayList<>();
        statuses.add(Room.RoomStatus.AVAILABLE);
        statuses.add(Room.RoomStatus.CLEANING);
        return roomRepository.findByRoomTypeAndStatusIn(type, statuses);
    }

    @Override
    public List<Room> getBookableRooms() {
        return roomRepository.findAllAvailableAndCleaningRooms();
    }

    @Override
    public long countAvailableRooms() {
        return roomRepository.countByStatus(Room.RoomStatus.AVAILABLE);
    }

    @Override
    public boolean isRoomNumberExists(String roomNumber) {
        return roomRepository.existsByRoomNumber(roomNumber);
    }

    @Override
    public long countByStatus(Room.RoomStatus status) {
        return roomRepository.countByStatus(status);
    }

    @Override
    public Room getRoomByTypeId(Long typeId) {
        // 根据房间类型ID查找对应类型
        Optional<RoomType> roomType = roomTypeRepository.findById(typeId);
        if (!roomType.isPresent()) {
            return null;
        }
        
        // 查找该类型的可用房间
        List<Room> availableRooms = getAvailableRoomsByType(roomType.get());
        if (availableRooms.isEmpty()) {
            return null;
        }
        
        // 返回第一个可用的房间
        return availableRooms.get(0);
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    @Override
    public RoomType addRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }
    
    @Override
    public RoomType getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }
    
    @Override
    public boolean isRoomTypeInUse(Long typeId) {
        return roomRepository.existsByRoomTypeId(typeId);
    }

    @Override
    public RoomType updateRoomType(Long id, RoomType roomTypeDetails) {
        RoomType existingType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("房间类型不存在，ID: " + id));

        // 检查名称是否变更以及是否冲突
        if (roomTypeDetails.getName() != null && !roomTypeDetails.getName().equals(existingType.getName())) {
            if (roomTypeRepository.existsByName(roomTypeDetails.getName())) {
                throw new RuntimeException("房间类型名称 '" + roomTypeDetails.getName() + "' 已存在");
            }
            existingType.setName(roomTypeDetails.getName());
        }

        // 更新其他字段
        if (roomTypeDetails.getBasePrice() != null) {
            existingType.setBasePrice(roomTypeDetails.getBasePrice());
        }
        if (roomTypeDetails.getCapacity() != null) {
            existingType.setCapacity(roomTypeDetails.getCapacity());
        }
        if (roomTypeDetails.getAmenities() != null) {
            existingType.setAmenities(roomTypeDetails.getAmenities());
        }
        if (roomTypeDetails.getDescription() != null) {
            existingType.setDescription(roomTypeDetails.getDescription());
        }
        
        // 注意：这里没有更新 createTime，updateTime 会由 JPA 自动处理

        return roomTypeRepository.save(existingType);
    }

    @Override
    public void deleteRoomType(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new RuntimeException("房间类型不存在，ID: " + id);
        }
        // 注意：在使用此方法前，应在 Controller 层检查 isRoomTypeInUse
        roomTypeRepository.deleteById(id);
    }
    
    @Override
    public long countAvailableRoomsByType(Long typeId) {
        Optional<RoomType> roomType = roomTypeRepository.findById(typeId);
        if (!roomType.isPresent()) {
            return 0;
        }
        
        List<Room> availableRooms = getAvailableRoomsByType(roomType.get());
        return availableRooms.size();
    }
    
    @Override
    public int getAvailableRoomsCountByTypeAndDateRange(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // 获取指定类型的所有房间
        Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(typeId);
        if (!roomTypeOpt.isPresent()) {
            return 0;
        }
        
        RoomType roomType = roomTypeOpt.get();
        List<Room> roomsOfType = getRoomsByType(roomType);
        
        // 统计可用房间数量
        int availableCount = 0;
        
        for (Room room : roomsOfType) {
            // 首先检查房间当前状态是否可用
            if (room.getStatus() != Room.RoomStatus.AVAILABLE && 
                room.getStatus() != Room.RoomStatus.CLEANING) {
                continue;
            }
            
            // 再检查该房间在指定时间段内是否已被预订
            boolean isAvailable = getReservationService().isRoomAvailable(
                room.getId(), 
                checkIn.toString(),
                checkOut.toString()
            );
            
            if (isAvailable) {
                availableCount++;
            }
        }
        
        return availableCount;
    }
}