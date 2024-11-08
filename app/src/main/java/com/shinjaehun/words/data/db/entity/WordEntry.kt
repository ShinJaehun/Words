package com.shinjaehun.words.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "words_database")
data class WordEntry(
    @PrimaryKey(autoGenerate = false)
    val word: String = "",
    val desc: String = "",
    val likes: Int = 0,
    val uid: String? = null,
    @get:Exclude val isAlive: Boolean = true,
    @get:Exclude var swiped : Boolean = false
)