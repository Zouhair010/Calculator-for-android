package com.example.calculator;

import android.os.Bundle;
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
    private boolean parenthesesOpen = false;

    /**
     * Casts a Double to a Long if it has no fractional part, otherwise returns the Double.
     * This is used for displaying results neatly (e.g., 4.0 becomes 4).
     * @param number The Double to cast.
     * @return A Long if the number is a whole number, otherwise the original Double.
     */
    private Object cast(Double number) {
        long longNumber = number.longValue();
        double doubleNumber = (double)longNumber;
        // Check if the double value of the long is equal to the original double
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
                // Attempts to parse as Integer and then checks if the string representation matches the original
                Integer n =  Integer.valueOf(obj.toString());
                if(n.toString().equals(obj)){
                    return true;
                }
            }
            catch (NumberFormatException e){
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
            catch (NumberFormatException e){
                return false;
            }
        }
        else if (obj instanceof Integer){
            return true;
        }
        return false;
    }
    /**
     * Checks if an ArrayList contains a specific item by iterating through the list.
     * @param items The ArrayList to search through.
     * @param item The item to search for.
     * @return true if the item is found, false otherwise.
     */
    private boolean contains(ArrayList<Object> items ,Object item){
        for (Object i : items){
            if (i.equals(item)){
                return true;
            }
        }
        return false;
    }
    // REMOVED: private boolean contains(char[] items ,char item){...} - This overloaded method was unused.

    /**
     * Calculates the exponent of a number (a^b) using a loop for positive integer exponents (b).
     * Note: This implementation only supports non-negative integer exponents.
     * @param a The base.
     * @param b The exponent.
     * @return The result of a raised to the power of b.
     */
    private Double exponent(Double a, Double b) {
        Double times = b;
        Double exp = (double) 1;
        while (times>0) {
            exp*=a;
            times--;
        }
        return exp;
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
    private ArrayList<Object> operation(ArrayList<Object> items , char operator){
        int i = 0;
        // Loop continues as long as the operator is found and the index is within bounds
        while (contains(items,operator) && i<items.size()){
            if (items.get(i).equals(operator)){
                // Assumes format is always [number1, operator, number2] at this stage
                Double number1 = (Double)items.get(i-1);
                Double number2 = (Double)items.get(i+1);
                // Perform the operation
                switch(operator) {
                    case '^':
                        items.set(i-1, exponent(number1,number2));
                        break;
                    case '*':
                        items.set(i-1, number1*number2);
                        break;
                    case '/':
                        items.set(i-1, number1/number2);
                        break;
                    case '+':
                        items.set(i-1, number1+number2);
                        break;
                    case '-':
                        items.set(i-1, number1-number2);
                        break;
                }
                // Remove the operator and the second operand
                items.remove(i);
                items.remove(i);

                i = 0; // Reset index to re-scan from the beginning to catch operations that might become adjacent
                continue; // Continue to the next iteration of the loop
            }
            i++;
        }
        return items;
    }
    /**
     * Calculates the square root (√) of the operand that immediately follows it in the token list.
     * It uses the Babylonian method for approximation. The token list is modified in-place.
     * @param items The list of numbers and operators, which may contain the '√' character.
     */
    private void squareRoot(ArrayList<Object> items) {
        int i = 0;
        // Loop continues as long as the square root operator is found and the index is within bounds
        while (contains(items,'√') && i<items.size()){
            if (items.get(i).equals('√')){

                Double number = (Double)items.get(i+1);
                // Initial guess for the square root
                Double initial = (number+1)/10;
                Double sqrtVal;

                // Babylonian method iteration
                while(true) {
                    // New approximation: (initial + (number / initial)) / 2
                    sqrtVal = (initial+(number/initial))/2;
                    // Check for convergence
                    if (initial.equals(sqrtVal)) {
                        break;
                    }
                    initial = sqrtVal;
                }
                // Perform the operation
                // Remove the operator and the second operand
                items.remove(i);
                items.set(i,sqrtVal);
                i = 0; // Reset index to re-scan from the beginning to catch operations that might become adjacent
                continue; // Continue to the next iteration of the loop
            }
            i++;
        }
    }
    /**
     * Calculates the result of an expression stored in a list of tokens, respecting the order of operations (PEMDAS/BODMAS).
     * Operations are executed in the order: Exponentiation (^), Square Root (√), Multiplication/Division (*, /), Addition/Subtraction (+, -).
     * @param items The list of numbers (Doubles) and operators (Characters).
     * @return The final result of the calculation as a Double.
     */
    private Double calculation(ArrayList<Object> items){
        // Order of Operations:
        operation(items,'^');
        squareRoot(items); // Square root is performed after exponentiation
        operation(items,'*');
        operation(items,'/');
        operation(items,'+');
        operation(items,'-');

        // After all operations, only the final result should remain at index 0
        return (Double)items.get(0);
    }
    /**
     * Splits a mathematical expression string into a list of numbers (Doubles) and operators (Characters).
     * It handles multi-digit numbers and decimal points.
     * @param string The input expression string.
     * @return An ArrayList of Objects, containing Doubles for numbers and Characters for operators.
     */
    private ArrayList<Object> split(String string){
        ArrayList<Object> items = new ArrayList<Object>();
        ArrayList<String> number = new ArrayList<String>();

        for (char c : string.toCharArray()) {
            // If the character is a digit or a decimal point, append it to the current number string
            if(isDigit(c) || c=='.'){
                number.add(""+c);
            }
            else{
                // If an operator is encountered, finalize the current number (if any) and add it to items
                if (number.size()>0){
                    items.add(Double.parseDouble(String.join("",number)));
                }
                // Add the operator
                items.add(c);
                // Start a new number accumulator
                number = new ArrayList<String>();
            }
        }
        // Add the last number if the expression didn't end with an operator
        if (number.size()>0){
            items.add(Double.parseDouble(String.join("",number)));
        }
        return items;
    }
    /**
     * Finds and evaluates expressions within the first set of matching parentheses in the list until all are resolved.
     * It relies on finding the first '(' and the first ')' after it.
     * The evaluation within the parentheses is recursive by calling the main calculation logic.
     * @param items The list of numbers and operators, which may include parentheses.
     */
    private void calculationInParentheses(ArrayList<Object> items){
        // Continues until no more opening and closing parentheses are found
        while (contains(items,'(')&&contains(items,')')){
            // Extract the sublist between the first '(' and the first ')'
            ArrayList<Object> inParentheses = new ArrayList<Object>(items.subList(items.indexOf('(')+1,items.indexOf(')')));
            int size = inParentheses.size();
            int start = items.indexOf('(');

            // Remove the content of the parentheses (including '(' and ')') from the original list
            for (int j=0 ; j<=size; j++){
                items.remove(start+1);
            }

            // Calculate the result of the sub-expression and replace the original '(' with the result
            items.set(start, calculation(inParentheses));
        }
    }
    /**
     * Main calculation function. It retrieves the expression from the screen, processes it by:
     * 1. Splitting into tokens (numbers and operators).
     * 2. Evaluating expressions in parentheses.
     * 3. Calculating the final result following the order of operations.
     * Finally, it displays the result (casted to Long if an integer) or an "ERROR!" with a Toast notification.
     */
    private void calculate(){
        try {
            // 1. Tokenize the input string
            ArrayList<Object> items = split(screen.getText().toString());
            // 2. Resolve parentheses
            calculationInParentheses(items);
            // 3. Perform final calculation
            calculation(items);
            // 4. Display the final result
            screen.setText(""+cast((Double) items.get(0)));
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            // Show a short "ERROR!" notification to the user
            Toast.makeText(this,"ERROR!",Toast.LENGTH_SHORT).show();
            // Display "ERROR!" on the calculator screen
            screen.setText("ERROR!");
            return;
        }
    }
    /**
     * Appends the text of the clicked button (choice) to the text field (screen).
     * This is the general handler for most calculator buttons.
     * @param view The button that was clicked.
     */
    private void onclick(View view) {
        Button button = (Button) view;
        String text = screen.getText().toString();
        screen.setText(text+button.getText().toString());
    }

    /**
     * Clears the text field (screen).
     */
    private void clear() {
        screen.setText("");
    }
    /**
     * Removes the last character from the text field (screen).
     */
    private void remove() {
        if(screen.getText().length()==0) {
            return;
        }
        // Array to hold characters as strings
        String[] elements = new String[screen.getText().length()];
        int trucker = 0;

        // Populate the array with individual characters
        for(char c : screen.getText().toString().toCharArray()) {
            elements[trucker] = ""+c;
            trucker++;
        }
        // Rebuild the string using all elements except the last one
        screen.setText(String.join("", Arrays.copyOf(elements, elements.length-1)));
    }
    /**
     * Toggles between inserting an opening "(" and a closing ")" parenthesis and updates the state.
     * @return A "(" if no parenthesis is currently open, or a ")" if one is.
     */
    private String parantheses() {
        if(!parenthesesOpen) {
            parenthesesOpen = true;
            return "(";
        }
        else {
            parenthesesOpen = false;
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

        // Handle system bars and screen insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the screen EditText
        screen = findViewById(R.id.screenEditTextText);

        // --- Set up numerical button click listeners (allow appending of digits) ---
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

        findViewById(R.id.buttonPoint).setOnClickListener(v -> {
            // Allows '.' only if the screen is not empty and a decimal point is not already present
            if (screen.getText().toString().length()>0 && !screen.getText().toString().contains(".")){
                onclick(v);
            }
        });
        // --- Set up basic operator button click listeners (prevent starting an expression with an operator) ---
        findViewById(R.id.buttonAdd).setOnClickListener(v -> {
            if (screen.getText().toString().length()>0){onclick(v);}
        });
        findViewById(R.id.buttonSubtract).setOnClickListener(v -> {
            if (screen.getText().toString().length()>0){onclick(v);}
        });
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> {
            if (screen.getText().toString().length()>0){onclick(v);}
        });
        findViewById(R.id.buttonDivid).setOnClickListener(v -> {
            if (screen.getText().toString().length()>0){onclick(v);}
        });
        findViewById(R.id.buttonExponent).setOnClickListener(v -> {
            if (screen.getText().toString().length()>0){onclick(v);}
        });

        // --- Set up special function button click listeners ---
        findViewById(R.id.buttonSquareRoot).setOnClickListener(v -> {onclick(v);}); // Appends '√'

        findViewById(R.id.buttonPy).setOnClickListener(v -> {
            // Appends Pi (22/7) only if a decimal point is not currently in the screen (simple check)
            if (!screen.getText().toString().contains(".")) {
                String text = screen.getText().toString();
                // Appends an approximation of Pi (22/7)
                screen.setText(text + ((double) 22 / 7));
            }
        });
        findViewById(R.id.buttonEulir).setOnClickListener(v -> {
            // Appends Euler's number (19/7) only if a decimal point is not currently in the screen (simple check)
            if (!screen.getText().toString().contains(".")) {
                String text = screen.getText().toString();
                // Appends an approximation of Euler's number 'e' (19/7)
                screen.setText(text+((double)19/7));
            }
        });
        findViewById(R.id.buttonParenth).setOnClickListener(v -> {
            String text = screen.getText().toString();
            // Appends the appropriate parenthesis based on the current state
            screen.setText(text+parantheses());
        });

        // --- Set up action button click listeners ---
        findViewById(R.id.buttonEquals).setOnClickListener(v -> {calculate();});
        findViewById(R.id.buttonDelete).setOnClickListener(v -> {remove();});
        findViewById(R.id.buttonClear).setOnClickListener(v -> {clear();});

    }
}