package kr.co.lion.modigm.db.chat

import android.content.Context
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
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.model.UserData

class ChatRoomDataSource {

    private val db = Firebase.firestore
    // 공통으로 쓰이는 collectionReferenceSequence - Sequence 로 설정
    private val collectionReferenceSequence = db.collection("Sequence")
    // 공통으로 쓰이는 collectionReference - ChatRoomData 로 설정
    private val collectionReference = db.collection("ChatRoomData")

    // 채팅 방 시퀀스 번호를 Get 후 반환함
    suspend fun getChatRoomSequence():Int{

        var chatRoomSequence = -1

        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            val documentReference = collectionReferenceSequence.document("ChatRoomSequence")
            val documentSnapShot = documentReference.get().await()
            chatRoomSequence = documentSnapShot.getLong("value")?.toInt()!!
        }
        coroutine1.join()

        return chatRoomSequence
    }

    // 채팅 방 시퀀스 값을 변경함 (Update)
    suspend fun updateChatRoomSequence(userSequence:Int) {
        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            val documentReference = collectionReferenceSequence.document("ChatRoomSequence")
            // 저장할 데이터를 담을 HashMap을 만들어준다.
            val map = mutableMapOf<String, Long>()
            map["value"] = userSequence.toLong()
            // 저장
            documentReference.set(map)
        }
        coroutine1.join()
    }

    // 채팅 방을 생성함 (Create)
    suspend fun insertChatRoomData(chatRoomData: ChatRoomData){
        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            collectionReference.add(chatRoomData)
        }
        coroutine1.join()
    }

    // 리스너를 추가하고 데이터 변경 시에 실행하는 메서드로 변경
    // 채팅방 데이터 가져옴 (Read) groupChat = true -> 그룹 / false -> 1:1
    fun updateChatRoomsListener(userId: String, groupChat: Boolean , onUpdate: (List<ChatRoomData>) -> Unit) {
        collectionReference
            .whereArrayContains("chatMemberList", userId)
            .whereEqualTo("groupChat", groupChat)
            .orderBy("lastChatFullTime", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // 에러 처리
                    return@addSnapshotListener
                }
                val chatRooms = mutableListOf<ChatRoomData>()
                for (document in value!!) {
                    val chatRoom = document.toObject(ChatRoomData::class.java)
                    chatRoom?.let {
                        chatRooms.add(it)
                    }
                }
                // Update 된 채팅 방을 콜백을 통해 전달
                onUpdate(chatRooms)
            }
    }

    // 모든 방을 가져온다(검색에서 사용)
    fun updateChatAllRoomsListener(userId: String, onUpdate: (List<ChatRoomData>) -> Unit) {
        collectionReference
            .whereArrayContains("chatMemberList", userId)
            .orderBy("lastChatFullTime", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // 에러 처리
                    return@addSnapshotListener
                }
                val chatRooms = mutableListOf<ChatRoomData>()
                for (document in value!!) {
                    val chatRoom = document.toObject(ChatRoomData::class.java)
                    chatRoom?.let {
                        chatRooms.add(it)
                    }
                }
                // Update 된 채팅 방을 콜백을 통해 전달
                onUpdate(chatRooms)
            }
    }

    // 해당 채팅방 데이터 [마지막 메세지, 마지막 메세지 시간] 변경함 (Update)
    suspend fun updateChatRoomLastMessageAndTime(chatIdx: Int, chatMessage: String, chatFullTime: Long, chatTime: String): MutableList<ChatRoomData> {
        var chatRooms = mutableListOf<ChatRoomData>()

        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            // 내 아이디의 chatIdx 와 groupChat이 true인 경우 필터링
            // chatIdx는 UserData가 있다면 그 Data에서 아이디 별 소속해있는 채팅방 int형 list를 가져와야함.
            val querySnapshot = collectionReference
                .whereEqualTo("chatIdx", chatIdx)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val documentReference = document.reference
                documentReference.update(
                    mapOf(
                        "lastChatMessage" to chatMessage,
                        "lastChatFullTime" to chatFullTime,
                        "lastChatTime" to chatTime
                    )
                ).await()
            }
        }
        coroutine1.join()

        return chatRooms
    }

    // 채팅방에 사용자 추가 / chatMemberList 배열에 ID 추가 (Update)
    suspend fun addUserToChatMemberList(chatIdx: Int, userId: String) {
        val querySnapshot = collectionReference
            .whereEqualTo("chatIdx", chatIdx)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            val documentReference = document.reference
            documentReference.update(
                "chatMemberList", com.google.firebase.firestore.FieldValue.arrayUnion(userId),
                "participantCount", com.google.firebase.firestore.FieldValue.increment(1)
            ).await()
        }
    }

    // 채팅방 나가기 / chatMemberList 배열에서 내 ID를 제거 (Update)
    suspend fun removeUserFromChatMemberList(chatIdx: Int, userId: String) {
        val querySnapshot = collectionReference
            .whereEqualTo("chatIdx", chatIdx)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            val documentReference = document.reference
            documentReference.update(
                "chatMemberList", com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                "participantCount", com.google.firebase.firestore.FieldValue.increment(-1)
            ).await()
        }
    }

    // 로그인 한 사용자 해당 채팅 방 메세지 읽음 처리 (Update)
    suspend fun chatRoomMessageAsRead(chatIdx: Int, loginUserId: String) {
        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            val chatRoomRef = collectionReference.whereEqualTo("chatIdx", chatIdx)
            val querySnapshot = chatRoomRef.get().await()
            querySnapshot.forEach { document ->
                val chatRoom = document.toObject(ChatRoomData::class.java)
                chatRoom?.let {
                    it.unreadMessageCount[loginUserId] = 0
                    document.reference.set(it).await()
                }
            }
        }
        coroutine1.join()
    }

    // 메세지 전송시 해당 채팅 방 사용자에 안읽은 메세지 카운트 증가
    suspend fun increaseUnreadMessageCount(chatIdx: Int, senderId: String) {
        val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
            val chatRoomRef = collectionReference.whereEqualTo("chatIdx", chatIdx)
            val querySnapshot = chatRoomRef.get().await()
            querySnapshot.forEach { document ->
                val chatRoom = document.toObject(ChatRoomData::class.java)
                chatRoom?.let {
                    for (participant in it.chatMemberList) {
                        // 현재 입장 여부 확인
//                            if (participant != senderId && it.chatMemberState[participant] == false) {
//                                it.unreadMessageCount[participant] = it.unreadMessageCount.getOrDefault(participant, 0) + 1
//                            }
                        // 로그인 사용자만 빼고 Count 증가
                        if (participant != senderId) {
                            it.unreadMessageCount[participant] = it.unreadMessageCount.getOrDefault(participant, 0) + 1
                        }
                    }
                    document.reference.set(it).await()
                }
            }
        }
        coroutine1.join()
    }

    // 채팅 방 안 멤버 데이터 가져옴
    fun getUsersDataListListener(chatMemberList: List<String>, onUpdate: (List<UserData>) -> Unit) {
        val usersData = mutableListOf<UserData>()

        for (uid in chatMemberList) {
            val userDocRef = db.collection("User").whereEqualTo("userUid", uid)
            userDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val userDataList = mutableListOf<UserData>()
                for (doc in snapshot?.documents ?: emptyList()) {
                    val userData = doc.toObject(UserData::class.java)
                    userData?.let {
                        userDataList.add(it)
                    }
                }

                synchronized(usersData) {
                    usersData.addAll(userDataList)
                    onUpdate(usersData)
                }
            }
        }
    }

    companion object {

        // 공통으로 쓰이는 collectionReferenceSequence - Sequence 로 설정
        val collectionReferenceSequence = Firebase.firestore.collection("Sequence")
        // 공통으로 쓰이는 collectionReference - ChatRoomData 로 설정
        val collectionReference = Firebase.firestore.collection("ChatRoomData")

        // 그룹 채팅 방 시퀀스 번호를 Get 후 반환함
        suspend fun getChatRoomGroupSequence():Int{

            var chatRoomSequence = -1

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val documentReference = collectionReferenceSequence.document("ChatRoomGroupSequence")
                val documentSnapShot = documentReference.get().await()
                chatRoomSequence = documentSnapShot.getLong("value")?.toInt()!!
            }
            coroutine1.join()

            return chatRoomSequence
        }

        // 1:1 채팅 방 시퀀스 번호를 Get 후 반환함
        suspend fun getChatRoomSequence():Int{

            var chatRoomSequence = -1

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val documentReference = collectionReferenceSequence.document("ChatRoomSequence")
                val documentSnapShot = documentReference.get().await()
                chatRoomSequence = documentSnapShot.getLong("value")?.toInt()!!
            }
            coroutine1.join()

            return chatRoomSequence
        }

        // 그룹 채팅 방 시퀀스 값을 변경함 (Update)
        suspend fun updateChatRoomGroupSequence(userSequence:Int) {
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val documentReference = collectionReferenceSequence.document("ChatRoomGroupSequence")
                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Long>()
                map["value"] = userSequence.toLong()
                // 저장
                documentReference.set(map)
            }
            coroutine1.join()
        }

        // 1:1 채팅 방 시퀀스 값을 변경함 (Update)
        suspend fun updateChatRoomSequence(userSequence:Int) {
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val documentReference = collectionReferenceSequence.document("ChatRoomSequence")
                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Long>()
                map["value"] = userSequence.toLong()
                // 저장
                documentReference.set(map)
            }
            coroutine1.join()
        }

        // 채팅 방을 생성함 (Create)
        suspend fun insertChatRoomData(chatRoomData: ChatRoomData){
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val documentId = "${chatRoomData.chatIdx}_${chatRoomData.groupChat}_${chatRoomData.chatTitle}"
                collectionReference.document(documentId).set(chatRoomData)
            }
            coroutine1.join()
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

        // 사용자 프로필 사진을 받아오는 메서드
        suspend fun loadChatRoomImage(context: Context, imageFileName: String, imageView: ImageView){
            // 이미지가 등록되어 있지 않으면 불러오지 않는다
            if (imageFileName.isNotEmpty()) {
                val job1 = CoroutineScope(Dispatchers.IO).launch {
                    // 이미지에 접근할 수 있는 객체를 가져온다.
                    val storageRef = Firebase.storage.reference.child("studyPic/$imageFileName")
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

        // userUid를 통해 해당 user의 Name을 가져온다
        suspend fun getUserNameByUid(userUid: String): String? {
            val querySnapshot = Firebase.firestore.collection("User")
                .whereEqualTo("userUid", userUid)
                .get()
                .await()

            // 문서가 존재하는지 확인하고 userName을 반환
            for (document in querySnapshot.documents) {
                return document.getString("userName")
            }
            return null
        }

        // userUid를 통해 해당 user의 userProfilePic을 가져온다
        suspend fun getUserProfilePicByUid(userUid: String): String? {
            val querySnapshot = Firebase.firestore.collection("User")
                .whereEqualTo("userUid", userUid)
                .get()
                .await()

            // 문서가 존재하는지 확인하고 userName을 반환
            for (document in querySnapshot.documents) {
                return document.getString("userProfilePic")
            }
            return null
        }

        // 로그인 한 사용자 해당 채팅 방 메세지 읽음 처리 (Update)
        suspend fun chatRoomMessageAsRead(chatIdx: Int, loginUserId: String) {
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val chatRoomRef = collectionReference.whereEqualTo("chatIdx", chatIdx)
                val querySnapshot = chatRoomRef.get().await()
                querySnapshot.forEach { document ->
                    val chatRoom = document.toObject(ChatRoomData::class.java)
                    chatRoom?.let {
                        it.unreadMessageCount[loginUserId] = 0
                        document.reference.set(it).await()
                    }
                }
            }
            coroutine1.join()
        }
    }
}