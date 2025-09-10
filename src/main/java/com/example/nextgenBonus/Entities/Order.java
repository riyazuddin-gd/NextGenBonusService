package com.example.nextgenBonus.Entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")  // matches your PostgreSQL table name "orders"
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // many orders belong to one member
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "order_cc", precision = 10, scale = 2, nullable = false)
    private BigDecimal orderCC;

    // Constructors
    public Order() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public BigDecimal getOrderCC() {
        return orderCC;
    }

    public void setOrderCC(BigDecimal orderCC) {
        this.orderCC = orderCC;
    }
}
