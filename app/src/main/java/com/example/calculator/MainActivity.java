package com.example.calculator;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main Activity for the Calculator application.
 * Manages the UI, button click handlers, and core calculation logic using a tokenization and
 * precedence-based evaluation approach.
 */
public class MainActivity extends AppCompatActivity {

    /** The EditText field that serves as the calculator screen/display. */
    private EditText screen;
    /** Tracks whether an opening parenthesis has been placed without a corresponding closing one. */
    private int openingParenthesesCounter = 0;

    /**
     * Casts a Double to a Long if it has no fractional part, otherwise returns the Double.
     * This is used for displaying results neatly (e.g., 4.0 becomes 4).
     * @param number The Double to cast.
     * @return A Long if the number is a whole number, otherwise the original Double.
     */
    private Object formatResultForDisplay(Double number) {
        long longNumber = number.longValue();
        double doubleNumber = (double)longNumber;
        // Check if the double value of the long is equal to the original double, indicating no fractional part.
        if (doubleNumber==number){
            return longNumber;
        }
        return number;
    }

    /**
     * Checks if an object represents a single digit (0-9) as a String, Character, or Integer.
     * Note: This method's logic may be fragile and less reliable than Character.isDigit(char).
     * @param obj The object to check (can be String, Character, or Integer).
     * @return true if the object is a digit, false otherwise.
     */
    private boolean isDigit(Object obj){
        if (obj instanceof String){
            try{
                // Attempts to parse as Integer and then checks if the string representation matches the original.
                Integer n =  Integer.valueOf(obj.toString());
                if(n.toString().equals(obj)){
                    return true;
                }
            }
            catch (NumberFormatException e){ // Using try-catch for flow control is generally discouraged.
                return false;
            }
        }
        else if (obj instanceof Character){
            try{
                // Similar check for Character, involves converting to string and then back
                Integer n = Integer.valueOf(obj.toString());
                Character c = n.toString().toCharArray()[0];
                if(c.equals(obj)){
                    return true;
                }
            }
            catch (NumberFormatException e){ // This check is complex and can be simplified.
                return false;
            }
        }
        else if (obj instanceof Integer){
            return true;
        }
        return false;
    }

    /**
     * Checks if a Double value represents an integer.
     * @param number The Double to check.
     * @return true if the number has no fractional part, false otherwise.
     */
    private boolean isInteger(Double number) {
        // A number is an integer if its double value is the same as its long value.
        long longNumber = number.longValue();
        double doubleNumber = (double)longNumber;
        return doubleNumber == number;
    }

    /**
     * Calculates the power of a number (base^exponent).
     * This method handles positive and negative, integer and fractional exponents.
     * Note: This implementation only supports non-negative bases for fractional exponents.
     * @param base The base number.
     * @param exponentValue The exponent.
     * @return The result of base raised to the power of exponentValue.
     */
    private Double exponent(Double base, Double exponentValue) {
        // Handle negative exponents by calculating the reciprocal of the result for the positive exponent.
        boolean isNegative = false;
        if (exponentValue < 0) {
            exponentValue *= -1.0;
            isNegative = true;
        }
        // Handle the case of a zero exponent, which always results in 1.
        if (exponentValue == 0) {
            return 1.0;
        }
        // If the exponent is an integer, calculate power through simple multiplication.
        if (isInteger(exponentValue)){
            Double result = 1.0;
            for (int i = 0; i < exponentValue; i++) {
                result *= base;
            }
            if (isNegative){return 1/result;}
            return result;
        }
        // If the exponent is a fraction, convert it to a numerator/denominator pair.
        // The calculation becomes (base^(numerator/denominator)), which is equivalent to
        // (base^(1/denominator))^numerator, calculated here as (root(base, denominator))^numerator.
        else {
            String exponentString = exponentValue.toString();
            String fractionalPartString = exponentString.substring(exponentString.indexOf(".") + 1);

            // Initial numerator and denominator from the decimal part.
            Double denominator = 1.0;
            for (int i = 0; i < fractionalPartString.length(); i++) {
                denominator *= 10;
            }
            Double numerator = denominator * Double.valueOf(exponentValue);

            // Simplify the fraction by dividing by common factors (2 and 3).
            // This is a basic simplification and could be improved with a GCD algorithm.
            Double gcDivisor = gcd(numerator,denominator);

            numerator /= gcDivisor;
            denominator /= gcDivisor;

            // Recursively call exponent with the root as the new base and the numerator as the new exponent.
            Double result = exponent(root(base, denominator), numerator);

            // If the original exponent was negative, return the reciprocal.
            if (isNegative) { return 1.0 / result; }
            return result;
        }
    }

    /**
     * Calculates the nth root of a number using Newton's method.
     * This method finds a value 'x' such that x^rootDegree = number.
     * It uses the iterative formula from Newton's method:
     * x_next = x_current - (x_current^rootDegree - number) / (rootDegree * x_current^(rootDegree-1))
     * @param number The number to find the root of. Must be non-negative if rootDegree is even.
     * @param rootDegree The degree of the root (e.g., 2 for square root).
     * @return The nth root of the number, or null if an even root of a negative number is attempted.
     */
    private static Double root(Double number, Double rootDegree) {
        // An even root of a negative number is not a real number.
        if (number < 0 && rootDegree % 2 == 0){
            return null;
        }
        // Set an initial guess for Newton's method. A good initial guess is crucial
        // for the speed and stability of convergence.
        Double currentGuess;
        // For numbers <= 1, the number itself is a good starting point.
        if (number <= 1.0) {
            currentGuess = number;
            // For very large numbers, number/rootDegree provides a better initial estimate.
        } else if (number>exponent(10.0,14.0)){
            currentGuess = number / rootDegree;
        } else{
            // For other numbers, a value close to 1 is a reasonable default.
            currentGuess = 0.99999999999999;
        }
        Double nextGuess = 1.0;
        int iteration = 300;
        while(iteration-- > 0) {
            // Apply one iteration of Newton's method to get a better approximation of the root.
            double fx = exponent(currentGuess, rootDegree) - number;
            double f_prime_x = rootDegree * exponent(currentGuess, rootDegree - 1);
            nextGuess = currentGuess - (fx / f_prime_x);

            // Check for convergence. If the guess is no longer changing significantly,
            // we have found the root to a sufficient precision.
            if (Math.abs(currentGuess - nextGuess) < 1e-12) {
                return nextGuess;
            }
            currentGuess = nextGuess;
        }
        return nextGuess;
    }

    /**
     * Calculates the greatest common divisor (GCD) of two numbers using the Euclidean algorithm.
     * This implementation converts Double inputs to long integers to perform the calculation.
     * @param a The first number.
     * @param b The second number.
     * @return The greatest common divisor of a and b, as a Double.
     */
    private Double gcd(Double a, Double b) {
        // The algorithm works with positive integers, so take the absolute value of the inputs.
        a = (a>0) ? a : -a;
        b = (b>0) ? b : -b;

        // Convert Doubles to longs for the Euclidean algorithm.
        // Assign the larger number to x and the smaller to y.
        long x = (a>b) ? a.longValue() : b.longValue();
        long y = (a<b) ? a.longValue()  : b.longValue() ;

        // Apply the Euclidean algorithm: repeatedly replace the larger number
        // with the remainder of the division of the larger number by the smaller one.
        while (y!=0){
            long temp = x;
            x = y;
            y = temp%y;
        }
        // The GCD is the last non-zero remainder, which is stored in x.
        return (double) x;
    }

    /**
     * Performs a specific arithmetic operation (+, -, *, /, ^) on a list of numbers and operators,
     * following the left-to-right order for the specified operator.
     * The list is modified in-place by replacing the sub-expression (number1, operator, number2)
     * with the result (number1 operator number2).
     * @param items The list of numbers (Doubles) and operators (Characters).
     * @param operator The character of the operation to perform.
     * @return The modified list after performing all occurrences of the specified operation.
     */
    private void operation(ArrayList<Object> items , char operator, int index){
        // Assumes format is always [number1, operator, number2] at this stage.
        Double number1 = (Double)items.get(index-1);
        Double number2 = (Double)items.get(index+1);
        // Perform the operation
        switch(operator) {
            case '^':
                items.set(index-1, exponent(number1,number2));
                break;
            case '*':
                items.set(index-1, number1*number2);
                break;
            case '/':
                items.set(index-1, number1/number2);
                break;
            case '+':
                items.set(index-1, number1+number2);
                break;
            case '-':
                items.set(index-1, number1-number2);
                break;
        }
        // Remove the operator and the second operand, leaving the result in place of the first operand.
        items.remove(index);
        items.remove(index);
    }

    /**
     * Calculates the square root (√) of the operand that immediately follows it in the token list.
     * It uses the Babylonian method for approximation. The token list is modified in-place.
     * @param items The list of numbers and operators, which may contain the '√' character.
     */
    private void squareRoot(ArrayList<Object> items, int index) {
        Double number = (Double)items.get(index+1);
        // Square root of a negative number is not supported in this implementation.
        if (number<0){
            return;
        }
        // Initial guess for the square root.
        Double initial = (number+1)/10;
        Double sqrtVal;

        // Babylonian method iteration for finding the square root.
        while(true) {
            // New approximation: (initial + (number / initial)) / 2.
            sqrtVal = (initial+(number/initial))/2;
            // Check for convergence (when the initial and new approximation are effectively equal).
            if (initial.equals(sqrtVal)) {
                break;
            }
            initial = sqrtVal;
        }
        // Remove the operator token
        items.remove(index);
        // Set the result in the position of the original operand
        items.set(index,sqrtVal);
    }

    /**
     * Calculates the result of an expression stored in a list of tokens, respecting the order of operations (PEMDAS/BODMAS).
     * Operations are executed in the order: Exponentiation (^), Square Root (√), Multiplication/Division (*, /), Addition/Subtraction (+, -).
     * @param items The list of numbers (Doubles) and operators (Characters).
     * @return The final result of the calculation as a Double.
     */
    private Double calculate(ArrayList<Object> items){
        int index = 0; // Index for iterating through the token list.
        // 1. Process Exponents (^) and Square Roots (√) - Highest precedence.
        if (items.contains('√') || items.contains('^')){
            while (index<items.size()){
                if(items.get(index).equals('√')){
                    squareRoot(items,index);
                } // Note: squareRoot modifies the list and the loop continues from the next index.
                else if(items.get(index).equals('^')){
                    operation(items,'^',index);
                    continue; // Continue from the beginning after modification
                }
                index++;
            }
        }
        // 2. Process Multiplication (*) and Division (/) - Medium precedence.
        if (items.contains('*') || items.contains('/')){
            index = 0;
            while (index<items.size()){
                if(items.get(index).equals('*')){
                    operation(items,'*',index);
                    continue; // Continue from the beginning after modification
                }
                else if(items.get(index).equals('/')){
                    operation(items,'/',index);
                    continue; // Continue from the beginning after modification
                }
                index++;
            }
        }
        // 3. Process Addition (+) and Subtraction (-) - Lowest precedence.
        if (items.contains('+') || items.contains('-')){
            index = 0;
            while (index<items.size()){
                if(items.get(index).equals('+')){
                    operation(items,'+',index);
                    continue; // Continue from the beginning after modification
                }
                else if(items.get(index).equals('-')){
                    // Check for unary minus (negative sign at the start of the expression)
                    if (index==0 && (index+1)<items.size()){
                        Double number = (Double) (items.get(index+1));
                        items.set(index,number*-1); // Set the token to the negative value
                        items.remove(index+1);
                        continue; // Continue from the beginning after modification
                    }
                    // If not unary minus, perform binary subtraction
                    operation(items,'-',index);
                    continue; // Continue from the beginning after modification
                }
                index++;
            }
        }
        // After all operations, only the final result should remain at index 0.
        return (Double)items.get(0);
    }
    /**
     * Splits a mathematical expression string into a list of numbers (Doubles) and operators (Characters).
     * It handles multi-digit numbers and decimal points.
     * @param string The input expression string.
     * @return An ArrayList of Objects, containing Doubles for numbers and Characters for operators.
     */
    private ArrayList<Object> splitIntoNumbersOperators(String string){
        ArrayList<Object> items = new ArrayList<Object>();
        // Wrap the entire expression in parentheses to simplify parenthesis resolution logic.
        items.add('(');
        ArrayList<String> number = new ArrayList<String>();
        for (char c : string.toCharArray()) {
            // If the character is a digit or a decimal point, append it to the current number string.
            if(isDigit(c) || c=='.'){
                number.add(""+c);
            }
            else{
                // If an operator is encountered, finalize the current number (if any) and add it to items.
                if (number.size()>0){
                    Double num = Double.parseDouble(String.join("",number));
                    items.add(num);
                }
                // Add the operator token.
                items.add(c);
                // Start a new number accumulator.
                number = new ArrayList<String>();
            }
        }
        // Add the last number if the expression didn't end with an operator.
        if (number.size()>0){
            Double num = Double.parseDouble(String.join("",number));
            items.add(num);
        }
        items.add(')');
        return items;
    }

    /**
     * Calculates the result of the expression between the provided start '(' and end ')' indices.
     * The original list is modified in-place: the sub-expression (including parentheses) is removed,
     * and the calculated result is placed at the starting index.
     * @param items The main list of tokens.
     * @param start The index of the opening parenthesis '('.
     * @param end The index of the closing parenthesis ')'.
     */
    private void calculateSubExpression(ArrayList<Object> items, int start, int end){
        // Extract the sub-expression that needs to be calculated.
        ArrayList<Object> itemsBetweenParentheses = new ArrayList<Object>(items.subList(start+1,end));
        // Remove the content of the parentheses (including ')') from the original list.
        for (int j=start+1 ; j<=end; j++){
            items.remove(start+1);
        }

        // Calculate the result of the sub-expression and replace the opening parenthesis '(' with the result.
        items.set(start, calculate(itemsBetweenParentheses));
    }

    /**
     * Resolves nested parentheses by finding the innermost pairs first, calculating their result,
     * and replacing the sub-expression with the result. This process repeats until no parentheses are left.
     * @param items The list of tokens, which is modified in-place.
     */
    private void resolveNestedParentheses(ArrayList<Object> items){
        int index = 0;
        int start = -1; // Tracks the index of the most recently found '('.
        while (index<items.size()){
            if(items.get(index).equals('(')) start = index; // Mark the start of a potential nested expression.
            else if(items.get(index).equals(')')){
                // Found a closing parenthesis.
                if (start != -1){
                    // This is a complete, innermost expression: calculate it.
                    calculateSubExpression(items, start, index);
                    index = 0;
                    continue;
                }
            }
            index++;
        }
    }

    /**
     * Main calculation function. It retrieves the expression from the screen, processes it by:
     * 1. Splitting into tokens (numbers and operators).
     * 2. Evaluating expressions in parentheses.
     * 3. Calculating the final result following the order of operations.
     * Finally, it displays the result (casted to Long if an integer) or an "ERROR!" with a Toast notification.
     */
    private void executeCalculation(){
        try {
            // 1. Tokenize the input string
            ArrayList<Object> items = splitIntoNumbersOperators(screen.getText().toString());
            // 2. Resolve parentheses from the inside out.
            resolveNestedParentheses(items);
            // 3. Display the final result, formatting to Long if it's a whole number.
            screen.setText(""+formatResultForDisplay((Double) items.get(0)));
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            // On any calculation error, clear the screen.
            screen.setText("");
            // Show a short "ERROR!" notification to the user
            Toast.makeText(this,"ERROR!",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Appends the text of the clicked button (choice) to the text field (screen).
     * This is the general handler for most calculator buttons.
     * @param view The button that was clicked.
     */
    private void onclick(View view) {
        Button button = (Button) view;
        // Get current text and append the new character from the button.
        String text = screen.getText().toString();
        screen.setText(text+button.getText().toString());
    }

    /**
     * Clears the text field (screen).
     */
    private void clearScreen() {
        screen.setText("");
        // Reset the parenthesis counter as well.
        openingParenthesesCounter = 0;
    }
    /**
     * Removes the last character from the text field (screen).
     */
    private void deleteLastItem() {
        if(screen.getText().length()==0) {
            return; // Nothing to remove
        }
        // Array to hold characters as strings
        String[] elements = new String[screen.getText().length()]; // This is an inefficient way to remove the last char.
        int trucker = 0;
        // Populate the array with individual characters
        for(char c : screen.getText().toString().toCharArray()) {
            elements[trucker] = ""+c;
            trucker++;
        }
        // Adjust parenthesis counter if a parenthesis is being deleted.
        if (elements.length>0){
            if (elements[elements.length-1].equals("(")){
                openingParenthesesCounter--; // Decrement if an opening parenthesis is removed.
            }
            else if (elements[elements.length-1].equals(")")){
                openingParenthesesCounter++; // Increment if a closing parenthesis is removed (counterintuitive, but matches toggle logic).
            }
        }
        // Rebuild the string using all elements except the last one.
        // A more efficient approach would be to use screen.getText().delete(length-1, length).
        screen.setText(String.join("", Arrays.copyOf(elements, elements.length-1)));
    }
    /**
     * Toggles between inserting an opening "(" and a closing ")" parenthesis and updates the state.
     * @return A "(" if no parenthesis is currently open, or a ")" if one is.
     */
    private String toggleParenthesis() {
        // If the counter is 0, it means we need an opening parenthesis.
        if (openingParenthesesCounter==0){
            openingParenthesesCounter++;
            return "(";
        }
        else {
            openingParenthesesCounter--;
            return ")";
        }
    }

    /**
     * Called when the activity is first created. Initializes the UI and sets up all button listeners,
     * including basic input validation.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply system window insets to main view for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the screen EditText
        screen = findViewById(R.id.screenEditTextText);

        // --- Set up numerical button click listeners (basic number input) ---
        findViewById(R.id.button0).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button1).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button2).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button3).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button4).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button5).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button6).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button7).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button8).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.button9).setOnClickListener(v -> {onclick(v);});

        // --- Operator and Special Function Listeners with Validation ---

        findViewById(R.id.buttonAdd).setOnClickListener(v -> {
            // Only allow '+' if the screen is not empty AND the last character is a digit or ')'.
            if (screen.getText().toString().length()>0 &&
                    (isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1])
                            || screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]==')')){
                onclick(v);
            }
        });
        findViewById(R.id.buttonSubtract).setOnClickListener(v -> {{onclick(v);}}); // Subtraction is always allowed (can be unary minus)
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> { // Same validation as addition.
            // Only allow '*' if the screen is not empty AND the last character is a digit or ')'
            if (screen.getText().toString().length()>0 &&
                    (isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1])
                            || screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]==')')){
                onclick(v);
            }
        });
        findViewById(R.id.buttonDivid).setOnClickListener(v -> { // Same validation as addition.
            // Only allow '/' if the screen is not empty AND the last character is a digit or ')'
            if (screen.getText().toString().length()>0 &&
                    (isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1])
                            || screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]==')')){
                onclick(v);
            }
        });
        findViewById(R.id.buttonExponent).setOnClickListener(v -> { // Same validation as addition.
            // Only allow '^' if the screen is not empty AND the last character is a digit or ')'
            if (screen.getText().toString().length()>0 &&
                    (isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1])
                            || screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]==')')){
                onclick(v);
                String text = screen.getText().toString();
                // Automatically add an opening parenthesis after exponent to enforce order of operations.
                screen.setText(text+"(");
                openingParenthesesCounter++;
            }
        });
        findViewById(R.id.buttonPoint).setOnClickListener(v -> {
            // Only allow '.' if the screen is not empty AND the last character is a digit
            if (screen.getText().toString().length()>0 &&
                    isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1])){
                onclick(v);
            }
        });
        findViewById(R.id.buttonSquareRoot).setOnClickListener(v -> { // Validation for square root.
            // Allow '√' if the screen is empty OR if the last character is not a digit (i.e., it follows an operator)
            if (screen.getText().toString().length()==0 ||
                    (screen.getText().toString().length()>0 &&
                            !isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]))) {
                onclick(v);
                String text = screen.getText().toString();
                // Automatically add an opening parenthesis after square root.
                screen.setText(text+"(");
                openingParenthesesCounter++;
            }
        });

        findViewById(R.id.buttonPy).setOnClickListener(v -> {
            // Appends Pi (22/7 approx) only if the current entry does not already contain a decimal point
            if ((screen.getText().toString().length()>0 &&
                    !isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]) &&
                    screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]!='.') ||
                    screen.getText().toString().length()==0) {
                String text = screen.getText().toString();
                screen.setText(text + ((double) 22 / 7)); // Uses double division for approximation
            }
        });
        findViewById(R.id.buttonEulir).setOnClickListener(v -> {
            // Appends Euler's number (19/7 approx) only if the current entry does not already contain a decimal point
            if ((screen.getText().toString().length()>0 &&
                    !isDigit(screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]) &&
                    screen.getText().toString().toCharArray()[screen.getText().toString().length()-1]!='.') ||
                    screen.getText().toString().length()==0) {
                String text = screen.getText().toString();
                screen.setText(text+((double)19/7)); // Uses double division for approximation
            }
        });
        findViewById(R.id.buttonParenth).setOnClickListener(v -> {
            String text = screen.getText().toString();
            // Appends '(' or ')' based on the internal state (parenthesesOpen flag)
            screen.setText(text+toggleParenthesis());
        });

        // --- Set up action button click listeners ---
        findViewById(R.id.buttonEquals).setOnClickListener(v -> {executeCalculation();});
        findViewById(R.id.buttonDelete).setOnClickListener(v -> {deleteLastItem();});
        findViewById(R.id.buttonClear).setOnClickListener(v -> {clearScreen();});
    }
}