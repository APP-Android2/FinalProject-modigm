package kr.co.lion.modigm.db.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.ChatMessagesData
import java.io.File

class ChatMessagesDataSource {

    private val db = Firebase.firestore
    // 공통으로 쓰이는 collectionReference - ChatMessagesData 로 설정
    private val collectionReference = db.collection("ChatMessagesData")

    // Firestore 실시간 업데이트 리스너
    fun getChatMessagesListener(chatIdx: Int, onUpdate: (List<ChatMessagesData>) -> Unit) {
        collectionReference
            .whereEqualTo("chatIdx", chatIdx)
            .orderBy("chatFullTime", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // 에러 처리
                    return@addSnapshotListener
                }

                val chatMessages = mutableListOf<ChatMessagesData>()
                for (document in value!!) {
                    val chatMessage = document.toObject(ChatMessagesData::class.java)
                    chatMessage?.let{
                        chatMessages.add(it)
                    }
                }
                // 업데이트된 채팅 메시지를 콜백을 통해 전달
                onUpdate(chatMessages)
            }
    }

    // 메세지 전송 db 추가
    suspend fun insertChatMessagesData(chatMessagesData: ChatMessagesData, chatIdx: Int, chatSenderName:String, chatFullTime:Long){
        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            val documentId = "${chatIdx}_${chatSenderName}_${chatFullTime}"
            collectionReference.document(documentId).set(chatMessagesData)
        }
        coroutine1.join()
    }

    companion object {

        // 공통으로 쓰이는 collectionReference - ChatMessagesData 로 설정
        private val collectionReference = Firebase.firestore.collection("ChatMessagesData")

        // 이미지 데이터를 firebase storage에 업로드는 메서드
        suspend fun uploadMessageImage(context: Context, fileName:String, uploadFileName:String) {
            // 외부저장소 까지의 경로를 가져온다.
            val filePath = context.getExternalFilesDir(null).toString()
            // 서버로 업로드할 파일의 경로
            val file = File("${filePath}/${fileName}")
            val uri = Uri.fromFile(file)

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val storageRef = Firebase.storage.reference.child("chatMessages/Pic/$uploadFileName")
                    storageRef.putFile(uri).await()  // await을 사용하여 코루틴이 완료될 때까지 기다림
                    Log.d("chatLog3", "이미지 메시지 업로드 성공: $uploadFileName")
                } catch (e: Exception) {
                    Log.e("chatLog3", "Error - 이미지 업로드 실패: ${e.message}")
                }
            }

//            val job1 = CoroutineScope(Dispatchers.IO).launch {
//                // Storage에 접근할 수 있는 객체를 가져온다.(폴더의 이름과 파일이름을 저장해준다.
//                val storageRef = Firebase.storage.reference.child("chatMessages/Pic/$uploadFileName")
//                // 업로드한다.
//                storageRef.putFile(uri)
//            }

            job1.join()
        }

        // 사용자 프로필 사진을 받아오는 메서드
        suspend fun loadChatImageMessage(context: Context, imageFileName: String, imageView: ImageView){
            // 이미지가 등록되어 있지 않으면 불러오지 않는다
            if (imageFileName.isNotEmpty()) {
                val job1 = CoroutineScope(Dispatchers.IO).launch {
                    // 이미지에 접근할 수 있는 객체를 가져온다.
                    val storageRef = Firebase.storage.reference.child("chatMessages/Pic/$imageFileName")
                    // 이미지의 주소를 가지고 있는 Uri 객체를 받아온다.
                    val imageUri = storageRef.downloadUrl.await()
                    // 이미지 데이터를 받아와 이미지 뷰에 보여준다.
                    CoroutineScope(Dispatchers.Main).launch {
                        Glide.with(context).load(imageUri).into(imageView)
                    }
                }
                job1.join()
            }
        }

        // 현재 채팅방 메세지 데이터 전체 가져오기
        suspend fun getChatMessages(chatIdx: Int): MutableList<ChatMessagesData> {
            var chatMessages = mutableListOf<ChatMessagesData>()

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {

                // chatIdx로 해당 채팅 방 전체 메세지 내용을 가져오며 작성했었던 시간을 오름차순으로 보여준다.
                val querySnapshot = collectionReference
                    .whereEqualTo("chatIdx", chatIdx)
                    .orderBy("chatFullTime", Query.Direction.ASCENDING)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val chatMessage = document.toObject(ChatMessagesData::class.java)
                    chatMessage?.let {
                        chatMessages.add(it)
                    }
                }
            }
            coroutine1.join()

            return chatMessages
        }
    } // companion object
}