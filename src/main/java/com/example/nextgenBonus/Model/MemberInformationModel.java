package com.example.nextgenBonus.Model;

public class MemberInformationModel {
    private String name;
    private String memberLevel;
    private Double totalCC;
    private Double thisMonthCC;
    private Double lastMonthCC;
    private Long downlineCount;
    private Double thisMonthOrderCC;
    private Double lastMonthOrderCC;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double getThisMonthCC() {
        return thisMonthCC;
    }

    public void setThisMonthCC(Double thisMonthCC) {
        this.thisMonthCC = thisMonthCC;
    }

    public Double getLastMonthCC() {
        return lastMonthCC;
    }

    public void setLastMonthCC(Double lastMonthCC) {
        this.lastMonthCC = lastMonthCC;
    }

    public Long getDownlineCount() {
        return downlineCount;
    }

    public void setDownlineCount(Long downlineCount) {
        this.downlineCount = downlineCount;
    }

    public Double getThisMonthOrderCC() {
        return thisMonthOrderCC;
    }

    public void setThisMonthOrderCC(Double thisMonthOrderCC) {
        this.thisMonthOrderCC = thisMonthOrderCC;
    }

    public Double getLastMonthOrderCC() {
        return lastMonthOrderCC;
    }

    public void setLastMonthOrderCC(Double lastMonthOrderCC) {
        this.lastMonthOrderCC = lastMonthOrderCC;
    }
}

