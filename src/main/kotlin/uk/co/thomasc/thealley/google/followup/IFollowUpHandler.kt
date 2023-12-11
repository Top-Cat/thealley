package uk.co.thomasc.thealley.google.followup

fun interface IFollowUpHandler {
    fun invoke(result: IFollowUp)
}
