package promptsanitizer;

public class MainApp {

    public static void main(String[] args) {
        new Sanitizer(
                System.getProperty("user.home") +
                        System.getProperty("file.separator") +
                        "personal_dictionary.json"
        ).createUI();
    }
}
