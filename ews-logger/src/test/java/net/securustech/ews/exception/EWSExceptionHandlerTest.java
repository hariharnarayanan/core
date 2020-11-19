package net.securustech.ews.exception;

import net.securustech.ews.exception.entities.*;
import net.securustech.ews.exception.handler.EWSExceptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        EWSExceptionHandler.class,
        EWSExceptionHandlerTest.TestConfiguration.class,
        EWSExceptionHandlerTest.EWSExceptionThrowingController.class})
public class EWSExceptionHandlerTest {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Configuration
    @EnableWebMvc
    public static class TestConfiguration {
    }

    @Controller
    @RequestMapping("/tests")
    public static class EWSExceptionThrowingController {

        @RequestMapping(value = "/handleBadRequestException", method = RequestMethod.GET)
        public
        @ResponseBody
        String handleBadRequestException() throws EWSBadRequestException {
            throw new EWSBadRequestException();
        }

        @RequestMapping(value = "/handleRuntimeException", method = RequestMethod.GET)
        public
        @ResponseBody
        String handleRuntimeException() throws Exception {
            throw new IllegalArgumentException("500");
        }

        @RequestMapping(value = "/handleEWSSQLException", method = RequestMethod.GET)
        public
        @ResponseBody
        String handleEWSSQLException() throws Exception {
            throw new EWSSQLException();
        }

        @RequestMapping(value = "/handleSQLException", method = RequestMethod.GET)
        public
        @ResponseBody
        String handleSQLException() throws Exception {
            throw new SQLException();
        }

        @RequestMapping(value = "/handleException", method = RequestMethod.GET)
        public
        @ResponseBody
        String handleException() throws Exception {
            throw new Exception();
        }
    }

    @Test
    public void testHandleBadRequestException() throws Exception {
        mockMvc.perform(get("/tests/handleBadRequestException"))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult result) throws Exception {
                        result.getResponse().getContentAsString().contains("400");
                    }
                })
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testHandleNotFoundException() throws Exception {
        mockMvc.perform(get("/tests/handleNotFoundException"))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult result) throws Exception {
                        result.getResponse().getContentAsString().contains("400");
                    }
                })
                .andExpect(status().isNotFound());
    }

    @Test
    public void testHandleEWSSQLException() throws Exception {
        mockMvc.perform(get("/tests/handleEWSSQLException"))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult result) throws Exception {
                        result.getResponse().getContentAsString().contains("500");
                    }
                })
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testHandleSQLException() throws Exception {
        mockMvc.perform(get("/tests/handleSQLException"))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult result) throws Exception {
                        result.getResponse().getContentAsString().contains("500");
                    }
                })
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testHandleException() throws Exception {
        mockMvc.perform(get("/tests/handleException"))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult result) throws Exception {
                        result.getResponse().getContentAsString().contains("500");
                    }
                })
                .andExpect(status().isInternalServerError());
    }
}