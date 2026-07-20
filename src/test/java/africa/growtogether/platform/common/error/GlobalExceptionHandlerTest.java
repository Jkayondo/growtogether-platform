package africa.growtogether.platform.common.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.web.RequestContextFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest
@ContextConfiguration(classes = GlobalExceptionHandlerTest.ValidationController.class)
@Import({ApiResponses.class, RequestContextFilter.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void returnsStandardValidationFailure() throws Exception {
        mockMvc.perform(post("/test/validate")
                .contentType("application/json")
                .content("{\"name\":\"\",\"email\":\"invalid\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("GT-VALIDATION-001"))
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.metadata.correlationId").exists());
    }

    @RestController
    @RequestMapping("/test")
    static class ValidationController {
        @PostMapping("/validate")
        ResponseEntity<Void> validate(@Valid @RequestBody ValidationRequest request) {
            return ResponseEntity.noContent().build();
        }
    }

    record ValidationRequest(
        @NotBlank(message = "Name is required.") String name,
        @Email(message = "Email must be valid.") String email
    ) {}
}
