
package rw.university.sims;

import org.junit.jupiter.api.Test;
import rw.university.sims.course.CourseOffering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CourseOfferingTest {

    @Test
    void shouldCreateCourseOffering() {
        CourseOffering course = new CourseOffering(
                1L,
                "CS101",
                "Introduction to Programming",
                40
        );

        assertEquals(1L, course.getId());
        assertEquals("CS101", course.getCourseCode());
        assertEquals("Introduction to Programming", course.getCourseName());
        assertEquals(40, course.getCapacity());
    }

    @Test
    void shouldCreateCourseOfferingWithNoId() {
        CourseOffering course = new CourseOffering(
                null,
                "CS102",
                "Database Systems",
                35
        );

        assertNotNull(course);
        assertEquals("CS102", course.getCourseCode());
        assertEquals(35, course.getCapacity());
    }
}
