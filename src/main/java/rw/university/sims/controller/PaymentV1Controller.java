package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.PaymentCreateRequest;
import rw.university.sims.dto.request.PaymentReverseRequest;
import rw.university.sims.dto.response.*;
import rw.university.sims.enums.PaymentStatus;
import rw.university.sims.service.FinanceService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller {
    private final FinanceService service;
    public PaymentV1Controller(FinanceService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAuthority('PAYMENT_RECORD')")
    public GenericResponse<PaymentResponse> record(@Valid @RequestBody PaymentCreateRequest request,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        PaymentRecordOutcome result=service.recordPayment(request,Long.valueOf(jwt.getSubject()),http.getRemoteAddr());
        return GenericResponse.success(result.created()?201:200,result.created()?"Payment recorded successfully":"Matching payment already exists",result.payment());}
    @PostMapping("/{id}/confirm") @PreAuthorize("hasAuthority('PAYMENT_CONFIRM')")
    public GenericResponse<PaymentResponse> confirm(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(200,"Payment confirmed successfully",service.confirmPayment(id,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
    @PostMapping("/{id}/fail") @PreAuthorize("hasAuthority('PAYMENT_CONFIRM')")
    public GenericResponse<PaymentResponse> fail(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(200,"Payment marked as failed",service.failPayment(id,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
    @PostMapping("/{id}/reverse") @PreAuthorize("hasAuthority('PAYMENT_REVERSE')")
    public GenericResponse<PaymentResponse> reverse(@PathVariable Long id,@Valid @RequestBody PaymentReverseRequest request,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(200,"Payment reversed successfully",service.reversePayment(id,request.reason(),Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
    @GetMapping @PreAuthorize("hasAuthority('BALANCE_READ')")
    public GenericResponse<PageResponse<PaymentResponse>> list(@RequestParam(required=false) Long studentId,@RequestParam(required=false) Long semesterId,
       @RequestParam(required=false) PaymentStatus status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
       @RequestParam(defaultValue="receivedAt") String sortBy,@RequestParam(defaultValue="desc") String direction,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Payments retrieved successfully",service.listPayments(studentId,semesterId,status,page,size,sortBy,direction,Long.valueOf(jwt.getSubject())));}
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('BALANCE_READ')")
    public GenericResponse<PaymentDetailResponse> get(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Payment retrieved successfully",service.getPayment(id,Long.valueOf(jwt.getSubject())));}
    @GetMapping("/mine") @PreAuthorize("hasAuthority('BALANCE_READ')")
    public GenericResponse<PageResponse<PaymentResponse>> mine(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
       @RequestParam(defaultValue="desc") String direction,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Your payments retrieved successfully",service.myPayments(page,size,direction,Long.valueOf(jwt.getSubject())));}
}
