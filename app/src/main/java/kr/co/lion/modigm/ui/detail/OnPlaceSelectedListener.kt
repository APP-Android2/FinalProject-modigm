package kr.co.lion.modigm.ui.detail

// PlaceBottomSheet와 DetailEditFragment간의 통신을 위한 인터페이스 정의(주소 선택)
interface OnPlaceSelectedListener {
    fun onPlaceSelected(placeName: String , detailPlaceNmae:String)
}