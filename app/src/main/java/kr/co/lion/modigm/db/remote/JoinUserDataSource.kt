package kr.co.lion.modigm.db.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.UserInfoData

class JoinUserDataSource() {
    private val db = Firebase.firestore
    private val userCollection = db.collection("User")

    //사용자 정보 저장
    suspend fun insetUserData(userInfoData: UserInfoData): Boolean{
        return try {
            userCollection.add(userInfoData).await()
            true
        }catch (error: Exception){
            Log.e("Modigm_Error","insetUserData() error : $error")
            false
        }
    }

}

