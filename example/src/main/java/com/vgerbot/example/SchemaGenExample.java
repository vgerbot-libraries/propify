package com.vgerbot.example;

/**
 * Example demonstrating the usage of schema-generated classes.
 * 
 * After compilation, this example shows how to use the generated User and Pet classes
 * that are created from JSON Schema and OpenAPI definitions.
 */
public class SchemaGenExample {
    
    public static void main(String[] args) {
        System.out.println("============== SchemaGen Examples ==============\n");
        
        // Example 1: Using JSON Schema generated User class
        System.out.println("--- JSON Schema Example: User ---");
        
        // Note: After compilation, you would use the generated class like this:
        /*
        User user = User.builder()
            .id(1)
            .username("johndoe")
            .email("john@example.com")
            .age(30)
            .active(true)
            .roles(Arrays.asList("admin", "user"))
            .profile(User.Profile.builder()
                .firstName("John")
                .lastName("Doe")
                .bio("Software developer")
                .build())
            .build();
        
        System.out.println("User created: " + user);
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        
        // Jackson serialization example
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(user);
        System.out.println("JSON: " + json);
        
        // Deserialization
        User deserializedUser = mapper.readValue(json, User.class);
        System.out.println("Deserialized: " + deserializedUser);
        */
        
        System.out.println("\n--- OpenAPI Example: Pet ---");
        
        // Example 2: Using OpenAPI generated Pet class
        /*
        Pet pet = Pet.builder()
            .id(123L)
            .name("Fluffy")
            .tag("cat")
            .status("available")
            .birthDate(LocalDate.of(2020, 5, 15))
            .price(299.99)
            .build();
        
        System.out.println("Pet created: " + pet);
        System.out.println("Pet name: " + pet.getName());
        System.out.println("Status: " + pet.getStatus());
        
        // Modify using setters
        pet.setStatus("sold");
        pet.setPrice(249.99);
        System.out.println("Updated pet: " + pet);
        
        // Use with REST clients
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Pet> response = restTemplate.getForEntity(
            "https://api.example.com/pets/" + pet.getId(),
            Pet.class
        );
        Pet retrievedPet = response.getBody();
        */
        
        System.out.println("\nNote: Uncomment the code above after running Maven compile");
        System.out.println("The classes will be generated during annotation processing");
        System.out.println("\nGenerated classes will have:");
        System.out.println("- Getters and setters for all properties");
        System.out.println("- Builder pattern for fluent construction");
        System.out.println("- Jackson annotations for JSON serialization");
        System.out.println("- Bean Validation annotations (e.g., @NotNull, @Email, @Size)");
        System.out.println("- equals(), hashCode(), and toString() methods");
        System.out.println("- Serializable implementation");
    }
}

