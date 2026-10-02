package com.dollaraccounting.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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

private val money =
    DecimalFormat("#,##0.00")

private val dateFormat =
    SimpleDateFormat(
        "yyyy/MM/dd HH:mm",
        Locale.US
    )

class AppStorage(context: Context) {

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

                Purchase(
                    id =
                        p[0].toLongOrNull()
                            ?: return@mapNotNull null,

                    personId =
                        p[1].toLongOrNull()
                            ?: return@mapNotNull null,

                    productId =
                        p[2].toLongOrNull()
                            ?: return@mapNotNull null,

                    quantity =
                        p[3].toIntOrNull()
                            ?: return@mapNotNull null,

                    unitPrice =
                        p[4].toDoubleOrNull()
                            ?: return@mapNotNull null,

                    createdAt =
                        p[5].toLongOrNull()
                            ?: return@mapNotNull null
                )
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

                Payment(
                    id =
                        p[0].toLongOrNull()
                            ?: return@mapNotNull null,

                    personId =
                        p[1].toLongOrNull()
                            ?: return@mapNotNull null,

                    amount =
                        p[2].toDoubleOrNull()
                            ?: return@mapNotNull null,

                    createdAt =
                        p[3].toLongOrNull()
                            ?: return@mapNotNull null
                )
            }
    }
}

private fun productQuantity(
    productId: Long,
    purchases: List<Purchase>
): Int {

    return purchases
        .filter {
            it.productId == productId
        }
        .sumOf {
            it.quantity
        }
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
                        Text("کالاها")
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
                    .padding(16.dp)
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
                            payments = payments
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
                            storage = storage,
                            onProductClick = {

                                selectedProductId =
                                    it

                                page =
                                    "productDetail"
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

                            purchases =
                                purchases,

                            onBack = {
                                page =
                                    "products"
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
                                    System
                                        .currentTimeMillis()

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

                    "pay" -> {

                        PaymentPage(
                            people = people,

                            onSave = {
                                    person,
                                    amount ->

                                val time =
                                    System
                                        .currentTimeMillis()

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

                                storage
                                    .savePayments(
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
    payments: List<Payment>
) {

    val totalPurchases =
        purchases.sumOf {
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
                    "داشبورد"
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
                    title = "کل پرداخت",
                    value =
                        "$${
                            money.format(
                                totalPayments
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
                    "مانده کل بدهی",

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
                        purchases
                    )

                val average =
                    productAverage(
                        product.id,
                        purchases
                    )

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            22.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                        horizontalArrangement =
                            Arrangement
                                .SpaceBetween,

                        verticalAlignment =
                            Alignment
                                .CenterVertically
                    ) {

                        Column {

                            Text(
                                "$${
                                    money.format(
                                        average
                                    )
                                }",
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                "میانگین خرید",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall
                            )
                        }

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
                                "موجودی: " +
                                    "$quantity عدد",
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
                    "مانده تأمین‌کنندگان",
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

                val balance =
                    personDebt(
                        person.id,
                        purchases,
                        payments
                    )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),

                    horizontalArrangement =
                        Arrangement
                            .SpaceBetween
                ) {

                    Text(
                        "$${
                            money.format(
                                balance
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

@Composable
fun PeoplePage(
    people: MutableList<Person>,
    purchases: List<Purchase>,
    payments: List<Payment>,
    storage: AppStorage
) {

    var showAdd by remember {
        mutableStateOf(false)
    }

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

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Button(
                onClick = {
                    showAdd =
                        !showAdd
                }
            ) {

                Icon(
                    Icons.Default.PersonAdd,
                    null
                )

                Spacer(
                    Modifier.width(5.dp)
                )

                Text("افزودن")
            }

            PageHeader(
                "اشخاص"
            )
        }

        AnimatedVisibility(
            visible = showAdd,
            enter =
                fadeIn() +
                    slideInVertically(),
            exit =
                fadeOut() +
                    slideOutVertically()
        ) {

            Card(
                shape =
                    RoundedCornerShape(
                        22.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            14.dp
                        ),

                    verticalArrangement =
                        Arrangement
                            .spacedBy(
                                8.dp
                            )
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

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    )

                    OutlinedTextField(
                        value =
                            lastName,

                        onValueChange = {
                            lastName = it
                        },

                        label = {
                            Text(
                                "نام خانوادگی"
                            )
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    )

                    Button(
                        onClick = {

                            if (
                                firstName
                                    .isNotBlank() ||
                                lastName
                                    .isNotBlank()
                            ) {

                                people.add(
                                    Person(
                                        id =
                                            System
                                                .currentTimeMillis(),

                                        firstName =
                                            firstName
                                                .trim(),

                                        lastName =
                                            lastName
                                                .trim()
                                    )
                                )

                                storage
                                    .savePeople(
                                        people
                                    )

                                firstName = ""
                                lastName = ""
                                showAdd = false
                            }
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    ) {

                        Text("ذخیره")
                    }
                }
            }
        }

        if (people.isEmpty()) {

            EmptyCard(
                "هنوز شخصی تعریف نشده"
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement
                        .spacedBy(8.dp)
            ) {

                items(
                    people,
                    key = {
                        it.id
                    }
                ) { person ->

                    val balance =
                        personDebt(
                            person.id,
                            purchases,
                            payments
                        )

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth(),

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

                            Column {

                                Text(
                                    "$${
                                        money.format(
                                            balance
                                        )
                                    }",
                                    fontWeight =
                                        FontWeight
                                            .Black
                                )

                                Text(
                                    "مانده بدهی",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall
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
}

@Composable
fun ProductsPage(
    products: MutableList<Product>,
    purchases: List<Purchase>,
    storage: AppStorage,
    onProductClick: (Long) -> Unit
) {

    var showAdd by remember {
        mutableStateOf(false)
    }

    var name by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Button(
                onClick = {
                    showAdd =
                        !showAdd
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    null
                )

                Text(" کالای جدید")
            }

            PageHeader(
                "کالاها"
            )
        }

        AnimatedVisibility(
            visible = showAdd
        ) {

            Card(
                shape =
                    RoundedCornerShape(
                        22.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            14.dp
                        ),

                    verticalArrangement =
                        Arrangement
                            .spacedBy(
                                8.dp
                            )
                ) {

                    OutlinedTextField(
                        value = name,

                        onValueChange = {
                            name = it
                        },

                        label = {
                            Text("نام کالا")
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    )

                    Button(
                        onClick = {

                            val productName =
                                name.trim()

                            if (
                                productName
                                    .isNotBlank() &&
                                products.none {
                                    it.name.equals(
                                        productName,
                                        true
                                    )
                                }
                            ) {

                                products.add(
                                    Product(
                                        id =
                                            System
                                                .currentTimeMillis(),

                                        name =
                                            productName
                                    )
                                )

                                storage
                                    .saveProducts(
                                        products
                                    )

                                name = ""
                                showAdd = false
                            }
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    ) {

                        Text("ذخیره")
                    }
                }
            }
        }

        if (products.isEmpty()) {

            EmptyCard(
                "هنوز کالایی تعریف نشده"
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement
                        .spacedBy(8.dp)
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
                            purchases
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
                                        FontWeight
                                            .Bold
                                )

                                Text(
                                    "موجودی " +
                                        "$quantity" +
                                        " • میانگین $" +
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
fun ProductDetailPage(
    product: Product?,
    people: List<Person>,
    purchases: List<Purchase>,
    onBack: () -> Unit
) {

    if (product == null) {

        EmptyCard(
            "کالا پیدا نشد"
        )

        return
    }

    val history =
        purchases
            .filter {
                it.productId ==
                    product.id
            }
            .sortedByDescending {
                it.createdAt
            }

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        Icons.Default
                            .ArrowBack,
                        null
                    )
                }

                PageHeader(
                    title =
                        product.name,
                    subtitle =
                        "تاریخچه خرید"
                )
            }
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
                                purchases
                            )
                        } عدد",

                    modifier =
                        Modifier.weight(1f)
                )

                MetricCard(
                    title = "میانگین",

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
                "خریدهای این کالا",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        }

        if (history.isEmpty()) {

            item {

                EmptyCard(
                    "هنوز خریدی برای این کالا ثبت نشده"
                )
            }

        } else {

            items(
                history,
                key = {
                    it.id
                }
            ) { purchase ->

                val person =
                    people.find {
                        it.id ==
                            purchase.personId
                    }

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth(),

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
                            person?.fullName
                                ?: "شخص نامشخص",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            "${purchase.quantity} عدد × " +
                                "$${
                                    money.format(
                                        purchase.unitPrice
                                    )
                                }"
                        )

                        Text(
                            "جمع: $" +
                                money.format(
                                    purchase.quantity *
                                        purchase.unitPrice
                                )
                        )

                        Text(
                            dateFormat.format(
                                Date(
                                    purchase.createdAt
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
                "خرید دلاری جدید"
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
                "قیمت واحد دلار",

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
                Icons.Default
                    .CheckCircle,
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
