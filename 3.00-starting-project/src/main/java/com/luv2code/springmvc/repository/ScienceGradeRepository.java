package com.luv2code.springmvc.repository;

import com.luv2code.springmvc.models.ScienceGrade;
import org.springframework.data.repository.CrudRepository;

public interface ScienceGradeRepository extends CrudRepository<ScienceGrade, Integer> {

    Iterable<ScienceGrade> findGradeByStudentId(int id);

    void deleteByStudentId(int id);
}
