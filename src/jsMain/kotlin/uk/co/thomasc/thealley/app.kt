import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.id
import react.Props
import react.createElement
import react.dom.div
import react.dom.render
import react.fc
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import uk.co.thomasc.thealley.home

fun main() {
    window.onload = {
        document.getElementById("root")?.let { root ->
            render(root) {
                app { }
            }
        }
    }
}

val app = fc<Props> {
    BrowserRouter {
        Routes {
            Route {
                attrs.path = "/"
                attrs.element = createElement(home)
            }
            Route {
                attrs.path = "*"
                attrs.element = createElement(notFound)
            }
        }
    }
}

val notFound = fc<Props> {
    div {
        attrs.id = "notfound"
        +"Not found"
    }
}
