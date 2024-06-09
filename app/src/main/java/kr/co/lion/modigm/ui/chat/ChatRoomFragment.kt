package kr.co.lion.modigm.ui.chat

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatRoomBinding
import kr.co.lion.modigm.db.chat.ChatMessagesDataSource
import kr.co.lion.modigm.db.chat.ChatRoomDataSource
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomMemberAdapter
import kr.co.lion.modigm.ui.chat.adapter.MessageAdapter
import kr.co.lion.modigm.ui.chat.vm.ChatMessagesViewModel
import kr.co.lion.modigm.ui.chat.vm.ChatRoomViewModel
import kr.co.lion.modigm.util.CameraUtil
import kr.co.lion.modigm.util.hideSoftInput
import java.text.SimpleDateFormat
import java.util.Date

class ChatRoomFragment : Fragment() {

    lateinit var fragmentChatRoomBinding: FragmentChatRoomBinding
    lateinit var mainActivity: MainActivity

    // 앨범 실행을 위한 런처
    lateinit var albumLauncher: ActivityResultLauncher<Intent>

    // 메시지 사진 등록 여부
    var isProductAddPicture = false

    // 리사이클러뷰 아이템의 개수에 맞게 비트맵 배열 초기화
    lateinit var imageBitmap: Bitmap

    // firestore 이미지 경로 배열
    lateinit var imagePath: String

    // 현재 입력 한 채팅 메시지
    var chatMessage = ""

    // 확인할 권한 목록
    val permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_MEDIA_LOCATION
    )

    private val usersDataList = mutableListOf<UserData>()
    private val usersDataHashMap = HashMap<String, UserData>()

    // 어댑터
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatRoomMemberAdapter: ChatRoomMemberAdapter

    // 뷰 모델
    private val chatRoomViewModel: ChatRoomViewModel by viewModels()
    private val chatMessagesViewModel: ChatMessagesViewModel by viewModels()

    // 보낼 메세지를 담고 있을 리스트
    private val messages = mutableListOf<ChatMessagesData>()

    // 현재 로그인 한 사용자 정보
    // FirebaseAuth 인스턴스를 가져옴
    val auth = FirebaseAuth.getInstance()
    val authCurrentUser = auth.currentUser
    // val loginUserId = (authCurrentUser?.uid).toString()
    private val loginUserId = "b9TKzZEJfih7OOnOEoSQE2aNAWu2" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
    private var loginUserName = "홍길동" // 현재 사용자의 Name을 설정 (DB 연동 후 교체)
//    private val loginUserId = "BZPI3tpRAeZ55jrenfuEFuyGc6B2" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
//    private var loginUserName = "테스트" // 현재 사용자의 Name을 설정 (DB 연동 후 교체)

    // 현재 방 번호, 제목, 그룹 채팅방 여부 변수 초기 세팅
    var chatIdx = 0
    var chatTitle = ""
    var chatMemberList = listOf<String>()
    var isGroupChat = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatRoomBinding = FragmentChatRoomBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // Bundle로부터 데이터 가져오기
        arguments?.let {
            chatIdx = it.getInt("chatIdx")
            chatTitle = it.getString("chatTitle").toString()
            chatMemberList = it.getStringArrayList("chatMemberList")!!
            isGroupChat = it.getBoolean("groupChat")
        }

        // 뒤로 가기 콜백 설정
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 이전 프래그먼트를 스택에서 팝
                parentFragmentManager.popBackStack()
            }
        })



        return fragmentChatRoomBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로그인 유저 Name 값 가져오기 (로그인 처리 하고 주석 풀어서 사용)
        // getUserNameByUid()

        // 카메라 InitData
        cameraInitData()

        // 입장시 -> 메시지 읽음 처리
        readMessage()

        // 채팅 방 - (툴바) 세팅
        settingToolbar()

        // 채팅 입력 칸 (입력 여부에 따른) 버튼 세팅
        setupEditTextListener()

        // RecyclerView 초기화
        setupRecyclerView()
        setupMemberRecyclerView()

        // 사이드 네비게이션 클릭 Event
        sideNavigationTextViewClickEvent()

        // 입력칸 + 버튼 클릭 Event
        addIconButtonChatMessage()

        // 실시간 수신
        getAndUpdateLiveChatMessages()
        getAndUpdateLiveChatMembers()

        // 데이터 변경 관찰
        observeData()
    }


    // Pause 상태 - 채팅방 나갈때도 포함되며 Destory에 안쓴 이유는 (onDestory 보다 먼저 실행되서...) 어차피 readMessage 처리 해야해서
    override fun onPause() {
        super.onPause()
        readMessage()
    }

    // 카메라 관련 데이터 초기 세팅
    fun cameraInitData() {
        val context = requireContext()

        // 권한 확인
        requestPermissions(permissionList, 0)

        // 앨범 실행을 위한 런처
        val albumContract = ActivityResultContracts.StartActivityForResult()
        albumLauncher = registerForActivityResult(albumContract) {
            // 사진 선택을 완료한 후 돌아왔다면
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                // 선택한 이미지의 경로 데이터를 관리하는 Uri 객체를 추출한다.
                val uri = it.data?.data
                if (uri != null) {
                    // 안드로이드 Q(10) 이상이라면
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // 이미지를 생성할 수 있는 객체를 생성한다.
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        // Bitmap을 생성한다.
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        // 컨텐츠 프로바이더를 통해 이미지 데이터에 접근한다.
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null) {
                            cursor.moveToNext()

                            // 이미지의 경로를 가져온다.
                            val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(idx)

                            // 이미지를 생성한다
                            BitmapFactory.decodeFile(source)
                        } else {
                            null
                        }
                    }

                    // 회전 각도값을 가져온다.
                    val degree = CameraUtil.getDegree(context, uri)
                    // 회전 이미지를 가져온다
                    val bitmap2 = CameraUtil.rotateBitmap(bitmap!!, degree.toFloat())
                    // 크기를 줄인 이미지를 가져온다.
                    val bitmap3 = CameraUtil.resizeBitmap(bitmap2, 1024)

                    // 이미지 비트맵이 추가되는지 확인하고 추가
                    imageBitmap = bitmap3

                    val currentTimeText = SimpleDateFormat("yyMMdd_HHmm_ss").format(Date())
                    var serverFileName = "${chatIdx}_ImageMessage_${currentTimeText}.jpg"
                    imagePath = serverFileName

                    isProductAddPicture = true

                    // 이미지 추가 후 메시지 전송
                    addChatMessagesData()
                }
            }
        }
    }

    // 툴바 세팅
    fun settingToolbar() {
        fragmentChatRoomBinding.apply {
            toolbarChatRoom.apply {
                setNavigationViewWidth()
                title = "$chatTitle"
                if (isGroupChat == true) subtitle = "현재인원 ${chatMemberList.size}명"
                // 왼쪽 네비게이션 버튼(Back)
                setNavigationOnClickListener {
                    // 뒤로가기
                    parentFragmentManager.popBackStack()
                    // requireActivity().supportFragmentManager.popBackStack()
                    // mainActivity.removeFragment(FragmentName.CHAT_ROOM)
                }
                // 오른쪽 툴바 버튼(Menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.chatroom_toolbar_menu -> {
                            drawerLayoutContent.openDrawer(GravityCompat.END)
                        }
                    }
                    true
                }
            }
        }
    }

    // 네비게이션 뷰의 너비를 동적으로 설정하는 함수
    private fun setNavigationViewWidth() {
        with(fragmentChatRoomBinding){
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val params = navigationViewContent.layoutParams
            params.width = (width * 0.8).toInt() // 화면 폭의 80%로 설정
            navigationViewContent.layoutParams = params
        }
    }

    // 네비게이션 뷰의 멤버 RecyclerView 초기화
    private fun setupMemberRecyclerView() {
        with(fragmentChatRoomBinding.recyclerViewChatRoomMemeberList){
            layoutManager = LinearLayoutManager(context)
            chatRoomMemberAdapter = ChatRoomMemberAdapter(usersDataList)
            adapter = chatRoomMemberAdapter
        }
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {
        // 대화방 목록 RecyclerView 설정
        with(fragmentChatRoomBinding.recyclerView){
            layoutManager = LinearLayoutManager(requireContext())
            messageAdapter = MessageAdapter(loginUserId, messages, usersDataHashMap)
            adapter = messageAdapter
        }
    }

    // 메세지 전송
    fun addChatMessagesData() {
        CoroutineScope(Dispatchers.Main).launch {
            with(fragmentChatRoomBinding){

                val text = editTextMessage.text.toString().trim()
                // 현재 시간
                val now = System.currentTimeMillis()
                val currentTimeText = SimpleDateFormat("HH:mm").format(Date())


                val chatIdx = chatIdx
                // 현재 로그인 한 계정의 아이디
                val chatSenderId = loginUserId
                val chatSenderName = loginUserName
                chatMessage = text
                val chatFullTime = now
                val chatTime = currentTimeText.toString()
                val chatDateSeparator = SimpleDateFormat("yyyy년 MM월 dd일").format(Date())

                if (isProductAddPicture == true){
                    // 이미지의 뷰의 이미지 데이터를 파일로 저장한다.
                    CameraUtil.saveImageViewIndividualItemData(mainActivity, imageBitmap, "uploadTemp.jpg")
                    Log.v("chatLog 이거", "$imageBitmap")
                    // 서버로 업로드한다.
                    ChatMessagesDataSource.uploadMessageImage(mainActivity, "uploadTemp.jpg", imagePath)
                    Log.v("chatLog 이거", "$imagePath")
                    chatMessage = imagePath

                    val message = ChatMessagesData(
                        chatIdx,
                        chatSenderId,
                        chatSenderName,
                        chatMessage,
                        chatFullTime,
                        chatTime,
                        chatDateSeparator,
                    )

                    // 메세지 전송 후 저장
                    chatMessagesViewModel.insertChatMessagesData(message, chatIdx, loginUserName, now)
                    CoroutineScope(Dispatchers.Main).launch {
                        // 메세지 전송 후 해당 채팅 방 마지막 메세지 및 시간 변경
                        val coroutine1 = chatRoomViewModel.updateChatRoomLastMessageAndTime(chatIdx, "사진", chatFullTime, chatTime)
                        coroutine1.join()
                        // 안읽은 메시지 카운트 증가
                        chatRoomViewModel.increaseUnreadMessageCount(chatIdx, chatSenderId)
                    }

                    messages.add(message)
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    fragmentChatRoomBinding.recyclerView.scrollToPosition(messages.size - 1)
                    editTextMessage.text.clear()

                    // 전송 후 키보드 숨기기
                    activity?.hideSoftInput()
                }
                else {

                    if (text.isNotEmpty()) {
                        val message = ChatMessagesData(
                            chatIdx,
                            chatSenderId,
                            chatSenderName,
                            chatMessage,
                            chatFullTime,
                            chatTime,
                            chatDateSeparator,
                        )

                        // 메세지 전송 후 저장
                        chatMessagesViewModel.insertChatMessagesData(message, chatIdx, loginUserName, now)
                        CoroutineScope(Dispatchers.Main).launch {
                            // 메세지 전송 후 해당 채팅 방 마지막 메세지 및 시간 변경
                            val coroutine1 = chatRoomViewModel.updateChatRoomLastMessageAndTime(chatIdx, chatMessage, chatFullTime, chatTime)
                            coroutine1.join()
                            // 안읽은 메시지 카운트 증가
                            chatRoomViewModel.increaseUnreadMessageCount(chatIdx, chatSenderId)
                        }

                        messages.add(message)
                        messageAdapter.notifyItemInserted(messages.size - 1)
                        fragmentChatRoomBinding.recyclerView.scrollToPosition(messages.size - 1)
                        editTextMessage.text.clear()

                        // 전송 후 키보드 숨기기
                        activity?.hideSoftInput()
                    }

                }
            }
        }
    }

    // 해당 채팅 방의 메시지 추가 및 가져오기 - (DB 연동하면 나중에 삭제해야함)
    private fun getAndUpdateMessages() {
        /*
        CoroutineScope(Dispatchers.Main).launch {
            val messagesList = ChatMessagesDataSource.getChatMessages(chatIdx)

            messages.clear()
            messages.addAll(messagesList)

            // 어댑터에 데이터 변경을 알림
            messageAdapter.notifyDataSetChanged()
        }
        */

//        // Firestore 실시간 업데이트 리스너 등록
//        ChatMessagesDataSource.getChatMessagesListener(chatIdx) { updatedMessages ->
//            // 업데이트된 메시지로 어댑터의 메시지 리스트 업데이트
//            messageAdapter.updateMessages(updatedMessages)
//            // RecyclerView 최하단 표시
//            scrollToBottom()
//        }
    }

    // 데이터 변경 관찰
    private fun observeData() {
        // 데이터 변경 관찰
        chatMessagesViewModel.chatMessages.observe(viewLifecycleOwner) { updatedMessages ->
            messages.clear()
            messages.addAll(updatedMessages)
            if (::messageAdapter.isInitialized) {
                messageAdapter.notifyDataSetChanged()
            }
            // messageAdapter.notifyDataSetChanged()
            scrollToBottom()
            Log.d("chatLog1", "Room - observeData() 메시지 데이터 변경")
        }

        chatRoomViewModel.userDataList.observe(viewLifecycleOwner) { userDataList ->
            usersDataList.clear()
            usersDataList.addAll(userDataList)
            for (userData in usersDataList) {
                // 각 사용자의 UID를 키로 사용하여 사용자 데이터를 HashMap에 저장
                usersDataHashMap[userData.userUid] = userData
            }

            chatRoomMemberAdapter.notifyDataSetChanged()

            if (::messageAdapter.isInitialized) {
                messageAdapter.notifyDataSetChanged()
            }
            // messageAdapter.notifyDataSetChanged()
            Log.d("chatLog1", "Room - observeData() 채팅 방 멤버 데이터 변경")
        }
    }

    // 채팅 방 메시지 데이터 실시간 수신
    private fun getAndUpdateLiveChatMessages() {
        chatMessagesViewModel.getChatMessagesListener(chatIdx)
    }

    // 채팅 방 메시지 데이터 실시간 수신
    private fun getAndUpdateLiveChatMembers() {
        chatRoomViewModel.getUsersDataList(chatMemberList)
    }

    // RecyclerView (대화 맨 마지막 기준)으로 설정
    private fun scrollToBottom() {
        // 만약 메시지가 하나도 없다면 스크롤 할 필요가 없음 -> 함수 바로 종료
        if (messages.isEmpty()) return

        // 메시지가 하나 이상 있다면 마지막 아이템의 인덱스를 구한다
        val lastIndex = messages.size - 1

        // RecyclerView를 마지막 아이템으로 스크롤합니다.
        fragmentChatRoomBinding.recyclerView.scrollToPosition(lastIndex)
    }

    // 채팅 입력 칸 - 변경 관련 리스너
    fun setupEditTextListener() {
        with(fragmentChatRoomBinding){
            editTextMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 텍스트 변경 전 호출
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트가 변경될 때 호출
                    updateEditTextInText()
                }

                override fun afterTextChanged(s: Editable?) {
                    // 텍스트 변경 후 호출
                }
            })
            // 전송 버튼 클릭 시 메시지 전송
            imageButtonChatRoomSend.setOnClickListener {
                addChatMessagesData()
            }
        }
    }

    // 채팅 입력 칸 - 입력 상태 여부에 따라 설정
    fun updateEditTextInText() {
        with(fragmentChatRoomBinding){
            if (editTextMessage.text.toString().isEmpty()){
                imageButtonChatRoomSend.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#999999"))
            } else {
                imageButtonChatRoomSend.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A51C5"))
            }
        }
    }

    // 사이드 네비게이션 클릭 Event
    fun sideNavigationTextViewClickEvent() {
        with(fragmentChatRoomBinding){
            // 공지 클릭 시
            textViewSpeaker.setOnClickListener {
                // 눌렸을 때의 효과
                it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textViewClickGray))

                // 잠시 후 기본 상태로 돌아가기
                it.postDelayed({
                    it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }, 30)
            }

            // 대화방 나가기 클릭 시
            textViewLeaveChat.setOnClickListener {
                // 눌렸을 때의 효과
                it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textViewClickGray))

                // 잠시 후 기본 상태로 돌아가기
                it.postDelayed({
                    it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }, 30)

                // 해당 채팅방 멤버 리스트 제거 및 나가기
                outChatRoom()
            }
        }
    }

    // 대화방 나가기
    fun outChatRoom() {
        CoroutineScope(Dispatchers.Main).launch {
            val coroutine1 = chatRoomViewModel.removeUserFromChatMemberList(chatIdx, loginUserId)
            coroutine1.join()
            parentFragmentManager.popBackStack()
        }
    }

    // 메세지 읽음 처리
    fun readMessage() {
        Log.d("chatLog1", "ReadMessage 실행")
        CoroutineScope(Dispatchers.Main).launch {
            val coroutine1 = chatRoomViewModel.chatRoomMessageAsRead(chatIdx, loginUserId)
            coroutine1.join()
            ChatRoomDataSource.chatRoomMessageAsRead(chatIdx, loginUserId)
        }
    }

    // 카메라 앨범 관련 설정 해야함
    // + 버튼
    fun addIconButtonChatMessage(){
        with(fragmentChatRoomBinding.imageButtonChatRoomAdd){
            setOnClickListener {
                Log.d("chatLog1", "addIconButton 클릭")
                // 앨범 띄워주기
                startAlbum()
            }
        }
    }

    // 앨범 사용 시작
    fun startAlbum(){
        // 앨범에서 사진을 선택할 수 있도록 셋팅된 인텐트를 생성한다.
        val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // 실행할 액티비티의 타입을 설정(이미지를 선택할 수 있는 것이 뜨게 한다)
        albumIntent.setType("image/*")
        // 선택할 수 있는 파들의 MimeType을 설정한다.
        // 여기서 선택한 종류의 파일만 선택이 가능하다. 모든 이미지로 설정한다.
        val mimeType = arrayOf("image/*")
        albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // 액티비티를 실행한다.
        albumLauncher.launch(albumIntent)
    }

    // 로그인 유저 Name 값 가져오기
    fun getUserNameByUid() {
        CoroutineScope(Dispatchers.Main).launch {
            loginUserName = ChatRoomDataSource.getUserNameByUid(loginUserId)!!
        }
    }
}