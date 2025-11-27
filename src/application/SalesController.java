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
import InventaireArticle.Caisse;
import InventaireArticle.Inventaire;
import InventaireArticle.LignePanier;
import InventaireArticle.Panier;
import InventaireArticle.Vente;
import UtilisateurApplication.Administrateur;

public class SalesController implements Initializable {

    // Available Products Section
    @FXML private TableView<ArticleEpicerie> availableProductsTable;
    @FXML private TableColumn<ArticleEpicerie, String> availableIdColumn;
    @FXML private TableColumn<ArticleEpicerie, String> availableNameColumn;
    @FXML private TableColumn<ArticleEpicerie, String> availableCategoryColumn;
    @FXML private TableColumn<ArticleEpicerie, Double> availablePriceColumn;
    @FXML private TableColumn<ArticleEpicerie, Integer> availableStockColumn;
    @FXML private TableColumn<ArticleEpicerie, String> availableAddColumn;

    @FXML private TextField productSearchField;
    @FXML private ComboBox<String> searchCategoryFilter;
    @FXML private Button searchProductButton;
    @FXML private Button clearSearchButton;

    // Quick Add Section
    @FXML private TextField addProductIdField;
    @FXML private TextField addQuantityField;
    @FXML private Button addToCartButton;

    // Cart Section
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartProductColumn;
    @FXML private TableColumn<CartItem, Integer> cartQuantityColumn;
    @FXML private TableColumn<CartItem, Double> cartUnitPriceColumn;
    @FXML private TableColumn<CartItem, Double> cartTotalPriceColumn;
    @FXML private TableColumn<CartItem, String> cartActionsColumn;

    // Summary Section
    @FXML private Label saleSummaryLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label cartItemsCountLabel;

    // Action Buttons
    @FXML private Button finalizeSaleButton;
    @FXML private Button modifyCartButton;
    @FXML private Button clearCartButton;
    @FXML private Button cancelSaleButton;

    private Panier panierVente = new Panier();
    private Inventaire inventaire;
    private Caisse caisse;
    private Administrateur admin;
    
    private ObservableList<ArticleEpicerie> allProducts;
    private ObservableList<ArticleEpicerie> filteredProducts;
    private ObservableList<CartItem> cartItems;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SalesController initialized!");
        setupAvailableProductsTable();
        setupCartTable();
        setupEventHandlers();
        initializeFilters();
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
        System.out.println("Inventaire set with " + (inventaire != null ? inventaire.getArticles().size() : 0) + " articles");
        loadAvailableProducts();
    }

    public void setCaisse(Caisse caisse) {
        this.caisse = caisse;
    }

    public void setAdmin(Administrateur admin) {
        this.admin = admin;
    }

    private void setupAvailableProductsTable() {
        // Set up cell value factories
        availableIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        availableNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        availableCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        availablePriceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        availableStockColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        // Format price column
        availablePriceColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, Double>() {
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

        // Format stock column with color coding
        availableStockColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(quantity.toString());
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

        // Add to cart button column
        availableAddColumn.setCellFactory(column -> new TableCell<ArticleEpicerie, String>() {
            private final Button addButton = new Button("➕");

            {
                addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                addButton.setOnAction(e -> {
                    ArticleEpicerie article = getTableView().getItems().get(getIndex());
                    if (article != null) {
                        addProductToCart(article, 1);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ArticleEpicerie article = getTableView().getItems().get(getIndex());
                    if (article != null && article.getQuantiteStock() > 0) {
                        setGraphic(addButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void setupCartTable() {
        cartProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        cartTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Format price columns
        cartUnitPriceColumn.setCellFactory(column -> new TableCell<CartItem, Double>() {
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

        cartTotalPriceColumn.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f€", price));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });

        // Actions column (remove button)
        cartActionsColumn.setCellFactory(column -> new TableCell<CartItem, String>() {
            private final Button removeButton = new Button("🗑️");

            {
                removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                removeButton.setOnAction(e -> {
                    CartItem cartItem = getTableView().getItems().get(getIndex());
                    if (cartItem != null) {
                        removeFromCart(cartItem.getProductId());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });

        cartItems = FXCollections.observableArrayList();
        cartTable.setItems(cartItems);
    }

    private void setupEventHandlers() {
        searchProductButton.setOnAction(e -> searchProducts());
        clearSearchButton.setOnAction(e -> clearSearch());
        addToCartButton.setOnAction(e -> addToCartByID());
        finalizeSaleButton.setOnAction(e -> finalizeSale());
        clearCartButton.setOnAction(e -> clearCart());
        modifyCartButton.setOnAction(e -> showModifyCartDialog());
        cancelSaleButton.setOnAction(e -> cancelSale());
    }

    private void initializeFilters() {
        // Initialize will be called after products are loaded
    }

    private void loadAvailableProducts() {
        if (inventaire == null) return;

        allProducts = FXCollections.observableArrayList(inventaire.getArticles().values());
        filteredProducts = FXCollections.observableArrayList(allProducts);
        
        // Filter out out-of-stock products initially
        filteredProducts.removeIf(article -> article.getQuantiteStock() <= 0);
        
        availableProductsTable.setItems(filteredProducts);
        
        // Populate category filter
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("Toutes catégories");
        for (ArticleEpicerie article : allProducts) {
            String category = article.getCategorie();
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
        searchCategoryFilter.setItems(categories);
        searchCategoryFilter.getSelectionModel().selectFirst();
        
        updateSaleSummary();
    }

    public void searchProducts() {
        if (allProducts == null) return;

        String searchTerm = productSearchField.getText().toLowerCase();
        String selectedCategory = searchCategoryFilter.getValue();

        filteredProducts.clear();

        for (ArticleEpicerie article : allProducts) {
            boolean matchesSearch = searchTerm.isEmpty() || 
                                  article.getNom().toLowerCase().contains(searchTerm) ||
                                  article.getId().toLowerCase().contains(searchTerm);
            
            boolean matchesCategory = selectedCategory == null || 
                                    selectedCategory.equals("Toutes catégories") || 
                                    article.getCategorie().equals(selectedCategory);
            
            boolean inStock = article.getQuantiteStock() > 0;

            if (matchesSearch && matchesCategory && inStock) {
                filteredProducts.add(article);
            }
        }
        
        updateSaleSummary();
    }

    public void clearSearch() {
        productSearchField.clear();
        searchCategoryFilter.getSelectionModel().selectFirst();
        searchProducts();
    }

    public void addToCartByID() {
        String productId = addProductIdField.getText().trim();
        String quantityStr = addQuantityField.getText().trim();

        if (productId.isEmpty()) {
            showError("Veuillez entrer un ID de produit!");
            return;
        }

        ArticleEpicerie article = inventaire.getArticle(productId);
        if (article == null) {
            showError("Produit non trouvé!");
            return;
        }

        if (article.getQuantiteStock() <= 0) {
            showError("Ce produit n'est plus en stock!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            showError("Quantité invalide!");
            return;
        }

        if (quantity <= 0) {
            showError("Quantité invalide!");
            return;
        }

        if (quantity > article.getQuantiteStock()) {
            showError("Stock insuffisant! Stock disponible: " + article.getQuantiteStock());
            return;
        }

        addProductToCart(article, quantity);
        addProductIdField.clear();
        addQuantityField.setText("1");
    }

    public void addProductToCart(ArticleEpicerie article, int quantity) {
        // Check if product is already in cart
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(article.getId())) {
                // Update quantity if product exists
                int newQuantity = cartItem.getQuantity() + quantity;
                if (newQuantity > article.getQuantiteStock()) {
                    showError("Stock insuffisant! Stock disponible: " + article.getQuantiteStock());
                    return;
                }
                cartItem.setQuantity(newQuantity);
                updateCartInPanier();
                updateCartDisplay();
                showSuccess("Quantité mise à jour: " + article.getNom());
                return;
            }
        }

        // Add new product to cart
        panierVente.ajouterArticle(article, quantity);
        
        CartItem newItem = new CartItem(
            article.getId(),
            article.getNom(),
            quantity,
            article.getPrix(),
            article.getPrix() * quantity
        );
        
        cartItems.add(newItem);
        updateCartDisplay();
        showSuccess(quantity + " x " + article.getNom() + " ajouté(s) au panier!");
    }

    public void removeFromCart(String productId) {
        cartItems.removeIf(item -> item.getProductId().equals(productId));
        panierVente.supprimerArticle(productId);
        updateCartDisplay();
        showSuccess("Produit retiré du panier");
    }

    private void updateCartInPanier() {
        panierVente.vider();
        for (CartItem item : cartItems) {
            ArticleEpicerie article = inventaire.getArticle(item.getProductId());
            if (article != null) {
                panierVente.ajouterArticle(article, item.getQuantity());
            }
        }
    }

    private void updateCartDisplay() {
        double sousTotal = panierVente.getTotal();
        double tva = sousTotal * 0.20;
        double totalAvecTVA = sousTotal + tva;

        subtotalLabel.setText(String.format("%.2f€", sousTotal));
        taxLabel.setText(String.format("%.2f€", tva));
        totalLabel.setText(String.format("%.2f€", totalAvecTVA));
        cartItemsCountLabel.setText(String.valueOf(cartItems.size()));

        updateSaleSummary();
    }

    private void updateSaleSummary() {
        int availableProducts = filteredProducts != null ? filteredProducts.size() : 0;
        int cartItemsCount = cartItems.size();
        
        saleSummaryLabel.setText(cartItemsCount + " article(s) dans le panier | " + availableProducts + " produit(s) disponible(s)");
    }

    public void showModifyCartDialog() {
        if (cartItems.isEmpty()) {
            showError("Le panier est vide!");
            return;
        }

        // Create a dialog to modify quantities
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Modifier le panier");
        dialog.setHeaderText("Entrez l'ID du produit à modifier:");
        dialog.setContentText("ID Produit:");

        dialog.showAndWait().ifPresent(productId -> {
            CartItem cartItem = cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

            if (cartItem == null) {
                showError("Produit non trouvé dans le panier!");
                return;
            }

            ArticleEpicerie article = inventaire.getArticle(productId);
            if (article == null) {
                showError("Produit non trouvé dans l'inventaire!");
                return;
            }

            TextInputDialog quantityDialog = new TextInputDialog(String.valueOf(cartItem.getQuantity()));
            quantityDialog.setTitle("Modifier la quantité");
            quantityDialog.setHeaderText("Modifier la quantité pour: " + cartItem.getProductName());
            quantityDialog.setContentText("Nouvelle quantité:");

            quantityDialog.showAndWait().ifPresent(quantityStr -> {
                try {
                    int newQuantity = Integer.parseInt(quantityStr);
                    if (newQuantity <= 0) {
                        removeFromCart(productId);
                    } else if (newQuantity > article.getQuantiteStock()) {
                        showError("Stock insuffisant! Stock disponible: " + article.getQuantiteStock());
                    } else {
                        cartItem.setQuantity(newQuantity);
                        updateCartInPanier();
                        updateCartDisplay();
                        showSuccess("Quantité modifiée pour: " + cartItem.getProductName());
                    }
                } catch (NumberFormatException e) {
                    showError("Quantité invalide!");
                }
            });
        });
    }

    public void clearCart() {
        if (cartItems.isEmpty()) {
            showError("Le panier est déjà vide!");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Vider le panier");
        confirmation.setContentText("Êtes-vous sûr de vouloir vider tout le panier?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            cartItems.clear();
            panierVente.vider();
            updateCartDisplay();
            showSuccess("Panier vidé");
        }
    }

    public void finalizeSale() {
        if (panierVente.estVide()) {
            showError("Le panier est vide. Ajoutez des produits avant de finaliser la vente.");
            return;
        }

        // Check stock availability
        boolean stockInsuffisant = false;
        StringBuilder messageErreur = new StringBuilder();

        for (LignePanier ligne : panierVente.getLignes()) {
            ArticleEpicerie article = inventaire.getArticle(ligne.getArticle().getId());
            if (article == null || !article.estDisponible(ligne.getQuantite())) {
                stockInsuffisant = true;
                if (article != null) {
                    messageErreur.append("✗ ").append(article.getNom())
                                 .append(": Stock insuffisant (disponible: ")
                                 .append(article.getQuantiteStock()).append(")\n");
                } else {
                    messageErreur.append("✗ Article introuvable dans l'inventaire\n");
                }
            }
        }

        if (stockInsuffisant) {
            showError("Impossible de finaliser la vente:\n" + messageErreur.toString());
            return;
        }

        // Show confirmation with summary
        double sousTotal = panierVente.getTotal();
        double tva = sousTotal * 0.20;
        double totalAvecTVA = sousTotal + tva;

        String confirmationMessage = String.format(
            "RÉCAPITULATIF DE LA VENTE:\n\n" +
            "Articles: %d\n" +
            "Sous-total: %.2f€\n" +
            "TVA (20%%): %.2f€\n" +
            "TOTAL À PAYER: %.2f€\n\n" +
            "Confirmer et finaliser la vente?",
            cartItems.size(), sousTotal, tva, totalAvecTVA
        );

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de vente");
        confirmation.setHeaderText("Finaliser la vente");
        confirmation.setContentText(confirmationMessage);

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            Vente vente = caisse.enregistrerVente(panierVente, inventaire);
            
            if (vente == null) {
                showError("Erreur lors de l'enregistrement de la vente!");
                return;
            }

            showSuccess("Vente finalisée avec succès!\n\n" + vente.genererFacture());
            
            // Clear cart and refresh available products
            cartItems.clear();
            panierVente.vider();
            updateCartDisplay();
            loadAvailableProducts();
        }
    }

    public void cancelSale() {
        if (!cartItems.isEmpty()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Annuler la vente");
            confirmation.setContentText("Êtes-vous sûr de vouloir annuler cette vente? Tous les articles du panier seront perdus.");

            if (confirmation.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        Stage stage = (Stage) cancelSaleButton.getScene().getWindow();
        stage.close();
    }

    // Inner class for cart items
    public static class CartItem {
        private final String productId;
        private final String productName;
        private int quantity;
        private final double unitPrice;
        private double totalPrice;

        public CartItem(String productId, String productName, int quantity, double unitPrice, double totalPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        // Getters
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalPrice() { return totalPrice; }

        // Setters
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.totalPrice = this.unitPrice * quantity;
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
}