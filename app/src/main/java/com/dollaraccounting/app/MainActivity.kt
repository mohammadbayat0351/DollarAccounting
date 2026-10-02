package com.dollaraccounting.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                DollarAccountingApp(this)
            }
        }
    }
}

data class Person(
    val id: Long,
    val firstName: String,
    val lastName: String
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class Product(
    val id: Long,
    val name: String
)

data class Purchase(
    val id: Long,
    val personId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val createdAt: Long
)

data class Payment(
    val id: Long,
    val personId: Long,
    val amount: Double,
    val createdAt: Long
)

data class Sale(
    val id: Long,
    val customer: String,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val createdAt: Long
)

private val money = DecimalFormat("#,##0.00")

private val dateFormat =
    SimpleDateFormat(
        "yyyy/MM/dd HH:mm",
        Locale.US
    )

class AppStorage(context: Context) {

    // مهم:
    // اسم حافظه را تغییر نده.
    // دیتای نسخه فعلی داخل همین v4 قرار دارد.
    private val prefs =
        context.getSharedPreferences(
            "dollar_accounting_v4",
            Context.MODE_PRIVATE
        )

    private fun clean(value: String): String {
        return value
            .replace("|", " ")
            .replace(";;", " ")
            .trim()
    }

    private fun rows(key: String): List<String> {

        val text =
            prefs.getString(key, "") ?: ""

        return if (text.isBlank()) {
            emptyList()
        } else {
            text.split(";;")
        }
    }

    fun savePeople(
        people: List<Person>
    ) {

        val text =
            people.joinToString(";;") {
                "${it.id}|${clean(it.firstName)}|${clean(it.lastName)}"
            }

        prefs.edit()
            .putString("people", text)
            .apply()
    }

    fun loadPeople(): List<Person> {

        return rows("people")
            .mapNotNull { row ->

                val p = row.split("|")

                if (p.size < 3) {
                    return@mapNotNull null
                }

                Person(
                    id =
                        p[0].toLongOrNull()
                            ?: return@mapNotNull null,

                    firstName = p[1],
                    lastName = p[2]
                )
            }
            .filter {
                it.fullName.isNotBlank()
            }
    }

    fun saveProducts(
        products: List<Product>
    ) {

        val text =
            products.joinToString(";;") {
                "${it.id}|${clean(it.name)}"
            }

        prefs.edit()
            .putString("products", text)
            .apply()
    }

    fun loadProducts(): List<Product> {

        return rows("products")
            .mapNotNull { row ->

                val p = row.split("|")

                if (p.size < 2) {
                    return@mapNotNull null
                }

                Product(
                    id =
                        p[0].toLongOrNull()
                            ?: return@mapNotNull null,

                    name = p[1]
                )
            }
            .filter {
                it.name.isNotBlank()
            }
    }

    fun savePurchases(
        purchases: List<Purchase>
    ) {

        val text =
            purchases.joinToString(";;") {

                "${it.id}|" +
                    "${it.personId}|" +
                    "${it.productId}|" +
                    "${it.quantity}|" +
                    "${it.unitPrice}|" +
                    "${it.createdAt}"
            }

        prefs.edit()
            .putString("purchases", text)
            .apply()
    }

    fun loadPurchases(): List<Purchase> {

        return rows("purchases")
            .mapNotNull { row ->

                val p = row.split("|")

                if (p.size < 6) {
                    return@mapNotNull null
                }

                try {

                    Purchase(
                        id = p[0].toLong(),
                        personId = p[1].toLong(),
                        productId = p[2].toLong(),
                        quantity = p[3].toInt(),
                        unitPrice = p[4].toDouble(),
                        createdAt = p[5].toLong()
                    )

                } catch (_: Exception) {
                    null
                }
            }
    }

    fun savePayments(
        payments: List<Payment>
    ) {

        val text =
            payments.joinToString(";;") {

                "${it.id}|" +
                    "${it.personId}|" +
                    "${it.amount}|" +
                    "${it.createdAt}"
            }

        prefs.edit()
            .putString("payments", text)
            .apply()
    }

    fun loadPayments(): List<Payment> {

        return rows("payments")
            .mapNotNull { row ->

                val p = row.split("|")

                if (p.size < 4) {
                    return@mapNotNull null
                }

                try {

                    Payment(
                        id = p[0].toLong(),
                        personId = p[1].toLong(),
                        amount = p[2].toDouble(),
                        createdAt = p[3].toLong()
                    )

                } catch (_: Exception) {
                    null
                }
            }
    }

    fun saveSales(
        sales: List<Sale>
    ) {

        val text =
            sales.joinToString(";;") {

                "${it.id}|" +
                    "${clean(it.customer)}|" +
                    "${it.productId}|" +
                    "${it.quantity}|" +
                    "${it.unitPrice}|" +
                    "${it.createdAt}"
            }

        prefs.edit()
            .putString("sales", text)
            .apply()
    }

    fun loadSales(): List<Sale> {

        return rows("sales")
            .mapNotNull { row ->

                val p = row.split("|")

                if (p.size < 6) {
                    return@mapNotNull null
                }

                try {

                    Sale(
                        id = p[0].toLong(),
                        customer = p[1],
                        productId = p[2].toLong(),
                        quantity = p[3].toInt(),
                        unitPrice = p[4].toDouble(),
                        createdAt = p[5].toLong()
                    )

                } catch (_: Exception) {
                    null
                }
            }
    }
}

private fun productQuantity(
    productId: Long,
    purchases: List<Purchase>,
    sales: List<Sale>
): Int {

    val bought =
        purchases
            .filter {
                it.productId == productId
            }
            .sumOf {
                it.quantity
            }

    val sold =
        sales
            .filter {
                it.productId == productId
            }
            .sumOf {
                it.quantity
            }

    return bought - sold
}

private fun productAverage(
    productId: Long,
    purchases: List<Purchase>
): Double {

    val list =
        purchases.filter {
            it.productId == productId
        }

    val quantity =
        list.sumOf {
            it.quantity
        }

    if (quantity == 0) {
        return 0.0
    }

    val total =
        list.sumOf {
            it.quantity * it.unitPrice
        }

    return total / quantity
}

private fun personDebt(
    personId: Long,
    purchases: List<Purchase>,
    payments: List<Payment>
): Double {

    val bought =
        purchases
            .filter {
                it.personId == personId
            }
            .sumOf {
                it.quantity * it.unitPrice
            }

    val paid =
        payments
            .filter {
                it.personId == personId
            }
            .sumOf {
                it.amount
            }

    return bought - paid
}

@Composable
fun DollarAccountingApp(
    context: Context
) {

    val storage =
        remember {
            AppStorage(context)
        }

    val people =
        remember {
            mutableStateListOf<Person>()
                .apply {
                    addAll(
                        storage.loadPeople()
                    )
                }
        }

    val products =
        remember {
            mutableStateListOf<Product>()
                .apply {
                    addAll(
                        storage.loadProducts()
                    )
                }
        }

    val purchases =
        remember {
            mutableStateListOf<Purchase>()
                .apply {
                    addAll(
                        storage.loadPurchases()
                    )
                }
        }

    val payments =
        remember {
            mutableStateListOf<Payment>()
                .apply {
                    addAll(
                        storage.loadPayments()
                    )
                }
        }

    val sales =
        remember {
            mutableStateListOf<Sale>()
                .apply {
                    addAll(
                        storage.loadSales()
                    )
                }
        }

    var page by remember {
        mutableStateOf("home")
    }

    var selectedProductId by remember {
        mutableStateOf<Long?>(null)
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = page == "home",
                    onClick = {
                        page = "home"
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            null
                        )
                    },
                    label = {
                        Text("خانه")
                    }
                )

                NavigationBarItem(
                    selected = page == "buy",
                    onClick = {
                        page = "buy"
                    },
                    icon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            null
                        )
                    },
                    label = {
                        Text("خرید")
                    }
                )

                NavigationBarItem(
                    selected = page == "sell",
                    onClick = {
                        page = "sell"
                    },
                    icon = {
                        Icon(
                            Icons.Default.PointOfSale,
                            null
                        )
                    },
                    label = {
                        Text("فروش")
                    }
                )

                NavigationBarItem(
                    selected = page == "pay",
                    onClick = {
                        page = "pay"
                    },
                    icon = {
                        Icon(
                            Icons.Default.Payments,
                            null
                        )
                    },
                    label = {
                        Text("پرداخت")
                    }
                )

                NavigationBarItem(
                    selected = page == "products",
                    onClick = {
                        page = "products"
                    },
                    icon = {
                        Icon(
                            Icons.Default.Inventory2,
                            null
                        )
                    },
                    label = {
                        Text("کالا")
                    }
                )

                NavigationBarItem(
                    selected = page == "people",
                    onClick = {
                        page = "people"
                    },
                    icon = {
                        Icon(
                            Icons.Default.People,
                            null
                        )
                    },
                    label = {
                        Text("اشخاص")
                    }
                )
            }
        }

    ) { padding ->

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(14.dp)
        ) {

            AnimatedContent(
                targetState = page,
                label = "page"
            ) { currentPage ->

                when (currentPage) {

                    "home" -> {

                        HomePage(
                            people = people,
                            products = products,
                            purchases = purchases,
                            payments = payments,
                            sales = sales
                        )
                    }

                    "people" -> {

                        PeoplePage(
                            people = people,
                            purchases = purchases,
                            payments = payments,
                            storage = storage
                        )
                    }

                    "products" -> {

                        ProductsPage(
                            products = products,
                            purchases = purchases,
                            sales = sales,
                            storage = storage,
                            onProductClick = {

                                selectedProductId = it
                                page = "productDetail"
                            }
                        )
                    }

                    "productDetail" -> {

                        ProductDetailPage(
                            product =
                                products.find {
                                    it.id ==
                                        selectedProductId
                                },

                            people = people,
                            purchases = purchases,
                            sales = sales,

                            onBack = {
                                page = "products"
                            }
                        )
                    }

                    "buy" -> {

                        BuyPage(
                            people = people,
                            products = products,

                            onSave = {
                                    person,
                                    product,
                                    quantity,
                                    price ->

                                val time =
                                    System.currentTimeMillis()

                                purchases.add(
                                    Purchase(
                                        id = time,
                                        personId = person.id,
                                        productId = product.id,
                                        quantity = quantity,
                                        unitPrice = price,
                                        createdAt = time
                                    )
                                )

                                storage.savePurchases(
                                    purchases
                                )

                                page = "home"
                            }
                        )
                    }

                    "sell" -> {

                        SellPage(
                            products = products,
                            purchases = purchases,
                            sales = sales,

                            onSave = {
                                    customer,
                                    product,
                                    quantity,
                                    price ->

                                val time =
                                    System.currentTimeMillis()

                                sales.add(
                                    Sale(
                                        id = time,
                                        customer = customer,
                                        productId = product.id,
                                        quantity = quantity,
                                        unitPrice = price,
                                        createdAt = time
                                    )
                                )

                                storage.saveSales(
                                    sales
                                )

                                page = "home"
                            }
                        )
                    }

                    "pay" -> {

                        PaymentPage(
                            people = people,

                            onSave = {
                                    person,
                                    amount ->

                                val time =
                                    System.currentTimeMillis()

                                payments.add(
                                    Payment(
                                        id = time,
                                        personId = person.id,
                                        amount = amount,
                                        createdAt = time
                                    )
                                )

                                storage.savePayments(
                                    payments
                                )

                                page = "home"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null
) {

    Column(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.End
    ) {

        Text(
            text = title,
            style =
                MaterialTheme
                    .typography
                    .headlineMedium,
            fontWeight =
                FontWeight.Black
        )

        if (subtitle != null) {

            Text(
                text = subtitle,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier =
        Modifier.fillMaxWidth()
) {

    Card(
        modifier = modifier,
        shape =
            RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                title,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(6.dp)
            )

            Text(
                value,
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Black
            )
        }
    }
}

@Composable
fun EmptyCard(
    text: String
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp)
    ) {

        Text(
            text = text,
            modifier =
                Modifier.padding(18.dp)
        )
    }
}

@Composable
fun HomePage(
    people: List<Person>,
    products: List<Product>,
    purchases: List<Purchase>,
    payments: List<Payment>,
    sales: List<Sale>
) {

    val totalPurchases =
        purchases.sumOf {
            it.quantity *
                it.unitPrice
        }

    val totalSales =
        sales.sumOf {
            it.quantity *
                it.unitPrice
        }

    val totalPayments =
        payments.sumOf {
            it.amount
        }

    val totalDebt =
        totalPurchases -
            totalPayments

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(12.dp),

        contentPadding =
            PaddingValues(
                bottom = 16.dp
            )
    ) {

        item {

            PageHeader(
                title =
                    "حسابداری دلاری",
                subtitle =
                    "خرید، فروش و موجودی"
            )
        }

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                MetricCard(
                    title = "کل خرید",
                    value =
                        "$${
                            money.format(
                                totalPurchases
                            )
                        }",
                    modifier =
                        Modifier.weight(1f)
                )

                MetricCard(
                    title = "کل فروش",
                    value =
                        "$${
                            money.format(
                                totalSales
                            )
                        }",
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }

        item {

            MetricCard(
                title =
                    "مانده بدهی تأمین‌کنندگان",

                value =
                    "$${
                        money.format(
                            totalDebt
                        )
                    }"
            )
        }

        item {

            Text(
                "موجودی کالاها",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        }

        if (products.isEmpty()) {

            item {

                EmptyCard(
                    "هنوز کالایی تعریف نشده"
                )
            }

        } else {

            items(
                products,
                key = {
                    it.id
                }
            ) { product ->

                val quantity =
                    productQuantity(
                        product.id,
                        purchases,
                        sales
                    )

                val average =
                    productAverage(
                        product.id,
                        purchases
                    )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(22.dp)
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            "میانگین خرید\n$${
                                money.format(
                                    average
                                )
                            }"
                        )

                        Column(
                            horizontalAlignment =
                                Alignment.End
                        ) {

                            Text(
                                product.name,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                "موجودی: $quantity عدد",
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }
                    }
                }
            }
        }

        if (people.isNotEmpty()) {

            item {

                Text(
                    "مانده اشخاص",
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            items(
                people,
                key = {
                    it.id
                }
            ) { person ->

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            "$${
                                money.format(
                                    personDebt(
                                        person.id,
                                        purchases,
                                        payments
                                    )
                                )
                            }",
                            fontWeight =
                                FontWeight.Black
                        )

                        Text(
                            person.fullName,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun BuyPage(
    people: List<Person>,
    products: List<Product>,
    onSave:
        (
            Person,
            Product,
            Int,
            Double
        ) -> Unit
) {

    var selectedPerson by remember {
        mutableStateOf<Person?>(null)
    }

    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }

    var personMenu by remember {
        mutableStateOf(false)
    }

    var productMenu by remember {
        mutableStateOf(false)
    }

    var quantityText by remember {
        mutableStateOf("")
    }

    var priceText by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        PageHeader(
            title = "ثبت خرید",
            subtitle =
                "ورود کالا به انبار"
        )

        if (
            people.isEmpty() ||
            products.isEmpty()
        ) {

            EmptyCard(
                "ابتدا حداقل یک شخص و یک کالا تعریف کنید"
            )

            return@Column
        }

        ExposedDropdownMenuBox(
            expanded =
                personMenu,

            onExpandedChange = {
                personMenu =
                    !personMenu
            }
        ) {

            OutlinedTextField(
                value =
                    selectedPerson
                        ?.fullName
                        ?: "",

                onValueChange = {},

                readOnly = true,

                label = {
                    Text(
                        "تأمین‌کننده"
                    )
                },

                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded =
                    personMenu,

                onDismissRequest = {
                    personMenu = false
                }
            ) {

                people.forEach {
                        person ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                person.fullName
                            )
                        },

                        onClick = {

                            selectedPerson =
                                person

                            personMenu =
                                false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded =
                productMenu,

            onExpandedChange = {
                productMenu =
                    !productMenu
            }
        ) {

            OutlinedTextField(
                value =
                    selectedProduct
                        ?.name
                        ?: "",

                onValueChange = {},

                readOnly = true,

                label = {
                    Text("کالا")
                },

                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded =
                    productMenu,

                onDismissRequest = {
                    productMenu = false
                }
            ) {

                products.forEach {
                        product ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                product.name
                            )
                        },

                        onClick = {

                            selectedProduct =
                                product

                            productMenu =
                                false
                        }
                    )
                }
            }
        }

        NumberField(
            label = "تعداد",
            value =
                quantityText,

            onChange = {
                quantityText = it
            }
        )

        NumberField(
            label =
                "قیمت خرید واحد دلار",

            value =
                priceText,

            onChange = {
                priceText = it
            }
        )

        val quantity =
            quantityText
                .toIntOrNull()

        val price =
            priceText
                .toDoubleOrNull()

        if (
            quantity != null &&
            price != null
        ) {

            MetricCard(
                title = "جمع خرید",

                value =
                    "$${
                        money.format(
                            quantity *
                                price
                        )
                    }"
            )
        }

        Button(
            onClick = {

                val q =
                    quantityText
                        .toIntOrNull()

                val p =
                    priceText
                        .toDoubleOrNull()

                if (
                    selectedPerson != null &&
                    selectedProduct != null &&
                    q != null &&
                    q > 0 &&
                    p != null &&
                    p >= 0
                ) {

                    onSave(
                        selectedPerson!!,
                        selectedProduct!!,
                        q,
                        p
                    )
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                Icons.Default.CheckCircle,
                null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text("ثبت خرید")
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun SellPage(
    products: List<Product>,
    purchases: List<Purchase>,
    sales: List<Sale>,
    onSave:
        (
            String,
            Product,
            Int,
            Double
        ) -> Unit
) {

    var customer by remember {
        mutableStateOf("")
    }

    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }

    var productMenu by remember {
        mutableStateOf(false)
    }

    var quantityText by remember {
        mutableStateOf("")
    }

    var priceText by remember {
        mutableStateOf("")
    }

    val available =
        selectedProduct?.let {

            productQuantity(
                it.id,
                purchases,
                sales
            )

        } ?: 0

    val quantity =
        quantityText.toIntOrNull()

    val price =
        priceText.toDoubleOrNull()

    val tooMuch =
        quantity != null &&
            quantity > available

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        PageHeader(
            title = "ثبت فروش",
            subtitle =
                "خروج کالا از موجودی"
        )

        if (products.isEmpty()) {

            EmptyCard(
                "ابتدا حداقل یک کالا تعریف کنید"
            )

            return@Column
        }

        OutlinedTextField(
            value = customer,

            onValueChange = {
                customer = it
            },

            label = {
                Text("نام خریدار")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            shape =
                RoundedCornerShape(18.dp)
        )

        ExposedDropdownMenuBox(
            expanded =
                productMenu,

            onExpandedChange = {
                productMenu =
                    !productMenu
            }
        ) {

            OutlinedTextField(
                value =
                    selectedProduct
                        ?.name
                        ?: "",

                onValueChange = {},

                readOnly = true,

                label = {
                    Text("کالا")
                },

                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded =
                    productMenu,

                onDismissRequest = {
                    productMenu = false
                }
            ) {

                products.forEach {
                        product ->

                    val stock =
                        productQuantity(
                            product.id,
                            purchases,
                            sales
                        )

                    DropdownMenuItem(
                        text = {

                            Text(
                                "${product.name} • موجودی $stock"
                            )
                        },

                        onClick = {

                            selectedProduct =
                                product

                            productMenu =
                                false
                        }
                    )
                }
            }
        }

        MetricCard(
            title =
                "موجودی قابل فروش",

            value =
                "$available عدد"
        )

        NumberField(
            label =
                "تعداد فروش",

            value =
                quantityText,

            onChange = {
                quantityText = it
            }
        )

        NumberField(
            label =
                "قیمت فروش واحد دلار",

            value =
                priceText,

            onChange = {
                priceText = it
            }
        )

        if (tooMuch) {

            Text(
                "تعداد فروش از موجودی بیشتر است.",
                color =
                    MaterialTheme
                        .colorScheme
                        .error,
                fontWeight =
                    FontWeight.Bold
            )
        }

        if (
            quantity != null &&
            price != null
        ) {

            MetricCard(
                title = "جمع فروش",

                value =
                    "$${
                        money.format(
                            quantity *
                                price
                        )
                    }"
            )
        }

        Button(
            onClick = {

                if (
                    customer.isNotBlank() &&
                    selectedProduct != null &&
                    quantity != null &&
                    quantity > 0 &&
                    quantity <= available &&
                    price != null &&
                    price >= 0
                ) {

                    onSave(
                        customer.trim(),
                        selectedProduct!!,
                        quantity,
                        price
                    )
                }
            },

            enabled =
                !tooMuch,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                Icons.Default.PointOfSale,
                null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text("ثبت فروش")
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PaymentPage(
    people: List<Person>,
    onSave:
        (
            Person,
            Double
        ) -> Unit
) {

    var selectedPerson by remember {
        mutableStateOf<Person?>(null)
    }

    var personMenu by remember {
        mutableStateOf(false)
    }

    var amountText by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        PageHeader(
            title =
                "ثبت پرداخت",
            subtitle =
                "پرداخت به تأمین‌کننده"
        )

        if (people.isEmpty()) {

            EmptyCard(
                "ابتدا شخص مورد نظر را تعریف کنید"
            )

            return@Column
        }

        ExposedDropdownMenuBox(
            expanded =
                personMenu,

            onExpandedChange = {
                personMenu =
                    !personMenu
            }
        ) {

            OutlinedTextField(
                value =
                    selectedPerson
                        ?.fullName
                        ?: "",

                onValueChange = {},

                readOnly = true,

                label = {
                    Text(
                        "تأمین‌کننده"
                    )
                },

                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded =
                    personMenu,

                onDismissRequest = {
                    personMenu = false
                }
            ) {

                people.forEach {
                        person ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                person.fullName
                            )
                        },

                        onClick = {

                            selectedPerson =
                                person

                            personMenu =
                                false
                        }
                    )
                }
            }
        }

        NumberField(
            label =
                "مبلغ پرداختی دلار",

            value =
                amountText,

            onChange = {
                amountText = it
            }
        )

        Button(
            onClick = {

                val amount =
                    amountText
                        .toDoubleOrNull()

                if (
                    selectedPerson != null &&
                    amount != null &&
                    amount > 0
                ) {

                    onSave(
                        selectedPerson!!,
                        amount
                    )
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Icon(
                Icons.Default.Payments,
                null
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text("ثبت پرداخت")
        }
    }
}

@Composable
fun ProductsPage(
    products: MutableList<Product>,
    purchases: List<Purchase>,
    sales: List<Sale>,
    storage: AppStorage,
    onProductClick: (Long) -> Unit
) {

    var productName by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        PageHeader(
            title = "کالاها",
            subtitle =
                "موجودی واقعی"
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value =
                    productName,

                onValueChange = {
                    productName = it
                },

                label = {
                    Text(
                        "نام کالای جدید"
                    )
                },

                singleLine = true,

                modifier =
                    Modifier.weight(1f)
            )

            IconButton(
                onClick = {

                    val name =
                        productName.trim()

                    if (
                        name.isNotBlank() &&
                        products.none {
                            it.name.equals(
                                name,
                                true
                            )
                        }
                    ) {

                        products.add(
                            Product(
                                id =
                                    System.currentTimeMillis(),

                                name = name
                            )
                        )

                        storage.saveProducts(
                            products
                        )

                        productName = ""
                    }
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )
            }
        }

        if (products.isEmpty()) {

            EmptyCard(
                "هنوز کالایی تعریف نشده"
            )

        } else {

            LazyColumn(
                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    products,
                    key = {
                        it.id
                    }
                ) { product ->

                    val quantity =
                        productQuantity(
                            product.id,
                            purchases,
                            sales
                        )

                    val average =
                        productAverage(
                            product.id,
                            purchases
                        )

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {

                                    onProductClick(
                                        product.id
                                    )
                                },

                        shape =
                            RoundedCornerShape(
                                22.dp
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        16.dp
                                    ),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.ChevronLeft,
                                null
                            )

                            Column(
                                horizontalAlignment =
                                    Alignment.End
                            ) {

                                Text(
                                    product.name,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    "موجودی $quantity" +
                                        " • میانگین خرید $" +
                                        money.format(
                                            average
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeoplePage(
    people: MutableList<Person>,
    purchases: List<Purchase>,
    payments: List<Payment>,
    storage: AppStorage
) {

    var firstName by remember {
        mutableStateOf("")
    }

    var lastName by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        PageHeader(
            title = "اشخاص",
            subtitle =
                "تأمین‌کنندگان و مانده بدهی"
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(6.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value =
                    firstName,

                onValueChange = {
                    firstName = it
                },

                label = {
                    Text("نام")
                },

                singleLine = true,

                modifier =
                    Modifier.weight(1f)
            )

            OutlinedTextField(
                value =
                    lastName,

                onValueChange = {
                    lastName = it
                },

                label = {
                    Text("فامیل")
                },

                singleLine = true,

                modifier =
                    Modifier.weight(1f)
            )

            IconButton(
                onClick = {

                    if (
                        firstName.isNotBlank() ||
                        lastName.isNotBlank()
                    ) {

                        people.add(
                            Person(
                                id =
                                    System.currentTimeMillis(),

                                firstName =
                                    firstName.trim(),

                                lastName =
                                    lastName.trim()
                            )
                        )

                        storage.savePeople(
                            people
                        )

                        firstName = ""
                        lastName = ""
                    }
                }
            ) {

                Icon(
                    Icons.Default.PersonAdd,
                    null
                )
            }
        }

        LazyColumn(
            modifier =
                Modifier.weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            items(
                people,
                key = {
                    it.id
                }
            ) { person ->

                val debt =
                    personDebt(
                        person.id,
                        purchases,
                        payments
                    )

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                "$${
                                    money.format(
                                        debt
                                    )
                                }",
                                fontWeight =
                                    FontWeight.Black
                            )

                            Text(
                                "مانده بدهی",
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }

                        Text(
                            person.fullName,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailPage(
    product: Product?,
    people: List<Person>,
    purchases: List<Purchase>,
    sales: List<Sale>,
    onBack: () -> Unit
) {

    if (product == null) {

        EmptyCard(
            "کالا پیدا نشد"
        )

        return
    }

    data class ProductHistory(
        val id: String,
        val time: Long,
        val title: String,
        val info: String,
        val type: String
    )

    val history =

        purchases
            .filter {
                it.productId ==
                    product.id
            }
            .map { purchase ->

                val person =
                    people.find {
                        it.id ==
                            purchase.personId
                    }

                ProductHistory(
                    id =
                        "buy_${purchase.id}",

                    time =
                        purchase.createdAt,

                    title =
                        "خرید از ${
                            person?.fullName
                                ?: "شخص نامشخص"
                        }",

                    info =
                        "${purchase.quantity} عدد × " +
                            "$${
                                money.format(
                                    purchase.unitPrice
                                )
                            }",

                    type = "buy"
                )
            } +

            sales
                .filter {
                    it.productId ==
                        product.id
                }
                .map { sale ->

                    ProductHistory(
                        id =
                            "sale_${sale.id}",

                        time =
                            sale.createdAt,

                        title =
                            "فروش به ${sale.customer}",

                        info =
                            "${sale.quantity} عدد × " +
                                "$${
                                    money.format(
                                        sale.unitPrice
                                    )
                                }",

                        type = "sale"
                    )
                }

    val sortedHistory =
        history.sortedByDescending {
            it.time
        }

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(10.dp),

        contentPadding =
            PaddingValues(
                bottom = 16.dp
            )
    ) {

        item {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    Icons.Default.ArrowBack,
                    null
                )
            }

            PageHeader(
                title =
                    product.name,

                subtitle =
                    "تاریخچه ورود و خروج"
            )
        }

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                MetricCard(
                    title = "موجودی",

                    value =
                        "${
                            productQuantity(
                                product.id,
                                purchases,
                                sales
                            )
                        } عدد",

                    modifier =
                        Modifier.weight(1f)
                )

                MetricCard(
                    title =
                        "میانگین خرید",

                    value =
                        "$${
                            money.format(
                                productAverage(
                                    product.id,
                                    purchases
                                )
                            )
                        }",

                    modifier =
                        Modifier.weight(1f)
                )
            }
        }

        item {

            Text(
                "گردش کالا",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        }

        if (
            sortedHistory.isEmpty()
        ) {

            item {

                EmptyCard(
                    "هنوز خرید یا فروشی برای این کالا ثبت نشده"
                )
            }

        } else {

            items(
                sortedHistory,
                key = {
                    it.id
                }
            ) { historyItem ->

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            20.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    16.dp
                                ),

                        horizontalAlignment =
                            Alignment.End
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                if (
                                    historyItem.type ==
                                    "buy"
                                ) {
                                    Icons.Default
                                        .ArrowDownward
                                } else {
                                    Icons.Default
                                        .ArrowUpward
                                },

                                null
                            )

                            Spacer(
                                Modifier.width(
                                    8.dp
                                )
                            )

                            Text(
                                historyItem.title,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            Modifier.height(
                                6.dp
                            )
                        )

                        Text(
                            historyItem.info
                        )

                        Text(
                            dateFormat.format(
                                Date(
                                    historyItem.time
                                )
                            ),

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,

        onValueChange =
            onChange,

        label = {
            Text(label)
        },

        modifier =
            Modifier.fillMaxWidth(),

        singleLine = true,

        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Decimal
            ),

        shape =
            RoundedCornerShape(
                18.dp
            )
    )
}
