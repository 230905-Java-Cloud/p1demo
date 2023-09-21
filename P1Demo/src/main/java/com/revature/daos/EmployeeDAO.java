package com.revature.daos;

import com.revature.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/* By extending JpaRepository, we get access to various DAO methods that we DON'T NEED TO WRITE
    ctrl + click on JpaRepository to see what methods are provided for us already

    JpaRepository takes two values in its generic -
    -the DB table/model we're dealing with
    -the data type of the model's ID (in wrapper class form)
 */

@Repository //we want this interface to be a bean
public interface EmployeeDAO extends JpaRepository<Employee, Integer> {

    //extending JpaRepository give us a bunch of methods out the box. See this custom DAO method below:

    //I want to be able to find an employee by username.
    //Unfortunately, Spring Data only knows the primary key. We have to define this on our own.
    //Spring Data IS smart enough to implement this method for us. We just define the abstraction.
    public Optional<Employee> findByUsername(String username);

    /* HOW DOES THIS WORK?
    By having a method name starting with "findBy" and ending in the variable you want to find by!
    Spring needs your method name to be camelCase, or it will throw very vague errors
    Also, it can't have underscores.

    There are a lot of options for custom DAO methods.
    Look into things like Native Queries if you need really specific DAO methods */

}
