package kr.co.lion.modigm.ui.join.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.UserInfoRepository

class JoinViewModel : ViewModel() {

    // 리포지터리
    private val _userInfoRepository = UserInfoRepository()
    // 파이어베이스 인증
//    private val _auth = FirebaseAuth.getInstance()
//    private var _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
//    val user: LiveData<FirebaseUser?> = _user
//    private var _smsCode: MutableLiveData<String> = MutableLiveData()



    // 회원가입 이탈 시 이미 Auth에 등록되어있는 정보 삭제
    fun deleteUserAuth(){
        viewModelScope.launch {
//            if(_auth.currentUser != null){
//                _auth.currentUser?.delete()
//            }
        }
    }

    // UserInfoData 객체 생성
    fun createUserInfoData(): UserData {
        // 각 화면에서 응답받은 정보 가져오기

        // 회원 정보 객체 생성

        // return
        return UserData()
    }

    // 이메일 계정 회원 가입 완료
    fun completeJoinEmailUser(user: UserData){
        // UserInfoData 객체 생성

        // 파이어 스토어에 저장
    }
    // SNS 계정 회원 가입 완료
    fun completeJoinSnsUser(user: UserData){
        // UserInfoData 객체 생성

        // 파이어 스토어에 저장
    }
}