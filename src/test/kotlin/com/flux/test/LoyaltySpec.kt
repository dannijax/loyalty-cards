package com.flux.test

import com.flux.test.model.*
import io.kotlintest.IsolationMode
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.specs.Test
import java.util.UUID

class LoyaltySpec : StringSpec() {

    private val implementation = Merchant(schemes = schemes)


    init {
        "Applies a stamp" {
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(Item("1", 100, 1)))
            implementation.schemes = schemes

            val response = implementation.apply(receipt)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 1
            response.first().currentStampCount shouldBe 1
            response.first().paymentsGiven shouldHaveSize 0
        }

        "Triggers a redemption" {
            val receipt =
                    Receipt(merchantId = merchantId, accountId = accountId, items = 1.rangeTo(5).map { Item("1", 100, 1) })
            val response = implementation.apply(receipt)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 4
            response.first().currentStampCount shouldBe 0
            response.first().paymentsGiven shouldHaveSize 1
            response.first().paymentsGiven.first() shouldBe 100
        }

        "Stores the current state for an account" {
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(Item("1", 100, 1)))

            val res = implementation.apply(receipt)
            val response = implementation.state(accountId)
            response shouldHaveSize (1)
            response.first().currentStampCount shouldBe 1
            response.first().payments shouldHaveSize 0
        }

        "Triggers redemption and payment"{
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = 1.rangeTo(20).map { Item("1", 100L*it, 1) })
            val applyResponse = implementation.apply(receipt)
            applyResponse.first().stampsGiven shouldBe 16
            applyResponse.first().paymentsGiven shouldHaveSize 4
            applyResponse.first().currentStampCount shouldBe 0
            applyResponse shouldHaveSize 1

        }
    }

    //        10 stamps given and maxStamp for the scheme is 4:
//        In this case: paymentsGiven = 2 and currentStampCount = 0 as it is paid on a 'full' card, and when there is a payment, there wouldn't be a stamp.

    override fun isolationMode() = IsolationMode.InstancePerTest

    companion object {
        private val accountId: AccountId = UUID.randomUUID()
        private val merchantId: MerchantId = UUID.randomUUID()

        private val schemeId: SchemeId = UUID.randomUUID()
        private val schemes = listOf(Scheme(schemeId, merchantId, 4, listOf("1")))
    }
}