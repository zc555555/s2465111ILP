package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.pizzadrone.controller.RestControllerClass;
import uk.ac.ed.inf.pizzadrone.interfaces.OrderValidation;
import uk.ac.ed.inf.pizzadrone.data.DataService;
import uk.ac.ed.inf.pizzadrone.data.DeliveryPathCalculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestControllerClass.class)
public class UuidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderValidation orderValidation;

    @MockBean
    private DataService dataService;

    @MockBean
    private DeliveryPathCalculator deliveryPathCalculator;

    @Test
    void testGetUuid() throws Exception {
        mockMvc.perform(get("/uuid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2465111"));
    }
}

