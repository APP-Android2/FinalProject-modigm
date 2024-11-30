package kr.co.lion.modigm.db

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreKeyProvider {
    private val firestore = FirebaseFirestore.getInstance()

    // Firestore에서 AWS 키를 가져오는 함수
    suspend fun getAwsKeys(): Triple<String, String, String> {
        val document = firestore.collection("keys").document("aws_keys").get().await()
        val accessKey = document.getString("accessKey") ?: throw IllegalStateException("Access Key not found")
        val secretKey = document.getString("secretKey") ?: throw IllegalStateException("Secret Key not found")
        val bucketName = document.getString("bucketName") ?: throw IllegalStateException("Bucket Name not found")

        return Triple(accessKey, secretKey, bucketName)
    }
}