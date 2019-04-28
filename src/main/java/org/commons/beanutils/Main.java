package org.commons.beanutils;

import java.util.HashMap;
import java.util.Map;

import org.commons.beanutils.pojo.Student;

public class Main {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "gqm");
        map.put("age", 22);
        map.put("exist", true);
        map.put("weight", 74);

        Student s = BeanUtilsFaced.mapToBean(map, Student.class);
        System.out.println(s);

        Student s1 = new Student();
        BeanUtilsFaced.BeanToBean(s1, s);
        System.out.println(s1);
        
        Student s2 = new Student();
        BeanUtilsFaced.mapToBean(map, s2);
        System.out.println(s2);
    }
}
