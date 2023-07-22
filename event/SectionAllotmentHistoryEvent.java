package com.iskool.event;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;

import com.iskool.dto.SectionAllotmentDTO;

public class SectionAllotmentHistoryEvent extends ApplicationEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private SectionAllotmentDTO dto;

	public SectionAllotmentHistoryEvent(Object source, SectionAllotmentDTO dto) {
		super(source);
		this.dto = dto;
	}

	public SectionAllotmentDTO getDto() {
		return dto;
	}

	public void setDto(SectionAllotmentDTO dto) {
		this.dto = dto;
	}

}
