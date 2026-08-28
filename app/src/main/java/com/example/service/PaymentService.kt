package com.example.service

import com.example.model.*
import java.util.UUID

sealed class PaymentResult {
    data class Success(val transactionId: String, val message: String) : PaymentResult()
    data class PendingVerification(val paymentRecord: PaymentRecord, val message: String) : PaymentResult()
    data class Failed(val error: String) : PaymentResult()
}

interface PaymentProcessor {
    suspend fun processPayment(orderId: String, amount: Double, reference: String = ""): PaymentResult
}

class CashPaymentService : PaymentProcessor {
    override suspend fun processPayment(orderId: String, amount: Double, reference: String): PaymentResult {
        val txId = "CASH-" + UUID.randomUUID().toString().take(8).uppercase()
        return PaymentResult.Success(txId, "Cash payment of NPR $amount accepted at POS counter.")
    }
}

class EsewaPaymentService : PaymentProcessor {
    override suspend fun processPayment(orderId: String, amount: Double, reference: String): PaymentResult {
        // eSewa digital wallet gateway protocol simulation for Nepal
        val txId = "ESEWA-" + (reference.ifBlank { UUID.randomUUID().toString().take(8).uppercase() })
        return PaymentResult.Success(txId, "eSewa Payment of NPR $amount verified successfully.")
    }
}

class KhaltiPaymentService : PaymentProcessor {
    override suspend fun processPayment(orderId: String, amount: Double, reference: String): PaymentResult {
        // Khalti merchant verification protocol
        val txId = "KHALTI-" + (reference.ifBlank { UUID.randomUUID().toString().take(8).uppercase() })
        return PaymentResult.Success(txId, "Khalti Wallet payment of NPR $amount verified.")
    }
}

class BankPaymentService : PaymentProcessor {
    override suspend fun processPayment(orderId: String, amount: Double, reference: String): PaymentResult {
        val paymentRecord = PaymentRecord(
            paymentId = UUID.randomUUID().toString(),
            orderId = orderId,
            method = PaymentMethod.BANK,
            amount = amount,
            transactionId = "BANK-REF-${reference.ifBlank { System.currentTimeMillis().toString().takeLast(6) }}",
            reference = reference,
            bankVerificationStatus = BankVerificationStatus.PENDING,
            status = PaymentStatus.PENDING
        )
        return PaymentResult.PendingVerification(
            paymentRecord,
            "Bank payment reference '$reference' submitted. Awaiting manager approval."
        )
    }
}

class CODPaymentService : PaymentProcessor {
    override suspend fun processPayment(orderId: String, amount: Double, reference: String): PaymentResult {
        val txId = "COD-" + UUID.randomUUID().toString().take(8).uppercase()
        return PaymentResult.Success(txId, "Cash on Delivery recorded. Payable upon delivery: NPR $amount.")
    }
}

object PaymentManager {
    val cashService = CashPaymentService()
    val esewaService = EsewaPaymentService()
    val khaltiService = KhaltiPaymentService()
    val bankService = BankPaymentService()
    val codService = CODPaymentService()

    suspend fun executePayment(
        method: PaymentMethod,
        orderId: String,
        amount: Double,
        reference: String = ""
    ): PaymentResult {
        return when (method) {
            PaymentMethod.CASH -> cashService.processPayment(orderId, amount, reference)
            PaymentMethod.ESEWA -> esewaService.processPayment(orderId, amount, reference)
            PaymentMethod.KHALTI -> khaltiService.processPayment(orderId, amount, reference)
            PaymentMethod.BANK -> bankService.processPayment(orderId, amount, reference)
            PaymentMethod.COD -> codService.processPayment(orderId, amount, reference)
        }
    }
}
