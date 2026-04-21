package org.example.rhcamunda.aspect;

public class AuditContextHolder {

    private static final ThreadLocal<String> ancienEtat = new ThreadLocal<>();

    public static void setAncienEtat(String etat) {
        ancienEtat.set(etat);
    }

    public static String getAncienEtat() {
        return ancienEtat.get();
    }

    public static void clear() {
        ancienEtat.remove();
    }
}