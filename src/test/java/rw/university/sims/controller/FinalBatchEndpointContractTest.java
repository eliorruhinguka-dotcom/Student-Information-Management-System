package rw.university.sims.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinalBatchEndpointContractTest {
    @Test
    void finalBatchDeclaresExactlySixEndpoints() {
        List<Class<?>> controllers = List.of(
                ResultCorrectionExtensionV1Controller.class,
                ReportV1Controller.class);
        long count = controllers.stream()
                .flatMap(type -> List.of(type.getDeclaredMethods()).stream())
                .filter(FinalBatchEndpointContractTest::isRoute)
                .count();
        assertEquals(6, count, "Final SIMS batch must declare exactly six endpoint methods");
    }

    private static boolean isRoute(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }
}
