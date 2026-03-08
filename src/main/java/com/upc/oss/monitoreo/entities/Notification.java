package com.upc.oss.monitoreo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local")
    private Store store;

    @Column(name = "tipo")
    private String alertType;

    @Column(name = "nivel_gravedad")
    private String severityLevel;

    @Column(name = "mensaje")
    private String message;

    @Column(name = "fecha_generada", updatable = false)
    private LocalDate generatedAt;

    @Column(name = "leida")
    private Boolean isRead = false;
}
