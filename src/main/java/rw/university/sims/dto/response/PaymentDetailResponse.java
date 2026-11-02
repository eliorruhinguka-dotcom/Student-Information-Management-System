package rw.university.sims.dto.response;
import java.util.List;
public record PaymentDetailResponse(PaymentResponse payment, List<PaymentStatusHistoryResponse> statusHistory) {}
