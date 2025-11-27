package application;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;

import InventaireArticle.ArticleEpicerie;
import InventaireArticle.ArticleNonPerissable;
import InventaireArticle.ArticlePerissable;
import InventaireArticle.Inventaire;
import UtilisateurApplication.Administrateur;

public class ArticleFormController {

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField categoryField;

    @FXML
    private DatePicker expiryDateField;

    @FXML
    private TextField shelfLifeField;

    @FXML
    private RadioButton perishableRadio;

    @FXML
    private RadioButton nonPerishableRadio;

    @FXML
    private VBox perishableSection;

    @FXML
    private VBox nonPerishableSection;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private ToggleGroup articleTypeGroup;
    private String mode;
    private Inventaire inventaire;
    private Administrateur admin;

    @FXML
    private void initialize() {
        articleTypeGroup = new ToggleGroup();
        perishableRadio.setToggleGroup(articleTypeGroup);
        nonPerishableRadio.setToggleGroup(articleTypeGroup);
        perishableRadio.setSelected(true);

        // Add listeners for radio buttons
        perishableRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            perishableSection.setVisible(newVal);
            nonPerishableSection.setVisible(!newVal);
        });

        nonPerishableRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            nonPerishableSection.setVisible(newVal);
            perishableSection.setVisible(!newVal);
        });
    }

    @FXML
    private void handleSaveButton() {
        if (mode.equals("add")) {
            addArticle();
        } else if (mode.equals("edit")) {
            editArticle();
        } else if (mode.equals("delete")) {
            deleteArticle();
        }
    }

    @FXML
    private void handleCancelButton() {
        closeWindow();
    }

    private void addArticle() {
        // Your exact logic from main
        try {
            String id = idField.getText();
            String nom = nameField.getText();
            double prix = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(quantityField.getText());
            String categorie = categoryField.getText();

            ArticleEpicerie article = null;

            if (perishableRadio.isSelected()) {
                LocalDate dateExpiration = expiryDateField.getValue();
                if (dateExpiration == null) {
                    showError("Veuillez sélectionner une date d'expiration!");
                    return;
                }
                article = new ArticlePerissable(id, nom, prix, stock, categorie, dateExpiration);
            } else {
                int dureeConservation = Integer.parseInt(shelfLifeField.getText());
                article = new ArticleNonPerissable(id, nom, prix, stock, categorie, dureeConservation);
            }

            if (article != null) {
                admin.ajouterArticle(inventaire, article);
                showSuccess("Article ajouté avec succès!");
                closeWindow();
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void editArticle() {
        // Your exact logic from main
        String id = idField.getText();
        ArticleEpicerie article = inventaire.getArticle(id);

        if (article == null) {
            showError("Article non trouvé!");
            return;
        }

        try {
            String nom = nameField.getText().isEmpty() ? article.getNom() : nameField.getText();
            double prix = priceField.getText().isEmpty() ? article.getPrix() : Double.parseDouble(priceField.getText());
            int stock = quantityField.getText().isEmpty() ? article.getQuantiteStock() : Integer.parseInt(quantityField.getText());
            String categorie = categoryField.getText().isEmpty() ? article.getCategorie() : categoryField.getText();

            admin.modifierArticle(inventaire, id, nom, prix, stock, categorie);
            showSuccess("Article modifié avec succès!");
            closeWindow();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void deleteArticle() {
        // Your exact logic from main
        String id = idField.getText();
        ArticleEpicerie article = inventaire.getArticle(id);

        if (article == null) {
            showError("Article non trouvé!");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Confirmer la suppression de l'article: " + article.getNom() + "?");

        if (confirmation.showAndWait().get().getText().equals("OK")) {
            admin.supprimerArticle(inventaire, id);
            showSuccess("Article supprimé avec succès!");
            closeWindow();
        }
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

    public void setMode(String mode) {
        this.mode = mode;
        setupFormForMode();
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
    }

    public void setAdmin(Administrateur admin) {
        this.admin = admin;
    }

    private void setupFormForMode() {
        if (mode.equals("delete")) {
            // Only show ID field for deletion
            nameField.setDisable(true);
            priceField.setDisable(true);
            quantityField.setDisable(true);
            categoryField.setDisable(true);
            perishableRadio.setDisable(true);
            nonPerishableRadio.setDisable(true);
            expiryDateField.setDisable(true);
            shelfLifeField.setDisable(true);
            saveButton.setText("🗑️ Supprimer");
        } else if (mode.equals("edit")) {
            saveButton.setText("✏️ Modifier");
        } else {
            saveButton.setText("➕ Ajouter");
        }
    }
}