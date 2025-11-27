package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

import InventaireArticle.ArticleEpicerie;
import InventaireArticle.ArticlePerissable;
import InventaireArticle.Inventaire;

public class InventoryViewController implements Initializable {

    @FXML private TableView<ArticleEpicerie> inventoryTable;
    @FXML private TableColumn<ArticleEpicerie, String> idColumn;
    @FXML private TableColumn<ArticleEpicerie, String> nameColumn;
    @FXML private TableColumn<ArticleEpicerie, String> categoryColumn;
    @FXML private TableColumn<ArticleEpicerie, Double> priceColumn;
    @FXML private TableColumn<ArticleEpicerie, Integer> quantityColumn;
    @FXML private TableColumn<ArticleEpicerie, String> statusColumn;
    @FXML private TableColumn<ArticleEpicerie, String> expiryColumn;
    @FXML private TableColumn<ArticleEpicerie, String> typeColumn;

    @FXML private Label summaryLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> stockFilter;
    @FXML private Button applyFiltersButton;
    @FXML private Button clearFiltersButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private Inventaire inventaire;
    private ObservableList<ArticleEpicerie> allArticles;
    private ObservableList<ArticleEpicerie> filteredArticles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("InventoryViewController initialized!"); // Debug line
        setupTableColumns();
        setupFilters();
        initializeComboBoxes();
        
        // If inventory was set before initialize, load data
        if (inventaire != null) {
            loadInventoryData();
        }
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
        System.out.println("Inventaire set with " + (inventaire != null ? inventaire.getArticles().size() : 0) + " articles"); // Debug
        
        // If UI is already initialized, load data immediately
        if (inventoryTable != null) {
            loadInventoryData();
        }
    }

    private void initializeComboBoxes() {
        // Initialize stock filter options
        ObservableList<String> stockOptions = FXCollections.observableArrayList(
            "Tous les stocks", "En stock", "Stock faible", "Rupture de stock"
        );
        stockFilter.setItems(stockOptions);
        stockFilter.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        priceColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f€", price));
                }
            }
        });

        quantityColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(quantity.toString());
                    // Color coding
                    if (quantity == 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (quantity <= 10) {
                        setStyle("-fx-text-fill: #ecc94b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Status column
        statusColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    ArticleEpicerie article = getTableView().getItems().get(getIndex());
                    if (article != null) {
                        int stock = article.getQuantiteStock();
                        if (stock == 0) {
                            setText("RUPTURE");
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else if (stock <= 10) {
                            setText("FAIBLE");
                            setStyle("-fx-text-fill: #ecc94b; -fx-font-weight: bold;");
                        } else {
                            setText("OK");
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });

        // Type column
        typeColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    ArticleEpicerie article = getTableView().getItems().get(getIndex());
                    if (article != null) {
                        setText(article instanceof ArticlePerissable ? "Périssable" : "Non périssable");
                    }
                }
            }
        });

        // Expiry column
        expiryColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    ArticleEpicerie article = getTableView().getItems().get(getIndex());
                    if (article instanceof ArticlePerissable) {
                        ArticlePerissable perissable = (ArticlePerissable) article;
                        setText(perissable.getDateExpiration().toString());
                        
                        if (perissable.estPerime()) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else if (perissable.getJoursRestants() <= 3) {
                            setStyle("-fx-text-fill: #ecc94b; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #27ae60;");
                        }
                    } else {
                        setText("N/A");
                        setStyle("-fx-text-fill: #718096;");
                    }
                }
            }
        });
    }

    public void setupFilters() {
        applyFiltersButton.setOnAction(e -> applyFilters());
        clearFiltersButton.setOnAction(e -> clearFilters());
    }

    public void loadInventoryData() {
        if (inventaire == null) {
            System.out.println("Inventaire is null!"); // Debug
            return;
        }

        System.out.println("Loading inventory data..."); // Debug
        System.out.println("Number of articles: " + inventaire.getArticles().size()); // Debug

        // Load all articles
        allArticles = FXCollections.observableArrayList(inventaire.getArticles().values());
        filteredArticles = FXCollections.observableArrayList(allArticles);
        
        inventoryTable.setItems(filteredArticles);
        
        // Populate category filter
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("Toutes les catégories");
        for (ArticleEpicerie article : allArticles) {
            String category = article.getCategorie();
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
        categoryFilter.setItems(categories);
        categoryFilter.getSelectionModel().selectFirst();
        
        updateStatistics();
        updateSummary();
        
        System.out.println("Inventory data loaded successfully!"); // Debug
    }

    public void applyFilters() {
        if (allArticles == null) return;

        filteredArticles.clear();
        
        String selectedCategory = categoryFilter.getValue();
        String selectedStock = stockFilter.getValue();

        for (ArticleEpicerie article : allArticles) {
            boolean categoryMatch = selectedCategory == null || 
                                  selectedCategory.equals("Toutes les catégories") || 
                                  article.getCategorie().equals(selectedCategory);
            
            boolean stockMatch = true;
            if (selectedStock != null) {
                switch (selectedStock) {
                    case "En stock":
                        stockMatch = article.getQuantiteStock() > 10;
                        break;
                    case "Stock faible":
                        stockMatch = article.getQuantiteStock() > 0 && article.getQuantiteStock() <= 10;
                        break;
                    case "Rupture de stock":
                        stockMatch = article.getQuantiteStock() == 0;
                        break;
                }
            }

            if (categoryMatch && stockMatch) {
                filteredArticles.add(article);
            }
        }
        
        updateStatistics();
        updateSummary();
    }

    public void clearFilters() {
        categoryFilter.getSelectionModel().selectFirst();
        stockFilter.getSelectionModel().selectFirst();
        applyFilters();
    }

    public void updateStatistics() {
        if (filteredArticles == null) return;

        int totalItems = filteredArticles.size();
        double totalValue = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;

        for (ArticleEpicerie article : filteredArticles) {
            totalValue += article.getPrix() * article.getQuantiteStock();
            if (article.getQuantiteStock() == 0) {
                outOfStockCount++;
            } else if (article.getQuantiteStock() <= 10) {
                lowStockCount++;
            }
        }

        totalItemsLabel.setText(String.valueOf(totalItems));
        totalValueLabel.setText(String.format("%.2f€", totalValue));
        lowStockLabel.setText(String.valueOf(lowStockCount));
        outOfStockLabel.setText(String.valueOf(outOfStockCount));
    }

    public void updateSummary() {
        if (filteredArticles == null) return;
        
        int total = filteredArticles.size();
        summaryLabel.setText(total + " article(s) trouvé(s)");
    }

    @FXML
    public void handleExportButton() {
        showInformation("Export", "Fonctionnalité d'export à implémenter");
    }

    @FXML
    public void handleRefreshButton() {
        loadInventoryData();
        showSuccess("Inventaire actualisé");
    }

    @FXML
    public void handleBackButton() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}