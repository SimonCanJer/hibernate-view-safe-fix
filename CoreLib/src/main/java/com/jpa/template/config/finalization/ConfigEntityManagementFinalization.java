package com.jpa.template.config.finalization;

import com.jpa.template.config.common.ITransactionInterceptionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import java.io.IOException;

/**
 * This class installs a simple  filters, which
 * does the one simple work:
 * it finalizes all operatios which have been posponed during workflow.
 * The work of filer done after callind doFilter in chain- that means upon all UI (View) operations are done
 * @see #interceptionManagement
 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
 * @see ITransactionInterceptionManagement
 *
 * */
@Configuration
public class ConfigEntityManagementFinalization {
    @Autowired
    ITransactionInterceptionManagement interceptionManagement;
    @Bean
    public FilterRegistrationBean<Filter> loggingFilter()
    {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new Filter(){
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                filterChain.doFilter(servletRequest,servletResponse);
                interceptionManagement.finalizeInterceptedPostponedOperations();
            }
        });
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
