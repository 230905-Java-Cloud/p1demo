package com.revature.daos;

import com.revature.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/*Hi, if you're reading this you're probably revising the demo to find keys to your own P1.

This app doesn't have any endpoints that query or manipulate the Roles table
But we DO need this interface to insert Employees.

Employees have Roles. But we don't want to insert an entire Role object when inserting an Employee.
So I made this Interface solely for access to the findById method, which we'll use to add an Employee.

Basically, the Role id will be included in the path variable for the POST method that adds employees
The path variable will get extracted, and used as the argument to find the appropriate role by Id.
Once the appropriate Role is found, it's simply a matter of using setRole() on the incoming Employee.

 */


@Repository
public interface RoleDAO extends JpaRepository<Role, Integer> {
}
