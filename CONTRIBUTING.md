# Contributing to Propify

Thank you for your interest in contributing to Propify! We welcome contributions from the community.

## How to Contribute

### Reporting Issues

If you find a bug or have a feature request:

1. Check if the issue already exists in the [issue tracker](https://github.com/vgerbot-libraries/propify/issues)
2. If not, create a new issue with a clear title and description
3. Include relevant details:
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Your environment (Java version, build tool, OS)
   - Code samples or stack traces if applicable

### Submitting Pull Requests

1. **Fork the repository** and create your branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Write clean, readable code
   - Follow the existing code style
   - Add tests for new features
   - Update documentation as needed

3. **Test your changes**:
   ```bash
   mvn clean test
   ```

4. **Commit your changes**:
   - Use clear, descriptive commit messages
   - Reference issue numbers if applicable (e.g., "Fix #123: Description")

5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open a Pull Request**:
   - Provide a clear description of the changes
   - Link to any related issues
   - Ensure CI builds pass

## Development Setup

### Prerequisites

- Java 8 or higher
- Maven 3.6+
- Git

### Building from Source

```bash
# Clone the repository
git clone https://github.com/vgerbot-libraries/propify.git
cd propify

# Build the project
mvn clean install

# Run tests
mvn test

# Run examples
cd example
mvn clean compile exec:java
```

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods focused and concise
- Write tests for new functionality

## Testing

- Write unit tests for all new features
- Ensure all tests pass before submitting PR
- Aim for good test coverage
- Include both positive and negative test cases

## Documentation

- Update relevant documentation in `docs/` for feature changes
- Update README.md if adding major features
- Add examples to `example/` for new functionality
- Include Javadoc for public APIs

## Questions?

If you have questions about contributing, feel free to:

- Open a discussion in the [issue tracker](https://github.com/vgerbot-libraries/propify/issues)
- Check existing documentation in the `docs/` folder

## License

By contributing to Propify, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing! 🎉
