package kr.co.lion.modigm.db.remote

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class StudyDataSource {
    private val studyCollection = Firebase.firestore.collection("Study")

}