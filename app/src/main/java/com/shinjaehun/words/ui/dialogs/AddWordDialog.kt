package com.shinjaehun.words.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.shawnlin.numberpicker.NumberPicker
import com.shinjaehun.words.R
import com.shinjaehun.words.data.db.entity.WordEntry
import com.shinjaehun.words.internal.coordinateButtonWithInputs
import com.shinjaehun.words.internal.setFadeInAnimationForViews
import com.shinjaehun.words.internal.setFadeOutAnimationForViews


class AddWordDialog(context: Context) : Dialog(context) {

    private lateinit var imm : InputMethodManager

    private var isEditEnabled = false

    private var currentNumber = 0

    private val wordsList = mutableListOf(
        WordEntry("하나", "One"),
        WordEntry("둘", "Two"),
        WordEntry("셋", "Three"),
        WordEntry("넷", "Four"),
        WordEntry("다섯", "Five"),
        WordEntry("여섯", "Six"),
        WordEntry("일곱", "Seven"),
        WordEntry("여덟", "Eight"),
        WordEntry("아홉", "Nine"),
        WordEntry("열", "Ten")
    )

    override fun show() {
        super.show()

        val et_desc = findViewById<EditText>(R.id.et_desc)
        val et_word = findViewById<EditText>(R.id.et_word)

        et_desc.setText("")
        et_word.setText("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.add_word_dialog)
        setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        val bt_submit = findViewById<AppCompatButton>(R.id.bt_submit)
        val bt_create = findViewById<ImageButton>(R.id.bt_create)
        val bt_cancel = findViewById<ImageButton>(R.id.bt_cancel)
        val et_desc = findViewById<EditText>(R.id.et_desc)
        val et_word = findViewById<EditText>(R.id.et_word)

        coordinateButtonWithInputs(bt_submit, et_desc, et_word)

        imm = (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)

        bt_submit.setOnClickListener {
            dismiss()
            if (!isEditEnabled)
                transferListener?.transferFields(et_word.text.toString(), et_desc.text.toString())
            else {
                transferListener?.transferList(wordsList.take(currentNumber))
                isEditEnabled = false
            }
        }

        initNumberPicker()

        bt_create.setOnClickListener {
            isEditEnabled = !isEditEnabled
            imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).rootView.windowToken,0)
            changeVisibility(isEditEnabled)
        }

        bt_cancel.setOnClickListener {
            isEditEnabled = !isEditEnabled
            changeVisibility(isEditEnabled)
        }

        window!!.attributes = lp
    }

    private fun changeVisibility(isEdit: Boolean) {
        val bt_submit = findViewById<AppCompatButton>(R.id.bt_submit)
        val bt_create = findViewById<ImageButton>(R.id.bt_create)
        val bt_cancel = findViewById<ImageButton>(R.id.bt_cancel)
        val et_desc = findViewById<EditText>(R.id.et_desc)
        val et_word = findViewById<EditText>(R.id.et_word)
        val relative_edit = findViewById<RelativeLayout>(R.id.relative_edit)
        val relative_picker = findViewById<LinearLayout>(R.id.relative_picker)
        val text_random = findViewById<TextView>(R.id.text_random)

        if (isEdit) {
            setFadeInAnimationForViews(bt_cancel, relative_picker)
            setFadeOutAnimationForViews(bt_create, relative_edit,text_random)
            if (currentNumber != 0) bt_submit.isEnabled = true
        } else {
            setFadeInAnimationForViews(bt_create, relative_edit,text_random)
            setFadeOutAnimationForViews(bt_cancel, relative_picker)
            if (et_desc.text.isEmpty() || et_word.text.isEmpty())
                bt_submit.isEnabled = false
        }
    }

    private fun setDefaultVisibility(){
        val bt_create = findViewById<ImageButton>(R.id.bt_create)
        val bt_cancel = findViewById<ImageButton>(R.id.bt_cancel)
        val relative_edit = findViewById<RelativeLayout>(R.id.relative_edit)
        val relative_picker = findViewById<LinearLayout>(R.id.relative_picker)
        val text_random = findViewById<TextView>(R.id.text_random)

        bt_cancel.visibility = View.GONE
        bt_create.visibility = View.VISIBLE
        relative_picker.visibility = View.GONE
        relative_edit.visibility = View.VISIBLE
        text_random.visibility = View.VISIBLE
    }

    interface OnTransferInfoListener {
        fun transferFields(wordString: String, descString: String)
        fun transferList(words: List<WordEntry>)
    }

    private fun initNumberPicker() {
        val number_picker = findViewById<NumberPicker>(R.id.number_picker)
        val bt_submit = findViewById<AppCompatButton>(R.id.bt_submit)

        with(number_picker) {
            maxValue = 10
            minValue = 0
            value = 1
            isFadingEdgeEnabled = true
            setOnValueChangedListener { picker, oldVal, newVal ->
                if (newVal != 0) {
                    currentNumber = newVal
                    bt_submit.isEnabled = true
                } else
                    bt_submit.isEnabled = false
            }
        }
    }

    var transferListener: OnTransferInfoListener? = null

    inline fun setTransferListener(
        crossinline wordCallback: (Map<String, String>) -> Unit = {},
        crossinline listCallback: (List<WordEntry>) -> Unit = {}
    ) {
        transferListener = object : OnTransferInfoListener {
            override fun transferFields(wordString: String, descString: String) {
                wordCallback(mapOf("word" to wordString, "desc" to descString))
            }

            override fun transferList(words: List<WordEntry>) {
                listCallback(words)
            }
        }
    }

}