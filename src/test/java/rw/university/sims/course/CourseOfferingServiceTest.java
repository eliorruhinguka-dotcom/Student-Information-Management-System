
package rw.university.sims.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseOfferingServiceTest {

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @InjectMocks
    private CourseOfferingService courseOfferingService;

    private CourseOffering courseOffering;

    @BeforeEach
    void setUp() {
        courseOffering = new CourseOffering(
                null, "CS101", "Introduction to Computer Science", 50
        );
    }

    @Test
    void shouldCreateCourseOffering() {
        when(courseOfferingRepository.existsByCourseCode("CS101"))
                .thenReturn(false);
        when(courseOfferingRepository.save(courseOffering))
                .thenReturn(courseOffering);

        CourseOffering result =
                courseOfferingService.createCourseOffering(courseOffering);

        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        verify(courseOfferingRepository).save(courseOffering);
    }

    @Test
    void shouldRejectDuplicateCourseCode() {
        when(courseOfferingRepository.existsByCourseCode("CS101"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> courseOfferingService.createCourseOffering(courseOffering));

        verify(courseOfferingRepository, never()).save(any(CourseOffering.class));
    }

    @Test
    void shouldReturnAllCourseOfferings() {
        when(courseOfferingRepository.findAll())
                .thenReturn(List.of(courseOffering));

        List<CourseOffering> result =
                courseOfferingService.getAllCourseOfferings();

        assertEquals(1, result.size());
        assertEquals("CS101", result.get(0).getCourseCode());
    }

    @Test
    void shouldReturnCourseOfferingById() {
        when(courseOfferingRepository.findById(1L))
                .thenReturn(Optional.of(courseOffering));

        CourseOffering result =
                courseOfferingService.getCourseOfferingById(1L);

        assertEquals("CS101", result.getCourseCode());
    }

    @Test
    void shouldThrowWhenCourseOfferingDoesNotExist() {
        when(courseOfferingRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> courseOfferingService.getCourseOfferingById(99L));
    }
}
