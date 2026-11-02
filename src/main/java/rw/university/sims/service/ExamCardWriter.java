package rw.university.sims.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.entity.*;
import rw.university.sims.repository.ExamCardRepository;

@Service
public class ExamCardWriter {
    private final ExamCardRepository repository;
    private final EntityManager entityManager;

    public ExamCardWriter(ExamCardRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(Long registrationId, Long studentId, Long semesterId,
                       Long requestedById, String cardNumber) {
        CourseRegistration registration = entityManager.getReference(CourseRegistration.class, registrationId);
        Student student = entityManager.getReference(Student.class, studentId);
        Semester semester = entityManager.getReference(Semester.class, semesterId);
        UserAccount requester = entityManager.getReference(UserAccount.class, requestedById);
        repository.saveAndFlush(new ExamCard(registration, student, semester, cardNumber, requester));
    }
}
