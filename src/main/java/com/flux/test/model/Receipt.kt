package com.flux.test.model

import java.util.UUID

data class Receipt(
    val id: ReceiptId = UUID.randomUUID(),
    val accountId: AccountId,
    val merchantId: MerchantId,
    val items: List<Item>
)

data class Item(
    /**
     * The unique ID of the item - only unique for one merchant and may be duplicated across merchants
     */
    val sku: String,

    /**
     * The price of one instance of this item, the total amount of the receipt is the sum of item.price * item.quantity for all the items in the receipt
     */
    val price: Long,

    /**
     * Quantity of this item purchased, if an item has a quantity of 2 then it can generate 2 stamps
     */
    val quantity: Int
)

data class AccountDetails(
        /**
         * The unique ID of the account
         */
        val accountId: AccountId,
        /**
         * The Unique ID of the scheme
         */
        val schemeId: SchemeId,
        /**
         * The payments given for the scheme
         */
        val payments: List<Long>,
        /**
         * The current stamp position for the account in this scheme, e.g. if the account has 1/4 stamps then this would return 1
         */
        val currentStampCount: Int
)