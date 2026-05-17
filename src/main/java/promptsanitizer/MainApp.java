package promptsanitizer;

//compile with javac -cp json-20250107.jar MainSanitizerApp.java
//run with java -cp json-20250107.jar:. MainSanitizerApp
//json-20250107.jar comes from https://repo1.maven.org/maven2/org/json/json/20250107/
public class MainApp {

    public static void main(String[] args) {
        new Sanitizer(
                System.getProperty("user.home") +
                        System.getProperty("file.separator") +
                        "personal_dictionary.json"
        ).createUI();
    }
}
