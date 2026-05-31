package com.biltuthyrning.gui;

import com.biltuthyrning.model.User;
import com.biltuthyrning.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginGUI {

    private static final String PRIMARY  = "#1C4E80";
    private static final String BG       = "#EEF2F7";
    private static final String CARD     = "#FFFFFF";
    private static final String BORDER   = "#D5DCE8";
    private static final String TEXT     = "#2C3E50";
    private static final String MUTED    = "#7F8C8D";

    private static final String FIELD_STYLE =
        "-fx-padding: 8 10; -fx-border-color: " + BORDER + "; " +
        "-fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 13;";
    private static final String BTN_PRIMARY =
        "-fx-background-color: #1C4E80; -fx-text-fill: white; -fx-font-weight: bold; " +
        "-fx-font-size: 13; -fx-padding: 9 0; -fx-background-radius: 5; -fx-cursor: hand;";
    private static final String BTN_SECONDARY =
        "-fx-background-color: #ECF0F1; -fx-text-fill: " + TEXT + "; -fx-font-weight: bold; " +
        "-fx-font-size: 13; -fx-padding: 9 0; -fx-background-radius: 5; -fx-cursor: hand;";

    private final UserService userService;
    private User loggedInUser = null;

    public LoginGUI(UserService userService) {
        this.userService = userService;
    }

    public User showAndWait() {
        Stage stage = new Stage();
        stage.setTitle("Logga in – BilUthyrning");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: " + BG + ";");
        root.setTop(buildBanner());
        root.setCenter(buildTabPane(stage));

        stage.setScene(new Scene(root, 390, 340));
        stage.showAndWait();
        return loggedInUser;
    }

    // ── Top banner ────────────────────────────────────────────────────────────

    private HBox buildBanner() {
        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER);
        banner.setStyle("-fx-background-color: " + PRIMARY + "; -fx-padding: 18 24;");

        Label icon  = styled("🚗", "26", "white", true);
        Label title = styled("BilUthyrning", "18", "white", true);

        banner.getChildren().addAll(icon, title);
        return banner;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private TabPane buildTabPane(Stage stage) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: " + BG + ";");

        Tab loginTab    = new Tab("  Logga in  ",   buildLoginPane(stage));
        Tab registerTab = new Tab("  Registrera  ", buildRegisterPane());

        tabs.getTabs().addAll(loginTab, registerTab);
        return tabs;
    }

    // ── Login pane ────────────────────────────────────────────────────────────

    private VBox buildLoginPane(Stage stage) {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(22, 28, 22, 28));
        pane.setStyle("-fx-background-color: " + CARD + ";");

        TextField     usernameField = field("Användarnamn");
        PasswordField passwordField = passField("Lösenord");

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12;");

        Button loginBtn = new Button("Logga in");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(BTN_PRIMARY);
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText();
            if (user.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("Fyll i alla fält.");
                return;
            }
            Optional<User> result = userService.login(user, pass);
            if (result.isPresent()) {
                loggedInUser = result.get();
                stage.close();
            } else {
                errorLbl.setText("Felaktigt användarnamn eller lösenord.");
                passwordField.clear();
            }
        });

        pane.getChildren().addAll(
            fieldRow("Användarnamn", usernameField),
            fieldRow("Lösenord",     passwordField),
            loginBtn,
            errorLbl
        );
        return pane;
    }

    // ── Register pane ─────────────────────────────────────────────────────────

    private VBox buildRegisterPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(22, 28, 22, 28));
        pane.setStyle("-fx-background-color: " + CARD + ";");

        TextField     usernameField = field("Välj användarnamn");
        PasswordField passwordField = passField("Välj lösenord");
        PasswordField confirmField  = passField("Bekräfta lösenord");

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-size: 12;");

        Button registerBtn = new Button("Skapa konto");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(BTN_SECONDARY);
        registerBtn.setOnAction(e -> {
            String user    = usernameField.getText().trim();
            String pass    = passwordField.getText();
            String confirm = confirmField.getText();
            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                statusLbl.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12;");
                statusLbl.setText("Fyll i alla fält.");
                return;
            }
            if (!pass.equals(confirm)) {
                statusLbl.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12;");
                statusLbl.setText("Lösenorden matchar inte.");
                return;
            }
            try {
                userService.register(user, pass, "USER");
                statusLbl.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12;");
                statusLbl.setText("Konto skapat! Logga in via fliken ovan.");
                usernameField.clear();
                passwordField.clear();
                confirmField.clear();
            } catch (Exception ex) {
                statusLbl.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12;");
                statusLbl.setText(ex.getMessage());
            }
        });

        pane.getChildren().addAll(
            fieldRow("Användarnamn", usernameField),
            fieldRow("Lösenord",     passwordField),
            fieldRow("Bekräfta",     confirmField),
            registerBtn,
            statusLbl
        );
        return pane;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VBox fieldRow(String labelText, Control input) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 11; -fx-text-fill: " + MUTED + ";");
        box.getChildren().addAll(lbl, input);
        return box;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(FIELD_STYLE);
        return f;
    }

    private PasswordField passField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(FIELD_STYLE);
        return f;
    }

    private Label styled(String text, String size, String color, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: " + size + "; -fx-text-fill: " + color + ";" +
            (bold ? " -fx-font-weight: bold;" : ""));
        return l;
    }
}
