package rw.university.sims.dto.response;

public record GenericResponse<T>(
        int status,
        String code,
        String message,
        T data
) {

    public static <T> GenericResponse<T> success(
            int status,
            String message,
            T data) {

        return new GenericResponse<>(
                status,
                "SUCCESS",
                message,
                data
        );
    }

    public static <T> GenericResponse<T> error(
            int status,
            String code,
            String message) {

        return new GenericResponse<>(
                status,
                code,
                message,
                null
        );
    }
}