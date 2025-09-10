package com.example.nextgenBonus.Model;

import java.math.BigDecimal;
import java.util.List;

public class MemberInformationModel {
    private String id ;
    private String name;
    private String memberLevel;
    private BigDecimal totalCC;
    private Double thisMonthCC;
    private Double lastMonthCC;
    private Long downlineCount;
    private List<Downline> downline;

    // Getters and setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMemberLevel() { return memberLevel; }
    public void setMemberLevel(String memberLevel) { this.memberLevel = memberLevel; }

    public BigDecimal getTotalCC() { return totalCC; }
    public void setTotalCC(BigDecimal totalCC) { this.totalCC = totalCC; }

    public Double getThisMonthCC() { return thisMonthCC; }
    public void setThisMonthCC(Double thisMonthCC) { this.thisMonthCC = thisMonthCC; }

    public Double getLastMonthCC() { return lastMonthCC; }
    public void setLastMonthCC(Double lastMonthCC) { this.lastMonthCC = lastMonthCC; }

    public Long getDownlineCount() { return downlineCount; }
    public void setDownlineCount(Long downlineCount) { this.downlineCount = downlineCount; }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Downline> getDownline() {
        return downline;
    }

    public void setDownlines(List<Downline> downlines) {
        this.downline = downlines;
    }
}
