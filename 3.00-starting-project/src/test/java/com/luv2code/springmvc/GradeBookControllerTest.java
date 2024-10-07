package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.repository.MathGradeRepository;
import com.luv2code.springmvc.repository.StudentRepository;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class GradeBookControllerTest {

    private static MockHttpServletRequest request;

    @Autowired
    public JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

    @Autowired
    private MathGradeRepository mathGradeRepository;

    @Mock
    private StudentAndGradeService studentCreateServiceMock;

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

    @BeforeAll
    public static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Stanislav");
        request.setParameter("lastname", "Angelov");
        request.setParameter("emailAddress", "sangelov_2321350@unwe.bg");
    }

    // integration test to DB
    @BeforeEach
    public void beforeEach() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @Test
    public void getStudentHttpRequest() throws Exception{
        CollegeStudent student = new GradebookCollegeStudent("Eric", "Rudy"
                , "eric_rudy@luv2code_school.com");

        CollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby"
                , "chad_darby@luv2code_school.com");

        List<CollegeStudent> students = new ArrayList<>(Arrays.asList(student, studentTwo));

        when(studentCreateServiceMock.getGradeBook()).thenReturn(students);

        assertIterableEquals(students, studentCreateServiceMock.getGradeBook());

        // perform a GET request to '/'. Set expectation for status OK
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");
    }

    @Test
    public void createStudentHttpRequest() throws Exception {

        CollegeStudent studentOne = new CollegeStudent("Eric", "Rody", "eric_rody@luv2code_school.com");
        List<CollegeStudent> students = new ArrayList<>(Arrays.asList(studentOne));

        when(studentCreateServiceMock.getGradeBook()).thenReturn(students);

        assertIterableEquals(students, studentCreateServiceMock.getGradeBook());

        MvcResult result = this.mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameterValues("firstname"))
                .param("lastname", request.getParameterValues("lastname"))
                .param("emailAddress", request.getParameterValues("emailAddress")))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");

        // now verify if the student was add to DB
        CollegeStudent verifyStudent = studentRepository.findByEmailAddress("chad.darby@luv2code_school.com");

        assertNotNull(verifyStudent, "Student should be in DB");
    }

    @Test
    public void deleteStudentHttpRequest() throws Exception{
        // first make sure if student exist
        assertTrue(studentRepository.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/delete/student/{id}", 1))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "index");

        // make sure that the student was actually deleted
        assertFalse(studentRepository.findById(1).isPresent());
    }

    @Test
    public void deleteStudentHttpRequestErrorPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/delete/student/{id}", 0))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @Test
    public void studentInformationHttpRequest() throws Exception {
        assertTrue(studentRepository.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 1))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");
    }

    // test for invalid student
    @Test
    public void studentInformationHttpRequestStudentDoesNotExist() throws Exception {
        assertFalse(studentRepository.findById(0).isPresent());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}", 0))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @Test
    public void createValidGradeHttpRequest() throws Exception {
        assertTrue(studentRepository.findById(1).isPresent());

        GradebookCollegeStudent student = studentAndGradeService.studentInformation(1);

        assertEquals(1, student.getStudentGrades().getMathGradeResults().size());

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.0")
                .param("gradeType", "math")
                .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");

        student = studentAndGradeService.studentInformation(1);

        assertEquals(2, student.getStudentGrades().getMathGradeResults().size());
    }

    @Test
    public void createValidGradeHttpRequestWhenStudentDoesNotExist() throws Exception{
        // perform HTTP request
        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "history")
                .param("studentId", "0"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @Test
    public void createNonValidGradeHttpRequestGradeTypeDoesNotExistEmptyResponse() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("grade", "85.00")
                .param("gradeType", "Software technologies")
                .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

    @Test
    public void deleteAvailableGradeHttpRequest() throws Exception {

        Optional<MathGrade> mathGrade = mathGradeRepository.findById(1);

        assertTrue(mathGrade.isPresent());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/grades/{id}/{gradeType}", 1, "math"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(modelAndView, "studentInformation");

        // make sure that this grade was deleted and not present in DB
        mathGrade = mathGradeRepository.findById(1);
        assertFalse(mathGrade.isPresent());
    }

    @Test // test with 2
    public void deleteGradeWhenTheGradeIdNotValid() throws Exception {

        Optional<MathGrade> mathGrade = mathGradeRepository.findById(2);
        assertFalse(mathGrade.isPresent());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/grades/{id}/{gradeType}", 2, "math"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(modelAndView, "error");
    }

//    @Test // check for invalid subject
//    public void deleteANonValidGradeHttpRequest() throws Exception {
//
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/grade/{id}/{gradeType}", 1, "SQL course"))
//                .andExpect(status().isOk()).andReturn();
//
//        ModelAndView modelAndView = mvcResult.getModelAndView();
//        ModelAndViewAssert.assertViewName(modelAndView, "error");
//    }

    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

}
