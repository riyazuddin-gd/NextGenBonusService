package com.example.nextgenBonus.Repository;
import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;



public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT SUM(o.orderCC) FROM Order o " +
            "WHERE o.member = :member " +
            "AND o.createdDate BETWEEN :start AND :end")
    Double sumOrderCCByMemberAndDateRange(Member member,
                                          LocalDateTime start,
                                          LocalDateTime end);

    @Query("SELECT SUM(o.orderCC) FROM Order o " +
            "WHERE o.member IN :members " +
            "AND o.createdDate BETWEEN :start AND :end")
    Double sumOrderCCByDownlinesAndDateRange(List<Member> members,
                                             LocalDateTime start,
                                             LocalDateTime end);
}
