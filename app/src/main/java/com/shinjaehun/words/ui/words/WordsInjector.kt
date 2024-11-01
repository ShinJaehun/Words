package com.shinjaehun.words.ui.words

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.shinjaehun.words.data.WordsDao
import com.shinjaehun.words.data.WordsDatabase
import com.shinjaehun.words.data.providers.PreferenceProvider
import com.shinjaehun.words.data.repository.DatabaseRepository
import com.shinjaehun.words.data.repository.DatabaseRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class WordsInjector(
    application: Application
): AndroidViewModel(application) {

    private fun getRepository(): DatabaseRepository {
        return DatabaseRepositoryImpl(
            wordsDao = WordsDatabase.invoke(getApplication(), CoroutineScope(Job() + Dispatchers.Main)).wordsDao()
            // 근데 이렇게 해도 되는거 맞어?
            // coroutinescope가 필요한 이유가 뭐야?
        )
    }

    private fun getPreferenceProvider(): PreferenceProvider {
        return PreferenceProvider(getApplication())
    }

    fun provideWordsListViewModelFactory(): WordsViewModelFactory =
        WordsViewModelFactory(getRepository(), getPreferenceProvider())
}