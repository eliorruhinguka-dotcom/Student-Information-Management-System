package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.ExamCardGenerateRequest;
import rw.university.sims.dto.response.*;
import rw.university.sims.service.ExamCardService;

@RestController
@RequestMapping("/api/v1")
public class ExamCardV1Controller {
    private final ExamCardService service;
    public ExamCardV1Controller(ExamCardService service){this.service=service;}
    @PostMapping("/exam-cards") @PreAuthorize("hasAuthority('EXAMCARD_GENERATE')")
    public GenericResponse<ExamCardResponse> generate(@Valid @RequestBody ExamCardGenerateRequest request,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        var outcome=service.generate(request.courseRegistrationId(),Long.valueOf(jwt.getSubject()),http.getRemoteAddr());
        return GenericResponse.success(outcome.created()?201:200,outcome.created()?"Exam card generated successfully":"Exam card was already issued",outcome.card());}
    @GetMapping("/exam-cards/mine") @PreAuthorize("hasAuthority('EXAMCARD_READ')")
    public GenericResponse<PageResponse<ExamCardResponse>> mine(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="desc") String direction,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Your exam cards retrieved successfully",service.mine(page,size,direction,Long.valueOf(jwt.getSubject())));}
    @GetMapping("/exam-cards/verify/{cardNumber}") @PreAuthorize("hasAuthority('EXAMCARD_VERIFY')")
    public GenericResponse<ExamCardVerificationResponse> verify(@PathVariable String cardNumber,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Exam card verification completed",service.verify(cardNumber,Long.valueOf(jwt.getSubject())));}
    @GetMapping("/students/{id}/exam-card-attempts") @PreAuthorize("hasAuthority('EXAMCARD_VERIFY')")
    public GenericResponse<PageResponse<ExamCardAttemptResponse>> attempts(@PathVariable Long id,@RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="desc") String direction,@AuthenticationPrincipal Jwt jwt){
        return GenericResponse.success(200,"Exam card attempts retrieved successfully",service.attempts(id,page,size,direction,Long.valueOf(jwt.getSubject())));}
}
