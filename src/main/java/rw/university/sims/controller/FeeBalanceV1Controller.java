package rw.university.sims.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.response.FeeBalanceResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.service.FinanceService;

@RestController
@RequestMapping("/api/v1/students")
public class FeeBalanceV1Controller {
    private final FinanceService service;
    public FeeBalanceV1Controller(FinanceService service){this.service=service;}
    @GetMapping("/{id}/fee-balance") @PreAuthorize("hasAuthority('BALANCE_READ')")
    public GenericResponse<FeeBalanceResponse> studentBalance(@PathVariable Long id,@RequestParam Long semesterId,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Student fee balance retrieved successfully",service.studentBalance(id,semesterId,Long.valueOf(jwt.getSubject())));}
    @GetMapping("/me/fee-balance") @PreAuthorize("hasAuthority('BALANCE_READ')")
    public GenericResponse<FeeBalanceResponse> mine(@RequestParam Long semesterId,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Your fee balance retrieved successfully",service.myBalance(semesterId,Long.valueOf(jwt.getSubject())));}
}
