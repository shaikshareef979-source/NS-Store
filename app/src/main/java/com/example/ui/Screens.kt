package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.CartItem
import com.example.data.OrderEntity
import com.example.data.Product
import com.example.data.UserProfile
import com.example.data.WishlistItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ECommerceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeProduct by viewModel.selectedProduct.collectAsState()

    val totalCartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SHOP,
                    onClick = { viewModel.setScreen(AppScreen.SHOP) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Shop") },
                    label = { Text("Shop") },
                    modifier = Modifier.testTag("nav_shop_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CART,
                    onClick = { viewModel.setScreen(AppScreen.CART) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (totalCartCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(totalCartCount.toString(), modifier = Modifier.testTag("cart_badge_count"))
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    },
                    label = { Text("Cart") },
                    modifier = Modifier.testTag("nav_cart_tab")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE,
                    onClick = { viewModel.setScreen(AppScreen.PROFILE) },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                AppScreen.SHOP -> ShopScreen(viewModel)
                AppScreen.CART -> CartScreen(viewModel)
                AppScreen.PROFILE -> ProfileScreen(viewModel)
            }

            // Detail view overlay as a beautiful modal
            activeProduct?.let { product ->
                ProductDetailDialog(product = product, viewModel = viewModel, onDismiss = { viewModel.selectProduct(null) })
            }
        }
    }
}

@Composable
fun ShopScreen(viewModel: ECommerceViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Shop Custom Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text("NS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NS Store",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Curated premium essentials",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Static Lock badge symbolizing safety & checkout protection
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Secure Checkout verified",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Secure checkout",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search products, gear, gadgets...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Category Tabs Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCategory(category) },
                            label = { Text(category, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }
            }

            // Promotional Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Exclusive tech banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Dark elegant gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1D1B20).copy(alpha = 0.9f),
                                        Color(0xFF1D1B20).copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(220.dp)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SUMMER SPECTACULAR",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Elevate Your Dynamic Lifestyle",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enjoy 25% checkout reduction. Fully secured end-to-end.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Catalog Header
            item {
                Text(
                    text = "Featured Products",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                )
            }

            // Product Cards (Custom Grid implemented within LazyColumn to support elegant scrolling)
            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SentimentDissatisfied,
                            contentDescription = "No products found",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No products match your search",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Try searching another category or keyword.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Chunk products into rows of 2
                val productChunks = filteredProducts.chunked(2)
                items(productChunks) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (product in rowItems) {
                            ProductCard(
                                product = product,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.selectProduct(product) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .testTag("product_card_${product.id}")
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Elegant Canvas background placeholder representing product thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background artistic accents
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.05f))
                )
                
                Icon(
                    painter = painterResource(id = product.imageResId),
                    contentDescription = product.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )

                // Rating overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1D1B20).copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        product.rating.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Category overlay
                Text(
                    text = product.category,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "View Details",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailDialog(product: Product, viewModel: ECommerceViewModel, onDismiss: () -> Unit) {
    var selectedSize by remember { mutableStateOf(product.sizes.first()) }
    var selectedColorIndex by remember { mutableStateOf(0) }
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val isWishlisted = wishlistItems.any { it.productId == product.id }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .testTag("product_detail_modal")
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Detail Header Card image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceContainer)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }

                    IconButton(
                        onClick = { viewModel.toggleWishlist(product) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f), CircleShape)
                            .testTag("wishlist_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isWishlisted) Color.Red else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Icon(
                        painter = painterResource(id = product.imageResId),
                        contentDescription = product.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Category & Rating Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category.uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(product.rating.toString(), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Description",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Size Selector
                    Text("Select Size", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.sizes.forEach { size ->
                            val isSelected = selectedSize == size
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { selectedSize = size }
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text(
                                    text = size,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Color", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            product.colorNames[selectedColorIndex],
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        product.colors.forEachIndexed { index, hex ->
                            val isSelected = selectedColorIndex == index
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Add To Cart Button
                    Button(
                        onClick = {
                            viewModel.addToCart(product, selectedSize, product.colorNames[selectedColorIndex])
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_to_cart_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Shopping Cart", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(viewModel: ECommerceViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val checkoutStep by viewModel.checkoutStep.collectAsState()

    AnimatedContent(
        targetState = checkoutStep,
        transitionSpec = {
            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
        }
    ) { step ->
        when (step) {
            CheckoutStep.IDLE -> CartListScreen(viewModel, cartItems)
            CheckoutStep.SHIPPING -> CheckoutShippingScreen(viewModel)
            CheckoutStep.PAYMENT -> CheckoutPaymentScreen(viewModel)
            CheckoutStep.PROCESSING -> CheckoutProcessingScreen()
            CheckoutStep.SUCCESS -> CheckoutSuccessScreen(viewModel)
        }
    }
}

@Composable
fun CartListScreen(viewModel: ECommerceViewModel, cartItems: List<CartItem>) {
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val tax by viewModel.cartTax.collectAsState()
    val shipping by viewModel.cartShipping.collectAsState()
    val total by viewModel.cartTotal.collectAsState()

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.RemoveShoppingCart,
                    contentDescription = "Empty Cart",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your Shopping Cart is Empty",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Explore our premium products collection and add items to your secure checkout cart.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = Alignment.Center.run { TextAlign.Center },
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.setScreen(AppScreen.SHOP) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("go_shopping_button")
            ) {
                Text("Start Shopping", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Shopping Cart",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(item = item, viewModel = viewModel)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Financial Breakdown Panel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bill Summary", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Items Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Text(viewModel.formatAmount(subtotal), color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Secured Shipping", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Text(if (shipping == 0.0) "FREE" else viewModel.formatAmount(shipping), color = if (shipping == 0.0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = if (shipping == 0.0) FontWeight.Bold else FontWeight.Normal)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("State Tax (8.25%)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Text(viewModel.formatAmount(tax), color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(viewModel.formatAmount(total), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Bottom bar with checkout trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = { viewModel.startCheckout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("checkout_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Checkout (${viewModel.formatAmount(total)})", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, viewModel: ECommerceViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle placeholder for product thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Size: ${item.size}  •  Color: ${item.color}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.formatAmount(item.price),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }

            // Adjust Quantities
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.updateCartItemQuantity(item, false) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
                
                Text(
                    text = item.quantity.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { viewModel.updateCartItemQuantity(item, true) },
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increment", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun CheckoutShippingScreen(viewModel: ECommerceViewModel) {
    val shippingAddress by viewModel.shippingAddress.collectAsState()
    val billingPhone by viewModel.billingPhone.collectAsState()
    val billingName by viewModel.billingName.collectAsState()
    val checkoutError by viewModel.checkoutError.collectAsState()
    val total by viewModel.cartTotal.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.resetCheckout() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Shipping Information", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Stage progression bar
            CheckoutProgressBar(currentStep = 1)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Verify Shipping Destination", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = billingName,
                onValueChange = { viewModel.billingName.value = it },
                label = { Text("Recipient Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shipping_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = shippingAddress,
                onValueChange = { viewModel.shippingAddress.value = it },
                label = { Text("Delivery Street Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("shipping_address_input"),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = billingPhone,
                onValueChange = { viewModel.billingPhone.value = it },
                label = { Text("Secure Contact Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shipping_phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            checkoutError?.let { err ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            Button(
                onClick = { viewModel.proceedToPayment() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("proceed_payment_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Proceed to Payments Detail", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun CheckoutPaymentScreen(viewModel: ECommerceViewModel) {
    val cardNumber by viewModel.cardNumber.collectAsState()
    val cardExpiry by viewModel.cardExpiry.collectAsState()
    val cardCvv by viewModel.cardCvv.collectAsState()
    val billingName by viewModel.billingName.collectAsState()
    val checkoutError by viewModel.checkoutError.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.startCheckout() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Secure Payments Gate", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            CheckoutProgressBar(currentStep = 2)
            Spacer(modifier = Modifier.height(20.dp))

            // Wallet Check Banner
            profile?.let { prof ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Secured Wallet Authorization", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Available Balance: ${viewModel.formatAmount(prof.walletBalance)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Virtual Credit Card Visual Graphic (Premium Dark Card matches HTML's Obsidian palette #1D1B20)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Subtle futuristic tech lines on card
                            val gradient = Brush.radialGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.2f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                radius = size.width * 0.8f
                            )
                            drawRect(gradient)
                        }
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NS SECURE PAY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = "Visa",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            // Pretty format credit card number block
                            val padded = cardNumber.padEnd(16, '•')
                            val formatted = padded.chunked(4).joinToString(" ")
                            Text(
                                text = formatted,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("CARDHOLDER", color = Color(0xFFCAC4D0), fontSize = 9.sp)
                                Text(billingName.uppercase(), color = Color.White, fontSize = 11.sp, maxLines = 1)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("VALID THRU", color = Color(0xFFCAC4D0), fontSize = 9.sp)
                                Text(cardExpiry.ifEmpty { "MM/YY" }, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Enter Secured Card Credentials", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = billingName,
                onValueChange = { viewModel.billingName.value = it },
                label = { Text("Cardholder Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { 
                    // Formatting credit card input to numbers
                    val filtered = it.filter { ch -> ch.isDigit() }.take(16)
                    viewModel.cardNumber.value = filtered
                },
                label = { Text("Credit Card Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_card_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() || ch == '/' }.take(5)
                        viewModel.cardExpiry.value = filtered
                    },
                    label = { Text("Expiry (MM/YY)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("payment_expiry_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = cardCvv,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }.take(4)
                        viewModel.cardCvv.value = filtered
                    },
                    label = { Text("CVV Security Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("payment_cvv_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            checkoutError?.let { err ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            Button(
                onClick = { viewModel.completeSecurePayment() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("complete_payment_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify & Pay ${viewModel.formatAmount(total)}", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CheckoutProcessingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Encrypted Connection icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .rotate(rotation)
            )
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            "Securing Bank Authorization",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Securing merchant tunnels, authenticating signatures, and validating tokenized debit flows...",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = Alignment.Center.run { TextAlign.Center },
            modifier = Modifier.padding(horizontal = 20.dp),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CheckoutSuccessScreen(viewModel: ECommerceViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Transaction Authorized",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Payment Fully Secure",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Transaction Authorized & Cryptographically Signed",
            fontSize = 11.sp,
            color = Color(0xFF10B981),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Order is now registered and being packaged! Real-time telemetry tracking has been generated on your user profile timeline.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = Alignment.Center.run { TextAlign.Center },
            modifier = Modifier.padding(horizontal = 10.dp),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(30.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { 
                    viewModel.setScreen(AppScreen.PROFILE)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("view_orders_button"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Order History", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { 
                    viewModel.setScreen(AppScreen.SHOP)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("success_continue_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Keep Shopping", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CheckoutProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (currentStep >= 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("1", color = if (currentStep >= 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        HorizontalDivider(modifier = Modifier.width(60.dp), color = if (currentStep >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, thickness = 2.dp)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (currentStep >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("2", color = if (currentStep >= 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ECommerceViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val orders by viewModel.orderHistory.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    // Forms
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    // Sync form variables when profile loads
    LaunchedEffect(profile) {
        profile?.let { prof ->
            editName = prof.name
            editEmail = prof.email
            editPhone = prof.phone
            editAddress = prof.address
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // High polished User Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_user_avatar),
                        contentDescription = "Profile portrait photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                profile?.let { prof ->
                    Text(
                        text = prof.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = prof.email,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Secure Wallet Dashboard Card
        item {
            profile?.let { prof ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("NS WALLET BALANCE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.formatAmount(prof.walletBalance),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Icon(
                                Icons.Default.Wallet,
                                contentDescription = "Wallet Balance",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Secure Funds Boost", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50.0, 100.0, 250.0).forEach { amount ->
                                Button(
                                    onClick = { viewModel.addWalletFunds(amount) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("fund_add_$amount"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("+₹${amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Profile Details (Edit or View Mode)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Personal Credentials",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(
                            onClick = { 
                                if (isEditMode) {
                                    // Save
                                    viewModel.updateProfileInfo(editName, editEmail, editPhone, editAddress)
                                }
                                isEditMode = !isEditMode
                            },
                            modifier = Modifier.testTag("profile_edit_button")
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Save" else "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isEditMode) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_email"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Contact Phone") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Street Address") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_address"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    } else {
                        profile?.let { prof ->
                            ProfileItemField(icon = Icons.Default.Person, label = "Full Name", value = prof.name)
                            ProfileItemField(icon = Icons.Default.Email, label = "Email Address", value = prof.email)
                            ProfileItemField(icon = Icons.Default.Phone, label = "Contact Phone", value = prof.phone)
                            ProfileItemField(icon = Icons.Default.LocationOn, label = "Street Address", value = prof.address)
                        }
                    }
                }
            }
        }

        // Saved Wishlist section
        item {
            Text(
                text = "My Saved Wishlist",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        if (wishlistItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Your wishlist is currently empty.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(wishlistItems) { item ->
                        WishlistProductItem(item = item, viewModel = viewModel)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Chronological order histories with status timelines
        item {
            Text(
                text = "Secure Order History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No orders placed yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            items(orders) { order ->
                OrderHistoryCard(order = order, viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProfileItemField(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun OrderHistoryCard(order: OrderEntity, viewModel: ECommerceViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ORDER ID", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(order.orderId, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }

                // Beautiful status badge with green pulse indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(order.status, color = Color(0xFF3B82F6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("ITEMS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(order.itemsSummary, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(order.timestamp))
                    Text("TRANSACTION DATE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL SECURED", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(viewModel.formatAmount(order.totalAmount), color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WishlistProductItem(item: WishlistItem, viewModel: ECommerceViewModel) {
    val matchedProduct = viewModel.productsList.find { it.id == item.productId }
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable {
                if (matchedProduct != null) {
                    viewModel.selectProduct(matchedProduct)
                }
            }
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                
                // Remove button top right
                IconButton(
                    onClick = {
                        if (matchedProduct != null) {
                            viewModel.toggleWishlist(matchedProduct)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove from Wishlist",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = viewModel.formatAmount(item.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
