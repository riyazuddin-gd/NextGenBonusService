package com.example.nextgenBonus.Repository;

import com.example.nextgenBonus.Entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByDistributorId(String distributorId);
}