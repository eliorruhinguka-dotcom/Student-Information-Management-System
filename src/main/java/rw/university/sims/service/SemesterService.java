package rw.university.sims.service;

import rw.university.sims.dto.request.SemesterRequest;
import rw.university.sims.entity.Semester;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.SemesterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public SemesterService(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    @Transactional
    public Semester createSemester(SemesterRequest request) {
        validateDates(request);

        if (semesterRepository.existsByNameIgnoreCaseAndAcademicYear(
                request.getName(),
                request.getAcademicYear())) {
            throw new ResourceConflictException(
                "A semester with this name and academic year already exists"
            );
        }

        Semester semester = new Semester(
            request.getName().trim(),
            request.getAcademicYear(),
            request.getStartDate(),
            request.getEndDate(),
            request.getMaxCreditLoad()
        );

        return semesterRepository.save(semester);
    }

    @Transactional(readOnly = true)
    public List<Semester> getAllSemesters() {
        return semesterRepository.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public Semester getSemesterById(Long id) {
        return semesterRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Semester not found: " + id)
            );
    }

    @Transactional
    public Semester updateSemester(Long id, SemesterRequest request) {
        validateDates(request);

        Semester semester = getSemesterById(id);

        if (semesterRepository.existsByNameIgnoreCaseAndAcademicYearAndIdNot(
                request.getName(),
                request.getAcademicYear(),
                id)) {
            throw new ResourceConflictException(
                "Another semester already uses this name and academic year"
            );
        }

        semester.setName(request.getName().trim());
        semester.setAcademicYear(request.getAcademicYear());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setMaxCreditLoad(request.getMaxCreditLoad());

        return semesterRepository.save(semester);
    }

    private void validateDates(SemesterRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                "Semester end date cannot be before its start date"
            );
        }
    }
}