package com.upc.oss.monitoreo.entities;

import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "objetivo_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesObjective {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_objetivo")
    private Long idObjective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local", nullable = false)
    private Store store;

    @Column(name = "monto_objetivo", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "tipo_periodo")
    private String periodType;

    @Column(name = "fecha_inicio")
    private LocalDate startDate;

    @Column(name = "fecha_fin")
    private LocalDate endDate;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private SalesObjectiveStatus status = SalesObjectiveStatus.ACTIVO;
}
