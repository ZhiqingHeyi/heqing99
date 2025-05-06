package com.hotel.converter;

import com.hotel.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA AttributeConverter to handle case-insensitive mapping 
 * for the Room.RoomStatus enum.
 */
@Converter(autoApply = true)
public class RoomStatusConverter implements AttributeConverter<Room.RoomStatus, String> {

    private static final Logger logger = LoggerFactory.getLogger(RoomStatusConverter.class);

    @Override
    public String convertToDatabaseColumn(Room.RoomStatus attribute) {
        // Store enum names in uppercase in the database
        return (attribute == null) ? null : attribute.name();
    }

    @Override
    public Room.RoomStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        String upperDbData = dbData.trim().toUpperCase();
        try {
            return Room.RoomStatus.valueOf(upperDbData);
        } catch (IllegalArgumentException e) {
            // Log a warning if an unknown status is found in the database after uppercasing
            logger.warn("Unknown Room.RoomStatus value found in database: '{}'. Returning null.", dbData);
            // Depending on requirements, could return a default status or throw an exception.
            // Returning null might lead to NullPointerExceptions downstream if not handled carefully.
            return null; 
        }
    }
} 