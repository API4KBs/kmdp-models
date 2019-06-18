/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
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
package edu.mayo.kmdp.util.ws;

import com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

public class HeaderForwardServerInterceptor extends HandlerInterceptorAdapter {

    private Set<String> headersToForward;

    public HeaderForwardServerInterceptor(Set<String> headersToForward) {
        this.headersToForward = Sets.newHashSet(headersToForward);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String,String> values = Maps.newHashMap();

        this.headersToForward.stream().forEach(header -> values.put(header, request.getHeader(header)));
        WebSessionContext.setHeaders(values);

        return true;
    }

}
