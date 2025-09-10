package com.example.nextgenBonus.Repository;

import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Model.Downline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByDistributorId(String distributorId);



}