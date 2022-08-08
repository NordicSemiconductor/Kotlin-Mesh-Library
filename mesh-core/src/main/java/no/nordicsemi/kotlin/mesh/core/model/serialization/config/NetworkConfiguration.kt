package no.nordicsemi.kotlin.mesh.core.model.serialization.config

/**
 * Defines the configuration type to use when serializing a network based on the Mesh Configuration
 * Database.
 */
sealed class NetworkConfiguration {
    /**
     * Specifies that the full network must be exported.
     */
    object Full : NetworkConfiguration()

    /**
     * Specifies that only the given configuration of the network must be exported.
     *
     * @param networkKeysConfig            Configuration of the network keys to be exported.
     * @param applicationKeysConfig        Configuration of the application keys to be exported.
     * @param provisionersConfig           Configuration of the provisioner to be exported.
     * @param nodesConfig                  Configuration of the nodes to be exported.
     * @param groupsConfig                 Configuration of the groups to be exported.
     * @param scenesConfig                 Configuration of the scenes to be exported.
     */
    data class Partial(
        val networkKeysConfig: NetworkKeysConfig = NetworkKeysConfig.All,
        val applicationKeysConfig: ApplicationKeysConfig = ApplicationKeysConfig.All,
        val provisionersConfig: ProvisionersConfig = ProvisionersConfig.All,
        val nodesConfig: NodesConfig = NodesConfig.All(),
        val groupsConfig: GroupsConfig = GroupsConfig.All,
        val scenesConfig: ScenesConfig = ScenesConfig.All
    ) : NetworkConfiguration()
}