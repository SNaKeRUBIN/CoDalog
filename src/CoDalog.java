package CoDalogFinal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;
import java.io.FileReader;

public class CoDalog {

    public static void main(String[] args) {

        long startTime = 0;

        try {
            try (Reader reader = new BufferedReader(
                    // Change this path
                    new FileReader(
                            "D:\\kbase\\small.txt"))) {
                System.out.println("Enter either 'Naive' or Seminaive': ");
                Scanner input = new Scanner(System.in);
                String userInput = input.nextLine();
                System.out.println("Are magic sets being used (Y/N)? ");
                String inputMagicSets = input.nextLine();
                startTime = System.nanoTime();
                Parser parser = new Parser(userInput, reader, inputMagicSets);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        LoggerClass.logger("Execution time: " + duration);
    }

}
