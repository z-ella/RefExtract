package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HistoryEntity::class, CustomRuleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun customRuleDao(): CustomRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ref_extract.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default extensible custom rules
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultDao = getInstance(context).customRuleDao()
                            defaultDao.insert(
                                CustomRuleEntity(
                                    id = "rule_jira",
                                    name = "Issue Tracking Ticket",
                                    regexPattern = "\\b[A-Z]{2,6}-\\d{1,6}\\b",
                                    categoryLabel = "TICKET_ID",
                                    defaultConfidence = 0.95f,
                                    description = "Matches project management identifiers (e.g., JIRA-1042, CORE-99)",
                                    isEnabled = true
                                )
                            )
                            defaultDao.insert(
                                CustomRuleEntity(
                                    id = "rule_isbn",
                                    name = "ISBN Book Identifier",
                                    regexPattern = "\\b(?:ISBN(?:-1[03])?:?\\s*)?(?=[0-9X]{10}${'$'}|(?=(?:[0-9]+[-\\s]){3})[-\\s0-9X]{13}${'$'}|97[89][0-9]{10}${'$'}|(?=(?:[0-9]+[-\\s]){4})[-\\s0-9]{17}${'$'})(?:97[89][-\\s]?)?[0-9]{1,5}[-\\s]?[0-9]+[-\\s]?[0-9]+[-\\s]?[0-9X]\\b",
                                    categoryLabel = "ISBN_NUMBER",
                                    defaultConfidence = 0.96f,
                                    description = "Matches international standard book numbers (ISBN-10 & ISBN-13)",
                                    isEnabled = true
                                )
                            )
                            defaultDao.insert(
                                CustomRuleEntity(
                                    id = "rule_email",
                                    name = "Email Address",
                                    regexPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
                                    categoryLabel = "EMAIL_CONTACT",
                                    defaultConfidence = 0.99f,
                                    description = "Matches standard RFC 5322 electronic mail endpoints",
                                    isEnabled = true
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
