package net.securustech.ews.logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EWSLoggerInterceptor.class})
public class EWSLoggerInterceptorTest {

    @Autowired
    private EWSLoggerInterceptor ewsLoggerInterceptor;

    private MockHttpServletRequest httpServletRequest;

    @Before
    public void setUp() throws Exception {

        httpServletRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));
    }

    @Test
    public void shouldReturnApplicationNameAsWEBPORTAL() throws Exception {

        httpServletRequest.addHeader("applicationName", "WebPortal");
        assertEquals("WEBPORTAL", ewsLoggerInterceptor.getHeader("applicationName"));
    }

    @Test
    public void shouldReturnUsernameAsMOBILE() throws Exception {

        httpServletRequest.addHeader("username", "Mobile");
        assertEquals("MOBILE", ewsLoggerInterceptor.getHeader("username"));
    }

    @Test
    public void shouldReturnMigrationIndicatorAsYES() throws Exception {

        httpServletRequest.addHeader("migrationIndicator", "YeS");
        assertEquals("YES", ewsLoggerInterceptor.getHeader("migrationIndicator"));
    }
}