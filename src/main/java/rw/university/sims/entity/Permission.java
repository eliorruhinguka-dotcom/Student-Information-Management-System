package rw.university.sims.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    public Permission() {
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
}