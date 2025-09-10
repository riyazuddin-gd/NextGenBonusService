package com.example.nextgenBonus.Entities;




import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "member")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "sponsor", cascade = CascadeType.ALL)
    private List<Member> downlines;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime createdDate;

    @Column(name = "distributor_id", nullable = false, unique = true, length = 50)
    private String distributorId;

    @Column(name = "member_level", length = 20)
    private String memberLevel;

    @Column(name = "name", length = 100)
    private String name;

//    @ManyToOne
//    @JoinColumn(name = "sponsor_id", referencedColumnName = "distributor_id")
//    private Member sponsor;
@ManyToOne
@JoinColumn(name = "sponsor_id", referencedColumnName = "distributor_id")
@JsonIgnore
private Member sponsor;
    @Column(name = "totalcc", precision = 15, scale = 2)
    private BigDecimal totalcc;
    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    // Constructors
    public Member() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<Member> getDownlines() { return downlines; }
    public void setDownlines(List<Member> downlines) { this.downlines = downlines; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }

    public String getMemberLevel() { return memberLevel; }
    public void setMemberLevel(String memberLevel) { this.memberLevel = memberLevel; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Member getSponsor() { return sponsor; }
    public void setSponsor(Member sponsor) { this.sponsor = sponsor; }

    public BigDecimal getTotalcc() { return totalcc; }
    public void setTotalcc(BigDecimal totalcc) { this.totalcc = totalcc; }
}

