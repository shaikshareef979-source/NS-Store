package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val cardHolder: String,
    val cardNumber: String,
    val cardExpiry: String,
    val cardCvv: String,
    val walletBalance: Double
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val category: String,
    val imageResId: Int,
    val quantity: Int,
    val size: String,
    val color: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val timestamp: Long,
    val totalAmount: Double,
    val itemsSummary: String,
    val paymentMethod: String,
    val status: String
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val category: String,
    val imageResId: Int
)

@Dao
interface ECommerceDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM wishlist_items")
    fun getWishlistItems(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteWishlistItem(productId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId LIMIT 1)")
    fun isProductInWishlist(productId: Int): Flow<Boolean>
}

@Database(entities = [UserProfile::class, CartItem::class, OrderEntity::class, WishlistItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eCommerceDao(): ECommerceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ns_store_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
