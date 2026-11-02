package rw.university.sims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.Programme;

import java.util.List;

public interface ProgrammeRepository
        extends JpaRepository<Programme, Long> {

    List<Programme> findByDepartment_Id(Long departmentId);

    List<Programme> findByActiveTrue();

    List<Programme> findByDepartment_IdAndActiveTrue(
            Long departmentId);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code, Long id);

    long countByDepartment_IdAndActiveTrue(Long departmentId);

}