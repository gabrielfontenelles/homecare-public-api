package com.example.homecare_adm_app.api.util;


import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    public static String removeAccents(String str) {
        if (str == null) {
            return null;
        }
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static String formatCpf(String cpf) {
        if (isEmpty(cpf)) {
            return cpf;
        }

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return cpf;
        }

        return cpf.substring(0, 3) + "." +
                cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" +
                cpf.substring(9);
    }

    public static String formatTelefone(String telefone) {
        if (isEmpty(telefone)) {
            return telefone;
        }

        telefone = telefone.replaceAll("\\D", "");

        if (telefone.length() == 11) {
            return "(" + telefone.substring(0, 2) + ") " +
                    telefone.substring(2, 7) + "-" +
                    telefone.substring(7);
        } else if (telefone.length() == 10) {
            return "(" + telefone.substring(0, 2) + ") " +
                    telefone.substring(2, 6) + "-" +
                    telefone.substring(6);
        }

        return telefone;
    }

    public static String formatCep(String cep) {
        if (isEmpty(cep)) {
            return cep;
        }

        cep = cep.replaceAll("\\D", "");

        if (cep.length() != 8) {
            return cep;
        }

        return cep.substring(0, 5) + "-" + cep.substring(5);
    }
}