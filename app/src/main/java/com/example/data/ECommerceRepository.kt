package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val category: String,
    val imageResId: Int,
    val sizes: List<String> = listOf("S", "M", "L", "XL"),
    val colors: List<String> = listOf("#1A1A1A", "#F5F5F7", "#10B981", "#3B82F6"),
    val colorNames: List<String> = listOf("Midnight", "Frost", "Emerald", "Azure")
)

class ECommerceRepository(private val dao: ECommerceDao) {

    // Statically defined premium products
    val products = listOf(
        Product(
            id = 101,
            title = "Horizon Watch Pro",
            description = "Premium smart wearable featuring a vivid AMOLED screen, 14-day battery life, continuous oxygen tracking, and dynamic fitness metrics. Hand-finished titanium frame.",
            price = 299.99,
            rating = 4.8,
            category = "Electronics",
            imageResId = android.R.drawable.ic_menu_myplaces
        ),
        Product(
            id = 102,
            title = "AeroBuds Ultra",
            description = "Sleek spatial audio earbuds equipped with adaptive Active Noise Cancellation, high-fidelity custom drivers, and 36 hours of playtime with the pocket charging vault.",
            price = 149.99,
            rating = 4.7,
            category = "Electronics",
            imageResId = android.R.drawable.ic_lock_silent_mode
        ),
        Product(
            id = 103,
            title = "Vortex Utility Backpack",
            description = "A weatherproof tactical roll-top tech pack with custom modular sleeves, hidden passport pocket, magnetic buckles, and impact-resistant laptop compartment.",
            price = 89.99,
            rating = 4.6,
            category = "Clothing",
            imageResId = android.R.drawable.ic_menu_manage
        ),
        Product(
            id = 104,
            title = "Starlight Desk Prism",
            description = "Smart spectrum LED accent light syncing with sound waves. Casts smooth gradients, provides dynamic mood presets, and connects to voice assistant smart systems.",
            price = 59.99,
            rating = 4.9,
            category = "Home Goods",
            imageResId = android.R.drawable.ic_menu_compass
        ),
        Product(
            id = 105,
            title = "MagHub Wireless Charger",
            description = "3-in-1 weighted premium leather charging platform. Fast charges your phone, smartwatch, and earbuds concurrently with elegant magnetic lock alignments.",
            price = 79.99,
            rating = 4.5,
            category = "Accessories",
            imageResId = android.R.drawable.ic_menu_slideshow
        ),
        Product(
            id = 106,
            title = "RFID Slim Leather Shield",
            description = "Handcrafted full-grain Italian leather security wallet. Fits 8 cards, features a quick-pull security tab, integrated money clip, and military-grade RFID-blocking lining.",
            price = 39.99,
            rating = 4.8,
            category = "Clothing",
            imageResId = android.R.drawable.ic_lock_lock
        )
    )

    // User profile actions
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun saveProfile(profile: UserProfile) {
        dao.saveUserProfile(profile)
    }

    suspend fun ensureDefaultProfileCreated() {
        val existing = dao.getUserProfile().firstOrNull()
        if (existing == null) {
            val defaultProfile = UserProfile(
                id = 1,
                name = "Shaik Shareef",
                email = "shaikshareef979@gmail.com",
                phone = "+1 (555) 019-2834",
                address = "742 Evergreen Terrace, Springfield",
                cardHolder = "SHAIK SHAREEF",
                cardNumber = "4532 7812 9043 5421",
                cardExpiry = "12/28",
                cardCvv = "342",
                walletBalance = 1500.00
            )
            dao.saveUserProfile(defaultProfile)
        }
    }

    // Shopping Cart actions
    val cartItems: Flow<List<CartItem>> = dao.getCartItems()

    suspend fun addToCart(cartItem: CartItem) {
        dao.insertCartItem(cartItem)
    }

    suspend fun deleteCartItem(productId: Int) {
        dao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    // Order History actions
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()

    suspend fun createOrder(order: OrderEntity) {
        dao.insertOrder(order)
    }

    // Wishlist actions
    val wishlistItems: Flow<List<WishlistItem>> = dao.getWishlistItems()

    suspend fun addToWishlist(item: WishlistItem) {
        dao.insertWishlistItem(item)
    }

    suspend fun removeFromWishlist(productId: Int) {
        dao.deleteWishlistItem(productId)
    }

    fun isProductInWishlist(productId: Int): Flow<Boolean> {
        return dao.isProductInWishlist(productId)
    }
}
