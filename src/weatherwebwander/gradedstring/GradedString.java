/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander.gradedstring;

/**
 *
 * @author simonkenny
 */
public class GradedString {
    private final String str;
    private int grade;

    public GradedString(String str) {
        this.str = str;
        grade = 0;
    }

    public GradedString(String str, int grade) {
        this.str = str;
        this.grade = grade;
    }

    public void addToGrade(int val) {
        grade += val;
    }

    public String getString() {
        return str;
    }

    public int getGrade() {
        return grade;
    }
}