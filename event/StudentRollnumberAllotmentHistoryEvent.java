package com.iskool.event;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;

import com.iskool.dto.StudentRollnumberAllotmentHistoryDto;

public class StudentRollnumberAllotmentHistoryEvent extends ApplicationEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private StudentRollnumberAllotmentHistoryDto dto;

	public StudentRollnumberAllotmentHistoryEvent(Object source, StudentRollnumberAllotmentHistoryDto dto) {
		super(source);
		this.dto = dto;
	}

	public StudentRollnumberAllotmentHistoryDto getDto() {
		return dto;
	}

	public void setDto(StudentRollnumberAllotmentHistoryDto dto) {
		this.dto = dto;
	}

	
}
