package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.FeeObligationCreateRequest;
import rw.university.sims.dto.response.*;
import rw.university.sims.enums.FeeObligationStatus;
import rw.university.sims.service.FinanceService;

@RestController
@RequestMapping("/api/v1/fee-obligations")
public class FeeObligationV1Controller {
    private final FinanceService service;
    public FeeObligationV1Controller(FinanceService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAuthority('FEE_OBLIGATION_MANAGE')")
    public GenericResponse<FeeObligationResponse> create(@Valid @RequestBody FeeObligationCreateRequest request,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(201,"Fee obligation created successfully",service.createObligation(request,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
    @GetMapping @PreAuthorize("hasAuthority('BALANCE_READ')")
    public GenericResponse<PageResponse<FeeObligationResponse>> list(@RequestParam(required=false) Long studentId,@RequestParam(required=false) Long semesterId,
      @RequestParam(required=false) FeeObligationStatus status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
      @RequestParam(defaultValue="createdAt") String sortBy,@RequestParam(defaultValue="desc") String direction,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Fee obligations retrieved successfully",service.listObligations(studentId,semesterId,status,page,size,sortBy,direction,Long.valueOf(jwt.getSubject())));}
    @PatchMapping("/{id}/cancel") @PreAuthorize("hasAuthority('FEE_OBLIGATION_CANCEL')")
    public GenericResponse<FeeObligationResponse> cancel(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(200,"Fee obligation cancelled successfully",service.cancelObligation(id,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
}
