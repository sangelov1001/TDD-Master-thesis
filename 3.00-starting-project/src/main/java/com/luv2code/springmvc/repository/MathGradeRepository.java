package com.luv2code.springmvc.repository;

import com.luv2code.springmvc.models.MathGrade;
import org.springframework.data.repository.CrudRepository;

public interface MathGradeRepository extends CrudRepository<MathGrade, Integer> {

     Iterable<MathGrade> findGradeByStudentId(int id);

     void deleteByStudentId(int id);
}
