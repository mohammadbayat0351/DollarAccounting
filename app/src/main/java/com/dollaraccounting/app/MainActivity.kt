package com.dollaraccounting.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
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
    val name: String,
    var quantity: Int = 0,
    var averagePrice: Double = 0.0
)

data class SupplierAccount(
    val personId: Long,
    var purchases: Double = 0.0,
    var payments: Double = 0.0
)

private val dollarFormat = DecimalFormat("#,##0.00")

class AppStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "dollar_accounting",
            Context.MODE_PRIVATE
        )

    fun savePeople(people: List<Person>) {
        val text = people.joinToString(";;") {
            "${it.id}|${clean(it.firstName)}|${clean(it.lastName)}"
        }
        prefs.edit().putString("people", text).apply()
    }

    fun loadPeople(): List<Person> {
        val text = prefs.getString("people", "") ?: ""

        if (text.isBlank()) return emptyList()

        return text.split(";;").mapNotNull { row ->
            val p = row.split("|")
            if (p.size < 3) return@mapNotNull null

            Person(
                id = p[0].toLongOrNull() ?: return@mapNotNull null,
                firstName = p[1],
                lastName = p[2]
            )
        }
    }

    fun saveProducts(products: List<Product>) {
        val text = products.joinToString(";;") {
            "${it.id}|${clean(it.name)}|${it.quantity}|${it.averagePrice}"
        }
        prefs.edit().putString("products", text).apply()
    }

    fun loadProducts(): List<Product> {
        val text = prefs.getString("products", "") ?: ""

        if (text.isBlank()) return emptyList()

        return text.split(";;").mapNotNull { row ->
            val p = row.split("|")
            if (p.size < 4) return@mapNotNull null

            Product(
                id = p[0].toLongOrNull() ?: return@mapNotNull null,
                name = p[1],
                quantity = p[2].toIntOrNull() ?: 0,
                averagePrice = p[3].toDoubleOrNull() ?: 0.0
            )
        }
    }

    fun saveAccounts(accounts: List<SupplierAccount>) {
        val text = accounts.joinToString(";;") {
            "${it.personId}|${it.purchases}|${it.payments}"
        }
        prefs.edit().putString("accounts", text).apply()
    }

    fun loadAccounts(): List<SupplierAccount> {
        val text = prefs.getString("accounts", "") ?: ""

        if (text.isBlank()) return emptyList()

        return text.split(";;").mapNotNull { row ->
            val p = row.split("|")
            if (p.size < 3) return@mapNotNull null

            SupplierAccount(
                personId =
                    p[0].toLongOrNull()
                        ?: return@mapNotNull null,
                purchases =
                    p[1].toDoubleOrNull() ?: 0.0,
                payments =
                    p[2].toDoubleOrNull() ?: 0.0
            )
        }
    }

    private fun clean(value: String): String {
        return value
            .replace("|", " ")
            .replace(";;", " ")
    }
}

@Composable
fun DollarAccountingApp(context: Context) {

    val storage = remember {
        AppStorage(context)
    }

    val people = remember {
        mutableStateListOf<Person>().apply {
            addAll(storage.loadPeople())
        }
    }

    val products = remember {
        mutableStateListOf<Product>().apply {
            addAll(storage.loadProducts())
        }
    }

    val accounts = remember {
        mutableStateListOf<SupplierAccount>().apply {
            addAll(storage.loadAccounts())
        }
    }

    var page by remember {
        mutableStateOf("home")
    }

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = page == "home",
                    onClick = { page = "home" },
                    icon = { Text("⌂") },
                    label = { Text("خانه") }
                )

                NavigationBarItem(
                    selected = page == "buy",
                    onClick = { page = "buy" },
                    icon = { Text("+") },
                    label = { Text("خرید") }
                )

                NavigationBarItem(
                    selected = page == "pay",
                    onClick = { page = "pay" },
                    icon = { Text("$") },
                    label = { Text("پرداخت") }
                )

                NavigationBarItem(
                    selected = page == "products",
                    onClick = { page = "products" },
                    icon = { Text("□") },
                    label = { Text("کالاها") }
                )

                NavigationBarItem(
                    selected = page == "people",
                    onClick = { page = "people" },
                    icon = { Text("●") },
                    label = { Text("اشخاص") }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "حسابداری دلاری",
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            when (page) {

                "home" -> HomePage(
                    people = people,
                    products = products,
                    accounts = accounts
                )

                "buy" -> BuyPage(
                    people = people,
                    products = products,
                    accounts = accounts,
                    storage = storage,
                    onDone = {
                        page = "home"
                    }
                )

                "pay" -> PaymentPage(
                    people = people,
                    accounts = accounts,
                    storage = storage,
                    onDone = {
                        page = "home"
                    }
                )

                "products" -> ProductsPage(
                    products = products,
                    storage = storage
                )

                "people" -> PeoplePage(
                    people = people,
                    storage = storage
                )
            }
        }
    }
}

@Composable
fun HomePage(
    people: List<Person>,
    products: List<Product>,
    accounts: List<SupplierAccount>
) {

    val purchases =
        accounts.sumOf { it.purchases }

    val payments =
        accounts.sumOf { it.payments }

    val debt =
        purchases - payments

    Text(
        "داشبورد",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    InfoCard(
        "کل خرید دلاری",
        "$${dollarFormat.format(purchases)}"
    )

    InfoCard(
        "کل پرداخت",
        "$${dollarFormat.format(payments)}"
    )

    InfoCard(
        "مانده بدهی",
        "$${dollarFormat.format(debt)}"
    )

    Spacer(Modifier.height(4.dp))

    Text(
        "موجودی کالاها",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    if (products.isEmpty()) {

        Text("هنوز کالایی تعریف نشده است.")

    } else {

        products.forEach { product ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    Modifier.padding(14.dp)
                ) {

                    Text(
                        product.name,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "موجودی: ${product.quantity} عدد"
                    )

                    Text(
                        "میانگین خرید: $" +
                            dollarFormat.format(
                                product.averagePrice
                            )
                    )
                }
            }
        }
    }

    if (accounts.isNotEmpty()) {

        Spacer(Modifier.height(6.dp))

        Text(
            "مانده تأمین‌کنندگان",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        accounts.forEach { account ->

            val person =
                people.find {
                    it.id == account.personId
                }

            if (person != null) {

                Text(
                    "${person.fullName}: $" +
                        dollarFormat.format(
                            account.purchases -
                                account.payments
                        )
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String
) {

    Card(
        Modifier.fillMaxWidth()
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {

            Text(title)

            Spacer(
                Modifier.height(4.dp)
            )

            Text(
                value,
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PeoplePage(
    people: MutableList<Person>,
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            "اشخاص",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = {
                showAdd = !showAdd
            }
        ) {
            Text("+ افزودن شخص")
        }
    }

    if (showAdd) {

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
            },
            label = {
                Text("نام")
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
            },
            label = {
                Text("نام خانوادگی")
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            modifier =
                Modifier.fillMaxWidth(),

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

                    storage.savePeople(people)

                    firstName = ""
                    lastName = ""
                    showAdd = false
                }
            }
        ) {
            Text("ذخیره شخص")
        }
    }

    if (people.isEmpty()) {

        Text("هنوز شخصی تعریف نشده است.")

    } else {

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            items(people) { person ->

                Card(
                    Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = person.fullName,
                        modifier =
                            Modifier.padding(16.dp),
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProductsPage(
    products: MutableList<Product>,
    storage: AppStorage
) {

    var showAdd by remember {
        mutableStateOf(false)
    }

    var productName by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            "کالاها",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = {
                showAdd = !showAdd
            }
        ) {
            Text("+ افزودن کالا")
        }
    }

    if (showAdd) {

        OutlinedTextField(
            value = productName,
            onValueChange = {
                productName = it
            },
            label = {
                Text("نام کالا")
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            modifier =
                Modifier.fillMaxWidth(),

            onClick = {

                val name =
                    productName.trim()

                if (
                    name.isNotBlank() &&
                    products.none {
                        it.name.equals(
                            name,
                            ignoreCase = true
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

                    storage.saveProducts(products)

                    productName = ""
                    showAdd = false
                }
            }
        ) {
            Text("ذخیره کالا")
        }
    }

    if (products.isEmpty()) {

        Text("هنوز کالایی تعریف نشده است.")

    } else {

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            items(products) { product ->

                Card(
                    Modifier.fillMaxWidth()
                ) {

                    Column(
                        Modifier.padding(16.dp)
                    ) {

                        Text(
                            product.name,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            "موجودی: ${product.quantity}"
                        )

                        Text(
                            "میانگین خرید: $" +
                                dollarFormat.format(
                                    product.averagePrice
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyPage(
    people: List<Person>,
    products: MutableList<Product>,
    accounts: MutableList<SupplierAccount>,
    storage: AppStorage,
    onDone: () -> Unit
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

    Text(
        "ثبت خرید",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    if (people.isEmpty()) {

        Text(
            "ابتدا از قسمت اشخاص، تأمین‌کننده را تعریف کنید."
        )

        return
    }

    if (products.isEmpty()) {

        Text(
            "ابتدا از قسمت کالاها، کالا را تعریف کنید."
        )

        return
    }

    ExposedDropdownMenuBox(
        expanded = personMenu,
        onExpandedChange = {
            personMenu = !personMenu
        }
    ) {

        OutlinedTextField(
            value =
                selectedPerson?.fullName ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("تأمین‌کننده")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults
                    .TrailingIcon(
                        expanded =
                            personMenu
                    )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = personMenu,
            onDismissRequest = {
                personMenu = false
            }
        ) {

            people.forEach { person ->

                DropdownMenuItem(
                    text = {
                        Text(person.fullName)
                    },
                    onClick = {
                        selectedPerson =
                            person
                        personMenu = false
                    }
                )
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = productMenu,
        onExpandedChange = {
            productMenu =
                !productMenu
        }
    ) {

        OutlinedTextField(
            value =
                selectedProduct?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("کالا")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults
                    .TrailingIcon(
                        expanded =
                            productMenu
                    )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = productMenu,
            onDismissRequest = {
                productMenu = false
            }
        ) {

            products.forEach { product ->

                DropdownMenuItem(
                    text = {
                        Text(product.name)
                    },
                    onClick = {
                        selectedProduct =
                            product
                        productMenu = false
                    }
                )
            }
        }
    }

    OutlinedTextField(
        value = quantityText,
        onValueChange = {
            quantityText = it
        },
        label = {
            Text("تعداد")
        },
        modifier =
            Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Number
            )
    )

    OutlinedTextField(
        value = priceText,
        onValueChange = {
            priceText = it
        },
        label = {
            Text("قیمت واحد دلار")
        },
        modifier =
            Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Decimal
            )
    )

    val q =
        quantityText.toIntOrNull()

    val price =
        priceText.toDoubleOrNull()

    if (
        q != null &&
        price != null
    ) {

        Text(
            "جمع خرید: $" +
                dollarFormat.format(
                    q * price
                )
        )
    }

    Button(
        modifier =
            Modifier.fillMaxWidth(),

        onClick = {

            val person =
                selectedPerson

            val product =
                selectedProduct

            val quantity =
                quantityText.toIntOrNull()

            val unitPrice =
                priceText.toDoubleOrNull()

            if (
                person != null &&
                product != null &&
                quantity != null &&
                quantity > 0 &&
                unitPrice != null &&
                unitPrice >= 0
            ) {

                val oldQuantity =
                    product.quantity

                val oldAverage =
                    product.averagePrice

                val newQuantity =
                    oldQuantity + quantity

                val oldValue =
                    oldQuantity *
                        oldAverage

                val purchaseValue =
                    quantity *
                        unitPrice

                product.averagePrice =
                    (
                        oldValue +
                            purchaseValue
                    ) /
                    newQuantity.toDouble()

                product.quantity =
                    newQuantity

                var account =
                    accounts.find {
                        it.personId ==
                            person.id
                    }

                if (account == null) {

                    account =
                        SupplierAccount(
                            personId =
                                person.id
                        )

                    accounts.add(
                        account
                    )
                }

                account.purchases +=
                    purchaseValue

                storage.saveProducts(
                    products
                )

                storage.saveAccounts(
                    accounts
                )

                onDone()
            }
        }
    ) {

        Text("ثبت خرید")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPage(
    people: List<Person>,
    accounts: MutableList<SupplierAccount>,
    storage: AppStorage,
    onDone: () -> Unit
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

    Text(
        "ثبت پرداخت",
        style =
            MaterialTheme.typography.titleLarge,
        fontWeight =
            FontWeight.Bold
    )

    if (people.isEmpty()) {

        Text(
            "ابتدا شخص مورد نظر را در قسمت اشخاص تعریف کنید."
        )

        return
    }

    ExposedDropdownMenuBox(
        expanded = personMenu,
        onExpandedChange = {
            personMenu = !personMenu
        }
    ) {

        OutlinedTextField(
            value =
                selectedPerson?.fullName ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text("تأمین‌کننده")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults
                    .TrailingIcon(
                        expanded =
                            personMenu
                    )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = personMenu,
            onDismissRequest = {
                personMenu = false
            }
        ) {

            people.forEach { person ->

                DropdownMenuItem(
                    text = {
                        Text(person.fullName)
                    },
                    onClick = {
                        selectedPerson =
                            person
                        personMenu = false
                    }
                )
            }
        }
    }

    OutlinedTextField(
        value = amountText,
        onValueChange = {
            amountText = it
        },
        label = {
            Text("مبلغ پرداختی دلار")
        },
        modifier =
            Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Decimal
            )
    )

    Button(
        modifier =
            Modifier.fillMaxWidth(),

        onClick = {

            val person =
                selectedPerson

            val amount =
                amountText.toDoubleOrNull()

            if (
                person != null &&
                amount != null &&
                amount > 0
            ) {

                var account =
                    accounts.find {
                        it.personId ==
                            person.id
                    }

                if (account == null) {

                    account =
                        SupplierAccount(
                            personId =
                                person.id
                        )

                    accounts.add(
                        account
                    )
                }

                account.payments +=
                    amount

                storage.saveAccounts(
                    accounts
                )

                onDone()
            }
        }
    ) {

        Text("ثبت پرداخت")
    }
}
