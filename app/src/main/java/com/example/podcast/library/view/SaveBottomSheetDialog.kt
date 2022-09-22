package com.example.podcast.library.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.podcast.Extras
import com.example.podcast.databinding.FSaveBottomSheetBinding
import com.example.podcast.library.utils.hide
import com.example.podcast.library.utils.show
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SaveBottomSheetDialog: BottomSheetDialogFragment() {

    companion object {
        fun newInstance(
            title: String,
            button: String?,
            requestCode: String? = null
        ): SaveBottomSheetDialog {
            return SaveBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString(Extras.SAVE_BOTTOM_SHEET_TITLE, title)
                    putString(Extras.SAVE_BOTTOM_SHEET_BUTTON, button)
                    putString(Extras.REQUEST_CODE, requestCode)
                }
            }
        }
    }

    private lateinit var binding: FSaveBottomSheetBinding
    var onSave: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FSaveBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.tvTitle.text = arguments?.getString(Extras.SAVE_BOTTOM_SHEET_TITLE)
        val buttonStr = arguments?.getString(Extras.SAVE_BOTTOM_SHEET_BUTTON)
        if (buttonStr.isNullOrBlank()) {
            binding.btnOK.hide()
        } else {
            binding.btnOK.show()
            binding.btnOK.text = arguments?.getString(Extras.SAVE_BOTTOM_SHEET_BUTTON)
            binding.btnOK.setOnClickListener {
                onSave?.invoke(binding.etFilename.text.toString())
                dismiss()
            }
        }
    }

}
