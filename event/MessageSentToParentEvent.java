package com.iskool.event;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;

import com.iskool.entity.AcademicStandardFeeComponent;
import com.iskool.entity.Student;
import com.iskool.entity.StudentParentDetails;

public class MessageSentToParentEvent extends ApplicationEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Student student;
	private StudentParentDetails parentDetails;
	private AcademicStandardFeeComponent component;

	public MessageSentToParentEvent(Object source, Student student, StudentParentDetails parentDetails,
			AcademicStandardFeeComponent component) {
		super(source);
		this.student = student;
		this.parentDetails = parentDetails;
		this.component = component;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public StudentParentDetails getParentDetails() {
		return parentDetails;
	}

	public void setParentDetails(StudentParentDetails parentDetails) {
		this.parentDetails = parentDetails;
	}

	public AcademicStandardFeeComponent getComponent() {
		return component;
	}

	public void setComponent(AcademicStandardFeeComponent component) {
		this.component = component;
	}

	@Override
	public String toString() {
		return "MessageSentToParentEvent [student=" + student + ", parentDetails=" + parentDetails + ", component="
				+ component + "]";
	}

}
