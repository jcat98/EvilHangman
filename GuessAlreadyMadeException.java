package hangman;

public class GuessAlreadyMadeException extends Exception {
    public GuessAlreadyMadeException() {
    }

    public GuessAlreadyMadeException(String message) {
        super(message);
    }
}
