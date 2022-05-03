package no.nordicsemi.kotlin.mesh.core.model

/**
 * Locations defined by bluetooth SIG.
 * Refer: https://www.bluetooth.com/specifications/assigned-numbers/gatt-namespace-descriptors
 *
 * @property value 16-bit unsigned location value.
 */
sealed class Location(val value: UShort) {
    internal companion object {

        /**
         * Returns the Location for a given location value.
         *
         * @param value 16-bit value of location.
         * @return Returns the Location type.
         */
        fun from(value: UShort) = when (value.toInt()) {
            0x0108u.toInt() -> Auxiliary
            0x0101u.toInt() -> Back
            0x0107u.toInt() -> Backup
            0x0103u.toInt() -> Bottom
            0x0012u.toInt() -> Eighteenth
            0x0008u.toInt() -> Eighth
            0x0050u.toInt() -> Eightieth
            0x0058u.toInt() -> EightyEighth
            0x0055u.toInt() -> EightyFifth
            0x0051u.toInt() -> EightyFirst
            0x0054u.toInt() -> EightyFourth
            0x0059u.toInt() -> EightyNineth
            0x0052u.toInt() -> EightySecond
            0x0057u.toInt() -> EightySeventh
            0x0056u.toInt() -> EightySixth
            0x0053u.toInt() -> EightyThird
            0x000bu.toInt() -> Eleventh
            0x0110u.toInt() -> External
            0x000fu.toInt() -> Fifteenth
            0x0005u.toInt() -> Fifth
            0x0032u.toInt() -> Fiftieth
            0x003au.toInt() -> FiftyEighth
            0x0037u.toInt() -> FiftyFifth
            0x0033u.toInt() -> FiftyFirst
            0x0036u.toInt() -> FiftyFourth
            0x003bu.toInt() -> FiftyNineth
            0x0034u.toInt() -> FiftySecond
            0x0039u.toInt() -> FiftySeventh
            0x0038u.toInt() -> FiftySixth
            0x0035u.toInt() -> FiftyThird
            0x0001u.toInt() -> First
            0x010Au.toInt() -> Flash
            0x0028u.toInt() -> Fortieth
            0x000eu.toInt() -> Fourteenth
            0x0004u.toInt() -> Fourth
            0x0030u.toInt() -> FourtyEighth
            0x002du.toInt() -> FourtyFifth
            0x0029u.toInt() -> FourtyFirst
            0x002cu.toInt() -> FourtyFourth
            0x0031u.toInt() -> FourtyNineth
            0x002au.toInt() -> FourtySecond
            0x002fu.toInt() -> FourtySeventh
            0x002eu.toInt() -> FourtySixth
            0x002bu.toInt() -> FourtyThird
            0x0100u.toInt() -> Front
            0x010Bu.toInt() -> Inside
            0x010Fu.toInt() -> Internal
            0x010Du.toInt() -> Left
            0x0105u.toInt() -> Lower
            0x0106u.toInt() -> Main
            0x0013u.toInt() -> Nineteenth
            0x0009u.toInt() -> Nineth
            0x005au.toInt() -> Ninetieth
            0x0062u.toInt() -> NinetyEighth
            0x005fu.toInt() -> NinetyFifth
            0x005bu.toInt() -> NinetyFirst
            0x005eu.toInt() -> NinetyFourth
            0x0063u.toInt() -> NinetyNineth
            0x005cu.toInt() -> NinetySecond
            0x0061u.toInt() -> NinetySeventh
            0x0060u.toInt() -> NinetySixth
            0x005du.toInt() -> NinetyThird
            0x0076u.toInt() -> OneHundredAndEighteenth
            0x006cu.toInt() -> OneHundredAndEighth
            0x00bcu.toInt() -> OneHundredAndEightyEighth
            0x00b9u.toInt() -> OneHundredAndEightyFifth
            0x00b5u.toInt() -> OneHundredAndEightyFirst
            0x00b8u.toInt() -> OneHundredAndEightyFourth
            0x00bdu.toInt() -> OneHundredAndEightyNineth
            0x00b6u.toInt() -> OneHundredAndEightySecond
            0x00bbu.toInt() -> OneHundredAndEightySeventh
            0x00bau.toInt() -> OneHundredAndEightySixth
            0x00b7u.toInt() -> OneHundredAndEightyThird
            0x006fu.toInt() -> OneHundredAndEleventh
            0x0073u.toInt() -> OneHundredAndFifteenth
            0x0069u.toInt() -> OneHundredAndFifth
            0x009eu.toInt() -> OneHundredAndFiftyEighth
            0x009bu.toInt() -> OneHundredAndFiftyFifth
            0x0097u.toInt() -> OneHundredAndFiftyFirst
            0x009au.toInt() -> OneHundredAndFiftyFourth
            0x009fu.toInt() -> OneHundredAndFiftyNineth
            0x0098u.toInt() -> OneHundredAndFiftySecond
            0x009du.toInt() -> OneHundredAndFiftySeventh
            0x009cu.toInt() -> OneHundredAndFiftySixth
            0x0099u.toInt() -> OneHundredAndFiftyThird
            0x0065u.toInt() -> OneHundredAndFirst
            0x0072u.toInt() -> OneHundredAndFourteenth
            0x0068u.toInt() -> OneHundredAndFourth
            0x0094u.toInt() -> OneHundredAndFourtyEighth
            0x0091u.toInt() -> OneHundredAndFourtyFifth
            0x008du.toInt() -> OneHundredAndFourtyFirst
            0x0090u.toInt() -> OneHundredAndFourtyFourth
            0x0095u.toInt() -> OneHundredAndFourtyNineth
            0x008eu.toInt() -> OneHundredAndFourtySecond
            0x0093u.toInt() -> OneHundredAndFourtySeventh
            0x0092u.toInt() -> OneHundredAndFourtySixth
            0x008fu.toInt() -> OneHundredAndFourtyThird
            0x0077u.toInt() -> OneHundredAndNineteenth
            0x006du.toInt() -> OneHundredAndNineth
            0x00c6u.toInt() -> OneHundredAndNinetyEighth
            0x00c3u.toInt() -> OneHundredAndNinetyFifth
            0x00bfu.toInt() -> OneHundredAndNinetyFirst
            0x00c2u.toInt() -> OneHundredAndNinetyFourth
            0x00c7u.toInt() -> OneHundredAndNinetyNineth
            0x00c0u.toInt() -> OneHundredAndNinetySecond
            0x00c5u.toInt() -> OneHundredAndNinetySeventh
            0x00c4u.toInt() -> OneHundredAndNinetySixth
            0x00c1u.toInt() -> OneHundredAndNinetyThird
            0x0066u.toInt() -> OneHundredAndSecond
            0x0075u.toInt() -> OneHundredAndSeventeenth
            0x006bu.toInt() -> OneHundredAndSeventh
            0x00b2u.toInt() -> OneHundredAndSeventyEighth
            0x00afu.toInt() -> OneHundredAndSeventyFifth
            0x00abu.toInt() -> OneHundredAndSeventyFirst
            0x00aeu.toInt() -> OneHundredAndSeventyFourth
            0x00b3u.toInt() -> OneHundredAndSeventyNineth
            0x00acu.toInt() -> OneHundredAndSeventySecond
            0x00b1u.toInt() -> OneHundredAndSeventySeventh
            0x00b0u.toInt() -> OneHundredAndSeventySixth
            0x00adu.toInt() -> OneHundredAndSeventyThird
            0x0074u.toInt() -> OneHundredAndSixteenth
            0x006au.toInt() -> OneHundredAndSixth
            0x00a8u.toInt() -> OneHundredAndSixtyEighth
            0x00a5u.toInt() -> OneHundredAndSixtyFifth
            0x00a1u.toInt() -> OneHundredAndSixtyFirst
            0x00a4u.toInt() -> OneHundredAndSixtyFourth
            0x00a9u.toInt() -> OneHundredAndSixtyNineth
            0x00a2u.toInt() -> OneHundredAndSixtySecond
            0x00a7u.toInt() -> OneHundredAndSixtySeventh
            0x00a6u.toInt() -> OneHundredAndSixtySixth
            0x00a3u.toInt() -> OneHundredAndSixtyThird
            0x006eu.toInt() -> OneHundredAndTenth
            0x0067u.toInt() -> OneHundredAndThird
            0x0071u.toInt() -> OneHundredAndThirteenth
            0x008au.toInt() -> OneHundredAndThirtyEighth
            0x0087u.toInt() -> OneHundredAndThirtyFifth
            0x0083u.toInt() -> OneHundredAndThirtyFirst
            0x0086u.toInt() -> OneHundredAndThirtyFourth
            0x008bu.toInt() -> OneHundredAndThirtyNineth
            0x0084u.toInt() -> OneHundredAndThirtySecond
            0x0089u.toInt() -> OneHundredAndThirtySeventh
            0x0088u.toInt() -> OneHundredAndThirtySixth
            0x0085u.toInt() -> OneHundredAndThirtyThird
            0x0070u.toInt() -> OneHundredAndTwelveth
            0x0080u.toInt() -> OneHundredAndTwentyEighth
            0x007du.toInt() -> OneHundredAndTwentyFifth
            0x0079u.toInt() -> OneHundredAndTwentyFirst
            0x007cu.toInt() -> OneHundredAndTwentyFourth
            0x0081u.toInt() -> OneHundredAndTwentyNineth
            0x007au.toInt() -> OneHundredAndTwentySecond
            0x007fu.toInt() -> OneHundredAndTwentySeventh
            0x007eu.toInt() -> OneHundredAndTwentySixth
            0x007bu.toInt() -> OneHundredAndTwentyThird
            0x00b4u.toInt() -> OneHundredEightieth
            0x0096u.toInt() -> OneHundredFiftieth
            0x008cu.toInt() -> OneHundredFortieth
            0x00beu.toInt() -> OneHundredNinetieth
            0x00aau.toInt() -> OneHundredSeventieth
            0x00a0u.toInt() -> OneHundredSixtieth
            0x0082u.toInt() -> OneHundredThirtieth
            0x0078u.toInt() -> OneHundredTwentieth
            0x0064u.toInt() -> OneHundredth
            0x010Cu.toInt() -> Outside
            0x010Eu.toInt() -> Right
            0x0002u.toInt() -> Second
            0x0011u.toInt() -> Seventeenth
            0x0007u.toInt() -> Seventh
            0x0046u.toInt() -> Seventieth
            0x004eu.toInt() -> SeventyEighth
            0x004bu.toInt() -> SeventyFifth
            0x0047u.toInt() -> SeventyFirst
            0x004au.toInt() -> SeventyFourth
            0x004fu.toInt() -> SeventyNineth
            0x0048u.toInt() -> SeventySecond
            0x004du.toInt() -> SeventySeventh
            0x004cu.toInt() -> SeventySixth
            0x0049u.toInt() -> SeventyThird
            0x0010u.toInt() -> Sixteenth
            0x0006u.toInt() -> Sixth
            0x003cu.toInt() -> Sixtieth
            0x0044u.toInt() -> SixtyEighth
            0x0041u.toInt() -> SixtyFifth
            0x003du.toInt() -> SixtyFirst
            0x0040u.toInt() -> SixtyFourth
            0x0045u.toInt() -> SixtyNineth
            0x003eu.toInt() -> SixtySecond
            0x0043u.toInt() -> SixtySeventh
            0x0042u.toInt() -> SixtySixth
            0x003fu.toInt() -> SixtyThird
            0x0109u.toInt() -> Supplementary
            0x000au.toInt() -> Tenth
            0x0003u.toInt() -> Third
            0x000du.toInt() -> Thirteenth
            0x001eu.toInt() -> Thirtieth
            0x0026u.toInt() -> ThirtyEighth
            0x0023u.toInt() -> ThirtyFifth
            0x001fu.toInt() -> ThirtyFirst
            0x0022u.toInt() -> ThirtyFourth
            0x0027u.toInt() -> ThirtyNineth
            0x0020u.toInt() -> ThirtySecond
            0x0025u.toInt() -> ThirtySeventh
            0x0024u.toInt() -> ThirtySixth
            0x0021u.toInt() -> ThirtyThird
            0x0102u.toInt() -> Top
            0x000cu.toInt() -> Twelveth
            0x0014u.toInt() -> Twentieth
            0x001cu.toInt() -> TwentyEighth
            0x0019u.toInt() -> TwentyFifth
            0x0015u.toInt() -> TwentyFirst
            0x0018u.toInt() -> TwentyFourth
            0x001du.toInt() -> TwentyNineth
            0x0016u.toInt() -> TwentySecond
            0x001bu.toInt() -> TwentySeventh
            0x001au.toInt() -> TwentySixth
            0x0017u.toInt() -> TwentyThird
            0x00dau.toInt() -> TwoHundredAndEighteenth
            0x00d0u.toInt() -> TwoHundredAndEighth
            0x00d3u.toInt() -> TwoHundredAndEleventh
            0x00d7u.toInt() -> TwoHundredAndFifteenth
            0x00cdu.toInt() -> TwoHundredAndFifth
            0x00ffu.toInt() -> TwoHundredAndFiftyFifth
            0x00fbu.toInt() -> TwoHundredAndFiftyFirst
            0x00feu.toInt() -> TwoHundredAndFiftyFourth
            0x00fcu.toInt() -> TwoHundredAndFiftySecond
            0x00fdu.toInt() -> TwoHundredAndFiftyThird
            0x00c9u.toInt() -> TwoHundredAndFirst
            0x00d6u.toInt() -> TwoHundredAndFourteenth
            0x00ccu.toInt() -> TwoHundredAndFourth
            0x00f8u.toInt() -> TwoHundredAndFourtyEighth
            0x00f5u.toInt() -> TwoHundredAndFourtyFifth
            0x00f1u.toInt() -> TwoHundredAndFourtyFirst
            0x00f4u.toInt() -> TwoHundredAndFourtyFourth
            0x00f9u.toInt() -> TwoHundredAndFourtyNineth
            0x00f2u.toInt() -> TwoHundredAndFourtySecond
            0x00f7u.toInt() -> TwoHundredAndFourtySeventh
            0x00f6u.toInt() -> TwoHundredAndFourtySixth
            0x00f3u.toInt() -> TwoHundredAndFourtyThird
            0x00dbu.toInt() -> TwoHundredAndNineteenth
            0x00d1u.toInt() -> TwoHundredAndNineth
            0x00cau.toInt() -> TwoHundredAndSecond
            0x00d9u.toInt() -> TwoHundredAndSeventeenth
            0x00cfu.toInt() -> TwoHundredAndSeventh
            0x00d8u.toInt() -> TwoHundredAndSixteenth
            0x00ceu.toInt() -> TwoHundredAndSixth
            0x00d2u.toInt() -> TwoHundredAndTenth
            0x00cbu.toInt() -> TwoHundredAndThird
            0x00d5u.toInt() -> TwoHundredAndThirteenth
            0x00eeu.toInt() -> TwoHundredAndThirtyEighth
            0x00ebu.toInt() -> TwoHundredAndThirtyFifth
            0x00e7u.toInt() -> TwoHundredAndThirtyFirst
            0x00eau.toInt() -> TwoHundredAndThirtyFourth
            0x00efu.toInt() -> TwoHundredAndThirtyNineth
            0x00e8u.toInt() -> TwoHundredAndThirtySecond
            0x00edu.toInt() -> TwoHundredAndThirtySeventh
            0x00ecu.toInt() -> TwoHundredAndThirtySixth
            0x00e9u.toInt() -> TwoHundredAndThirtyThird
            0x00d4u.toInt() -> TwoHundredAndTwelveth
            0x00e4u.toInt() -> TwoHundredAndTwentyEighth
            0x00e1u.toInt() -> TwoHundredAndTwentyFifth
            0x00ddu.toInt() -> TwoHundredAndTwentyFirst
            0x00e0u.toInt() -> TwoHundredAndTwentyFourth
            0x00e5u.toInt() -> TwoHundredAndTwentyNineth
            0x00deu.toInt() -> TwoHundredAndTwentySecond
            0x00e3u.toInt() -> TwoHundredAndTwentySeventh
            0x00e2u.toInt() -> TwoHundredAndTwentySixth
            0x00dfu.toInt() -> TwoHundredAndTwentyThird
            0x00fau.toInt() -> TwoHundredFiftieth
            0x00f0u.toInt() -> TwoHundredFortieth
            0x00e6u.toInt() -> TwoHundredThirtieth
            0x00dcu.toInt() -> TwoHundredTwentieth
            0x00c8u.toInt() -> TwoHundredth
            0x0104u.toInt() -> Upper
            else -> Unknown
        }

        /**
         * Returns the description for a given location
         *
         * @param value Location.
         * @return Returns the human readable description of the location.
         */
        fun from(value: Location) = when (value) {
            is Auxiliary -> "Auxiliary"
            is Back -> "Back"
            is Backup -> "Backup"
            is Bottom -> "Bottom"
            is Eighteenth -> "Eighteenth"
            is Eighth -> "Eighth"
            is Eightieth -> "Eightieth"
            is EightyEighth -> "Eighty-eighth"
            is EightyFifth -> "Eighty-fifth"
            is EightyFirst -> "Eighty-first"
            is EightyFourth -> "Eighty-fourth"
            is EightyNineth -> "Eighty-nineth"
            is EightySecond -> "Eighty-second"
            is EightySeventh -> "Eighty-seventh"
            is EightySixth -> "Eighty-sixth"
            is EightyThird -> "Eighty-third"
            is Eleventh -> "Eleventh"
            is External -> "External"
            is Fifteenth -> "Fifteenth"
            is Fifth -> "Fifth"
            is Fiftieth -> "Fiftieth"
            is FiftyEighth -> "Fifty-eighth"
            is FiftyFifth -> "Fifty-fifth"
            is FiftyFirst -> "Fifty-first"
            is FiftyFourth -> "Fifty-fourth"
            is FiftyNineth -> "Fifty-nineth"
            is FiftySecond -> "Fifty-second"
            is FiftySeventh -> "Fifty-seventh"
            is FiftySixth -> "Fifty-sixth"
            is FiftyThird -> "Fifty-third"
            is First -> "First"
            is Flash -> "Flash"
            is Fortieth -> "Fortieth"
            is Fourteenth -> "Fourteenth"
            is Fourth -> "Fourth"
            is FourtyEighth -> "Fourty-eighth"
            is FourtyFifth -> "Fourty-fifth"
            is FourtyFirst -> "Fourty-first"
            is FourtyFourth -> "Fourty-fourth"
            is FourtyNineth -> "Fourty-nineth"
            is FourtySecond -> "Fourty-second"
            is FourtySeventh -> "Fourty-seventh"
            is FourtySixth -> "Fourty-sixth"
            is FourtyThird -> "Fourty-third"
            is Front -> "Front"
            is Inside -> "Inside"
            is Internal -> "Internal"
            is Left -> "Left"
            is Lower -> "Lower"
            is Main -> "Main"
            is Nineteenth -> "Nineteenth"
            is Nineth -> "Nineth"
            is Ninetieth -> "Ninetieth"
            is NinetyEighth -> "Ninety-eighth"
            is NinetyFifth -> "Ninety-fifth"
            is NinetyFirst -> "Ninety-first"
            is NinetyFourth -> "Ninety-fourth"
            is NinetyNineth -> "Ninety-nineth"
            is NinetySecond -> "Ninety-second"
            is NinetySeventh -> "Ninety-seventh"
            is NinetySixth -> "Ninety-sixth"
            is NinetyThird -> "Ninety-third"
            is OneHundredAndEighteenth -> "One-hundred-and-eighteenth"
            is OneHundredAndEighth -> "One-hundred-and-eighth"
            is OneHundredAndEightyEighth -> "One-hundred-and-eighty-eighth"
            is OneHundredAndEightyFifth -> "One-hundred-and-eighty-fifth"
            is OneHundredAndEightyFirst -> "One-hundred-and-eighty-first"
            is OneHundredAndEightyFourth -> "One-hundred-and-eighty-fourth"
            is OneHundredAndEightyNineth -> "One-hundred-and-eighty-nineth"
            is OneHundredAndEightySecond -> "One-hundred-and-eighty-second"
            is OneHundredAndEightySeventh -> "One-hundred-and-eighty-seventh"
            is OneHundredAndEightySixth -> "One-hundred-and-eighty-sixth"
            is OneHundredAndEightyThird -> "One-hundred-and-eighty-third"
            is OneHundredAndEleventh -> "One-hundred-and-eleventh"
            is OneHundredAndFifteenth -> "One-hundred-and-fifteenth"
            is OneHundredAndFifth -> "One-hundred-and-fifth"
            is OneHundredAndFiftyEighth -> "One-hundred-and-fifty-eighth"
            is OneHundredAndFiftyFifth -> "One-hundred-and-fifty-fifth"
            is OneHundredAndFiftyFirst -> "One-hundred-and-fifty-first"
            is OneHundredAndFiftyFourth -> "One-hundred-and-fifty-fourth"
            is OneHundredAndFiftyNineth -> "One-hundred-and-fifty-nineth"
            is OneHundredAndFiftySecond -> "One-hundred-and-fifty-second"
            is OneHundredAndFiftySeventh -> "One-hundred-and-fifty-seventh"
            is OneHundredAndFiftySixth -> "One-hundred-and-fifty-sixth"
            is OneHundredAndFiftyThird -> "One-hundred-and-fifty-third"
            is OneHundredAndFirst -> "One-hundred-and-first"
            is OneHundredAndFourteenth -> "One-hundred-and-fourteenth"
            is OneHundredAndFourth -> "One-hundred-and-fourth"
            is OneHundredAndFourtyEighth -> "One-hundred-and-fourty-eighth"
            is OneHundredAndFourtyFifth -> "One-hundred-and-fourty-fifth"
            is OneHundredAndFourtyFirst -> "One-hundred-and-fourty-first"
            is OneHundredAndFourtyFourth -> "One-hundred-and-fourty-fourth"
            is OneHundredAndFourtyNineth -> "One-hundred-and-fourty-nineth"
            is OneHundredAndFourtySecond -> "One-hundred-and-fourty-second"
            is OneHundredAndFourtySeventh -> "One-hundred-and-fourty-seventh"
            is OneHundredAndFourtySixth -> "One-hundred-and-fourty-sixth"
            is OneHundredAndFourtyThird -> "One-hundred-and-fourty-third"
            is OneHundredAndNineteenth -> "One-hundred-and-nineteenth"
            is OneHundredAndNineth -> "One-hundred-and-nineth"
            is OneHundredAndNinetyEighth -> "One-hundred-and-ninety-eighth"
            is OneHundredAndNinetyFifth -> "One-hundred-and-ninety-fifth"
            is OneHundredAndNinetyFirst -> "One-hundred-and-ninety-first"
            is OneHundredAndNinetyFourth -> "One-hundred-and-ninety-fourth"
            is OneHundredAndNinetyNineth -> "One-hundred-and-ninety-nineth"
            is OneHundredAndNinetySecond -> "One-hundred-and-ninety-second"
            is OneHundredAndNinetySeventh -> "One-hundred-and-ninety-seventh"
            is OneHundredAndNinetySixth -> "One-hundred-and-ninety-sixth"
            is OneHundredAndNinetyThird -> "One-hundred-and-ninety-third"
            is OneHundredAndSecond -> "One-hundred-and-second"
            is OneHundredAndSeventeenth -> "One-hundred-and-seventeenth"
            is OneHundredAndSeventh -> "One-hundred-and-seventh"
            is OneHundredAndSeventyEighth -> "One-hundred-and-seventy-eighth"
            is OneHundredAndSeventyFifth -> "One-hundred-and-seventy-fifth"
            is OneHundredAndSeventyFirst -> "One-hundred-and-seventy-first"
            is OneHundredAndSeventyFourth -> "One-hundred-and-seventy-fourth"
            is OneHundredAndSeventyNineth -> "One-hundred-and-seventy-nineth"
            is OneHundredAndSeventySecond -> "One-hundred-and-seventy-second"
            is OneHundredAndSeventySeventh -> "One-hundred-and-seventy-seventh"
            is OneHundredAndSeventySixth -> "One-hundred-and-seventy-sixth"
            is OneHundredAndSeventyThird -> "One-hundred-and-seventy-third"
            is OneHundredAndSixteenth -> "One-hundred-and-sixteenth"
            is OneHundredAndSixth -> "One-hundred-and-sixth"
            is OneHundredAndSixtyEighth -> "One-hundred-and-sixty-eighth"
            is OneHundredAndSixtyFifth -> "One-hundred-and-sixty-fifth"
            is OneHundredAndSixtyFirst -> "One-hundred-and-sixty-first"
            is OneHundredAndSixtyFourth -> "One-hundred-and-sixty-fourth"
            is OneHundredAndSixtyNineth -> "One-hundred-and-sixty-nineth"
            is OneHundredAndSixtySecond -> "One-hundred-and-sixty-second"
            is OneHundredAndSixtySeventh -> "One-hundred-and-sixty-seventh"
            is OneHundredAndSixtySixth -> "One-hundred-and-sixty-sixth"
            is OneHundredAndSixtyThird -> "One-hundred-and-sixty-third"
            is OneHundredAndTenth -> "One-hundred-and-tenth"
            is OneHundredAndThird -> "One-hundred-and-third"
            is OneHundredAndThirteenth -> "One-hundred-and-thirteenth"
            is OneHundredAndThirtyEighth -> "One-hundred-and-thirty-eighth"
            is OneHundredAndThirtyFifth -> "One-hundred-and-thirty-fifth"
            is OneHundredAndThirtyFirst -> "One-hundred-and-thirty-first"
            is OneHundredAndThirtyFourth -> "One-hundred-and-thirty-fourth"
            is OneHundredAndThirtyNineth -> "One-hundred-and-thirty-nineth"
            is OneHundredAndThirtySecond -> "One-hundred-and-thirty-second"
            is OneHundredAndThirtySeventh -> "One-hundred-and-thirty-seventh"
            is OneHundredAndThirtySixth -> "One-hundred-and-thirty-sixth"
            is OneHundredAndThirtyThird -> "One-hundred-and-thirty-third"
            is OneHundredAndTwelveth -> "One-hundred-and-twelveth"
            is OneHundredAndTwentyEighth -> "One-hundred-and-twenty-eighth"
            is OneHundredAndTwentyFifth -> "One-hundred-and-twenty-fifth"
            is OneHundredAndTwentyFirst -> "One-hundred-and-twenty-first"
            is OneHundredAndTwentyFourth -> "One-hundred-and-twenty-fourth"
            is OneHundredAndTwentyNineth -> "One-hundred-and-twenty-nineth"
            is OneHundredAndTwentySecond -> "One-hundred-and-twenty-second"
            is OneHundredAndTwentySeventh -> "One-hundred-and-twenty-seventh"
            is OneHundredAndTwentySixth -> "One-hundred-and-twenty-sixth"
            is OneHundredAndTwentyThird -> "One-hundred-and-twenty-third"
            is OneHundredEightieth -> "One-hundred-eightieth"
            is OneHundredFiftieth -> "One-hundred-fiftieth"
            is OneHundredFortieth -> "One-hundred-fortieth"
            is OneHundredNinetieth -> "One-hundred-ninetieth"
            is OneHundredSeventieth -> "One-hundred-seventieth"
            is OneHundredSixtieth -> "One-hundred-sixtieth"
            is OneHundredThirtieth -> "One-hundred-thirtieth"
            is OneHundredTwentieth -> "One-hundred-twentieth"
            is OneHundredth -> "One-hundredth"
            is Outside -> "Outside"
            is Right -> "Right"
            is Second -> "Second"
            is Seventeenth -> "Seventeenth"
            is Seventh -> "Seventh"
            is Seventieth -> "Seventieth"
            is SeventyEighth -> "Seventy-eighth"
            is SeventyFifth -> "Seventy-fifth"
            is SeventyFirst -> "Seventy-first"
            is SeventyFourth -> "Seventy-fourth"
            is SeventyNineth -> "Seventy-nineth"
            is SeventySecond -> "Seventy-second"
            is SeventySeventh -> "Seventy-seventh"
            is SeventySixth -> "Seventy-sixth"
            is SeventyThird -> "Seventy-third"
            is Sixteenth -> "Sixteenth"
            is Sixth -> "Sixth"
            is Sixtieth -> "Sixtieth"
            is SixtyEighth -> "Sixty-eighth"
            is SixtyFifth -> "Sixty-fifth"
            is SixtyFirst -> "Sixty-first"
            is SixtyFourth -> "Sixty-fourth"
            is SixtyNineth -> "Sixty-nineth"
            is SixtySecond -> "Sixty-second"
            is SixtySeventh -> "Sixty-seventh"
            is SixtySixth -> "Sixty-sixth"
            is SixtyThird -> "Sixty-third"
            is Supplementary -> "Supplementary"
            is Tenth -> "Tenth"
            is Third -> "Third"
            is Thirteenth -> "Thirteenth"
            is Thirtieth -> "Thirtieth"
            is ThirtyEighth -> "Thirty-eighth"
            is ThirtyFifth -> "Thirty-fifth"
            is ThirtyFirst -> "Thirty-first"
            is ThirtyFourth -> "Thirty-fourth"
            is ThirtyNineth -> "Thirty-nineth"
            is ThirtySecond -> "Thirty-second"
            is ThirtySeventh -> "Thirty-seventh"
            is ThirtySixth -> "Thirty-sixth"
            is ThirtyThird -> "Thirty-third"
            is Top -> "Top"
            is Twelveth -> "Twelveth"
            is Twentieth -> "Twentieth"
            is TwentyEighth -> "Twenty-eighth"
            is TwentyFifth -> "Twenty-fifth"
            is TwentyFirst -> "Twenty-first"
            is TwentyFourth -> "Twenty-fourth"
            is TwentyNineth -> "Twenty-nineth"
            is TwentySecond -> "Twenty-second"
            is TwentySeventh -> "Twenty-seventh"
            is TwentySixth -> "Twenty-sixth"
            is TwentyThird -> "Twenty-third"
            is TwoHundredAndEighteenth -> "Two-hundred-and-eighteenth"
            is TwoHundredAndEighth -> "Two-hundred-and-eighth"
            is TwoHundredAndEleventh -> "Two-hundred-and-eleventh"
            is TwoHundredAndFifteenth -> "Two-hundred-and-fifteenth"
            is TwoHundredAndFifth -> "Two-hundred-and-fifth"
            is TwoHundredAndFiftyFifth -> "Two-hundred-and-fifty-fifth"
            is TwoHundredAndFiftyFirst -> "Two-hundred-and-fifty-first"
            is TwoHundredAndFiftyFourth -> "Two-hundred-and-fifty-fourth"
            is TwoHundredAndFiftySecond -> "Two-hundred-and-fifty-second"
            is TwoHundredAndFiftyThird -> "Two-hundred-and-fifty-third"
            is TwoHundredAndFirst -> "Two-hundred-and-first"
            is TwoHundredAndFourteenth -> "Two-hundred-and-fourteenth"
            is TwoHundredAndFourth -> "Two-hundred-and-fourth"
            is TwoHundredAndFourtyEighth -> "Two-hundred-and-fourty-eighth"
            is TwoHundredAndFourtyFifth -> "Two-hundred-and-fourty-fifth"
            is TwoHundredAndFourtyFirst -> "Two-hundred-and-fourty-first"
            is TwoHundredAndFourtyFourth -> "Two-hundred-and-fourty-fourth"
            is TwoHundredAndFourtyNineth -> "Two-hundred-and-fourty-nineth"
            is TwoHundredAndFourtySecond -> "Two-hundred-and-fourty-second"
            is TwoHundredAndFourtySeventh -> "Two-hundred-and-fourty-seventh"
            is TwoHundredAndFourtySixth -> "Two-hundred-and-fourty-sixth"
            is TwoHundredAndFourtyThird -> "Two-hundred-and-fourty-third"
            is TwoHundredAndNineteenth -> "Two-hundred-and-nineteenth"
            is TwoHundredAndNineth -> "Two-hundred-and-nineth"
            is TwoHundredAndSecond -> "Two-hundred-and-second"
            is TwoHundredAndSeventeenth -> "Two-hundred-and-seventeenth"
            is TwoHundredAndSeventh -> "Two-hundred-and-seventh"
            is TwoHundredAndSixteenth -> "Two-hundred-and-sixteenth"
            is TwoHundredAndSixth -> "Two-hundred-and-sixth"
            is TwoHundredAndTenth -> "Two-hundred-and-tenth"
            is TwoHundredAndThird -> "Two-hundred-and-third"
            is TwoHundredAndThirteenth -> "Two-hundred-and-thirteenth"
            is TwoHundredAndThirtyEighth -> "Two-hundred-and-thirty-eighth"
            is TwoHundredAndThirtyFifth -> "Two-hundred-and-thirty-fifth"
            is TwoHundredAndThirtyFirst -> "Two-hundred-and-thirty-first"
            is TwoHundredAndThirtyFourth -> "Two-hundred-and-thirty-fourth"
            is TwoHundredAndThirtyNineth -> "Two-hundred-and-thirty-nineth"
            is TwoHundredAndThirtySecond -> "Two-hundred-and-thirty-second"
            is TwoHundredAndThirtySeventh -> "Two-hundred-and-thirty-seventh"
            is TwoHundredAndThirtySixth -> "Two-hundred-and-thirty-sixth"
            is TwoHundredAndThirtyThird -> "Two-hundred-and-thirty-third"
            is TwoHundredAndTwelveth -> "Two-hundred-and-twelveth"
            is TwoHundredAndTwentyEighth -> "Two-hundred-and-twenty-eighth"
            is TwoHundredAndTwentyFifth -> "Two-hundred-and-twenty-fifth"
            is TwoHundredAndTwentyFirst -> "Two-hundred-and-twenty-first"
            is TwoHundredAndTwentyFourth -> "Two-hundred-and-twenty-fourth"
            is TwoHundredAndTwentyNineth -> "Two-hundred-and-twenty-nineth"
            is TwoHundredAndTwentySecond -> "Two-hundred-and-twenty-second"
            is TwoHundredAndTwentySeventh -> "Two-hundred-and-twenty-seventh"
            is TwoHundredAndTwentySixth -> "Two-hundred-and-twenty-sixth"
            is TwoHundredAndTwentyThird -> "Two-hundred-and-twenty-third"
            is TwoHundredFiftieth -> "Two-hundred-fiftieth"
            is TwoHundredFortieth -> "Two-hundred-fortieth"
            is TwoHundredThirtieth -> "Two-hundred-thirtieth"
            is TwoHundredTwentieth -> "Two-hundred-twentieth"
            is TwoHundredth -> "Two-hundredth"
            is Unknown -> "Unknown"
            is Upper -> "Upper"
        }
    }
}

object Auxiliary : Location(0x0108u)
object Back : Location(0x0101u)
object Backup : Location(0x0107u)
object Bottom : Location(0x0103u)
object Eighteenth : Location(0x0012u)
object Eighth : Location(0x0008u)
object Eightieth : Location(0x0050u)
object EightyEighth : Location(0x0058u)
object EightyFifth : Location(0x0055u)
object EightyFirst : Location(0x0051u)
object EightyFourth : Location(0x0054u)
object EightyNineth : Location(0x0059u)
object EightySecond : Location(0x0052u)
object EightySeventh : Location(0x0057u)
object EightySixth : Location(0x0056u)
object EightyThird : Location(0x0053u)
object Eleventh : Location(0x000bu)
object External : Location(0x0110u)
object Fifteenth : Location(0x000fu)
object Fifth : Location(0x0005u)
object Fiftieth : Location(0x0032u)
object FiftyEighth : Location(0x003au)
object FiftyFifth : Location(0x0037u)
object FiftyFirst : Location(0x0033u)
object FiftyFourth : Location(0x0036u)
object FiftyNineth : Location(0x003bu)
object FiftySecond : Location(0x0034u)
object FiftySeventh : Location(0x0039u)
object FiftySixth : Location(0x0038u)
object FiftyThird : Location(0x0035u)
object First : Location(0x0001u)
object Flash : Location(0x010Au)
object Fortieth : Location(0x0028u)
object Fourteenth : Location(0x000eu)
object Fourth : Location(0x0004u)
object FourtyEighth : Location(0x0030u)
object FourtyFifth : Location(0x002du)
object FourtyFirst : Location(0x0029u)
object FourtyFourth : Location(0x002cu)
object FourtyNineth : Location(0x0031u)
object FourtySecond : Location(0x002au)
object FourtySeventh : Location(0x002fu)
object FourtySixth : Location(0x002eu)
object FourtyThird : Location(0x002bu)
object Front : Location(0x0100u)
object Inside : Location(0x010Bu)
object Internal : Location(0x010Fu)
object Left : Location(0x010Du)
object Lower : Location(0x0105u)
object Main : Location(0x0106u)
object Nineteenth : Location(0x0013u)
object Nineth : Location(0x0009u)
object Ninetieth : Location(0x005au)
object NinetyEighth : Location(0x0062u)
object NinetyFifth : Location(0x005fu)
object NinetyFirst : Location(0x005bu)
object NinetyFourth : Location(0x005eu)
object NinetyNineth : Location(0x0063u)
object NinetySecond : Location(0x005cu)
object NinetySeventh : Location(0x0061u)
object NinetySixth : Location(0x0060u)
object NinetyThird : Location(0x005du)
object OneHundredAndEighteenth : Location(0x0076u)
object OneHundredAndEighth : Location(0x006cu)
object OneHundredAndEightyEighth : Location(0x00bcu)
object OneHundredAndEightyFifth : Location(0x00b9u)
object OneHundredAndEightyFirst : Location(0x00b5u)
object OneHundredAndEightyFourth : Location(0x00b8u)
object OneHundredAndEightyNineth : Location(0x00bdu)
object OneHundredAndEightySecond : Location(0x00b6u)
object OneHundredAndEightySeventh : Location(0x00bbu)
object OneHundredAndEightySixth : Location(0x00bau)
object OneHundredAndEightyThird : Location(0x00b7u)
object OneHundredAndEleventh : Location(0x006fu)
object OneHundredAndFifteenth : Location(0x0073u)
object OneHundredAndFifth : Location(0x0069u)
object OneHundredAndFiftyEighth : Location(0x009eu)
object OneHundredAndFiftyFifth : Location(0x009bu)
object OneHundredAndFiftyFirst : Location(0x0097u)
object OneHundredAndFiftyFourth : Location(0x009au)
object OneHundredAndFiftyNineth : Location(0x009fu)
object OneHundredAndFiftySecond : Location(0x0098u)
object OneHundredAndFiftySeventh : Location(0x009du)
object OneHundredAndFiftySixth : Location(0x009cu)
object OneHundredAndFiftyThird : Location(0x0099u)
object OneHundredAndFirst : Location(0x0065u)
object OneHundredAndFourteenth : Location(0x0072u)
object OneHundredAndFourth : Location(0x0068u)
object OneHundredAndFourtyEighth : Location(0x0094u)
object OneHundredAndFourtyFifth : Location(0x0091u)
object OneHundredAndFourtyFirst : Location(0x008du)
object OneHundredAndFourtyFourth : Location(0x0090u)
object OneHundredAndFourtyNineth : Location(0x0095u)
object OneHundredAndFourtySecond : Location(0x008eu)
object OneHundredAndFourtySeventh : Location(0x0093u)
object OneHundredAndFourtySixth : Location(0x0092u)
object OneHundredAndFourtyThird : Location(0x008fu)
object OneHundredAndNineteenth : Location(0x0077u)
object OneHundredAndNineth : Location(0x006du)
object OneHundredAndNinetyEighth : Location(0x00c6u)
object OneHundredAndNinetyFifth : Location(0x00c3u)
object OneHundredAndNinetyFirst : Location(0x00bfu)
object OneHundredAndNinetyFourth : Location(0x00c2u)
object OneHundredAndNinetyNineth : Location(0x00c7u)
object OneHundredAndNinetySecond : Location(0x00c0u)
object OneHundredAndNinetySeventh : Location(0x00c5u)
object OneHundredAndNinetySixth : Location(0x00c4u)
object OneHundredAndNinetyThird : Location(0x00c1u)
object OneHundredAndSecond : Location(0x0066u)
object OneHundredAndSeventeenth : Location(0x0075u)
object OneHundredAndSeventh : Location(0x006bu)
object OneHundredAndSeventyEighth : Location(0x00b2u)
object OneHundredAndSeventyFifth : Location(0x00afu)
object OneHundredAndSeventyFirst : Location(0x00abu)
object OneHundredAndSeventyFourth : Location(0x00aeu)
object OneHundredAndSeventyNineth : Location(0x00b3u)
object OneHundredAndSeventySecond : Location(0x00acu)
object OneHundredAndSeventySeventh : Location(0x00b1u)
object OneHundredAndSeventySixth : Location(0x00b0u)
object OneHundredAndSeventyThird : Location(0x00adu)
object OneHundredAndSixteenth : Location(0x0074u)
object OneHundredAndSixth : Location(0x006au)
object OneHundredAndSixtyEighth : Location(0x00a8u)
object OneHundredAndSixtyFifth : Location(0x00a5u)
object OneHundredAndSixtyFirst : Location(0x00a1u)
object OneHundredAndSixtyFourth : Location(0x00a4u)
object OneHundredAndSixtyNineth : Location(0x00a9u)
object OneHundredAndSixtySecond : Location(0x00a2u)
object OneHundredAndSixtySeventh : Location(0x00a7u)
object OneHundredAndSixtySixth : Location(0x00a6u)
object OneHundredAndSixtyThird : Location(0x00a3u)
object OneHundredAndTenth : Location(0x006eu)
object OneHundredAndThird : Location(0x0067u)
object OneHundredAndThirteenth : Location(0x0071u)
object OneHundredAndThirtyEighth : Location(0x008au)
object OneHundredAndThirtyFifth : Location(0x0087u)
object OneHundredAndThirtyFirst : Location(0x0083u)
object OneHundredAndThirtyFourth : Location(0x0086u)
object OneHundredAndThirtyNineth : Location(0x008bu)
object OneHundredAndThirtySecond : Location(0x0084u)
object OneHundredAndThirtySeventh : Location(0x0089u)
object OneHundredAndThirtySixth : Location(0x0088u)
object OneHundredAndThirtyThird : Location(0x0085u)
object OneHundredAndTwelveth : Location(0x0070u)
object OneHundredAndTwentyEighth : Location(0x0080u)
object OneHundredAndTwentyFifth : Location(0x007du)
object OneHundredAndTwentyFirst : Location(0x0079u)
object OneHundredAndTwentyFourth : Location(0x007cu)
object OneHundredAndTwentyNineth : Location(0x0081u)
object OneHundredAndTwentySecond : Location(0x007au)
object OneHundredAndTwentySeventh : Location(0x007fu)
object OneHundredAndTwentySixth : Location(0x007eu)
object OneHundredAndTwentyThird : Location(0x007bu)
object OneHundredEightieth : Location(0x00b4u)
object OneHundredFiftieth : Location(0x0096u)
object OneHundredFortieth : Location(0x008cu)
object OneHundredNinetieth : Location(0x00beu)
object OneHundredSeventieth : Location(0x00aau)
object OneHundredSixtieth : Location(0x00a0u)
object OneHundredThirtieth : Location(0x0082u)
object OneHundredTwentieth : Location(0x0078u)
object OneHundredth : Location(0x0064u)
object Outside : Location(0x010Cu)
object Right : Location(0x010Eu)
object Second : Location(0x0002u)
object Seventeenth : Location(0x0011u)
object Seventh : Location(0x0007u)
object Seventieth : Location(0x0046u)
object SeventyEighth : Location(0x004eu)
object SeventyFifth : Location(0x004bu)
object SeventyFirst : Location(0x0047u)
object SeventyFourth : Location(0x004au)
object SeventyNineth : Location(0x004fu)
object SeventySecond : Location(0x0048u)
object SeventySeventh : Location(0x004du)
object SeventySixth : Location(0x004cu)
object SeventyThird : Location(0x0049u)
object Sixteenth : Location(0x0010u)
object Sixth : Location(0x0006u)
object Sixtieth : Location(0x003cu)
object SixtyEighth : Location(0x0044u)
object SixtyFifth : Location(0x0041u)
object SixtyFirst : Location(0x003du)
object SixtyFourth : Location(0x0040u)
object SixtyNineth : Location(0x0045u)
object SixtySecond : Location(0x003eu)
object SixtySeventh : Location(0x0043u)
object SixtySixth : Location(0x0042u)
object SixtyThird : Location(0x003fu)
object Supplementary : Location(0x0109u)
object Tenth : Location(0x000au)
object Third : Location(0x0003u)
object Thirteenth : Location(0x000du)
object Thirtieth : Location(0x001eu)
object ThirtyEighth : Location(0x0026u)
object ThirtyFifth : Location(0x0023u)
object ThirtyFirst : Location(0x001fu)
object ThirtyFourth : Location(0x0022u)
object ThirtyNineth : Location(0x0027u)
object ThirtySecond : Location(0x0020u)
object ThirtySeventh : Location(0x0025u)
object ThirtySixth : Location(0x0024u)
object ThirtyThird : Location(0x0021u)
object Top : Location(0x0102u)
object Twelveth : Location(0x000cu)
object Twentieth : Location(0x0014u)
object TwentyEighth : Location(0x001cu)
object TwentyFifth : Location(0x0019u)
object TwentyFirst : Location(0x0015u)
object TwentyFourth : Location(0x0018u)
object TwentyNineth : Location(0x001du)
object TwentySecond : Location(0x0016u)
object TwentySeventh : Location(0x001bu)
object TwentySixth : Location(0x001au)
object TwentyThird : Location(0x0017u)
object TwoHundredAndEighteenth : Location(0x00dau)
object TwoHundredAndEighth : Location(0x00d0u)
object TwoHundredAndEleventh : Location(0x00d3u)
object TwoHundredAndFifteenth : Location(0x00d7u)
object TwoHundredAndFifth : Location(0x00cdu)
object TwoHundredAndFiftyFifth : Location(0x00ffu)
object TwoHundredAndFiftyFirst : Location(0x00fbu)
object TwoHundredAndFiftyFourth : Location(0x00feu)
object TwoHundredAndFiftySecond : Location(0x00fcu)
object TwoHundredAndFiftyThird : Location(0x00fdu)
object TwoHundredAndFirst : Location(0x00c9u)
object TwoHundredAndFourteenth : Location(0x00d6u)
object TwoHundredAndFourth : Location(0x00ccu)
object TwoHundredAndFourtyEighth : Location(0x00f8u)
object TwoHundredAndFourtyFifth : Location(0x00f5u)
object TwoHundredAndFourtyFirst : Location(0x00f1u)
object TwoHundredAndFourtyFourth : Location(0x00f4u)
object TwoHundredAndFourtyNineth : Location(0x00f9u)
object TwoHundredAndFourtySecond : Location(0x00f2u)
object TwoHundredAndFourtySeventh : Location(0x00f7u)
object TwoHundredAndFourtySixth : Location(0x00f6u)
object TwoHundredAndFourtyThird : Location(0x00f3u)
object TwoHundredAndNineteenth : Location(0x00dbu)
object TwoHundredAndNineth : Location(0x00d1u)
object TwoHundredAndSecond : Location(0x00cau)
object TwoHundredAndSeventeenth : Location(0x00d9u)
object TwoHundredAndSeventh : Location(0x00cfu)
object TwoHundredAndSixteenth : Location(0x00d8u)
object TwoHundredAndSixth : Location(0x00ceu)
object TwoHundredAndTenth : Location(0x00d2u)
object TwoHundredAndThird : Location(0x00cbu)
object TwoHundredAndThirteenth : Location(0x00d5u)
object TwoHundredAndThirtyEighth : Location(0x00eeu)
object TwoHundredAndThirtyFifth : Location(0x00ebu)
object TwoHundredAndThirtyFirst : Location(0x00e7u)
object TwoHundredAndThirtyFourth : Location(0x00eau)
object TwoHundredAndThirtyNineth : Location(0x00efu)
object TwoHundredAndThirtySecond : Location(0x00e8u)
object TwoHundredAndThirtySeventh : Location(0x00edu)
object TwoHundredAndThirtySixth : Location(0x00ecu)
object TwoHundredAndThirtyThird : Location(0x00e9u)
object TwoHundredAndTwelveth : Location(0x00d4u)
object TwoHundredAndTwentyEighth : Location(0x00e4u)
object TwoHundredAndTwentyFifth : Location(0x00e1u)
object TwoHundredAndTwentyFirst : Location(0x00ddu)
object TwoHundredAndTwentyFourth : Location(0x00e0u)
object TwoHundredAndTwentyNineth : Location(0x00e5u)
object TwoHundredAndTwentySecond : Location(0x00deu)
object TwoHundredAndTwentySeventh : Location(0x00e3u)
object TwoHundredAndTwentySixth : Location(0x00e2u)
object TwoHundredAndTwentyThird : Location(0x00dfu)
object TwoHundredFiftieth : Location(0x00fau)
object TwoHundredFortieth : Location(0x00f0u)
object TwoHundredThirtieth : Location(0x00e6u)
object TwoHundredTwentieth : Location(0x00dcu)
object TwoHundredth : Location(0x00c8u)
object Unknown : Location(0x0000u)
object Upper : Location(0x0104u)