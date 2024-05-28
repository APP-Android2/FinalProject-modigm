package kr.co.lion.modigm.ui.chat.dao

import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.ChatRoomData

class ChatRoomDao {
    companion object{

        // 채팅 방 시퀀스 번호를 Get 후 Int 형으로 반환
        suspend fun getChatRoomSequence():Int{

            var chatRoomSequence = -1

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("Sequence")
                val documentReference = collectionReference.document("ChatRoomSequence")
                val documentSnapShot = documentReference.get().await()
                chatRoomSequence = documentSnapShot.getLong("value")?.toInt()!!
            }
            coroutine1.join()

            return chatRoomSequence
        }

        // 채팅 방 시퀀스 값을 Update 한다.
        suspend fun updateChatRoomSequence(userSequence:Int){
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("Sequence")
                val documentReference = collectionReference.document("ChatRoomSequence")
                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Long>()
                map["value"] = userSequence.toLong()
                // 저장
                documentReference.set(map)
            }
            coroutine1.join()
        }

        // 채팅 방을 Create 한다.
        suspend fun insertChatRoomData(chatRoomData: ChatRoomData){
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("ChatRoomData")
                collectionReference.add(chatRoomData)
            }
            coroutine1.join()
        }

        // 단체 채팅방 데이터 가져오기
        suspend fun getGroupChatRooms(userId: String): MutableList<ChatRoomData> {
            var chatRooms = mutableListOf<ChatRoomData>()

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("ChatRoomData")

                // 내 아이디의 chatIdx 와 groupChat이 true인 경우 필터링
                // chatIdx는 UserData가 있다면 그 Data에서 아이디 별 소속해있는 채팅방 int형 list를 가져와야함.
                val querySnapshot = collectionReference
                    .whereArrayContains("chatMemberList", userId)
                    .whereEqualTo("groupChat", true)
                    .orderBy("lastChatFullTime", Query.Direction.DESCENDING)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val chatRoom = document.toObject(ChatRoomData::class.java)
                    chatRoom?.let {
                        chatRooms.add(it)
                    }
                }
            }
            coroutine1.join()

            return chatRooms
        }

        // 1:1 채팅방 데이터 가져오기
        suspend fun getOneToOneChatRooms(userId: String): MutableList<ChatRoomData> {
            var chatRooms = mutableListOf<ChatRoomData>()

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("ChatRoomData")

                // 내 아이디의 chatIdx 와 groupChat이 true인 경우 필터링
                // chatIdx는 UserData가 있다면 그 Data에서 아이디 별 소속해있는 채팅방 int형 list를 가져와야함.
                val querySnapshot = collectionReference
                    .whereArrayContains("chatMemberList", userId)
                    .whereEqualTo("groupChat", false)
                    .orderBy("lastChatFullTime", Query.Direction.DESCENDING)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val chatRoom = document.toObject(ChatRoomData::class.java)
                    chatRoom?.let {
                        chatRooms.add(it)
                    }
                }
            }
            coroutine1.join()

            return chatRooms
        }

        // 해당 채팅방 마지막 메세지, 마지막 메세지 시간 저장
        suspend fun updateChatRoomLastMessageAndTime(chatIdx: Int, chatMessage: String, chatFullTime: Long, chatTime: String): MutableList<ChatRoomData> {
            var chatRooms = mutableListOf<ChatRoomData>()

            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("ChatRoomData")

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

        // chatMemberList 배열에서 내 ID를 제거
        suspend fun removeUserFromChatMemberList(chatIdx: Int, userId: String) {
            val coroutine1 = CoroutineScope(Dispatchers.IO).launch {
                val collectionReference = Firebase.firestore.collection("ChatRoomData")

                val querySnapshot = collectionReference
                    .whereEqualTo("chatIdx", chatIdx)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val documentReference = document.reference
                    documentReference.update("chatMemberList",
                        com.google.firebase.firestore.FieldValue.arrayRemove(userId)
                    ).await()
                }
            }
            coroutine1.join()
        }
    }
}