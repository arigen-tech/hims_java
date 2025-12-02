package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mas_room_category")
public class MasRoomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_category_id")
    private Long roomCategoryId;

    @Column(name = "room_category_name",length = 50,nullable = false)
    private String roomCategoryName;

    @Column(name = "status")
    private String status;

    @Column(name = "last_update_date")
    private LocalDate lastUpdatedDate;

    @Column(name = "created_by",length = 200)
    private String createdBy;

    @Column(name = "last_updated_by",length = 200)
    private String updatedBy;

}
