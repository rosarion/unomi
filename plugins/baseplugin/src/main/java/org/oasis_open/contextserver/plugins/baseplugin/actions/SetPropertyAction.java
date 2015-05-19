package org.oasis_open.contextserver.plugins.baseplugin.actions;

/*
 * #%L
 * context-server-services
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Jahia Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.oasis_open.contextserver.api.Event;
import org.oasis_open.contextserver.api.actions.Action;
import org.oasis_open.contextserver.api.actions.ActionExecutor;
import org.oasis_open.contextserver.api.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SetPropertyAction implements ActionExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SetPropertyAction.class.getName());
    private DefaultResolver resolver = new DefaultResolver();

    public SetPropertyAction() {
    }

    public String getActionId() {
        return "setPropertyAction";
    }

    public int execute(Action action, Event event) {
        Object propertyValue = action.getParameterValues().get("setPropertyValue");
        if (propertyValue != null && propertyValue.equals("now")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            propertyValue = format.format(event.getTimeStamp());
        }
        String propertyName = (String) action.getParameterValues().get("setPropertyName");

        Object target = Boolean.TRUE.equals(action.getParameterValues().get("storeInSession")) ? event.getSession() : event.getProfile();

        try {
            while (resolver.hasNested(propertyName)) {
                Object v = PropertyUtils.getProperty(target, resolver.next(propertyName));
                if (v == null) {
                    v = new LinkedHashMap<>();
                    PropertyUtils.setProperty(target, resolver.next(propertyName), v);
                }
                propertyName = resolver.remove(propertyName);
                target = v;
            }
            String setPropertyStrategy = (String) action.getParameterValues().get("setPropertyStrategy");

            if (setPropertyStrategy != null && setPropertyStrategy.equals("addValue")) {
                Object previousValue = PropertyUtils.getProperty(target, propertyName);
                List<Object> values = new ArrayList<>();
                if (previousValue != null && previousValue instanceof List) {
                    values.addAll((List) previousValue);
                } else if (previousValue != null) {
                    values.add(previousValue);
                }
                if (!values.contains(propertyValue)) {
                    values.add(propertyValue);
                    BeanUtils.setProperty(target, propertyName, values);
                    return Boolean.TRUE.equals(action.getParameterValues().get("storeInSession")) ? EventService.SESSION_UPDATED : EventService.PROFILE_UPDATED;
                }
            } else if (propertyValue != null && !propertyValue.equals(BeanUtils.getProperty(target, propertyName))) {
                if (setPropertyStrategy == null ||
                        setPropertyStrategy.equals("alwaysSet") ||
                        (setPropertyStrategy.equals("setIfMissing") && BeanUtils.getProperty(target, propertyName) == null)) {
                    BeanUtils.setProperty(target, propertyName, propertyValue);
                    return Boolean.TRUE.equals(action.getParameterValues().get("storeInSession")) ? EventService.SESSION_UPDATED : EventService.PROFILE_UPDATED;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Cannot set property", e);
        }
        return EventService.NO_CHANGE;
    }

}
