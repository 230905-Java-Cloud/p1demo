package com.revature.daos;

import com.revature.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* By extending JpaRepository, we get access to various DAO methods that we DON'T NEED TO WRITE
    ctrl + click on JpaRepository to see what methods are provided for us already

    JpaRepository takes two values in its generic -
    -the DB table/model we're dealing with
    -the data type of the model's ID (in wrapper class form)
 */

@Repository //we want this interface to be a bean
public interface EmployeeDAO extends JpaRepository<Employee, Integer> {

    //our DAO is done!

    //for now - TODO: I will show custom DAO methods for more complicated procedures

}
