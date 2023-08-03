package no.nordicsemi.kotlin.mesh.core

/**
 * Enum class that defines Proxy filter types.
 *
 * @property type Filter type.
 */
enum class ProxyFilterType(val type: UByte) {

    /**
     * An inclusion list filter has an associated inclusion list containing destination addresses
     * that are of interest for the Proxy Client.
     *
     * The inclusion list filter blocks all messages except those targeting addresses added to the
     * list.
     */
    INCLUSION_LIST(0x00u),

    /**
     * An exclusion list filter has an associated exclusion list containing destination addresses
     * that are NOT of the Proxy Client interest.
     *
     * The exclusion list filter forwards all messages except those targeting addresses added to the
     * list.
     */
    EXCLUSION_LIST(0x01u);

    companion object {

        /**
         * Returns the [ProxyFilterType] from the given filter type.
         *
         * @param filterType Filter type.
         * @return [ProxyFilterType] or if invalid throws an [IllegalArgumentException] exception.
         * @throws IllegalArgumentException if the filter type is invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(filterType: UByte) = ProxyFilterType.values().find {
            it.type == filterType
        } ?: throw IllegalArgumentException("Illegal filter type")
    }
}