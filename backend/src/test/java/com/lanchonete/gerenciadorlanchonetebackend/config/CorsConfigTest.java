package com.lanchonete.gerenciadorlanchonetebackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CorsConfigTest.CorsTestController.class)
// Com o SecurityConfig real: o preflight (OPTIONS) é respondido antes de exigir o token
@Import({CorsConfigTest.CorsTestController.class, SecurityConfig.class})
@TestPropertySource(properties = "app.cors.origens=http://localhost:5173,https://lanchonete.vercel.app")
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @RestController
    static class CorsTestController {
        @GetMapping("/api/teste-cors")
        String ok() {
            return "ok";
        }
    }

    @Test
    void permitePreflightDeOrigemConfigurada() throws Exception {
        mockMvc.perform(options("/api/teste-cors")
                        .header(HttpHeaders.ORIGIN, "https://lanchonete.vercel.app")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://lanchonete.vercel.app"));
    }

    @Test
    void recusaOrigemNaoConfigurada() throws Exception {
        mockMvc.perform(options("/api/teste-cors")
                        .header(HttpHeaders.ORIGIN, "https://site-malicioso.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
