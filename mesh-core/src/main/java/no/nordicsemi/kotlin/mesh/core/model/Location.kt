@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

/**
 * Locations defined by bluetooth SIG.
 * Refer: https://www.bluetooth.com/specifications/assigned-numbers/gatt-namespace-descriptors
 *
 * @property value 16-bit unsigned location value.
 */
enum class Location(val value: UShort) {
    AUXILIARY(0x0108u),
    BACK(0x0101u),
    BACKUP(0x0107u),
    BOTTOM(0x0103u),
    EIGHTEENTH(0x0012u),
    EIGHTH(0x0008u),
    EIGHTIETH(0x0050u),
    EIGHTY_EIGHTH(0x0058u),
    EIGHTY_FIFTH(0x0055u),
    EIGHTY_FIRST(0x0051u),
    EIGHTY_FOURTH(0x0054u),
    EIGHTY_NINETH(0x0059u),
    EIGHTY_SECOND(0x0052u),
    EIGHTY_SEVENTH(0x0057u),
    EIGHTY_SIXTH(0x0056u),
    EIGHTY_THIRD(0x0053u),
    ELEVENTH(0x000Bu),
    EXTERNAL(0x0110u),
    FIFTEENTH(0x000Fu),
    FIFTH(0x0005u),
    FIFTIETH(0x0032u),
    FIFTY_EIGHTH(0x003Au),
    FIFTY_FIFTH(0x0037u),
    FIFTY_FIRST(0x0033u),
    FIFTY_FOURTH(0x0036u),
    FIFTY_NINETH(0x003Bu),
    FIFTY_SECOND(0x0034u),
    FIFTY_SEVENTH(0x0039u),
    FIFTY_SIXTH(0x0038u),
    FIFTY_THIRD(0x0035u),
    FIRST(0x0001u),
    FLASH(0x010Au),
    FORTIETH(0x0028u),
    FOURTEENTH(0x000Eu),
    FOURTH(0x0004u),
    FOURTY_EIGHTH(0x0030u),
    FOURTY_FIFTH(0x002Du),
    FOURTY_FIRST(0x0029u),
    FOURTY_FOURTH(0x002Cu),
    FOURTY_NINETH(0x0031u),
    FOURTY_SECOND(0x002Au),
    FOURTY_SEVENTH(0x002Fu),
    FOURTY_SIXTH(0x002Eu),
    FOURTY_THIRD(0x002Bu),
    FRONT(0x0100u),
    INSIDE(0x010Bu),
    INTERNAL(0x010Fu),
    LEFT(0x010Du),
    LOWER(0x0105u),
    MAIN(0x0106u),
    NINETEENTH(0x0013u),
    NINETH(0x0009u),
    NINETIETH(0x005Au),
    NINETY_EIGHTH(0x0062u),
    NINETY_FIFTH(0x005Fu),
    NINETY_FIRST(0x005Bu),
    NINETY_FOURTH(0x005Eu),
    NINETY_NINETH(0x0063u),
    NINETY_SECOND(0x005Cu),
    NINETY_SEVENTH(0x0061u),
    NINETY_SIXTH(0x0060u),
    NINETY_THIRD(0x005Du),
    ONE_HUNDRED_AND_EIGHTEENTH(0x0076u),
    ONE_HUNDRED_AND_EIGHTH(0x006Cu),
    ONE_HUNDRED_AND_EIGHTY_EIGHTH(0x00BCu),
    ONE_HUNDRED_AND_EIGHTY_FIFTH(0x00B9u),
    ONE_HUNDRED_AND_EIGHTY_FIRST(0x00B5u),
    ONE_HUNDRED_AND_EIGHTY_FOURTH(0x00B8u),
    ONE_HUNDRED_AND_EIGHTY_NINETH(0x00BDu),
    ONE_HUNDRED_AND_EIGHTY_SECOND(0x00B6u),
    ONE_HUNDRED_AND_EIGHTY_SEVENTH(0x00BBu),
    ONE_HUNDRED_AND_EIGHTY_SIXTH(0x00BAu),
    ONE_HUNDRED_AND_EIGHTY_THIRD(0x00B7u),
    ONE_HUNDRED_AND_ELEVENTH(0x006Fu),
    ONE_HUNDRED_AND_FIFTEENTH(0x0073u),
    ONE_HUNDRED_AND_FIFTH(0x0069u),
    ONE_HUNDRED_AND_FIFTY_EIGHTH(0x009Eu),
    ONE_HUNDRED_AND_FIFTY_FIFTH(0x009Bu),
    ONE_HUNDRED_AND_FIFTY_FIRST(0x0097u),
    ONE_HUNDRED_AND_FIFTY_FOURTH(0x009Au),
    ONE_HUNDRED_AND_FIFTY_NINETH(0x009Fu),
    ONE_HUNDRED_AND_FIFTY_SECOND(0x0098u),
    ONE_HUNDRED_AND_FIFTY_SEVENTH(0x009Du),
    ONE_HUNDRED_AND_FIFTY_SIXTH(0x009Cu),
    ONE_HUNDRED_AND_FIFTY_THIRD(0x0099u),
    ONE_HUNDRED_AND_FIRST(0x0065u),
    ONE_HUNDRED_AND_FOURTEENTH(0x0072u),
    ONE_HUNDRED_AND_FOURTH(0x0068u),
    ONE_HUNDRED_AND_FOURTY_EIGHTH(0x0094u),
    ONE_HUNDRED_AND_FOURTY_FIFTH(0x0091u),
    ONE_HUNDRED_AND_FOURTY_FIRST(0x008Du),
    ONE_HUNDRED_AND_FOURTY_FOURTH(0x0090u),
    ONE_HUNDRED_AND_FOURTY_NINETH(0x0095u),
    ONE_HUNDRED_AND_FOURTY_SECOND(0x008Eu),
    ONE_HUNDRED_AND_FOURTY_SEVENTH(0x0093u),
    ONE_HUNDRED_AND_FOURTY_SIXTH(0x0092u),
    ONE_HUNDRED_AND_FOURTY_THIRD(0x008Fu),
    ONE_HUNDRED_AND_NINETEENTH(0x0077u),
    ONE_HUNDRED_AND_NINETH(0x006Du),
    ONE_HUNDRED_AND_NINETY_EIGHTH(0x00C6u),
    ONE_HUNDRED_AND_NINETY_FIFTH(0x00C3u),
    ONE_HUNDRED_AND_NINETY_FIRST(0x00BFu),
    ONE_HUNDRED_AND_NINETY_FOURTH(0x00C2u),
    ONE_HUNDRED_AND_NINETY_NINETH(0x00C7u),
    ONE_HUNDRED_AND_NINETY_SECOND(0x00C0u),
    ONE_HUNDRED_AND_NINETY_SEVENTH(0x00C5u),
    ONE_HUNDRED_AND_NINETY_SIXTH(0x00C4u),
    ONE_HUNDRED_AND_NINETY_THIRD(0x00C1u),
    ONE_HUNDRED_AND_SECOND(0x0066u),
    ONE_HUNDRED_AND_SEVENTEENTH(0x0075u),
    ONE_HUNDRED_AND_SEVENTH(0x006Bu),
    ONE_HUNDRED_AND_SEVENTY_EIGHTH(0x00B2u),
    ONE_HUNDRED_AND_SEVENTY_FIFTH(0x00AFu),
    ONE_HUNDRED_AND_SEVENTY_FIRST(0x00ABu),
    ONE_HUNDRED_AND_SEVENTY_FOURTH(0x00AEu),
    ONE_HUNDRED_AND_SEVENTY_NINETH(0x00B3u),
    ONE_HUNDRED_AND_SEVENTY_SECOND(0x00ACu),
    ONE_HUNDRED_AND_SEVENTY_SEVENTH(0x00B1u),
    ONE_HUNDRED_AND_SEVENTY_SIXTH(0x00B0u),
    ONE_HUNDRED_AND_SEVENTY_THIRD(0x00ADu),
    ONE_HUNDRED_AND_SIXTEENTH(0x0074u),
    ONE_HUNDRED_AND_SIXTH(0x006Au),
    ONE_HUNDRED_AND_SIXTY_EIGHTH(0x00A8u),
    ONE_HUNDRED_AND_SIXTY_FIFTH(0x00A5u),
    ONE_HUNDRED_AND_SIXTY_FIRST(0x00A1u),
    ONE_HUNDRED_AND_SIXTY_FOURTH(0x00A4u),
    ONE_HUNDRED_AND_SIXTY_NINETH(0x00A9u),
    ONE_HUNDRED_AND_SIXTY_SECOND(0x00A2u),
    ONE_HUNDRED_AND_SIXTY_SEVENTH(0x00A7u),
    ONE_HUNDRED_AND_SIXTY_SIXTH(0x00A6u),
    ONE_HUNDRED_AND_SIXTY_THIRD(0x00A3u),
    ONE_HUNDRED_AND_TENTH(0x006Eu),
    ONE_HUNDRED_AND_THIRD(0x0067u),
    ONE_HUNDRED_AND_THIRTEENTH(0x0071u),
    ONE_HUNDRED_AND_THIRTY_EIGHTH(0x008Au),
    ONE_HUNDRED_AND_THIRTY_FIFTH(0x0087u),
    ONE_HUNDRED_AND_THIRTY_FIRST(0x0083u),
    ONE_HUNDRED_AND_THIRTY_FOURTH(0x0086u),
    ONE_HUNDRED_AND_THIRTY_NINETH(0x008Bu),
    ONE_HUNDRED_AND_THIRTY_SECOND(0x0084u),
    ONE_HUNDRED_AND_THIRTY_SEVENTH(0x0089u),
    ONE_HUNDRED_AND_THIRTY_SIXTH(0x0088u),
    ONE_HUNDRED_AND_THIRTY_THIRD(0x0085u),
    ONE_HUNDRED_AND_TWELVETH(0x0070u),
    ONE_HUNDRED_AND_TWENTY_EIGHTH(0x0080u),
    ONE_HUNDRED_AND_TWENTY_FIFTH(0x007Du),
    ONE_HUNDRED_AND_TWENTY_FIRST(0x0079u),
    ONE_HUNDRED_AND_TWENTY_FOURTH(0x007Cu),
    ONE_HUNDRED_AND_TWENTY_NINETH(0x0081u),
    ONE_HUNDRED_AND_TWENTY_SECOND(0x007Au),
    ONE_HUNDRED_AND_TWENTY_SEVENTH(0x007Fu),
    ONE_HUNDRED_AND_TWENTY_SIXTH(0x007Eu),
    ONE_HUNDRED_AND_TWENTY_THIRD(0x007Bu),
    ONE_HUNDRED_EIGHTIETH(0x00B4u),
    ONE_HUNDRED_FIFTIETH(0x0096u),
    ONE_HUNDRED_FORTIETH(0x008Cu),
    ONE_HUNDRED_NINETIETH(0x00BEu),
    ONE_HUNDRED_SEVENTIETH(0x00AAu),
    ONE_HUNDRED_SIXTIETH(0x00A0u),
    ONE_HUNDRED_THIRTIETH(0x0082u),
    ONE_HUNDRED_TWENTIETH(0x0078u),
    ONE_HUNDREDTH(0x0064u),
    OUTSIDE(0x010Cu),
    RIGHT(0x010Eu),
    SECOND(0x0002u),
    SEVENTEENTH(0x0011u),
    SEVENTH(0x0007u),
    SEVENTIETH(0x0046u),
    SEVENTY_EIGHTH(0x004Eu),
    SEVENTY_FIFTH(0x004Bu),
    SEVENTY_FIRST(0x0047u),
    SEVENTY_FOURTH(0x004Au),
    SEVENTY_NINETH(0x004Fu),
    SEVENTY_SECOND(0x0048u),
    SEVENTY_SEVENTH(0x004Du),
    SEVENTY_SIXTH(0x004Cu),
    SEVENTY_THIRD(0x0049u),
    SIXTEENTH(0x0010u),
    SIXTH(0x0006u),
    SIXTIETH(0x003Cu),
    SIXTY_EIGHTH(0x0044u),
    SIXTY_FIFTH(0x0041u),
    SIXTY_FIRST(0x003Du),
    SIXTY_FOURTH(0x0040u),
    SIXTY_NINETH(0x0045u),
    SIXTY_SECOND(0x003Eu),
    SIXTY_SEVENTH(0x0043u),
    SIXTY_SIXTH(0x0042u),
    SIXTY_THIRD(0x003Fu),
    SUPPLEMENTARY(0x0109u),
    TENTH(0x000Au),
    THIRD(0x0003u),
    THIRTEENTH(0x000Du),
    THIRTIETH(0x001Eu),
    THIRTY_EIGHTH(0x0026u),
    THIRTY_FIFTH(0x0023u),
    THIRTY_FIRST(0x001Fu),
    THIRTY_FOURTH(0x0022u),
    THIRTY_NINETH(0x0027u),
    THIRTY_SECOND(0x0020u),
    THIRTY_SEVENTH(0x0025u),
    THIRTY_SIXTH(0x0024u),
    THIRTY_THIRD(0x0021u),
    TOP(0x0102u),
    TWELVETH(0x000Cu),
    TWENTIETH(0x0014u),
    TWENTY_EIGHTH(0x001Cu),
    TWENTY_FIFTH(0x0019u),
    TWENTY_FIRST(0x0015u),
    TWENTY_FOURTH(0x0018u),
    TWENTY_NINETH(0x001Du),
    TWENTY_SECOND(0x0016u),
    TWENTY_SEVENTH(0x001Bu),
    TWENTY_SIXTH(0x001Au),
    TWENTY_THIRD(0x0017u),
    TWO_HUNDRED_AND_EIGHTEENTH(0x00DAu),
    TWO_HUNDRED_AND_EIGHTH(0x00D0u),
    TWO_HUNDRED_AND_ELEVENTH(0x00D3u),
    TWO_HUNDRED_AND_FIFTEENTH(0x00D7u),
    TWO_HUNDRED_AND_FIFTH(0x00CDu),
    TWO_HUNDRED_AND_FIFTY_FIFTH(0x00FFu),
    TWO_HUNDRED_AND_FIFTY_FIRST(0x00FBu),
    TWO_HUNDRED_AND_FIFTY_FOURTH(0x00FEu),
    TWO_HUNDRED_AND_FIFTY_SECOND(0x00FCu),
    TWO_HUNDRED_AND_FIFTY_THIRD(0x00FDu),
    TWO_HUNDRED_AND_FIRST(0x00C9u),
    TWO_HUNDRED_AND_FOURTEENTH(0x00D6u),
    TWO_HUNDRED_AND_FOURTH(0x00CCu),
    TWO_HUNDRED_AND_FOURTY_EIGHTH(0x00F8u),
    TWO_HUNDRED_AND_FOURTY_FIFTH(0x00F5u),
    TWO_HUNDRED_AND_FOURTY_FIRST(0x00F1u),
    TWO_HUNDRED_AND_FOURTY_FOURTH(0x00F4u),
    TWO_HUNDRED_AND_FOURTY_NINETH(0x00F9u),
    TWO_HUNDRED_AND_FOURTY_SECOND(0x00F2u),
    TWO_HUNDRED_AND_FOURTY_SEVENTH(0x00F7u),
    TWO_HUNDRED_AND_FOURTY_SIXTH(0x00F6u),
    TWO_HUNDRED_AND_FOURTY_THIRD(0x00F3u),
    TWO_HUNDRED_AND_NINETEENTH(0x00DBu),
    TWO_HUNDRED_AND_NINETH(0x00D1u),
    TWO_HUNDRED_AND_SECOND(0x00CAu),
    TWO_HUNDRED_AND_SEVENTEENTH(0x00D9u),
    TWO_HUNDRED_AND_SEVENTH(0x00CFu),
    TWO_HUNDRED_AND_SIXTEENTH(0x00D8u),
    TWO_HUNDRED_AND_SIXTH(0x00CEu),
    TWO_HUNDRED_AND_TENTH(0x00D2u),
    TWO_HUNDRED_AND_THIRD(0x00CBu),
    TWO_HUNDRED_AND_THIRTEENTH(0x00D5u),
    TWO_HUNDRED_AND_THIRTY_EIGHTH(0x00EEu),
    TWO_HUNDRED_AND_THIRTY_FIFTH(0x00EBu),
    TWO_HUNDRED_AND_THIRTY_FIRST(0x00E7u),
    TWO_HUNDRED_AND_THIRTY_FOURTH(0x00EAu),
    TWO_HUNDRED_AND_THIRTY_NINETH(0x00EFu),
    TWO_HUNDRED_AND_THIRTY_SECOND(0x00E8u),
    TWO_HUNDRED_AND_THIRTY_SEVENTH(0x00EDu),
    TWO_HUNDRED_AND_THIRTY_SIXTH(0x00ECu),
    TWO_HUNDRED_AND_THIRTY_THIRD(0x00E9u),
    TWO_HUNDRED_AND_TWELVETH(0x00D4u),
    TWO_HUNDRED_AND_TWENTY_EIGHTH(0x00E4u),
    TWO_HUNDRED_AND_TWENTY_FIFTH(0x00E1u),
    TWO_HUNDRED_AND_TWENTY_FIRST(0x00DDu),
    TWO_HUNDRED_AND_TWENTY_FOURTH(0x00E0u),
    TWO_HUNDRED_AND_TWENTY_NINETH(0x00E5u),
    TWO_HUNDRED_AND_TWENTY_SECOND(0x00DEu),
    TWO_HUNDRED_AND_TWENTY_SEVENTH(0x00E3u),
    TWO_HUNDRED_AND_TWENTY_SIXTH(0x00E2u),
    TWO_HUNDRED_AND_TWENTY_THIRD(0x00DFu),
    TWO_HUNDRED_FIFTIETH(0x00FAu),
    TWO_HUNDRED_FORTIETH(0x00F0u),
    TWO_HUNDRED_THIRTIETH(0x00E6u),
    TWO_HUNDRED_TWENTIETH(0x00DCu),
    TWO_HUNDREDTH(0x00C8u),
    UNKNOWN(0x0000u),
    UPPER(0x0104u);

    internal companion object {

        /**
         * Returns the Location for a given location value.
         *
         * @param value 16-bit value of location.
         * @return Returns the Location type.
         */
        fun from(value: UShort) = when (value.toInt()) {
            0x0108u.toInt() -> AUXILIARY
            0x0101u.toInt() -> BACK
            0x0107u.toInt() -> BACKUP
            0x0103u.toInt() -> BOTTOM
            0x0012u.toInt() -> EIGHTEENTH
            0x0008u.toInt() -> EIGHTH
            0x0050u.toInt() -> EIGHTIETH
            0x0058u.toInt() -> EIGHTY_EIGHTH
            0x0055u.toInt() -> EIGHTY_FIFTH
            0x0051u.toInt() -> EIGHTY_FIRST
            0x0054u.toInt() -> EIGHTY_FOURTH
            0x0059u.toInt() -> EIGHTY_NINETH
            0x0052u.toInt() -> EIGHTY_SECOND
            0x0057u.toInt() -> EIGHTY_SEVENTH
            0x0056u.toInt() -> EIGHTY_SIXTH
            0x0053u.toInt() -> EIGHTY_THIRD
            0x000bu.toInt() -> ELEVENTH
            0x0110u.toInt() -> EXTERNAL
            0x000fu.toInt() -> FIFTEENTH
            0x0005u.toInt() -> FIFTH
            0x0032u.toInt() -> FIFTIETH
            0x003au.toInt() -> FIFTY_EIGHTH
            0x0037u.toInt() -> FIFTY_FIFTH
            0x0033u.toInt() -> FIFTY_FIRST
            0x0036u.toInt() -> FIFTY_FOURTH
            0x003bu.toInt() -> FIFTY_NINETH
            0x0034u.toInt() -> FIFTY_SECOND
            0x0039u.toInt() -> FIFTY_SEVENTH
            0x0038u.toInt() -> FIFTY_SIXTH
            0x0035u.toInt() -> FIFTY_THIRD
            0x0001u.toInt() -> FIRST
            0x010Au.toInt() -> FLASH
            0x0028u.toInt() -> FORTIETH
            0x000eu.toInt() -> FOURTEENTH
            0x0004u.toInt() -> FOURTH
            0x0030u.toInt() -> FOURTY_EIGHTH
            0x002du.toInt() -> FOURTY_FIFTH
            0x0029u.toInt() -> FOURTY_FIRST
            0x002cu.toInt() -> FOURTY_FOURTH
            0x0031u.toInt() -> FOURTY_NINETH
            0x002au.toInt() -> FOURTY_SECOND
            0x002fu.toInt() -> FOURTY_SEVENTH
            0x002eu.toInt() -> FOURTY_SIXTH
            0x002bu.toInt() -> FOURTY_THIRD
            0x0100u.toInt() -> FRONT
            0x010Bu.toInt() -> INSIDE
            0x010Fu.toInt() -> INTERNAL
            0x010Du.toInt() -> LEFT
            0x0105u.toInt() -> LOWER
            0x0106u.toInt() -> MAIN
            0x0013u.toInt() -> NINETEENTH
            0x0009u.toInt() -> NINETH
            0x005au.toInt() -> NINETIETH
            0x0062u.toInt() -> NINETY_EIGHTH
            0x005fu.toInt() -> NINETY_FIFTH
            0x005bu.toInt() -> NINETY_FIRST
            0x005eu.toInt() -> NINETY_FOURTH
            0x0063u.toInt() -> NINETY_NINETH
            0x005cu.toInt() -> NINETY_SECOND
            0x0061u.toInt() -> NINETY_SEVENTH
            0x0060u.toInt() -> NINETY_SIXTH
            0x005du.toInt() -> NINETY_THIRD
            0x0076u.toInt() -> ONE_HUNDRED_AND_EIGHTEENTH
            0x006cu.toInt() -> ONE_HUNDRED_AND_EIGHTH
            0x00bcu.toInt() -> ONE_HUNDRED_AND_EIGHTY_EIGHTH
            0x00b9u.toInt() -> ONE_HUNDRED_AND_EIGHTY_FIFTH
            0x00b5u.toInt() -> ONE_HUNDRED_AND_EIGHTY_FIRST
            0x00b8u.toInt() -> ONE_HUNDRED_AND_EIGHTY_FOURTH
            0x00bdu.toInt() -> ONE_HUNDRED_AND_EIGHTY_NINETH
            0x00b6u.toInt() -> ONE_HUNDRED_AND_EIGHTY_SECOND
            0x00bbu.toInt() -> ONE_HUNDRED_AND_EIGHTY_SEVENTH
            0x00bau.toInt() -> ONE_HUNDRED_AND_EIGHTY_SIXTH
            0x00b7u.toInt() -> ONE_HUNDRED_AND_EIGHTY_THIRD
            0x006fu.toInt() -> ONE_HUNDRED_AND_ELEVENTH
            0x0073u.toInt() -> ONE_HUNDRED_AND_FIFTEENTH
            0x0069u.toInt() -> ONE_HUNDRED_AND_FIFTH
            0x009eu.toInt() -> ONE_HUNDRED_AND_FIFTY_EIGHTH
            0x009bu.toInt() -> ONE_HUNDRED_AND_FIFTY_FIFTH
            0x0097u.toInt() -> ONE_HUNDRED_AND_FIFTY_FIRST
            0x009au.toInt() -> ONE_HUNDRED_AND_FIFTY_FOURTH
            0x009fu.toInt() -> ONE_HUNDRED_AND_FIFTY_NINETH
            0x0098u.toInt() -> ONE_HUNDRED_AND_FIFTY_SECOND
            0x009du.toInt() -> ONE_HUNDRED_AND_FIFTY_SEVENTH
            0x009cu.toInt() -> ONE_HUNDRED_AND_FIFTY_SIXTH
            0x0099u.toInt() -> ONE_HUNDRED_AND_FIFTY_THIRD
            0x0065u.toInt() -> ONE_HUNDRED_AND_FIRST
            0x0072u.toInt() -> ONE_HUNDRED_AND_FOURTEENTH
            0x0068u.toInt() -> ONE_HUNDRED_AND_FOURTH
            0x0094u.toInt() -> ONE_HUNDRED_AND_FOURTY_EIGHTH
            0x0091u.toInt() -> ONE_HUNDRED_AND_FOURTY_FIFTH
            0x008du.toInt() -> ONE_HUNDRED_AND_FOURTY_FIRST
            0x0090u.toInt() -> ONE_HUNDRED_AND_FOURTY_FOURTH
            0x0095u.toInt() -> ONE_HUNDRED_AND_FOURTY_NINETH
            0x008eu.toInt() -> ONE_HUNDRED_AND_FOURTY_SECOND
            0x0093u.toInt() -> ONE_HUNDRED_AND_FOURTY_SEVENTH
            0x0092u.toInt() -> ONE_HUNDRED_AND_FOURTY_SIXTH
            0x008fu.toInt() -> ONE_HUNDRED_AND_FOURTY_THIRD
            0x0077u.toInt() -> ONE_HUNDRED_AND_NINETEENTH
            0x006du.toInt() -> ONE_HUNDRED_AND_NINETH
            0x00c6u.toInt() -> ONE_HUNDRED_AND_NINETY_EIGHTH
            0x00c3u.toInt() -> ONE_HUNDRED_AND_NINETY_FIFTH
            0x00bfu.toInt() -> ONE_HUNDRED_AND_NINETY_FIRST
            0x00c2u.toInt() -> ONE_HUNDRED_AND_NINETY_FOURTH
            0x00c7u.toInt() -> ONE_HUNDRED_AND_NINETY_NINETH
            0x00c0u.toInt() -> ONE_HUNDRED_AND_NINETY_SECOND
            0x00c5u.toInt() -> ONE_HUNDRED_AND_NINETY_SEVENTH
            0x00c4u.toInt() -> ONE_HUNDRED_AND_NINETY_SIXTH
            0x00c1u.toInt() -> ONE_HUNDRED_AND_NINETY_THIRD
            0x0066u.toInt() -> ONE_HUNDRED_AND_SECOND
            0x0075u.toInt() -> ONE_HUNDRED_AND_SEVENTEENTH
            0x006bu.toInt() -> ONE_HUNDRED_AND_SEVENTH
            0x00b2u.toInt() -> ONE_HUNDRED_AND_SEVENTY_EIGHTH
            0x00afu.toInt() -> ONE_HUNDRED_AND_SEVENTY_FIFTH
            0x00abu.toInt() -> ONE_HUNDRED_AND_SEVENTY_FIRST
            0x00aeu.toInt() -> ONE_HUNDRED_AND_SEVENTY_FOURTH
            0x00b3u.toInt() -> ONE_HUNDRED_AND_SEVENTY_NINETH
            0x00acu.toInt() -> ONE_HUNDRED_AND_SEVENTY_SECOND
            0x00b1u.toInt() -> ONE_HUNDRED_AND_SEVENTY_SEVENTH
            0x00b0u.toInt() -> ONE_HUNDRED_AND_SEVENTY_SIXTH
            0x00adu.toInt() -> ONE_HUNDRED_AND_SEVENTY_THIRD
            0x0074u.toInt() -> ONE_HUNDRED_AND_SIXTEENTH
            0x006au.toInt() -> ONE_HUNDRED_AND_SIXTH
            0x00a8u.toInt() -> ONE_HUNDRED_AND_SIXTY_EIGHTH
            0x00a5u.toInt() -> ONE_HUNDRED_AND_SIXTY_FIFTH
            0x00a1u.toInt() -> ONE_HUNDRED_AND_SIXTY_FIRST
            0x00a4u.toInt() -> ONE_HUNDRED_AND_SIXTY_FOURTH
            0x00a9u.toInt() -> ONE_HUNDRED_AND_SIXTY_NINETH
            0x00a2u.toInt() -> ONE_HUNDRED_AND_SIXTY_SECOND
            0x00a7u.toInt() -> ONE_HUNDRED_AND_SIXTY_SEVENTH
            0x00a6u.toInt() -> ONE_HUNDRED_AND_SIXTY_SIXTH
            0x00a3u.toInt() -> ONE_HUNDRED_AND_SIXTY_THIRD
            0x006eu.toInt() -> ONE_HUNDRED_AND_TENTH
            0x0067u.toInt() -> ONE_HUNDRED_AND_THIRD
            0x0071u.toInt() -> ONE_HUNDRED_AND_THIRTEENTH
            0x008au.toInt() -> ONE_HUNDRED_AND_THIRTY_EIGHTH
            0x0087u.toInt() -> ONE_HUNDRED_AND_THIRTY_FIFTH
            0x0083u.toInt() -> ONE_HUNDRED_AND_THIRTY_FIRST
            0x0086u.toInt() -> ONE_HUNDRED_AND_THIRTY_FOURTH
            0x008bu.toInt() -> ONE_HUNDRED_AND_THIRTY_NINETH
            0x0084u.toInt() -> ONE_HUNDRED_AND_THIRTY_SECOND
            0x0089u.toInt() -> ONE_HUNDRED_AND_THIRTY_SEVENTH
            0x0088u.toInt() -> ONE_HUNDRED_AND_THIRTY_SIXTH
            0x0085u.toInt() -> ONE_HUNDRED_AND_THIRTY_THIRD
            0x0070u.toInt() -> ONE_HUNDRED_AND_TWELVETH
            0x0080u.toInt() -> ONE_HUNDRED_AND_TWENTY_EIGHTH
            0x007du.toInt() -> ONE_HUNDRED_AND_TWENTY_FIFTH
            0x0079u.toInt() -> ONE_HUNDRED_AND_TWENTY_FIRST
            0x007cu.toInt() -> ONE_HUNDRED_AND_TWENTY_FOURTH
            0x0081u.toInt() -> ONE_HUNDRED_AND_TWENTY_NINETH
            0x007au.toInt() -> ONE_HUNDRED_AND_TWENTY_SECOND
            0x007fu.toInt() -> ONE_HUNDRED_AND_TWENTY_SEVENTH
            0x007eu.toInt() -> ONE_HUNDRED_AND_TWENTY_SIXTH
            0x007bu.toInt() -> ONE_HUNDRED_AND_TWENTY_THIRD
            0x00b4u.toInt() -> ONE_HUNDRED_EIGHTIETH
            0x0096u.toInt() -> ONE_HUNDRED_FIFTIETH
            0x008cu.toInt() -> ONE_HUNDRED_FORTIETH
            0x00beu.toInt() -> ONE_HUNDRED_NINETIETH
            0x00aau.toInt() -> ONE_HUNDRED_SEVENTIETH
            0x00a0u.toInt() -> ONE_HUNDRED_SIXTIETH
            0x0082u.toInt() -> ONE_HUNDRED_THIRTIETH
            0x0078u.toInt() -> ONE_HUNDRED_TWENTIETH
            0x0064u.toInt() -> ONE_HUNDREDTH
            0x010Cu.toInt() -> OUTSIDE
            0x010Eu.toInt() -> RIGHT
            0x0002u.toInt() -> SECOND
            0x0011u.toInt() -> SEVENTEENTH
            0x0007u.toInt() -> SEVENTH
            0x0046u.toInt() -> SEVENTIETH
            0x004eu.toInt() -> SEVENTY_EIGHTH
            0x004bu.toInt() -> SEVENTY_FIFTH
            0x0047u.toInt() -> SEVENTY_FIRST
            0x004au.toInt() -> SEVENTY_FOURTH
            0x004fu.toInt() -> SEVENTY_NINETH
            0x0048u.toInt() -> SEVENTY_SECOND
            0x004du.toInt() -> SEVENTY_SEVENTH
            0x004cu.toInt() -> SEVENTY_SIXTH
            0x0049u.toInt() -> SEVENTY_THIRD
            0x0010u.toInt() -> SIXTEENTH
            0x0006u.toInt() -> SIXTH
            0x003cu.toInt() -> SIXTIETH
            0x0044u.toInt() -> SIXTY_EIGHTH
            0x0041u.toInt() -> SIXTY_FIFTH
            0x003du.toInt() -> SIXTY_FIRST
            0x0040u.toInt() -> SIXTY_FOURTH
            0x0045u.toInt() -> SIXTY_NINETH
            0x003eu.toInt() -> SIXTY_SECOND
            0x0043u.toInt() -> SIXTY_SEVENTH
            0x0042u.toInt() -> SIXTY_SIXTH
            0x003fu.toInt() -> SIXTY_THIRD
            0x0109u.toInt() -> SUPPLEMENTARY
            0x000au.toInt() -> TENTH
            0x0003u.toInt() -> THIRD
            0x000du.toInt() -> THIRTEENTH
            0x001eu.toInt() -> THIRTIETH
            0x0026u.toInt() -> THIRTY_EIGHTH
            0x0023u.toInt() -> THIRTY_FIFTH
            0x001fu.toInt() -> THIRTY_FIRST
            0x0022u.toInt() -> THIRTY_FOURTH
            0x0027u.toInt() -> THIRTY_NINETH
            0x0020u.toInt() -> THIRTY_SECOND
            0x0025u.toInt() -> THIRTY_SEVENTH
            0x0024u.toInt() -> THIRTY_SIXTH
            0x0021u.toInt() -> THIRTY_THIRD
            0x0102u.toInt() -> TOP
            0x000cu.toInt() -> TWELVETH
            0x0014u.toInt() -> TWENTIETH
            0x001cu.toInt() -> TWENTY_EIGHTH
            0x0019u.toInt() -> TWENTY_FIFTH
            0x0015u.toInt() -> TWENTY_FIRST
            0x0018u.toInt() -> TWENTY_FOURTH
            0x001du.toInt() -> TWENTY_NINETH
            0x0016u.toInt() -> TWENTY_SECOND
            0x001bu.toInt() -> TWENTY_SEVENTH
            0x001au.toInt() -> TWENTY_SIXTH
            0x0017u.toInt() -> TWENTY_THIRD
            0x00dau.toInt() -> TWO_HUNDRED_AND_EIGHTEENTH
            0x00d0u.toInt() -> TWO_HUNDRED_AND_EIGHTH
            0x00d3u.toInt() -> TWO_HUNDRED_AND_ELEVENTH
            0x00d7u.toInt() -> TWO_HUNDRED_AND_FIFTEENTH
            0x00cdu.toInt() -> TWO_HUNDRED_AND_FIFTH
            0x00ffu.toInt() -> TWO_HUNDRED_AND_FIFTY_FIFTH
            0x00fbu.toInt() -> TWO_HUNDRED_AND_FIFTY_FIRST
            0x00feu.toInt() -> TWO_HUNDRED_AND_FIFTY_FOURTH
            0x00fcu.toInt() -> TWO_HUNDRED_AND_FIFTY_SECOND
            0x00fdu.toInt() -> TWO_HUNDRED_AND_FIFTY_THIRD
            0x00c9u.toInt() -> TWO_HUNDRED_AND_FIRST
            0x00d6u.toInt() -> TWO_HUNDRED_AND_FOURTEENTH
            0x00ccu.toInt() -> TWO_HUNDRED_AND_FOURTH
            0x00f8u.toInt() -> TWO_HUNDRED_AND_FOURTY_EIGHTH
            0x00f5u.toInt() -> TWO_HUNDRED_AND_FOURTY_FIFTH
            0x00f1u.toInt() -> TWO_HUNDRED_AND_FOURTY_FIRST
            0x00f4u.toInt() -> TWO_HUNDRED_AND_FOURTY_FOURTH
            0x00f9u.toInt() -> TWO_HUNDRED_AND_FOURTY_NINETH
            0x00f2u.toInt() -> TWO_HUNDRED_AND_FOURTY_SECOND
            0x00f7u.toInt() -> TWO_HUNDRED_AND_FOURTY_SEVENTH
            0x00f6u.toInt() -> TWO_HUNDRED_AND_FOURTY_SIXTH
            0x00f3u.toInt() -> TWO_HUNDRED_AND_FOURTY_THIRD
            0x00dbu.toInt() -> TWO_HUNDRED_AND_NINETEENTH
            0x00d1u.toInt() -> TWO_HUNDRED_AND_NINETH
            0x00cau.toInt() -> TWO_HUNDRED_AND_SECOND
            0x00d9u.toInt() -> TWO_HUNDRED_AND_SEVENTEENTH
            0x00cfu.toInt() -> TWO_HUNDRED_AND_SEVENTH
            0x00d8u.toInt() -> TWO_HUNDRED_AND_SIXTEENTH
            0x00ceu.toInt() -> TWO_HUNDRED_AND_SIXTH
            0x00d2u.toInt() -> TWO_HUNDRED_AND_TENTH
            0x00cbu.toInt() -> TWO_HUNDRED_AND_THIRD
            0x00d5u.toInt() -> TWO_HUNDRED_AND_THIRTEENTH
            0x00eeu.toInt() -> TWO_HUNDRED_AND_THIRTY_EIGHTH
            0x00ebu.toInt() -> TWO_HUNDRED_AND_THIRTY_FIFTH
            0x00e7u.toInt() -> TWO_HUNDRED_AND_THIRTY_FIRST
            0x00eau.toInt() -> TWO_HUNDRED_AND_THIRTY_FOURTH
            0x00efu.toInt() -> TWO_HUNDRED_AND_THIRTY_NINETH
            0x00e8u.toInt() -> TWO_HUNDRED_AND_THIRTY_SECOND
            0x00edu.toInt() -> TWO_HUNDRED_AND_THIRTY_SEVENTH
            0x00ecu.toInt() -> TWO_HUNDRED_AND_THIRTY_SIXTH
            0x00e9u.toInt() -> TWO_HUNDRED_AND_THIRTY_THIRD
            0x00d4u.toInt() -> TWO_HUNDRED_AND_TWELVETH
            0x00e4u.toInt() -> TWO_HUNDRED_AND_TWENTY_EIGHTH
            0x00e1u.toInt() -> TWO_HUNDRED_AND_TWENTY_FIFTH
            0x00ddu.toInt() -> TWO_HUNDRED_AND_TWENTY_FIRST
            0x00e0u.toInt() -> TWO_HUNDRED_AND_TWENTY_FOURTH
            0x00e5u.toInt() -> TWO_HUNDRED_AND_TWENTY_NINETH
            0x00deu.toInt() -> TWO_HUNDRED_AND_TWENTY_SECOND
            0x00e3u.toInt() -> TWO_HUNDRED_AND_TWENTY_SEVENTH
            0x00e2u.toInt() -> TWO_HUNDRED_AND_TWENTY_SIXTH
            0x00dfu.toInt() -> TWO_HUNDRED_AND_TWENTY_THIRD
            0x00fau.toInt() -> TWO_HUNDRED_FIFTIETH
            0x00f0u.toInt() -> TWO_HUNDRED_FORTIETH
            0x00e6u.toInt() -> TWO_HUNDRED_THIRTIETH
            0x00dcu.toInt() -> TWO_HUNDRED_TWENTIETH
            0x00c8u.toInt() -> TWO_HUNDREDTH
            0x0104u.toInt() -> UPPER
            else -> UNKNOWN
        }

        /**
         * Returns the description for a given location.
         *
         * @param value Location.
         * @return Returns the human readable description of the location.
         */
        fun nameOf(value: Location) = when (value) {
            AUXILIARY -> "Auxiliary"
            BACK -> "Back"
            BACKUP -> "Backup"
            BOTTOM -> "Bottom"
            EIGHTEENTH -> "Eighteenth"
            EIGHTH -> "Eighth"
            EIGHTIETH -> "Eightieth"
            EIGHTY_EIGHTH -> "Eighty-eighth"
            EIGHTY_FIFTH -> "Eighty-fifth"
            EIGHTY_FIRST -> "Eighty-first"
            EIGHTY_FOURTH -> "Eighty-fourth"
            EIGHTY_NINETH -> "Eighty-nineth"
            EIGHTY_SECOND -> "Eighty-second"
            EIGHTY_SEVENTH -> "Eighty-seventh"
            EIGHTY_SIXTH -> "Eighty-sixth"
            EIGHTY_THIRD -> "Eighty-third"
            ELEVENTH -> "Eleventh"
            EXTERNAL -> "External"
            FIFTEENTH -> "Fifteenth"
            FIFTH -> "Fifth"
            FIFTIETH -> "Fiftieth"
            FIFTY_EIGHTH -> "Fifty-eighth"
            FIFTY_FIFTH -> "Fifty-fifth"
            FIFTY_FIRST -> "Fifty-first"
            FIFTY_FOURTH -> "Fifty-fourth"
            FIFTY_NINETH -> "Fifty-nineth"
            FIFTY_SECOND -> "Fifty-second"
            FIFTY_SEVENTH -> "Fifty-seventh"
            FIFTY_SIXTH -> "Fifty-sixth"
            FIFTY_THIRD -> "Fifty-third"
            FIRST -> "First"
            FLASH -> "Flash"
            FORTIETH -> "Fortieth"
            FOURTEENTH -> "Fourteenth"
            FOURTH -> "Fourth"
            FOURTY_EIGHTH -> "Fourty-eighth"
            FOURTY_FIFTH -> "Fourty-fifth"
            FOURTY_FIRST -> "Fourty-first"
            FOURTY_FOURTH -> "Fourty-fourth"
            FOURTY_NINETH -> "Fourty-nineth"
            FOURTY_SECOND -> "Fourty-second"
            FOURTY_SEVENTH -> "Fourty-seventh"
            FOURTY_SIXTH -> "Fourty-sixth"
            FOURTY_THIRD -> "Fourty-third"
            FRONT -> "Front"
            INSIDE -> "Inside"
            INTERNAL -> "Internal"
            LEFT -> "Left"
            LOWER -> "Lower"
            MAIN -> "Main"
            NINETEENTH -> "Nineteenth"
            NINETH -> "Nineth"
            NINETIETH -> "Ninetieth"
            NINETY_EIGHTH -> "Ninety-eighth"
            NINETY_FIFTH -> "Ninety-fifth"
            NINETY_FIRST -> "Ninety-first"
            NINETY_FOURTH -> "Ninety-fourth"
            NINETY_NINETH -> "Ninety-nineth"
            NINETY_SECOND -> "Ninety-second"
            NINETY_SEVENTH -> "Ninety-seventh"
            NINETY_SIXTH -> "Ninety-sixth"
            NINETY_THIRD -> "Ninety-third"
            ONE_HUNDRED_AND_EIGHTEENTH -> "One-hundred-and-eighteenth"
            ONE_HUNDRED_AND_EIGHTH -> "One-hundred-and-eighth"
            ONE_HUNDRED_AND_EIGHTY_EIGHTH -> "One-hundred-and-eighty-eighth"
            ONE_HUNDRED_AND_EIGHTY_FIFTH -> "One-hundred-and-eighty-fifth"
            ONE_HUNDRED_AND_EIGHTY_FIRST -> "One-hundred-and-eighty-first"
            ONE_HUNDRED_AND_EIGHTY_FOURTH -> "One-hundred-and-eighty-fourth"
            ONE_HUNDRED_AND_EIGHTY_NINETH -> "One-hundred-and-eighty-nineth"
            ONE_HUNDRED_AND_EIGHTY_SECOND -> "One-hundred-and-eighty-second"
            ONE_HUNDRED_AND_EIGHTY_SEVENTH -> "One-hundred-and-eighty-seventh"
            ONE_HUNDRED_AND_EIGHTY_SIXTH -> "One-hundred-and-eighty-sixth"
            ONE_HUNDRED_AND_EIGHTY_THIRD -> "One-hundred-and-eighty-third"
            ONE_HUNDRED_AND_ELEVENTH -> "One-hundred-and-eleventh"
            ONE_HUNDRED_AND_FIFTEENTH -> "One-hundred-and-fifteenth"
            ONE_HUNDRED_AND_FIFTH -> "One-hundred-and-fifth"
            ONE_HUNDRED_AND_FIFTY_EIGHTH -> "One-hundred-and-fifty-eighth"
            ONE_HUNDRED_AND_FIFTY_FIFTH -> "One-hundred-and-fifty-fifth"
            ONE_HUNDRED_AND_FIFTY_FIRST -> "One-hundred-and-fifty-first"
            ONE_HUNDRED_AND_FIFTY_FOURTH -> "One-hundred-and-fifty-fourth"
            ONE_HUNDRED_AND_FIFTY_NINETH -> "One-hundred-and-fifty-nineth"
            ONE_HUNDRED_AND_FIFTY_SECOND -> "One-hundred-and-fifty-second"
            ONE_HUNDRED_AND_FIFTY_SEVENTH -> "One-hundred-and-fifty-seventh"
            ONE_HUNDRED_AND_FIFTY_SIXTH -> "One-hundred-and-fifty-sixth"
            ONE_HUNDRED_AND_FIFTY_THIRD -> "One-hundred-and-fifty-third"
            ONE_HUNDRED_AND_FIRST -> "One-hundred-and-first"
            ONE_HUNDRED_AND_FOURTEENTH -> "One-hundred-and-fourteenth"
            ONE_HUNDRED_AND_FOURTH -> "One-hundred-and-fourth"
            ONE_HUNDRED_AND_FOURTY_EIGHTH -> "One-hundred-and-fourty-eighth"
            ONE_HUNDRED_AND_FOURTY_FIFTH -> "One-hundred-and-fourty-fifth"
            ONE_HUNDRED_AND_FOURTY_FIRST -> "One-hundred-and-fourty-first"
            ONE_HUNDRED_AND_FOURTY_FOURTH -> "One-hundred-and-fourty-fourth"
            ONE_HUNDRED_AND_FOURTY_NINETH -> "One-hundred-and-fourty-nineth"
            ONE_HUNDRED_AND_FOURTY_SECOND -> "One-hundred-and-fourty-second"
            ONE_HUNDRED_AND_FOURTY_SEVENTH -> "One-hundred-and-fourty-seventh"
            ONE_HUNDRED_AND_FOURTY_SIXTH -> "One-hundred-and-fourty-sixth"
            ONE_HUNDRED_AND_FOURTY_THIRD -> "One-hundred-and-fourty-third"
            ONE_HUNDRED_AND_NINETEENTH -> "One-hundred-and-nineteenth"
            ONE_HUNDRED_AND_NINETH -> "One-hundred-and-nineth"
            ONE_HUNDRED_AND_NINETY_EIGHTH -> "One-hundred-and-ninety-eighth"
            ONE_HUNDRED_AND_NINETY_FIFTH -> "One-hundred-and-ninety-fifth"
            ONE_HUNDRED_AND_NINETY_FIRST -> "One-hundred-and-ninety-first"
            ONE_HUNDRED_AND_NINETY_FOURTH -> "One-hundred-and-ninety-fourth"
            ONE_HUNDRED_AND_NINETY_NINETH -> "One-hundred-and-ninety-nineth"
            ONE_HUNDRED_AND_NINETY_SECOND -> "One-hundred-and-ninety-second"
            ONE_HUNDRED_AND_NINETY_SEVENTH -> "One-hundred-and-ninety-seventh"
            ONE_HUNDRED_AND_NINETY_SIXTH -> "One-hundred-and-ninety-sixth"
            ONE_HUNDRED_AND_NINETY_THIRD -> "One-hundred-and-ninety-third"
            ONE_HUNDRED_AND_SECOND -> "One-hundred-and-second"
            ONE_HUNDRED_AND_SEVENTEENTH -> "One-hundred-and-seventeenth"
            ONE_HUNDRED_AND_SEVENTH -> "One-hundred-and-seventh"
            ONE_HUNDRED_AND_SEVENTY_EIGHTH -> "One-hundred-and-seventy-eighth"
            ONE_HUNDRED_AND_SEVENTY_FIFTH -> "One-hundred-and-seventy-fifth"
            ONE_HUNDRED_AND_SEVENTY_FIRST -> "One-hundred-and-seventy-first"
            ONE_HUNDRED_AND_SEVENTY_FOURTH -> "One-hundred-and-seventy-fourth"
            ONE_HUNDRED_AND_SEVENTY_NINETH -> "One-hundred-and-seventy-nineth"
            ONE_HUNDRED_AND_SEVENTY_SECOND -> "One-hundred-and-seventy-second"
            ONE_HUNDRED_AND_SEVENTY_SEVENTH -> "One-hundred-and-seventy-seventh"
            ONE_HUNDRED_AND_SEVENTY_SIXTH -> "One-hundred-and-seventy-sixth"
            ONE_HUNDRED_AND_SEVENTY_THIRD -> "One-hundred-and-seventy-third"
            ONE_HUNDRED_AND_SIXTEENTH -> "One-hundred-and-sixteenth"
            ONE_HUNDRED_AND_SIXTH -> "One-hundred-and-sixth"
            ONE_HUNDRED_AND_SIXTY_EIGHTH -> "One-hundred-and-sixty-eighth"
            ONE_HUNDRED_AND_SIXTY_FIFTH -> "One-hundred-and-sixty-fifth"
            ONE_HUNDRED_AND_SIXTY_FIRST -> "One-hundred-and-sixty-first"
            ONE_HUNDRED_AND_SIXTY_FOURTH -> "One-hundred-and-sixty-fourth"
            ONE_HUNDRED_AND_SIXTY_NINETH -> "One-hundred-and-sixty-nineth"
            ONE_HUNDRED_AND_SIXTY_SECOND -> "One-hundred-and-sixty-second"
            ONE_HUNDRED_AND_SIXTY_SEVENTH -> "One-hundred-and-sixty-seventh"
            ONE_HUNDRED_AND_SIXTY_SIXTH -> "One-hundred-and-sixty-sixth"
            ONE_HUNDRED_AND_SIXTY_THIRD -> "One-hundred-and-sixty-third"
            ONE_HUNDRED_AND_TENTH -> "One-hundred-and-tenth"
            ONE_HUNDRED_AND_THIRD -> "One-hundred-and-third"
            ONE_HUNDRED_AND_THIRTEENTH -> "One-hundred-and-thirteenth"
            ONE_HUNDRED_AND_THIRTY_EIGHTH -> "One-hundred-and-thirty-eighth"
            ONE_HUNDRED_AND_THIRTY_FIFTH -> "One-hundred-and-thirty-fifth"
            ONE_HUNDRED_AND_THIRTY_FIRST -> "One-hundred-and-thirty-first"
            ONE_HUNDRED_AND_THIRTY_FOURTH -> "One-hundred-and-thirty-fourth"
            ONE_HUNDRED_AND_THIRTY_NINETH -> "One-hundred-and-thirty-nineth"
            ONE_HUNDRED_AND_THIRTY_SECOND -> "One-hundred-and-thirty-second"
            ONE_HUNDRED_AND_THIRTY_SEVENTH -> "One-hundred-and-thirty-seventh"
            ONE_HUNDRED_AND_THIRTY_SIXTH -> "One-hundred-and-thirty-sixth"
            ONE_HUNDRED_AND_THIRTY_THIRD -> "One-hundred-and-thirty-third"
            ONE_HUNDRED_AND_TWELVETH -> "One-hundred-and-twelveth"
            ONE_HUNDRED_AND_TWENTY_EIGHTH -> "One-hundred-and-twenty-eighth"
            ONE_HUNDRED_AND_TWENTY_FIFTH -> "One-hundred-and-twenty-fifth"
            ONE_HUNDRED_AND_TWENTY_FIRST -> "One-hundred-and-twenty-first"
            ONE_HUNDRED_AND_TWENTY_FOURTH -> "One-hundred-and-twenty-fourth"
            ONE_HUNDRED_AND_TWENTY_NINETH -> "One-hundred-and-twenty-nineth"
            ONE_HUNDRED_AND_TWENTY_SECOND -> "One-hundred-and-twenty-second"
            ONE_HUNDRED_AND_TWENTY_SEVENTH -> "One-hundred-and-twenty-seventh"
            ONE_HUNDRED_AND_TWENTY_SIXTH -> "One-hundred-and-twenty-sixth"
            ONE_HUNDRED_AND_TWENTY_THIRD -> "One-hundred-and-twenty-third"
            ONE_HUNDRED_EIGHTIETH -> "One-hundred-eightieth"
            ONE_HUNDRED_FIFTIETH -> "One-hundred-fiftieth"
            ONE_HUNDRED_FORTIETH -> "One-hundred-fortieth"
            ONE_HUNDRED_NINETIETH -> "One-hundred-ninetieth"
            ONE_HUNDRED_SEVENTIETH -> "One-hundred-seventieth"
            ONE_HUNDRED_SIXTIETH -> "One-hundred-sixtieth"
            ONE_HUNDRED_THIRTIETH -> "One-hundred-thirtieth"
            ONE_HUNDRED_TWENTIETH -> "One-hundred-twentieth"
            ONE_HUNDREDTH -> "One-hundredth"
            OUTSIDE -> "Outside"
            RIGHT -> "Right"
            SECOND -> "Second"
            SEVENTEENTH -> "Seventeenth"
            SEVENTH -> "Seventh"
            SEVENTIETH -> "Seventieth"
            SEVENTY_EIGHTH -> "Seventy-eighth"
            SEVENTY_FIFTH -> "Seventy-fifth"
            SEVENTY_FIRST -> "Seventy-first"
            SEVENTY_FOURTH -> "Seventy-fourth"
            SEVENTY_NINETH -> "Seventy-nineth"
            SEVENTY_SECOND -> "Seventy-second"
            SEVENTY_SEVENTH -> "Seventy-seventh"
            SEVENTY_SIXTH -> "Seventy-sixth"
            SEVENTY_THIRD -> "Seventy-third"
            SIXTEENTH -> "Sixteenth"
            SIXTH -> "Sixth"
            SIXTIETH -> "Sixtieth"
            SIXTY_EIGHTH -> "Sixty-eighth"
            SIXTY_FIFTH -> "Sixty-fifth"
            SIXTY_FIRST -> "Sixty-first"
            SIXTY_FOURTH -> "Sixty-fourth"
            SIXTY_NINETH -> "Sixty-nineth"
            SIXTY_SECOND -> "Sixty-second"
            SIXTY_SEVENTH -> "Sixty-seventh"
            SIXTY_SIXTH -> "Sixty-sixth"
            SIXTY_THIRD -> "Sixty-third"
            SUPPLEMENTARY -> "Supplementary"
            TENTH -> "Tenth"
            THIRD -> "Third"
            THIRTEENTH -> "Thirteenth"
            THIRTIETH -> "Thirtieth"
            THIRTY_EIGHTH -> "Thirty-eighth"
            THIRTY_FIFTH -> "Thirty-fifth"
            THIRTY_FIRST -> "Thirty-first"
            THIRTY_FOURTH -> "Thirty-fourth"
            THIRTY_NINETH -> "Thirty-nineth"
            THIRTY_SECOND -> "Thirty-second"
            THIRTY_SEVENTH -> "Thirty-seventh"
            THIRTY_SIXTH -> "Thirty-sixth"
            THIRTY_THIRD -> "Thirty-third"
            TOP -> "Top"
            TWELVETH -> "Twelveth"
            TWENTIETH -> "Twentieth"
            TWENTY_EIGHTH -> "Twenty-eighth"
            TWENTY_FIFTH -> "Twenty-fifth"
            TWENTY_FIRST -> "Twenty-first"
            TWENTY_FOURTH -> "Twenty-fourth"
            TWENTY_NINETH -> "Twenty-nineth"
            TWENTY_SECOND -> "Twenty-second"
            TWENTY_SEVENTH -> "Twenty-seventh"
            TWENTY_SIXTH -> "Twenty-sixth"
            TWENTY_THIRD -> "Twenty-third"
            TWO_HUNDRED_AND_EIGHTEENTH -> "Two-hundred-and-eighteenth"
            TWO_HUNDRED_AND_EIGHTH -> "Two-hundred-and-eighth"
            TWO_HUNDRED_AND_ELEVENTH -> "Two-hundred-and-eleventh"
            TWO_HUNDRED_AND_FIFTEENTH -> "Two-hundred-and-fifteenth"
            TWO_HUNDRED_AND_FIFTH -> "Two-hundred-and-fifth"
            TWO_HUNDRED_AND_FIFTY_FIFTH -> "Two-hundred-and-fifty-fifth"
            TWO_HUNDRED_AND_FIFTY_FIRST -> "Two-hundred-and-fifty-first"
            TWO_HUNDRED_AND_FIFTY_FOURTH -> "Two-hundred-and-fifty-fourth"
            TWO_HUNDRED_AND_FIFTY_SECOND -> "Two-hundred-and-fifty-second"
            TWO_HUNDRED_AND_FIFTY_THIRD -> "Two-hundred-and-fifty-third"
            TWO_HUNDRED_AND_FIRST -> "Two-hundred-and-first"
            TWO_HUNDRED_AND_FOURTEENTH -> "Two-hundred-and-fourteenth"
            TWO_HUNDRED_AND_FOURTH -> "Two-hundred-and-fourth"
            TWO_HUNDRED_AND_FOURTY_EIGHTH -> "Two-hundred-and-fourty-eighth"
            TWO_HUNDRED_AND_FOURTY_FIFTH -> "Two-hundred-and-fourty-fifth"
            TWO_HUNDRED_AND_FOURTY_FIRST -> "Two-hundred-and-fourty-first"
            TWO_HUNDRED_AND_FOURTY_FOURTH -> "Two-hundred-and-fourty-fourth"
            TWO_HUNDRED_AND_FOURTY_NINETH -> "Two-hundred-and-fourty-nineth"
            TWO_HUNDRED_AND_FOURTY_SECOND -> "Two-hundred-and-fourty-second"
            TWO_HUNDRED_AND_FOURTY_SEVENTH -> "Two-hundred-and-fourty-seventh"
            TWO_HUNDRED_AND_FOURTY_SIXTH -> "Two-hundred-and-fourty-sixth"
            TWO_HUNDRED_AND_FOURTY_THIRD -> "Two-hundred-and-fourty-third"
            TWO_HUNDRED_AND_NINETEENTH -> "Two-hundred-and-nineteenth"
            TWO_HUNDRED_AND_NINETH -> "Two-hundred-and-nineth"
            TWO_HUNDRED_AND_SECOND -> "Two-hundred-and-second"
            TWO_HUNDRED_AND_SEVENTEENTH -> "Two-hundred-and-seventeenth"
            TWO_HUNDRED_AND_SEVENTH -> "Two-hundred-and-seventh"
            TWO_HUNDRED_AND_SIXTEENTH -> "Two-hundred-and-sixteenth"
            TWO_HUNDRED_AND_SIXTH -> "Two-hundred-and-sixth"
            TWO_HUNDRED_AND_TENTH -> "Two-hundred-and-tenth"
            TWO_HUNDRED_AND_THIRD -> "Two-hundred-and-third"
            TWO_HUNDRED_AND_THIRTEENTH -> "Two-hundred-and-thirteenth"
            TWO_HUNDRED_AND_THIRTY_EIGHTH -> "Two-hundred-and-thirty-eighth"
            TWO_HUNDRED_AND_THIRTY_FIFTH -> "Two-hundred-and-thirty-fifth"
            TWO_HUNDRED_AND_THIRTY_FIRST -> "Two-hundred-and-thirty-first"
            TWO_HUNDRED_AND_THIRTY_FOURTH -> "Two-hundred-and-thirty-fourth"
            TWO_HUNDRED_AND_THIRTY_NINETH -> "Two-hundred-and-thirty-nineth"
            TWO_HUNDRED_AND_THIRTY_SECOND -> "Two-hundred-and-thirty-second"
            TWO_HUNDRED_AND_THIRTY_SEVENTH -> "Two-hundred-and-thirty-seventh"
            TWO_HUNDRED_AND_THIRTY_SIXTH -> "Two-hundred-and-thirty-sixth"
            TWO_HUNDRED_AND_THIRTY_THIRD -> "Two-hundred-and-thirty-third"
            TWO_HUNDRED_AND_TWELVETH -> "Two-hundred-and-twelveth"
            TWO_HUNDRED_AND_TWENTY_EIGHTH -> "Two-hundred-and-twenty-eighth"
            TWO_HUNDRED_AND_TWENTY_FIFTH -> "Two-hundred-and-twenty-fifth"
            TWO_HUNDRED_AND_TWENTY_FIRST -> "Two-hundred-and-twenty-first"
            TWO_HUNDRED_AND_TWENTY_FOURTH -> "Two-hundred-and-twenty-fourth"
            TWO_HUNDRED_AND_TWENTY_NINETH -> "Two-hundred-and-twenty-nineth"
            TWO_HUNDRED_AND_TWENTY_SECOND -> "Two-hundred-and-twenty-second"
            TWO_HUNDRED_AND_TWENTY_SEVENTH -> "Two-hundred-and-twenty-seventh"
            TWO_HUNDRED_AND_TWENTY_SIXTH -> "Two-hundred-and-twenty-sixth"
            TWO_HUNDRED_AND_TWENTY_THIRD -> "Two-hundred-and-twenty-third"
            TWO_HUNDRED_FIFTIETH -> "Two-hundred-fiftieth"
            TWO_HUNDRED_FORTIETH -> "Two-hundred-fortieth"
            TWO_HUNDRED_THIRTIETH -> "Two-hundred-thirtieth"
            TWO_HUNDRED_TWENTIETH -> "Two-hundred-twentieth"
            TWO_HUNDREDTH -> "Two-hundredth"
            UNKNOWN -> "Unknown"
            UPPER -> "Upper"
        }
    }
}