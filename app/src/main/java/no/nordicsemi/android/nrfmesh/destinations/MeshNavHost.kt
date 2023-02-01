package no.nordicsemi.android.nrfmesh.destinations

/*
@Composable
fun MeshNavHost(
navController: NavHostController,
onNavigateToDestination: (MeshNavigationDestination, String) -> Unit,
onBackPressed: () -> Unit,
modifier: Modifier = Modifier,
startDestination: String = NodesDestination.route
) {
NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination
) {
    nodesGraph()
    groupsGraph()
    proxyFilterGraph()
    settingsGraph(
        navigateToExportNetwork = {
            onNavigateToDestination(ExportDestination, ExportDestination.route)
        },
        navigateToProvisioners = {
            onNavigateToDestination(ProvisionersDestination, ProvisionersDestination.route)
        },
        navigateToNetworkKeys = {
            onNavigateToDestination(NetworkKeysDestination, NetworkKeysDestination.route)
        },
        navigateToApplicationKeys = {},
        navigateToScenes = {},
        nestedGraphs = {
            exportGraph(onBackPressed = onBackPressed)
            provisionersGraph(
                onBackPressed = onBackPressed,
                onNavigateToProvisioner = { provisionerUuid ->
                    onNavigateToDestination(
                        ProvisionerDestination,
                        ProvisionerDestination.createNavigationRoute(
                            provisionerUuid = provisionerUuid
                        )
                    )
                }
            )
            networkKeysGraph(
                onBackPressed = onBackPressed,
                onNavigateToNetworkKey = { netKeyIndex ->
                    onNavigateToDestination(
                        NetworkKeyDestination,
                        NetworkKeyDestination.createNavigationRoute(
                            netKeyIndexArg = netKeyIndex
                        )
                    )
                }
            )
            applicationKeysGraph(
                onBackPressed = onBackPressed,
                onNavigateToApplicationKey = { appKeyIndex ->
                    onNavigateToDestination(
                        ApplicationKeyDestination,
                        ApplicationKeyDestination.createNavigationRoute(
                            appKeyIndexArg = appKeyIndex
                        )
                    )
                }
            )
            scenesGraph(
                onBackPressed = onBackPressed,
                onNavigateToScene = { sceneNumber ->
                    onNavigateToDestination(
                        SceneDestination,
                        SceneDestination.createNavigationRoute(
                            sceneNumberArg = sceneNumber
                        )
                    )
                }
            )
        }
    )
}
}
*/