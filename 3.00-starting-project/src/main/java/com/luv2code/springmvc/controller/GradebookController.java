package com.luv2code.springmvc.controller;

import com.luv2code.springmvc.exceptionHandling.StudentOrGradeErrorResponse;
import com.luv2code.springmvc.exceptionHandling.StudentOrGradeNotFoundException;
import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GradebookController {

	@Autowired
	private Gradebook gradebook;

	@Autowired
	private StudentAndGradeService studentAndGradeService;


	@PostMapping(value = "/")
	public String createStudent(@ModelAttribute("student") CollegeStudent student, Model model) {
		studentAndGradeService.createStudent(student.getFirstname(), student.getLastname(),
				student.getEmailAddress());
		Iterable<CollegeStudent> students = studentAndGradeService.getGradeBook();
		model.addAttribute("students", students);
		return "index";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getStudents(Model m) {
		Iterable<CollegeStudent> collegeStudents = studentAndGradeService.getGradeBook();
		m.addAttribute("students", collegeStudents);
		return "index";
	}

	@GetMapping("/delete/student/{id}")
	public String deleteStudentById(@PathVariable int id, Model model) {

		if (!studentAndGradeService.checkIfStudentIsNull(id)) {
			throw new StudentOrGradeNotFoundException("Student was not found");
		}

		studentAndGradeService.deleteStudent(id);
		Iterable<CollegeStudent> collegeStudents = studentAndGradeService.getGradeBook();
		model.addAttribute("students", collegeStudents);
		return "index";
	}


	@GetMapping("/studentInformation/{id}")
	public String studentInformation(@PathVariable int id, Model m) {

		if (!studentAndGradeService.checkIfStudentIsNull(id)) {
			throw new StudentOrGradeNotFoundException("Student or Grade was not found");
		}

		studentAndGradeService.configureStudentInformationModule(id, m);

			return "studentInformation";
	}

	@PostMapping(value = "/grades")
	public String createGrade(@RequestParam("grade") double grade,
							  @RequestParam("gradeType") String gradeType,
							  @RequestParam("studentId") int studentId, Model model) {

		if (!studentAndGradeService.checkIfStudentIsNull(studentId)) {
			throw new StudentOrGradeNotFoundException("Student or Grade was not found");
		}

		boolean success = studentAndGradeService.createGrade(grade, studentId, gradeType);

		if (!success) {
			throw new StudentOrGradeNotFoundException("Student or Grade was not found");
		}

		studentAndGradeService.configureStudentInformationModule(studentId, model);

		return "studentInformation";
	}

	@GetMapping("/grades/{id}/{gradeType}")
	public String deleteGrade(@PathVariable int id, @PathVariable String gradeType, Model model) {

		int studentId = studentAndGradeService.deleteGrade(id, gradeType);

		if (studentId == 0) {
			throw new StudentOrGradeNotFoundException("Student or Grade was not found");
		}

		studentAndGradeService.configureStudentInformationModule(studentId, model);

		return "studentInformation";
	}

	@ExceptionHandler
	public ResponseEntity<StudentOrGradeErrorResponse> handleException(StudentOrGradeNotFoundException exc) {

		StudentOrGradeErrorResponse error = new StudentOrGradeErrorResponse();

		error.setStatus(HttpStatus.NOT_FOUND.value());
		error.setMessage(exc.getMessage());
		error.setTimeStamp(System.currentTimeMillis());

		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler
	public ResponseEntity<StudentOrGradeErrorResponse> handleException(Exception exc) {

		StudentOrGradeErrorResponse error = new StudentOrGradeErrorResponse();

		error.setStatus(HttpStatus.BAD_REQUEST.value());
		error.setMessage(exc.getMessage());
		error.setTimeStamp(System.currentTimeMillis());

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
}
