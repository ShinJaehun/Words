package com.shinjaehun.words.data.repository

import androidx.lifecycle.LiveData
import com.shinjaehun.words.data.db.entity.WordEntry

interface DatabaseRepository {
    suspend fun getListOfWords() : LiveData<List<WordEntry>>

    suspend fun addWord(word: WordEntry)

    suspend fun deleteWord(word: WordEntry, isDelete: Boolean = true)

    suspend fun deleteAll()

//    suspend fun getListFromFirebase() : LiveData<List<WordEntry>>

    suspend fun addListOfWords(words: List<WordEntry>)
}