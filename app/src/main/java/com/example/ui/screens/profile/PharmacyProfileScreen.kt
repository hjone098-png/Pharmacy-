package com.example.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PharmacyProfile
import com.example.ui.navigation.Screen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TealContainer

@Composable
fun PharmacyProfileScreen(
    currentProfile: PharmacyProfile?,
    onSaveProfile: (PharmacyProfile) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCatalog: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var pharmacyName by remember { mutableStateOf(currentProfile?.pharmacyName.orEmpty()) }
    var pharmacistName by remember { mutableStateOf(currentProfile?.pharmacistName.orEmpty()) }
    var licenseNumber by remember { mutableStateOf(currentProfile?.licenseNumber.orEmpty()) }
    var phoneNumber by remember { mutableStateOf(currentProfile?.phoneNumber.orEmpty()) }
    var city by remember { mutableStateOf(currentProfile?.city.orEmpty()) }
    var neighborhood by remember { mutableStateOf(currentProfile?.neighborhood.orEmpty()) }
    var streetAddress by remember { mutableStateOf(currentProfile?.streetAddress.orEmpty()) }
    var locationCoordinatesOrUrl by remember { mutableStateOf(currentProfile?.locationCoordinatesOrUrl.orEmpty()) }
    var preferredSupplier by remember { mutableStateOf(currentProfile?.preferredSupplier.orEmpty()) }
    var defaultNotes by remember { mutableStateOf(currentProfile?.defaultNotes.orEmpty()) }

    var hasValidationAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(currentProfile) {
        if (currentProfile != null) {
            pharmacyName = currentProfile.pharmacyName
            pharmacistName = currentProfile.pharmacistName
            licenseNumber = currentProfile.licenseNumber
            phoneNumber = currentProfile.phoneNumber
            city = currentProfile.city
            neighborhood = currentProfile.neighborhood
            streetAddress = currentProfile.streetAddress
            locationCoordinatesOrUrl = currentProfile.locationCoordinatesOrUrl
            preferredSupplier = currentProfile.preferredSupplier
            defaultNotes = currentProfile.defaultNotes
        }
    }

    val isNameValid = pharmacyName.trim().isNotBlank()
    val isPhoneValid = phoneNumber.trim().isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pharmacy_profile_screen")
    ) {
        // Hero Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTeal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "تسجيل بيانات الصيدلية والموقع",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تُدرج هذه البيانات تلقائياً في ترويسة جميع طلبيات الأدوية لتسهيل وصول الموزعين ومندوبي التوصيل.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Basic Info Section
        Text(
            text = "البيانات الأساسية للصيدلية",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = pharmacyName,
            onValueChange = { pharmacyName = it },
            label = { Text("اسم الصيدلية *") },
            placeholder = { Text("مثال: صيدلية الأمل المركزية") },
            leadingIcon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = PrimaryTeal) },
            isError = hasValidationAttempted && !isNameValid,
            supportingText = {
                if (hasValidationAttempted && !isNameValid) {
                    Text("اسم الصيدلية مطلوب", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_pharmacy_name"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = pharmacistName,
                onValueChange = { pharmacistName = it },
                label = { Text("الصيدلي المسؤول") },
                placeholder = { Text("د. أحمد") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal) },
                singleLine = true,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("input_pharmacist_name"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text("رقم الترخيص") },
                placeholder = { Text("مثال: 94810") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryTeal) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_license_number"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("رقم الهاتف أو الواتساب للتواصل *") },
            placeholder = { Text("مثال: +966 50 123 4567") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryTeal) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = hasValidationAttempted && !isPhoneValid,
            supportingText = {
                if (hasValidationAttempted && !isPhoneValid) {
                    Text("رقم الهاتف مطلوب لتواصل الموزعين", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_phone_number"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Geographic Location Section
        Text(
            text = "الموقع الجغرافي والعنوان التفصيلي",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("المدينة") },
                placeholder = { Text("مثال: الرياض") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = PrimaryTeal) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_city"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = neighborhood,
                onValueChange = { neighborhood = it },
                label = { Text("الحي / المنطقة") },
                placeholder = { Text("مثال: حي النخيل") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryTeal) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_neighborhood"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = streetAddress,
            onValueChange = { streetAddress = it },
            label = { Text("العنوان التفصيلي وأقرب معلم") },
            placeholder = { Text("مثال: شارع الملك فهد، بجوار مجمع العيادات الطبي") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal) },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_street_address"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = locationCoordinatesOrUrl,
            onValueChange = { locationCoordinatesOrUrl = it },
            label = { Text("إحداثيات GPS أو رابط خرائط Google") },
            placeholder = { Text("https://maps.google.com/?q=24.7136,46.6753") },
            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryTeal) },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_location_url"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                Text(
                    text = "سيظهر هذا الرابط في رسالة الطلبية ليتمكن الموزع من فتح الموقع مباشرة بنقرة واحدة.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Supplier & Delivery Preferences
        Text(
            text = "تفضيلات التوريد والملاحظات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = preferredSupplier,
            onValueChange = { preferredSupplier = it },
            label = { Text("الموزع أو المستودع المعتمد") },
            placeholder = { Text("مثال: مستودع سقالة، المتحدة، الشرق الأوسط") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryTeal) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_supplier"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = defaultNotes,
            onValueChange = { defaultNotes = it },
            label = { Text("ملاحظات التوريد الدائمة (أوقات الاستلام وشروط التوصيل)") },
            placeholder = { Text("مثال: يرجى التوصيل في الفترة الصباحية من 9 ص حتى 1 م مع فحص تاريخ الصلاحية") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryTeal) },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_default_notes"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = {
                hasValidationAttempted = true
                if (isNameValid && isPhoneValid) {
                    val updated = PharmacyProfile(
                        id = 1,
                        pharmacyName = pharmacyName.trim(),
                        pharmacistName = pharmacistName.trim(),
                        licenseNumber = licenseNumber.trim(),
                        phoneNumber = phoneNumber.trim(),
                        city = city.trim(),
                        neighborhood = neighborhood.trim(),
                        streetAddress = streetAddress.trim(),
                        locationCoordinatesOrUrl = locationCoordinatesOrUrl.trim(),
                        preferredSupplier = preferredSupplier.trim(),
                        defaultNotes = defaultNotes.trim(),
                        isConfigured = true
                    )
                    onSaveProfile(updated)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_profile_btn")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ بيانات الصيدلية والموقع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToCatalog,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("go_to_catalog_btn")
        ) {
            Text(
                text = "الانتقال لاختيار المواد والكميات ➔",
                color = PrimaryTeal,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
