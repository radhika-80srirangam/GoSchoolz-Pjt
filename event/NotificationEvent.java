package com.iskool.event;

import com.iskool.entity.NotificationHdr;
import com.iskool.entity.SendEmailDetails;

public class NotificationEvent extends SendEmailDetails {

	private static final long serialVersionUID = 1L;

	private NotificationHdr notification;

	public NotificationEvent(Object source, NotificationHdr notification) {
		super(source);
		this.notification = notification;
	}

	public NotificationHdr getNotification() {
		return notification;
	}

	public void setNotification(NotificationHdr notification) {
		this.notification = notification;
	}

 
	
}
