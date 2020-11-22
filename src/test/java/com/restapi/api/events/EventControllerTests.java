package com.restapi.api.events;

import com.restapi.api.account.Account;
import com.restapi.api.account.AccountRepository;
import com.restapi.api.account.AccountRole;
import com.restapi.api.account.AccountService;
import com.restapi.api.common.AppProperties;
import com.restapi.api.common.BaseControllerTest;
import com.restapi.api.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTests extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

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
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                                    //spring oauth token값 인증을 위해 relaxedResponseFields 사용 .
                                    //만약 oauth token값 인증이 필요 없는 테스트라면 ResponseFields 사용.
                                    relaxedResponseFields(
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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

        mockMvc.perform(post("/api/events")
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists());

    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        //given
        IntStream.range(0, 30).forEach(this::generateEvent);

        //when & Then
        mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"));

    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEventsWithAuthentication() throws Exception {
        //given
        IntStream.range(0, 30).forEach(this::generateEvent);

        //when & Then
        mockMvc.perform(get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query-events"));

    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception{
        //Given
        Account account = this.createAccount();
        Event event = this.generateEvent(100, account);

        //When & Then
        mockMvc.perform(get("/api/events/{id}", event.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("name").exists())
                        .andExpect(jsonPath("id").exists())
                        .andExpect(jsonPath("_links.self").exists())
                        .andExpect(jsonPath("_links.profile").exists())
                        .andDo(document("get-an-event"))
                        .andDo(print());
    }

    @Test
    @TestDescription("없는 이벤트 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception{
        //When & Then
        mockMvc.perform(get("/api/events/1183"))
                        .andExpect(status().isNotFound())
                        .andDo(print());
    }

    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception{
        //Given
        Account account = this.createAccount();

        Event event = this.generateEvent(1000, account);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        String eventName = "updated event";
        eventDto.setName(eventName);

        //When & Then
        mockMvc.perform(put("/api/events/{id}", event.getId())
                                    .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(eventDto))
                            )
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("name").value(eventName))
                            .andExpect(jsonPath("_links.self").exists())
                            .andDo(document("update-event"));
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception{
        //Given
        Event event = this.generateEvent(400);
        EventDto eventDto = new EventDto();

        //When & Then
        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception{
        //Given
        Event event = this.generateEvent(500);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        //When & Then
        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception{
        //Given
        Event event = this.generateEvent(999);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        //When & Then
        mockMvc.perform(put("/api/events/12312312")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index) {
        Event event = buildEvent(index);
        return this.eventRepository.save(event);
    }

    private Event generateEvent(int index, Account account) {
        Event event = buildEvent(index);
        event.setManager(account);
        return this.eventRepository.save(event);
    }

    public Event buildEvent(int index) {
        Event event = Event.builder()
                .name("event " + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return event;
    }

    public String getAccessToken(boolean needToCreateAccount) throws Exception {
        // Given
        if (needToCreateAccount) {
            createAccount();
        }

        ResultActions perform = mockMvc.perform(post("/oauth/token")
                                            .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                                            .param("username", appProperties.getUserUsername())
                                            .param("password", appProperties.getUserPassword())
                                            .param("grant_type", "password"));

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        JacksonJsonParser parser = new JacksonJsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    public String getBearerToken() throws Exception {
        return getBearerToken(true);
    }

    public String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "bearer " + getAccessToken(needToCreateAccount);
    }

    public Account createAccount() {
        Account account = Account.builder()
                                            .email(appProperties.getUserUsername())
                                            .password(appProperties.getUserPassword())
                                            .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                                            .build();

        return this.accountService.saveAccount(account);
    }

}
