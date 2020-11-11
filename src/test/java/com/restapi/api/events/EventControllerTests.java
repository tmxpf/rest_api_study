package com.restapi.api.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restapi.api.common.RestDocsConfiguration;
import com.restapi.api.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                    .name("Spring")
                    .description("REST API Development with Spring")
                    .beginEnrollmentDateTime(LocalDateTime.of(2018,11, 23, 14, 21))
                    .closeEnrollmentDateTime(LocalDateTime.of(2018,11, 24, 14, 21))
                    .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                    .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                    .basePrice(100)
                    .maxPrice(200)
                    .limitOfEnrollment(100)
                    .location("강남역 D2 스타텁 팩토리")
                    .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                        .andDo(print())
                     .andExpect(status().isCreated())
                     .andExpect(jsonPath("id").exists())
                     .andExpect(header().exists(HttpHeaders.LOCATION))
                     .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                     .andExpect(jsonPath("id").value(Matchers.not(100)))
                     .andExpect(jsonPath("free").value(Matchers.not(true)))
                     .andExpect(jsonPath("offline").value(Matchers.not(false)))
                     .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                     .andExpect(jsonPath("_links.self").exists())
                     .andExpect(jsonPath("_links.query-events").exists())
                     .andExpect(jsonPath("_links.update-event").exists())
                     .andDo(document("create-event",
                                    links(
                                            linkWithRel("self").description("link to self"),
                                            linkWithRel("query-events").description("link to query events"),
                                            linkWithRel("update-event").description("link to update an existing event"),
                                            linkWithRel("profile").description("link to profile")
                                    ),
                                    requestHeaders(
                                            headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                            headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                                    ),
                                    requestFields(
                                            fieldWithPath("name").description("Name of new event"),
                                            fieldWithPath("description").description("description of new event"),
                                            fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                            fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                            fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                            fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                            fieldWithPath("location").description("location of new event"),
                                            fieldWithPath("basePrice").description("base price of new event"),
                                            fieldWithPath("maxPrice").description("max price of new event"),
                                            fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                                    ),
                                    responseHeaders(
                                            headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                            headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                                    ),
                                    responseFields(
                                            fieldWithPath("id").description("identifier of new event"),
                                            fieldWithPath("name").description("Name of new event"),
                                            fieldWithPath("description").description("description of new event"),
                                            fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                            fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                            fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                            fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                            fieldWithPath("location").description("location of new event"),
                                            fieldWithPath("basePrice").description("base price of new event"),
                                            fieldWithPath("maxPrice").description("max price of new event"),
                                            fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                            fieldWithPath("free").description("it tells if this event is free of not"),
                                            fieldWithPath("offline").description("it tells if this event is offline event of not"),
                                            fieldWithPath("eventStatus").description("event status"),
                                            fieldWithPath("_links.self.href").description("link to self"),
                                            fieldWithPath("_links.query-events.href").description("link to query event list"),
                                            fieldWithPath("_links.update-event.href").description("link to update existing event"),
                                            fieldWithPath("_links.profile.href").description("link to profile")
                                    )
                     ));
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.DRAFT)
                .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11, 26, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,11, 25, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists());

    }

}
