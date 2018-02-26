package com.wenyankeji.entity;

import java.io.Serializable;

public class NetRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private Label label;
	private User user;
	private ReceiveMessage receiveMessage;

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	public User getUser() {
		return user;
	}

	public ReceiveMessage getReceiveMessage() {
		return receiveMessage;
	}

	public void setReceiveMessage(ReceiveMessage receiveMessage) {
		this.receiveMessage = receiveMessage;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
