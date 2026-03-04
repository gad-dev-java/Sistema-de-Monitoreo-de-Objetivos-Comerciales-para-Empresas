package com.upc.oss.monitoreo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idCompany;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String ruc;

    @Column(name = "estado")
    private Boolean status = true;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDate createdAt;

    @Column(name = "ultima_actualizacion")
    private LocalDate updatedAt;
}
