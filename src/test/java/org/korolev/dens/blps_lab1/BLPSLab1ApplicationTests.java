package org.korolev.dens.blps_lab1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class BLPSLab1ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllChapters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/chapter/get/all"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
