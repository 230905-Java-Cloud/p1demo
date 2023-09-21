package com.revature.models;

import org.springframework.stereotype.Component;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Component
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleId;

    @Column(nullable = false, unique = true)
    private String roleTitle;

    @Column(nullable = false)
    private double roleSalary;

    //boilerplate code


    public Role() {
    }

    public Role(int roleId, String roleTitle, double roleSalary) {
        this.roleId = roleId;
        this.roleTitle = roleTitle;
        this.roleSalary = roleSalary;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public double getRoleSalary() {
        return roleSalary;
    }

    public void setRoleSalary(double roleSalary) {
        this.roleSalary = roleSalary;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleTitle='" + roleTitle + '\'' +
                ", roleSalary=" + roleSalary +
                '}';
    }
}
