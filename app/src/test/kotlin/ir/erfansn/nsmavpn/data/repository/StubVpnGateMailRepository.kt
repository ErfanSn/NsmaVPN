/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.erfansn.nsmavpn.data.repository

import java.net.URL

class StubVpnGateMailRepository : VpnGateMailRepository {

    lateinit var mirrorLinks: List<URL>

    override suspend fun isSubscribedToDailyMail(emailAddress: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun obtainMirrorSiteAddresses(emailAddress: String, lastDay: Int): List<URL> {
        return if (lastDay > 10) {
            throw NoVpnGateSubscriptionException(emailAddress)
        } else {
            mirrorLinks
        }
    }
}