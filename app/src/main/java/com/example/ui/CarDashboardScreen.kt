package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CarEntity
import com.example.data.ReservationEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDashboardScreen(
    viewModel: CarViewModel,
    modifier: Modifier = Modifier
) {
    val cars by viewModel.cars.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observe flow messages to show in feedback toasts/snackbars
    LaunchedEffect(Unit) {
        viewModel.messageFlow.collectLatest { uiMessage ->
            when (uiMessage) {
                is UiMessage.Success -> {
                    snackbarHostState.showSnackbar(
                        message = uiMessage.message,
                        withDismissAction = true
                    )
                }
                is UiMessage.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "⚠️ " + uiMessage.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    // Modal state for Reservation Dialog
    var showReserveDialogForCar by remember { mutableStateOf<CarEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_car_logo),
                            contentDescription = "Car Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "FLEET COMMAND",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                color = CyberCyan
                            )
                            Text(
                                text = "Car Rental & Management",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Quick refresh indicator
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarbonSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = CarbonBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Stats Cards Row
            FleetStatsRow(cars = cars)

            // Dynamic Custom styled Tab switcher
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CarbonSurface,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = CyberCyan
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("FLEET", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Fleet List") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("QUICK PARITY", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "CLI Emulator") }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("HISTORY", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Booking History") }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("MANAGE", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AddToPhotos, contentDescription = "Add New Car") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(CarbonBackground)
            ) {
                when (activeTab) {
                    0 -> FleetBrowseTab(
                        cars = cars,
                        onReserveClick = { showReserveDialogForCar = it },
                        onReturnClick = { viewModel.returnCar(it.regNo) }
                    )
                    1 -> CliEmulatorTab(
                        cars = cars,
                        onReserve = { reg, name, days -> viewModel.reserveCar(reg, name, days) },
                        onReturn = { reg -> viewModel.returnCar(reg) }
                    )
                    2 -> BookingsTab(
                        reservations = reservations
                    )
                    3 -> AddCarTab(
                        onAddCar = { reg, brand, rent, cat, fuel, trans, seats ->
                            viewModel.addNewCar(reg, brand, rent, cat, fuel, trans, seats)
                        }
                    )
                }
            }
        }
    }

    // Modal dialog trigger for reservation booking input
    showReserveDialogForCar?.let { car ->
        ReserveCarDialog(
            car = car,
            onDismiss = { showReserveDialogForCar = null },
            onConfirm = { name, days ->
                viewModel.reserveCar(car.regNo, name, days)
                showReserveDialogForCar = null
            }
        )
    }
}

@Composable
fun FleetStatsRow(cars: List<CarEntity>) {
    val total = cars.size
    val available = cars.count { it.available }
    val rented = total - available

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CarbonSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total Fleet",
            value = total.toString(),
            color = ElectricBlue,
            icon = Icons.Default.Garage,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Available",
            value = available.toString(),
            color = StatusGreen,
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Rented Out",
            value = rented.toString(),
            color = CyberCyan,
            icon = Icons.Default.HourglassBottom,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CarbonSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = color
            )
        }
    }
}

// TAB 1: FLEET BROWSE
@Composable
fun FleetBrowseTab(
    cars: List<CarEntity>,
    onReserveClick: (CarEntity) -> Unit,
    onReturnClick: (CarEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCars = cars.filter {
        it.brand.contains(searchQuery, ignoreCase = true) ||
        it.regNo.toString().contains(searchQuery) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Search & Filter Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search brand, reg, type...", color = TextSecondary) },
            prefix = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = CyberCyan, modifier = Modifier.padding(end = 6.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("search_car_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CarbonSurfaceVariant,
                unfocusedContainerColor = CarbonSurface,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = Color.Transparent
            )
        )

        if (filteredCars.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No vehicles match your search query",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCars) { car ->
                    CarItemCard(
                        car = car,
                        onReserveClick = { onReserveClick(car) },
                        onReturnClick = { onReturnClick(car) }
                    )
                }
            }
        }
    }
}

@Composable
fun CarItemCard(
    car: CarEntity,
    onReserveClick: () -> Unit,
    onReturnClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("car_item_${car.regNo}"),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (car.available) CyberCyan.copy(alpha = 0.3f) else ElectricBlue.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row: Brand & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = car.brand,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = HighContrastWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonSurfaceVariant),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Reg: ${car.regNo}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyberCyan
                            )
                        }
                    }
                    Text(
                        text = car.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Dynamic Tech-styled Availability Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (car.available) StatusGreenBg else StatusRedBg)
                        .border(
                            width = 1.dp,
                            color = if (car.available) StatusGreen.copy(alpha = 0.7f) else StatusRed.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (car.available) StatusGreen else StatusRed)
                        )
                        Text(
                            text = if (car.available) "AVAILABLE" else "RENTED OUT",
                            color = if (car.available) StatusGreen else StatusRed,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Specs Grid (Row of chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SpecChip(icon = Icons.Default.LocalGasStation, text = car.fuel)
                SpecChip(icon = Icons.Default.Settings, text = car.transmission)
                SpecChip(icon = Icons.Default.Groups, text = "${car.seats} Seats")
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CarbonSurfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Price & Booking CTA Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rent Cost",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${car.rent}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyberCyan
                        )
                        Text(
                            text = " /day",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                if (car.available) {
                    Button(
                        onClick = onReserveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reserve_cta_${car.regNo}")
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reserve", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onReturnClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CarbonSurfaceVariant,
                            contentColor = StatusGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("return_cta_${car.regNo}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SpecChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CarbonSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// TAB 2: CLI PARITY EMULATOR
@Composable
fun CliEmulatorTab(
    cars: List<CarEntity>,
    onReserve: (Int, String, Int) -> Unit,
    onReturn: (Int) -> Unit
) {
    // We emulate the options of original C++ app
    var selectedOption by remember { mutableStateOf(0) } // 0 = Home, 1 = Show, 2 = Reserve, 3 = Return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Retro C++ System Frame Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = CarbonSurface),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Retro Shell Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Yellow))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                    }
                    Text(
                        text = "CAR_RENTAL_SYSTEM.CPP - TERMINAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextSecondary
                    )
                }

                HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = selectedOption,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    modifier = Modifier.weight(1f)
                ) { target ->
                    when (target) {
                        0 -> CliMenuScreen(
                            onSelect = { selectedOption = it }
                        )
                        1 -> CliShowScreen(
                            cars = cars,
                            onBack = { selectedOption = 0 }
                        )
                        2 -> CliReserveScreen(
                            cars = cars,
                            onReserve = onReserve,
                            onBack = { selectedOption = 0 }
                        )
                        3 -> CliReturnScreen(
                            onReturn = onReturn,
                            onBack = { selectedOption = 0 }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CliMenuScreen(onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Car Rental Management System",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            ),
            color = CyberCyan
        )
        Text(
            text = "----------------------------",
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        val menuItems = listOf(
            "1. Show Available Cars" to 1,
            "2. Reserve a Car" to 2,
            "3. Return a Car" to 3
        )

        menuItems.forEach { (label, option) ->
            Button(
                onClick = { onSelect(option) },
                colors = ButtonDefaults.buttonColors(containerColor = CarbonSurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 4.dp)
                    .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = label,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun CliShowScreen(
    cars: List<CarEntity>,
    onBack: () -> Unit
) {
    val availableCars = cars.filter { it.available }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "> showFun()",
            fontFamily = FontFamily.Monospace,
            color = CyberCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = "AVAILABLE FLEET REGISTRY:",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = StatusGreen
                )
            }
            if (availableCars.isEmpty()) {
                item {
                    Text(
                        text = "No cars available at this moment.",
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            } else {
                items(availableCars) { car ->
                    Text(
                        text = "Car ${car.brand} with Reg ${car.regNo} and with Rent ${car.rent}",
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Back to C++ Menu", fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun CliReserveScreen(
    cars: List<CarEntity>,
    onReserve: (Int, String, Int) -> Unit,
    onBack: () -> Unit
) {
    var regInput by remember { mutableStateOf("") }
    var userDays by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "> reserveFun()",
            fontFamily = FontFamily.Monospace,
            color = CyberCyan
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = regInput,
            onValueChange = { regInput = it },
            placeholder = { Text("Enter Registration No (e.g. 123)", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("cli_reserve_reg_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberCyan
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            placeholder = { Text("Enter Customer Name", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth().testTag("cli_reserve_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberCyan
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = userDays,
            onValueChange = { userDays = it },
            placeholder = { Text("Enter Total Days", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("cli_reserve_days_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberCyan
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Cancel", color = TextSecondary, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    val reg = regInput.toIntOrNull() ?: 0
                    val days = userDays.toIntOrNull() ?: 0
                    onReserve(reg, customerName, days)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cli_reserve_execute_btn")
            ) {
                Text("Execute Reservation", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CliReturnScreen(
    onReturn: (Int) -> Unit,
    onBack: () -> Unit
) {
    var regInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "> returnFun()",
            fontFamily = FontFamily.Monospace,
            color = CyberCyan
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = regInput,
            onValueChange = { regInput = it },
            placeholder = { Text("Enter Registration No to return", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("cli_return_reg_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberCyan
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Cancel", color = TextSecondary, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    val reg = regInput.toIntOrNull() ?: 0
                    onReturn(reg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cli_return_execute_btn")
            ) {
                Text("Execute Return", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// TAB 3: BOOKINGS HISTORY LOG
@Composable
fun BookingsTab(reservations: List<ReservationEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "HISTORIC RESERVATIONS LOG",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = CyberCyan,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (reservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No reservation records available.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(reservations) { res ->
                    BookingRecordCard(res = res)
                }
            }
        }
    }
}

@Composable
fun BookingRecordCard(res: ReservationEntity) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateString = formatter.format(Date(res.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.dp, CarbonSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = res.carBrand,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HighContrastWhite
                    )
                    Text(
                        text = "Reg No: ${res.carRegNo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Small compact badge
                val active = res.status == "Active"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) StatusRedBg else StatusGreenBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = res.status.uppercase(),
                        color = if (active) StatusRed else StatusGreen,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CarbonSurfaceVariant, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CUSTOMER", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(res.customerName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DURATION", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${res.days} days", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL BILL", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("$${res.bill}", fontWeight = FontWeight.Bold, color = CyberCyan, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Booked on: $dateString",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// TAB 4: ADD VEHICLE FORM
@Composable
fun AddCarTab(
    onAddCar: (Int, String, Int, String, String, String, Int) -> Unit
) {
    var brand by remember { mutableStateOf("") }
    var regNoStr by remember { mutableStateOf("") }
    var rentStr by remember { mutableStateOf("") }

    // Categories
    val categories = listOf("Hatchback", "Sedan", "SUV", "Crossover", "Luxury")
    var selectedCategory by remember { mutableStateOf(categories[1]) }

    // Fuel types
    val fuels = listOf("Petrol", "Hybrid", "Diesel", "Electric")
    var selectedFuel by remember { mutableStateOf(fuels[0]) }

    // Transmission types
    val transmissions = listOf("Automatic", "Manual")
    var selectedTransmission by remember { mutableStateOf(transmissions[0]) }

    // Seats
    var seats by remember { mutableStateOf(5) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "FLEET EXPANSION PROTOCOL",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CyberCyan
            )
            Text(
                text = "Register a new high-performance vehicle into the active system.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        item {
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Vehicle Brand (e.g. Nissan, Land Rover)") },
                modifier = Modifier.fillMaxWidth().testTag("add_car_brand_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyberCyan
                )
            )
        }

        item {
            OutlinedTextField(
                value = regNoStr,
                onValueChange = { regNoStr = it },
                label = { Text("Registration ID (Integer)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("add_car_reg_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyberCyan
                )
            )
        }

        item {
            OutlinedTextField(
                value = rentStr,
                onValueChange = { rentStr = it },
                label = { Text("Daily Rent Rate ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("add_car_rent_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyberCyan
                )
            )
        }

        item {
            Text("Vehicle Category", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCategory = cat },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberCyan else CarbonSurfaceVariant
                        )
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterHorizontally),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text("Power Station / Fuel", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                fuels.forEach { fuel ->
                    val isSelected = fuel == selectedFuel
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFuel = fuel },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberCyan else CarbonSurfaceVariant
                        )
                    ) {
                        Text(
                            text = fuel,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterHorizontally),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Transmission", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        transmissions.forEach { t ->
                            val isSelected = t == selectedTransmission
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTransmission = t },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ElectricBlue else CarbonSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = t,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .align(Alignment.CenterHorizontally),
                                    color = if (isSelected) Color.White else TextPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Seating Size", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (seats > 2) seats-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = CyberCyan)
                        }
                        Text(
                            text = "$seats Seats",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { if (seats < 9) seats++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = CyberCyan)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val reg = regNoStr.toIntOrNull() ?: 0
                    val rent = rentStr.toIntOrNull() ?: 0
                    onAddCar(reg, brand, rent, selectedCategory, selectedFuel, selectedTransmission, seats)
                    // Reset fields
                    brand = ""
                    regNoStr = ""
                    rentStr = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_car_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deploy Vehicle To Active Fleet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// RESERVATION COMPACT MODAL DIALOG
@Composable
fun ReserveCarDialog(
    car: CarEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var daysStr by remember { mutableStateOf("1") }

    val days = daysStr.toIntOrNull() ?: 1
    val calculatedBill = car.rent * days

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "RESERVE VEHICLE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyberCyan
                )
                Text(
                    text = "${car.brand} (Reg No: ${car.regNo})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_customer_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyberCyan
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = daysStr,
                        onValueChange = { daysStr = it },
                        label = { Text("Rental Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("dialog_rent_days"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CyberCyan
                        )
                    )

                    // Stepper actions
                    IconButton(
                        onClick = {
                            val cur = daysStr.toIntOrNull() ?: 1
                            if (cur > 1) daysStr = (cur - 1).toString()
                        },
                        modifier = Modifier.background(CarbonSurfaceVariant, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrement", tint = CyberCyan)
                    }

                    IconButton(
                        onClick = {
                            val cur = daysStr.toIntOrNull() ?: 1
                            daysStr = (cur + 1).toString()
                        },
                        modifier = Modifier.background(CarbonSurfaceVariant, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increment", tint = CyberCyan)
                    }
                }

                // Billing Overview
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimated Bill", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = "$${car.rent} x $days days",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "$$calculatedBill",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyberCyan
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, days) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.testTag("dialog_confirm_btn")
            ) {
                Text("Confirm Rental Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CarbonSurface
    )
}
