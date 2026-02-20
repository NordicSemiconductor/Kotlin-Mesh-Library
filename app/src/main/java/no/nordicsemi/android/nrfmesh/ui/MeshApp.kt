package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import no.nordicsemi.android.nrfmesh.ui.network.NetworkScreenKey
import no.nordicsemi.android.nrfmesh.ui.network.networkScreenEntry
import no.nordicsemi.android.nrfmesh.ui.network.wizard.NetworkWizardKey
import no.nordicsemi.android.nrfmesh.ui.network.wizard.networkWizardEntry

@Composable
fun MeshApp() {
    val bStack = rememberNavBackStack(NetworkScreenKey)
    NavDisplay(
        backStack = bStack,
        entryProvider = entryProvider {
            networkScreenEntry(
                navigateToWizard = {
                    // Add the wizard to the back stack as the latest screen to be displayed
                    bStack.add(NetworkWizardKey)
                    // Remove the NetworkScreenKey from the backstack as navigating to the wizard
                    // should not allow going back
                    bStack.remove(NetworkScreenKey)
                }
            )
            networkWizardEntry(
                navigateToNetwork = {
                    bStack.add(NetworkScreenKey)
                    bStack.remove(NetworkWizardKey)
                }
            )
        }
    )
}