package com.example.nextgenBonus.Service;

import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Model.Downline;
import com.example.nextgenBonus.Model.MemberInformationModel;
import com.example.nextgenBonus.Repository.MemberRepository;
import com.example.nextgenBonus.Repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<Member> memberOpt = memberRepository.findByDistributorId(distributorId);
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("Member not found with distributorId: " + distributorId);
        }

        Member member = memberOpt.get();

        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        LocalDateTime thisMonthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime thisMonthEnd = thisMonth.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Member> downlines = member.getDownlines();

        Double thisMonthOrderCC = orderRepository.sumOrderCCByMemberAndDateRange(member, thisMonthStart, thisMonthEnd);
        Double lastMonthOrderCC = orderRepository.sumOrderCCByMemberAndDateRange(member, lastMonthStart, lastMonthEnd);

        Double thisMonthDownlineCC = (downlines == null || downlines.isEmpty()) ? 0.0 :
                orderRepository.sumOrderCCByDownlinesAndDateRange(downlines, thisMonthStart, thisMonthEnd);

        Double lastMonthDownlineCC = (downlines == null || downlines.isEmpty()) ? 0.0 :
                orderRepository.sumOrderCCByDownlinesAndDateRange(downlines, lastMonthStart, lastMonthEnd);

        MemberInformationModel dto = new MemberInformationModel();
        List<Downline> downlines1 = downlines.stream()
                .map(member1 -> {
                    System.out.println(member1.getDistributorId());
                  //  Optional<Member> member2 = memberRepository.findByDistributorId(member1.getDistributorId());
                    Downline downline = new Downline();
                    downline.setName(member1.getName());
                    downline.setLevel(member1.getMemberLevel());
                    return downline;
                })
                .collect(Collectors.toList());
        dto.setDownlines(downlines1);
        dto.setName(member.getName());
        dto.setMemberLevel(member.getMemberLevel());
        dto.setId(member.getDistributorId());
        BigDecimal totalcc = member.getTotalcc();
        dto.setTotalCC(BigDecimal.valueOf(totalcc != null ? totalcc.doubleValue() : 0.0));

        dto.setDownlineCount((long) (downlines != null ? downlines.size() : 0));

        dto.setThisMonthCC(thisMonthOrderCC != null ? thisMonthOrderCC : 0.0);
        dto.setLastMonthCC(lastMonthOrderCC != null ? lastMonthOrderCC : 0.0);


        return dto;
    }
}
