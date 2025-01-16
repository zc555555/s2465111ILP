package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.pizzadrone.controller.RestControllerClass;
import uk.ac.ed.inf.pizzadrone.data.NextPositionRequestForTest;
import uk.ac.ed.inf.pizzadrone.data.PositionForTest;
import uk.ac.ed.inf.pizzadrone.data.DataService;
import uk.ac.ed.inf.pizzadrone.data.DeliveryPathCalculator;
import uk.ac.ed.inf.pizzadrone.interfaces.OrderValidation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestControllerClass.class)
class NextPositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private NextPositionRequestForTest validRequest;
    private NextPositionRequestForTest invalidRequest;

    @MockBean
    private OrderValidation orderValidation;

    @MockBean
    private DataService dataService;

    @MockBean
    private DeliveryPathCalculator deliveryPathCalculator;

    @BeforeEach
    void setUp() {
        // Valid request setup
        PositionForTest validStart = new PositionForTest(55.944425, -3.188396);
        validRequest = new NextPositionRequestForTest();
        validRequest.setStart(validStart);
        validRequest.setAngle(90.0);

        // Invalid request setup (null start position)
        invalidRequest = new NextPositionRequestForTest();
        invalidRequest.setStart(null);
        invalidRequest.setAngle(90.0);

        // Mock DeliveryPathCalculator response
        PositionForTest nextPosition = new PositionForTest(55.944425, -3.188246); // Corrected mocked result
        when(deliveryPathCalculator.calculateNextPosition(validStart, 90.0))
                .thenReturn(nextPosition);
    }

    @Test
    void testNextPosition_ValidRequest() throws Exception {
        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lat").value(-3.188246))
                .andExpect(jsonPath("$.lng").value(55.944425));
    }

    @Test
    void testNextPosition_InvalidRequest() throws Exception {
        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
