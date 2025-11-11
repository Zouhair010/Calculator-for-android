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
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText screen;
    private boolean parenthesesOpen = false;

    /**
     * Casts a Double to a Long if it has no fractional part, otherwise returns the Double.
     * @param number The Double to cast.
     * @return A Long if the number is a whole number, otherwise the original Double.
     */
    private Object cast(Double number) {
        long longNumber = number.longValue();
        double doubleNumber = (double)longNumber;
        if (doubleNumber==number){
            return longNumber;
        }
        return number;
    }
    /**
     * Checks if an object represents a single digit.
     * @param obj The object to check (can be String, Character, or Integer).
     * @return true if the object is a digit, false otherwise.
     */
    private boolean isDigit(Object obj){
        if (obj instanceof String){
            try{
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
     * Checks if an ArrayList contains a specific item.
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
    /**
     * Calculates the exponent of a number (a^b) for positive integer exponents.
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
     * Performs a specific arithmetic operation (+, -, *, /, ^) on a list of numbers and operators.
     * The list is modified in-place.
     * @param items The list of numbers (Doubles) and operators (Characters).
     * @param operator The character of the operation to perform.
     * @return The modified list after performing all occurrences of the specified operation.
     */
    private ArrayList<Object> operation(ArrayList<Object> items , char operator){
        int i = 0;
        while (contains(items,operator) && i<items.size()){
            if (items.get(i).equals(operator)){
                Double number1 = (Double)items.get(i-1);
                Double number2 = (Double)items.get(i+1);
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
                items.remove(i);
                items.remove(i);
                i = 0; // Reset index to re-scan from the beginning
                continue; // Continue to the next iteration of the loop
            }
            i++;
        }
        return items;
    }
    /**
     * Calculates the result of an expression stored in a list, respecting the order of operations.
     * @param items The list of numbers and operators.
     * @return The final result of the calculation as a Double.
     */
    private Double calculation(ArrayList<Object> items){
        operation(items,'^');
        operation(items,'*');operation(items,'/');
        operation(items,'+');operation(items,'-');
        return (Double)items.get(0);
    }
    /**
     * Splits a mathematical expression string into a list of numbers (Doubles) and operators (Characters).
     * @param string The input expression string.
     * @return An ArrayList of Objects, containing Doubles for numbers and Characters for operators.
     */
    private ArrayList<Object> split(String string){
        ArrayList<Object> items = new ArrayList<Object>();
        ArrayList<String> number = new ArrayList<String>();

        for (char c : string.toCharArray()) {
            // If the character is a digit or a decimal point, append it to the current number
            if(isDigit(c) || c=='.'){
                number.add(""+c);
            }
            else{
                if (number.size()>0){
                    items.add(Double.parseDouble(String.join("",number)));
                }
                items.add(c);
                number = new ArrayList<String>();
            }
        }
        if (number.size()>0){
            items.add(Double.parseDouble(String.join("",number)));
        }
        return items;
    }
    /**
     * Finds and evaluates expressions within parentheses in the list.
     * @param items The list of numbers and operators, which may include parentheses.
     */
    private void calculationInParentheses(ArrayList<Object> items){
        while (contains(items,'(')&&contains(items,')')){
            ArrayList<Object> inParentheses = new ArrayList<Object>(items.subList(items.indexOf('(')+1,items.indexOf(')')));
            int size = inParentheses.size();
            int start = items.indexOf('(');
            for (int j=0 ; j<=size; j++){
                items.remove(start+1);
            }
            items.set(start, calculation(inParentheses));
        }
    }
    /**
     * Main calculation function. It takes the text from the screen, processes it, and displays the result or an error.
     * @param textField The text field containing the expression to calculate.
     */
    private void calculate(){
        try {
            ArrayList<Object> items = split(screen.getText().toString());
            calculationInParentheses(items);
            calculation(items);
            screen.setText(""+cast((Double) items.get(0)));
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            screen.setText("ERROR!");
            return;
        }
    }
    /**
     * Appends the given string (choice) to the text field.
     * @param choice The string to append, usually from a button press.
     */
    private void onclick(View view) {
        Button button = (Button) view;
        String text = screen.getText().toString();
        screen.setText(text+button.getText().toString());
    }

    /**
     * Clears the text field.
     */
    private void clear() {
        screen.setText("");
    }
    /**
     * Removes the last character from the text field.
     */
    private void remove() {
        if(screen.getText().length()==0) {
            return;
        }
        String[] elements = new String[screen.getText().length()];
        int trucker = 0;
        for(char c : screen.getText().toString().toCharArray()) {
            elements[trucker] = ""+c;
            trucker++;
        }
        screen.setText(String.join("", Arrays.copyOf(elements, elements.length-1)));
    }

    /**
     * Calculates the square root of the number in the text field using the Babylonian method.
     */
    private void squareRoot() {
        try {
            String text = screen.getText().toString();
            Double initial = (Double.valueOf(text)+1)/10;
            Double sqrtVal;
            while(true) {
                sqrtVal = (initial+(Double.valueOf(text)/initial))/2;
                if (initial.equals(sqrtVal)) {
                    break;
                }
                initial = sqrtVal;
            }
            screen.setText(sqrtVal.toString());
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            screen.setText("ERROR!");
            return;
        }
    }
    /**
     * Toggles between inserting an opening and a closing parenthesis.
     * @return A "(" if no parenthesis is open, or a ")" if one is.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        screen = findViewById(R.id.screenEditTextText);

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
        findViewById(R.id.buttonPoint).setOnClickListener(v -> {onclick(v);});

        findViewById(R.id.buttonAdd).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.buttonSubtract).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.buttonDivid).setOnClickListener(v -> {onclick(v);});
        findViewById(R.id.buttonExponent).setOnClickListener(v -> {onclick(v);});

        findViewById(R.id.buttonSquareRoot).setOnClickListener(v -> {squareRoot();});
        findViewById(R.id.buttonPy).setOnClickListener(v -> {
            String text = screen.getText().toString();
            screen.setText(text+(22/7));
        });
        findViewById(R.id.buttonEulir).setOnClickListener(v -> {
            String text = screen.getText().toString();
            screen.setText(text+(19/7));
        });
        findViewById(R.id.buttonParenth).setOnClickListener(v -> {
            String text = screen.getText().toString();
            screen.setText(text+parantheses());
        });
        findViewById(R.id.buttonEquals).setOnClickListener(v -> {calculate();});
        findViewById(R.id.buttonDelete).setOnClickListener(v -> {remove();});
        findViewById(R.id.buttonClear).setOnClickListener(v -> {clear();});

    }
}