package org.oasis_open.wemi.context.server.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by loom on 24.04.14.
 */
public class Event extends Item {

    public static final String EVENT_ITEM_TYPE = "event";
    private String eventType;
    private String visitorID = null;
    private Date timeStamp;

    private transient User user;

    // attributes are not serializable, and can be used to pass additional contextual object such as HTTP request
    // response objects, etc...
    private transient Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    public Event() {
        this.type = EVENT_ITEM_TYPE;
        this.timeStamp = new Date();
    }

    public Event(String itemId, String eventType, String visitorID, Date timeStamp) {
        super(itemId, EVENT_ITEM_TYPE, null, new Properties());
        this.eventType = eventType;
        setProperty("eventType", eventType);
        this.visitorID = visitorID;
        if (visitorID != null) {
            setProperty("visitorID", visitorID);
        }
        if (timeStamp != null) {
            this.timeStamp = timeStamp;
        } else {
            this.timeStamp = new Date();
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        setProperty("eventTimeStamp", format.format(this.timeStamp));
    }

    public void setVisitorID(String visitorID) {
        this.visitorID = visitorID;
        if (visitorID != null) {
            setProperty("visitorID", visitorID);
        }
    }

    public String getVisitorID() {
        return visitorID;
    }

    public String getEventType() {
        return eventType;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
