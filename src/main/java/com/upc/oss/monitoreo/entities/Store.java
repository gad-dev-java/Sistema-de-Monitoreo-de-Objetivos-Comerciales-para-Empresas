package com.upc.oss.monitoreo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "local")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_local")
    private Long idStore;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "direccion")
    private String address;

    @Column(name = "ciudad")
    private String city;

    @Column(name = "estado")
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Company company;

    @Column(name = "ultima_alerta_generada")
    private LocalDateTime lastAlertGenerated;
}
