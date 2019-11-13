package com.flux.test

import com.flux.test.model.*
import java.util.*

class Merchant(val id: UUID? = MerchantId.randomUUID(), override var schemes: List<Scheme>) : ImplementMe {

    private var accountSchemes = mutableListOf<AccountId>()
    private var schemeDetails = mutableMapOf<SchemeId, AccountDetails>()


    override fun apply(receipt: Receipt): List<ApplyResponse> {
        val sortedItems = mutableListOf<Item>()
        val paymentsGiven = mutableListOf<Long>()

        val applyResponses = mutableListOf<ApplyResponse>()

        schemes.forEach { scheme ->
            var stampsGiven = 0
            val applicableItems = canSchemeBeApplied(receipt.items, scheme.skus)
            applicableItems.forEachIndexed { index, item ->
                //avoid applying multiple schemes to the same item
                if (!sortedItems.contains(item)) {
                    stampsGiven += stampsGenerated(item)
                }
            }
            val payments = generatePayment(stampsGiven, scheme.maxStamps)
            paymentsGiven.addAll(payments(applicableItems, payments, scheme.maxStamps))
            val response = ApplyResponse(schemeId = scheme.id, currentStampCount = stampPosition(stampsGiven, scheme.maxStamps), stampsGiven = stampsGiven - payments, paymentsGiven = paymentsGiven)
            applyResponses.add(response)
            accountSchemes.add(receipt.accountId)
            schemeDetails[scheme.id] = AccountDetails(receipt.accountId, scheme.id, paymentsGiven, if (stampsGiven < scheme.maxStamps) stampsGiven else 0)
        }

        return applyResponses
    }

    override fun state(accountId: AccountId): List<StateResponse> {
        val stateResponses = mutableListOf<StateResponse>()
        schemes.forEach { scheme ->
            val acc = schemeDetails[scheme.id]
            stateResponses.add(StateResponse(schemeId = scheme.id, currentStampCount = acc?.currentStampCount!!, payments = acc.payments))
        }

        return stateResponses
    }


    /**
     * checks if the scheme can be applied to an item
     * @param item
     * @param skus
     * @return true if scheme can be applied
     */
    private fun canSchemeBeApplied(item: Item, skus: List<String>) = skus.contains(item.sku)

    /**
     * checks if the scheme can be applied to a list of item
     * @param items
     * @param skus
     * @return List of Items  that scheme can be applied
     */
    private fun canSchemeBeApplied(items: List<Item>, skus: List<String>) = items.filter { canSchemeBeApplied(it, skus) }

    /**
     * Total number of stamps generated by an Item
     * @param item
     * @return total number of stamps
     */
    private fun stampsGenerated(item: Item) = item.quantity

    /**
     * Get the total number of payment generated by an account in a scheme
     * @param totalStamps
     * @param maxStamps
     * @return Int the total number of payments generated
     */
    private fun generatePayment(totalStamps: Int, maxStamps: Int): Int {
        if (totalStamps <= maxStamps) return 0

        return totalStamps.div(maxStamps + 1)
    }

    /**
     * Get the stamp count position in the current apply
     * @param totalStamps
     * @param maxStamps
     */
    private fun stampPosition(totalStamps: Int, maxStamps: Int): Int {
        return if (totalStamps <= maxStamps) {
            totalStamps
        } else {
            totalStamps % (maxStamps + 1)
        }
    }

    /**
     * Get all the value of the payments to be redeemed by the account
     * @param listOfItems
     * @param paymentCount
     * @param maxStamps
     *
     * @return List of values of the payment generated by the account
     */
    private fun payments(listOfItems: List<Item>, paymentCount: Int, maxStamps: Int): List<Long> {
        if (paymentCount < 1) return emptyList()
        val paymentItems = mutableListOf<Long>()
        for (i in 1.rangeTo(paymentCount)) {
            val index = if (i <= 1) i*maxStamps else (i*maxStamps +(i-1))
            paymentItems.add(listOfItems[index].price)
        }
        return paymentItems
    }
}
