package com.restapi.api;

import com.restapi.api.common.BaseControllerTest;
import com.restapi.api.common.RestDocsConfiguration;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

class RestApiApplicationTests extends BaseControllerTest {

    @Test
    void contextLoads() {
    }

}
