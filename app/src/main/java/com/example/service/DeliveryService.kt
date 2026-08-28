package com.example.service

import com.example.model.DeliveryProviderConfig
import com.example.model.DeliveryStatus
import com.example.model.Order

interface DeliveryServiceProvider {
    val providerConfig: DeliveryProviderConfig
    suspend fun createDeliveryTask(order: Order): Boolean
    suspend fun trackDelivery(trackingId: String): DeliveryStatus
}

class Provider1Service(override val providerConfig: DeliveryProviderConfig) : DeliveryServiceProvider {
    override suspend fun createDeliveryTask(order: Order): Boolean {
        // Integrate with Provider 1 (e.g. Foodmandu API)
        // Note: Real credentials should be configured in settings
        return true
    }

    override suspend fun trackDelivery(trackingId: String): DeliveryStatus {
        return DeliveryStatus.ON_THE_WAY
    }
}

class Provider2Service(override val providerConfig: DeliveryProviderConfig) : DeliveryServiceProvider {
    override suspend fun createDeliveryTask(order: Order): Boolean {
        // Integrate with Provider 2 (e.g. Bhoj Deals API)
        return true
    }

    override suspend fun trackDelivery(trackingId: String): DeliveryStatus {
        return DeliveryStatus.ASSIGNED
    }
}

class Provider3Service(override val providerConfig: DeliveryProviderConfig) : DeliveryServiceProvider {
    override suspend fun createDeliveryTask(order: Order): Boolean {
        // Integrate with Provider 3 (e.g. Pathao / TJW Direct In-House Riders)
        return true
    }

    override suspend fun trackDelivery(trackingId: String): DeliveryStatus {
        return DeliveryStatus.PICKED_UP
    }
}

object DeliveryManager {
    fun getProvider(config: DeliveryProviderConfig): DeliveryServiceProvider {
        return when (config.providerId) {
            "delivery_provider_1" -> Provider1Service(config)
            "delivery_provider_2" -> Provider2Service(config)
            else -> Provider3Service(config)
        }
    }
}
