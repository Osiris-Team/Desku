package com.osiris.desku.frontend_frameworks;

import com.osiris.desku.App;
import com.osiris.desku.frontend_frameworks.bootstrap.Bootstrap;

import java.io.IOException;
import java.util.*;

public class CSSVariableExtractor {

    public void printSortedCSSVariables(String css) {
        // Use a TreeMap to store variables grouped by values (for sorting)
        Map<String, List<String>> variablesByValue = new TreeMap<>();

        // Remove unnecessary characters from the CSS content
        css = css.replaceAll(":root\\s*\\{|\\s*}", "");

        // Split the CSS content by semicolons to handle multiple variable declarations
        String[] declarations = css.split(";");

        // Counter for total checked variables
        int totalCheckedVariables = 0;

        // Process each declaration
        for (String declaration : declarations) {
            declaration = declaration.trim();
            if (declaration.startsWith("--")) {
                totalCheckedVariables++; // Increment total checked variables counter

                // Extract variable name and value
                int colonIndex = declaration.indexOf(':');
                if (colonIndex != -1) {
                    String variable = declaration.substring(0, colonIndex).trim();
                    String value = declaration.substring(colonIndex + 1).trim();

                    // Add variable to the map, grouping by value
                    variablesByValue.computeIfAbsent(value, k -> new ArrayList<>()).add(variable);
                }
            }
        }

        // Remove variables that have only one usage
        variablesByValue.values().removeIf(variables -> variables.size() <= 1);

        // Sort variablesByValue by size of lists (number of usages) in descending order
        List<Map.Entry<String, List<String>>> sortedEntries = new ArrayList<>(variablesByValue.entrySet());
        sortedEntries.sort((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()));

        // Print sorted variables by value and list size in descending order
        int totalDuplicates = 0;
        for (Map.Entry<String, List<String>> entry : sortedEntries) {
            List<String> variables = entry.getValue();
            if (variables.size() > 1) {
                totalDuplicates += variables.size() - 1; // Count duplicates (excluding the first occurrence)
                Collections.sort(variables); // Sort variables alphabetically
                System.out.println(entry.getKey() + ": " + variables);
            }
        }

        // Print total amount of duplicates and total amount of checked variables
        System.out.println("Total amount of duplicates: " + totalDuplicates);
        System.out.println("Total amount of checked variables: " + totalCheckedVariables);
    }

    public static void main(String[] args) throws IOException {
        CSSVariableExtractor extractor = new CSSVariableExtractor();
        String cssContent = App.getCSS(Bootstrap.class);
        //String cssContent = App.getCSS(SemanticUI.class);
        //String cssContent = App.getCSS(Pico.class);
        extractor.printSortedCSSVariables(cssContent);
    }
}
