package application;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.Map;

import UtilisateurApplication.Administrateur;

public class CreateAdminController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    private Map<String, Administrateur> admins;
    private AdminAuthController authController;

    @FXML
    private void handleCreateButton() {
        String nom = fullNameField.getText();
        String email = emailField.getText();
        String motDePasse = passwordField.getText();
        String confirmation = confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
            showError("Veuillez remplir tous les champs!");
            return;
        }

        if (!motDePasse.equals(confirmation)) {
            showError("Les mots de passe ne correspondent pas!");
            return;
        }

        // Your exact ID generation logic from main
        String id = "A" + String.format("%04d", admins.size() + 1);
        
        Administrateur admin = new Administrateur(id, nom, email, motDePasse);
        admins.put(id, admin);

        showSuccess("Compte administrateur créé avec succès!\nVotre ID admin: " + id);
        
        if (authController != null) {
            authController.addAdmin(admin);
        }
        
        closeWindow();
    }

    @FXML
    private void handleCancelButton() {
        closeWindow();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setAdmins(Map<String, Administrateur> admins) {
        this.admins = admins;
    }

    public void setAuthController(AdminAuthController authController) {
        this.authController = authController;
    }
}