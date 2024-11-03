package com.shinjaehun.words.ui.words

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.data.providers.PreferenceProvider
import com.shinjaehun.words.data.repository.DatabaseRepository
import com.shinjaehun.words.data.utils.FirebaseUtil
import com.shinjaehun.words.internal.commitWords
import com.shinjaehun.words.internal.deleteWord
import com.shinjaehun.words.internal.lazyDeferred
import com.shinjaehun.words.internal.setWord
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "WordsViewModel"

class WordsViewModel(
    private val databaseRepository: DatabaseRepository,
    val prefsProvider: PreferenceProvider
) : ViewModel() {

    fun addWord(word: WordEntry) {
        if (prefsProvider.isParticularSyncNeeded) {
            FirebaseUtil.setCurrentWord(word) {
                if (!it.isSuccessful)
                    prefsProvider.isSyncNeeded = true

                addScopedWord(word)
            }
        } else prefsProvider.setSyncConditions {
            addScopedWord(word)
        }
//        addScopedWord(word)
    }

    fun deleteWords(
        word: WordEntry? = null,
        words: List<WordEntry>? = null
    ) {
        Log.i(TAG, "deleteWords - word: $word, words $words")

        if (prefsProvider.isParticularSyncNeeded) {
            Log.i(TAG, "deleteWords - prefsProvider.isParticularSyncNeeded: ${prefsProvider.isParticularSyncNeeded}")
            if (word != null) {
                //여기서 FB와 RDB 양쪽 모두 바로 삭제함....
                FirebaseUtil.deleteCurrentWord(word) {
                    if (!it.isSuccessful)
                        prefsProvider.setSyncConditions {
                            deleteScopedWord(word.copy(isAlive = false), false)
                        }
                    else
                        deleteScopedWord(word)
                }
            } else if (words != null) {
                // FB는 삭제되는데
                // RDB에서는 그대로 살아 있고 isAlive만 0으로 처리됨
                val batch = FirebaseUtil.firestore.batch()
                words.forEach {
                    batch.deleteWord(it.word)
                }
                batch.commitWords {
                    words.forEach {
                        deleteScopedWord(it.copy(isAlive = false), false)
                    }
                }
            }
        } else prefsProvider.setSyncConditions {
            word?.let {
                deleteScopedWord(word.copy(isAlive = false), false)
            }
            words?.forEach {
                deleteScopedWord(it.copy(isAlive = false), false)
            }
        }
//        word?.let {
//            deleteScopedWord(word.copy(isAlive = false), false)
//        }
//        words?.forEach {
//            deleteScopedWord(it.copy(isAlive = false), false)
//        }
    }

    fun commitSynchronizationAsync(words: List<WordEntry>?) = viewModelScope.async {
        if (prefsProvider.isFullSyncNeeded) {
            val batch = FirebaseUtil.firestore.batch()

            Log.i(TAG, "prefsProvider.currentSyncUid: ${prefsProvider.currentSyncUid}")
            Log.i(TAG, "words: $words")

            words?.forEach {
                // FB에서 isAlive가 0인 자료 삭제
                // uid가 null이면 uid 채워줌
                when {
                    !it.isAlive -> batch.deleteWord(it.word)
                    it.uid == prefsProvider.currentSyncUid -> batch.setWord(it)
                    it.uid == null ->
                        batch.setWord(it.copy(uid = prefsProvider.currentSyncUid))
                }
            }

            batch.commitWords {
                // RDB에서 isAlive가 0인 자료 삭제
                // 당연히 딴 애들 자료도 삭제(이게 들어가 있으면 안되는거지?)
                Log.i(TAG, "이게 실행되어야 RoomDB에서도 삭제되는 거 아니?")
                prefsProvider.isSyncNeeded = false
                words?.forEach { word ->
                    if (!word.isAlive || (word.uid != null && word.uid != FirebaseUtil.uid))
                        deleteScopedWord(word)
                }
            }

            words

        } else words
//        words
    }

    fun addListOfWords(words: List<WordEntry>, isFirestoreNeeded: Boolean = false) = viewModelScope.launch {
        if (isFirestoreNeeded) {
            val batch = FirebaseUtil.firestore.batch()

            words.forEach { batch.setWord(it) }

            batch.commitWords {
                addScopedListOfWords(words)
                prefsProvider.isSyncNeeded = true
            }
        } else
            databaseRepository.addListOfWords(words)
//        databaseRepository.addListOfWords(words)

    }

    private fun addScopedListOfWords(words: List<WordEntry>) = viewModelScope.launch {
        databaseRepository.addListOfWords(words)
    }

    private fun addScopedWord(word: WordEntry) = viewModelScope.launch {
        databaseRepository.addWord(word)
    }

    private fun deleteScopedWord(word: WordEntry, isDelete: Boolean = true) = viewModelScope.launch {
        Log.i(TAG, "진짜 지우니? word: ${word.word} isDelete: $isDelete")
        databaseRepository.deleteWord(word, isDelete)
    }

    val words by lazyDeferred {
        databaseRepository.getListOfWords()
    }

    val firestoreWords
        get() = viewModelScope.async {
            databaseRepository.getListFromFirebase()
        }


}