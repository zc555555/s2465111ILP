package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.ac.ed.inf.pizzadrone.controller.RestControllerClass;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CalcDeliveryPathControllerTest {

    @Autowired
    private RestControllerClass restController;

    private MockMvc mockMvc;
    private static final String ORDERS_URL = "https://ilp-rest-2024.azurewebsites.net/orders";
    private static final Logger logger = Logger.getLogger(CalcDeliveryPathControllerTest.class.getName());

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(restController).build();
    }

    private List<Map<String, Object>> fetchOrders() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new URL(ORDERS_URL), new TypeReference<>() {});
    }

    @Test
    public void testCalcDeliveryPaths() throws Exception {
        List<Map<String, Object>> orders = fetchOrders();

        for (Map<String, Object> order : orders) {

            if (!order.containsKey("valid") || order.get("valid") == null) {
                logger.warning("Skipping order due to missing 'valid' field: " + order);
                continue;
            }

            Boolean isValid = Boolean.valueOf(order.get("valid").toString());


            if (!isValid) {
                logger.info("Skipping invalid order: " + order);
                continue;
            }

            mockMvc.perform(post("/calcDeliveryPath")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(order)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.path").exists())
                    .andExpect(jsonPath("$.path.length()").isNumber());
        }
    }
}
