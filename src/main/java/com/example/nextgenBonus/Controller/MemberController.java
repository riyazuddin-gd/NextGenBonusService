package com.example.nextgenBonus.Controller;

import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Model.ChatRequest;
import com.example.nextgenBonus.Model.ChatResponse;
import com.example.nextgenBonus.Model.MemberInformationModel;
import com.example.nextgenBonus.Service.MemberService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/information/{distributorId}")
    public ResponseEntity<MemberInformationModel> getMemberInfo(@PathVariable String distributorId) {
        MemberInformationModel memberInformationModel = memberService.getMemberSummaryByDistributorId(distributorId);
        if (memberInformationModel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memberInformationModel);
    }
}
