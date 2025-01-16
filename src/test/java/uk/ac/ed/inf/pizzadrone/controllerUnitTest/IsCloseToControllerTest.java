package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.pizzadrone.controller.RestControllerClass;
import uk.ac.ed.inf.pizzadrone.data.LngLatPairRequestForTest;
import uk.ac.ed.inf.pizzadrone.data.PositionForTest;
import uk.ac.ed.inf.pizzadrone.data.DataService;
import uk.ac.ed.inf.pizzadrone.data.DeliveryPathCalculator;
import uk.ac.ed.inf.pizzadrone.interfaces.OrderValidation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestControllerClass.class)
public class IsCloseToControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderValidation orderValidation;

    @MockBean
    private DataService dataService;

    @MockBean
    private DeliveryPathCalculator deliveryPathCalculator;

    @Test
    public void testIsCloseTo_ValidRequest_False() throws Exception {

        String json = """
        {
            "position1": {"lng": -3.186874, "lat": 55.944494},
            "position2": {"lng": -3.1875, "lat": 55.945}
        }
    """;

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    public void testIsCloseTo_ValidRequest_True() throws Exception {

        String json = """
        {
            "position1": {"lng": -3.186874, "lat": 55.944494},
            "position2": {"lng": -3.1869, "lat": 55.9445}
        }
    """;

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testIsCloseTo_InvalidRequest_NullPosition() throws Exception {
        LngLatPairRequestForTest request = new LngLatPairRequestForTest(
                null,
                new PositionForTest(-3.186900, 55.944400)
        );

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIsCloseTo_InvalidRequest_OutOfRangePosition() throws Exception {
        LngLatPairRequestForTest request = new LngLatPairRequestForTest(
                new PositionForTest(-181.0, 55.944494),
                new PositionForTest(-3.186900, 91.0)
        );

        mockMvc.perform(post("/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

