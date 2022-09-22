package hangman;

public class EmptyDictionaryException extends Exception {
    //Thrown when dictionary file is empty or no words in dictionary match the length

    public EmptyDictionaryException() {
    }

    public EmptyDictionaryException(String message) {
        super(message);
    }
}
