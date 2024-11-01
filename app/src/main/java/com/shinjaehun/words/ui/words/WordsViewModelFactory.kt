package com.shinjaehun.words.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shinjaehun.words.data.providers.PreferenceProvider
import com.shinjaehun.words.data.repository.DatabaseRepository

@Suppress("UNCHECKED_CAST")
class WordsViewModelFactory(
    private val databaseRepository: DatabaseRepository,
    private val prefsProvider: PreferenceProvider
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WordsViewModel(databaseRepository,prefsProvider) as T
    }
}