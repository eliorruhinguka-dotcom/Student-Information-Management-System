package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.*;
import rw.university.sims.dto.response.*;
import rw.university.sims.service.GradeScaleService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/grade-scales")
public class GradeScaleV1Controller {
    private final GradeScaleService service;
    public GradeScaleV1Controller(GradeScaleService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAuthority('GRADE_SCALE_MANAGE')")
    public GenericResponse<GradeScaleResponse> create(@Valid @RequestBody GradeScaleCreateRequest request,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(201,"Grade scale created successfully",service.create(request,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
    @GetMapping @PreAuthorize("hasAuthority('RESULT_READ_ALL')")
    public GenericResponse<List<GradeScaleResponse>> list(){return GenericResponse.success(200,"Grade scales retrieved successfully",service.list());}
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('GRADE_SCALE_MANAGE')")
    public GenericResponse<GradeScaleResponse> replace(@PathVariable Long id,@Valid @RequestBody GradeScaleUpdateRequest request,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(200,"Grade scale updated successfully",service.replace(id,request,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
    @PostMapping("/{id}/activate") @PreAuthorize("hasAuthority('GRADE_SCALE_MANAGE')")
    public GenericResponse<GradeScaleResponse> activate(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt,HttpServletRequest http){
        return GenericResponse.success(200,"Grade scale activated successfully",service.activate(id,Long.valueOf(jwt.getSubject()),http.getRemoteAddr()));}
}
