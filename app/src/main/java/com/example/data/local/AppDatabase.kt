package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PharmacyProfile::class,
        Medication::class,
        OrderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pharmacyDao(): PharmacyDao
    abstract fun medicationDao(): MedicationDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharma_order_v2.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val pharmacyDao = database.pharmacyDao()
            val medicationDao = database.medicationDao()

            if (pharmacyDao.getProfile() == null) {
                pharmacyDao.saveProfile(
                    PharmacyProfile(
                        id = 1,
                        pharmacyName = "صيدلية النخبة الطبية",
                        pharmacistName = "د. أحمد عبد الله الشمري",
                        licenseNumber = "PH-94810",
                        phoneNumber = "+966 55 123 4567",
                        city = "الرياض",
                        neighborhood = "حي السليمانية",
                        streetAddress = "شارع الأمير ممدوح بن عبد العزيز، بجانب مجمع العيادات",
                        locationCoordinatesOrUrl = "https://maps.google.com/?q=24.7001,46.6845 (24°42'00\"N 46°41'04\"E)",
                        preferredSupplier = "شركة سقالة الدوائية المتطورة",
                        defaultNotes = "الرجاء فحص تاريخ الصلاحية والتسليم في الفترة الصباحية (9:00 ص - 1:00 م)",
                        isConfigured = true
                    )
                )
            }

            if (medicationDao.getCount() == 0) {
                val initialMedicines = listOf(
                    Medication(
                        tradeName = "Panadol Extra 500mg",
                        scientificName = "Paracetamol + Caffeine",
                        category = "مسكنات وخافضات حرارة",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 45,
                        minStockAlert = 15,
                        unitPrice = 18.50,
                        manufacturer = "GSK GlaxoSmithKline",
                        notes = "أقراص مغلفة سريعة الامتصاص"
                    ),
                    Medication(
                        tradeName = "Augmentin 1g",
                        scientificName = "Amoxicillin + Clavulanic Acid",
                        category = "مضادات حيوية",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 12,
                        minStockAlert = 15,
                        unitPrice = 64.00,
                        manufacturer = "GSK GlaxoSmithKline",
                        notes = "مضاد حيوي واسع المجال - تركيز 1 جرام"
                    ),
                    Medication(
                        tradeName = "Nexium 40mg",
                        scientificName = "Esomeprazole Magnesium",
                        category = "الجهاز الهضمي",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 8,
                        minStockAlert = 10,
                        unitPrice = 72.50,
                        manufacturer = "AstraZeneca",
                        notes = "علاج حموضة المعدة وارتجاع المريء"
                    ),
                    Medication(
                        tradeName = "Concor 5mg",
                        scientificName = "Bisoprolol Fumarate",
                        category = "أدوية القلب والضغط",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 30,
                        minStockAlert = 10,
                        unitPrice = 31.00,
                        manufacturer = "Merck",
                        notes = "خافض لضغط الدم ومنظم لضربات القلب"
                    ),
                    Medication(
                        tradeName = "Brufen 400mg",
                        scientificName = "Ibuprofen",
                        category = "مسكنات وخافضات حرارة",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 24,
                        minStockAlert = 10,
                        unitPrice = 22.00,
                        manufacturer = "Abbott",
                        notes = "مضاد للالتهاب ومسكن للآلام"
                    ),
                    Medication(
                        tradeName = "Zithromax 500mg",
                        scientificName = "Azithromycin",
                        category = "مضادات حيوية",
                        dosageForm = "كبسولات",
                        packageUnit = "علبة",
                        currentStock = 5,
                        minStockAlert = 10,
                        unitPrice = 48.00,
                        manufacturer = "Pfizer",
                        notes = "عبوة 3 كبسولات لالتهابات الجهاز التنفسي"
                    ),
                    Medication(
                        tradeName = "Aerius 5mg",
                        scientificName = "Desloratadine",
                        category = "الحساسية والتنفسي",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 18,
                        minStockAlert = 10,
                        unitPrice = 34.00,
                        manufacturer = "Bayer",
                        notes = "مضاد هيستامين لا يسبب النعاس"
                    ),
                    Medication(
                        tradeName = "Glucophage 1000mg",
                        scientificName = "Metformin Hydrochloride",
                        category = "أدوية السكري",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 50,
                        minStockAlert = 20,
                        unitPrice = 28.50,
                        manufacturer = "Merck",
                        notes = "منظم سكر الدم من النوع الثاني"
                    ),
                    Medication(
                        tradeName = "Lipitor 20mg",
                        scientificName = "Atorvastatin Calcium",
                        category = "أدوية القلب والضغط",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 14,
                        minStockAlert = 12,
                        unitPrice = 85.00,
                        manufacturer = "Pfizer",
                        notes = "مخفض للكوليسترول والدهون الثلاثية"
                    ),
                    Medication(
                        tradeName = "Neurobion Ampoules",
                        scientificName = "Vitamin B1 + B6 + B12",
                        category = "فيتامينات ومكملات",
                        dosageForm = "حقن",
                        packageUnit = "باكت",
                        currentStock = 16,
                        minStockAlert = 10,
                        unitPrice = 26.00,
                        manufacturer = "Merck",
                        notes = "حقن عضلية لدعم الأعصاب الطرفية"
                    ),
                    Medication(
                        tradeName = "Ventolin Inhaler 100mcg",
                        scientificName = "Salbutamol Sulfate",
                        category = "الحساسية والتنفسي",
                        dosageForm = "بخاخ",
                        packageUnit = "عبوة",
                        currentStock = 7,
                        minStockAlert = 10,
                        unitPrice = 33.00,
                        manufacturer = "GSK GlaxoSmithKline",
                        notes = "موسع للشعب الهوائية سريع المفعول"
                    ),
                    Medication(
                        tradeName = "Voltaren Emulgel 1%",
                        scientificName = "Diclofenac Diethylamine",
                        category = "مسكنات وخافضات حرارة",
                        dosageForm = "مرهم/جل",
                        packageUnit = "أنبوب",
                        currentStock = 22,
                        minStockAlert = 10,
                        unitPrice = 27.50,
                        manufacturer = "Novartis",
                        notes = "جل مسكن لآلام المفاصل والعضلات"
                    ),
                    Medication(
                        tradeName = "Prospan Cough Syrup",
                        scientificName = "Ivy Leaves Extract (Hedera)",
                        category = "الحساسية والتنفسي",
                        dosageForm = "شراب",
                        packageUnit = "قارورة",
                        currentStock = 19,
                        minStockAlert = 10,
                        unitPrice = 38.00,
                        manufacturer = "Engelhard",
                        notes = "شراب طبيعي مذيب وطارد للبلغم"
                    ),
                    Medication(
                        tradeName = "Vitamin D3 50,000 IU",
                        scientificName = "Cholecalciferol",
                        category = "فيتامينات ومكملات",
                        dosageForm = "كبسولات",
                        packageUnit = "علبة",
                        currentStock = 40,
                        minStockAlert = 15,
                        unitPrice = 45.00,
                        manufacturer = "Jamieson",
                        notes = "جرعة أسبوعية لرفع مخزون فيتامين د"
                    ),
                    Medication(
                        tradeName = "Flagyl 500mg",
                        scientificName = "Metronidazole",
                        category = "مضادات حيوية",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 35,
                        minStockAlert = 10,
                        unitPrice = 16.00,
                        manufacturer = "Sanofi",
                        notes = "مضاد للطفيليات والبكتيريا اللاهوائية"
                    ),
                    Medication(
                        tradeName = "Otrivin Adult 0.1%",
                        scientificName = "Xylometazoline Hydrochloride",
                        category = "الحساسية والتنفسي",
                        dosageForm = "قطرات/بخاخ",
                        packageUnit = "قارورة",
                        currentStock = 12,
                        minStockAlert = 10,
                        unitPrice = 15.00,
                        manufacturer = "Novartis",
                        notes = "بخاخ لإزالة احتقان الأنف والجيوب الأنفية"
                    ),
                    Medication(
                        tradeName = "Cataflam 50mg",
                        scientificName = "Diclofenac Potassium",
                        category = "مسكنات وخافضات حرارة",
                        dosageForm = "أقراص",
                        packageUnit = "علبة",
                        currentStock = 28,
                        minStockAlert = 12,
                        unitPrice = 29.50,
                        manufacturer = "Novartis",
                        notes = "مسكن سريع لآلام الأسنان والصداع"
                    ),
                    Medication(
                        tradeName = "Betadine Solution 10%",
                        scientificName = "Povidone Iodine 10%",
                        category = "مستلزمات طبية",
                        dosageForm = "محلول مطهر",
                        packageUnit = "قارورة",
                        currentStock = 25,
                        minStockAlert = 10,
                        unitPrice = 19.50,
                        manufacturer = "Mundipharma",
                        notes = "محلول تعقيم وتطهير الجروح"
                    )
                )
                medicationDao.insertAll(initialMedicines)
            }
        }
    }
}
