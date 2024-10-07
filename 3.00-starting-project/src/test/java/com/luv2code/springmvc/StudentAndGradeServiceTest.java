package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeRepository;
import com.luv2code.springmvc.repository.MathGradeRepository;
import com.luv2code.springmvc.repository.ScienceGradeRepository;
import com.luv2code.springmvc.repository.StudentRepository;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MathGradeRepository mathGradeRepository;

    @Autowired
    private ScienceGradeRepository scienceGradeRepository;

    @Autowired
    private HistoryGradeRepository historyGradeRepository;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    // try with my credentials
    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @Test
    public void createStudentService() {
        studentService.createStudent("Stanislav", "Angelov", "...@unwe.bg");

        CollegeStudent student = studentRepository.findByEmailAddress("...@unwe.bg");

        assertEquals("...@unwe.bg", student.getEmailAddress(), "find my email");
    }

    @Test
    public void checkIfStudentIsNull() {

        // reuturn true if ID 1 exist in DB
        assertTrue(studentService.checkIfStudentIsNull(1));

        // return 0 if ID do not exist
        assertFalse(studentService.checkIfStudentIsNull(0));
    }

    // delete not even students bur grades to them as well
    @Test
    public void deleteStudentService() {
        Optional<CollegeStudent> studentToDelete = studentRepository.findById(1);
        Optional<MathGrade> deletedMathGrade = mathGradeRepository.findById(1);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradeRepository.findById(1);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradeRepository.findById(1);

        assertTrue(studentToDelete.isPresent(), "Return True");
        assertTrue(deletedMathGrade.isPresent());
        assertTrue(deletedScienceGrade.isPresent());
        assertTrue(deletedHistoryGrade.isPresent());

        studentService.deleteStudent(1);

        deletedMathGrade = mathGradeRepository.findById(1);
        deletedScienceGrade = scienceGradeRepository.findById(1);
        deletedHistoryGrade = historyGradeRepository.findById(1);

        studentToDelete = studentRepository.findById(1);
        assertFalse(studentToDelete.isPresent(), "Return False");
        assertFalse(deletedMathGrade.isPresent());
        assertFalse(deletedScienceGrade.isPresent());
        assertFalse(deletedHistoryGrade.isPresent());
    }

    @Test
    @Sql("/insertData.sql")
    public void createGradebookService() {
        Iterable<CollegeStudent> students = studentService.getGradeBook();

        List<CollegeStudent> collegeStudentList = new ArrayList<>();
        for (CollegeStudent collegeStudent : students) {
            collegeStudentList.add(collegeStudent);
        }
        // 1 // 5
        assertEquals(5, collegeStudentList.size());
    }

    @Test
    public void createGradeService() {
        // Create the Grade
        assertTrue(studentService.createGrade(80.50, 1, "math"));
        assertTrue(studentService.createGrade(80.50, 1, "science"));
        assertTrue(studentService.createGrade(80.50, 1, "history"));

        // Get all grades with student ID
        Iterable<MathGrade> mathGrades = mathGradeRepository.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeRepository.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeRepository.findGradeByStudentId(1);
        // Verify there are grades
        assertTrue(mathGrades.iterator().hasNext(), "Student has math grades");
        assertTrue(scienceGrades.iterator().hasNext(), "Student has science grades");
        assertTrue(historyGrades.iterator().hasNext(), "Student has history grades");
    }

    // if this test pass // then we cover all the cases in above method and its impl
    @Test
    public void createGradeServiceReturnError() {
        assertFalse(studentService.createGrade(105, 1, "math"));
        assertFalse(studentService.createGrade(-5, 1, "math"));
        assertFalse(studentService.createGrade(80.50, 2, "math"));
        assertFalse(studentService.createGrade(80.50, 1, "Informatics"));
    }

    @Test
    public void deleteGradeService() {
        assertEquals(1, studentService.deleteGrade(1, "math"),
                "Returns student id after delete");

        assertEquals(1, studentService.deleteGrade(1, "science"),
                "Returns student id after delete");

        assertEquals(1, studentService.deleteGrade(1, "history"),
                "Returns student id after delete");

        // check the case where expect is 0
        assertEquals(0, studentService.deleteGrade(1, "Computer science"),
                "No student should have 0 as id: return false");
    }

    // test above method edge case if we cover it
    @Test
    public void deleteGradeServiceReturnsStudentIdOfZero() {
        assertEquals(0, studentService.deleteGrade(0, "science"),
                "No student should have 0 as ID");

        assertEquals(0, studentService.deleteGrade(1, "informatics"),
                "No student should have a Informatics class");
    }

    // get student information from DB
    @Test
    public void studentInformation() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);

        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Chad", gradebookCollegeStudent.getFirstname());
        assertEquals("Darby", gradebookCollegeStudent.getLastname());
        assertEquals("chad.darby@luv2code_school.com", gradebookCollegeStudent.getEmailAddress());
        assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1);
    }

    @SqlGroup({ @Sql(scripts = "/insertData.sql", config = @SqlConfig(commentPrefix = "`")),
            @Sql("/overrideData.sql"),
            @Sql("/insertGrade.sql")})
    @Test
    public void getGradebookService() {
        Gradebook gradebook = studentService.getListOfGrades();
        Gradebook test = new Gradebook();

        for (GradebookCollegeStudent student : gradebook.getStudents()) {
            if (student.getId() > 10) {
                test.getStudents().add(student);
            }
        }
        assertEquals(4, test.getStudents().size());
        assertTrue(test.getStudents().get(0).getStudentGrades().getHistoryGradeResults() != null);
        assertTrue(test.getStudents().get(0).getStudentGrades().getScienceGradeResults() != null);
        assertTrue(test.getStudents().get(0).getStudentGrades().getMathGradeResults() != null);

    }

    @Test
    public void studentInformationServiceReturnNull() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
        assertNull(gradebookCollegeStudent);
    }


    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }
}
