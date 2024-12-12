package com.example.demo.bookingService;

import com.example.demo.config.BookingProperties;
import com.example.demo.config.ChuckProperties;
import com.example.demo.integration.BookingClient;
import com.example.demo.model.BookingResponse;
import com.example.demo.model.ChuckResponse;
import com.example.demo.model.Gender;
import com.example.demo.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate testRestTemplate;


    @MockBean
    private RestTemplate restTemplate;

    private static final int id = 1;
    private static final int bookingDateTo = 7;
    private static final Student newStudent = new Student("test", "test@test.ru", Gender.MALE);
    private static final BookingResponse booking = new BookingResponse(
            id,
            new BookingResponse.Booking(
                    newStudent.getName(),
                    "some-last-name",
                    new Random().nextInt(),
                    true,
                    new BookingResponse.BookingDates(
                            LocalDate.now(),
                            LocalDate.now().plusDays(bookingDateTo)
                    ),
                    "No needs"
            )
    );

    @Autowired
    BookingClient bookingClient;

    @Autowired
    BookingProperties bookingProperties;

    @Autowired
    ChuckProperties chuckProperties;

    @Test
    public void checkBookingId() throws JsonProcessingException {
        when(restTemplate.postForObject(eq(bookingProperties.getUrl() + "/booking"), any(), eq(BookingResponse.class)))
                .thenReturn(booking);

        when(restTemplate.exchange(
                eq(chuckProperties.getUrl()),
                eq(HttpMethod.GET),
                isNull(),
                Mockito.<ParameterizedTypeReference<ChuckResponse>>any()))
                .thenReturn(ResponseEntity.ok(new ChuckResponse("test joke")));

        testRestTemplate.postForEntity("/api/v1/students", newStudent, void.class);
        int bookingId = testRestTemplate.getForObject("/api/v1/students/1", Student.class).getBookingId();

        assertEquals(id, bookingId);
    }

    @Test
    public void checkBookingIdIfServerError() {
        int expected = -1;

        BookingResponse bookingRequest = new BookingResponse();

        when(restTemplate.postForObject(eq(bookingProperties.getUrl() + "/booking"), any(), eq(BookingResponse.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        int actual = bookingClient.createBooking(bookingRequest.toString());

        assertEquals(expected, actual);

    }
}
