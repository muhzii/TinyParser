package muhzi.app;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import muhzi.parser.Parser;
import muhzi.parser.SyntaxTreeNode;
import muhzi.parser.errors.ParserError;
import muhzi.parser.errors.SyntaxError;
import muhzi.parser.errors.TokenError;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {

    public TextArea codeTextArea;
    public DrawingPane drawingPane;
    public ScrollPane scrollPane;
    public MenuItem aboutBtn;
    public Label drawingPaneLabel;

    private Stage stage;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleAbout() {
        String header = "Copyright (C) 2019 Muhammed Ziad\n" +
                "<airomyst517@gmail.com>";

        String content = "This is a free software, you can redistribute " +
                "it and/or modify\n it under the terms of " +
                "the GNU General Public License.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About the software");
        alert.setHeaderText(header);
        alert.getDialogPane().setContent(new Label(content));
        alert.getDialogPane().getStyleClass().add("aboutDialog");
        alert.getDialogPane().getStylesheets()
                .add(getClass().getResource("window_styles.css").toExternalForm());
        alert.showAndWait();
    }

    public void handleParse() {
        drawingPane.clearPane(drawingPaneLabel);

        Parser parser = new Parser();
        Reader inputCode = new StringReader(codeTextArea.getText()+"\n");
        BufferedReader br = new BufferedReader(inputCode);

        try {
            SyntaxTreeNode syntaxTree = parser.parse(br);
            drawingPane.drawTree(syntaxTree, 0.1*scrollPane.getWidth(), 0.1*scrollPane.getHeight());
        } catch (ParserError | SyntaxError | TokenError e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception Dialog");
            alert.setHeaderText("Error occurred while parsing the code...");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import tiny code");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Path filePath = Paths.get(file.getPath());
                String code = new String(Files.readAllBytes(filePath));
                codeTextArea.setText(code);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleExit() {
        stage.close();
    }
}
