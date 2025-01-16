package uk.ac.ed.inf.pizzadrone.controllerUnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.ac.ed.inf.pizzadrone.controller.RestControllerClass;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class IsInRegionControllerTest {

    @Autowired
    private RestControllerClass restController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(restController).build();
    }

    @Test
    public void testIsInRegion_ValidRequest_InsideRegion() throws Exception {
        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "position": {"lng": 55.944425, "lat": -3.188396},
                                    "region": {
                                        "name": "TestRegion",
                                        "vertices": [
                                            {"lng": 55.944, "lat": -3.189, "valid": true},
                                            {"lng": 55.945, "lat": -3.189, "valid": true},
                                            {"lng": 55.945, "lat": -3.188, "valid": true},
                                            {"lng": 55.944, "lat": -3.188, "valid": true}
                                        ],
                                        "regionValid": true
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsInRegion_ValidRequest_OutsideRegion() throws Exception {
        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "position": {"lng": 55.946, "lat": -3.19},
                                    "region": {
                                        "name": "TestRegion",
                                        "vertices": [
                                            {"lng": 55.944, "lat": -3.189, "valid": true},
                                            {"lng": 55.945, "lat": -3.189, "valid": true},
                                            {"lng": 55.945, "lat": -3.188, "valid": true},
                                            {"lng": 55.944, "lat": -3.188, "valid": true}
                                        ],
                                        "regionValid": true
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_InvalidRequest() throws Exception {
        mockMvc.perform(post("/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "position": {"lng": 55.946, "lat": -3.19},
                                    "region": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}


