package com.karandev.paymaster.helper;
import org.springframework.stereotype.Service;

@Service
public class UniqueEmployeeCodeGenerator {

    public static String generateEmpCode(String companyName) {
        String[] words = companyName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        int random = (int) (Math.random() * 900_000) + 100_000;
        return "EMP-" + initials.toString() + random;
    }

}
