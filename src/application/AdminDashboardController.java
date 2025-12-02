package application;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.Map;

import InventaireArticle.ArticleEpicerie;
import InventaireArticle.ArticlePerissable;
import InventaireArticle.Caisse;
import InventaireArticle.Inventaire;
import InventaireArticle.Vente;
import UtilisateurApplication.Administrateur;

public class AdminDashboardController {

    @FXML
    private Label adminWelcomeLabel;

    @FXML
    private Button viewInventoryButton;

    @FXML
    private Button manageSaleButton;

    @FXML
    private Button addArticleButton;

    @FXML
    private Button editArticleButton;

    @FXML
    private Button deleteArticleButton;

    @FXML
    private Button searchArticleButton;

    @FXML
    private Button generateReportButton;

    @FXML
    private Button viewSalesButton;

    @FXML
    private Button logoutButton;

    private Administrateur admin;
    private Inventaire inventaire;
    private Caisse caisse;
    private Map<String, Administrateur> admins;

    @FXML
    private void initialize() {
        updateWelcomeLabel();
    }

    @FXML
    private void handleViewInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/inventory-view.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the inventory data
            InventoryViewController inventoryController = loader.getController();
            inventoryController.setInventaire(inventaire); // Pass your inventory object
            
            // Create a NEW window instead of replacing the current one
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventaire Complet - FreshMarket Pro");
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
            
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace(); // This will show you the exact error
        }
    }

    @FXML
    private void handleManageSale() {
        openSalesManagement();
    }

    @FXML
    private void handleAddArticle() {
        openArticleForm("add");
    }

    @FXML
    private void handleEditArticle() {
        openArticleForm("edit");
    }

    @FXML
    private void handleDeleteArticle() {
        openArticleForm("delete");
    }

    @FXML
    private void handleSearchArticle() {
        openSearchInterface();
    }

    @FXML
    private void handleGenerateReport() {
        openReportsInterface();
    }

    private void openReportsInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/reports-interface.fxml"));
            Parent root = loader.load();
            ReportsController controller = loader.getController();
            controller.setInventaire(inventaire);
            controller.setCaisse(caisse);
            controller.setAdmin(admin); 
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("📊 Rapports et Analytiques - FreshMarket Pro");
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture des rapports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewSales() {
        openSalesView();
    }
    private void openSalesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/sales-view.fxml"));
            Parent root = loader.load();
            SalesViewController controller = loader.getController();
            controller.setCaisse(caisse);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("💰 Ventes du Jour - FreshMarket Pro");
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture des ventes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        admin.deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/admin-auth.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showError("Erreur de déconnexion: " + e.getMessage());
        }
    }

    private void updateWelcomeLabel() {
        if (admin != null) {
            adminWelcomeLabel.setText("MENU ADMINISTRATEUR - " + admin.getNom());
        }
    }

    private void openSalesManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/sales-management.fxml"));
            Parent root = loader.load();
            SalesController controller = loader.getController();
            controller.setInventaire(inventaire);
            controller.setCaisse(caisse);
            controller.setAdmin(admin);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des ventes");
            stage.show();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void openArticleForm(String mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/article-form.fxml"));
            Parent root = loader.load();
            ArticleFormController controller = loader.getController();
            controller.setMode(mode);
            controller.setInventaire(inventaire);
            controller.setAdmin(admin);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(mode.equals("add") ? "Ajouter un article" : 
                          mode.equals("edit") ? "Modifier un article" : "Supprimer un article");
            stage.show();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void openSearchInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/search-interface.fxml"));
            Parent root = loader.load();
            SearchController controller = loader.getController();
            controller.setInventaire(inventaire);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Recherche de produits");
            stage.show();
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

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setAdmin(Administrateur admin) {
        this.admin = admin;
        updateWelcomeLabel();
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
    }

    public void setCaisse(Caisse caisse) {
        this.caisse = caisse;
    }

    public void setAdmins(Map<String, Administrateur> admins) {
        this.admins = admins;
    }
}