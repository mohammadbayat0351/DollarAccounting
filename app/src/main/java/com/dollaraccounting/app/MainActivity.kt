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
    var quantity: Int = 0,
    var averagePrice: Double = 0.0
)

data class Supplier(
    val name: String,
    var totalPurchases: Double = 0.0,
    var totalPayments: Double = 0.0
)

private val moneyFormat = DecimalFormat("#,##0.00")

@Composable
fun DollarAccountingApp() {

    val products = remember {
        mutableStateListOf<Product>()
    }

    val suppliers = remember {
        mutableStateListOf<Supplier>()
    }

    var currentPage by remember {
        mutableStateOf("خانه")
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                listOf(
                    "خانه",
                    "خرید",
                    "پرداخت",
                    "کالاها",
                    "اشخاص"
                ).forEach { page ->

                    NavigationBarItem(
                        selected = currentPage == page,

                        onClick = {
                            currentPage = page
                        },

                        icon = {
                            Text(
                                if (currentPage == page)
                                    "●"
                                else
                                    "○"
                            )
                        },

                        label = {
                            Text(page)
                        }
                    )
                }
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "حسابداری دلاری",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            when (currentPage) {

                "خانه" -> Dashboard(
                    products = products,
                    suppliers = suppliers
                )

                "خرید" -> PurchaseForm(
                    products = products,
                    suppliers = suppliers,
                    onDone = {
                        currentPage = "خانه"
                    }
                )

                "پرداخت" -> PaymentForm(
                    suppliers = suppliers,
                    onDone = {
                        currentPage = "خانه"
                    }
                )

                "کالاها" -> ProductsPage(
                    products
                )

                "اشخاص" -> SuppliersPage(
                    suppliers
                )
            }
        }
    }
}

@Composable
fun Dashboard(
    products: List<Product>,
    suppliers: List<Supplier>
) {

    val totalPurchases =
        suppliers.sumOf {
            it.totalPurchases
        }

    val totalPayments =
        suppliers.sumOf {
            it.totalPayments
        }

    val balance =
        totalPurchases - totalPayments

    val stock =
        products.sumOf {
            it.quantity
        }

    Text(
        "داشبورد",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    DashboardCard(
        "کل خرید دلاری",
        "$" + moneyFormat.format(totalPurchases)
    )

    DashboardCard(
        "کل پرداخت",
        "$" + moneyFormat.format(totalPayments)
    )

    DashboardCard(
        "مانده بدهی",
        "$" + moneyFormat.format(balance)
    )

    DashboardCard(
        "تعداد موجودی",
        stock.toString()
    )
}

@Composable
fun DashboardCard(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(title)

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                value,
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
fun PurchaseForm(
    products: MutableList<Product>,
    suppliers: MutableList<Supplier>,
    onDone: () -> Unit
) {

    var supplierName by remember {
        mutableStateOf("")
    }

    var productName by remember {
        mutableStateOf("")
    }

    var quantity by remember {
        mutableStateOf("")
    }

    var unitPrice by remember {
        mutableStateOf("")
    }

    Text(
        "ثبت خرید جدید",
        style =
            MaterialTheme.typography.headlineSmall,
        fontWeight =
            FontWeight.Bold
    )

    TextFieldBox(
        "نام تأمین‌کننده",
        supplierName
    ) {
        supplierName = it
    }

    TextFieldBox(
        "نام کالا",
        productName
    ) {
        productName = it
    }

    NumberField(
        "تعداد",
        quantity
    ) {
        quantity = it
    }

    NumberField(
        "قیمت واحد (دلار)",
        unitPrice
    ) {
        unitPrice = it
    }

    val qty =
        quantity.toIntOrNull()

    val price =
        unitPrice.toDoubleOrNull()

    if (
        qty != null &&
        price != null
    ) {

        Text(
            "جمع خرید: $" +
                    moneyFormat.format(
                        qty * price
                    )
        )
    }

    Button(
        modifier =
            Modifier.fillMaxWidth(),

        onClick = {

            val q =
                quantity.toIntOrNull()

            val priceValue =
                unitPrice.toDoubleOrNull()

            if (
                supplierName.isBlank() ||
                productName.isBlank() ||
                q == null ||
                q <= 0 ||
                priceValue == null ||
                priceValue < 0
            ) {
                return@Button
            }

            val supplier =
                suppliers.find {
                    it.name.trim()
                        .equals(
                            supplierName.trim(),
                            ignoreCase = true
                        )
                }
                    ?: Supplier(
                        supplierName.trim()
                    ).also {
                        suppliers.add(it)
                    }

            val product =
                products.find {
                    it.name.trim()
                        .equals(
                            productName.trim(),
                            ignoreCase = true
                        )
                }
                    ?: Product(
                        productName.trim()
                    ).also {
                        products.add(it)
                    }

            val oldQuantity =
                product.quantity

            val oldAverage =
                product.averagePrice

            val newQuantity =
                oldQuantity + q

            /*
             میانگین وزنی:

             (موجودی قبلی × میانگین قبلی)
             +
             (تعداد خرید جدید × قیمت جدید)

             تقسیم بر موجودی جدید
            */

            product.averagePrice =
                if (newQuantity > 0) {

                    (
                        oldQuantity
