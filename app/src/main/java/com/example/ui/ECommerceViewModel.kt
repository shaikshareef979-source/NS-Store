package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class AppScreen {
    SHOP, CART, PROFILE
}

enum class CheckoutStep {
    IDLE, SHIPPING, PAYMENT, PROCESSING, SUCCESS
}

class ECommerceViewModel(private val repository: ECommerceRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureDefaultProfileCreated()
        }
    }

    // Navigation and Filtering State
    private val _currentScreen = MutableStateFlow(AppScreen.SHOP)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // DB States
    val productsList: List<Product> = repository.products

    val categories: List<String> = listOf("All") + repository.products.map { it.category }.distinct()

    val filteredProducts: StateFlow<List<Product>> = combine(_searchQuery, _selectedCategory) { query, category ->
        repository.products.filter { product ->
            val matchesSearch = product.title.contains(query, ignoreCase = true) || 
                                product.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.products)

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val orderHistory: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Financial Totals
    val cartSubtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTax: StateFlow<Double> = cartSubtotal.map { sub ->
        sub * 0.0825 // 8.25% Tax
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartShipping: StateFlow<Double> = cartSubtotal.map { sub ->
        if (sub > 150.00 || sub == 0.0) 0.0 else 15.00
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, cartTax, cartShipping) { sub, tax, ship ->
        sub + tax + ship
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Checkout Flow States
    private val _checkoutStep = MutableStateFlow(CheckoutStep.IDLE)
    val checkoutStep: StateFlow<CheckoutStep> = _checkoutStep.asStateFlow()

    // Interactive Payment Fields with Auto-formatting/Validation
    val billingName = MutableStateFlow("")
    val shippingAddress = MutableStateFlow("")
    val billingPhone = MutableStateFlow("")
    val cardNumber = MutableStateFlow("")
    val cardExpiry = MutableStateFlow("")
    val cardCvv = MutableStateFlow("")
    
    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError.asStateFlow()

    // Methods
    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen != AppScreen.CART) {
            _checkoutStep.value = CheckoutStep.IDLE
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun addToCart(product: Product, size: String, color: String) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == product.id && it.size == size && it.color == color }
            if (existing != null) {
                repository.addToCart(existing.copy(quantity = existing.quantity + 1))
            } else {
                repository.addToCart(
                    CartItem(
                        productId = product.id,
                        title = product.title,
                        price = product.price,
                        category = product.category,
                        imageResId = product.imageResId,
                        quantity = 1,
                        size = size,
                        color = color
                    )
                )
            }
        }
    }

    fun updateCartItemQuantity(item: CartItem, increment: Boolean) {
        viewModelScope.launch {
            if (increment) {
                repository.addToCart(item.copy(quantity = item.quantity + 1))
            } else {
                if (item.quantity > 1) {
                    repository.addToCart(item.copy(quantity = item.quantity - 1))
                } else {
                    repository.deleteCartItem(item.productId)
                }
            }
        }
    }

    fun removeCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.deleteCartItem(item.productId)
        }
    }

    // Checkout State Controllers
    fun startCheckout() {
        val profile = userProfile.value ?: return
        billingName.value = profile.name
        shippingAddress.value = profile.address
        billingPhone.value = profile.phone
        cardNumber.value = profile.cardNumber
        cardExpiry.value = profile.cardExpiry
        cardCvv.value = profile.cardCvv
        _checkoutError.value = null
        _checkoutStep.value = CheckoutStep.SHIPPING
    }

    fun proceedToPayment() {
        if (shippingAddress.value.isBlank()) {
            _checkoutError.value = "Please enter a valid shipping address"
            return
        }
        if (billingPhone.value.isBlank()) {
            _checkoutError.value = "Please enter your contact phone number"
            return
        }
        _checkoutError.value = null
        _checkoutStep.value = CheckoutStep.PAYMENT
    }

    fun completeSecurePayment() {
        // Simple input validation
        if (cardNumber.value.length < 16) {
            _checkoutError.value = "Card number must be 16 digits"
            return
        }
        if (!cardExpiry.value.contains("/")) {
            _checkoutError.value = "Expiry date must be in MM/YY format"
            return
        }
        if (cardCvv.value.length < 3) {
            _checkoutError.value = "CVV must be 3 or 4 digits"
            return
        }

        val total = cartTotal.value
        val profile = userProfile.value ?: return

        if (profile.walletBalance < total) {
            _checkoutError.value = "Insufficient wallet balance (Total: ${formatAmount(total)}, Available: ${formatAmount(profile.walletBalance)})"
            return
        }

        _checkoutError.value = null
        _checkoutStep.value = CheckoutStep.PROCESSING

        viewModelScope.launch {
            // Simulated secure banking network authorization delay
            delay(2200)

            // Subtract amount from user wallet
            val updatedProfile = profile.copy(
                name = billingName.value,
                phone = billingPhone.value,
                address = shippingAddress.value,
                cardNumber = cardNumber.value,
                cardExpiry = cardExpiry.value,
                cardCvv = cardCvv.value,
                walletBalance = profile.walletBalance - total
            )
            repository.saveProfile(updatedProfile)

            // Generate order summary and place it in Order history
            val summary = cartItems.value.joinToString(", ") { "${it.title} x${it.quantity}" }
            val orderId = "NS-${System.currentTimeMillis().toString().takeLast(6)}-${(1000..9999).random()}"
            val newOrder = OrderEntity(
                orderId = orderId,
                timestamp = System.currentTimeMillis(),
                totalAmount = total,
                itemsSummary = summary,
                paymentMethod = "Visa ending in ${cardNumber.value.takeLast(4)}",
                status = "In Transit"
            )
            repository.createOrder(newOrder)

            // Clear the cart
            repository.clearCart()

            // Go to Success Phase
            _checkoutStep.value = CheckoutStep.SUCCESS
        }
    }

    fun addWalletFunds(amount: Double) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.saveProfile(profile.copy(walletBalance = profile.walletBalance + amount))
        }
    }

    fun updateProfileInfo(name: String, email: String, phone: String, address: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.saveProfile(profile.copy(
                name = name,
                email = email,
                phone = phone,
                address = address
            ))
        }
    }

    fun resetCheckout() {
        _checkoutStep.value = CheckoutStep.IDLE
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val inWishlist = wishlistItems.value.any { it.productId == product.id }
            if (inWishlist) {
                repository.removeFromWishlist(product.id)
            } else {
                repository.addToWishlist(
                    WishlistItem(
                        productId = product.id,
                        title = product.title,
                        price = product.price,
                        category = product.category,
                        imageResId = product.imageResId
                    )
                )
            }
        }
    }

    fun formatAmount(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(amount)
    }
}
