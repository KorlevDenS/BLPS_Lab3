package com.denis.korolev.notificationservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "topic")
public class Topic {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ColumnDefault("0")
    @Column(name = "views")
    private Integer views;

    @ColumnDefault("0")
    @Column(name = "fame")
    private Integer fame;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "owner")
    private Client owner;

}