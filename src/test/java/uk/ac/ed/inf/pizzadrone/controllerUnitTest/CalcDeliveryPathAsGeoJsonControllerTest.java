package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CalcDeliveryPathAsGeoJsonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ORDERS_URL = "https://ilp-rest-2024.azurewebsites.net/orders";

    @Test
    public void testCalcDeliveryPathAsGeoJson() throws Exception {
        List<Map<String, Object>> orders = fetchOrdersFromUrl();

        for (Map<String, Object> order : orders) {
            Boolean valid = (Boolean) order.get("valid");
            if (valid != null && valid) {
                String requestBody = new ObjectMapper().writeValueAsString(order);

                String responseContent = mockMvc.perform(post("/calcDeliveryPathAsGeoJson")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                assertThat(responseContent).isNotNull();
                assertThat(responseContent).contains("\"type\":\"FeatureCollection\"");
            }
        }
    }

    private List<Map<String, Object>> fetchOrdersFromUrl() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new URL(ORDERS_URL),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
    }
}


