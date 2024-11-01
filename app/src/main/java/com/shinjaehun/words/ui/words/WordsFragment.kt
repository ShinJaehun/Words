package com.shinjaehun.words.ui.words

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.shinjaehun.words.R
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.data.helpers.ActionModeCallback
import com.shinjaehun.words.data.helpers.SwipeItemTouchHelper
import com.shinjaehun.words.databinding.FragmentWordsBinding
import com.shinjaehun.words.ui.adapters.WordsAdapter
import com.shinjaehun.words.ui.base.ScopedFragment
import com.shinjaehun.words.ui.dialogs.AddWordDialog
import kotlinx.coroutines.launch

private const val TAG = "WordsFragment"

class WordsFragment : ScopedFragment() {

    private lateinit var binding: FragmentWordsBinding

//    private val mViewModelFactory: WordsViewModelFactory by instance()
    private lateinit var mViewModelFactory: WordsViewModelFactory
    private lateinit var mViewModel: WordsViewModel
    private lateinit var mAdapter: WordsAdapter
    private lateinit var mDialog: AddWordDialog
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private lateinit var actionModeCallback: ActionModeCallback
    private lateinit var mObserver: Observer<List<WordEntry>>
    private var firestoreWords: LiveData<List<WordEntry>>? = null
    private var actionMode: ActionMode? = null
    var deleteListener: OnCallbackDestroy? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // deprecated
//        setHasOptionsMenu(true)
        // 이유를 모르겠는데 menu selected가 작동하지 않아서 settingFragment를 확인할 수 없음...
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val options = navOptions {
                    anim {
                        enter = R.anim.fade_in
                        exit = R.anim.fade_out
                        popEnter = R.anim.fade_in
                        popExit =R.anim.fade_out
                    }
                }
                when (menuItem.itemId) {
                    R.id.action_settings -> {
                        Log.i(TAG, "settings true?")
                        findNavController().navigate(R.id.settingsFragment, null, options)
                    }
                    else -> {
                        Log.i(TAG, "settings false?")
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding = FragmentWordsBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        mViewModel = ViewModelProvider(this, mViewModelFactory).get(WordsViewModel::class.java)
        mViewModel = ViewModelProvider(
            this,
            WordsInjector(requireActivity().application).provideWordsListViewModelFactory()
        ).get(WordsViewModel::class.java)

        mDialog = AddWordDialog(this.requireActivity())
        mAdapter = WordsAdapter(requireContext().applicationContext)

        mDialog.setTransferListener (
            wordCallback = {
                bindButton(it.getValue("word"), it.getValue("desc"))
            },
            listCallback = {
                addListOfWords(it, true)
            }
        )

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Words"

        initCallback()

        bindUI()

        initAdapterListeners()

        initRemoveWordsObserver()

        initActionModeCallback()

        initCallbackDestroy()

        binding.buttonAddWord.setOnClickListener { mDialog.show() }


    }

    private fun bindUI() = launch {
        kotlinx.coroutines.Runnable { binding.progressBar.visibility = View.VISIBLE }.run()

        val currentWords = mViewModel.words.await()

//        if (mViewModel.prefsProvider.isSyncFirstLoad &&
//            mViewModel.prefsProvider.isFullSyncNeeded) {
//            firestoreWords = mViewModel.firestoreWords.await()
//            firestoreWords!!.observe(viewLifecycleOwner, mObserver)
//        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext().applicationContext)
        binding.recyclerView.adapter = mAdapter

        currentWords.observe(
            viewLifecycleOwner,
            Observer { words ->
                binding.progressBar.visibility = View.GONE
                if (words == null) return@Observer

                handleList(words)
            }
        )
    }

    private fun initAdapterListeners() {
        mAdapter.setOnWordClickListener(
            deleteCallback = {
                bindDeleting(it)
                deleteListener?.destroyCallback()
            },
            longCallback = {
                enableActionMode(it)
            },
            clickCallback = {
                if (mAdapter.getSelectedItemCount() > 0)
                    enableActionMode(it)
                else {
                    // Todo
                }
            }
        )
    }

    private fun bindButton(word: String, desc: String) {
        if(word.isNotEmpty() && desc.isNotEmpty()) {
            mViewModel.addWord(WordEntry(word, desc, uid = mViewModel.prefsProvider.currentSyncUid))
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun bindDeleting(
        word: WordEntry? = null,
        words: List<WordEntry>? = null
    ) = launch {
        mViewModel.deleteWords(word = word, words = words)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun initRemoveWordsObserver() {
        mObserver = Observer { word ->
            if (word == null) return@Observer
            Log.i(TAG, "word size: ${word.size.toString()}")
            addListOfWords(word)
            mViewModel.prefsProvider.isSyncFirstLoad = false
            binding.progressBar.visibility = View.GONE
            firestoreWords?.removeObserver(mObserver)
        }
    }

    private fun initCallbackDestroy() {
        deleteListener = object : OnCallbackDestroy {
            override fun destroyCallback() {
                actionModeCallback.destroyCallback()
            }
        }
    }

    private fun enableActionMode(position: Int) {
        if(actionMode == null) {
            actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        mAdapter.toggleSelection(position)
        val count = mAdapter.getSelectedItemCount()
        if (count == 0)
            actionMode?.finish()
        else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
        }
    }

    private fun deleteAllWords() {
        val selectedItemPositions = mAdapter.getSelectedItems()
        val list = mutableListOf<WordEntry>()

        for (i in selectedItemPositions.size - 1 downTo  0) {
            list.add(mAdapter.getItem(selectedItemPositions[i]))
            mAdapter.removeData(selectedItemPositions[i])
        }
        bindDeleting(words = list)
        mAdapter.notifyDataSetChanged()
    }

    private fun initActionModeCallback() {
        actionModeCallback = ActionModeCallback(requireActivity())
        actionModeCallback.setOnDestroyListener(
            deleteCallback = {
                deleteAllWords()
            },
            destroyCallback = {
                mAdapter.clearSelections()
                actionMode = null
            }
        )
    }

    private fun addListOfWords(words: List<WordEntry>, isFirestoreNeeded: Boolean = false) =launch {
        mViewModel.addListOfWords(words, isFirestoreNeeded)
    }

    private fun handleList(words: List<WordEntry>) = launch {
        val currentList = mViewModel.commitSynchronizationAsync(words).await()

        mAdapter.setWords(currentList?.filter { it.isAlive } as ArrayList<WordEntry>?)
    }

    private fun initCallback() {
        val callback = SwipeItemTouchHelper(mAdapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    // deprecated
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_main, menu)
//    }


    // deprecated
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val options = navOptions {
//            anim {
//                enter = R.anim.fade_in
//                exit = R.anim.fade_out
//                popEnter = R.anim.fade_in
//                popExit =R.anim.fade_out
//            }
//        }
//        when (item.itemId) {
//            R.id.action_settings -> {
//                findNavController().navigate(R.id.settingsFragment, null, options)
//                Log.i(TAG, "settings?")
//            }
//        }
//        return true
//    }

    interface OnCallbackDestroy {
        fun destroyCallback()
    }
}