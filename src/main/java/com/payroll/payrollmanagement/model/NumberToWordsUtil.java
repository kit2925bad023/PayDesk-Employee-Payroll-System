package com.payroll.payrollmanagement.model;

/**
 * SMALL HELPER CLASS: converts a number into words.
 *
 * This is only used to print the net salary in words on the salary summary card
 * (for example "Fifty Thousand Rupees Only"), which makes the salary slip look
 * like a real payslip from a company.
 */
public final class NumberToWordsUtil {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
            "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private NumberToWordsUtil() {
        // private constructor stops anyone from creating an object of this helper class
    }

    /** Main method: turns 50000 into "Fifty Thousand Rupees Only". */
    public static String convert(long number) {
        if (number <= 0) {
            return "Zero Rupees Only";
        }
        return convertNumber(number) + " Rupees Only";
    }

    /** Recursive helper that splits the number into crore / lakh / thousand / hundred. */
    private static String convertNumber(long number) {
        if (number < 20) {
            return ONES[(int) number];
        }
        if (number < 100) {
            // .trim() removes the extra space that appears when the last digit is 0
            // e.g. 50 becomes "Fifty" and not "Fifty ".
            return (TENS[(int) (number / 10)] + " " + ONES[(int) (number % 10)]).trim();
        }
        if (number < 1000) {
            return ONES[(int) (number / 100)] + " Hundred" + rest(number % 100);
        }
        if (number < 100000) {
            return convertNumber(number / 1000) + " Thousand" + rest(number % 1000);
        }
        if (number < 10000000) {
            return convertNumber(number / 100000) + " Lakh" + rest(number % 100000);
        }
        return convertNumber(number / 10000000) + " Crore" + rest(number % 10000000);
    }

    /** Adds " and " only when something is left over, so we never get double spaces. */
    private static String rest(long number) {
        if (number == 0) {
            return "";
        }
        String text = convertNumber(number);
        if (text.isEmpty()) {
            return "";
        }
        return " " + text;
    }
}
