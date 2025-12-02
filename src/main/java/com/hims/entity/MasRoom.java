package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mas_room")
public class MasRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name",length = 50,nullable = false)
    private String roomName;

    @Column(name = "no_of_beds",nullable = false)
    private  Integer noOfBeds;

    @Column(name = "status",length = 1)
    private String status;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDate lastUpdatedDate;

    @Column(name = "created_by",length = 200)
    private String createdBy;

    @Column(name = "last_updated_by",length = 200)
    private String lastUpdatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_category_id")
    private MasRoomCategory masRoomCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "ward_id")
    private MasWard masWard;


}
