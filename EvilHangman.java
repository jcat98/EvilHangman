package hangman;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.lang.Character;
import java.util.Set;

public class EvilHangman {

    public static void main(String[] args) {
        String dictionaryFileName = args[0];
        File file = new File(dictionaryFileName);
        int wordLength = Integer.parseInt(args[1]);
        int guessCount = Integer.parseInt(args[2]);

        EvilHangmanGame hangman = new EvilHangmanGame();
        try {
            hangman.startGame(file, wordLength);
        }
        catch (EmptyDictionaryException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        EvilHangman.play(hangman, guessCount);
    }

    public static void play(EvilHangmanGame hangman, int guessCount) {
        int guesses = guessCount;
        Set<String> words = new HashSet<>();
        while(guesses > 0 ) {
            EvilHangman.display(hangman, guesses);

            System.out.println("Enter guess: ");
            String input = new Scanner(System.in).nextLine();

            Boolean valid = false;
            while(!valid) {
                if(input.isEmpty()) {
                    System.out.print("Invalid input! Enter guess: ");
                    input = new Scanner(System.in).nextLine();
                }
                else {
                    valid = true;
                }
            }

            char c = input.charAt(0);
            valid = false;
            while(!valid) {
                if(!Character.isLetter(c)) {
                    System.out.print("Invalid input! Enter guess: ");
                    c = new Scanner(System.in).nextLine().charAt(0);
                }
                else {
                    valid = true;
                    try {
                        words = hangman.makeGuess(c);
                        if(EvilHangman.howManyGuessInWord(words, c)) {
                            guesses++;
                        };
                    }
                    catch (GuessAlreadyMadeException ex){
                        System.out.println("Guess already made! Enter guess: ");
                        c = new Scanner(System.in).nextLine().charAt(0);
                        valid = false;
                    }
                }

            }
            if(EvilHangman.endOfGame(hangman, words)) {
                System.out.println("You win! You guessed the word: " + hangman.getPattern());
                guesses = 0;
            }
            System.out.println("");
            guesses--;
        }

        if(!EvilHangman.endOfGame(hangman, words)) {
            int size = words.size();
            int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
            int i = 0;
            String randWord = "";
            for(String word : words)
            {
                if (i == item)
                    randWord = word;
                i++;
            }
            System.out.println("Sorry, you lost! The word was: " + randWord);
        }
    }

    public static void display(EvilHangmanGame hangman, int guesses) {
        System.out.format("You have %d guesses left \n", guesses);
        System.out.println("Used letters: "+ hangman.getGuessedLetters().toString());
        System.out.println("Word: " + hangman.getPattern());
    }

    public static boolean howManyGuessInWord(Set<String> words, char guess) {
        int numOfGuessesInWord = 0;
        String randWord = "";
        for(String word : words) {
            randWord = word;
        }

        for(int i = 0; i < randWord.length(); i++) {
            if(randWord.charAt(i) == guess) {
                numOfGuessesInWord++;
            }
        }

        if(numOfGuessesInWord == 0) {
            System.out.println("Sorry, there are no " + guess + "'s");
            return false;
        }
        else if(numOfGuessesInWord == 1) {
            System.out.println("Yes, there is one " + guess);
            return true;
        }
        else {
            System.out.println("Yes, there are are " + numOfGuessesInWord + " " + guess + "'s");
            return true;
        }
    }

    public static boolean endOfGame(EvilHangmanGame hangman, Set<String> words) {
        String pattern = hangman.getPattern();
        for(int i = 0; i < pattern.length(); i++) {
            if(pattern.charAt(i) == '-') {
                //System.out.println("Sorry, you lost! The word was: " + words);
                return false;
            }
        }
        //System.out.println("You win! You guessed the word: " + words);
        return true;
    }
}

