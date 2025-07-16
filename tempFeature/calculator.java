/**
 * A simple Calculator class that performs basic arithmetic operations.
 *
 * TODO:
 * - Add support for more operations (e.g., exponentiation, modulo)
 * - Add input validation and exception handling
 * - Write unit tests
 */

public class Calculator {

    public double add(double a, double b) {
        return a + b;
    }

    public double subtract(double a, double b) {
        return a - b;
    }

    public double multiply(double a, double b) {
        return a * b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero");
        }
        return a / b;
    }

    public static void main(String[] args) {
        Calculator calc = new Calculator();

        System.out.println("Add: " + calc.add(5, 3));
        System.out.println("Subtract: " + calc.subtract(5, 3));
        System.out.println("Multiply: " + calc.multiply(5, 3));
        System.out.println("Divide: " + calc.divide(5, 3));
    }
}
