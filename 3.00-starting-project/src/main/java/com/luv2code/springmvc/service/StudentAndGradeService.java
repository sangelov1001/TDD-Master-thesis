package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeRepository;
import com.luv2code.springmvc.repository.MathGradeRepository;
import com.luv2code.springmvc.repository.ScienceGradeRepository;
import com.luv2code.springmvc.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentAndGradeService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MathGradeRepository mathGradeRepository;

    @Autowired
    private ScienceGradeRepository scienceGradeRepository;

    @Autowired
    private HistoryGradeRepository historyGradeRepository;

    @Autowired
    private StudentGrades studentGrades;

    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;

    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;

    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;

    public void createStudent(String firstName, String lastName, String email) {
        CollegeStudent student = new CollegeStudent(firstName,lastName,email);
            student.setId(0);
            studentRepository.save(student);
    }

    public boolean checkIfStudentIsNull(int id) {
        Optional<CollegeStudent> student = studentRepository.findById(id);

        if (student.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public Iterable<CollegeStudent> getGradeBook() {
        Iterable<CollegeStudent> students = studentRepository.findAll();
        return students;
    }

    // return true if its successfully or not
    public boolean createGrade(double grade, int studentId, String gradeType) {

        if (!checkIfStudentIsNull(studentId)) {
            return false;
        }
        if (grade >= 0 && grade <= 100) {
            if (gradeType.equals("math")) {
                mathGrade.setId(0);
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);
                mathGradeRepository.save(mathGrade);
                return true;
            } else if (gradeType.equals("science")) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);
                scienceGradeRepository.save(scienceGrade);
                return true;
            } else if (gradeType.equals("history")) {
                historyGrade.setId(0);
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);
                historyGradeRepository.save(historyGrade);
                return true;
            }
        }
        return false;
    }

    public Gradebook getListOfGrades() {
        Iterable<CollegeStudent> collegeStudents = studentRepository.findAll();

        Iterable<MathGrade> mathGrades = mathGradeRepository.findAll();

        Iterable<ScienceGrade> scienceGrades = scienceGradeRepository.findAll();

        Iterable<HistoryGrade> historyGrades = historyGradeRepository.findAll();

        Gradebook gradebook = new Gradebook();

        for (CollegeStudent collegeStudent : collegeStudents) {
            List<Grade> mathGradesPerStudent = new ArrayList<>();
            List<Grade> scienceGradesPerStudent = new ArrayList<>();
            List<Grade> historyGradesPerStudent = new ArrayList<>();

            for (MathGrade grade : mathGrades) {
                if (grade.getStudentId() == collegeStudent.getId()) {
                    mathGradesPerStudent.add(grade);
                }
            }
            for (ScienceGrade grade : scienceGrades) {
                if (grade.getStudentId() == collegeStudent.getId()) {
                    scienceGradesPerStudent.add(grade);
                }
            }

            for (HistoryGrade grade : historyGrades) {
                if (grade.getStudentId() == collegeStudent.getId()) {
                    historyGradesPerStudent.add(grade);
                }
            }

            studentGrades.setMathGradeResults(mathGradesPerStudent);
            studentGrades.setScienceGradeResults(scienceGradesPerStudent);
            studentGrades.setHistoryGradeResults(historyGradesPerStudent);

            GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(collegeStudent.getId(), collegeStudent.getFirstname(), collegeStudent.getLastname(),
                    collegeStudent.getEmailAddress(), studentGrades);

            gradebook.getStudents().add(gradebookCollegeStudent);
        }

        return gradebook;
    }

    public int deleteGrade(int id, String gradeType) {
        int studentId = 0;
        if (gradeType.equals("math")) {
            Optional<MathGrade> mathGrade = mathGradeRepository.findById(id);
            if (!mathGrade.isPresent()) {
                return studentId;
            }
            studentId = mathGrade.get().getStudentId();
            mathGradeRepository.deleteById(id);
        } else if (gradeType.equals("science")) {
            Optional<ScienceGrade> scienceGrade = scienceGradeRepository.findById(id);
            if (!scienceGrade.isPresent()) {
                return studentId;
            }
            studentId = scienceGrade.get().getStudentId();
            scienceGradeRepository.deleteById(id);
        } else if (gradeType.equals("history")) {
            Optional<HistoryGrade> historyGrade = historyGradeRepository.findById(id);
            if (!historyGrade.isPresent()) {
                return studentId;
            }
            studentId = historyGrade.get().getStudentId();
            historyGradeRepository.deleteById(id);
        }
        return studentId;
    }

    public GradebookCollegeStudent studentInformation(int id) {

        if (!checkIfStudentIsNull(id)) {
            return null;
        }
        // retrieve student from DB
        Optional<CollegeStudent> student = studentRepository.findById(id);
        Iterable<MathGrade> mathGrade = mathGradeRepository.findGradeByStudentId(id);
        Iterable<ScienceGrade> scienceGrade = scienceGradeRepository.findGradeByStudentId(id);
        Iterable<HistoryGrade> historyGrade = historyGradeRepository.findGradeByStudentId(id);

        // convert Iterable to a List
        List<Grade> mathGradeList = new ArrayList<>();
        mathGrade.forEach(mathGradeList::add);

        List<Grade> scenceGradeList = new ArrayList<>();
        scienceGrade.forEach(scenceGradeList::add);

        List<Grade> historyGradeList = new ArrayList<>();
        historyGrade.forEach(historyGradeList::add);

        studentGrades.setMathGradeResults(mathGradeList);
        studentGrades.setScienceGradeResults(scenceGradeList);
        studentGrades.setHistoryGradeResults(historyGradeList);

        GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(student.get().getId(),
                student.get().getFirstname(), student.get().getLastname(), student.get().getEmailAddress(),
                studentGrades);

        return gradebookCollegeStudent;
    }

    public void configureStudentInformationModule(int id, Model model) {

        GradebookCollegeStudent studentEntity = studentInformation(id);
        model.addAttribute("student", studentEntity);

        if (!studentEntity.getStudentGrades().getMathGradeResults().isEmpty()) {
            model.addAttribute("mathAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getMathGradeResults()
            ));
        } else {
            model.addAttribute("mathAverage", "N/A");
        }

        if (!studentEntity.getStudentGrades().getScienceGradeResults().isEmpty()) {
            model.addAttribute("scienceAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getScienceGradeResults()
            ));
        } else {
            model.addAttribute("scienceAverage", "N/A");
        }

        if (!studentEntity.getStudentGrades().getHistoryGradeResults().isEmpty()) {
            model.addAttribute("historyAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getHistoryGradeResults()
            ));
        } else {
            model.addAttribute("historyAverage", "N/A");
        }
    }

    // delete student and all the grades to it
    public void deleteStudent(int id) {
        if (checkIfStudentIsNull(id)) {
            studentRepository.deleteById(id);
            mathGradeRepository.deleteByStudentId(id);
            scienceGradeRepository.deleteByStudentId(id);
            historyGradeRepository.deleteByStudentId(id);
        }
    }
}
