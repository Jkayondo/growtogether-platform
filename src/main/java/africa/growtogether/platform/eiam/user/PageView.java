package africa.growtogether.platform.eiam.user;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageView<T>(List<T> content, int page, int size, long totalElements,
                          int totalPages, boolean first, boolean last) {
    static <T> PageView<T> from(Page<T> result) {
        return new PageView<>(result.getContent(), result.getNumber(), result.getSize(),
            result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }
}
