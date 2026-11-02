package rw.university.sims.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Batch20EndpointContractTest {
    @Test
    void batchContainsExactlyTwentyMappedEndpoints() {
        List<Class<?>> controllers = List.of(AdmissionApplicationController.class, StudentV1Controller.class,
                AssessmentV1Controller.class, ResultV1Controller.class);
        long count = controllers.stream().flatMap(type -> java.util.Arrays.stream(type.getDeclaredMethods()))
                .filter(Batch20EndpointContractTest::hasHttpMapping).count();
        assertEquals(20, count);
    }
    private static boolean hasHttpMapping(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }
}
