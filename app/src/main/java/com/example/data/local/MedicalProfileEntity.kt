package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_profile")
data class MedicalProfileEntity(
    @PrimaryKey
    val id: String = "primary_user_medical_profile",
    val fullName: String,
    val bloodGroup: String, // e.g. "O+", "A+", "B+", "AB-", "Unknown"
    val age: Int,
    val severeAllergies: String, // e.g. "Penicillin, Peanuts"
    val chronicConditions: String, // e.g. "Type 1 Diabetes, Asthma"
    val regularMedications: String, // e.g. "Insulin, Albuterol"
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val emergencyContactRelation: String,
    val isOrganDonor: Boolean = false,
    val medicalNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
