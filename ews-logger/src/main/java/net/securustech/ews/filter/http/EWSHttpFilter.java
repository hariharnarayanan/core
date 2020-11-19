package net.securustech.ews.filter.http;

import net.securustech.embs.util.CorrelationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
public class EWSHttpFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EWSHttpFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String correlationId = httpServletRequest.getHeader(CorrelationId.UUID);

        if ((httpServletRequest.getRequestURI().startsWith("/ews/")
                || httpServletRequest.getRequestURI().startsWith("/esp/"))
                && !currentRequestIsAsyncDispatcher(httpServletRequest)) {

            if (correlationId == null) {

                correlationId = UUID.randomUUID().toString();

                LOGGER.info("EWSUUIDFilter : NO UUID/CorrelationId in Header : Generated :::>>> " + correlationId);
            } else {

                LOGGER.info("EWSUUIDFilter : UUID/CorrelationId FOUND in Header :::>>> " + correlationId);
            }
            CorrelationId.setId(correlationId);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean currentRequestIsAsyncDispatcher(HttpServletRequest httpServletRequest) {

        return httpServletRequest.getDispatcherType().equals(DispatcherType.ASYNC);
    }
}