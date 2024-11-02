package com.shinjaehun.words.data.repository

import androidx.lifecycle.LiveData
import com.shinjaehun.words.data.WordsDao
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.data.utils.FirebaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepositoryImpl(
    private val wordsDao: WordsDao
) : DatabaseRepository {
    override suspend fun getListOfWords(): LiveData<List<WordEntry>> =
        withContext(Dispatchers.IO) {
            return@withContext wordsDao.getWordsList()
        }

    override suspend fun deleteAll() =
        withContext(Dispatchers.IO) {
            wordsDao.deleteAll()
        }

    override suspend fun addWord(word: WordEntry) =
        withContext(Dispatchers.IO) {
            wordsDao.insert(word)
        }

    override suspend fun deleteWord(word: WordEntry, isDelete: Boolean) =
        withContext(Dispatchers.IO) {
            if (isDelete || FirebaseUtil.uid == null)
                wordsDao.deleteWord(word.word)
            else
                wordsDao.insert(word) // 근데 왜 delete()가 아닌 insert()?????
            //        wordsDao.deleteWord(word.word)
        }

    override suspend fun getListFromFirebase(): LiveData<List<WordEntry>> =
        withContext(Dispatchers.IO) {
            FirebaseUtil.liveDataList
        }

    override suspend fun addListOfWords(words: List<WordEntry>) =
        withContext(Dispatchers.IO) {
            wordsDao.insertList(words)
        }
}