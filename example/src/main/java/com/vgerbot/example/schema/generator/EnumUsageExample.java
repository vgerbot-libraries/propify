package com.vgerbot.example.schema.generator;

import org.apache.commons.lang3.StringUtils;

/**
 * Example showing how to use generated enum classes.
 *
 * This demonstrates the enum classes that would be generated from schema definitions.
 */
public class EnumUsageExample {

    public static void main(String[] args) {
        System.out.println("=== Generated Enum Usage Example ===\n");

        demonstrateStringEnum();
        System.out.println();
        demonstrateIntegerEnum();
    }

    /**
     * Demonstrates usage of string-based enum (e.g., Status, Role)
     */
    private static void demonstrateStringEnum() {
        System.out.println("1. String-based Enum Usage:");
        System.out.println(StringUtils.repeat("-", 80));

        // The generated enum would look like this:
        // public enum Status {
        //     ACTIVE("active"),
        //     INACTIVE("inactive"),
        //     SUSPENDED("suspended"),
        //     DELETED("deleted");
        //
        //     private final String value;
        //     Status(String value) { this.value = value; }
        //     public String getValue() { return value; }
        //     public static Status fromValue(String value) { ... }
        // }

        System.out.println("Example usage in your code:");
        System.out.println("  // Setting enum value");
        System.out.println("  user.setStatus(User.Status.ACTIVE);");
        System.out.println();
        System.out.println("  // Getting enum value");
        System.out.println("  User.Status status = user.getStatus();");
        System.out.println("  String statusValue = status.getValue(); // Returns \"active\"");
        System.out.println();
        System.out.println("  // Converting from string");
        System.out.println("  User.Status fromString = User.Status.fromValue(\"active\");");
        System.out.println();
        System.out.println("  // Using in switch statements");
        System.out.println("  switch (user.getStatus()) {");
        System.out.println("      case ACTIVE:");
        System.out.println("          System.out.println(\"User is active\");");
        System.out.println("          break;");
        System.out.println("      case SUSPENDED:");
        System.out.println("          System.out.println(\"User is suspended\");");
        System.out.println("          break;");
        System.out.println("      default:");
        System.out.println("          System.out.println(\"Other status\");");
        System.out.println("  }");
    }

    /**
     * Demonstrates usage of integer-based enum (e.g., Priority)
     */
    private static void demonstrateIntegerEnum() {
        System.out.println("2. Integer-based Enum Usage:");
        System.out.println(StringUtils.repeat("-", 80));

        // The generated enum would look like this:
        // public enum Priority {
        //     VALUE_1(1),
        //     VALUE_2(2),
        //     VALUE_3(3),
        //     VALUE_4(4),
        //     VALUE_5(5);
        //
        //     private final Integer value;
        //     Priority(Integer value) { this.value = value; }
        //     public Integer getValue() { return value; }
        //     public static Priority fromValue(Integer value) { ... }
        // }

        System.out.println("Example usage in your code:");
        System.out.println("  // Setting enum value");
        System.out.println("  user.setPriority(User.Priority.VALUE_5);");
        System.out.println();
        System.out.println("  // Getting enum value");
        System.out.println("  User.Priority priority = user.getPriority();");
        System.out.println("  Integer priorityValue = priority.getValue(); // Returns 5");
        System.out.println();
        System.out.println("  // Converting from integer");
        System.out.println("  User.Priority fromInt = User.Priority.fromValue(3);");
        System.out.println();
        System.out.println("  // Comparing priorities");
        System.out.println("  if (user.getPriority().getValue() > 3) {");
        System.out.println("      System.out.println(\"High priority user\");");
        System.out.println("  }");
        System.out.println();

        System.out.println("\n" + StringUtils.repeat("=", 50));
        System.out.println("Key Features of Generated Enums:");
        System.out.println(StringUtils.repeat("=", 80));
        System.out.println("✓ Type-safe enum constants");
        System.out.println("✓ getValue() method to get the underlying value");
        System.out.println("✓ fromValue() method for deserialization");
        System.out.println("✓ Proper toString() implementation");
        System.out.println("✓ Works with Jackson JSON serialization");
        System.out.println("✓ Supports validation annotations");
        System.out.println("✓ IDE autocomplete support");
        System.out.println("✓ Compile-time type checking");
    }
}
