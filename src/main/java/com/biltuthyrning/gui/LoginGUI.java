package com.biltuthyrning.gui;

import com.biltuthyrning.model.User;
import com.biltuthyrning.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginGUI {

    private final UserService userService;
    private User loggedInUser = null;

    public LoginGUI(UserService userService) {
        this.userService = userService;
    }

    public User showAndWait() {
        Stage stage = new Stage();
        stage.setTitle("Logga in - BiltUthyrning");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab loginTab = new Tab("Logga in");
        loginTab.setContent(createLoginPane(stage));

        Tab registerTab = new Tab("Registrera");
        registerTab.setContent(createRegisterPane());

        tabPane.getTabs().addAll(loginTab, registerTab);

        stage.setScene(new Scene(tabPane, 360, 260));
        stage.showAndWait();

        return loggedInUser;
    }

    private VBox createLoginPane(Stage stage) {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(24));
        pane.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Användarnamn");
        usernameField.setPrefWidth(180);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Lösenord");
        passwordField.setPrefWidth(180);

        grid.add(new Label("Användarnamn:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Lösenord:"), 0, 1);
        grid.add(passwordField, 1, 1);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginBtn = new Button("Logga in");
        loginBtn.setPrefWidth(130);
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Fyll i alla fält");
                return;
            }
            Optional<User> user = userService.login(username, password);
            if (user.isPresent()) {
                loggedInUser = user.get();
                stage.close();
            } else {
                errorLabel.setText("Felaktigt användarnamn eller lösenord");
                passwordField.clear();
            }
        });

        pane.getChildren().addAll(grid, loginBtn, errorLabel);
        return pane;
    }

    private VBox createRegisterPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(24));
        pane.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Användarnamn");
        usernameField.setPrefWidth(180);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Lösenord");
        passwordField.setPrefWidth(180);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Bekräfta lösenord");
        confirmField.setPrefWidth(180);

        grid.add(new Label("Användarnamn:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Lösenord:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Bekräfta:"), 0, 2);
        grid.add(confirmField, 1, 2);

        Label statusLabel = new Label();

        Button registerBtn = new Button("Skapa konto");
        registerBtn.setPrefWidth(130);
        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Fyll i alla fält");
                return;
            }
            if (!password.equals(confirm)) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Lösenorden matchar inte");
                return;
            }
            try {
                userService.register(username, password, "USER");
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Konto skapat! Logga nu in.");
                usernameField.clear();
                passwordField.clear();
                confirmField.clear();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText(ex.getMessage());
            }
        });

        pane.getChildren().addAll(grid, registerBtn, statusLabel);
        return pane;
    }
}
