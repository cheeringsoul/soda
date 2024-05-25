package com.soda;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Event {
    protected String eventID;
    protected String eventName;
}
