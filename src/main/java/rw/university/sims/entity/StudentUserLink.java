package rw.university.sims.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_user_link")
public class StudentUserLink {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    public StudentUserLink() {
    }

    public StudentUserLink(UserAccount user, Student student) {
        this.user = user;
        this.student = student;
    }

    public Long getUserId() { return userId; }
    public UserAccount getUser() { return user; }
    public Student getStudent() { return student; }
}