package com.andersonmorales.pedidosya.entity;

import com.andersonmorales.pedidosya.enums.CategoriaComercio;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comercios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comercio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaComercio categoria;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean abierto = true;
}