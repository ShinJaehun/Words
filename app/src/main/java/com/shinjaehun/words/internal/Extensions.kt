package com.shinjaehun.words.internal

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.data.db.livedata.QueryLiveData
import com.shinjaehun.words.data.utils.FirebaseUtil

fun Context.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun <T> Query.liveData(clazz: Class<T>, parser: (DocumentSnapshot) -> Boolean): LiveData<List<T>> =
    QueryLiveData(this, parser = parser, clazz = clazz)

fun WriteBatch.deleteWord(word: String) = delete(
    FirebaseUtil
        .currentWordsDocRef.document(word)
)

fun WriteBatch.setWord(word: WordEntry) = set(
    FirebaseUtil.currentWordsDocRef.document(word.word),
    word,
    SetOptions.merge()
)

fun WriteBatch.commitWords(onSuccess: () -> Unit) =
    commit().addOnCompleteListener {
        if (it.isSuccessful) {
            onSuccess()
        }
    }
