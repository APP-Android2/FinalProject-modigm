package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentPlaceBottomSheetBinding
import kr.co.lion.modigm.ui.detail.adapter.PlaceSearchResultsAdapter
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.ui.VBBaseBottomSheetFragment

data class SimplePlace(
    val id: String,
    val name: String,
    val address: String
)

class PlaceBottomSheetFragment : VBBaseBottomSheetFragment<FragmentPlaceBottomSheetBinding>(FragmentPlaceBottomSheetBinding::inflate) {

    private lateinit var placesClient: PlacesClient

    private var placeSelectedListener: OnPlaceSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottomSheet 배경 설정
        view.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.PLACE_API_KEY)
        }
        placesClient = Places.createClient(requireContext())

        setupSearchBar()

        // ImageView 클릭 시 BottomSheet 닫기
        binding.imageViewPlaceBottomSheetClose.setOnClickListener {
            dismiss()  // BottomSheetDialogFragment의 dismiss() 메서드를 호출하여 바텀 시트를 닫음
        }
    }

    // 선택된 주소 감지 이벤트 트리거
    fun setOnPlaceSelectedListener(listener: OnPlaceSelectedListener) {
        placeSelectedListener = listener
    }

    fun setupSearchBar() {
        val adapter = PlaceSearchResultsAdapter(mutableListOf()) { place ->
            placeSelectedListener?.onPlaceSelected(place.name, place.address)
            dismiss()  // 바텀 시트 닫기
        }
        binding.recyclerViewSearchResults.adapter = adapter

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchPlace(s.toString(), adapter)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val dialog = dialog as? BottomSheetDialog

        dialog?.let {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            // 바텀 시트의 드래그를 비활성화(x버튼을 눌러야만 닫히고 손으로 잡아끌 수 없음)
            bottomSheetBehavior.isDraggable = false

            // 화면 높이의 80%로 설정
            bottomSheet.layoutParams.height = (getScreenHeight() * 0.85).toInt()
        }
    }

    fun getScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun searchPlace(query: String, adapter: PlaceSearchResultsAdapter) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val places = response.autocompletePredictions.map { prediction ->
                SimplePlace(
                    id = prediction.placeId,
                    name = prediction.getPrimaryText(null).toString(),
                    address = prediction.getSecondaryText(null).toString()
                )
            }
            if (places.isNotEmpty()) {
                adapter.updateItems(places)
                binding.recyclerViewSearchResults.visibility = View.VISIBLE
                binding.LayoutPlaceBlank.visibility = View.GONE
            } else {
                binding.recyclerViewSearchResults.visibility = View.GONE
                binding.LayoutPlaceBlank.visibility = View.VISIBLE
            }
            Log.d("SearchResults", "Found ${places.size} places")
        }.addOnFailureListener { exception ->
            binding.recyclerViewSearchResults.visibility = View.GONE
            binding.LayoutPlaceBlank.visibility = View.VISIBLE
            Log.e("SearchResults", "Error retrieving places: ${exception.localizedMessage}")
        }
    }


}