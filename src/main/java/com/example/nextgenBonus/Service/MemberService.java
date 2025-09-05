package com.example.nextgenBonus.Service;
import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Model.MemberInformationModel;
import com.example.nextgenBonus.Repository.MemberRepository;
import com.example.nextgenBonus.Repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    public MemberService(MemberRepository memberRepository, OrderRepository orderRepository) {
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public MemberInformationModel getMemberSummaryByDistributorId(String distributorId) {
        Member member = memberRepository.findByDistributorId(distributorId);
        if (member == null) {
            throw new RuntimeException("Member not found with distributorId: " + distributorId);
        }

        // Calculate date ranges
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        LocalDateTime thisMonthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime thisMonthEnd = thisMonth.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        // Get downlines
        List<Member> downlines = member.getDownlines();

        // Fetch CC values
        Double thisMonthOrderCC = orderRepository.sumOrderCCByMemberAndDateRange(member, thisMonthStart, thisMonthEnd);
        Double lastMonthOrderCC = orderRepository.sumOrderCCByMemberAndDateRange(member, lastMonthStart, lastMonthEnd);

        Double thisMonthDownlineCC = orderRepository.sumOrderCCByDownlinesAndDateRange(downlines, thisMonthStart, thisMonthEnd);
        Double lastMonthDownlineCC = orderRepository.sumOrderCCByDownlinesAndDateRange(downlines, lastMonthStart, lastMonthEnd);

        MemberInformationModel dto = new MemberInformationModel();
        dto.setName(member.getName());
        dto.setMemberLevel(member.getMemberLevel());
        dto.setTotalCC(member.getTotalCC());
        dto.setDownlineCount((long) downlines.size());
        dto.setThisMonthCC(thisMonthDownlineCC != null ? thisMonthDownlineCC : 0.0);
        dto.setLastMonthCC(lastMonthDownlineCC != null ? lastMonthDownlineCC : 0.0);
        dto.setThisMonthOrderCC(thisMonthOrderCC != null ? thisMonthOrderCC : 0.0);
        dto.setLastMonthOrderCC(lastMonthOrderCC != null ? lastMonthOrderCC : 0.0);

        return dto;
    }
}

