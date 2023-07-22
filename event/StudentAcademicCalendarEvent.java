package com.iskool.event;

import com.iskool.dto.StudentWorkingMonthDTO;
import com.iskool.entity.SendEmailDetails;

public class StudentAcademicCalendarEvent extends SendEmailDetails {

	private static final long serialVersionUID = 1L;

	private StudentWorkingMonthDTO studentWorkingMonthDTO;

	public StudentAcademicCalendarEvent(Object source, StudentWorkingMonthDTO request) {
		super(source);
		this.studentWorkingMonthDTO = request;
	}

	public StudentWorkingMonthDTO getStudentWorkingMonthDTO() {
		return studentWorkingMonthDTO;
	}

	public void setStudentWorkingMonthDTO(StudentWorkingMonthDTO studentWorkingMonthDTO) {
		this.studentWorkingMonthDTO = studentWorkingMonthDTO;
	}
	
	
}
