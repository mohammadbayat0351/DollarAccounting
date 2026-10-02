package com.dollaraccounting.app

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
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                DollarAccountingApp()
            }
        }
    }
}

data class Product(
    val name: String,
    var quantity: Int,
    var averagePrice: Double
)

data class Supplier(
    val name: String,
    var purchases: Double,
    var payments: Double
)

val dollarFormat = DecimalFormat("#,##0.00")

@Composable
fun DollarAccountingApp() {

    val products = remember {
        mutableStateListOf<Product>()
    }

    val suppliers = remember {
        mutableStateListOf<Supplier>()
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
                    selected = page == "suppliers",
                    onClick = { page = "suppliers" },
                    icon = { Text("●") },
                    label = { Text("اشخاص") }
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "حسابداری دلاری",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            when (page) {

                "home" -> {
                    Dashboard(
                        products = products,
                        suppliers = suppliers
                    )
                }

                "buy" -> {
                    BuyPage(
                        products = products,
                        suppliers = suppliers,
                        onSaved = {
                            page = "home"
                        }
                    )
                }

                "pay" -> {
                    PayPage(
                        suppliers = suppliers,
                        onSaved = {
                            page = "home"
                        }
                    )
                }

                "products" -> {
                    ProductList(products)
                }

                "suppliers" -> {
                    SupplierList(suppliers)
                }
            }
        }
    }
}

@Composable
fun Dashboard(
    products: List<Product>,
    suppliers: List<Supplier>
) {

    val totalPurchases = suppliers.sumOf {
        it.purchases
    }

    val totalPayments = suppliers.sumOf {
        it.payments
    }

    val totalDebt = totalPurchases - totalPayments

    val totalStock = products.sumOf {
        it.quantity
    }

    Text(
        text = "داشبورد",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    InfoCard(
        title = "کل خرید",
        value = "$" + dollarFormat.format(totalPurchases)
    )

    InfoCard(
        title = "کل پرداخت",
        value = "$" + dollarFormat.format(totalPayments)
    )

    InfoCard(
        title = "مانده بدهی",
        value = "$" + dollarFormat.format(totalDebt)
    )

    InfoCard(
        title = "تعداد موجودی",
        value = totalStock.toString()
    )
}

@Composable
fun InfoCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(text = title)

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BuyPage(
    products: MutableList<Product>,
    suppliers: MutableList<Supplier>,
    onSaved: () -> Unit
) {

    var supplierName by remember {
        mutableStateOf("")
    }

    var productName by remember {
        mutableStateOf("")
    }

    var quantityText by remember {
        mutableStateOf("")
    }

    var priceText by remember {
        mutableStateOf("")
    }

    Text(
        text = "ثبت خرید دلاری",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
        value = supplierName,
        onValueChange = {
            supplierName = it
        },
        label = {
            Text("نام تأمین‌کننده")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = productName,
        onValueChange = {
            productName = it
        },
        label = {
            Text("نام کالا")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = quantityText,
        onValueChange = {
            quantityText = it
        },
        label = {
            Text("تعداد")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
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
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )

    val previewQuantity = quantityText.toIntOrNull()
    val previewPrice = priceText.toDoubleOrNull()

    if (
        previewQuantity != null &&
        previewPrice != null
    ) {

        val total = previewQuantity * previewPrice

        Text(
            text = "جمع خرید: $" +
                dollarFormat.format(total)
        )
    }

    Button(
        onClick = {

            val quantity =
                quantityText.toIntOrNull()

            val unitPrice =
                priceText.toDoubleOrNull()

            if (
                supplierName.isNotBlank() &&
                productName.isNotBlank() &&
                quantity != null &&
                quantity > 0 &&
                unitPrice != null &&
                unitPrice >= 0
            ) {

                var supplier =
                    suppliers.find {
                        it.name == supplierName.trim()
                    }

                if (supplier == null) {

                    supplier = Supplier(
                        name = supplierName.trim(),
                        purchases = 0.0,
                        payments = 0.0
                    )

                    suppliers.add(supplier)
                }

                var product =
                    products.find {
                        it.name == productName.trim()
                    }

                if (product == null) {

                    product = Product(
                        name = productName.trim(),
                        quantity = 0,
                        averagePrice = 0.0
                    )

                    products.add(product)
                }

                val oldQuantity =
                    product.quantity

                val oldAverage =
                    product.averagePrice

                val newQuantity =
                    oldQuantity + quantity

                val oldValue =
                    oldQuantity * oldAverage

                val newPurchaseValue =
                    quantity * unitPrice

                val newAverage =
                    (oldValue + newPurchaseValue) /
                        newQuantity.toDouble()

                product.quantity =
                    newQuantity

                product.averagePrice =
                    newAverage

                supplier.purchases =
                    supplier.purchases +
                        newPurchaseValue

                onSaved()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {

        Text("ثبت خرید")
    }
}

@Composable
fun PayPage(
    suppliers: MutableList<Supplier>,
    onSaved: () -> Unit
) {

    var supplierName by remember {
        mutableStateOf("")
    }

    var amountText by remember {
        mutableStateOf("")
    }

    Text(
        text = "ثبت پرداخت",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
        value = supplierName,
        onValueChange = {
            supplierName = it
        },
        label = {
            Text("نام تأمین‌کننده")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = amountText,
        onValueChange = {
            amountText = it
        },
        label = {
            Text("مبلغ پرداختی دلار")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )

    Button(
        onClick = {

            val amount =
                amountText.toDoubleOrNull()

            if (
                supplierName.isNotBlank() &&
                amount != null &&
                amount > 0
            ) {

                var supplier =
                    suppliers.find {
                        it.name == supplierName.trim()
                    }

                if (supplier == null) {

                    supplier = Supplier(
                        name = supplierName.trim(),
                        purchases = 0.0,
                        payments = 0.0
                    )

                    suppliers.add(supplier)
                }

                supplier.payments =
                    supplier.payments + amount

                onSaved()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {

        Text("ثبت پرداخت")
    }
}

@Composable
fun ProductList(
    products: List<Product>
) {

    Text(
        text = "کالاها و موجودی",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    if (products.isEmpty()) {

        Text(
            text = "هنوز کالایی ثبت نشده."
        )

    } else {

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            items(products) { product ->

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = product.name,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "موجودی: ${product.quantity}"
                        )

                        Text(
                            text =
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

@Composable
fun SupplierList(
    suppliers: List<Supplier>
) {

    Text(
        text = "حساب تأمین‌کنندگان",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    if (suppliers.isEmpty()) {

        Text(
            text = "هنوز شخصی ثبت نشده."
        )

    } else {

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            items(suppliers) { supplier ->

                val debt =
                    supplier.purchases -
                        supplier.payments

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = supplier.name,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "کل خرید: $" +
                                dollarFormat.format(
                                    supplier.purchases
                                )
                        )

                        Text(
                            text =
                                "کل پرداخت: $" +
                                dollarFormat.format(
                                    supplier.payments
                                )
                        )

                        Text(
                            text =
                                "مانده: $" +
                                dollarFormat.format(
                                    debt
                                ),
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
