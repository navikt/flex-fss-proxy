package no.nav.helse.flex.fss.proxy.inntektskomponenten

data class HentInntekterRequest(

    val ident: Ident,
    val ainntektsfilter: String,
    val formaal: String,
    val maanedFom: String,
    val maanedTom: String
)

data class Ident(

    val identifikator: String,
    val aktoerType: String
)

data class HentInntekterResponse(
    val arbeidsInntektMaaned: List<ArbeidsInntektMaaned> = emptyList(),
    val ident: Ident
)

data class Virksomhet(
    val identifikator: String,
    val aktoerType: String
)

data class InntektListe(
    val inntektType: String,
    val virksomhet: Virksomhet,
)

data class ArbeidsforholdFrilanser(
    val arbeidsforholdstype: String,
)

data class ArbeidsInntektMaaned(
    val aarMaaned: String,
    val arbeidsInntektInformasjon: ArbeidsInntektInformasjon
)

data class ArbeidsInntektInformasjon(
    val inntektListe: List<InntektListe> = emptyList(),
    val arbeidsforholdListe: List<ArbeidsforholdFrilanser> = emptyList()
)
