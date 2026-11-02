package rw.university.sims.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_prerequisite")
public class CoursePrerequisite {

    @EmbeddedId
    private CoursePrerequisiteId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @MapsId("prerequisiteCourseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;

    public CoursePrerequisite() {
    }

    public CoursePrerequisite(Course course, Course prerequisiteCourse) {
        this.course = course;
        this.prerequisiteCourse = prerequisiteCourse;
        this.id = new CoursePrerequisiteId(
                course.getId(),
                prerequisiteCourse.getId()
        );
    }

    public CoursePrerequisiteId getId() {
        return id;
    }

    public void setId(CoursePrerequisiteId id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Course getPrerequisiteCourse() {
        return prerequisiteCourse;
    }

    public void setPrerequisiteCourse(Course prerequisiteCourse) {
        this.prerequisiteCourse = prerequisiteCourse;
    }
}