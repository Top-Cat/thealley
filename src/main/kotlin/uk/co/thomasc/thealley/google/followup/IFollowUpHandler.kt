package uk.co.thomasc.thealley.google.followup

fun interface IFollowUpHandler {
    abstract fun invoke(result: IFollowUp)
}
