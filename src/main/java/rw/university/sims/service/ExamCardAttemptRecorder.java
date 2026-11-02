package rw.university.sims.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.entity.*;
import rw.university.sims.enums.ExamCardAttemptOutcome;
import rw.university.sims.repository.ExamCardAttemptRepository;

@Service
public class ExamCardAttemptRecorder {
    private final ExamCardAttemptRepository repository;
    private final EntityManager entityManager;

    public ExamCardAttemptRecorder(ExamCardAttemptRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long studentId, Long registrationId, Long cardId,
                       ExamCardAttemptOutcome outcome, String reason,
                       Long requesterId, String ip) {
        Student student = entityManager.getReference(Student.class, studentId);
        CourseRegistration registration = registrationId == null
                ? null : entityManager.getReference(CourseRegistration.class, registrationId);
        ExamCard card = cardId == null ? null : entityManager.getReference(ExamCard.class, cardId);
        UserAccount requester = entityManager.getReference(UserAccount.class, requesterId);
        repository.saveAndFlush(new ExamCardAttempt(student, registration, card, outcome, reason, requester, ip));
    }
}
