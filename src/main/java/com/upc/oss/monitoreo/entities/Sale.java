package com.upc.oss.monitoreo.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Builder
@Entity
@Table(name = "venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local", nullable = false)
    private Store store;

    @CreatedDate
    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDate saleDate;

    @Column(name = "monto", nullable = false)
    private BigDecimal amount;

    @Column(name = "descripcion")
    private String description;

    @CreatedDate
    @Column(name = "fecha_registro_sistema")
    private LocalDateTime registeredAt;
}
