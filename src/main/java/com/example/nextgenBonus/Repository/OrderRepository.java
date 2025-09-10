package com.example.nextgenBonus.Repository;

import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Sum of order_cc for a single member within date range
    @Query("SELECT COALESCE(SUM(o.orderCC), 0) FROM Order o WHERE o.member = :member AND o.createdDate BETWEEN :startDate AND :endDate")
    Double sumOrderCCByMemberAndDateRange(@Param("member") Member member,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Sum of order_cc for a list of members (downlines) within date range
    @Query("SELECT COALESCE(SUM(o.orderCC), 0) FROM Order o WHERE o.member IN :members AND o.createdDate BETWEEN :startDate AND :endDate")
    Double sumOrderCCByDownlinesAndDateRange(@Param("members") List<Member> members,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
