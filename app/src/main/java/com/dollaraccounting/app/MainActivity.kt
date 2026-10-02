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

/* -------------------- DATA -------------------- */

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

data class InventoryResult(
    val quantity: Int,
    val average: Double,
    val value: Double
)

private val money =
    DecimalFormat("#,##0.00")

private val dateFormat =
    SimpleDateFormat(
        "yyyy/MM/dd HH:mm",
        Locale.US
    )

/* -------------------- STORAGE -------------------- */

class AppStorage(context: Context) {

    /*
     * این اسم عمداً همان v4 مانده.
     * تغییرش نده تا اطلاعات فعلی حفظ شود.
     */
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
            prefs.getString(key, "")
                ?: ""

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
            .putString(
                "people",
                text
            )
            .apply()
    }

    fun loadPeople(): List<Person> {

        return rows("people")
            .mapNotNull { row ->

                val p =
                    row.split("|")

                if (p.size < 3) {
                    return@mapNotNull null
                }

                val id =
                    p[0].toLongOrNull()
                        ?: return@mapNotNull null

                Person(
                    id = id,
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
            .putString(
                "products",
                text
            )
            .apply()
    }

    fun loadProducts(): List<Product> {

        return rows("products")
            .mapNotNull { row ->

                val p =
                    row.split("|")

                if (p.size < 2) {
                    return@mapNotNull null
                }

                val id =
                    p[0].toLongOrNull()
                        ?: return@mapNotNull null

                Product(
                    id = id,
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
            .putString(
                "purchases",
                text
            )
            .apply()
    }

    fun loadPurchases(): List<Purchase> {

        return rows("purchases")
            .mapNotNull { row ->

                val p =
                    row.split("|")

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
            .putString(
                "payments",
                text
            )
            .apply()
    }

    fun loadPayments(): List<Payment> {

        return rows("payments")
            .mapNotNull { row ->

                val p =
                    row.split("|")

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
            .putString(
                "sales",
                text
            )
            .apply()
    }

    fun loadSales(): List<Sale> {

        return rows("sales")
            .mapNotNull { row ->

                val p =
                    row.split("|")

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

/* -------------------- INVENTORY -------------------- */

/*
 * قلب محاسبات برنامه
 *
 * خرید:
 * موجودی و ارزش موجودی زیاد می‌شود.
 *
 * فروش:
 * تعداد کم می‌شود.
 * ارزش کالا با میانگین همان لحظه کم می‌شود.
 *
 * قیمت فروش هیچ اثری روی میانگین خرید ندارد.
 */
private fun calculateInventory(
    productId: Long,
    purchases: List<Purchase>,
    sales: List<Sale>
): InventoryResult {

    data class Event(
        val time: Long,
        val id: Long,
        val isPurchase: Boolean,
        val quantity: Int,
        val unitPrice: Double
    )

    val events =
        mutableListOf<Event>()

    purchases
        .filter {
            it.productId == productId
        }
        .forEach {

            events.add(
                Event(
                    time = it.createdAt,
                    id = it.id,
                    isPurchase = true,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice
                )
            )
        }

    sales
        .filter {
            it.productId == productId
        }
        .forEach {

            events.add(
                Event(
                    time = it.createdAt,
                    id = it.id,
                    isPurchase = false,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice
                )
            )
        }

    /*
     * ترتیب زمانی خیلی مهم است.
     */
    val sortedEvents =
        events.sortedWith(
            compareBy<Event> {
                it.time
            }.thenBy {
                it.id
            }
        )

    var quantity = 0
    var inventoryValue = 0.0
    var average = 0.0

    sortedEvents.forEach { event ->

        if (event.isPurchase) {

            /*
             * ارزش خرید جدید به ارزش موجودی قبلی اضافه می‌شود.
             */
            val purchaseValue =
                event.quantity *
                    event.unitPrice

            inventoryValue +=
                purchaseValue

            quantity +=
                event.quantity

            average =
                if (quantity > 0) {
                    inventoryValue /
                        quantity
                } else {
                    0.0
                }

        } else {

            /*
             * فروش باید با میانگین خرید همان لحظه
             * از ارزش موجودی کم شود.
             */
            val quantityToRemove =
                event.quantity.coerceAtMost(
                    quantity
                )

            inventoryValue -=
                quantityToRemove *
                    average

            quantity -=
                quantityToRemove

            if (quantity <= 0) {

                quantity = 0
                inventoryValue = 0.0
                average = 0.0

            } else {

                /*
                 * فروش میانگین را تغییر نمی‌دهد.
                 */
                average =
                    inventoryValue /
                        quantity
            }
        }
    }

    if (
        inventoryValue < 0.000001
    ) {
        inventoryValue = 0.0
    }

    return InventoryResult(
        quantity = quantity,
        average = average,
        value = inventoryValue
    )
}

private fun productQuantity(
    productId: Long,
    purchases: List<Purchase>,
    sales: List<Sale>
): Int {

    return calculateInventory(
        productId,
        purchases,
        sales
    ).quantity
}

private fun productAverage(
    productId: Long,
    purchases: List<Purchase>,
    sales: List<Sale>
): Double {

    return calculateInventory(
        productId,
        purchases,
        sales
    ).average
}

private fun personDebt(
    personId: Long,
    purchases: List<Purchase>,
    payments: List<Payment>
): Double {

    val bought =
        purchases
            .filter {
                it.personId ==
                    personId
            }
            .sumOf {
                it.quantity *
                    it.unitPrice
            }

    val paid =
        payments
            .filter {
                it.personId ==
                    personId
            }
            .sumOf {
                it.amount
            }

    return bought - paid
}

/* -------------------- APP -------------------- */

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
                    selected =
                        page == "home",

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
                    selected =
                        page == "buy",

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
                    selected =
                        page == "sell",

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
                    selected =
                        page == "pay",

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
                    selected =
                        page == "products",

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
                    selected =
                        page == "people",

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
                                        personId =
                                            person.id,
                                        productId =
                                            product.id,
                                        quantity =
                                            quantity,
                                        unitPrice =
                                            price,
                                        createdAt =
                                            time
                                    )
                                )

                                storage
                                    .savePurchases(
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
                                        customer =
                                            customer,
                                        productId =
                                            product.id,
                                        quantity =
                                            quantity,
                                        unitPrice =
                                            price,
                                        createdAt =
                                            time
                                    )
                                )

                                storage.saveSales(
                                    sales
                                )
                            }
                        )
                    }

                    "pay" -> {

                        PaymentPage(
                            people = people,
                            payments = payments,

                            onSave = {
                                    person,
                                    amount ->

                                val time =
                                    System.currentTimeMillis()

                                payments.add(
                                    Payment(
                                        id = time,
                                        personId =
                                            person.id,
                                        amount =
                                            amount,
                                        createdAt =
                                            time
                                    )
                                )

                                storage.savePayments(
                                    payments
                                )
                            }
                        )
                    }

                    "products" -> {

                        ProductsPage(
                            products = products,
                            purchases = purchases,
                            sales = sales,
                            storage = storage,

                            onProductClick = {

                                selectedProductId =
                                    it

                                page =
                                    "productDetail"
                            }
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
                                page =
                                    "products"
                            }
                        )
                    }
                }
            }
        }
    }
}

/* -------------------- COMMON UI -------------------- */

@Composable
fun PageHeader(
    title: String,
    subtitle: String = ""
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

        if (
            subtitle.isNotBlank()
        ) {

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
            RoundedCornerShape(
                22.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                title,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(
                    5.dp
                )
            )

            Text(
                value,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

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
            Modifier.fillMaxWidth()
    ) {

        Text(
            text,

            modifier =
                Modifier.padding(
                    16.dp
                )
        )
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

        singleLine = true,

        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Decimal
            ),

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            )
    )
}

/* -------------------- HOME -------------------- */

@Composable
fun HomePage(
    people: List<Person>,
    products: List<Product>,
    purchases: List<Purchase>,
    payments: List<Payment>,
    sales: List<Sale>
) {

    val totalBuy =
        purchases.sumOf {
            it.quantity *
                it.unitPrice
        }

    val totalSale =
        sales.sumOf {
            it.quantity *
                it.unitPrice
        }

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 16.dp
            )
    ) {

        item {

            PageHeader(
                "حسابداری دلاری",
                "خرید، فروش و موجودی واقعی"
            )
        }

        item {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                MetricCard(
                    "کل خرید",

                    "$${
                        money.format(
                            totalBuy
                        )
                    }",

                    Modifier.weight(1f)
                )

                MetricCard(
                    "کل فروش",

                    "$${
                        money.format(
                            totalSale
                        )
                    }",

                    Modifier.weight(1f)
                )
            }
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

                val inventory =
                    calculateInventory(
                        product.id,
                        purchases,
                        sales
                    )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            20.dp
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
                            Arrangement
                                .SpaceBetween
                    ) {

                        Text(
                            "میانگین $${money.format(inventory.average)}"
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
                                "موجودی: ${inventory.quantity} عدد",

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

        if (
            people.isNotEmpty()
        ) {

            item {

                Text(
                    "مانده تأمین‌کنندگان",

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

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                8.dp
                            ),

                    horizontalArrangement =
                        Arrangement
                            .SpaceBetween
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
                            FontWeight.Bold
                    )

                    Text(
                        person.fullName
                    )
                }
            }
        }
    }
}

/* -------------------- BUY -------------------- */

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

    var person by remember {
        mutableStateOf<Person?>(null)
    }

    var product by remember {
        mutableStateOf<Product?>(null)
    }

    var personMenu by remember {
        mutableStateOf(false)
    }

    var productMenu by remember {
        mutableStateOf(false)
    }

    var qty by remember {
        mutableStateOf("")
    }

    var price by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        PageHeader(
            "ثبت خرید",
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
                    person?.fullName
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

                people.forEach { p ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                p.fullName
                            )
                        },

                        onClick = {
                            person = p
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
                    product?.name
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
                    productMenu =
                        false
                }
            ) {

                products.forEach { p ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                p.name
                            )
                        },

                        onClick = {
                            product = p
                            productMenu =
                                false
                        }
                    )
                }
            }
        }

        NumberField(
            "تعداد",
            qty
        ) {
            qty = it
        }

        NumberField(
            "قیمت خرید واحد دلار",
            price
        ) {
            price = it
        }

        val q =
            qty.toIntOrNull()

        val p =
            price.toDoubleOrNull()

        if (
            q != null &&
            p != null
        ) {

            MetricCard(
                "جمع خرید",

                "$${
                    money.format(
                        q * p
                    )
                }"
            )
        }

        Button(
            onClick = {

                if (
                    person != null &&
                    product != null &&
                    q != null &&
                    q > 0 &&
                    p != null &&
                    p >= 0
                ) {

                    onSave(
                        person!!,
                        product!!,
                        q,
                        p
                    )
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("ثبت خرید")
        }
    }
}

/* -------------------- SELL + REPORT -------------------- */

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

    var product by remember {
        mutableStateOf<Product?>(null)
    }

    var menu by remember {
        mutableStateOf(false)
    }

    var qty by remember {
        mutableStateOf("")
    }

    var price by remember {
        mutableStateOf("")
    }

    val available =
        product?.let {

            productQuantity(
                it.id,
                purchases,
                sales
            )

        } ?: 0

    val q =
        qty.toIntOrNull()

    val p =
        price.toDoubleOrNull()

    val tooMuch =
        q != null &&
            q > available

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 20.dp
            )
    ) {

        item {

            PageHeader(
                "ثبت فروش",
                "خروج کالا از موجودی"
            )
        }

        if (
            products.isEmpty()
        ) {

            item {

                EmptyCard(
                    "ابتدا کالا تعریف کنید"
                )
            }

            return@LazyColumn
        }

        item {

            OutlinedTextField(
                value = customer,

                onValueChange = {
                    customer = it
                },

                label = {
                    Text(
                        "نام خریدار"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )
        }

        item {

            ExposedDropdownMenuBox(
                expanded = menu,

                onExpandedChange = {
                    menu = !menu
                }
            ) {

                OutlinedTextField(
                    value =
                        product?.name
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
                    expanded = menu,

                    onDismissRequest = {
                        menu = false
                    }
                ) {

                    products.forEach { x ->

                        val stock =
                            productQuantity(
                                x.id,
                                purchases,
                                sales
                            )

                        DropdownMenuItem(
                            text = {

                                Text(
                                    "${x.name} • موجودی $stock"
                                )
                            },

                            onClick = {

                                product = x
                                menu = false
                            }
                        )
                    }
                }
            }
        }

        item {

            MetricCard(
                "موجودی قابل فروش",
                "$available عدد"
            )
        }

        item {

            NumberField(
                "تعداد فروش",
                qty
            ) {
                qty = it
            }
        }

        item {

            NumberField(
                "قیمت فروش واحد دلار",
                price
            ) {
                price = it
            }
        }

        if (tooMuch) {

            item {

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
        }

        if (
            q != null &&
            p != null
        ) {

            item {

                MetricCard(
                    "جمع فروش",

                    "$${
                        money.format(
                            q * p
                        )
                    }"
                )
            }
        }

        item {

            Button(
                onClick = {

                    if (
                        customer.isNotBlank() &&
                        product != null &&
                        q != null &&
                        q > 0 &&
                        q <= available &&
                        p != null &&
                        p >= 0
                    ) {

                        onSave(
                            customer.trim(),
                            product!!,
                            q,
                            p
                        )

                        customer = ""
                        product = null
                        qty = ""
                        price = ""
                    }
                },

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    !tooMuch
            ) {

                Icon(
                    Icons.Default.PointOfSale,
                    null
                )

                Spacer(
                    Modifier.width(
                        8.dp
                    )
                )

                Text("ثبت فروش")
            }
        }

        item {

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Text(
                "گزارش فروش‌ها",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Black
            )
        }

        if (
            sales.isEmpty()
        ) {

            item {

                EmptyCard(
                    "هنوز فروشی ثبت نشده"
                )
            }

        } else {

            items(
                sales.sortedByDescending {
                    it.createdAt
                },

                key = {
                    it.id
                }
            ) { sale ->

                val soldProduct =
                    products.find {
                        it.id ==
                            sale.productId
                    }

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

                        Text(
                            soldProduct?.name
                                ?: "کالای نامشخص",

                            fontWeight =
                                FontWeight.Black
                        )

                        Text(
                            "خریدار: ${sale.customer}"
                        )

                        Text(
                            "${sale.quantity} عدد × $${money.format(sale.unitPrice)}"
                        )

                        Text(
                            "جمع: $${money.format(sale.quantity * sale.unitPrice)}",

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            dateFormat.format(
                                Date(
                                    sale.createdAt
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

/* -------------------- PAYMENT + REPORT -------------------- */

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PaymentPage(
    people: List<Person>,
    payments: List<Payment>,
    onSave:
        (
            Person,
            Double
        ) -> Unit
) {

    var person by remember {
        mutableStateOf<Person?>(null)
    }

    var menu by remember {
        mutableStateOf(false)
    }

    var amount by remember {
        mutableStateOf("")
    }

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 20.dp
            )
    ) {

        item {

            PageHeader(
                "ثبت پرداخت",
                "پرداخت به تأمین‌کننده"
            )
        }

        if (
            people.isEmpty()
        ) {

            item {

                EmptyCard(
                    "ابتدا شخص تعریف کنید"
                )
            }

            return@LazyColumn
        }

        item {

            ExposedDropdownMenuBox(
                expanded = menu,

                onExpandedChange = {
                    menu = !menu
                }
            ) {

                OutlinedTextField(
                    value =
                        person?.fullName
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
                    expanded = menu,

                    onDismissRequest = {
                        menu = false
                    }
                ) {

                    people.forEach { x ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    x.fullName
                                )
                            },

                            onClick = {

                                person = x
                                menu = false
                            }
                        )
                    }
                }
            }
        }

        item {

            NumberField(
                "مبلغ پرداختی دلار",
                amount
            ) {
                amount = it
            }
        }

        item {

            Button(
                onClick = {

                    val a =
                        amount.toDoubleOrNull()

                    if (
                        person != null &&
                        a != null &&
                        a > 0
                    ) {

                        onSave(
                            person!!,
                            a
                        )

                        person = null
                        amount = ""
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
                    Modifier.width(
                        8.dp
                    )
                )

                Text(
                    "ثبت پرداخت"
                )
            }
        }

        item {

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Text(
                "گزارش پرداخت‌ها",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Black
            )
        }

        if (
            payments.isEmpty()
        ) {

            item {

                EmptyCard(
                    "هنوز پرداختی ثبت نشده"
                )
            }

        } else {

            items(
                payments.sortedByDescending {
                    it.createdAt
                },

                key = {
                    it.id
                }
            ) { payment ->

                val paymentPerson =
                    people.find {
                        it.id ==
                            payment.personId
                    }

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

                        Text(
                            paymentPerson
                                ?.fullName
                                ?: "شخص نامشخص",

                            fontWeight =
                                FontWeight.Black
                        )

                        Text(
                            "پرداخت: $${money.format(payment.amount)}",

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            dateFormat.format(
                                Date(
                                    payment.createdAt
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

/* -------------------- PRODUCTS -------------------- */

@Composable
fun ProductsPage(
    products: MutableList<Product>,
    purchases: List<Purchase>,
    sales: List<Sale>,
    storage: AppStorage,
    onProductClick:
        (Long) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        PageHeader(
            "کالاها",
            "موجودی و میانگین خرید"
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                },

                label = {
                    Text(
                        "نام کالای جدید"
                    )
                },

                modifier =
                    Modifier.weight(1f),

                singleLine = true
            )

            IconButton(
                onClick = {

                    val n =
                        name.trim()

                    if (
                        n.isNotBlank() &&
                        products.none {
                            it.name.equals(
                                n,
                                true
                            )
                        }
                    ) {

                        products.add(
                            Product(
                                System.currentTimeMillis(),
                                n
                            )
                        )

                        storage.saveProducts(
                            products
                        )

                        name = ""
                    }
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )
            }
        }

        LazyColumn(
            modifier =
                Modifier.weight(1f),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            items(
                products,
                key = {
                    it.id
                }
            ) { product ->

                val inventory =
                    calculateInventory(
                        product.id,
                        purchases,
                        sales
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
                            20.dp
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
                            Arrangement
                                .SpaceBetween,

                        verticalAlignment =
                            Alignment
                                .CenterVertically
                    ) {

                        Icon(
                            Icons.Default
                                .ChevronLeft,
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
                                "موجودی ${inventory.quantity} • میانگین خرید $${money.format(inventory.average)}"
                            )

                            Text(
                                "ارزش موجودی $${money.format(inventory.value)}",

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
}

/* -------------------- PEOPLE -------------------- */

@Composable
fun PeoplePage(
    people: MutableList<Person>,
    purchases: List<Purchase>,
    payments: List<Payment>,
    storage: AppStorage
) {

    var first by remember {
        mutableStateOf("")
    }

    var last by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        PageHeader(
            "اشخاص",
            "مانده بدهی تأمین‌کنندگان"
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    6.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = first,

                onValueChange = {
                    first = it
                },

                label = {
                    Text("نام")
                },

                modifier =
                    Modifier.weight(1f),

                singleLine = true
            )

            OutlinedTextField(
                value = last,

                onValueChange = {
                    last = it
                },

                label = {
                    Text("فامیل")
                },

                modifier =
                    Modifier.weight(1f),

                singleLine = true
            )

            IconButton(
                onClick = {

                    if (
                        first.isNotBlank() ||
                        last.isNotBlank()
                    ) {

                        people.add(
                            Person(
                                System.currentTimeMillis(),
                                first.trim(),
                                last.trim()
                            )
                        )

                        storage.savePeople(
                            people
                        )

                        first = ""
                        last = ""
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
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

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
                                .padding(
                                    16.dp
                                ),

                        horizontalArrangement =
                            Arrangement
                                .SpaceBetween
                    ) {

                        Column {

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
                                "مانده بدهی"
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

/* -------------------- PRODUCT HISTORY -------------------- */

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

    data class History(
        val id: String,
        val time: Long,
        val title: String,
        val info: String
    )

    val history =

        purchases
            .filter {
                it.productId ==
                    product.id
            }
            .map {

                History(
                    id =
                        "buy_${it.id}",

                    time =
                        it.createdAt,

                    title =
                        "خرید از ${
                            people.find { person ->
                                person.id ==
                                    it.personId
                            }?.fullName
                                ?: "نامشخص"
                        }",

                    info =
                        "${it.quantity} عدد × $${money.format(it.unitPrice)}"
                )
            } +

            sales
                .filter {
                    it.productId ==
                        product.id
                }
                .map {

                    History(
                        id =
                            "sale_${it.id}",

                        time =
                            it.createdAt,

                        title =
                            "فروش به ${it.customer}",

                        info =
                            "${it.quantity} عدد × $${money.format(it.unitPrice)}"
                    )
                }

    val inventory =
        calculateInventory(
            product.id,
            purchases,
            sales
        )

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),

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
                product.name,
                "تاریخچه ورود و خروج"
            )
        }

        item {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                MetricCard(
                    "موجودی",

                    "${inventory.quantity} عدد",

                    Modifier.weight(1f)
                )

                MetricCard(
                    "میانگین خرید",

                    "$${
                        money.format(
                            inventory.average
                        )
                    }",

                    Modifier.weight(1f)
                )
            }
        }

        item {

            MetricCard(
                "ارزش موجودی فعلی",

                "$${
                    money.format(
                        inventory.value
                    )
                }"
            )
        }

        if (
            history.isEmpty()
        ) {

            item {

                EmptyCard(
                    "هنوز خرید یا فروشی ثبت نشده"
                )
            }

        } else {

            items(
                history.sortedByDescending {
                    it.time
                },

                key = {
                    it.id
                }
            ) { h ->

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

                        Text(
                            h.title,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            h.info
                        )

                        Text(
                            dateFormat.format(
                                Date(
                                    h.time
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
