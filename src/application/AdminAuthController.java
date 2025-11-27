package application;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

import UtilisateurApplication.Administrateur;

public class AdminAuthController {

    @FXML
    private TextField adminIdField;

    @FXML
    private PasswordField adminPasswordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private Button backButton;

    private Map<String, Administrateur> admins = new HashMap<>();
    private MainController mainController;

    @FXML
    private void initialize() {
        // Initialize with default admin from your main code
        Administrateur adminDefault = new Administrateur("idriss", "Admin Principal", "admin@store.com", "idriss123");
        admins.put("idriss", adminDefault);
    }

    @FXML
    private void handleLogin() {
        String id = adminIdField.getText();
        String motDePasse = adminPasswordField.getText();

        if (id.isEmpty() || motDePasse.isEmpty()) {
            showError("Veuillez remplir tous les champs!");
            return;
        }

        Administrateur admin = admins.get(id);
        
        if (admin != null && admin.getMotDePasse().equals(motDePasse)) {
            admin.connecter();
            openAdminDashboard(admin);
        } else {
            showError("Identifiants incorrects!");
        }
    }

    @FXML
    private void handleCreateAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/create-admin.fxml"));
            Parent root = loader.load();
            CreateAdminController controller = loader.getController();
            controller.setAdmins(admins);
            controller.setAuthController(this);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Création de compte administrateur");
            stage.show();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Erreur de navigation: " + e.getMessage());
        }
    }

    private void openAdminDashboard(Administrateur admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/admin-dashboard.fxml"));
            Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setAdmin(admin);
            
            // Use the mainController field directly (not a method)
            if (mainController != null) {
                controller.setInventaire(mainController.getInventaire());
                controller.setCaisse(mainController.getCaisse());
            } else {
                showError("Erreur: Contrôleur principal non initialisé");
                return;
            }
            
            controller.setAdmins(admins);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord administrateur");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private MainController getMainController() {
        // This would be set when navigating from main menu
        return mainController;
    }

    public void addAdmin(Administrateur admin) {
        admins.put(admin.getId(), admin);
    }
}