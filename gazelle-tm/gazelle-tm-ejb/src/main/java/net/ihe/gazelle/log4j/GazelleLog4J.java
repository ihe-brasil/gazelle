/*
 * Copyright 2016 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.log4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.web.AbstractFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * <b>Class Description : </b>GazelleLog4J<br>
 * <br>
 *
 * @author Jean-Francois Labbé / IHE-Europe development Project
 * @version 1.0 - 22/03/16
 * @class GazelleLog4J
 * @package net.ihe.gazelle.log4j
 * @see jean-francois.labbe@ihe-europe.net - http://gazelle.ihe.net
 */
@Scope(ScopeType.APPLICATION)
@Name("net.ihe.gazelle.log4j.GazelleLog4J")
@Install
@BypassInterceptors
@Filter
public class GazelleLog4J extends AbstractFilter {

    private static final Logger LOG = LoggerFactory.getLogger(GazelleLog4J.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
            ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) req;
        String queryString = "";
        if (servletRequest.getQueryString() != null) {
            queryString = "?" + servletRequest.getQueryString();
        }
        MDC.put("path", servletRequest.getServletPath() + queryString);
        HttpSession session = ((HttpServletRequest) servletRequest).getSession(false);

        if (session != null) {
            Object attribute = session.getAttribute("org.jboss.seam.security.identity");
            if (attribute instanceof Identity) {
                Identity identity = (Identity) attribute;
                Credentials credentials = identity.getCredentials();

                String username = credentials != null ? credentials.getUsername() : null;
                if (username != null) {
                    MDC.put("username", username);
                }
            }
        }
        filterChain.doFilter(req, resp);
    }
}