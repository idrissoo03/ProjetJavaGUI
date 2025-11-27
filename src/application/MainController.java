package application;

import InventaireArticle.ArticleNonPerissable;
import InventaireArticle.ArticlePerissable;
import InventaireArticle.Caisse;
import InventaireArticle.Inventaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button adminButton;

    @FXML
    private Button quitButton;

    private Inventaire inventaire = new Inventaire();
    private Caisse caisse = new Caisse(500.0);

    @FXML
    private void initialize() {
        initialiserDonnees();
    }

    @FXML
    private void handleAdminButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/admin-auth.fxml"));
            Parent root = loader.load();
            
            // Pass this MainController instance to the AdminAuthController
            AdminAuthController authController = loader.getController();
            authController.setMainController(this);
            
            Stage stage = (Stage) adminButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Administrateur - FreshMarket Pro");
        } catch (Exception e) {
            showError("Erreur de navigation: " + e.getMessage());
        }
    }
    @FXML
    private void handleQuitButton() {
        System.out.println("\n   Merci d'avoir utilisé Notre System!");
        Stage stage = (Stage) quitButton.getScene().getWindow();
        stage.close();
    }

    private void initialiserDonnees() {
        ArticlePerissable lait = new ArticlePerissable(
            "A001", "Lait demi-écrémé 1L", 1.50, 5, "Produits laitiers", 
            java.time.LocalDate.now().plusDays(5)
        );
        
        ArticlePerissable pain = new ArticlePerissable(
            "A002", "Pain complet", 1.20, 30, "Boulangerie", 
            java.time.LocalDate.now().plusDays(2)
        );
        
        ArticleNonPerissable pates = new ArticleNonPerissable(
            "A003", "Pâtes Spaghetti 500g", 2.50, 100, "Épicerie salée", 365
        );
        
        ArticleNonPerissable riz = new ArticleNonPerissable(
            "A004", "Riz Basmati 1kg", 3.00, 80, "Épicerie salée", 730
        );
        
        ArticlePerissable fromage = new ArticlePerissable(
            "A005", "Fromage Camembert", 4.50, 25, "Produits laitiers", 
            java.time.LocalDate.now().plusDays(10)
        );
        
        ArticlePerissable yaourt = new ArticlePerissable(
            "A006", "Yaourt nature x4", 2.80, 40, "Produits laitiers", 
            java.time.LocalDate.now().plusDays(15)
        );
        
        ArticleNonPerissable huile = new ArticleNonPerissable(
            "A007", "Huile d'olive 1L", 8.50, 30, "Épicerie salée", 545
        );
        
        ArticlePerissable tomates = new ArticlePerissable(
            "A008", "Tomates fraîches 1kg", 3.20, 50, "Fruits et légumes", 
            java.time.LocalDate.now().plusDays(4)
        );
        
        inventaire.ajouterArticle(lait);
        inventaire.ajouterArticle(pain);
        inventaire.ajouterArticle(pates);
        inventaire.ajouterArticle(riz);
        inventaire.ajouterArticle(fromage);
        inventaire.ajouterArticle(yaourt);
        inventaire.ajouterArticle(huile);
        inventaire.ajouterArticle(tomates);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Inventaire getInventaire() {
        return inventaire;
    }

    public Caisse getCaisse() {
        return caisse;
    }
}