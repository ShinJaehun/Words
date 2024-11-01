package com.shinjaehun.words.ui.words

import android.util.Log
import androidx.lifecycle.ViewModel
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.data.providers.PreferenceProvider
import com.shinjaehun.words.data.repository.DatabaseRepository
import com.shinjaehun.words.data.utils.FirebaseUtil
import com.shinjaehun.words.internal.commitWords
import com.shinjaehun.words.internal.lazyDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "WordsViewModel"

class WordsViewModel(
    private val databaseRepository: DatabaseRepository,
    val prefsProvider: PreferenceProvider
) : ViewModel() {

    fun addWord(word: WordEntry) {
//        if (prefsProvider.isParticularSyncNeeded) {
//            FirebaseUtil.setCurrentWord(word) {
//                if (!it.isSuccessful)
//                    prefsProvider.isSyncNeeded = true
//
//                addScopedWord(word)
//            }
//        } else prefsProvider.setSyncConditions {
//            addScopedWord(word)
//        }
        addScopedWord(word)
    }

    fun deleteWords(
        word: WordEntry? = null,
        words: List<WordEntry>? = null
    ) {
        Log.i(TAG, "deleteWords - word: $word, words $words")

//        if (prefsProvider.isParticularSyncNeeded) {
//            Log.i(TAG, "deleteWords - prefsProvider.isParticularSyncNeeded: ${prefsProvider.isParticularSyncNeeded}")
//            if (word != null) {
//                FirebaseUtil.deleteCurrentWord(word) {
//                    if (!it.isSuccessful)
//                        prefsProvider.setSyncConditions {
//                            deleteScopedWord(word.copy(isAlive = false), false)
//                        }
//                    else
//                        deleteScopedWord(word)
//                }
//            } else if (words != null) {
//                val batch = FirebaseUtil.firestore.batch()
//                words.forEach {
//                    batch.deleteWord(it.word)
//                }
//                batch.commitWords {
//                    words.forEach {
//                        deleteScopedWord(it.copy(isAlive = false), false)
//                    }
//                }
//            }
//        } else prefsProvider.setSyncConditions {
//            word?.let {
//                deleteScopedWord(word.copy(isAlive = false), false)
//            }
//            words?.forEach {
//                deleteScopedWord(it.copy(isAlive = false), false)
//            }
//
//        }
        word?.let {
            deleteScopedWord(word.copy(isAlive = false), false)
        }
        words?.forEach {
            deleteScopedWord(it.copy(isAlive = false), false)
        }
    }

    fun commitSynchronizationAsync(words: List<WordEntry>?) = GlobalScope.async {
//        if (prefsProvider.isFullSyncNeeded) {
//            val batch = FirebaseUtil.firestore.batch()
//
//            words?.forEach {
//                when {
//                    !it.isAlive -> batch.deleteWord(it.word)
//                    it.uid == prefsProvider.currentSyncUid -> batch.setWord(it)
//                    it.uid == null ->
//                        batch.setWord(it.copy(uid = prefsProvider.currentSyncUid))
//                }
//            }
//
//            batch.commitWords {
//                prefsProvider.isSyncNeeded = false
//                words?.forEach { word ->
//                    if (!word.isAlive || (word.uid != null && word.uid != FirebaseUtil.uid))
//                        deleteScopedWord(word)
//                }
//            }
//
//            words
//
//        } else words
        words
    }

    fun addListOfWords(words: List<WordEntry>, isFirestoreNeeded: Boolean = false) = GlobalScope.launch {
//        if (isFirestoreNeeded) {
//            val batch = FirebaseUtil.firestore.batch()
//
//            words.forEach { batch.setWord(it) }
//
//            batch.commitWords {
//                addScopedListOfWords(words)
//                prefsProvider.isSyncNeeded = true
//            }
//        } else
//            databaseRepository.addListOfWords(words)
        databaseRepository.addListOfWords(words)

    }

    private fun addScopedListOfWords(words: List<WordEntry>) = GlobalScope.launch {
        databaseRepository.addListOfWords(words)
    }

    private fun addScopedWord(word: WordEntry) = GlobalScope.launch {
        databaseRepository.addWord(word)
    }

    private fun deleteScopedWord(word: WordEntry, isDelete: Boolean = true) = GlobalScope.launch {
        Log.i(TAG, "진짜 지우니?")
        databaseRepository.deleteWord(word, isDelete)
    }

    val words by lazyDeferred {
        databaseRepository.getListOfWords()
    }

//    val firestoreWords
//        get() = GlobalScope.async {
//            databaseRepository.getListFromFirebase()
//        }


}