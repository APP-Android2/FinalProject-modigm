package kr.co.lion.modigm.db.user

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.UserData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteUserDataSource {
    private val db = Firebase.firestore
    private val userCollection = db.collection("User")
    private val auth = Firebase.auth
    private val functions = Firebase.functions("asia-northeast3")



    //사용자 정보 저장
    suspend fun insetUserData(userInfoData: UserData): Boolean{
        return try {
            userCollection.add(userInfoData).await()
            true
        }catch (error: Exception){
            Log.e("Modigm_Error","insetUserData() error : $error")
            false
        }
    }

    // ----------------- 로그인 데이터 처리 -----------------

    // Firebase Functions를 통해 Custom Token 획득
    suspend fun getKakaoCustomToken(accessToken: String): String {
        // Firebase Function 호출에 전달할 데이터 생성
        val data = hashMapOf("token" to accessToken)
        return try {
            // Firebase Function 호출 및 결과 대기
            val result = functions.getHttpsCallable("getKakaoCustomAuth").call(data).await()
            // 결과 데이터를 맵으로 캐스팅하여 커스텀 토큰 추출
            val customToken = result.data as Map<*, *>
            // 커스텀 토큰을 문자열로 반환
            customToken["custom_token"] as String
        } catch (e: Exception) {
            // 에러 발생 시 예외를 던져 호출자에게 알림
            throw Exception("Failed to get custom token: ${e.message}", e)
        }
    }

    // Firebase Custom Token으로 로그인
    suspend fun signInWithCustomToken(customToken: String) {
        // Firebase Custom Token을 사용하여 Firebase에 로그인
        auth.signInWithCustomToken(customToken).await()
    }

    // 깃허브 로그인
    suspend fun signInWithGithub(context: Activity) = suspendCancellableCoroutine { cont ->
        // GitHub OAuthProvider 생성
        val provider = OAuthProvider.newBuilder("github.com")
        // GitHub 로그인 시도 및 결과 대기
        auth.startActivityForSignInWithProvider(context, provider.build()).addOnSuccessListener { authResult ->
            // 로그인 성공 시 자격 증명을 코루틴으로 반환
            cont.resume(authResult.credential)
        }.addOnFailureListener { e ->
            // 로그인 실패 시 예외를 코루틴으로 반환
            cont.resumeWithException(e)
        }
    }

    // ----------------- 로그인 데이터 처리 끝-----------------

    companion object{
        // 사용자 번호 시퀀스값을 가져온다.
        suspend fun getUserSequence():Int{

            var userSequence = -1

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("Sequence")
                // 사용자 번호 시퀀스 값을 가지고 있는 문서에 접근할 수 있는 객체를 가져온다.
                val documentReference = collectionReference.document("UserSequence")
                // 문서내에 있는 데이터를 가져올 수 있는 객체를 가져온다.
                val documentSnapShot = documentReference.get().await()
                userSequence = documentSnapShot.getLong("value")?.toInt()!!
            }
            job1.join()

            return userSequence
        }

        // 사용자 시퀀스 값을 업데이트 한다.
        suspend fun updateUserSequence(userSequence:Int){
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("Sequence")
                // 사용자 번호 시퀀스 값을 가지고 있는 문서에 접근할 수 있는 객체를 가져온다.
                val documentReference = collectionReference.document("UserSequence")
                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Long>()
                map["value"] = userSequence.toLong()
                // 저장한다.
                documentReference.set(map)
            }
            job1.join()
        }

        // 사용자 정보를 저장한다.
        suspend fun addUserData(user: UserData){
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("User")
                // 컬럭션에 문서를 추가한다.
                // 문서를 추가할 때 객체나 맵을 지정한다.
                // 추가된 문서 내부의 필드는 객체가 가진 프로퍼티의 이름이나 맵에 있는 데이터의 이름과 동일하게 결정된다.
                collectionReference.add(user)
            }
            job1.join()
        }

        // 입력한 아이디가 저장되어 있는 문서가 있는지 확인한다(중복처리)
        // 사용할 수 있는 아이디라면 true, 존재하는 아이디라면 false를 반환한다.
        suspend fun checkUserIdExist(joinUserId:String) : Boolean{

            var chk = false

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("UserData")
                // UserId 필드가 사용자가 입력한 아이디와 같은 문서들을 가져온다.
                // whereEqualTo : 같은것
                // whereGreaterThan : 큰것
                // whereGreaterThanOrEqualTo : 크거나 같은 것
                // whereLessThan : 작은 것
                // whereLessThanOrEqualTo : 작거나 같은 것
                // whereNotEqualTo : 다른 것
                // 필드의 이름, 값 형태로 넣어준다
                val queryShapshot = collectionReference.whereEqualTo("userId", joinUserId).get().await()
                // 반환되는 리스트에 담긴 문서 객체가 없다면 존재하는 아이디로 취급한다.
                chk = queryShapshot.isEmpty
            }

            job1.join()

            return chk
        }

        // uid를 통해 사용자 정보를 가져오는 메서드
        suspend fun loadUserDataByUid(uid: String): UserData? {
            // 사용자 정보 객체를 담을 변수
            var user: UserData? = null

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // User 컬렉션 접근 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("User")
                // Members 필드가 매개변수로 들어오는 Members 같은 문서들을 가져온다.
                val querySnapshot = collectionReference.whereEqualTo("userNumber", uid).get().await()
                // 만약 가져온 것이 있다면
                if(querySnapshot.isEmpty == false){
                    // 가져온 문서객체들이 들어 있는 리스트에서 첫 번째 객체를 추출한다.
                    // 아이디가 동일한 사용는 없기 때문에 무조건 하나만 나오기 때문이다
                    user = querySnapshot.documents[0].toObject(UserData::class.java)
                    Log.d("test1234", "member")
                }
            }
            job1.join()

            return user
        }

        // 사용자 프로필 사진을 받아오는 메서드
        suspend fun loadUserProfilePic(context: Context, imageFileName: String, imageView: ImageView){
            // 이미지가 등록되어 있지 않으면 불러오지 않는다
            if (imageFileName.isNotEmpty()) {
                val job1 = CoroutineScope(Dispatchers.IO).launch {
                    // 이미지에 접근할 수 있는 객체를 가져온다.
                    val storageRef = Firebase.storage.reference.child("userProfile/$imageFileName")
                    // 이미지의 주소를 가지고 있는 Uri 객체를 받아온다.
                    val imageUri = storageRef.downloadUrl.await()
                    // 이미지 데이터를 받아와 이미지 뷰에 보여준다.
                    CoroutineScope(Dispatchers.Main).launch {
                        Glide.with(context).load(imageUri).into(imageView)
                    }
                }
                job1.join()
                // 이미지는 용량이 매우 클 수 있다. 즉 이미지 데이터를 내려받는데 시간이 오래걸릴 수도 있다.
                // 이에, 이미지 데이터를 받아와 보여주는 코루틴을 작업이 끝날 때 까지 대기 하지 않는다.
                // 그 이유는 데이터를 받아오는데 걸리는 오랜 시간 동안 화면에 아무것도 나타나지 않을 수 있기 때문이다.
                // 따라서 이 메서드는 제일 마지막에 호출해야 한다.(다른 것들을 모두 보여준 후에...)
            }
        }

        // 모든 사용자의 정보를 가져온다.
        suspend fun getAllUsers() : MutableList<UserData>{
            // 사용자 정보를 담을 리스트
            val userList = mutableListOf<UserData>()

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 모든 사용자 정보를 가져온다
                val querySnapshot = Firebase.firestore.collection("User").get().await()
                // 가져온 문서의 수 만큼 반복한다.
                querySnapshot.forEach {
                    // UserData 객체에 담는다.
                    val user = it.toObject(UserData::class.java)
                    // 리스트에 담는다.
                    userList.add(user)
                }
            }
            job1.join()

            return userList
        }

        // 사용자 정보를 수정하는 메서드
        suspend fun updateUserData(user: UserData){
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("User")

                // 컬렉션이 가지고 있는 문서들 중에 수정할 사용자 정보를 가져온다.
                val query = collectionReference.whereEqualTo("userNumber", user.userUid).get().await()

                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Any?>()
                map["userName"] = user.userName
                map["userPhone"] = user.userPhone
                map["userProfilePic"] = user.userProfilePic
                map["userIntro"] = user.userIntro
                map["userInterestList"] = user.userInterestList
                map["userLinkList"] = user.userLinkList
                map["userNumber"] = user.userUid

                // 저장한다.
                // 가져온 문서 중 첫 번째 문서에 접근하여 데이터를 수정한다.
                query.documents[0].reference.update(map)
            }

            job1.join()
        }

        // 사용자의 상태를 변경하는 메서드
        suspend fun invalidateMember(uid: String){
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("Members")
                // 컬렉션이 가지고 있는 문서들 중에 userIdx 필드가 지정된 사용자 번호값하고 같은 Document들을 가져온다.
                val query = collectionReference.whereEqualTo("uid", uid).get().await()

                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Any>()
                map["state"] = false
                // 저장한다.
                // 가져온 문서 중 첫 번째 문서에 접근하여 데이터를 수정한다.
                query.documents[0].reference.update(map)
            }
            job1.join()
        }
    }
}