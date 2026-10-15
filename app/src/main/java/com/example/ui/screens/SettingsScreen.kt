package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.preferences.AppPreferences
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.MosqueBlack
import com.example.ui.theme.MosqueCardDark
import com.example.ui.theme.MosqueSurfaceVariant
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.service.AdhanPlaybackService
import com.google.android.gms.location.LocationServices
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: AppPreferences,
    theme: LedTheme,
    language: AppLanguage,
    onSettingsChanged: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val activeColor = Color(theme.primaryHex)
    val borderColor = Color(theme.borderHex)
    val isUrdu = language == AppLanguage.URDU

    var showCityDialog by remember { mutableStateOf(false) }
    var showCustomCoordDialog by remember { mutableStateOf(false) }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }

    // GPS Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            locationStatusMessage = if (isUrdu) "مقام کا تعین کیا جا رہا ہے..." else "Fetching GPS location..."
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lng = location.longitude
                        var cityName = "GPS Location"
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(lat, lng, 1)
                            if (!addresses.isNullOrEmpty()) {
                                cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "My Location"
                            }
                        } catch (_: Exception) {}

                        prefs.applyGpsCoordinates(lat, lng, cityName, TimeZone.getDefault().id)
                        locationStatusMessage = if (isUrdu) "$cityName محفوظ ہو گیا" else "Location updated: $cityName"
                        onSettingsChanged()
                    } else {
                        locationStatusMessage = if (isUrdu) "GPS سگنل دستیاب نہیں، دستی شہر منتخب کریں" else "No GPS lock. Please select city manually."
                    }
                }.addOnFailureListener {
                    locationStatusMessage = if (isUrdu) "GPS میں خرابی: دستی شہر منتخب کریں" else "GPS error. Please select city manually."
                }
            } catch (e: SecurityException) {
                locationStatusMessage = "Location permission denied"
            }
        } else {
            locationStatusMessage = if (isUrdu) "اجازت نہیں ملی۔ دستی شہر منتخب کریں۔" else "Permission denied. Select city manually."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isUrdu) "ترتیبات (Settings)" else "Settings",
                        fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = activeColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MosqueBlack
                )
            )
        },
        containerColor = MosqueBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Section
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "مقام اور شہر" else "LOCATION & CITY",
                    theme = theme,
                    icon = Icons.Default.LocationOn
                ) {
                    Text(
                        text = "${if (isUrdu) "منتخب شہر:" else "Current:"} ${prefs.customCityName} (${if (prefs.useAutoGps) "GPS" else "Manual"})",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                    )

                    if (locationStatusMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = locationStatusMessage!!,
                            fontSize = 12.sp,
                            color = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("gps_locate_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isUrdu) "GPS خودکار" else "Use GPS", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showCityDialog = true },
                            modifier = Modifier.weight(1f).testTag("select_city_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = activeColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isUrdu) "شہر بدلیں" else "Select City", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { showCustomCoordDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isUrdu) "مخصوص کوآرڈینیٹس درج کریں" else "Enter Custom Coordinates (Lat/Long)",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // Display Theme Section
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "ڈسپلے تھیم" else "DISPLAY THEME",
                    theme = theme,
                    icon = Icons.Default.Palette
                ) {
                    Text(
                        text = if (isUrdu) "مسجد ایل ای ڈی رنگ منتخب کریں:" else "Select Mosque LED Clock Color:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LedTheme.values().forEach { t ->
                            val isSelected = prefs.ledTheme == t
                            val color = Color(t.primaryHex)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .testTag("theme_option_${t.code}")
                                    .clickable {
                                        prefs.ledTheme = t
                                        onSettingsChanged()
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.DarkGray,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = t.displayName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) activeColor else Color.Gray,
                                    fontFamily = ShareTechMonoFontFamily
                                )
                            }
                        }
                    }
                }
            }

            // Language Section
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "زبان (Language)" else "LANGUAGE",
                    theme = theme,
                    icon = Icons.Default.Translate
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = prefs.language == lang
                            OutlinedButton(
                                onClick = {
                                    prefs.language = lang
                                    onSettingsChanged()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_${lang.code}"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                                    contentColor = if (isSelected) activeColor else Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) activeColor else borderColor
                                    )
                                )
                            ) {
                                Text(
                                    text = "${lang.displayName} (${lang.nativeName})",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Calculation Method Section
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "حساب کا طریقہ کار" else "CALCULATION CONVENTION",
                    theme = theme,
                    icon = Icons.Default.Calculate
                ) {
                    CalculationMethod.values().forEach { method ->
                        val isSelected = prefs.calculationMethod == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.calculationMethod = method
                                    onSettingsChanged()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    prefs.calculationMethod = method
                                    onSettingsChanged()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = activeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isUrdu) method.urduName else method.englishName,
                                    fontSize = 13.sp,
                                    color = if (isSelected) activeColor else Color.White,
                                    fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                                )
                                Text(
                                    text = "Fajr: ${method.fajrAngle}° | Isha: ${if (method.ishaIntervalMinutes != null) "${method.ishaIntervalMinutes}m" else "${method.ishaAngle}°"}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontFamily = ShareTechMonoFontFamily
                                )
                            }
                        }
                    }
                }
            }

            // Juristic Method (Asr) Section
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "عصر کی فقہی ترتیب" else "ASR JURISTIC SCHOOL",
                    theme = theme,
                    icon = Icons.Default.AccountBalance
                ) {
                    JuristicMethod.values().forEach { juristic ->
                        val isSelected = prefs.juristicMethod == juristic
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.juristicMethod = juristic
                                    onSettingsChanged()
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    prefs.juristicMethod = juristic
                                    onSettingsChanged()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = activeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUrdu) juristic.urduName else juristic.englishName,
                                fontSize = 14.sp,
                                color = if (isSelected) activeColor else Color.White,
                                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                            )
                        }
                    }
                }
            }

            // Hijri Date Adjustment
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "ہجری تاریخ کی ترتیب" else "HIJRI DATE ADJUSTMENT",
                    theme = theme,
                    icon = Icons.Default.CalendarMonth
                ) {
                    Text(
                        text = if (isUrdu) "رویتِ ہلال کے مطابق ہجری دن کو ایڈجسٹ کریں:" else "Adjust for local moon sighting (-2 to +2 days):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(-2, -1, 0, 1, 2).forEach { offset ->
                            val isSelected = prefs.hijriOffset == offset
                            OutlinedButton(
                                onClick = {
                                    prefs.hijriOffset = offset
                                    onSettingsChanged()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) activeColor else Color.Transparent,
                                    contentColor = if (isSelected) Color.Black else Color.White
                                ),
                                modifier = Modifier.size(50.dp, 36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (offset > 0) "+$offset" else "$offset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Notifications & Lock-Screen
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "نوٹیفکیشن اور لاک اسکرین" else "NOTIFICATIONS & LOCK-SCREEN",
                    theme = theme,
                    icon = Icons.Default.Notifications
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isUrdu) "مستقل لائیو کاؤنٹ ڈاؤن نوٹیفکیشن" else "Persistent Lock-Screen Notification",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isUrdu) "لاک اسکرین اور اسٹیٹس بار پر اگلی نماز کا لائیو کاؤنٹ ڈاؤن دکھائیں" else "Displays next prayer and live countdown on your lock screen and notification shade.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = prefs.isPersistentNotificationEnabled,
                            onCheckedChange = {
                                prefs.isPersistentNotificationEnabled = it
                                onSettingsChanged()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = activeColor, checkedTrackColor = activeColor.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isUrdu) "انفرادی نماز کی اذان کے انتباہات:" else "Prayer Time Alerts (Azan / Chime):",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA).forEach { prayer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isUrdu) prayer.urduName else prayer.englishName,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                            )
                            Switch(
                                checked = prefs.isAlertEnabled(prayer),
                                onCheckedChange = {
                                    prefs.setAlertEnabled(prayer, it)
                                    onSettingsChanged()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = activeColor, checkedTrackColor = activeColor.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Customizable Adhan Sound Alert Section
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "اذان ساؤنڈ الرٹ (Adhan Sound)" else "ADHAN SOUND & AUDIO ALERT",
                    theme = theme,
                    icon = Icons.Default.VolumeUp
                ) {
                    Text(
                        text = if (isUrdu) "نماز کے وقت بجنے والا اذان ساؤنڈ منتخب کریں:" else "Select customizable audio alert played when prayer time is reached:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AdhanSound.values().forEach { sound ->
                        val isSelected = prefs.selectedAdhanSound == sound
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.selectedAdhanSound = sound
                                    onSettingsChanged()
                                }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    prefs.selectedAdhanSound = sound
                                    onSettingsChanged()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = activeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUrdu) sound.urduName else sound.englishName,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) activeColor else Color.White,
                                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isUrdu) "اذان والیوم:" else "Adhan Volume:",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${(prefs.adhanVolume * 100).toInt()}%",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 13.sp,
                            color = activeColor
                        )
                    }
                    Slider(
                        value = prefs.adhanVolume,
                        onValueChange = {
                            prefs.adhanVolume = it
                            onSettingsChanged()
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = activeColor,
                            activeTrackColor = activeColor,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview & Stop Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                AdhanPlaybackService.previewAdhan(context, prefs.selectedAdhanSound)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUrdu) "آڈیو چیک کریں" else "Test Sound",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                AdhanPlaybackService.stopAdhan(context)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUrdu) "بند کریں" else "Stop Audio",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Manual Minute Adjustments
            item {
                SettingsSectionCard(
                    title = if (isUrdu) "دستی منٹ ایڈجسٹمنٹ" else "MANUAL MINUTE ADJUSTMENTS",
                    theme = theme,
                    icon = Icons.Default.Tune
                ) {
                    Text(
                        text = if (isUrdu) "مقامی مسجد کے وقت سے ہم آہنگ کرنے کے لیے منٹ بڑھائیں یا گھٹائیں:" else "Fine-tune individual prayer times (+/- minutes) to match your local mosque:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(PrayerType.FAJR, PrayerType.SUNRISE, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA).forEach { prayer ->
                        val currentAdj = prefs.getMinuteAdjustment(prayer)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isUrdu) prayer.urduName else prayer.englishName,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        prefs.setMinuteAdjustment(prayer, currentAdj - 1)
                                        onSettingsChanged()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.LightGray)
                                }

                                Text(
                                    text = if (currentAdj > 0) "+${currentAdj}m" else "${currentAdj}m",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 13.sp,
                                    color = if (currentAdj != 0) activeColor else Color.White,
                                    modifier = Modifier.width(44.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                IconButton(
                                    onClick = {
                                        prefs.setMinuteAdjustment(prayer, currentAdj + 1)
                                        onSettingsChanged()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        CitySelectionDialog(
            cities = CityDirectory.CITIES,
            currentCityId = prefs.selectedCityId,
            theme = theme,
            language = language,
            onCitySelected = { city ->
                prefs.applyCity(city)
                showCityDialog = false
                onSettingsChanged()
            },
            onDismiss = { showCityDialog = false }
        )
    }

    // Custom Coordinate Dialog
    if (showCustomCoordDialog) {
        CustomCoordinateDialog(
            currentName = prefs.customCityName,
            currentLat = prefs.latitude,
            currentLng = prefs.longitude,
            theme = theme,
            language = language,
            onSave = { name, lat, lng ->
                prefs.applyGpsCoordinates(lat, lng, name, TimeZone.getDefault().id)
                showCustomCoordDialog = false
                onSettingsChanged()
            },
            onDismiss = { showCustomCoordDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    theme: LedTheme,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val activeColor = Color(theme.primaryHex)
    val borderColor = Color(theme.borderHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MosqueCardDark)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun CitySelectionDialog(
    cities: List<CityInfo>,
    currentCityId: String,
    theme: LedTheme,
    language: AppLanguage,
    onCitySelected: (CityInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val isUrdu = language == AppLanguage.URDU
    val activeColor = Color(theme.primaryHex)

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) cities else {
            val q = searchQuery.lowercase()
            cities.filter {
                it.name.lowercase().contains(q) ||
                it.urduName.contains(q) ||
                it.country.lowercase().contains(q) ||
                it.countryUrdu.contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MosqueCardDark,
        title = {
            Text(
                text = if (isUrdu) "شہر منتخب کریں" else "Select City",
                color = Color.White,
                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isUrdu) "شہر تلاش کریں..." else "Search city...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = activeColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredCities) { city ->
                        val isSelected = city.id == currentCityId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isUrdu) city.urduName else city.name,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeColor else Color.White,
                                    fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
                                )
                                Text(
                                    text = if (isUrdu) city.countryUrdu else city.country,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = activeColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "بند کریں" else "Cancel", color = activeColor)
            }
        }
    )
}

@Composable
private fun CustomCoordinateDialog(
    currentName: String,
    currentLat: Double,
    currentLng: Double,
    theme: LedTheme,
    language: AppLanguage,
    onSave: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var cityName by remember { mutableStateOf(currentName) }
    var latText by remember { mutableStateOf(currentLat.toString()) }
    var lngText by remember { mutableStateOf(currentLng.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    val activeColor = Color(theme.primaryHex)
    val isUrdu = language == AppLanguage.URDU

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MosqueCardDark,
        title = {
            Text(
                text = if (isUrdu) "مخصوص کوآرڈینیٹس" else "Custom Coordinates",
                color = Color.White,
                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Latitude (e.g. 24.8607)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("Longitude (e.g. 67.0011)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = error!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    if (lat == null || lng == null || lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
                        error = "Invalid coordinates. Lat must be -90..90, Lng -180..180"
                    } else {
                        onSave(cityName.ifBlank { "Custom Location" }, lat, lng)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeColor)
            ) {
                Text(if (isUrdu) "محفوظ کریں" else "Save", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "منسوخ" else "Cancel", color = Color.LightGray)
            }
        }
    )
}
