
package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;
import rw.university.sims.enums.StudentStatus;

public record StudentStatusUpdateRequest(
        @NotNull StudentStatus status
) {
}
