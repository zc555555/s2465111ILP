package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.pizzadrone.controller.RestControllerClass;
import uk.ac.ed.inf.pizzadrone.interfaces.OrderValidation;
import uk.ac.ed.inf.pizzadrone.data.DataService;
import uk.ac.ed.inf.pizzadrone.data.DeliveryPathCalculator;
import uk.ac.ed.inf.pizzadrone.data.LngLatPairRequestForTest;
import uk.ac.ed.inf.pizzadrone.data.PositionForTest;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestControllerClass.class)
public class DistanceToControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderValidation orderValidation;

    @MockBean
    private DataService dataService;

    @MockBean
    private DeliveryPathCalculator deliveryPathCalculator;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void testCalculateDistance_ValidRequest() throws Exception {
        LngLatPairRequestForTest request = new LngLatPairRequestForTest(
                new PositionForTest(-3.186874, 55.944494),
                new PositionForTest(-3.186900, 55.944400)
        );

        String response = mockMvc.perform(post("/distanceTo")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        double actualValue = Double.parseDouble(response);
        assertThat(actualValue, closeTo(0.0001, 1e-5));
    }
    @Test
    void testCalculateDistance_InvalidRequest() throws Exception {

        LngLatPairRequestForTest request = new LngLatPairRequestForTest(null, null);

        mockMvc.perform(post("/distanceTo")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}



