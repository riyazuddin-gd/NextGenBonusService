package com.example.nextgenBonus.Entities;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "member")  // lowercase for PostgreSQL
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String distributorId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    private String memberLevel;

    private Double totalCC = 0.0;

    // Self-reference for sponsor
    @ManyToOne
    @JoinColumn(name = "sponsor_id")
    private Member sponsor;

    @OneToMany(mappedBy = "sponsor")
    private List<Member> downlines;

    @OneToMany(mappedBy = "member")
    private List<Order> orders;

    // ----- Getters and Setters -----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(String distributorId) {
        this.distributorId = distributorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
    }

    public Double getTotalCC() {
        return totalCC;
    }

    public void setTotalCC(Double totalCC) {
        this.totalCC = totalCC;
    }

    public Member getSponsor() {
        return sponsor;
    }

    public void setSponsor(Member sponsor) {
        this.sponsor = sponsor;
    }

    public List<Member> getDownlines() {
        return downlines;
    }

    public void setDownlines(List<Member> downlines) {
        this.downlines = downlines;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
