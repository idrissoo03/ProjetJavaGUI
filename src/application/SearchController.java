package application;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import java.util.List;

import InventaireArticle.ArticleEpicerie;
import InventaireArticle.Inventaire;

public class SearchController {

    @FXML
    private RadioButton searchByNameRadio;

    @FXML
    private RadioButton searchByCategoryRadio;

    @FXML
    private TextField searchInputField;

    @FXML
    private TableView<?> resultsTable;

    @FXML
    private Button searchExecuteButton;

    @FXML
    private Button newSearchButton;

    @FXML
    private Button backButton;

    private ToggleGroup searchTypeGroup;
    private Inventaire inventaire;

    @FXML
    private void initialize() {
        searchTypeGroup = new ToggleGroup();
        searchByNameRadio.setToggleGroup(searchTypeGroup);
        searchByCategoryRadio.setToggleGroup(searchTypeGroup);
        searchByNameRadio.setSelected(true);
    }

    @FXML
    private void handleSearchExecute() {
        // Your exact search logic from main
        String searchTerm = searchInputField.getText();
        
        if (searchTerm.isEmpty()) {
            showError("Veuillez entrer un terme de recherche!");
            return;
        }

        List<ArticleEpicerie> resultats;

        if (searchByNameRadio.isSelected()) {
            resultats = inventaire.rechercherParNom(searchTerm);
        } else {
            resultats = inventaire.rechercherParCategorie(searchTerm);
        }

        if (resultats.isEmpty()) {
            showInformation("Recherche", "Aucun produit trouvé");
        } else {
            StringBuilder results = new StringBuilder("=== RÉSULTATS DE LA RECHERCHE ===\n");
            for (ArticleEpicerie article : resultats) {
                results.append(article.toString()).append("\n");
            }
            showInformation("Résultats de recherche", results.toString());
        }
    }

    @FXML
    private void handleNewSearch() {
        searchInputField.clear();
        searchByNameRadio.setSelected(true);
    }

    @FXML
    private void handleBackButton() {
        closeWindow();
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

    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
    }
}