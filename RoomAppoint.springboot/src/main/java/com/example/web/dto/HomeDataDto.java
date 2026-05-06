package com.example.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HomeDataDto {

    @JsonProperty("AppointStatus")
    private Integer AppointStatus;

    @JsonProperty("RoomName")
    private String RoomName;

    @JsonProperty("SeatName")
    private String SeatName;

    @JsonProperty("BeginTime")
    private String BeginTime;

    @JsonProperty("TodayStudyMinutes")
    private Integer TodayStudyMinutes;

    @JsonProperty("Balance")
    private BigDecimal Balance;

    @JsonProperty("TotalInUse")
    private Integer TotalInUse;

    @JsonProperty("TotalAvailable")
    private Integer TotalAvailable;

    @JsonProperty("RecommendedRooms")
    private List<RoomSummary> RecommendedRooms;

    @JsonProperty("Rooms")
    private List<RoomSummary> Rooms;

    @JsonProperty("RecentRoomName")
    private String RecentRoomName;

    @JsonProperty("RecentRoomId")
    private Integer RecentRoomId;

    @Data
    public static class RoomSummary {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("Name")
        private String Name;

        @JsonProperty("Cover")
        private String Cover;

        @JsonProperty("TotalSeats")
        private Integer TotalSeats;

        @JsonProperty("AvailableSeats")
        private Integer AvailableSeats;

        @JsonProperty("RecommendTag")
        private String RecommendTag;
    }
}
