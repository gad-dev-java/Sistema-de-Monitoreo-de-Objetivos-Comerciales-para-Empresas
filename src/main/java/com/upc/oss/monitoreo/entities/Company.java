package com.upc.oss.monitoreo.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@EntityListeners(AuditingEntityListener.class)
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
    private Boolean status;

    @CreatedDate
    @Column(name = "fecha_registro", updatable = false, nullable = false)
    private LocalDate createdAt;

    @LastModifiedDate
    @Column(name = "ultima_actualizacion")
    private LocalDate updatedAt;
}
