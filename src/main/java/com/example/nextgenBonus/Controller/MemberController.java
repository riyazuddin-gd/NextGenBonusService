package com.example.nextgenBonus.Controller;

import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Model.ChatRequest;
import com.example.nextgenBonus.Model.ChatResponse;
import com.example.nextgenBonus.Model.MemberInformationModel;
import com.example.nextgenBonus.Service.MemberService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<MemberInformationModel> getMemberInfo(@RequestParam String distributorId) {
        MemberInformationModel memberInformationModel =  memberService.getMemberSummaryByDistributorId(distributorId);
        return ResponseEntity.ok(memberInformationModel);
    }
}
