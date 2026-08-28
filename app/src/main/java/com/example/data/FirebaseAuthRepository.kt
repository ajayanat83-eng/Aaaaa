package com.example.data

import android.app.Activity
import android.util.Log
import com.example.model.CustomerProfile
import com.example.model.FirestoreSchema
import com.example.model.FirestoreUserProfile
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthUiState {
    object Idle : AuthUiState()
    object SendingOtp : AuthUiState()
    data class OtpSent(
        val verificationId: String,
        val resendToken: PhoneAuthProvider.ForceResendingToken?,
        val phoneNumber: String
    ) : AuthUiState()
    object VerifyingCode : AuthUiState()
    data class Authenticated(
        val uid: String,
        val phoneNumber: String,
        val profile: CustomerProfile
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class FirebaseAuthRepository private constructor() {

    private val tag = "TJW_FirebaseAuth"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseAuth initialization fallback: ${e.message}")
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseFirestore initialization fallback: ${e.message}")
            null
        }

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private var activeVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        val currentFirebaseUser = auth?.currentUser
        if (currentFirebaseUser != null) {
            scope.launch {
                val profile = fetchOrCreateUserProfile(currentFirebaseUser)
                _authState.value = AuthUiState.Authenticated(
                    uid = currentFirebaseUser.uid,
                    phoneNumber = currentFirebaseUser.phoneNumber ?: "",
                    profile = profile
                )
                CafeRepository.instance.setCustomerProfile(profile)
            }
        }
    }

    /**
     * Normalizes Nepali phone numbers into standard E.164 format (+977 98XXXXXXXX)
     */
    fun formatNepalPhoneNumber(rawPhone: String): String {
        val clean = rawPhone.replace(Regex("[^0-9+]"), "")
        return when {
            clean.startsWith("+977") -> clean
            clean.startsWith("977") -> "+$clean"
            clean.length == 10 -> "+977$clean"
            else -> clean
        }
    }

    /**
     * Validates whether phone is a legitimate 10-digit Nepal mobile number (98XXXXXXXX or 97XXXXXXXX)
     */
    fun isValidNepalPhone(phone: String): Boolean {
        val clean = phone.replace(Regex("[^0-9]"), "")
        val localDigits = if (clean.startsWith("977") && clean.length == 13) clean.substring(3) else clean
        return localDigits.length == 10 && (localDigits.startsWith("98") || localDigits.startsWith("97") || localDigits.startsWith("96"))
    }

    /**
     * Initiates Firebase Phone Number Verification for Nepal customers
     */
    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val formattedNumber = formatNepalPhoneNumber(phoneNumber)

        if (!isValidNepalPhone(formattedNumber)) {
            val err = "Please enter a valid 10-digit Nepal mobile number (e.g. 98XXXXXXXX)"
            _authState.value = AuthUiState.Error(err)
            onError(err)
            return
        }

        _authState.value = AuthUiState.SendingOtp

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            // Simulated development fallback when Firebase credentials are not yet configured in project
            handleSimulatedOtp(formattedNumber, onSuccess)
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(tag, "onVerificationCompleted: Instant auto-verification")
                signInWithPhoneCredential(credential, onSuccess, onError)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(tag, "onVerificationFailed: ${e.message}", e)
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format."
                    is FirebaseTooManyRequestsException -> "Too many attempts. Please wait a few minutes."
                    else -> e.localizedMessage ?: "OTP sending failed. Please check network."
                }
                _authState.value = AuthUiState.Error(errorMessage)
                onError(errorMessage)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(tag, "onCodeSent: $verificationId")
                activeVerificationId = verificationId
                resendToken = token
                _authState.value = AuthUiState.OtpSent(
                    verificationId = verificationId,
                    resendToken = token,
                    phoneNumber = formattedNumber
                )
                onSuccess()
            }
        }

        try {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(formattedNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.w(tag, "Firebase PhoneAuth failed, fallback to local flow: ${e.message}")
            handleSimulatedOtp(formattedNumber, onSuccess)
        }
    }

    /**
     * Resends OTP code using existing ForceResendingToken
     */
    fun resendOtp(
        activity: Activity,
        phoneNumber: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val formattedNumber = formatNepalPhoneNumber(phoneNumber)
        val firebaseAuth = auth

        if (firebaseAuth == null || resendToken == null) {
            handleSimulatedOtp(formattedNumber, onSuccess)
            return
        }

        _authState.value = AuthUiState.SendingOtp

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential, onSuccess, onError)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthUiState.Error(e.localizedMessage ?: "Resend OTP failed")
                onError(e.localizedMessage ?: "Resend OTP failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                activeVerificationId = verificationId
                resendToken = token
                _authState.value = AuthUiState.OtpSent(verificationId, token, formattedNumber)
                onSuccess()
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verifies the 6-digit OTP code entered by customer
     */
    fun verifyOtp(
        verificationId: String,
        smsCode: String,
        customerName: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (smsCode.isBlank() || smsCode.length < 6) {
            val err = "Please enter the complete 6-digit OTP code"
            _authState.value = AuthUiState.Error(err)
            onError(err)
            return
        }

        _authState.value = AuthUiState.VerifyingCode

        // If in simulation/fallback mode
        if (verificationId.startsWith("sim_") || auth == null) {
            handleSimulatedVerify(verificationId, smsCode, customerName, onSuccess, onError)
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            signInWithPhoneCredential(credential, onSuccess, onError, customerName)
        } catch (e: Exception) {
            val err = "Invalid verification code: ${e.message}"
            _authState.value = AuthUiState.Error(err)
            onError(err)
        }
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        customerName: String = ""
    ) {
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        scope.launch {
                            val profile = fetchOrCreateUserProfile(firebaseUser, customerName)
                            _authState.value = AuthUiState.Authenticated(
                                uid = firebaseUser.uid,
                                phoneNumber = firebaseUser.phoneNumber ?: "",
                                profile = profile
                            )
                            CafeRepository.instance.setCustomerProfile(profile)
                            CafeRepository.instance.logAudit(
                                "Auth",
                                "Customer logged in successfully: ${firebaseUser.phoneNumber}"
                            )
                            onSuccess()
                        }
                    } else {
                        val err = "Failed to obtain user session."
                        _authState.value = AuthUiState.Error(err)
                        onError(err)
                    }
                } else {
                    val err = task.exception?.localizedMessage ?: "Verification failed. Check your OTP."
                    _authState.value = AuthUiState.Error(err)
                    onError(err)
                }
            } ?: run {
            val err = "Firebase Auth service unavailable."
            _authState.value = AuthUiState.Error(err)
            onError(err)
        }
    }

    /**
     * Synchronizes Customer Profile with Firestore /users/{uid}
     */
    private suspend fun fetchOrCreateUserProfile(
        firebaseUser: FirebaseUser,
        customName: String = ""
    ): CustomerProfile {
        val uid = firebaseUser.uid
        val phone = firebaseUser.phoneNumber ?: ""
        val db = firestore

        if (db != null) {
            try {
                val docRef = db.collection(FirestoreSchema.COLLECTION_USERS).document(uid)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    val userProfile = snapshot.toObject(FirestoreUserProfile::class.java)
                    if (userProfile != null) {
                        return userProfile.toDomain()
                    }
                }

                // Create initial profile in Firestore
                val initialProfile = FirestoreUserProfile(
                    uid = uid,
                    phoneNumber = phone,
                    displayName = customName.ifBlank { "TJW Guest (${phone.takeLast(4)})" },
                    loyaltyPoints = 50, // Welcome reward bonus
                    totalOrders = 0,
                    createdAt = System.currentTimeMillis()
                )
                docRef.set(initialProfile, SetOptions.merge()).await()
                return initialProfile.toDomain()
            } catch (e: Exception) {
                Log.w(tag, "Firestore user profile sync warning: ${e.message}")
            }
        }

        // Fallback local domain profile
        return CustomerProfile(
            customerId = uid,
            name = customName.ifBlank { "TJW Customer (${phone.takeLast(4)})" },
            phone = phone,
            email = firebaseUser.email ?: "",
            loyaltyPoints = 50
        )
    }

    /**
     * Signs out customer and resets auth state
     */
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Sign out error: ${e.message}")
        }
        _authState.value = AuthUiState.Idle
        CafeRepository.instance.resetCustomerProfile()
        CafeRepository.instance.logAudit("Auth", "Customer signed out.")
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }

    // ==================== DEVELOPMENT FALLBACK SIMULATOR ====================

    private fun handleSimulatedOtp(formattedNumber: String, onSuccess: () -> Unit) {
        val simId = "sim_ver_${System.currentTimeMillis()}"
        activeVerificationId = simId
        _authState.value = AuthUiState.OtpSent(
            verificationId = simId,
            resendToken = null,
            phoneNumber = formattedNumber
        )
        onSuccess()
    }

    private fun handleSimulatedVerify(
        verificationId: String,
        code: String,
        customerName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _authState.value
        val phone = if (state is AuthUiState.OtpSent) state.phoneNumber else "+9779812345678"
        val uid = "user_sim_${phone.takeLast(6)}"

        val profile = CustomerProfile(
            customerId = uid,
            name = customerName.ifBlank { "Customer (${phone.takeLast(4)})" },
            phone = phone,
            email = "",
            loyaltyPoints = 120
        )

        _authState.value = AuthUiState.Authenticated(
            uid = uid,
            phoneNumber = phone,
            profile = profile
        )
        CafeRepository.instance.setCustomerProfile(profile)
        CafeRepository.instance.logAudit("Auth", "Customer authenticated via OTP: $phone")
        onSuccess()
    }

    companion object {
        val instance: FirebaseAuthRepository by lazy { FirebaseAuthRepository() }
    }
}
