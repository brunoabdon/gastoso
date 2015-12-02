//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package br.nom.abdon.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;


public class CrossOriginFilter implements Filter {

    private static final Logger log = Logger.getLogger(CrossOriginFilter.class.getName());
    
    // Request headers
    private static final String ORIGIN_HEADER = "Origin";
    private static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
    private static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
    // Response headers
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    
    // ENV CONFIG
    private static final String ABD_HTTP_ALLOWED_ORIGINS_ENVVAR = "ABD_HTTP_ALLOWED_ORIGINS";
    
    // Implementation constants
    private static final String ANY_ORIGIN = "*";
    private static final List<String> SIMPLE_HTTP_METHODS = Arrays.asList("GET", "POST", "HEAD");

    private static final Set<String> allowedMethods = new HashSet<>(
        Arrays.asList("GET","POST","PUT","HEAD","OPTIONS","DELETE")
    );
            
    private static final Set<String> allowedHeaders = new HashSet<>(
        Arrays.asList(
            "Accept",
            "Accept-Encoding",
            "Accept-Language",
            "Connection",
            "Content-Type",
            "Host",
            "Origin",
            "X-Abd-auth_token",
            "X-Requested-With")
    );

    private static final String preflightMaxAge = "1800";

    private static final String commifiedAllowedMethods = 
        StringUtils.join(allowedMethods, ',');
    
    private static final String commifiedAllowedHeaders = 
        StringUtils.join(allowedHeaders, ',');
    
    private boolean anyOriginAllowed;
    private final List<String> allowedOrigins = new ArrayList<>();

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        final String allowedOriginsConfig = 
            System.getenv(ABD_HTTP_ALLOWED_ORIGINS_ENVVAR);
        
        if (allowedOriginsConfig == null) {
            throw new ServletException(
                "Env var " + ABD_HTTP_ALLOWED_ORIGINS_ENVVAR + " não configurada");
        }
        
        final String[] allowedOriginsArray = allowedOriginsConfig.split(",");

        log.log(
            Level.INFO,"{0} = {1}", new
            Object[]{ABD_HTTP_ALLOWED_ORIGINS_ENVVAR, allowedOriginsConfig});

        for (String allowedOrigin : allowedOriginsArray)
        {
            allowedOrigin = allowedOrigin.trim();
            if (allowedOrigin.length() > 0)
            {
                if (ANY_ORIGIN.equals(allowedOrigin))
                {
                    anyOriginAllowed = true;
                    this.allowedOrigins.clear();
                    break;
                }
                else
                {
                    this.allowedOrigins.add(allowedOrigin);
                }
            }
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        handle((HttpServletRequest)request, (HttpServletResponse)response, chain);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        String origin = request.getHeader(ORIGIN_HEADER);
        // Is it a cross origin request ?
        if (origin != null && isEnabled(request))
        {
            if (originMatches(origin))
            {
                if (isSimpleRequest(request))
                {
                    log.log(Level.FINEST,"Cross-origin request to {} is a simple cross-origin request", request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                }
                else if (isPreflightRequest(request))
                {
                    log.log(Level.FINEST,"Cross-origin request to {} is a preflight cross-origin request", request.getRequestURI());
                    handlePreflightResponse(request, response, origin);
                }
                else
                {
                    log.log(Level.FINEST,"Cross-origin request to {} is a non-simple cross-origin request", request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                }
            }
            else
            {
                log.log(Level.FINEST,"Cross-origin request to " + request.getRequestURI() + " with origin " + origin + " does not match allowed origins " + allowedOrigins);
            }
        }

        chain.doFilter(request, response);
    }

    protected boolean isEnabled(HttpServletRequest request)
    {
        // WebSocket clients such as Chrome 5 implement a version of the WebSocket
        // protocol that does not accept extra response headers on the upgrade response
        for (Enumeration connections = request.getHeaders("Connection"); connections.hasMoreElements();)
        {
            String connection = (String)connections.nextElement();
            if ("Upgrade".equalsIgnoreCase(connection))
            {
                for (Enumeration upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements();)
                {
                    String upgrade = (String)upgrades.nextElement();
                    if ("WebSocket".equalsIgnoreCase(upgrade))
                        return false;
                }
            }
        }
        return true;
    }

    private boolean originMatches(String originList)
    {
        if (anyOriginAllowed)
            return true;

        if (originList.trim().length() == 0)
            return false;

        String[] origins = originList.split(" ");
        for (String origin : origins)
        {
            if (origin.trim().length() == 0)
                continue;

            for (String allowedOrigin : allowedOrigins)
            {
                if (allowedOrigin.contains("*"))
                {
                    Matcher matcher = createMatcher(origin,allowedOrigin);
                    if (matcher.matches())
                        return true;
                }
                else if (allowedOrigin.equals(origin))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Matcher createMatcher(String origin, String allowedOrigin)
    {
        String regex = parseAllowedWildcardOriginToRegex(allowedOrigin);
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(origin);
    }

    private String parseAllowedWildcardOriginToRegex(String allowedOrigin)
    {
        String regex = allowedOrigin.replace(".","\\.");
        return regex.replace("*",".*"); // we want to be greedy here to match multiple subdomains, thus we use .*
    }

    private boolean isSimpleRequest(HttpServletRequest request)
    {
        String method = request.getMethod();
        if (SIMPLE_HTTP_METHODS.contains(method))
        {
            // TODO: implement better detection of simple headers
            // The specification says that for a request to be simple, custom request headers must be simple.
            // Here for simplicity I just check if there is a Access-Control-Request-Method header,
            // which is required for preflight requests
            return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
        }
        return false;
    }

    private boolean isPreflightRequest(HttpServletRequest request)
    {
        return 
            "OPTIONS".equalsIgnoreCase(request.getMethod())
            && request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) != null;
    }

    private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin)
    {
        log.log(Level.FINEST,"handling simple respnse");
        
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    }

    private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin)
    {
        log.log(Level.FINEST,"handling preflight");
        
        boolean methodAllowed = isMethodAllowed(request);
        if (!methodAllowed)
            return;
        boolean headersAllowed = areHeadersAllowed(request);
        if (!headersAllowed)
            return;
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, preflightMaxAge);
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, commifiedAllowedMethods);
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commifiedAllowedHeaders);
    }

    private boolean isMethodAllowed(HttpServletRequest request)
    {
        final String accessControlRequestMethod = 
            request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
        log.log(Level.FINEST,
            "{0} is {1}", 
            new Object[]{
                ACCESS_CONTROL_REQUEST_METHOD_HEADER, 
                accessControlRequestMethod});
        final boolean result = 
                accessControlRequestMethod != null
                && allowedMethods.contains(accessControlRequestMethod);
        log.log(Level.FINEST,
                "Method {0} is {1} among allowed methods {2}", 
                new Object[]{
                    accessControlRequestMethod, 
                    (result ? "" : " not"),
                    allowedMethods});
        return result;
    }

    private boolean areHeadersAllowed(HttpServletRequest request)
    {
        final String accessControlRequestHeaders = 
            request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
        
        log.log(Level.FINEST,"{0} is {1}", 
            new Object[]{
                ACCESS_CONTROL_REQUEST_HEADERS_HEADER, 
                accessControlRequestHeaders});

        return 
            accessControlRequestHeaders != null 
            && 
            Arrays.asList(accessControlRequestHeaders.split(","))
            .parallelStream()
            .map(String::trim)
            .allMatch(
                providedHeader ->  
                    allowedHeaders
                    .parallelStream()
                    .anyMatch(providedHeader::equalsIgnoreCase));
    }

    @Override
    public void destroy()
    {
        anyOriginAllowed = false;
        allowedOrigins.clear();
        allowedMethods.clear();
        allowedHeaders.clear();
    }
}
