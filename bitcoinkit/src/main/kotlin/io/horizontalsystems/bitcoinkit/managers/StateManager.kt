package io.horizontalsystems.bitcoinkit.managers

import io.horizontalsystems.bitcoinkit.core.RealmFactory
import io.horizontalsystems.bitcoinkit.models.KitState
import io.horizontalsystems.bitcoinkit.network.NetworkParameters
import io.horizontalsystems.bitcoinkit.network.RegTest
import io.realm.Realm

class StateManager(private val realmFactory: RealmFactory, val network: NetworkParameters) {

    var apiSynced: Boolean
        get() {
            if (network is RegTest) {
                return true
            }

            return realmFactory.realm.use {
                getKitState(it).apiSynced
            }
        }
        set(value) {
            setKitState { kitState ->
                kitState.apiSynced = value
            }
        }

    private fun getKitState(realm: Realm): KitState {
        return realm.where(KitState::class.java).findFirst() ?: KitState()
    }

    private fun setKitState(setMethod: (KitState) -> Unit) {
        realmFactory.realm.use { realm ->
            val kitState = realm.where(KitState::class.java).findFirst() ?: KitState()

            realm.executeTransaction {
                setMethod(kitState)
                it.insertOrUpdate(kitState)
            }
        }
    }

}
