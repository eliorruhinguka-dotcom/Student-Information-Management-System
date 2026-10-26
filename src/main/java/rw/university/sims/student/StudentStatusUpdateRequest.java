
package rw.university.sims.student;

import jakarta.validation.constraints.NotNull;

public record StudentStatusUpdateRequest(
        @NotNull StudentStatus status
) {
}
