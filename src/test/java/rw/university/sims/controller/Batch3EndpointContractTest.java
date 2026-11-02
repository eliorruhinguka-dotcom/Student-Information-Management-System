package rw.university.sims.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Batch3EndpointContractTest {
    @Test void thisBatchDeclaresExactlyTwentyEndpoints(){
        List<Class<?>> controllers=List.of(GradeScaleV1Controller.class,FeeObligationV1Controller.class,
                PaymentV1Controller.class,FeeBalanceV1Controller.class,ExamCardV1Controller.class);
        long count=controllers.stream().flatMap(c->List.of(c.getDeclaredMethods()).stream())
                .filter(Batch3EndpointContractTest::isRoute).count();
        assertEquals(20,count,"Batch 3 must declare exactly 20 mapped endpoints");
    }
    private static boolean isRoute(Method m){
        return m.isAnnotationPresent(GetMapping.class)||m.isAnnotationPresent(PostMapping.class)
                ||m.isAnnotationPresent(PutMapping.class)||m.isAnnotationPresent(PatchMapping.class)
                ||m.isAnnotationPresent(DeleteMapping.class)||m.isAnnotationPresent(RequestMapping.class);
    }
}
